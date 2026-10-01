package cn.superiormc.enchantmentreform.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;

import java.util.Collection;
import java.util.List;

public final class PaperCommandRegistrar {

    private PaperCommandRegistrar() {
    }

    public static void bind(BootstrapContext context) {
        context.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event ->
                event.registrar().register(
                        "enchantmentreform",
                        "Open the enchantment catalogue or reload runtime files.",
                        List.of("er", "enchants"),
                        new EnchantmentReformCommand()));
    }

    private static final class EnchantmentReformCommand implements BasicCommand {

        private final MainCommand executor = new MainCommand();
        private final MainCommandTab tabCompleter = new MainCommandTab();

        @Override
        public void execute(CommandSourceStack source, String[] args) {
            executor.execute(source.getSender(), args);
        }

        @Override
        public Collection<String> suggest(CommandSourceStack source, String[] args) {
            return tabCompleter.suggest(source.getSender(), args);
        }
    }
}
