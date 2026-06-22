package fun.vegax.commands;

import fun.vegax.utils.client.managers.api.command.ICommandSystem;
import fun.vegax.utils.client.managers.api.command.argparser.IArgParserManager;
import fun.vegax.commands.argparser.ArgParserManager;

public enum CommandSystem implements ICommandSystem {
    INSTANCE;

    @Override
    public IArgParserManager getParserManager() {
        return ArgParserManager.INSTANCE;
    }
}
