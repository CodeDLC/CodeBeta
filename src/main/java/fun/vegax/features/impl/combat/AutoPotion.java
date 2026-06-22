package fun.vegax.features.impl.combat;

import fun.vegax.events.player.TickEvent;
import fun.vegax.features.module.Module;
import fun.vegax.features.module.ModuleCategory;
import fun.vegax.features.module.setting.implement.BooleanSetting;
import fun.vegax.features.module.setting.implement.MultiSelectSetting;
import fun.vegax.utils.client.managers.event.EventHandler;
import fun.vegax.utils.math.time.TimerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Hand;

import java.util.Optional;

@SuppressWarnings("All")
public class AutoPotion extends Module {

    private final BooleanSetting autoOff = new BooleanSetting("Авто отключение", "Выключить после использования всех бафов");

    private final MultiSelectSetting potions = new MultiSelectSetting("Бросать", "Какие зелья бросать")
            .value("Силу", "Скорость", "Огнестойкость")
            .selected("Силу", "Скорость", "Огнестойкость");

    private final TimerUtil timer = new TimerUtil();
    private int packetSequence = 0;

    private static final float THROW_PITCH = 90f;

    public AutoPotion() {
        super("AutoPotion", "AutoBuff", ModuleCategory.COMBAT);
        setup(potions, autoOff);
    }

    private enum PotionType {
        STRENGTH(StatusEffects.STRENGTH, "Силу"),
        SPEED(StatusEffects.SPEED, "Скорость"),
        FIRE_RESISTANCE(StatusEffects.FIRE_RESISTANCE, "Огнестойкость");

        final RegistryEntry<StatusEffect> effect;
        final String settingName;

        PotionType(RegistryEntry<StatusEffect> effect, String settingName) {
            this.effect = effect;
            this.settingName = settingName;
        }

        public boolean isEnabled(AutoPotion module) {
            return module.potions.isSelected(this.settingName);
        }
    }

    private boolean isEatingFood() {
        return mc.player.isUsingItem()
                && !mc.player.getActiveItem().isOf(Items.SHIELD)
                && !mc.player.getActiveItem().isOf(Items.BOW)
                && !mc.player.getActiveItem().isOf(Items.TRIDENT);
    }

    private int findPotionSlot(PotionType type) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isOf(Items.SPLASH_POTION)) continue;

            Optional<PotionContentsComponent> comp = Optional.ofNullable(
                    stack.getComponents().get(DataComponentTypes.POTION_CONTENTS)
            );
            if (comp.isEmpty()) continue;

            for (StatusEffectInstance eff : comp.get().getEffects()) {
                if (eff.getEffectType() == type.effect) return i;
            }
        }
        return -1;
    }

    private boolean hasEffect(RegistryEntry<StatusEffect> effect) {
        return mc.player.hasStatusEffect(effect);
    }

    private boolean canBuff(PotionType type) {
        return !hasEffect(type.effect) && type.isEnabled(this) && findPotionSlot(type) != -1;
    }

    private boolean needsBuff() {
        return canBuff(PotionType.STRENGTH)
                || canBuff(PotionType.SPEED)
                || canBuff(PotionType.FIRE_RESISTANCE);
    }

    private boolean canThrow() {
        return !isEatingFood()
                && needsBuff()
                && mc.player.isOnGround()
                && mc.world.getBlockState(mc.player.getBlockPos().down()).getBlock() != Blocks.AIR
                && timer.hasTimeElapsed(500);
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (mc.player == null || mc.world == null) return;

        if (!canThrow()) {
            if (!needsBuff() && autoOff.isValue()) this.switchState();
            return;
        }

        int savedSlot = mc.player.getInventory().selectedSlot;
        float savedPitch = mc.player.getPitch();

        // Ставим питч 90 (вниз) — зелье упадёт под ноги
        mc.player.setPitch(THROW_PITCH);

        throwPotion(PotionType.STRENGTH, savedSlot);
        throwPotion(PotionType.SPEED, savedSlot);
        throwPotion(PotionType.FIRE_RESISTANCE, savedSlot);

        // Восстанавливаем слот и питч
        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(savedSlot));
        mc.player.getInventory().selectedSlot = savedSlot;
        mc.player.setPitch(savedPitch);

        timer.resetCounter();

        if (autoOff.isValue()) this.switchState();
    }

    private void throwPotion(PotionType type, int savedSlot) {
        if (!canBuff(type)) return;

        int slot = findPotionSlot(type);
        if (slot == -1) return;

        mc.player.getInventory().selectedSlot = slot;
        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(slot));
        mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(
                Hand.MAIN_HAND,
                packetSequence++,
                mc.player.getYaw(),
                mc.player.getPitch()
        ));
    }
}