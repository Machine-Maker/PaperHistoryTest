package org.bukkit.craftbukkit.entity;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPosition;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketPlayInCloseWindow;
import net.minecraft.network.protocol.game.PacketPlayOutOpenWindow;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.ITileInventory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumMainHand;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.inventory.Container;
import net.minecraft.world.inventory.Containers;
import net.minecraft.world.item.ItemCooldown;
import net.minecraft.world.item.crafting.CraftingManager;
import net.minecraft.world.item.crafting.IRecipe;
import net.minecraft.world.item.trading.IMerchant;
import net.minecraft.world.level.block.BlockBed;
import net.minecraft.world.level.block.BlockEnchantmentTable;
import net.minecraft.world.level.block.BlockWorkbench;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.state.IBlockData;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.inventory.CraftContainer;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.inventory.CraftInventoryDoubleChest;
import org.bukkit.craftbukkit.inventory.CraftInventoryLectern;
import org.bukkit.craftbukkit.inventory.CraftInventoryPlayer;
import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.inventory.CraftMerchantCustom;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MainHand;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

public class CraftHumanEntity extends CraftLivingEntity implements HumanEntity {
    private CraftInventoryPlayer inventory;
    private final CraftInventory enderChest;
    protected final PermissibleBase perm = new PermissibleBase(this);
    private boolean op;
    private GameMode mode;

    public CraftHumanEntity(final CraftServer server, final EntityHuman entity) {
        super(server, entity);
        mode = server.getDefaultGameMode();
        this.inventory = new CraftInventoryPlayer(entity.getInventory());
        enderChest = new CraftInventory(entity.getEnderChest());
    }

    @Override
    public PlayerInventory getInventory() {
        return inventory;
    }

    @Override
    public EntityEquipment getEquipment() {
        return inventory;
    }

    @Override
    public Inventory getEnderChest() {
        return enderChest;
    }

    @Override
    public MainHand getMainHand() {
        return getHandle().getMainHand() == EnumMainHand.LEFT ? MainHand.LEFT : MainHand.RIGHT;
    }

    @Override
    public ItemStack getItemInHand() {
        return getInventory().getItemInHand();
    }

    @Override
    public void setItemInHand(ItemStack item) {
        getInventory().setItemInHand(item);
    }

    @Override
    public ItemStack getItemOnCursor() {
        return CraftItemStack.asCraftMirror(getHandle().containerMenu.getCarried());
    }

    @Override
    public void setItemOnCursor(ItemStack item) {
        net.minecraft.world.item.ItemStack stack = CraftItemStack.asNMSCopy(item);
        getHandle().containerMenu.setCarried(stack);
        if (this instanceof CraftPlayer) {
            getHandle().containerMenu.broadcastCarriedItem(); // Send set slot for cursor
        }
    }

    @Override
    public int getSleepTicks() {
        return getHandle().sleepCounter;
    }

    @Override
    public boolean sleep(Location location, boolean force) {
        Preconditions.checkArgument(location != null, "Location cannot be null");
        Preconditions.checkArgument(location.getWorld() != null, "Location needs to be in a world");
        Preconditions.checkArgument(location.getWorld().equals(getWorld()), "Cannot sleep across worlds");

        BlockPosition blockposition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        IBlockData iblockdata = getHandle().level.getType(blockposition);
        if (!(iblockdata.getBlock() instanceof BlockBed)) {
            return false;
        }

        if (getHandle().sleep(blockposition, force).left().isPresent()) {
            return false;
        }

        // From BlockBed
        iblockdata = (IBlockData) iblockdata.set(BlockBed.OCCUPIED, true);
        getHandle().level.setTypeAndData(blockposition, iblockdata, 4);

        return true;
    }

    @Override
    public void wakeup(boolean setSpawnLocation) {
        Preconditions.checkState(isSleeping(), "Cannot wakeup if not sleeping");

        getHandle().wakeup(true, setSpawnLocation);
    }

    @Override
    public Location getBedLocation() {
        Preconditions.checkState(isSleeping(), "Not sleeping");

        BlockPosition bed = getHandle().getBedPosition().get();
        return new Location(getWorld(), bed.getX(), bed.getY(), bed.getZ());
    }

    @Override
    public String getName() {
        return getHandle().getName();
    }

    @Override
    public boolean isOp() {
        return op;
    }

    @Override
    public boolean isPermissionSet(String name) {
        return perm.isPermissionSet(name);
    }

    @Override
    public boolean isPermissionSet(Permission perm) {
        return this.perm.isPermissionSet(perm);
    }

    @Override
    public boolean hasPermission(String name) {
        return perm.hasPermission(name);
    }

    @Override
    public boolean hasPermission(Permission perm) {
        return this.perm.hasPermission(perm);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value) {
        return perm.addAttachment(plugin, name, value);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin) {
        return perm.addAttachment(plugin);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks) {
        return perm.addAttachment(plugin, name, value, ticks);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, int ticks) {
        return perm.addAttachment(plugin, ticks);
    }

    @Override
    public void removeAttachment(PermissionAttachment attachment) {
        perm.removeAttachment(attachment);
    }

    @Override
    public void recalculatePermissions() {
        perm.recalculatePermissions();
    }

    @Override
    public void setOp(boolean value) {
        this.op = value;
        perm.recalculatePermissions();
    }

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return perm.getEffectivePermissions();
    }

    @Override
    public GameMode getGameMode() {
        return mode;
    }

    @Override
    public void setGameMode(GameMode mode) {
        if (mode == null) {
            throw new IllegalArgumentException("Mode cannot be null");
        }

        this.mode = mode;
    }

    @Override
    public EntityHuman getHandle() {
        return (EntityHuman) entity;
    }

    public void setHandle(final EntityHuman entity) {
        super.setHandle(entity);
        this.inventory = new CraftInventoryPlayer(entity.getInventory());
    }

    @Override
    public String toString() {
        return "CraftHumanEntity{" + "id=" + getEntityId() + "name=" + getName() + '}';
    }

    @Override
    public InventoryView getOpenInventory() {
        return getHandle().containerMenu.getBukkitView();
    }

    @Override
    public InventoryView openInventory(Inventory inventory) {
        if (!(getHandle() instanceof EntityPlayer)) return null;
        EntityPlayer player = (EntityPlayer) getHandle();
        Container formerContainer = getHandle().containerMenu;

        ITileInventory iinventory = null;
        if (inventory instanceof CraftInventoryDoubleChest) {
            iinventory = ((CraftInventoryDoubleChest) inventory).tile;
        } else if (inventory instanceof CraftInventoryLectern) {
            iinventory = ((CraftInventoryLectern) inventory).tile;
        } else if (inventory instanceof CraftInventory) {
            CraftInventory craft = (CraftInventory) inventory;
            if (craft.getInventory() instanceof ITileInventory) {
                iinventory = (ITileInventory) craft.getInventory();
            }
        }

        if (iinventory instanceof ITileInventory) {
            if (iinventory instanceof TileEntity) {
                TileEntity te = (TileEntity) iinventory;
                if (!te.hasWorld()) {
                    te.setWorld(getHandle().level);
                }
            }
        }

        Containers<?> container = CraftContainer.getNotchInventoryType(inventory);
        if (iinventory instanceof ITileInventory) {
            getHandle().openContainer(iinventory);
        } else {
            openCustomInventory(inventory, player, container);
        }

        if (getHandle().containerMenu == formerContainer) {
            return null;
        }
        getHandle().containerMenu.checkReachable = false;
        return getHandle().containerMenu.getBukkitView();
    }

    private static void openCustomInventory(Inventory inventory, EntityPlayer player, Containers<?> windowType) {
        if (player.connection == null) return;
        Preconditions.checkArgument(windowType != null, "Unknown windowType");
        Container container = new CraftContainer(inventory, player, player.nextContainerCounter());

        container = CraftEventFactory.callInventoryOpenEvent(player, container);
        if (container == null) return;

        String title = container.getBukkitView().getTitle();

        player.connection.sendPacket(new PacketPlayOutOpenWindow(container.containerId, windowType, CraftChatMessage.fromString(title)[0]));
        player.containerMenu = container;
        player.initMenu(container);
    }

    @Override
    public InventoryView openWorkbench(Location location, boolean force) {
        if (location == null) {
            location = getLocation();
        }
        if (!force) {
            Block block = location.getBlock();
            if (block.getType() != Material.CRAFTING_TABLE) {
                return null;
            }
        }
        getHandle().openContainer(((BlockWorkbench) Blocks.CRAFTING_TABLE).getInventory(null, getHandle().level, new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ())));
        if (force) {
            getHandle().containerMenu.checkReachable = false;
        }
        return getHandle().containerMenu.getBukkitView();
    }

    @Override
    public InventoryView openEnchanting(Location location, boolean force) {
        if (location == null) {
            location = getLocation();
        }
        if (!force) {
            Block block = location.getBlock();
            if (block.getType() != Material.ENCHANTING_TABLE) {
                return null;
            }
        }

        // If there isn't an enchant table we can force create one, won't be very useful though.
        BlockPosition pos = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        getHandle().openContainer(((BlockEnchantmentTable) Blocks.ENCHANTING_TABLE).getInventory(null, getHandle().level, pos));

        if (force) {
            getHandle().containerMenu.checkReachable = false;
        }
        return getHandle().containerMenu.getBukkitView();
    }

    @Override
    public void openInventory(InventoryView inventory) {
        if (!(getHandle() instanceof EntityPlayer)) return; // TODO: NPC support?
        if (((EntityPlayer) getHandle()).connection == null) return;
        if (getHandle().containerMenu != getHandle().inventoryMenu) {
            // fire INVENTORY_CLOSE if one already open
            ((EntityPlayer) getHandle()).connection.a(new PacketPlayInCloseWindow(getHandle().containerMenu.containerId));
        }
        EntityPlayer player = (EntityPlayer) getHandle();
        Container container;
        if (inventory instanceof CraftInventoryView) {
            container = ((CraftInventoryView) inventory).getHandle();
        } else {
            container = new CraftContainer(inventory, this.getHandle(), player.nextContainerCounter());
        }

        // Trigger an INVENTORY_OPEN event
        container = CraftEventFactory.callInventoryOpenEvent(player, container);
        if (container == null) {
            return;
        }

        // Now open the window
        Containers<?> windowType = CraftContainer.getNotchInventoryType(inventory.getTopInventory());
        String title = inventory.getTitle();
        player.connection.sendPacket(new PacketPlayOutOpenWindow(container.containerId, windowType, CraftChatMessage.fromString(title)[0]));
        player.containerMenu = container;
        player.initMenu(container);
    }

    @Override
    public InventoryView openMerchant(Villager villager, boolean force) {
        Preconditions.checkNotNull(villager, "villager cannot be null");

        return this.openMerchant((Merchant) villager, force);
    }

    @Override
    public InventoryView openMerchant(Merchant merchant, boolean force) {
        Preconditions.checkNotNull(merchant, "merchant cannot be null");

        if (!force && merchant.isTrading()) {
            return null;
        } else if (merchant.isTrading()) {
            // we're not supposed to have multiple people using the same merchant, so we have to close it.
            merchant.getTrader().closeInventory();
        }

        IMerchant mcMerchant;
        IChatBaseComponent name;
        int level = 1; // note: using level 0 with active 'is-regular-villager'-flag allows hiding the name suffix
        if (merchant instanceof CraftAbstractVillager) {
            mcMerchant = ((CraftAbstractVillager) merchant).getHandle();
            name = ((CraftAbstractVillager) merchant).getHandle().getScoreboardDisplayName();
            if (merchant instanceof CraftVillager) {
                level = ((CraftVillager) merchant).getHandle().getVillagerData().getLevel();
            }
        } else if (merchant instanceof CraftMerchantCustom) {
            mcMerchant = ((CraftMerchantCustom) merchant).getMerchant();
            name = ((CraftMerchantCustom) merchant).getMerchant().getScoreboardDisplayName();
        } else {
            throw new IllegalArgumentException("Can't open merchant " + merchant.toString());
        }

        mcMerchant.setTradingPlayer(this.getHandle());
        mcMerchant.openTrade(this.getHandle(), name, level);

        return this.getHandle().containerMenu.getBukkitView();
    }

    @Override
    public void closeInventory() {
        getHandle().closeInventory();
    }

    @Override
    public boolean isBlocking() {
        return getHandle().isBlocking();
    }

    @Override
    public boolean isHandRaised() {
        return getHandle().isHandRaised();
    }

    @Override
    public boolean setWindowProperty(InventoryView.Property prop, int value) {
        return false;
    }

    @Override
    public int getExpToLevel() {
        return getHandle().getExpToLevel();
    }

    @Override
    public float getAttackCooldown() {
        return getHandle().getAttackCooldown(0.5f);
    }

    @Override
    public boolean hasCooldown(Material material) {
        Preconditions.checkArgument(material != null, "material");

        return getHandle().getCooldownTracker().hasCooldown(CraftMagicNumbers.getItem(material));
    }

    @Override
    public int getCooldown(Material material) {
        Preconditions.checkArgument(material != null, "material");

        ItemCooldown.Info cooldown = getHandle().getCooldownTracker().cooldowns.get(CraftMagicNumbers.getItem(material));
        return (cooldown == null) ? 0 : Math.max(0, cooldown.endTime - getHandle().getCooldownTracker().tickCount);
    }

    @Override
    public void setCooldown(Material material, int ticks) {
        Preconditions.checkArgument(material != null, "material");
        Preconditions.checkArgument(ticks >= 0, "Cannot have negative cooldown");

        getHandle().getCooldownTracker().setCooldown(CraftMagicNumbers.getItem(material), ticks);
    }

    @Override
    public boolean discoverRecipe(NamespacedKey recipe) {
        return discoverRecipes(Arrays.asList(recipe)) != 0;
    }

    @Override
    public int discoverRecipes(Collection<NamespacedKey> recipes) {
        return getHandle().discoverRecipes(bukkitKeysToMinecraftRecipes(recipes));
    }

    @Override
    public boolean undiscoverRecipe(NamespacedKey recipe) {
        return undiscoverRecipes(Arrays.asList(recipe)) != 0;
    }

    @Override
    public int undiscoverRecipes(Collection<NamespacedKey> recipes) {
        return getHandle().undiscoverRecipes(bukkitKeysToMinecraftRecipes(recipes));
    }

    @Override
    public boolean hasDiscoveredRecipe(NamespacedKey recipe) {
        return false;
    }

    @Override
    public Set<NamespacedKey> getDiscoveredRecipes() {
        return ImmutableSet.of();
    }

    private Collection<IRecipe<?>> bukkitKeysToMinecraftRecipes(Collection<NamespacedKey> recipeKeys) {
        Collection<IRecipe<?>> recipes = new ArrayList<>();
        CraftingManager manager = getHandle().level.getMinecraftServer().getCraftingManager();

        for (NamespacedKey recipeKey : recipeKeys) {
            Optional<? extends IRecipe<?>> recipe = manager.getRecipe(CraftNamespacedKey.toMinecraft(recipeKey));
            if (!recipe.isPresent()) {
                continue;
            }

            recipes.add(recipe.get());
        }

        return recipes;
    }

    @Override
    public org.bukkit.entity.Entity getShoulderEntityLeft() {
        if (!getHandle().getShoulderEntityLeft().isEmpty()) {
            Optional<Entity> shoulder = EntityTypes.a(getHandle().getShoulderEntityLeft(), getHandle().level);

            return (!shoulder.isPresent()) ? null : shoulder.get().getBukkitEntity();
        }

        return null;
    }

    @Override
    public void setShoulderEntityLeft(org.bukkit.entity.Entity entity) {
        getHandle().setShoulderEntityLeft(entity == null ? new NBTTagCompound() : ((CraftEntity) entity).save());
        if (entity != null) {
            entity.remove();
        }
    }

    @Override
    public org.bukkit.entity.Entity getShoulderEntityRight() {
        if (!getHandle().getShoulderEntityRight().isEmpty()) {
            Optional<Entity> shoulder = EntityTypes.a(getHandle().getShoulderEntityRight(), getHandle().level);

            return (!shoulder.isPresent()) ? null : shoulder.get().getBukkitEntity();
        }

        return null;
    }

    @Override
    public void setShoulderEntityRight(org.bukkit.entity.Entity entity) {
        getHandle().setShoulderEntityRight(entity == null ? new NBTTagCompound() : ((CraftEntity) entity).save());
        if (entity != null) {
            entity.remove();
        }
    }

    @Override
    public boolean dropItem(boolean dropAll) {
        return getHandle().dropItem(dropAll);
    }

    @Override
    public float getExhaustion() {
        return getHandle().getFoodData().exhaustionLevel;
    }

    @Override
    public void setExhaustion(float value) {
        getHandle().getFoodData().exhaustionLevel = value;
    }

    @Override
    public float getSaturation() {
        return getHandle().getFoodData().saturationLevel;
    }

    @Override
    public void setSaturation(float value) {
        getHandle().getFoodData().saturationLevel = value;
    }

    @Override
    public int getFoodLevel() {
        return getHandle().getFoodData().foodLevel;
    }

    @Override
    public void setFoodLevel(int value) {
        getHandle().getFoodData().foodLevel = value;
    }

    @Override
    public int getSaturatedRegenRate() {
        return getHandle().getFoodData().saturatedRegenRate;
    }

    @Override
    public void setSaturatedRegenRate(int i) {
        getHandle().getFoodData().saturatedRegenRate = i;
    }

    @Override
    public int getUnsaturatedRegenRate() {
        return getHandle().getFoodData().unsaturatedRegenRate;
    }

    @Override
    public void setUnsaturatedRegenRate(int i) {
        getHandle().getFoodData().unsaturatedRegenRate = i;
    }

    @Override
    public int getStarvationRate() {
        return getHandle().getFoodData().starvationRate;
    }

    @Override
    public void setStarvationRate(int i) {
        getHandle().getFoodData().starvationRate = i;
    }
}
