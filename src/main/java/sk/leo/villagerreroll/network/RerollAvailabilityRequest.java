package sk.leo.villagerreroll.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import sk.leo.villagerreroll.VillagerReroll;

public record RerollAvailabilityRequest() implements CustomPayload {
    public static final Id<RerollAvailabilityRequest> ID = new Id<>(Identifier.of(VillagerReroll.MOD_ID, "reroll_availability_request"));
    public static final PacketCodec<RegistryByteBuf, RerollAvailabilityRequest> CODEC = PacketCodec.unit(new RerollAvailabilityRequest());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
