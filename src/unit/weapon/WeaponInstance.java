package unit.weapon;

import unit.Unit;
import unit.UnitType;

import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.BiFunction;
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
        return data.get(other.type).damage * Math.max(0.05f, thisUnit.firingTempHP / thisUnit.type.hitPoints) * other.getTileDamageMultiplier();
    }

    public void fire(Unit thisUnit, Unit otherUnit) {
        if (requiresAmmo)
            ammo--;
        otherUnit.firingTempHP -= getDamageAgainst(thisUnit, otherUnit);
    }

    public enum FireAnimState {
        HOLD,
        FIRE,
        EMPTY
    }
}
