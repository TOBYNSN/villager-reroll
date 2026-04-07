package sk.leo.villagerreroll;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.village.Merchant;
import sk.leo.villagerreroll.accessor.VillagerDataAccessor;
import sk.leo.villagerreroll.mixin.MerchantScreenHandlerAccessor;
import sk.leo.villagerreroll.network.RerollAvailability;
import sk.leo.villagerreroll.network.RerollAvailabilityRequest;
import sk.leo.villagerreroll.network.RerollTradesPayload;

public class VillagerReroll implements ModInitializer {
    public static final String MOD_ID = "villager_reroll";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /**
     * Checks if the villager can still be rerolled.
     * Reroll is allowed only if:
     * 1. The villager has never traded before.
     * 2. The villager is still a Novice (level 1).
     *
     * @param villager The villager entity to check.
     * @return true if reroll is allowed, false otherwise.
     */
    public static boolean isRerollAllowed(net.minecraft.entity.passive.VillagerEntity villager) {
        if (villager == null) return false;
        
        // Check if the villager has ever traded
        boolean hasTradedOnce = ((VillagerDataAccessor) villager).villager_reroll$hasTradedOnce();
        if (hasTradedOnce) return false;
        
        // Check villager level - only Novice (level 1) can reroll
        int level = villager.getVillagerData().getLevel();
        if (level > 1) return false;

        // Check experience - must be 0
        if (villager.getExperience() > 0) return false;
        
        return true;
    }

    /**
     * Checks if the merchant's trades can be rerolled based on current offers.
     * Reroll button should only be clickable if all trade offers are exhausted (uses == 0).
     *
     * @param offers The list of trade offers to check.
     * @return true if all offers are exhausted, false otherwise.
     */
    public static boolean canReroll(net.minecraft.village.TradeOfferList offers) {
        if (offers == null || offers.isEmpty()) {
            return false;
        }
        return offers.stream().allMatch(offer -> offer.getUses() == 0);
    }

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Villager Reroll...");

        PayloadTypeRegistry.playC2S().register(RerollTradesPayload.ID, RerollTradesPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(RerollAvailabilityRequest.ID, RerollAvailabilityRequest.CODEC);
        PayloadTypeRegistry.playS2C().register(RerollAvailability.ID, RerollAvailability.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(RerollTradesPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                LOGGER.info("Player {} requested trade reroll", context.player().getName().getString());
                
                if (context.player().currentScreenHandler instanceof net.minecraft.screen.MerchantScreenHandler merchantHandler) {
                    Merchant merchant = ((MerchantScreenHandlerAccessor) merchantHandler).getMerchant();

                    if (merchant instanceof net.minecraft.entity.passive.VillagerEntity villager) {
                        if (!isRerollAllowed(villager)) {
                            // Deny on server and inform client
                            ServerPlayNetworking.send(context.player(), new RerollAvailability(false));
                            return;
                        }
                        // Perform reroll and sync offers
                        villager.setOffers(null);
                        net.minecraft.village.TradeOfferList newOffers = merchant.getOffers();
                        context.player().sendTradeOffers(
                            merchantHandler.syncId, 
                            newOffers, 
                            1, 
                            merchant.getExperience(), 
                            true, 
                            true
                        );
                        // Inform client reroll is still available until first successful trade occurs
                        ServerPlayNetworking.send(context.player(), new RerollAvailability(true));
                    }
                }
            });
        });

        // Client asks whether reroll is available for the currently open merchant
        ServerPlayNetworking.registerGlobalReceiver(RerollAvailabilityRequest.ID, (payload, context) -> {
            context.server().execute(() -> {
                boolean available = false;
                if (context.player().currentScreenHandler instanceof net.minecraft.screen.MerchantScreenHandler merchantHandler) {
                    Merchant merchant = ((MerchantScreenHandlerAccessor) merchantHandler).getMerchant();
                    if (merchant instanceof net.minecraft.entity.passive.VillagerEntity villager) {
                        available = isRerollAllowed(villager);
                    }
                }
                ServerPlayNetworking.send(context.player(), new RerollAvailability(available));
            });
        });
    }
}
