package fun.vegax.features.impl.movement;

import antidaunleak.api.annotation.Native;
import fun.vegax.events.player.TickEvent;
import fun.vegax.utils.interactions.interact.PlayerInteractionHelper;
import fun.vegax.utils.interactions.inv.InventoryTask;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.item.ItemStack;
import net.minecraft.item.consume.UseAction;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;

import fun.vegax.utils.client.managers.event.EventHandler;
import fun.vegax.utils.client.managers.event.types.EventType;
import fun.vegax.features.module.Module;
import fun.vegax.features.module.ModuleCategory;
import fun.vegax.features.module.setting.implement.SelectSetting;
import fun.vegax.utils.client.Instance;
import fun.vegax.utils.math.time.StopWatch;
import fun.vegax.utils.math.script.Script;
import fun.vegax.events.item.UsingItemEvent;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class NoSlow extends Module {
    public static NoSlow getInstance() {
        return Instance.get(NoSlow.class);
    }

    private final StopWatch notifWatch = new StopWatch();
    private final Script script = new Script();
    private boolean finish;

    public final SelectSetting itemMode = new SelectSetting("Режим предмета", "Выберите режим обхода").value("Grim Old", "SpookyTime", "Grim", "ReallyWorld", "SlothAC");

    public NoSlow() {
        super("NoSlow", "No Slow", ModuleCategory.MOVEMENT);
        setup(itemMode);
    }
    private int ticks = 0;

    @EventHandler
    public void onUpdate(TickEvent event) {
        if (mc.player == null || mc.player.isGliding()) return;

        if (itemMode.getSelected().equals("SpookyTime") || itemMode.getSelected().equals("Grim")) {
            if (mc.player.isUsingItem()) {
                ticks++;
            } else {
                ticks = 0;
            }
        }
        
        // Логика для режима ReallyWorld
        if (itemMode.getSelected().equals("ReallyWorld")) {
            if (!mc.player.isGliding()) {
                if (mc.player.isUsingItem()) {
                    ticks++;
                } else {
                    ticks = 0;
                }
            }
        }
    }

    @EventHandler
    @Native(type = Native.Type.VMProtectBeginUltra)
    public void onUsingItem(UsingItemEvent e) {
        Hand first = mc.player.getActiveHand();
        Hand second = first.equals(Hand.MAIN_HAND) ? Hand.OFF_HAND : Hand.MAIN_HAND;


        switch (e.getType()) {
            case EventType.ON -> {
                switch (itemMode.getSelected()) {
                    case "Grim Old" -> {
                        if (mc.player.getOffHandStack().getUseAction().equals(UseAction.NONE) || mc.player.getMainHandStack().getUseAction().equals(UseAction.NONE)) {
                            PlayerInteractionHelper.interactItem(first);
                            PlayerInteractionHelper.interactItem(second);
                            e.cancel();
                        }
                    }
                    case "SpookyTime" -> {
                        if (ticks < 5) {
                            mc.player.networkHandler.sendPacket(new ClickSlotC2SPacket(
                                    0,
                                    0,
                                    15,
                                    0,
                                    SlotActionType.PICKUP,
                                    ItemStack.EMPTY,
                                    Int2ObjectMaps.emptyMap()
                            ));
                        }
                        if (ticks > 6) e.cancel();
                    }
                    case "Grim" -> {
                        if (ticks < 5) {
                            mc.player.networkHandler.sendPacket(new ClickSlotC2SPacket(
                                    0,
                                    0,
                                    15,
                                    0,
                                    SlotActionType.PICKUP,
                                    ItemStack.EMPTY,
                                    Int2ObjectMaps.emptyMap()
                            ));
                        }
                        if (ticks > 6) e.cancel();
                    }
                    case "ReallyWorld" -> {
                        if (ticks == 1 || ticks == 2) {
                            e.cancel();
                        }
                        if (ticks >= 2) {
                            ticks = 0;
                        }
                        if (ticks == 0) {
                            // Не отменяем событие
                        }
                    }
                    case "SlothAC" -> {
                        Hand active = mc.player.getActiveHand();
                        if (active != null) {
                            Hand opposite = (active == Hand.MAIN_HAND) ? Hand.OFF_HAND : Hand.MAIN_HAND;
                            PlayerInteractionHelper.interactItem(opposite);
                            e.cancel();
                        }
                    }
                }
            }
            case EventType.POST -> {
                while (!script.isFinished()) script.update();
            }
        }
    }
}
