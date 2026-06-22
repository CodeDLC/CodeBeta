package fun.vegax.events.block;

import net.minecraft.util.math.BlockPos;
import fun.vegax.utils.client.managers.event.events.Event;

public record BreakBlockEvent(BlockPos blockPos) implements Event {}
