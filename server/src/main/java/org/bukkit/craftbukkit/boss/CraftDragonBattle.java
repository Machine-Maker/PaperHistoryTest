package org.bukkit.craftbukkit.boss;

import com.google.common.base.Preconditions;
import net.minecraft.server.EnderDragonBattle;
import net.minecraft.server.Entity;
import net.minecraft.server.EnumDragonRespawn;
import org.bukkit.Location;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.DragonBattle;
import org.bukkit.entity.EnderDragon;

public class CraftDragonBattle implements DragonBattle {

    private final EnderDragonBattle handle;

    public CraftDragonBattle(EnderDragonBattle handle) {
        this.handle = handle;
    }

    @Override
    public EnderDragon getEnderDragon() {
        Entity entity = handle.world.getEntity(handle.dragonUUID);
        return (entity != null) ? (EnderDragon) entity.getBukkitEntity() : null;
    }

    @Override
    public BossBar getBossBar() {
        return new CraftBossBar(handle.bossBattle);
    }

    @Override
    public Location getEndPortalLocation() {
        return new Location(handle.world.getWorld(), handle.exitPortalLocation.getX(), handle.exitPortalLocation.getY(), handle.exitPortalLocation.getZ());
    }

    @Override
    public boolean hasBeenPreviouslyKilled() {
        return handle.isPreviouslyKilled();
    }

    @Override
    public void initiateRespawn() {
        this.handle.initiateRespawn();
    }

    @Override
    public RespawnPhase getRespawnPhase() {
        return toBukkitRespawnPhase(handle.respawnPhase);
    }

    @Override
    public boolean setRespawnPhase(RespawnPhase phase) {
        Preconditions.checkArgument(phase != null && phase != RespawnPhase.NONE, "Invalid respawn phase provided: %s", phase);

        if (handle.respawnPhase == null) {
            return false;
        }

        this.handle.setRespawnPhase(toNMSRespawnPhase(phase));
        return true;
    }

    @Override
    public void resetCrystals() {
        this.handle.resetCrystals();
    }

    @Override
    public int hashCode() {
        return handle.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CraftDragonBattle && ((CraftDragonBattle) obj).handle == this.handle;
    }

    private RespawnPhase toBukkitRespawnPhase(EnumDragonRespawn phase) {
        return (phase != null) ? RespawnPhase.values()[phase.ordinal()] : RespawnPhase.NONE;
    }

    private EnumDragonRespawn toNMSRespawnPhase(RespawnPhase phase) {
        return (phase != RespawnPhase.NONE) ? EnumDragonRespawn.values()[phase.ordinal()] : null;
    }
}
