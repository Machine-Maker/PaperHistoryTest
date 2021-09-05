package org.bukkit.event.player;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Holds information for player teleport events
 */
public class PlayerTeleportEvent extends PlayerMoveEvent {
    private static final HandlerList handlers = new HandlerList();
    private TeleportCause cause = TeleportCause.UNKNOWN;

    // Paper start - Teleport API
    private boolean dismounted = true;
    private final java.util.Set<io.papermc.paper.entity.RelativeTeleportFlag> teleportFlagSet;
    // Paper end

    public PlayerTeleportEvent(@NotNull final Player player, @NotNull final Location from, @Nullable final Location to) {
        super(player, from, to);
        teleportFlagSet = java.util.Collections.emptySet(); // Paper - Teleport API
    }

    public PlayerTeleportEvent(@NotNull final Player player, @NotNull final Location from, @Nullable final Location to, @NotNull final TeleportCause cause) {
        this(player, from, to);

        this.cause = cause;
    }

    // Paper start - Teleport API
    @org.jetbrains.annotations.ApiStatus.Experimental
    public PlayerTeleportEvent(@NotNull final Player player, @NotNull final Location from, @Nullable final Location to, @NotNull final TeleportCause cause, boolean dismounted, @NotNull java.util.Set<io.papermc.paper.entity.@NotNull RelativeTeleportFlag> teleportFlagSet) {
        super(player, from, to);

        this.dismounted = dismounted;
        this.teleportFlagSet = teleportFlagSet;
        this.cause = cause;
    }
    // Paper end

    /**
     * Gets the cause of this teleportation event
     *
     * @return Cause of the event
     */
    @NotNull
    public TeleportCause getCause() {
        return cause;
    }

    public enum TeleportCause {
        /**
         * Indicates the teleporation was caused by a player throwing an Ender
         * Pearl
         */
        ENDER_PEARL,
        /**
         * Indicates the teleportation was caused by a player executing a
         * command
         */
        COMMAND,
        /**
         * Indicates the teleportation was caused by a plugin
         */
        PLUGIN,
        /**
         * Indicates the teleportation was caused by a player entering a
         * Nether portal
         */
        NETHER_PORTAL,
        /**
         * Indicates the teleportation was caused by a player entering an End
         * portal
         */
        END_PORTAL,
        /**
         * Indicates the teleportation was caused by a player teleporting to a
         * Entity/Player via the spectator menu
         */
        SPECTATE,
        /**
         * Indicates the teleportation was caused by a player entering an End
         * gateway
         */
        END_GATEWAY,
        /**
         * Indicates the teleportation was caused by a player consuming chorus
         * fruit
         */
        CHORUS_FRUIT,
        /**
         * Indicates the teleportation was caused by an event not covered by
         * this enum
         */
        UNKNOWN;
    }

    // Paper start - Teleport API
    /**
     * Gets if the player will be dismounted in this teleportation.
     *
     * @return dismounted or not
     */
    @org.jetbrains.annotations.ApiStatus.Experimental
    public boolean willDismountPlayer() {
        return this.dismounted;
    }

    /**
     * Returns the relative teleportation flags used in this teleportation.
     * This determines which axis the player will not lose their velocity in.
     *
     * @return an immutable set of relative teleportation flags
     */
    @org.jetbrains.annotations.ApiStatus.Experimental
    @NotNull
    public java.util.Set<io.papermc.paper.entity.@NotNull RelativeTeleportFlag> getRelativeTeleportationFlags() {
        return this.teleportFlagSet;
    }
    // Paper end

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
