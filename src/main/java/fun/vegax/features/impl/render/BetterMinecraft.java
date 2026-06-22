package fun.vegax.features.impl.render;

import antidaunleak.api.annotation.Native;
import fun.vegax.events.packet.PacketEvent;
import fun.vegax.features.impl.combat.Aura;
import fun.vegax.features.module.Module;
import fun.vegax.features.module.ModuleCategory;
import fun.vegax.features.module.setting.implement.BooleanSetting;
import fun.vegax.utils.client.Instance;
import fun.vegax.utils.client.managers.event.EventHandler;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BetterMinecraft extends Module {

    public static BetterMinecraft getInstance() {
        return Instance.get(BetterMinecraft.class);
    }

    BooleanSetting betterButton = new BooleanSetting("Кастомные кнопки", "язаипалсяэтопаститьспасите")
            .setValue(true);
    BooleanSetting tabVanishButton = new BooleanSetting("Спектаторы в табе", "язаипалсяэтопаститьспасите")
            .setValue(true);

    public BetterMinecraft() {
        super("BetterMinecraft", "Better Minecraft", ModuleCategory.RENDER);
        setup(betterButton, tabVanishButton);
    }

}
