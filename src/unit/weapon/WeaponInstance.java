package unit.weapon;

import level.Level;
import level.tile.TileSet;
import unit.Unit;
import unit.UnitData;
import unit.type.UnitType;

import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.BiFunction;

public class WeaponInstance {
    public final boolean requiresAmmo, runsAnim;
    public final int ammoCapacity;
    public int ammo;
    public final HashMap<UnitType, AttackData> data;
    public final ProjectileType projectileType;
    public final BiFunction<UnitData, Level, TileSet> tilesInFiringRange;
    public final WeaponTemplate template;

    public WeaponInstance(WeaponTemplate template) {
        this.template = template;
        requiresAmmo = template.requiresAmmo;
        ammoCapacity = template.ammoCapacity;
        projectileType = template.projectileType;
        ammo = ammoCapacity;
        data = template.data;
        runsAnim = template.runsAnim;
        tilesInFiringRange = template.tilesInFiringRange;
    }

    public float getDamageAgainst(FiringData firingData) {
        float damageLeft = 0.25f + 0.75f * (firingData.thisData().hitPoints / firingData.thisData().type.hitPoints);
        float shieldDamage = getShieldDamage(firingData, damageLeft);
        damageLeft = newDamageLeft(damageLeft, shieldDamage, firingData.otherData().shieldHP);
        float hullDamage = getHullDamage(firingData, damageLeft);
        return hullDamage + Math.min(firingData.otherData().shieldHP, shieldDamage);
    }

    public float getHullDamage(FiringData firingData, float damageLeft) {
        return data.get(firingData.otherData().type).damage * damageLeft * firingData.getDamageMultiplier();
    }

    public float getShieldDamage(FiringData firingData, float damageLeft) {
        return data.get(firingData.otherData().type).shieldDamage * firingData.getDamageMultiplier() * damageLeft;
    }

    private float newDamageLeft(float damageLeft, float damageDealt, float maxAvailable) {
        if (damageDealt == 0)
            return 0;
        return damageLeft * (1 - Math.min(damageDealt, maxAvailable) / damageDealt);
    }

    public void fire(FiringData firingData) {
        if (requiresAmmo)
            firingData.thisData().weaponAmmo--;
        float damageLeft = Math.max(0.1f, firingData.thisData().hitPoints / firingData.thisData().type.hitPoints);
        float shieldDamage = getShieldDamage(firingData, damageLeft);
        damageLeft = newDamageLeft(damageLeft, shieldDamage, firingData.otherData().shieldHP);
        float hullDamage = getHullDamage(firingData, damageLeft);

        UnitData otherData = firingData.otherData();
        otherData.hitPoints = Math.max(0, otherData.hitPoints - hullDamage);
        if (otherData.hitPoints < 0.05f)
            otherData.hitPoints = 0;
        otherData.shieldHP = Math.max(0, otherData.shieldHP - shieldDamage);
        if (otherData.shieldHP < 0.05f)
            otherData.shieldHP = 0;
    }

    public enum FireAnimState {
        HOLD,
        FIRE,
        EMPTY
    }
}
