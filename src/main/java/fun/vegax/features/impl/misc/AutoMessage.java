package fun.vegax.features.impl.misc;

import fun.vegax.events.player.TickEvent;
import fun.vegax.features.impl.combat.Aura;
import fun.vegax.features.module.Module;
import fun.vegax.features.module.ModuleCategory;
import fun.vegax.utils.client.managers.event.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import java.util.Random;
import java.util.List;

public class AutoMessage extends Module {

    private final List<String> phrases = List.of(
            "!- %s Ебанный бездарь, на что ты надеялся?",
            "!- %s На колени перед королем",
            "!- %s Настрой свою килку бездарь",
            "!- %s Сьел все свои яблоки и все равно проебал",
            "!- %s Забыл как хотел отсосать мне хуй?",
            "!- %s Валяется в ногах перед своим богом"
    );

    private PlayerEntity lastTarget = null;
    private final Random random = new Random();

    public AutoMessage() {
        super("AutoMessage", "AutoGG", ModuleCategory.MISC);
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (mc.player == null || mc.world == null) return;

        Aura aura = Aura.getInstance();
        if (aura != null && aura.isState() && aura.getTarget() instanceof PlayerEntity targetPlayer) {
            lastTarget = targetPlayer;
        }

        if (lastTarget != null) {
            boolean isDead = lastTarget.isDead() || lastTarget.getHealth() <= 0 || !mc.world.getPlayers().contains(lastTarget);

            if (isDead) {
                if (mc.player.distanceTo(lastTarget) < 20) {
                    sendDeathMessage(lastTarget.getName().getString());
                }
                lastTarget = null;
            } else if (mc.player.distanceTo(lastTarget) > 30) {
                lastTarget = null;
            }
        }
    }

    private void sendDeathMessage(String name) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;

        String message = phrases.get(random.nextInt(phrases.size()));

        String finalMessage = message.contains("%s") ? String.format(message, name) : message + " " + name;

        mc.getNetworkHandler().sendChatMessage(finalMessage);
    }
}
