package fun.vegax.common.discord.callbacks;

import com.sun.jna.Callback;
import fun.vegax.common.discord.utils.DiscordUser;

public interface ReadyCallback extends Callback {
    void apply(DiscordUser var1);
}
