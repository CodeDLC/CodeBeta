package fun.vegax.utils.client;

import lombok.experimental.UtilityClass;
import fun.vegax.utils.client.managers.api.draggable.AbstractDraggable;
import fun.vegax.features.module.Module;
import fun.vegax.VegaXDLC;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@UtilityClass
public class Instance {
    private final ConcurrentMap<Class<? extends Module>, Module> instanceModules = new ConcurrentHashMap<>();
    private final ConcurrentMap<Class<? extends AbstractDraggable>, AbstractDraggable> instanceDraggables = new ConcurrentHashMap<>();

    public <T extends Module> T get(Class<T> clazz) {
        return clazz.cast(instanceModules.computeIfAbsent(clazz, instance -> VegaXDLC.getInstance().getModuleProvider().get(instance)));
    }

    public <T extends Module> T get(String module) {
        return VegaXDLC.getInstance().getModuleProvider().get(module);
    }

    public <T extends AbstractDraggable> T getDraggable(Class<T> clazz) {
        return clazz.cast(instanceDraggables.computeIfAbsent(clazz, instance -> VegaXDLC.getInstance().getDraggableRepository().get(instance)));
    }

    public <T extends AbstractDraggable> T getDraggable(String draggable) {
        return VegaXDLC.getInstance().getDraggableRepository().get(draggable);
    }
}
