package org.bukkit.entity;

import org.bukkit.DyeColor;
import org.jetbrains.annotations.NotNull;

/**
 * Meow.
 */
public interface Cat extends Tameable, Sittable {

    /**
     * Gets the current type of this cat.
     *
     * @return Type of the cat.
     */
    @NotNull
    public Type getCatType();

    /**
     * Sets the current type of this cat.
     *
     * @param type New type of this cat.
     */
    public void setCatType(@NotNull Type type);

    /**
     * Get the collar color of this cat
     *
     * @return the color of the collar
     */
    @NotNull
    public DyeColor getCollarColor();

    /**
     * Set the collar color of this cat
     *
     * @param color the color to apply
     */
    public void setCollarColor(@NotNull DyeColor color);

    /**
     * Represents the various different cat types there are.
     */
    public enum Type {
        TABBY,
        BLACK,
        RED,
        SIAMESE,
        BRITISH_SHORTHAIR,
        CALICO,
        PERSIAN,
        RAGDOLL,
        WHITE,
        JELLIE,
        ALL_BLACK;
    }

    // Paper Start - More cat api
    /**
     * Sets if the cat is lying down.
     * This is visual and does not affect the behaviour of the cat.
     *
     * @param lyingDown whether the cat should lie down
     */
    public void setLyingDown(boolean lyingDown);

    /**
     * Gets if the cat is lying down.
     *
     * @return whether the cat is lying down
     */
    public boolean isLyingDown();

    /**
     * Sets if the cat has its head up.
     * This is visual and does not affect the behaviour of the cat.
     *
     * @param headUp head is up
     */
    public void setHeadUp(boolean headUp);

    /**
     * Gets if the cat has its head up.
     *
     * @return head is up
     */
    public boolean isHeadUp();
    // Paper End - More cat api
}
