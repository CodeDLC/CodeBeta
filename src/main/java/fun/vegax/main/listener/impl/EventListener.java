package fun.vegax.main.listener.impl;

import fun.vegax.utils.interactions.inv.InventoryFlowManager;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import fun.vegax.utils.client.managers.event.EventHandler;
import fun.vegax.utils.client.managers.api.draggable.AbstractDraggable;
import fun.vegax.utils.client.packet.network.Network;
import fun.vegax.VegaXDLC;
import fun.vegax.main.listener.Listener;
import fun.vegax.events.item.UsingItemEvent;
import fun.vegax.events.packet.PacketEvent;
import fun.vegax.events.player.TickEvent;

public class EventListener implements Listener {
    public static boolean serverSprint;
    public static int selectedSlot;

    @EventHandler
    public void onTick(TickEvent e) {
        Network.tick();
        VegaXDLC.getInstance().getAttackPerpetrator().tick();
        InventoryFlowManager.tick();
        VegaXDLC.getInstance().getDraggableRepository().draggable().forEach(AbstractDraggable::tick);
    }

    @EventHandler
    public void onPacket(PacketEvent e) {
        switch (e.getPacket()) {
            case ClientCommandC2SPacket command -> serverSprint = switch (command.getMode()) {
                case ClientCommandC2SPacket.Mode.START_SPRINTING -> true;
                case ClientCommandC2SPacket.Mode.STOP_SPRINTING -> false;
                default -> serverSprint;
            };
            case UpdateSelectedSlotC2SPacket slot -> selectedSlot = slot.getSelectedSlot();
            default -> {}
        }
        Network.packet(e);
        VegaXDLC.getInstance().getAttackPerpetrator().onPacket(e);
        VegaXDLC.getInstance().getDraggableRepository().draggable().forEach(drag -> drag.packet(e));
    }

    @EventHandler
    public void onUsingItemEvent(UsingItemEvent e) {
        VegaXDLC.getInstance().getAttackPerpetrator().onUsingItem(e);
    }
}
