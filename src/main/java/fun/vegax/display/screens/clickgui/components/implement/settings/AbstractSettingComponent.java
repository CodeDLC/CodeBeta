package fun.vegax.display.screens.clickgui.components.implement.settings;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import fun.vegax.features.module.setting.Setting;
import fun.vegax.display.screens.clickgui.components.AbstractComponent;

@Getter
@RequiredArgsConstructor
public abstract class AbstractSettingComponent extends AbstractComponent {
    protected final Setting setting;
}
