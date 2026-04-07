package sk.leo.villagerreroll;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import sk.leo.villagerreroll.network.RerollAvailability;

public class VillagerRerollClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(RerollAvailability.ID, (payload, context) -> {
            boolean available = payload.available();
            context.client().execute(() -> {
                net.minecraft.client.MinecraftClient mc = net.minecraft.client.MinecraftClient.getInstance();
                if (mc.currentScreen instanceof net.minecraft.client.gui.screen.ingame.HandledScreen<?> handled &&
                        mc.currentScreen instanceof net.minecraft.client.gui.screen.ingame.MerchantScreen &&
                        mc.currentScreen instanceof sk.leo.villagerreroll.client.ClientRerollBridge bridge) {
                    bridge.villager_reroll$setRerollAvailable(available);
                }
            });
        });
    }
}
