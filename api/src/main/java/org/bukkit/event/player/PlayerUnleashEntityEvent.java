package org.bukkit.event.player;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called prior to an entity being unleashed due to a player's action.
 */
public class PlayerUnleashEntityEvent extends EntityUnleashEvent implements Cancellable {
    private final Player player;
    private boolean cancelled = false;

    // Paper start - drop leash variable
    @Deprecated
    public PlayerUnleashEntityEvent(@NotNull Entity entity, @NotNull Player player) {
        this(entity, player, false);
    }

    public PlayerUnleashEntityEvent(@NotNull Entity entity, @NotNull Player player, boolean dropLeash) {
        super(entity, UnleashReason.PLAYER_UNLEASH, dropLeash);
        // Paper end
        this.player = player;
    }

    /**
     * Returns the player who is unleashing the entity.
     *
     * @return The player
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
