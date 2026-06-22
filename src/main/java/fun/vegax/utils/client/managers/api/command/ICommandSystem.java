package fun.vegax.utils.client.managers.api.command;

import fun.vegax.utils.client.managers.api.command.argparser.IArgParserManager;

public interface ICommandSystem {
    IArgParserManager getParserManager();
}
