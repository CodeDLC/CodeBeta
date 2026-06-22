package fun.vegax.common.discord.utils;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.sun.jna.Structure;
import fun.vegax.common.discord.utils.RPCButton;

public class DiscordVegaXDLCPresence extends Structure {
    public String largeImageKey;
    public String largeImageText;
    public String smallImageText;
    public String smallImageKey;
    public boolean instance;
    public String state;
    public String details;
    public long startTimestamp;
    public long endTimestamp;
    public String partyId;
    public int partySize;
    public int partyMax;
    public int partyPrivacy;
    public String matchSecret;
    public String joinSecret;
    public String spectateSecret;
    public String button_label_1;
    public String button_url_1;
    public String button_label_2;
    public String button_url_2;

    public DiscordVegaXDLCPresence() {
        this.setStringEncoding("UTF-8");
    }

    protected List<String> getFieldOrder() {
        return Arrays.asList("state", "details", "startTimestamp", "endTimestamp", "largeImageKey", "largeImageText", "smallImageKey", "smallImageText", "partyId", "partySize", "partyMax", "partyPrivacy", "matchSecret", "joinSecret", "spectateSecret", "button_label_1", "button_url_1", "button_label_2", "button_url_2", "instance");
    }

    public static class Builder {
        private final DiscordVegaXDLCPresence vortexPresence = new DiscordVegaXDLCPresence();

        public Builder setSmallImage(String var1) {
            return this.setSmallImage(var1, "");
        }

        public Builder setDetails(String var1) {
            if (var1 != null && !var1.isEmpty()) {
                this.vortexPresence.details = var1.substring(0, Math.min(var1.length(), 128));
            }

            return this;
        }

        public Builder setLargeImage(String var1, String var2) {
            this.vortexPresence.largeImageKey = var1;
            this.vortexPresence.largeImageText = var2;
            return this;
        }

        public Builder setState(String var1) {
            if (var1 != null && !var1.isEmpty()) {
                this.vortexPresence.state = var1.substring(0, Math.min(var1.length(), 128));
            }

            return this;
        }

        public Builder setInstance(boolean var1) {
            if ((this.vortexPresence.button_label_1 == null || !this.vortexPresence.button_label_1.isEmpty()) && (this.vortexPresence.button_label_2 == null || !this.vortexPresence.button_label_2.isEmpty())) {
                this.vortexPresence.instance = var1;
            }
            return this;
        }

        public Builder setPartyId(String var1) {
            this.vortexPresence.partyId = var1;
            return this;
        }

        public Builder setPartySize(int var1) {
            this.vortexPresence.partySize = var1;
            return this;
        }

        public Builder setPartyMax(int var1) {
            this.vortexPresence.partyMax = var1;
            return this;
        }

        public Builder setPartyPrivacy(int var1) {
            this.vortexPresence.partyPrivacy = var1;
            return this;
        }

        public Builder setSmallImage(String var1, String var2) {
            this.vortexPresence.smallImageKey = var1;
            this.vortexPresence.smallImageText = var2;
            return this;
        }

        public Builder setButtons(List<RPCButton> buttons) {
            if (buttons != null && !buttons.isEmpty()) {
                int var2 = Math.min(buttons.size(), 2);
                this.vortexPresence.button_label_1 = buttons.get(0).getLabel();
                this.vortexPresence.button_url_1 = buttons.get(0).getUrl();
                if (var2 == 2) {
                    this.vortexPresence.button_label_2 = buttons.get(1).getLabel();
                    this.vortexPresence.button_url_2 = buttons.get(1).getUrl();
                }
            }

            return this;
        }

        public Builder setStartTimestamp(OffsetDateTime var1) {
            this.vortexPresence.startTimestamp = var1.toEpochSecond();
            return this;
        }

        public Builder setSecrets(String var1, String var2, String var3) {
            if ((this.vortexPresence.button_label_1 == null || !this.vortexPresence.button_label_1.isEmpty()) && (this.vortexPresence.button_label_2 == null || !this.vortexPresence.button_label_2.isEmpty())) {
                this.vortexPresence.matchSecret = var1;
                this.vortexPresence.joinSecret = var2;
                this.vortexPresence.spectateSecret = var3;
            }
            return this;
        }

        public Builder setStartTimestamp(long var1) {
            this.vortexPresence.startTimestamp = var1;
            return this;
        }

        public Builder setSecrets(String var1, String var2) {
            if ((this.vortexPresence.button_label_1 == null || !this.vortexPresence.button_label_1.isEmpty()) && (this.vortexPresence.button_label_2 == null || !this.vortexPresence.button_label_2.isEmpty())) {
                this.vortexPresence.joinSecret = var1;
                this.vortexPresence.spectateSecret = var2;
            }
            return this;
        }

        public Builder setEndTimestamp(long var1) {
            this.vortexPresence.endTimestamp = var1;
            return this;
        }

        public Builder setEndTimestamp(OffsetDateTime var1) {
            this.vortexPresence.endTimestamp = var1.toEpochSecond();
            return this;
        }

        public Builder setLargeImage(String var1) {
            return this.setLargeImage(var1, "");
        }

        public DiscordVegaXDLCPresence build() {
            return this.vortexPresence;
        }
    }
}
