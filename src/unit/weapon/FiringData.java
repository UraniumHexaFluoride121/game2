package unit.weapon;

import level.Level;
import level.tile.TileType;
import render.UIColourTheme;
import unit.Unit;
import unit.UnitData;
import unit.stats.Modifier;
import unit.stats.ModifierCategory;
import unit.stats.SingleModifier;

import java.awt.*;
import java.util.ArrayList;

public final class FiringData {
    public final Unit thisUnit, otherUnit;
    public final UnitData thisData, otherData;
    public final Level level;

    public FiringData(Unit thisUnit, Unit otherUnit, Level level) {
        this.thisUnit = thisUnit;
        this.otherUnit = otherUnit;
        this.level = level;
        thisData = new UnitData(thisUnit);
        otherData = new UnitData(otherUnit);
    }

    private FiringData(Unit thisUnit, Unit otherUnit, UnitData thisData, UnitData otherData, Level level) {
        this.thisUnit = thisUnit;
        this.otherUnit = otherUnit;
        this.thisData = thisData;
        this.otherData = otherData;
        this.level = level;
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
        ArrayList<DamageModifier> modifiers = new ArrayList<>(weapon.template.getModifiers(otherUnit.type).stream().map(m -> new DamageModifier(m, true)).toList());
        TileType tile = level.getTile(otherUnit.pos).type;
        modifiers.add(new DamageModifier(tile.damageModifier, false));
        return modifiers;
    }

    public static FiringData reverse(FiringData old) {
        return new FiringData(old.otherUnit, old.thisUnit, old.otherData, old.thisData, old.level);
    }

    public void updateTempHP() {
        thisUnit.firingTempHP = thisData.hitPoints;
        thisUnit.shieldFiringTempHP = thisData.shieldHP;
        otherUnit.firingTempHP = otherData.hitPoints;
        otherUnit.shieldFiringTempHP = otherData.shieldHP;
        if (thisData.weaponAmmo != -1)
            thisUnit.ammo = thisData.weaponAmmo;
        if (otherData.weaponAmmo != -1)
            otherUnit.ammo = otherData.weaponAmmo;
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
        if (!thisUnit.stats.tilesInFiringRange(thisData).contains(otherUnit.pos))
            return null;
        if (thisUnit.stats.consumesAmmo() && thisUnit.ammo == 0)
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
        if (thisUnit.stats.consumesAmmo() && thisUnit.ammo == 0)
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
