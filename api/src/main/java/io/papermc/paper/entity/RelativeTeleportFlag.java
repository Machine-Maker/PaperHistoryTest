package io.papermc.paper.entity;

import org.bukkit.Location;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 * Represents coordinates in a teleportation that should be handled relatively.
 *
 * @see org.bukkit.entity.Player#teleport(Location, PlayerTeleportEvent.TeleportCause, boolean, boolean, RelativeTeleportFlag...)
 */
@org.jetbrains.annotations.ApiStatus.Experimental
public enum RelativeTeleportFlag {
    /**
     * Represents the player's X coordinate
     */
    X,
    /**
     * Represents the player's Y coordinate
     */
    Y,
    /**
     * Represents the player's Z coordinate
     */
    Z,
    /**
     * Represents the player's yaw
     */
    YAW,
    /**
     * Represents the player's pitch
     */
    PITCH;

}
