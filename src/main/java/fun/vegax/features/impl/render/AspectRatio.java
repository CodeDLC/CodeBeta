package fun.vegax.features.impl.render;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import fun.vegax.utils.client.managers.event.EventHandler;
import fun.vegax.features.module.Module;
import fun.vegax.features.module.ModuleCategory;
import fun.vegax.features.module.setting.implement.SliderSettings;
import fun.vegax.events.render.AspectRatioEvent;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AspectRatio extends Module {

    SliderSettings ratioSetting = new SliderSettings("Соотношение", "Настройка значения соотношения сторон")
            .setValue(1.0F).range(0.1F, 2.0F);


    public AspectRatio() {
        super("AspectRatio", "Aspect Ratio", ModuleCategory.RENDER);
        setup(ratioSetting);
    }

    @EventHandler
    public void onAspectRatio(AspectRatioEvent e) {
            e.setRatio(ratioSetting.getValue());
            e.cancel();
    }
}
