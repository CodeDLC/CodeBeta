package fun.vegax.common.discord.callbacks;

import com.sun.jna.Callback;
import fun.vegax.common.discord.utils.DiscordUser;

public interface JoinRequestCallback extends Callback {
    void apply(DiscordUser var1);
}
