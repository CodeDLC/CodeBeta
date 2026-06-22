package fun.vegax.features.impl.combat;

import fun.vegax.VegaXDLC;
import fun.vegax.events.player.TickEvent;
import fun.vegax.features.module.Module;
import fun.vegax.features.module.ModuleCategory;
import fun.vegax.features.module.setting.implement.ValueSetting;
import fun.vegax.utils.client.managers.event.EventHandler;
import fun.vegax.utils.interactions.inv.InventoryTask;
import fun.vegax.utils.interactions.item.ItemToolkit;
import fun.vegax.utils.math.script.Script;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Hand;

public class AutoGApple extends Module {

    private final Script script = new Script();

    private final ValueSetting minHp = new ValueSetting(
            "Минимальное хп",
            "Минимальное хп для поедания",
            1.0, 1.0, 20.0
    );

    public AutoGApple() {
        super("AutoGApple", "Auto GApple", ModuleCategory.COMBAT);
        setup(minHp);
    }

    @EventHandler
    public void onTick(TickEvent e) {
        script.update();

        // Если AutoTotem есть в базе — раскомментируй и добавь класс AutoTotem
        // AutoTotem autoTotem = VegaXDLC.getInstance().getModuleRepository().get(AutoTotem.class);
        // if (autoTotem != null && autoTotem.isState() && autoTotem.trigger()) return;

        float health = mc.player.getHealth() + mc.player.getAbsorptionAmount();
        float maxHealth = mc.player.getMaxHealth();

        if (health >= maxHealth) return;
        if (health > (float) minHp.getValue()) return;

        Slot slot = InventoryTask.getSlot(s -> s.getStack().isOf(Items.GOLDEN_APPLE));
        if (slot == null) return;

        swapAndEat(slot);
    }

    private boolean swapAndEat(Slot slot) {
        ItemStack stack = slot.getStack();
        if (mc.player.getItemCooldownManager().isCoolingDown(stack)) return false;

        if (!mc.player.getOffHandStack().getItem().equals(stack.getItem())) {
            if (script.isFinished()) {
                InventoryTask.swapHand(slot, Hand.OFF_HAND, true, true);
                script.cleanup().addTickStep(0, () -> InventoryTask.swapHand(slot, Hand.OFF_HAND, true, true));
            }
        } else {
            ItemToolkit.INSTANCE.useHand(Hand.OFF_HAND);
        }
        return true;
    }

    @Override
    public void deactivate() {
        script.cleanup();
        super.deactivate();
    }
}