package sk.leo.villagerreroll.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import sk.leo.villagerreroll.VillagerReroll;

public record RerollAvailability(boolean available) implements CustomPayload {
    public static final Id<RerollAvailability> ID = new Id<>(Identifier.of(VillagerReroll.MOD_ID, "reroll_availability"));
    public static final PacketCodec<RegistryByteBuf, RerollAvailability> CODEC = PacketCodec.tuple(
            PacketCodecs.BOOLEAN, RerollAvailability::available, RerollAvailability::new
    );
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
