package unit.weapon;

import level.Level;
import level.tile.TileType;
import unit.UnitData;
import unit.UnitLike;
import unit.stats.modifiers.types.Modifier;
import unit.stats.modifiers.types.ModifierCategory;

import java.awt.*;
import java.util.ArrayList;
import java.util.function.Function;

public final class FiringData {
    public final UnitLike<?> thisUnit, otherUnit;
    public final UnitData thisData, otherData;
    public final Function<Point, TileType> tileType;
    public DamageHandler handler = new DamageHandler();

    public FiringData(UnitLike<?> thisUnit, UnitLike<?> otherUnit, Function<Point, TileType> tileType, Level level) {
        this.thisUnit = thisUnit;
        this.otherUnit = otherUnit;
        this.tileType = tileType;
        thisData = thisUnit.data.copy();
        otherData = otherUnit.data.copy();
        if (level != null)
            level.unitSet.forEach(handler::add);
        else {
            handler.add(thisUnit);
            handler.add(otherUnit);
        }
    }

    private FiringData(UnitLike<?> thisUnit, UnitLike<?> otherUnit, UnitData thisData, UnitData otherData, Function<Point, TileType> tileType, DamageHandler handler) {
        this.thisUnit = thisUnit;
        this.otherUnit = otherUnit;
        this.thisData = thisData;
        this.otherData = otherData;
        this.tileType = tileType;
        this.handler = handler;
    }

    public float getDamageMultiplier() {
        return getDamageMultiplier(getBestWeaponAgainst(false));
    }

    public float getDamageMultiplier(WeaponInstance weapon) {
        return Modifier.multiplicativeEffect(ModifierCategory.DAMAGE, damageModifiers(weapon).stream().filter(m -> m.isOnAttackingUnit).map(m -> m.modifier).toList()) *
                Modifier.multiplicativeEffect(ModifierCategory.INCOMING_DAMAGE, damageModifiers(weapon).stream().filter(m -> !m.isOnAttackingUnit).map(m -> m.modifier).toList());
    }

    public float unitDamageMultiplier() {
        return 0.25f + 0.75f * handler.get(thisData.pos).hitPoints / thisUnit.stats.maxHP();
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
        /*if (thisUnit instanceof Unit u) {
            float damageEffect = u.getLevel().teamData.get(u.data.team).getCardUnitModifier(ModifierCategory.DAMAGE, u, Float::sum);
            if (!MathUtil.equal(damageEffect, 1, 0.001f))
                modifiers.add(new DamageModifier(new SingleModifier(
                        damageEffect,
                        "Card Modifiers", "The combined " + ModifierCategory.DAMAGE.getName().toLowerCase() + " modifier from all applicable cards.",
                        Modifier::percentMultiplicative, ModifierCategory.DAMAGE)
                        .setListAndMainColour(Modifier.listColourFromEffectPercentMultiplicative(damageEffect, true)), true));
        }*/
        return modifiers;
    }

    public static FiringData reverse(FiringData old) {
        return new FiringData(old.otherUnit, old.thisUnit, old.otherData, old.thisData, old.tileType, old.handler);
    }

    public void realiseEffects(Function<Point, UnitLike<?>> getUnit) {
        handler.realise(getUnit);
        if (thisData.ammo != -1)
            thisUnit.data.ammo = thisData.ammo;
        if (otherData.ammo != -1)
            otherUnit.data.ammo = otherData.ammo;
    }

    public void applyEffects(Function<Point, UnitLike<?>> getUnit) {

    }

    public FiringData setThisPos(Point pos) {
        handler.move(thisData.pos, pos);
        thisData.pos = pos;
        return this;
    }

    public FiringData setOtherPos(Point pos) {
        handler.move(otherData.pos, pos);
        otherData.pos = pos;
        return this;
    }

    public WeaponInstance getBestWeaponAgainst(boolean fireWeapon) {
        if (!thisUnit.stats.tilesInFiringRange(thisData).contains(otherData.pos))
            return null;
        if (thisUnit.stats.consumesAmmo() && thisUnit.data.ammo == 0)
            return null;
        WeaponInstance bestWeapon = null;
        Damage damage = null;
        for (WeaponInstance weapon : thisUnit.weapons) {
            Damage newDamage = weapon.getDamageAgainst(this);
            if (damage == null ||
                    handler.get(otherData.pos).displayDamageDealt(newDamage).amount >
                            handler.get(otherData.pos).displayDamageDealt(damage).amount) {
                damage = newDamage;
                bestWeapon = weapon;
            }
        }
        if (damage != null && fireWeapon) {
            otherUnit.stats.acceptDamage(handler, damage);
        }
        return bestWeapon;
    }

    public float getDamageAgainst() {
        if (thisUnit.stats.consumesAmmo() && thisUnit.data.ammo == 0)
            return 0;
        Damage damage = null;
        for (WeaponInstance weapon : thisUnit.weapons) {
            Damage newDamage = displayDamage(weapon);
            if (damage == null || newDamage.amount > damage.amount) {
                damage = newDamage;
            }
        }
        return damage == null ? 0 : damage.amount;
    }

    public Damage displayDamage(WeaponInstance weapon) {
        return handler.get(otherData.pos).displayDamageDealt(weapon.getDamageAgainst(this));
    }

    public record DamageModifier(Modifier modifier, boolean isOnAttackingUnit) {

    }
}
