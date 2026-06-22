package fun.vegax.features.impl.combat;

import fun.vegax.events.packet.PacketEvent;
import fun.vegax.events.player.TickEvent;
import fun.vegax.features.module.Module;
import fun.vegax.features.module.ModuleCategory;
import fun.vegax.features.module.setting.implement.BooleanSetting;
import fun.vegax.features.module.setting.implement.SelectSetting;
import fun.vegax.features.module.setting.implement.ValueSetting;
import fun.vegax.utils.client.managers.event.EventHandler;
import fun.vegax.utils.interactions.interact.PlayerInteractionHelper;
import fun.vegax.utils.interactions.inv.InventoryTask;
import fun.vegax.utils.math.script.Script;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;

import java.util.Comparator;

public class AutoMace extends Module {

    // ── Общие настройки ──────────────────────────────────────────────────────

    private final SelectSetting mode = new SelectSetting("Режим", "Режим работы")
            .value("Обычный", "Элитра")
            .selected("Обычный");

    // ── Настройки Элитра режима ───────────────────────────────────────────────

    private final ValueSetting searchRange = new ValueSetting(
            "Дальность цели", "Макс. XZ дистанция до цели",
            8.0, 1.0, 30.0
    ).visible(() -> mode.getSelected().equals("Элитра"));

    private final ValueSetting strikeHeight = new ValueSetting(
            "Высота удара", "На сколько блоков выше цели нужно быть",
            3.0, 1.0, 15.0
    ).visible(() -> mode.getSelected().equals("Элитра"));

    private final BooleanSetting autoLaunch = new BooleanSetting(
            "Авто взлёт", "При включении надеть элитры, прыгнуть и использовать фейерверк"
    ).visible(() -> mode.getSelected().equals("Элитра"));

    private final BooleanSetting swapBack = new BooleanSetting(
            "Вернуть броню", "Вернуть нагрудник после удара булавой"
    ).setValue(true).visible(() -> mode.getSelected().equals("Элитра"));

    // ── Состояние ─────────────────────────────────────────────────────────────

    private boolean isAttacking = false;
    private boolean hasMaceHit = false;
    private int savedChestplateSlot = -1;
    private final Script launchScript = new Script();

    public AutoMace() {
        super("AutoMace", "Auto Mace", ModuleCategory.COMBAT);
        setup(mode, searchRange, strikeHeight, autoLaunch, swapBack);
    }

    @Override
    public void activate() {
        isAttacking = false;
        hasMaceHit = false;
        savedChestplateSlot = -1;
        launchScript.cleanup();

        if (mode.getSelected().equals("Элитра") && autoLaunch.isValue()) {
            scheduleLaunch();
        }
    }

    @Override
    public void deactivate() {
        isAttacking = false;
        hasMaceHit = false;
        launchScript.cleanup();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  ОБЫЧНЫЙ РЕЖИМ — перехватываем атаку и подменяем на булаву
    // ══════════════════════════════════════════════════════════════════════════

    @EventHandler
    public void onPacket(PacketEvent e) {
        if (!mode.getSelected().equals("Обычный")) return;
        if (mc.player == null || mc.world == null) return;
        if (!e.isSend()) return;

        Packet<?> packet = e.getPacket();
        if (!(packet instanceof PlayerInteractEntityC2SPacket)) return;
        if (isAttacking) return;

        int maceSlot = findInHotbar(Items.MACE);
        if (maceSlot == -1) return;

        int currentSlot = mc.player.getInventory().selectedSlot;
        if (maceSlot == currentSlot) return;

        e.cancel();
        isAttacking = true;

        mc.player.getInventory().selectedSlot = maceSlot;
        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(maceSlot));
        mc.player.networkHandler.sendPacket(packet);
        mc.player.getInventory().selectedSlot = currentSlot;
        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(currentSlot));

        isAttacking = false;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  ЭЛИТРА РЕЖИМ — лети, падай, бей булавой сверху
    // ══════════════════════════════════════════════════════════════════════════

    @EventHandler
    public void onTick(TickEvent e) {
        if (!mode.getSelected().equals("Элитра")) return;
        if (mc.player == null || mc.world == null) return;

        launchScript.update();

        if (hasMaceHit) return;

        PlayerEntity target = findNearestTarget();
        if (target == null) return;

        // Условия удара: летим на элитрах + падаем + выше цели на strikeHeight + в XZ дистанции
        if (!mc.player.isGliding()) return;
        if (mc.player.getVelocity().y >= 0) return;

        double heightDiff = mc.player.getY() - target.getY();
        double xzDist = Math.sqrt(
                Math.pow(mc.player.getX() - target.getX(), 2) +
                Math.pow(mc.player.getZ() - target.getZ(), 2)
        );

        if (heightDiff < strikeHeight.getValue()) return;
        if (xzDist > searchRange.getValue()) return;

        strikeWithMace(target);
    }

    private void strikeWithMace(PlayerEntity target) {
        int maceSlot = findInHotbar(Items.MACE);
        if (maceSlot == -1) return;

        hasMaceHit = true;
        int currentSlot = mc.player.getInventory().selectedSlot;

        mc.player.getInventory().selectedSlot = maceSlot;
        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(maceSlot));
        mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(target, mc.player.isSneaking()));
        mc.player.swingHand(Hand.MAIN_HAND);
        mc.player.getInventory().selectedSlot = currentSlot;
        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(currentSlot));

        // Вернуть нагрудник
        if (swapBack.isValue() && savedChestplateSlot != -1) {
            swapBackChestplate();
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  АВТО ВЗЛЁТ
    // ══════════════════════════════════════════════════════════════════════════

    private void scheduleLaunch() {
        launchScript.cleanup()
            .addTickStep(0, () -> {
                // Шаг 1: надеть элитры
                equipElytra();
            })
            .addTickStep(3, () -> {
                // Шаг 2: прыжок
                mc.options.jumpKey.setPressed(true);
            })
            .addTickStep(1, () -> {
                mc.options.jumpKey.setPressed(false);
            })
            .addTickStep(1, () -> {
                // Шаг 3: начать планирование
                PlayerInteractionHelper.startFallFlying();
            })
            .addTickStep(2, () -> {
                // Шаг 4: использовать фейерверк
                useFirework();
                hasMaceHit = false;
            });
    }

    private void equipElytra() {
        // Если уже в элитрах — пропускаем
        if (mc.player.getEquippedStack(net.minecraft.entity.EquipmentSlot.CHEST).isOf(Items.ELYTRA)) return;

        // Ищем элитры в инвентаре
        Slot elytraSlot = InventoryTask.getSlot(s -> s.getStack().isOf(Items.ELYTRA) && !s.getStack().isEmpty());
        if (elytraSlot == null) return;

        // Слот нагрудника в screen handler — id 6
        Slot chestSlot = getChestScreenSlot();
        if (chestSlot == null) return;

        // Сохраняем слот нагрудника для возврата
        savedChestplateSlot = elytraSlot.id;

        // Меняем: берём элитры → кладём в нагрудник → возвращаем нагрудник на место элитр
        InventoryTask.clickSlot(elytraSlot, 0, SlotActionType.PICKUP, false);
        InventoryTask.clickSlot(chestSlot, 0, SlotActionType.PICKUP, false);
        InventoryTask.clickSlot(elytraSlot, 0, SlotActionType.PICKUP, false);
    }

    private void swapBackChestplate() {
        Slot chestSlot = getChestScreenSlot();
        if (chestSlot == null) return;

        // Берём нагрудник с сохранённого слота → кладём в слот брони → возвращаем элитры
        InventoryTask.clickSlot(savedChestplateSlot, 0, SlotActionType.PICKUP, false);
        InventoryTask.clickSlot(chestSlot, 0, SlotActionType.PICKUP, false);
        InventoryTask.clickSlot(savedChestplateSlot, 0, SlotActionType.PICKUP, false);

        savedChestplateSlot = -1;
    }

    private void useFirework() {
        // Приоритет: левая рука
        if (mc.player.getOffHandStack().isOf(Items.FIREWORK_ROCKET)) {
            PlayerInteractionHelper.interactItem(Hand.OFF_HAND);
            return;
        }
        // Хотбар
        int slot = findInHotbar(Items.FIREWORK_ROCKET);
        if (slot == -1) return;

        int cur = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = slot;
        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(slot));
        PlayerInteractionHelper.interactItem(Hand.MAIN_HAND);
        mc.player.getInventory().selectedSlot = cur;
        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(cur));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  ХЕЛПЕРЫ
    // ══════════════════════════════════════════════════════════════════════════

    private PlayerEntity findNearestTarget() {
        return mc.world.getPlayers().stream()
                .filter(p -> p != mc.player)
                .filter(p -> !p.isDead())
                .min(Comparator.comparingDouble(p -> mc.player.distanceTo(p)))
                .orElse(null);
    }

    /** Слот нагрудника — id=6 в PlayerScreenHandler */
    private Slot getChestScreenSlot() {
        return InventoryTask.slots()
                .filter(s -> s.id == 6)
                .findFirst()
                .orElse(null);
    }

    private int findInHotbar(Item item) {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).isOf(item)) return i;
        }
        return -1;
    }
}