package sk.leo.villagerreroll.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import sk.leo.villagerreroll.VillagerReroll;

public record RerollTradesPayload() implements CustomPayload {
    public static final Id<RerollTradesPayload> ID = new Id<>(Identifier.of(VillagerReroll.MOD_ID, "reroll_trades"));
    public static final PacketCodec<RegistryByteBuf, RerollTradesPayload> CODEC = PacketCodec.unit(new RerollTradesPayload());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
