package fun.vegax.main.listener;

import antidaunleak.api.annotation.Native;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import fun.vegax.VegaXDLC;
import fun.vegax.main.listener.impl.EventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ListenerRepository {
    final List<Listener> listeners = new ArrayList<>();
    
    public void setup() {
        registerListeners(new EventListener());
    }


    public void registerListeners(Listener... listeners) {
        this.listeners.addAll(List.of(listeners));
        Arrays.stream(listeners).forEach(listener -> VegaXDLC.getInstance().getEventManager().register(listener));
    }
}
