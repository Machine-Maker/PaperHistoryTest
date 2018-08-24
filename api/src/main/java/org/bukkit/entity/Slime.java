package org.bukkit.entity;

/**
 * Represents a Slime.
 */
public interface Slime extends Mob {

    /**
     * @return The size of the slime
     */
    public int getSize();

    /**
     * Setting the size of the slime (regardless of previous size)
     * will set the following attributes:
     * <ul>
     *     <li>{@link org.bukkit.attribute.Attribute#GENERIC_MAX_HEALTH}</li>
     *     <li>{@link org.bukkit.attribute.Attribute#GENERIC_MOVEMENT_SPEED}</li>
     *     <li>{@link org.bukkit.attribute.Attribute#GENERIC_ATTACK_DAMAGE}</li>
     * </ul>
     * to their per-size defaults and heal the
     * slime to its max health (assuming it's alive).
     *
     * @param sz The new size of the slime.
     */
    public void setSize(int sz);

    // Paper start
    /**
     * Get whether this slime can randomly wander/jump around on its own
     *
     * @return true if can wander
     */
    public boolean canWander();

    /**
     * Set whether this slime can randomly wander/jump around on its own
     *
     * @param canWander true if can wander
     */
    public void setWander(boolean canWander);
    // Paper end
}
