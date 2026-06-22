package fun.vegax.mixins.game.world;

import fun.vegax.utils.interactions.interact.PlayerInteractionHelper;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import fun.vegax.utils.client.managers.event.EventManager;
import fun.vegax.utils.display.interfaces.QuickImports;
import fun.vegax.events.player.EntitySpawnEvent;
import fun.vegax.events.render.WorldLoadEvent;

@Mixin(ClientWorld.class)
public class ClientWorldMixin implements QuickImports {

    @Inject(method = "<init>", at = @At("RETURN"))
    public void initHook(CallbackInfo info) {
        EventManager.callEvent(new WorldLoadEvent());
    }

    @Inject(method = "addEntity", at = @At("HEAD"), cancellable = true)
    public void addEntityHook(Entity entity, CallbackInfo ci) {
        if (PlayerInteractionHelper.nullCheck()) return;
        EntitySpawnEvent event = new EntitySpawnEvent(entity);
        EventManager.callEvent(event);
        if (event.isCancelled()) ci.cancel();
    }
}
