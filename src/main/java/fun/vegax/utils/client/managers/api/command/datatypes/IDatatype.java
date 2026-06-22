package fun.vegax.utils.client.managers.api.command.datatypes;

import fun.vegax.utils.client.managers.api.command.exception.CommandException;
import fun.vegax.utils.display.interfaces.QuickImports;

import java.util.stream.Stream;

public interface IDatatype extends QuickImports {
    Stream<String> tabComplete(IDatatypeContext ctx) throws CommandException;
}
