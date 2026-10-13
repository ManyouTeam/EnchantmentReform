package cn.superiormc.enchantmentreform.nms.named;

import cn.superiormc.enchantmentreform.nms.NmsLog;

import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.ResettableClassFileTransformer;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.jar.asm.ClassReader;
import net.bytebuddy.jar.asm.ClassVisitor;
import net.bytebuddy.jar.asm.ClassWriter;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Label;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.utility.JavaModule;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Trident;

import java.lang.instrument.Instrumentation;
import java.util.logging.Logger;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

public final class ArrowWaterResistancePatch {

    static final String TAG = "enchantmentreform:aquatic_arrow";
    private static final String WATER_SYNC_TAG = "enchantmentreform:aquatic_arrow_water_sync";
    private static final String SKIP_SYNC_TAG = "enchantmentreform:aquatic_arrow_skip_sync";
    private static final String TARGET = "net.minecraft.world.entity.projectile.arrow.AbstractArrow";
    private static final String TRACKER = "net.minecraft.server.level.ServerEntity";
    private static Instrumentation instrumentation;
    private static volatile ResettableClassFileTransformer transformer;
    private static boolean attempted;

    private ArrowWaterResistancePatch() {
    }

    public static boolean setEnabled(AbstractArrow arrow, boolean enabled) {
        if (arrow instanceof Trident) {
            return false;
        }
        if (!enabled) {
            arrow.removeScoreboardTag(TAG);
            arrow.removeScoreboardTag(WATER_SYNC_TAG);
            arrow.removeScoreboardTag(SKIP_SYNC_TAG);
            return true;
        }
        if (transformer == null) {
            return false;
        }
        return arrow.getScoreboardTags().contains(TAG) || arrow.addScoreboardTag(TAG);
    }

    public static synchronized boolean install(Logger logger) {
        if (attempted) {
            return transformer != null;
        }
        attempted = true;
        try {
            Class<?> target = Class.forName(TARGET);
            // Fail closed if the named ABI or vanilla flight branches have changed.
            java.lang.reflect.Method tagsMethod;
            try {
                tagsMethod = target.getMethod("entityTags");
            } catch (NoSuchMethodException ignored) {
                tagsMethod = target.getMethod("getTags");
            }
            if (tagsMethod.getReturnType() != java.util.Set.class) {
                throw new IllegalStateException("Unrecognized entity tag accessor");
            }
            String tagsOwner = tagsMethod.getDeclaringClass().getName().replace('.', '/');
            String tagsName = tagsMethod.getName();
            Class<?> tracker = Class.forName(TRACKER);
            tracker.getMethod("sendChanges");
            if (tracker.getDeclaredField("forceStateResync").getType() != boolean.class) {
                throw new IllegalStateException("Unrecognized tracker resync flag");
            }
            Class<?> entity = tracker.getDeclaredField("entity").getType();
            java.lang.reflect.Field velocityField;
            try {
                velocityField = entity.getField("syncVelocity");
            } catch (NoSuchFieldException ignored) {
                velocityField = entity.getField("hurtMarked");
            }
            if (velocityField.getType() != boolean.class) {
                throw new IllegalStateException("Unrecognized velocity sync flag");
            }
            target.getMethod("isInGround");
            String velocityName = velocityField.getName();
            String velocityOwner = velocityField.getDeclaringClass().getName().replace('.', '/');
            String entityDescriptor = "L" + entity.getName().replace('.', '/') + ";";
            int[] checks = {0};
            String resource = "/" + TARGET.replace('.', '/') + ".class";
            try (var input = target.getResourceAsStream(resource)) {
                if (input == null) {
                    throw new IllegalStateException("Arrow class bytes unavailable");
                }
                new ClassReader(input).accept(new ClassVisitor(Opcodes.ASM9) {
                    @Override
                    public MethodVisitor visitMethod(int access, String name, String descriptor,
                                                     String signature, String[] exceptions) {
                        if (!name.equals("tick") || !descriptor.equals("()V")) {
                            return null;
                        }
                        return new MethodVisitor(Opcodes.ASM9) {
                            @Override
                            public void visitMethodInsn(int opcode, String owner, String name,
                                                        String descriptor, boolean isInterface) {
                                if (name.equals("isInWater") && descriptor.equals("()Z")) {
                                    checks[0]++;
                                }
                            }
                        };
                    }
                }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            }
            if (checks[0] < 2) {
                throw new IllegalStateException("Unrecognized arrow water/air flight branches");
            }
            instrumentation = ByteBuddyAgent.install();
            var failure = new java.util.concurrent.atomic.AtomicReference<Throwable>();
            transformer = new AgentBuilder.Default()
                    .disableClassFormatChanges()
                    .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                    .with(new AgentBuilder.Listener.Adapter() {
                        @Override
                        public void onError(String type, ClassLoader loader, JavaModule module,
                                            boolean loaded, Throwable throwable) {
                            failure.set(throwable);
                        }
                    })
                    .type(named(TARGET).or(named(TRACKER)))
                    .transform((builder, type, loader, module, domain) -> {
                        if (type.getName().equals(TRACKER)) {
                            return builder.visit(new AsmVisitorWrapper.ForDeclaredMethods()
                                    .writerFlags(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS)
                                    .method(named("sendChanges").and(takesArguments(0)),
                                            (instrumentedType, method, visitor, context, pool, writer, reader) ->
                                                    trackingVisitor(visitor, tagsOwner, tagsName, entityDescriptor,
                                                            velocityOwner, velocityName)));
                        }
                        return builder.visit(
                            new AsmVisitorWrapper.ForDeclaredMethods()
                                    .writerFlags(ClassWriter.COMPUTE_MAXS)
                                    .method(named("tick").and(takesArguments(0)),
                                            (instrumentedType, method, visitor, context, pool, writer, reader) ->
                                                    new MethodVisitor(Opcodes.ASM9, visitor) {
                                                        @Override
                                                        public void visitMethodInsn(int opcode, String owner,
                                                                                    String name, String descriptor,
                                                                                    boolean isInterface) {
                                                            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                                                            if (name.equals("isInWater") && descriptor.equals("()Z")) {
                                                                // water && !aquatic; no plugin classes are referenced
                                                                // from transformed server bytecode. Water state itself
                                                                // stays intact for extinguishing and other systems.
                                                                super.visitVarInsn(Opcodes.ALOAD, 0);
                                                                super.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                                                                        tagsOwner, tagsName,
                                                                        "()Ljava/util/Set;", false);
                                                                super.visitLdcInsn(TAG);
                                                                super.visitMethodInsn(Opcodes.INVOKEINTERFACE,
                                                                        "java/util/Set", "contains", "(Ljava/lang/Object;)Z", true);
                                                                super.visitInsn(Opcodes.ICONST_1);
                                                                super.visitInsn(Opcodes.IXOR);
                                                                super.visitInsn(Opcodes.IAND);
                                                            }
                                                        }
                                                    }));
                    })
                    .installOn(instrumentation);
            if (failure.get() != null) {
                transformer.reset(instrumentation, AgentBuilder.RedefinitionStrategy.RETRANSFORMATION);
                transformer = null;
                throw new IllegalStateException("Arrow transformation failed", failure.get());
            }
            NmsLog.info(logger, NmsLog.AQUATIC_ARROW, "enabled (flight and client synchronization).");
            NmsLog.debug(logger, NmsLog.AQUATIC_ARROW, "entity tag accessor: " + tagsName + ".");
            return true;
        } catch (Throwable throwable) {
            NmsLog.warning(logger, NmsLog.AQUATIC_ARROW, "unavailable; patch installation failed.", throwable);
            return false;
        }
    }

    private static MethodVisitor trackingVisitor(MethodVisitor visitor, String tagsOwner, String tagsName,
                                                 String entityDescriptor, String velocityOwner, String velocityName) {
        String trackerOwner = TRACKER.replace('.', '/');
        String arrowOwner = TARGET.replace('.', '/');
        return new MethodVisitor(Opcodes.ASM9, visitor) {
            private void loadEntity() {
                super.visitVarInsn(Opcodes.ALOAD, 0);
                super.visitFieldInsn(Opcodes.GETFIELD, trackerOwner, "entity", entityDescriptor);
            }

            private void updateTag(String operation, String tag) {
                loadEntity();
                super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, tagsOwner, tagsName, "()Ljava/util/Set;", false);
                super.visitLdcInsn(tag);
                super.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Set", operation, "(Ljava/lang/Object;)Z", true);
            }

            @Override
            public void visitCode() {
                super.visitCode();
                Label done = new Label();
                Label dry = new Label();
                Label waterSync = new Label();
                Label send = new Label();
                loadEntity();
                super.visitTypeInsn(Opcodes.INSTANCEOF, arrowOwner);
                super.visitJumpInsn(Opcodes.IFEQ, done);
                loadEntity();
                super.visitTypeInsn(Opcodes.CHECKCAST, arrowOwner);
                super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, arrowOwner, "isInGround", "()Z", false);
                super.visitJumpInsn(Opcodes.IFNE, done);
                loadEntity();
                super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, tagsOwner, tagsName, "()Ljava/util/Set;", false);
                super.visitLdcInsn(TAG);
                super.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Set", "contains", "(Ljava/lang/Object;)Z", true);
                super.visitJumpInsn(Opcodes.IFEQ, done);
                loadEntity();
                super.visitTypeInsn(Opcodes.CHECKCAST, arrowOwner);
                super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, arrowOwner, "isInWater", "()Z", false);
                super.visitJumpInsn(Opcodes.IFEQ, dry);
                // First wet tick syncs immediately, then alternate skip/send while submerged.
                // Entity-local markers avoid a global map retaining removed arrows.
                updateTag("add", WATER_SYNC_TAG);
                super.visitJumpInsn(Opcodes.IFNE, waterSync);
                updateTag("remove", SKIP_SYNC_TAG);
                super.visitJumpInsn(Opcodes.IFNE, done);
                super.visitJumpInsn(Opcodes.GOTO, waterSync);
                super.visitLabel(dry);
                updateTag("remove", SKIP_SYNC_TAG);
                super.visitInsn(Opcodes.POP);
                updateTag("remove", WATER_SYNC_TAG);
                super.visitJumpInsn(Opcodes.IFEQ, done);
                // Leaving water gets one correction, then vanilla air tracking takes over.
                super.visitJumpInsn(Opcodes.GOTO, send);
                super.visitLabel(waterSync);
                updateTag("add", SKIP_SYNC_TAG);
                super.visitInsn(Opcodes.POP);
                super.visitLabel(send);
                super.visitVarInsn(Opcodes.ALOAD, 0);
                super.visitInsn(Opcodes.ICONST_1);
                super.visitFieldInsn(Opcodes.PUTFIELD, trackerOwner, "forceStateResync", "Z");
                loadEntity();
                super.visitInsn(Opcodes.ICONST_1);
                super.visitFieldInsn(Opcodes.PUTFIELD, velocityOwner, velocityName, "Z");
                super.visitLabel(done);
            }
        };
    }

    public static synchronized void uninstall(Logger logger) {
        if (transformer != null) {
            if (!transformer.reset(instrumentation, AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)) {
                NmsLog.warning(logger, NmsLog.AQUATIC_ARROW, "restoration failed.");
                return;
            }
            transformer = null;
            NmsLog.info(logger, NmsLog.AQUATIC_ARROW, "restored vanilla behavior.");
        }
        instrumentation = null;
        attempted = false;
    }
}
