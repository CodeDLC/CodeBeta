package fun.vegax.features.impl.combat;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import fun.vegax.features.module.Module;
import fun.vegax.features.module.ModuleCategory;
import fun.vegax.utils.client.Instance;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NoInteract extends Module {
    public static NoInteract getInstance() {
        return Instance.get(NoInteract.class);
    }

    public NoInteract() {
        super("NoInteract", "No Interact", ModuleCategory.COMBAT);
    }
}
