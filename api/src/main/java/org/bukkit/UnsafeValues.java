package org.bukkit;

import com.google.common.collect.Multimap;
import org.bukkit.advancement.Advancement;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.CreativeCategory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.PluginDescriptionFile;

/**
 * This interface provides value conversions that may be specific to a
 * runtime, or have arbitrary meaning (read: magic values).
 * <p>
 * Their existence and behavior is not guaranteed across future versions. They
 * may be poorly named, throw exceptions, have misleading parameters, or any
 * other bad programming practice.
 */
@Deprecated
public interface UnsafeValues {
    // Paper start
    net.kyori.adventure.text.flattener.ComponentFlattener componentFlattener();
    @Deprecated(forRemoval = true) net.kyori.adventure.text.serializer.plain.PlainComponentSerializer plainComponentSerializer();
    @Deprecated(forRemoval = true) net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer plainTextSerializer();
    @Deprecated(forRemoval = true) net.kyori.adventure.text.serializer.gson.GsonComponentSerializer gsonComponentSerializer();
    @Deprecated(forRemoval = true) net.kyori.adventure.text.serializer.gson.GsonComponentSerializer colorDownsamplingGsonComponentSerializer();
    @Deprecated(forRemoval = true) net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer legacyComponentSerializer();
    // Paper end

    void reportTimings(); // Paper
    Material toLegacy(Material material);

    Material fromLegacy(Material material);

    Material fromLegacy(MaterialData material);

    Material fromLegacy(MaterialData material, boolean itemPriority);

    BlockData fromLegacy(Material material, byte data);

    Material getMaterial(String material, int version);

    int getDataVersion();

    ItemStack modifyItemStack(ItemStack stack, String arguments);

    void checkSupported(PluginDescriptionFile pdf) throws InvalidPluginException;

    byte[] processClass(PluginDescriptionFile pdf, String path, byte[] clazz);

    /**
     * Load an advancement represented by the specified string into the server.
     * The advancement format is governed by Minecraft and has no specified
     * layout.
     * <br>
     * It is currently a JSON object, as described by the Minecraft Wiki:
     * http://minecraft.gamepedia.com/Advancements
     * <br>
     * Loaded advancements will be stored and persisted across server restarts
     * and reloads.
     * <br>
     * Callers should be prepared for {@link Exception} to be thrown.
     *
     * @param key the unique advancement key
     * @param advancement representation of the advancement
     * @return the loaded advancement or null if an error occurred
     */
    Advancement loadAdvancement(NamespacedKey key, String advancement);

    /**
     * Delete an advancement which was loaded and saved by
     * {@link #loadAdvancement(org.bukkit.NamespacedKey, java.lang.String)}.
     * <br>
     * This method will only remove advancement from persistent storage. It
     * should be accompanied by a call to {@link Server#reloadData()} in order
     * to fully remove it from the running instance.
     *
     * @param key the unique advancement key
     * @return true if a file matching this key was found and deleted
     */
    boolean removeAdvancement(NamespacedKey key);

    Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(Material material, EquipmentSlot slot);

    CreativeCategory getCreativeCategory(Material material);

    // Paper start
    /**
     * Server name to report to timings v2
     * @return name
     */
    String getTimingsServerName();

    /**
     * Called once by the version command on first use, then cached.
     */
    default com.destroystokyo.paper.util.VersionFetcher getVersionFetcher() {
        return new com.destroystokyo.paper.util.VersionFetcher.DummyVersionFetcher();
    }

    boolean isSupportedApiVersion(String apiVersion);

    static boolean isLegacyPlugin(org.bukkit.plugin.Plugin plugin) {
        return !Bukkit.getUnsafe().isSupportedApiVersion(plugin.getDescription().getAPIVersion());
    }

    byte[] serializeItem(ItemStack item);

    ItemStack deserializeItem(byte[] data);

    /**
     * Return the translation key for the Material, so the client can translate it into the active
     * locale when using a {@link net.kyori.adventure.text.TranslatableComponent}.
     * @return the translation key
     */
    String getTranslationKey(Material mat);

    /**
     * Return the translation key for the Block, so the client can translate it into the active
     * locale when using a {@link net.kyori.adventure.text.TranslatableComponent}.
     * @return the translation key
     */
    String getTranslationKey(org.bukkit.block.Block block);

    /**
     * Return the translation key for the EntityType, so the client can translate it into the active
     * locale when using a {@link net.kyori.adventure.text.TranslatableComponent}.<br>
     * This is <code>null</code>, when the EntityType isn't known to NMS (custom entities)
     * @return the translation key
     */
    String getTranslationKey(org.bukkit.entity.EntityType type);

    /**
     * Return the translation key for the ItemStack, so the client can translate it into the active
     * locale when using a {@link net.kyori.adventure.text.TranslatableComponent}.<br>
     * @return the translation key
     */
    String getTranslationKey(ItemStack itemStack);

    /**
     * Creates and returns the next EntityId available.
     * <p>
     * Use this when sending custom packets, so that there are no collisions on the client or server.
     */
    public int nextEntityId();

    /**
     * Gets the server-backed registry for a type.
     *
     * @param classOfT type
     * @param <T> type
     * @return the server-backed registry
     * @throws IllegalArgumentException if there isn't a registry for that type
     */
    <T extends Keyed> @org.jetbrains.annotations.NotNull Registry<T> registryFor(Class<T> classOfT);

    /**
     * Just don't use it.
     */
    @org.jetbrains.annotations.NotNull String getMainLevelName();

    /**
     * Gets the item rarity of a material. The material <b>MUST</b> be an item.
     * Use {@link Material#isItem()} before this.
     *
     * @param material the material to get the rarity of
     * @return the item rarity
     */
    public io.papermc.paper.inventory.ItemRarity getItemRarity(Material material);

    /**
     * Gets the item rarity of the itemstack. The rarity can change based on enchantements.
     *
     * @param itemStack the itemstack to get the rarity of
     * @return the itemstack rarity
     */
    public io.papermc.paper.inventory.ItemRarity getItemStackRarity(ItemStack itemStack);
    // Paper end
}
