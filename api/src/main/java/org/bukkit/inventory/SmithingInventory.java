package org.bukkit.inventory;

import org.jetbrains.annotations.Nullable;

/**
 * Interface to the inventory of a Smithing table.
 */
public interface SmithingInventory extends Inventory {

    /**
     * Check what item is in the result slot of this smithing table.
     *
     * @return the result item
     */
    @Nullable
    ItemStack getResult();

    /**
     * Set the item in the result slot of the smithing table
     *
     * @param newResult the new result item
     */
    void setResult(@Nullable ItemStack newResult);

    /**
     * Get the current recipe formed on the smithing table, if any.
     *
     * @return the recipe, or null if the current contents don't match any
     * recipe
     */
    @Nullable
    Recipe getRecipe();

    // Paper start
    /**
     * Gets the input equipment (first slot).
     *
     * @return input equipment item
     */
    @Nullable
    default ItemStack getInputEquipment() {
        return getItem(0);
    }

    /**
     * Sets the input equipment (first slot).
     *
     * @param itemStack item to set
     */
    default void setInputEquipment(@Nullable ItemStack itemStack) {
        setItem(0, itemStack);
    }

    /**
     * Gets the input mineral (second slot).
     *
     * @return input mineral item
     */
    @Nullable
    default ItemStack getInputMineral() {
        return getItem(1);
    }

    /**
     * Sets the input mineral (second slot).
     *
     * @param itemStack item to set
     */
    default void setInputMineral(@Nullable ItemStack itemStack) {
        setItem(1, itemStack);
    }
    // Paper end
}
