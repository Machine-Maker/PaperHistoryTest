package org.bukkit.craftbukkit.inventory;

import com.google.common.collect.ImmutableMap.Builder;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.DelegateDeserialization;
import org.bukkit.craftbukkit.inventory.CraftMetaItem.ItemMetaKey;
import org.bukkit.craftbukkit.inventory.CraftMetaItem.SerializableMeta;
import org.bukkit.inventory.meta.FireworkEffectMeta;

@DelegateDeserialization(SerializableMeta.class)
class CraftMetaCharge extends CraftMetaItem implements FireworkEffectMeta {
    static final ItemMetaKey EXPLOSION = new ItemMetaKey("Explosion", "firework-effect");

    private FireworkEffect effect;

    CraftMetaCharge(CraftMetaItem meta) {
        super(meta);

        if (meta instanceof CraftMetaCharge) {
            this.effect = ((CraftMetaCharge) meta).effect;
        }
    }

    CraftMetaCharge(Map<String, Object> map) {
        super(map);

        this.setEffect(SerializableMeta.getObject(FireworkEffect.class, map, EXPLOSION.BUKKIT, true));
    }

    CraftMetaCharge(CompoundTag tag) {
        super(tag);

        if (tag.contains(EXPLOSION.NBT)) {
            try {
                this.effect = CraftMetaFirework.getEffect(tag.getCompound(EXPLOSION.NBT));
            } catch (IllegalArgumentException ex) {
                // Ignore invalid effects
            }
        }
    }

    @Override
    public void setEffect(FireworkEffect effect) {
        this.effect = effect;
    }

    @Override
    public boolean hasEffect() {
        return this.effect != null;
    }

    @Override
    public FireworkEffect getEffect() {
        return this.effect;
    }

    @Override
    void applyToItem(CompoundTag itemTag) {
        super.applyToItem(itemTag);

        if (this.hasEffect()) {
            itemTag.put(EXPLOSION.NBT, CraftMetaFirework.getExplosion(effect));
        }
    }

    @Override
    boolean applicableTo(Material type) {
        switch (type) {
            case FIREWORK_STAR:
                return true;
            default:
                return false;
        }
    }

    @Override
    boolean isEmpty() {
        return super.isEmpty() && !this.hasChargeMeta();
    }

    boolean hasChargeMeta() {
        return this.hasEffect();
    }

    @Override
    boolean equalsCommon(CraftMetaItem meta) {
        if (!super.equalsCommon(meta)) {
            return false;
        }
        if (meta instanceof CraftMetaCharge) {
            CraftMetaCharge that = (CraftMetaCharge) meta;

            return (this.hasEffect() ? that.hasEffect() && this.effect.equals(that.effect) : !that.hasEffect());
        }
        return true;
    }

    @Override
    boolean notUncommon(CraftMetaItem meta) {
        return super.notUncommon(meta) && (meta instanceof CraftMetaCharge || !this.hasChargeMeta());
    }

    @Override
    int applyHash() {
        final int original;
        int hash = original = super.applyHash();

        if (this.hasEffect()) {
            hash = 61 * hash + this.effect.hashCode();
        }

        return hash != original ? CraftMetaCharge.class.hashCode() ^ hash : hash;
    }

    @Override
    public CraftMetaCharge clone() {
        return (CraftMetaCharge) super.clone();
    }

    @Override
    Builder<String, Object> serialize(Builder<String, Object> builder) {
        super.serialize(builder);

        if (this.hasEffect()) {
            builder.put(EXPLOSION.BUKKIT, effect);
        }

        return builder;
    }
}
