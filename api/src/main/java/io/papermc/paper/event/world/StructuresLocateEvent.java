package io.papermc.paper.event.world;

import io.papermc.paper.world.structure.ConfiguredStructure;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.world.WorldEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Called <b>before</b> a set of configured structures is located.
 * This happens when:
 * <ul>
 *     <li>The /locate command is used.<br></li>
 *     <li>An Eye of Ender is used.</li>
 *     <li>An Explorer/Treasure Map is activated.</li>
 *     <li>A dolphin swims to a treasure location.</li>
 *     <li>A trade is done with a villager for a map.</li>
 *     <li>{@link World#locateNearestStructure(Location, org.bukkit.StructureType, int, boolean)} is invoked.</li>
 * </ul>
 */
public class StructuresLocateEvent extends WorldEvent implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Location origin;
    private Result result;
    private List<ConfiguredStructure> configuredStructures;
    private int radius;
    private boolean findUnexplored;
    private boolean cancelled;

    public StructuresLocateEvent(@NotNull World world, @NotNull Location origin, @NotNull List<ConfiguredStructure> configuredStructures, int radius, boolean findUnexplored) {
        super(world);
        this.origin = origin;
        this.configuredStructures = configuredStructures;
        this.radius = radius;
        this.findUnexplored = findUnexplored;
    }

    /**
     * Gets the {@link Location} from which the search is to be conducted.
     *
     * @return {@link Location} where search begins
     */
    public @NotNull Location getOrigin() {
        return this.origin;
    }

    /**
     * Gets the {@link Location} and {@link ConfiguredStructure} set as the result, if it was defined.
     * <p>
     * Returns {@code null} if it has not been set by {@link StructuresLocateEvent#setResult(Result)}.
     * Since this event fires <i>before</i> the search is done, the actual result is unknown at this point.
     *
     * @return The result location and structure, if it has been set. null if it has not.
     * @see World#locateNearestStructure(Location, org.bukkit.StructureType, int, boolean)
     */
    public @Nullable Result getResult() {
        return this.result;
    }

    /**
     * Sets the result {@link Location} and {@link ConfiguredStructure}. This causes the search to be
     * skipped, and the result object passed here to be used as the result.
     *
     * @param result the {@link Location} and {@link ConfiguredStructure} of the search.
     */
    public void setResult(@Nullable Result result) {
        this.result = result;
    }

    /**
     * Gets a mutable list of ConfiguredStructures that are valid targets for the search.
     *
     * @return a mutable list of ConfiguredStructures
     */
    public @NotNull List<ConfiguredStructure> getConfiguredStructures() {
        return this.configuredStructures;
    }

    /**
     * Sets the list of ConfiguredStructures that are valid targets for the search.
     *
     * @param configuredStructures a list of ConfiguredStructure targets
     */
    public void setConfiguredStructures(@NotNull List<ConfiguredStructure> configuredStructures) {
        this.configuredStructures = new ArrayList<>(configuredStructures);
    }

    /**
     * Gets the search radius in which to attempt locating the structure.
     * <p>
     * This radius may not always be obeyed during the structure search!
     *
     * @return the search radius.
     */
    public int getRadius() {
        return this.radius;
    }

    /**
     * Sets the search radius in which to attempt locating the structure.
     * <p>
     * This radius may not always be obeyed during the structure search!
     *
     * @param radius the search radius.
     */
    public void setRadius(int radius) {
        this.radius = radius;
    }

    /**
     * Gets whether to search exclusively for unexplored structures.
     * <p>
     * As with the search radius, this value is not always obeyed.
     *
     * @return Whether to search for only unexplored structures.
     */
    public boolean shouldFindUnexplored() {
        return this.findUnexplored;
    }

    /**
     * Sets whether to search exclusively for unexplored structures.
     * <p>
     * As with the search radius, this value is not always obeyed.
     *
     * @param findUnexplored Whether to search for only unexplored structures.
     */
    public void setFindUnexplored(boolean findUnexplored) {
        this.findUnexplored = findUnexplored;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    /**
     * Result for {@link StructuresLocateEvent}.
     */
    public record Result(@NotNull Location position, @NotNull ConfiguredStructure configuredStructure) {
    }
}
