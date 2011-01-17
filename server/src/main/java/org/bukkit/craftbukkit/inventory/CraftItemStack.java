package org.bukkit.craftbukkit.inventory;

import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

public class CraftItemStack extends ItemStack {
    protected net.minecraft.server.ItemStack item;

    public CraftItemStack(net.minecraft.server.ItemStack item) {
        super(
            item != null ? item.c : 0,
            item != null ? item.a : 0,
            (byte)(item != null ? item.d : 0)
        );
        this.item = item;
    }

    /* 'Overwritten' constructors from ItemStack, yay for Java sucking */
    public CraftItemStack(final int type) {
        this(type, 0);
    }

    public CraftItemStack(final Material type) {
        this(type, 0);
    }

    public CraftItemStack(final int type, final int amount) {
        this(type, amount, (byte) 0);
    }

    public CraftItemStack(final Material type, final int amount) {
        this(type.getId(), amount);
    }

    public CraftItemStack(final int type, final int amount, final byte damage) {
        this(type, amount, damage, null);
    }

    public CraftItemStack(final Material type, final int amount, final byte damage) {
        this(type.getId(), amount, damage);
    }

    public CraftItemStack(final Material type, final int amount, final byte damage, final Byte data) {
        this(type.getId(), amount, damage, data);
    }

    public CraftItemStack(int type, int amount, byte damage, Byte data) {
        this(new net.minecraft.server.ItemStack(type, amount, data != null ? data : damage));
    }

    /*
     * Unsure if we have to sync before each of these calls the values in 'item'
     * are all public.
     */

    @Override
    public Material getType() {
        super.setTypeId(item != null ? item.c : 0); // sync, needed?
        return super.getType();
    }

    @Override
    public int getTypeId() {
        super.setTypeId(item != null ? item.c : 0); // sync, needed?
        return item != null ? item.c : 0;
    }

    @Override
    public void setTypeId(int type) {
        if (type == 0) {
            super.setTypeId(0);
            super.setAmount(0);
            item = null;
        } else {
            if (item == null) {
                item = new net.minecraft.server.ItemStack(type, 1, 0);
                super.setAmount(1);
            } else {
                item.c = type;
                super.setTypeId(item.c);
            }
        }
    }

    @Override
    public int getAmount() {
        super.setAmount(item != null ? item.a : 0); // sync, needed?
        return (item != null ? item.a : 0);
    }

    @Override
    public void setAmount(int amount) {
        if (amount == 0) {
            super.setTypeId(0);
            super.setAmount(0);
            item = null;
        } else {
            super.setAmount(amount);
            item.a = amount;
        }
    }

    @Override
    public void setDamage(final byte damage) {
        // Ignore damage if item is null
        if (item != null) {
            super.setDamage(damage);
            item.d = damage;
        } 
    }

    @Override
    public byte getDamage() {
        if (item != null) {
            super.setDamage((byte) item.d); // sync, needed?    
            return (byte) item.d;
        } else {
            return 0;
        }
    }

    @Override
    public int getMaxStackSize() {
        return item.a().b();
    }
}
