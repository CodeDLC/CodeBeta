package fun.vegax.utils.features.aura.striking;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import fun.vegax.features.module.setting.implement.SelectSetting;
import fun.vegax.utils.display.interfaces.QuickImports;
import fun.vegax.events.item.UsingItemEvent;
import fun.vegax.events.packet.PacketEvent;
import fun.vegax.utils.features.aura.warp.Turns;

import java.util.List;

@Getter
public class StrikerConstructor implements QuickImports {
    StrikeManager attackHandler = new StrikeManager();

    public void tick() {
        attackHandler.tick();
    }

    public void onPacket(PacketEvent e) {
        attackHandler.onPacket(e);
    }

    public void onUsingItem(UsingItemEvent e) {
        attackHandler.onUsingItem(e);
    }

    public void performAttack(AttackPerpetratorConfigurable configurable) {
        attackHandler.handleAttack(configurable);
    }

    @Getter
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public static class AttackPerpetratorConfigurable {
        LivingEntity target;
        Turns angle;
        float maximumRange;
        boolean onlyCritical, shouldBreakShield, shouldUnPressShield, eatAndAttack;
        Box box;
        SelectSetting aimMode;

        public AttackPerpetratorConfigurable(LivingEntity target, Turns angle, float maximumRange, List<String> options, SelectSetting aimMode, Box box) {
            this.target = target;
            this.angle = angle;
            this.maximumRange = maximumRange;
            this.onlyCritical = options.contains("Only Critical");
            this.shouldBreakShield = options.contains("Break Shield");
            this.shouldUnPressShield = options.contains("UnPress Shield");
            this.eatAndAttack = options.contains("No Attack When Eat");
            this.box = box;
            this.aimMode = aimMode;
        }
    }
}
