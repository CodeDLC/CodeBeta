package fun.vegax.common.discord;
import antidaunleak.api.UserProfile;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.Identifier;
import fun.vegax.common.discord.utils.*;
import fun.vegax.utils.display.interfaces.QuickImports;
import fun.vegax.utils.client.discord.Buffer;
import fun.vegax.VegaXDLC;
import java.io.IOException;
import java.util.Arrays;

@Setter
@Getter
public class DiscordManager implements QuickImports {
    private final DiscordDaemonThread discordDaemonThread = new DiscordDaemonThread();
    private boolean running = true;
    private DiscordInfo info = new DiscordInfo("Unknown", "", "");
    private Identifier avatarId;

    public void init() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("linux")) {
            return;
        }

        DiscordEventHandlers handlers = new DiscordEventHandlers.Builder()
                .ready((user) -> {
                    VegaXDLC.getInstance().getDiscordManager().setInfo(
                            new DiscordInfo(user.username,
                                    "https://cdn.discordapp.com/avatars/" + user.userId + "/" + user.avatar + ".png",
                                    user.userId));
                    DiscordVegaXDLCPresence vortexPresence = new DiscordVegaXDLCPresence.Builder()
                            .setStartTimestamp(System.currentTimeMillis() / 1000)
                            .setDetails("User: " + UserProfile.getInstance().profile("username"))
                            .setState("Uid: " + UserProfile.getInstance().profile("uid"))
                            .setLargeImage("https://i.postimg.cc/nznMWbhM/0001-0250.gif", "https://vegaxdlc.fun/")
                            .setSmallImage(VegaXDLC.getInstance().getDiscordManager().getInfo().avatarUrl, "https://vegaxdlc.fun/")
                            .setButtons(Arrays.asList(
                                    RPCButton.create("Телеграм", "https://t.me/vegaxdlc"),
                                    RPCButton.create("Дискорд", "https://discord.gg/zYctK4mjZZ")))
                            .build();
                    DiscordRPC.INSTANCE.Discord_UpdatePresence(vortexPresence);
                }).build();
        DiscordRPC.INSTANCE.Discord_Initialize("1493676433858629864", handlers, true, "");
        discordDaemonThread.start();
    }

    public void stopRPC() {
        DiscordRPC.INSTANCE.Discord_Shutdown();
        this.running = false;
    }

    public void load() throws IOException {
        if (avatarId == null && !info.avatarUrl.isEmpty()) {
            avatarId = Buffer.registerDynamicTexture("avatar-", Buffer.getHeadFromURL(info.avatarUrl));
        }
    }

    public Identifier getAvatarId() {
        return avatarId;
    }

    private class DiscordDaemonThread extends Thread {
        @Override
        public void run() {
            this.setName("Discord-RPC");
            try {
                while (VegaXDLC.getInstance().getDiscordManager().isRunning()) {
                    DiscordRPC.INSTANCE.Discord_RunCallbacks();
                    load();
                    Thread.sleep(15000);
                }
            } catch (Exception exception) {
                stopRPC();
            }
            super.run();
        }
    }

    public record DiscordInfo(String userName, String avatarUrl, String userId) {}
}
