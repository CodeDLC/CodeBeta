package fun.vegax.features.impl.misc;

import antidaunleak.api.annotation.Native;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.text.Text;

import fun.vegax.utils.client.managers.event.EventHandler;
import fun.vegax.features.module.Module;
import fun.vegax.features.module.ModuleCategory;
import fun.vegax.features.module.setting.implement.BindSetting;
import fun.vegax.features.module.setting.implement.MultiSelectSetting;
import fun.vegax.features.module.setting.implement.SelectSetting;
import fun.vegax.features.module.setting.implement.SliderSettings;
import fun.vegax.common.repository.friend.FriendUtils;
import fun.vegax.utils.client.packet.network.Network;
import fun.vegax.events.keyboard.KeyEvent;
import fun.vegax.events.player.TickEvent;
import fun.vegax.display.hud.Notifications;
import fun.vegax.display.hud.StaffList;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AutoLeave extends Module {

    SelectSetting leaveType = new SelectSetting("Тип выхода", "Позволяет выбрать тип выхода")
            .value("Hub", "Main Menu").selected("Main Menu");

    MultiSelectSetting triggerSetting = new MultiSelectSetting("Триггеры", "Выберите, в каких случаях произойдет выход")
            .value("Players", "Staff", "Bind").selected("Players", "Staff");

    SliderSettings distanceSetting = new SliderSettings("Максимальная дистанция", "Максимальная дистанция для активации авто-выхода")
            .setValue(10).range(5, 40).visible(() -> triggerSetting.isSelected("Players"));

    BindSetting bindSetting = new BindSetting("Кнопка (Bind)", "Кнопка для моментального выхода")
            .visible(() -> triggerSetting.isSelected("Bind"));

    public AutoLeave() {
        super("AutoLeave", "Auto Leave", ModuleCategory.MISC);
        setup(leaveType, triggerSetting, distanceSetting, bindSetting);
    }

    @EventHandler
    public void onKey(KeyEvent e) {
        if (mc.player == null || mc.world == null) return;
        if (!triggerSetting.isSelected("Bind")) return;
        if (!e.isKeyDown(bindSetting.getKey())) return;

        leave(Text.of("Нажата кнопка выхода"));
    }

    @EventHandler
    public void onTick(TickEvent e) {
        if (Network.isPvp()) return;

        if (triggerSetting.isSelected("Players"))
            mc.world.getPlayers().stream().filter(p -> mc.player.distanceTo(p) < distanceSetting.getValue() && mc.player != p && !FriendUtils.isFriend(p))
                    .findFirst().ifPresent(p -> leave(p.getName().copy().append(" - Появился рядом " + mc.player.distanceTo(p) + "м")));
        if (triggerSetting.isSelected("Staff") && !StaffList.getInstance().list.isEmpty())
            leave(Text.of("Стафф на сервере"));
    }

    public void leave(Text text) {
        switch (leaveType.getSelected()) {
            case "Hub" -> {
                Notifications.getInstance().addList(Text.of("[AutoLeave] ").copy().append(text), 10000);
                mc.getNetworkHandler().sendChatCommand("hub");
            }
            case "Main Menu" ->
                    mc.getNetworkHandler().getConnection().disconnect(Text.of("[Auto Leave] \n").copy().append(text));
        }
        setState(false);
    }
}