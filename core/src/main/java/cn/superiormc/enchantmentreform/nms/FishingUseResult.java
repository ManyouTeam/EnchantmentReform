package cn.superiormc.enchantmentreform.nms;

public record FishingUseResult(NmsStatus status) {

    public boolean successful() {
        return status == NmsStatus.SUCCESS;
    }
}
