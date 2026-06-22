package fun.vegax.utils.client.managers.api.command.datatypes;

import fun.vegax.utils.client.managers.api.command.exception.CommandException;
import fun.vegax.utils.client.managers.api.command.helpers.TabCompleteHelper;
import fun.vegax.common.repository.way.Way;
import fun.vegax.VegaXDLC;

import java.util.List;
import java.util.stream.Stream;

public enum WayDataType implements IDatatypeFor<Way> {
    INSTANCE;

    @Override
    public Stream<String> tabComplete(IDatatypeContext datatypeContext) throws CommandException {
        Stream<String> ways = getWay().stream().map(Way::name);
        String context = datatypeContext.getConsumer().getString();
        return new TabCompleteHelper().append(ways).filterPrefix(context).sortAlphabetically().stream();
    }

    @Override
    public Way get(IDatatypeContext datatypeContext) throws CommandException {
        String text = datatypeContext.getConsumer().getString();
        return getWay().stream().filter(s -> s.name().equalsIgnoreCase(text)).findFirst().orElse(null);
    }

    private List<? extends Way> getWay() {
        return VegaXDLC.getInstance().getWayRepository().wayList;
    }
}
