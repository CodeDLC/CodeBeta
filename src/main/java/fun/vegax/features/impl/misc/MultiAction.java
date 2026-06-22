package fun.vegax.features.impl.misc;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import fun.vegax.features.module.Module;
import fun.vegax.features.module.ModuleCategory;
import fun.vegax.utils.client.Instance;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MultiAction extends Module {
    public static MultiAction getInstance() {
        return Instance.get(MultiAction.class);
    }

    public MultiAction() {
        super("MultiAction", "Multi Action", ModuleCategory.MISC);
    }
}
