package cn.superiormc.enchantmentreform.paper.adapts.v26_1;

import cn.superiormc.enchantmentreform.paper.adapts.ComponentCodecRegistry;
import cn.superiormc.enchantmentreform.paper.adapts.VersionCodecs;

public class Paper261Codecs implements VersionCodecs {

    @Override
    public void registerCodecs(ComponentCodecRegistry registry) {
        registry.register(new DamageResistantCodec());
        registry.register(new BlocksAttacksCodec());
    }

}
