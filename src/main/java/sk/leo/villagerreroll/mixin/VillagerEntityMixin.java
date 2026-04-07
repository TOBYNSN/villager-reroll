package sk.leo.villagerreroll.mixin;

import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.village.TradeOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sk.leo.villagerreroll.accessor.VillagerDataAccessor;
import sk.leo.villagerreroll.network.RerollAvailability;

@Mixin(VillagerEntity.class)
public abstract class VillagerEntityMixin implements VillagerDataAccessor {

    @Unique
    private boolean hasTradedOnce = false;

    @Override
    public boolean villager_reroll$hasTradedOnce() {
        return this.hasTradedOnce;
    }

    @Override
    public void villager_reroll$setTradedOnce(boolean value) {
        this.hasTradedOnce = value;
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void writeHasTradedOnce(NbtCompound nbt, CallbackInfo ci) {
        nbt.putBoolean("has_traded_once", this.hasTradedOnce);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void readHasTradedOnce(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("has_traded_once")) {
            this.hasTradedOnce = nbt.getBoolean("has_traded_once");
        }
    }


    @Inject(method = "afterUsing", at = @At("HEAD"))
    private void onAfterUsing(TradeOffer offer, CallbackInfo ci) {
        if (!this.hasTradedOnce) {
            this.hasTradedOnce = true;
            // Aktualizuj klienta, ak je otvorené obchodné GUI s týmto villagerom
            var customer = ((VillagerEntity)(Object)this).getCustomer();
            if (customer instanceof ServerPlayerEntity serverPlayer) {
                // Po prvom trade by sa malo tlačidlo skryť (keďže isRerollAllowed teraz vráti false)
                net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(serverPlayer, new RerollAvailability(false));
            }
        }
    }
}
