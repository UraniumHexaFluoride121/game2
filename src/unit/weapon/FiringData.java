package unit.weapon;

import level.tile.TileType;
import unit.UnitData;
import unit.UnitLike;
import unit.stats.Modifier;
import unit.stats.ModifierCategory;

import java.awt.*;
import java.util.ArrayList;
import java.util.function.Function;

public final class FiringData {
    public final UnitLike<?> thisUnit, otherUnit;
    public final UnitData thisData, otherData;
    public final Function<Point, TileType> tileType;

    public FiringData(UnitLike<?> thisUnit, UnitLike<?> otherUnit, Function<Point, TileType> tileType) {
        this.thisUnit = thisUnit;
        this.otherUnit = otherUnit;
        this.tileType = tileType;
        thisData = thisUnit.data.copy();
        otherData = otherUnit.data.copy();
    }

    private FiringData(UnitLike<?> thisUnit, UnitLike<?> otherUnit, UnitData thisData, UnitData otherData, Function<Point, TileType> tileType) {
        this.thisUnit = thisUnit;
        this.otherUnit = otherUnit;
        this.thisData = thisData;
        this.otherData = otherData;
        this.tileType = tileType;
    }

    public float getDamageMultiplier() {
        return getDamageMultiplier(getBestWeaponAgainst(false));
    }

    public float getDamageMultiplier(WeaponInstance weapon) {
        return Modifier.multiplicativeEffect(ModifierCategory.DAMAGE, damageModifiers(weapon).stream().filter(m -> m.isOnAttackingUnit).map(m -> m.modifier).toList()) *
                Modifier.multiplicativeEffect(ModifierCategory.INCOMING_DAMAGE, damageModifiers(weapon).stream().filter(m -> !m.isOnAttackingUnit).map(m -> m.modifier).toList());
    }

    public float unitDamageMultiplier() {
        return 0.25f + 0.75f * thisData.hitPoints / thisUnit.stats.maxHP();
    }

    public float getShieldDamageMultiplier(WeaponInstance weapon) {
        return Modifier.multiplicativeEffect(ModifierCategory.SHIELD_DAMAGE, damageModifiers(weapon).stream().filter(m -> m.isOnAttackingUnit).map(m -> m.modifier).toList()) *
                Modifier.multiplicativeEffect(ModifierCategory.INCOMING_SHIELD_DAMAGE, damageModifiers(weapon).stream().filter(m -> !m.isOnAttackingUnit).map(m -> m.modifier).toList()) *
                getDamageMultiplier(weapon);
    }

    public ArrayList<DamageModifier> damageModifiers(WeaponInstance weapon) {
        ArrayList<DamageModifier> modifiers = new ArrayList<>(weapon.template.getModifiers(otherUnit.data.type).stream().map(m -> new DamageModifier(m, true)).toList());
        TileType tile = tileType.apply(otherUnit.data.pos);
        modifiers.add(new DamageModifier(tile.damageModifier, false));
        return modifiers;
    }

    public static FiringData reverse(FiringData old) {
        return new FiringData(old.otherUnit, old.thisUnit, old.otherData, old.thisData, old.tileType);
    }

    public void realiseDamage() {
        thisUnit.data.hitPoints = thisData.hitPoints;
        thisUnit.data.shieldHP = thisData.shieldHP;
        otherUnit.data.hitPoints = otherData.hitPoints;
        otherUnit.data.shieldHP = otherData.shieldHP;
        if (thisData.ammo != -1)
            thisUnit.data.ammo = thisData.ammo;
        if (otherData.ammo != -1)
            otherUnit.data.ammo = otherData.ammo;
    }

    public FiringData setThisPos(Point pos) {
        thisData.pos = pos;
        return this;
    }

    public FiringData setOtherPos(Point pos) {
        otherData.pos = pos;
        return this;
    }

    public WeaponInstance getBestWeaponAgainst(boolean fireWeapon) {
        if (!thisUnit.stats.tilesInFiringRange(thisData).contains(otherUnit.data.pos))
            return null;
        if (thisUnit.stats.consumesAmmo() && thisUnit.data.ammo == 0)
            return null;
        WeaponInstance bestWeapon = null;
        float damage = 0;
        for (WeaponInstance weapon : thisUnit.weapons) {
            float newDamage = weapon.getDamageAgainst(this);
            if (newDamage > damage) {
                damage = newDamage;
                bestWeapon = weapon;
            }
        }
        if (bestWeapon != null && fireWeapon) {
            bestWeapon.fire(this);
        }
        return bestWeapon;
    }

    public float getDamageAgainst() {
        if (thisUnit.stats.consumesAmmo() && thisUnit.data.ammo == 0)
            return 0;
        float damage = 0;
        for (WeaponInstance weapon : thisUnit.weapons) {
            float newDamage = weapon.getDamageAgainst(this);
            if (newDamage > damage) {
                damage = newDamage;
            }
        }
        return damage;
    }

    public record DamageModifier(Modifier modifier, boolean isOnAttackingUnit) {

    }
}
