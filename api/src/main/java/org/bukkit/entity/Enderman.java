package org.bukkit.entity;

import org.bukkit.block.data.BlockData;
import org.bukkit.material.MaterialData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents an Enderman.
 */
public interface Enderman extends Monster {

    // Paper start
    /**
     * Try to teleport the enderman to a random nearby location.
     *
     * May conditionally fail if the random location was not valid
     * @return If the enderman teleported successfully or not
     */

    public boolean teleportRandomly();
    // Paper end

    /**
     * Gets the id and data of the block that the Enderman is carrying.
     *
     * @return MaterialData containing the id and data of the block
     * @deprecated use {@link #getCarriedBlock()}
     */
    @NotNull
    @Deprecated // Paper
    public MaterialData getCarriedMaterial();

    /**
     * Sets the id and data of the block that the Enderman is carrying.
     *
     * @param material data to set the carried block to
     * @deprecated use {@link #setCarriedBlock(BlockData)}
     */
    @Deprecated // Paper
    public void setCarriedMaterial(@NotNull MaterialData material);

    /**
     * Gets the data of the block that the Enderman is carrying.
     *
     * @return BlockData containing the carried block, or null if none
     */
    @Nullable
    public BlockData getCarriedBlock();

    /**
     * Sets the data of the block that the Enderman is carrying.
     *
     * @param blockData data to set the carried block to, or null to remove
     */
    public void setCarriedBlock(@Nullable BlockData blockData);

    // Paper start
    /**
     * Returns whether the enderman is screaming/angry.
     *
     * @return whether the enderman is screaming
     */
    boolean isScreaming();

    /**
     * Sets whether the enderman is screaming/angry.
     *
     * @param screaming whether the enderman is screaming
     */
    void setScreaming(boolean screaming);

    /**
     * Returns whether the enderman has been stared at.
     * If set to true, players will hear an ambient sound.
     *
     * @return whether the enderman has been stared at
     */
    boolean hasBeenStaredAt();

    /**
     * Sets whether the enderman has been stared at.
     * If set to true, players will hear an ambient sound.
     *
     * @param hasBeenStaredAt whether the enderman has been stared at
     */
    void setHasBeenStaredAt(boolean hasBeenStaredAt);
    // Paper end
}
