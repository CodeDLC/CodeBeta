package fun.vegax.features.impl.player;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import fun.vegax.utils.client.managers.event.EventHandler;
import fun.vegax.features.module.Module;
import fun.vegax.features.module.ModuleCategory;
import fun.vegax.features.module.setting.implement.BooleanSetting;
import fun.vegax.features.module.setting.implement.TextSetting;
import fun.vegax.common.repository.friend.FriendUtils;
import fun.vegax.events.render.TextFactoryEvent;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NameProtect extends Module {
    TextSetting nameSetting = new TextSetting("Имя", "Никнейм, который будет заменен на ваш").setText("Protected").setMax(32);
    BooleanSetting friendsSetting = new BooleanSetting("Друзья", "Скрывает никнеймы друзей").setValue(true);

    public NameProtect() {
        super("NameProtect","Name Protect", ModuleCategory.PLAYER);
        setup(friendsSetting);
    }

    @EventHandler
    public void onTextFactory(TextFactoryEvent e) {
        e.replaceText(mc.getSession().getUsername(), nameSetting.getText());
        if (friendsSetting.isValue()) FriendUtils.getFriends().forEach(friend -> e.replaceText(friend.getName(), nameSetting.getText()));
    }
}
