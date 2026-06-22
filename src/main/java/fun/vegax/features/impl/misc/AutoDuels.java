package fun.vegax.features.impl.misc;

import fun.vegax.events.player.TickEvent;
import fun.vegax.features.module.Module;
import fun.vegax.features.module.ModuleCategory;
import fun.vegax.features.module.setting.implement.SelectSetting;
import fun.vegax.utils.client.managers.event.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AutoDuels extends Module {

    private static final MinecraftClient MC = MinecraftClient.getInstance();
    
    private final SelectSetting prioritySetting = new SelectSetting("Предпочитать", "Приоритет выбора противников")
            .value("Рандом", "Софтеров", "Ансофтеров")
            .selected("Рандом");

    private final SelectSetting kitSetting = new SelectSetting("Кит", "Выбор набора для дуэли")
            .value("Щит", "Шипы 3", "Лук", "Тотем", "НоуДебаф", "Шары", "Классик", "Читерский рай", "Незер")
            .selected("Щит");

    private final List<String> sentPlayers = new ArrayList<>();
    private long lastDuelTime = 0L;

    public AutoDuels() {
        super("AutoDuels", "AutoDuels", ModuleCategory.MISC);
        setup(prioritySetting, kitSetting);
    }

    @EventHandler
    public void onTick(TickEvent e) {
        if (MC.player == null || MC.player.networkHandler == null) return;

        List<String> playerNames = new ArrayList<>();
        for (PlayerListEntry entry : MC.player.networkHandler.getPlayerList()) {
            playerNames.add(entry.getProfile().getName());
        }
        
        if (prioritySetting.isSelected("Рандом")) {
            Collections.shuffle(playerNames);
        } else if (prioritySetting.isSelected("Софтеров")) {
            Collections.reverse(playerNames);
        }
        
        for (String name : playerNames) {
            if (System.currentTimeMillis() - lastDuelTime > 750L
                    && !sentPlayers.contains(name)
                    && !name.equals(MC.player.getNameForScoreboard())) {

                MC.player.networkHandler.sendChatCommand("duel " + name);
                sentPlayers.add(name);
                lastDuelTime = System.currentTimeMillis();
            }
        }
        
        if (MC.player.currentScreenHandler instanceof GenericContainerScreenHandler) {
            String title = MC.currentScreen.getTitle().getString();

            if (title.contains("Выбор набора")) {
                int slotIndex = kitSetting.getList().indexOf(kitSetting.getSelected());
                if (slotIndex != -1) {
                    click(slotIndex);
                }
            } else if (title.contains("Настройка поединка")) {
                click(0);
            }
        }
    }

    private void click(int slot) {
        MC.interactionManager.clickSlot(
                MC.player.currentScreenHandler.syncId,
                slot, 0,
                SlotActionType.PICKUP,
                MC.player
        );
    }

    @Override
    public void activate() {
        sentPlayers.clear();
        lastDuelTime = System.currentTimeMillis();
        super.activate(); //
    }

    @Override
    public void deactivate() {
        sentPlayers.clear();
        super.deactivate(); //
    }
}