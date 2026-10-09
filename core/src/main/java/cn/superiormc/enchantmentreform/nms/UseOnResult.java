package cn.superiormc.enchantmentreform.nms;

public record UseOnResult(NmsStatus status) {

    public boolean successful() {
        return status == NmsStatus.SUCCESS;
    }
}
