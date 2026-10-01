package cn.superiormc.enchantmentreform.skills;

import cn.superiormc.enchantmentreform.EnchantmentReform;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.nio.ByteBuffer;
import java.util.LinkedHashSet;
import java.util.Set;

/** Stores player-placed block positions in chunk PDC so tracking survives restarts. */
public final class PlacedBlockTracker {

    private final NamespacedKey key = new NamespacedKey(
            EnchantmentReform.instance, "skill_placed_blocks");

    public boolean isPlayerPlaced(Block block) {
        return read(block.getChunk()).contains(pack(block));
    }

    public void markPlayerPlaced(Block block) {
        Set<Integer> positions = read(block.getChunk());
        if (positions.add(pack(block))) {
            write(block.getChunk(), positions);
        }
    }

    public void remove(Block block) {
        Set<Integer> positions = read(block.getChunk());
        if (positions.remove(pack(block))) {
            write(block.getChunk(), positions);
        }
    }

    private Set<Integer> read(Chunk chunk) {
        byte[] bytes = chunk.getPersistentDataContainer().get(key, PersistentDataType.BYTE_ARRAY);
        Set<Integer> result = new LinkedHashSet<>();
        if (bytes == null || bytes.length == 0 || bytes.length % Integer.BYTES != 0) {
            return result;
        }
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        while (buffer.remaining() >= Integer.BYTES) {
            result.add(buffer.getInt());
        }
        return result;
    }

    private void write(Chunk chunk, Set<Integer> positions) {
        PersistentDataContainer container = chunk.getPersistentDataContainer();
        if (positions.isEmpty()) {
            container.remove(key);
            return;
        }
        ByteBuffer buffer = ByteBuffer.allocate(positions.size() * Integer.BYTES);
        positions.stream().sorted().forEach(buffer::putInt);
        container.set(key, PersistentDataType.BYTE_ARRAY, buffer.array());
    }

    private int pack(Block block) {
        return (block.getY() & 0x00FFFFFF) << 8
                | (block.getX() & 15) << 4
                | block.getZ() & 15;
    }
}
