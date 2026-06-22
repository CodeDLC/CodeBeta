package fun.vegax.features.impl.render;

import fun.vegax.events.player.TickEvent;
import fun.vegax.features.module.Module;
import fun.vegax.features.module.ModuleCategory;
import fun.vegax.utils.client.managers.event.EventHandler;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public class FullBright extends Module {

    public FullBright() {
        super("FullBright", "FullBright", ModuleCategory.RENDER);
    }

    @Override
    public void deactivate() {
        if (mc.player != null) {
            mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
        }
        super.deactivate();
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (mc.player != null) {
            mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 500, 255, true, false));
        }
    }
}
