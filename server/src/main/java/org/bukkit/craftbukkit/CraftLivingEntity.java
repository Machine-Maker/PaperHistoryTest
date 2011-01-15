
package org.bukkit.craftbukkit;

import net.minecraft.server.Entity;
import net.minecraft.server.EntityArrow;
import net.minecraft.server.EntityEgg;
import net.minecraft.server.EntityLiving;
import net.minecraft.server.EntitySnowball;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Egg;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Vehicle;

public class CraftLivingEntity extends CraftEntity implements LivingEntity {
    private EntityLiving entity;

    public CraftLivingEntity(final CraftServer server, final EntityLiving entity) {
        super(server, entity);
        this.entity = entity;
    }

    public int getHealth() {
        return entity.ba;
    }

    public void setHealth(int health) {
        if ((health < 0) || (health > 20)) {
            throw new IllegalArgumentException("Health must be between 0 and 20");
        }

        entity.ba = health;
    }

    @Override
    public EntityLiving getHandle() {
        return entity;
    }

    public void setHandle(final EntityLiving entity) {
        super.setHandle((Entity)entity);
        this.entity = entity;
    }

    @Override
    public String toString() {
        return "CraftLivingEntity{" + "id=" + getEntityId() + '}';
    }

    public Egg throwEgg() {
        net.minecraft.server.World world = ((CraftWorld)getWorld()).getHandle();
        EntityEgg egg = new EntityEgg(world, entity);
        world.a(egg);
        return new CraftEgg(server, egg);
    }

    public Snowball throwSnowball() {
        net.minecraft.server.World world = ((CraftWorld)getWorld()).getHandle();
        EntitySnowball snowball = new EntitySnowball(world, entity);
        world.a(snowball);
        return new CraftSnowball(server, snowball);
    }

    public Arrow shootArrow() {
        net.minecraft.server.World world = ((CraftWorld)getWorld()).getHandle();
        EntityArrow snowball = new EntityArrow(world, entity);
        world.a(snowball);
        return new CraftArrow(server, snowball);
    }

    public boolean isInsideVehicle() {
        return entity.k != null;
    }

    public boolean leaveVehicle() {
        if (entity.k == null) {
            return false;
        }
        
        entity.setPassengerOf(null);
        return true;
    }

    public Vehicle getVehicle() {
        if (entity.k == null) {
            return null;
        }
        
        org.bukkit.entity.Entity vehicle = ((CraftWorld)getWorld()).toCraftEntity(entity.k);
        if (vehicle instanceof Vehicle) {
            return (Vehicle)vehicle;
        }
        
        return null;
    }
}
