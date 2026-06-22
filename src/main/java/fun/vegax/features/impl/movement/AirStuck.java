package fun.vegax.features.impl.movement;

import fun.vegax.events.packet.PacketEvent;
import fun.vegax.events.player.InputEvent;
import fun.vegax.events.player.TickEvent;
import fun.vegax.features.module.Module;
import fun.vegax.features.module.ModuleCategory;
import fun.vegax.utils.client.managers.event.EventHandler;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AirStuck extends Module {

    public AirStuck() {
        super("AirStuck", "Air Stuck", ModuleCategory.MOVEMENT);
    }

    @EventHandler
    public void onInput(InputEvent e) {
        if (mc.player == null || mc.world == null) return;

        // Блокируем весь инпут через ивент
        e.inputNone();
    }

    @EventHandler
    public void onTick(TickEvent ignoredE) {
        if (mc.player == null || mc.world == null) return;

        // Полная заморозка
        mc.player.setVelocity(0, 0, 0);
        mc.player.setMovementSpeed(0);
        mc.player.setNoGravity(true);
    }

    @EventHandler
    public void onPacket(PacketEvent e) {
        if (mc.player == null || mc.world == null) return;

        switch (e.getPacket()) {
            case PlayerMoveC2SPacket ignored -> e.cancel();
            case PlayerInteractEntityC2SPacket ignored -> e.cancel();
            case PlayerActionC2SPacket ignored -> e.cancel();
            case HandSwingC2SPacket ignored -> e.cancel();
            case PlayerInputC2SPacket ignored -> e.cancel();
            default -> {}
        }
    }

    @Override
    public void deactivate() {
        if (mc.player != null) {
            mc.player.setNoGravity(false);
        }
    }
}
