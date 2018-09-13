package org.bukkit.event.inventory;

import org.bukkit.block.Block;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.inventory.CookingRecipe;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class FurnaceStartSmeltEvent extends BlockEvent {
    private static final HandlerList handlers = new HandlerList();
    private final ItemStack source;
    private final CookingRecipe<?> recipe;
    private int totalCookTime;

    @Deprecated // Paper - furnace cook speed multiplier
    public FurnaceStartSmeltEvent(@NotNull final Block furnace, @NotNull ItemStack source, @NotNull final CookingRecipe<?> recipe) {
        // Paper start - furnace cook speed multiplier
        this(furnace, source, recipe, recipe.getCookingTime());
    }

    public FurnaceStartSmeltEvent(@NotNull final Block furnace, @NotNull ItemStack source, @NotNull CookingRecipe<?> recipe, int cookingTime) {
        // Paper end
        super(furnace);
        this.source = source;
        this.recipe = recipe;
        this.totalCookTime = cookingTime; // Paper - furnace cook speed multiplier
    }

    /**
     * Gets the source ItemStack for this event
     *
     * @return the source ItemStack
     */
    @NotNull
    public ItemStack getSource() {
        return source;
    }

    /**
     * Gets the FurnaceRecipe associated with this event
     *
     * @return the FurnaceRecipe being cooked
     */
    @NotNull
    public CookingRecipe<?> getRecipe() {
        return recipe;
    }

    /**
     * Gets the total cook time associated with this event
     *
     * @return the total cook time
     */
    public int getTotalCookTime() {
        return totalCookTime;
    }

    /**
     * Sets the total cook time for this event
     *
     * @param cookTime the new total cook time
     */
    public void setTotalCookTime(int cookTime) {
        this.totalCookTime = cookTime;
    }

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
