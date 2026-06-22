package fun.vegax.mixins.client;

import fun.vegax.events.container.SetScreenEvent;
import fun.vegax.events.player.HotBarUpdateEvent;
import fun.vegax.features.impl.combat.NoInteract;
import fun.vegax.features.impl.misc.MultiAction;
import fun.vegax.features.impl.misc.SelfDestruct;
import fun.vegax.utils.client.logs.Logger;
import fun.vegax.utils.client.managers.event.EventManager;
import fun.vegax.utils.client.managers.file.exception.FileProcessingException;
import fun.vegax.utils.client.sound.SoundManager;
import fun.vegax.utils.client.window.WindowStyle;
import fun.vegax.utils.client.window.WindowTitleAnimation;
import fun.vegax.utils.display.font.Fonts;
import fun.vegax.VegaXDLC;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings({"UnusedMixin", "unused"})
@Environment(EnvType.CLIENT)
@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Shadow @Nullable
    public ClientPlayerInteractionManager interactionManager;

    @Shadow @Nullable
    public ClientPlayerEntity player;

    @Shadow @Final
    public GameRenderer gameRenderer;

    @Unique
    private final WindowTitleAnimation titleAnimation = WindowTitleAnimation.getInstance();

    // ================================
    // Инициализация и завершение
    // ================================

    @Inject(method = "<init>", at = @At("TAIL"))
    private void VegaXDLC$onInit(RunArgs args, CallbackInfo ci) {
        if (SelfDestruct.unhooked) return;

        Fonts.init();
        VegaXDLC$updateWindowTitle();
    }

    @Inject(method = "stop", at = @At("HEAD"))
    private void VegaXDLC$onStop(CallbackInfo ci) {
        if (SelfDestruct.unhooked) return;

        Logger.info("Stopping MinecraftClient");
        SoundManager.playSound(SoundManager.SHUTDOWN);

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        VegaXDLC$saveFiles();
    }

    @Unique
    private void VegaXDLC$saveFiles() {
        VegaXDLC instance = VegaXDLC.getInstance();
        if (instance == null || !instance.isInitialized()) return;

        try {
            instance.getFileController().saveFiles();
        } catch (FileProcessingException e) {
            Logger.error("Error saving files: " + e.getMessage());
        } finally {
            instance.getFileController().stopAutoSave();
        }
    }

    // ================================
    // MultiAction
    // ================================

    @Redirect(
            method = "handleInputEvents",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z")
    )
    private boolean VegaXDLC$onIsUsingItem(ClientPlayerEntity player) {
        if (SelfDestruct.unhooked) return player.isUsingItem();

        if (MultiAction.getInstance().isState()) {
            return false;
        }

        return player.isUsingItem();
    }

    // ================================
    // NoInteract
    // ================================

    @Inject(
            method = "doItemUse",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Hand;values()[Lnet/minecraft/util/Hand;"),
            cancellable = true
    )
    private void VegaXDLC$onItemUse(CallbackInfo ci) {
        if (SelfDestruct.unhooked) return;
        if (player == null || interactionManager == null) return;
        if (!NoInteract.getInstance().isState()) return;

        for (Hand hand : Hand.values()) {
            ItemStack stack = player.getStackInHand(hand);
            if (stack == null || stack.isEmpty()) continue;

            ActionResult result = interactionManager.interactItem(player, hand);
            if (!result.isAccepted()) continue;

            if (result instanceof ActionResult.Success success
                    && success.swingSource() == ActionResult.SwingSource.CLIENT) {
                gameRenderer.firstPersonRenderer.resetEquipProgress(hand);
                player.swingHand(hand);
            }

            ci.cancel();
            return;
        }
    }

    // ================================
    // Screen handling
    // ================================

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    protected void VegaXDLC$onSetScreen(Screen screen, CallbackInfo ci) {
        if (SelfDestruct.unhooked) return;

        VegaXDLC instance = VegaXDLC.getInstance();
        if (instance == null || !instance.isInitialized()) return;

        SetScreenEvent event = new SetScreenEvent(screen);
        EventManager.callEvent(event);

        instance.getDraggableRepository()
                .draggable()
                .forEach(drag -> drag.setScreen(event));

        Screen newScreen = event.getScreen();
        if (screen != newScreen) {
            MinecraftClient.getInstance().setScreen(newScreen);
            ci.cancel();
        }
    }

    // ================================
    // HotBar update event
    // ================================

    @Inject(
            method = "handleInputEvents",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getInventory()Lnet/minecraft/entity/player/PlayerInventory;"),
            cancellable = true
    )
    private void VegaXDLC$onHotBarUpdate(CallbackInfo ci) {
        if (SelfDestruct.unhooked) return;

        HotBarUpdateEvent event = new HotBarUpdateEvent();
        EventManager.callEvent(event);

        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    // ================================
    // Window title & style
    // ================================

    @Inject(method = "tick", at = @At("HEAD"))
    private void VegaXDLC$onTick(CallbackInfo ci) {
        if (SelfDestruct.unhooked) return;

        titleAnimation.updateTitle();
        VegaXDLC$updateWindowTitle();
    }

    @Inject(method = "updateWindowTitle", at = @At("HEAD"), cancellable = true)
    private void VegaXDLC$onUpdateWindowTitle(CallbackInfo ci) {
        if (SelfDestruct.unhooked) return;

        VegaXDLC$updateWindowTitle();
        ci.cancel();
    }

    @Inject(method = "onResolutionChanged", at = @At("TAIL"))
    private void VegaXDLC$onResolutionChanged(CallbackInfo ci) {
        if (SelfDestruct.unhooked) return;

        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            WindowStyle.setDarkMode(MinecraftClient.getInstance().getWindow().getHandle());
        }
    }

    @Unique
    private void VegaXDLC$updateWindowTitle() {
        MinecraftClient.getInstance()
                .getWindow()
                .setTitle(titleAnimation.getCurrentTitle());
    }
}
