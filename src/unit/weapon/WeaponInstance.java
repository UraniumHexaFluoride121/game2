package unit.weapon;

import unit.Unit;
import unit.type.UnitType;

import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Function;

public class WeaponInstance {
    public final boolean requiresAmmo, runsAnim;
    public final int ammoCapacity;
    public int ammo;
    public final HashMap<UnitType, AttackData> data;
    public final ProjectileType projectileType;
    public final Function<Unit, HashSet<Point>> tilesInFiringRange;

    public WeaponInstance(WeaponTemplate template) {
        requiresAmmo = template.requiresAmmo;
        ammoCapacity = template.ammoCapacity;
        projectileType = template.projectileType;
        ammo = ammoCapacity;
        data = template.data;
        runsAnim = template.runsAnim;
        tilesInFiringRange = template.tilesInFiringRange;
    }

    public float getDamageAgainst(Unit thisUnit, Unit other) {
        float damageLeft = Math.max(0.1f, thisUnit.firingTempHP / thisUnit.type.hitPoints);
        float shieldDamage = getShieldDamage(thisUnit, other, damageLeft);
        damageLeft = newDamageLeft(damageLeft, shieldDamage, other.shieldFiringTempHP);
        float hullDamage = getHullDamage(thisUnit, other, damageLeft);
        return hullDamage + Math.min(other.shieldFiringTempHP, shieldDamage);
    }

    public float getHullDamage(Unit thisUnit, Unit other, float damageLeft) {
        return data.get(other.type).damage * damageLeft * other.getTileDamageMultiplier();
    }

    public float getShieldDamage(Unit thisUnit, Unit other, float damageLeft) {
        return data.get(other.type).shieldDamage * other.getTileDamageMultiplier() * damageLeft;
    }

    private float newDamageLeft(float damageLeft, float damageDealt, float maxAvailable) {
        if (damageDealt == 0)
            return 0;
        return damageLeft * (1 - Math.min(damageDealt, maxAvailable) / damageDealt);
    }

    public void fire(Unit thisUnit, Unit other) {
        if (requiresAmmo)
            ammo--;
        float damageLeft = Math.max(0.1f, thisUnit.firingTempHP / thisUnit.type.hitPoints);
        float shieldDamage = getShieldDamage(thisUnit, other, damageLeft);
        damageLeft = newDamageLeft(damageLeft, shieldDamage, other.shieldFiringTempHP);
        float hullDamage = getHullDamage(thisUnit, other, damageLeft);
        other.firingTempHP -= Math.min(other.firingTempHP, hullDamage);

        if (other.firingTempHP < 0.05f)
            other.firingTempHP = 0;
        other.shieldFiringTempHP -= Math.min(other.shieldFiringTempHP, shieldDamage);
        if (other.shieldFiringTempHP < 0.05f)
            other.shieldFiringTempHP = 0;
    }

    public enum FireAnimState {
        HOLD,
        FIRE,
        EMPTY
    }
}
