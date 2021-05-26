package org.bukkit.entity;

import org.bukkit.inventory.meta.FireworkMeta;
import org.jetbrains.annotations.NotNull;

public interface Firework extends Projectile {

    /**
     * Get a copy of the fireworks meta
     *
     * @return A copy of the current Firework meta
     */
    @NotNull
    FireworkMeta getFireworkMeta();

    /**
     * Apply the provided meta to the fireworks
     * <p>
     * Adjusts detonation ticks automatically.
     *
     * @param meta The FireworkMeta to apply
     */
    void setFireworkMeta(@NotNull FireworkMeta meta);

    /**
     * Cause this firework to explode at earliest opportunity, as if it has no
     * remaining fuse.
     */
    void detonate();

    /**
     * Gets if the firework was shot at an angle (i.e. from a crossbow).
     *
     * A firework which was not shot at an angle will fly straight upwards.
     *
     * @return shot at angle status
     */
    boolean isShotAtAngle();

    /**
     * Sets if the firework was shot at an angle (i.e. from a crossbow).
     *
     * A firework which was not shot at an angle will fly straight upwards.
     *
     * @param shotAtAngle the new shotAtAngle
     */
    void setShotAtAngle(boolean shotAtAngle);

    // Paper start
    @org.jetbrains.annotations.Nullable
    public java.util.UUID getSpawningEntity();
    /**
     * If this firework is boosting an entity, return it
     * @return The entity being boosted
     */
    @org.jetbrains.annotations.Nullable
    public LivingEntity getBoostedEntity();
    // Paper end

    // Paper start - Firework API
    /**
     * Gets the item used in the firework.
     *
     * @return firework item
     */
    @NotNull
    public org.bukkit.inventory.ItemStack getItem();

    /**
     * Sets the item used in the firework.
     * <p>
     * Firework explosion effects are used from this item.
     *
     * @param itemStack item to set
     */
    void setItem(@org.jetbrains.annotations.Nullable org.bukkit.inventory.ItemStack itemStack);

    /**
     * Gets the number of ticks the firework has flown.
     *
     * @return ticks flown
     */
    int getTicksFlown();

    /**
     * Sets the number of ticks the firework has flown.
     * Setting this greater than detonation ticks will cause the firework to explode.
     *
     * @param ticks ticks flown
     */
    void setTicksFlown(int ticks);

    /**
     * Gets the number of ticks the firework will detonate on.
     *
     * @return the tick to detonate on
     */
    int getTicksToDetonate();

    /**
     * Set the amount of ticks the firework will detonate on.
     *
     * @param ticks ticks to detonate on
     */
    void setTicksToDetonate(int ticks);
    // Paper stop
}
