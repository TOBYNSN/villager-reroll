package sk.leo.villagerreroll.mixin;

import net.minecraft.entity.mob.ZombieVillagerEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sk.leo.villagerreroll.accessor.VillagerDataAccessor;

@Mixin(ZombieVillagerEntity.class)
public abstract class ZombieVillagerEntityMixin implements VillagerDataAccessor {

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
}
