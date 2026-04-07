package sk.leo.villagerreroll.mixin.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.MerchantScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.village.TradeOfferList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sk.leo.villagerreroll.VillagerReroll;
import sk.leo.villagerreroll.network.RerollAvailabilityRequest;
import sk.leo.villagerreroll.network.RerollTradesPayload;

@Mixin(HandledScreen.class)
public abstract class MerchantScreenMixin extends Screen implements sk.leo.villagerreroll.client.ClientRerollBridge {

    @Shadow protected int backgroundWidth;
    @Shadow protected int x;
    @Shadow protected int y;
    @Shadow protected ScreenHandler handler;

    @Unique
    private ButtonWidget rerollButton;

    @Unique
    private boolean villager_reroll$available = false;

    protected MerchantScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void addRerollButton(CallbackInfo ci) {
        if (!((Object)this instanceof MerchantScreen)) return;

        int x = this.x + this.backgroundWidth - 30;
        int y = this.y + 10;

        this.rerollButton = ButtonWidget.builder(Text.literal("⟳"), button -> {
            ClientPlayNetworking.send(new RerollTradesPayload());
        })
        .dimensions(x, y, 20, 20)
        .tooltip(net.minecraft.client.gui.tooltip.Tooltip.of(Text.literal("Reroll trades")))
        .build();

        this.addDrawableChild(this.rerollButton);
        // Po otvorení obrazovky si od servera vyžiadaj dostupnosť rerollu
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(new RerollAvailabilityRequest());
        updateButtonVisibility();
    }

    @Inject(method = "handledScreenTick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        updateButtonVisibility();
    }

    @Unique
    private void updateButtonVisibility() {
        if (this.rerollButton == null) return;
        if (!((Object)this instanceof MerchantScreen)) return;
        // Viditeľnosť vychádza zo serverom synchronizovaného stavu
        this.rerollButton.visible = this.villager_reroll$available;
    }
    @Override
    public void villager_reroll$setRerollAvailable(boolean available) {
        this.villager_reroll$available = available;
        updateButtonVisibility();
    }
}
