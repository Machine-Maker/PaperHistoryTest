package io.papermc.paper.entity;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

/**
 * Represents what part of the entity should be used when determining where to face a location/entity.
 *
 * @see org.bukkit.entity.Player#lookAt(Location, LookAnchor)
 * @see org.bukkit.entity.Player#lookAt(Entity, LookAnchor, LookAnchor)
 */
@org.jetbrains.annotations.ApiStatus.Experimental
public enum LookAnchor {
    /**
     * Represents the entity's feet.
     * @see LivingEntity#getLocation()
     */
    FEET,
    /**
     * Represents the entity's eyes.
     * @see LivingEntity#getEyeLocation()
     */
    EYES;
}
