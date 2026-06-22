package fun.vegax.main.client;

import fun.vegax.utils.client.chat.StringHelper;

import java.io.File;

public record ClientInfo(String clientName, String userName, String role, File clientDir, File filesDir) implements ClientInfoProvider {

    @Override
    public String getFullInfo() {
        return String.format("Welcome! Client: %s Version: %s Branch: %s", clientName, "CodeDev", StringHelper.getUserRole());
    }

    @Override
    public File configsDir() {
        return filesDir;
    }
}
