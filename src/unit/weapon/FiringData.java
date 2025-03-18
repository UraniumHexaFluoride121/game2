package unit.weapon;

import level.Level;
import unit.Unit;
import unit.UnitData;

import java.awt.*;
import java.util.ArrayList;

public record FiringData(UnitData thisData, UnitData otherData, ArrayList<WeaponInstance> weapons, Level level) {
    public float getDamageMultiplier() {
        return otherData.type.damageReduction(level.getTile(otherData.pos).type);
    }

    public static FiringData reverse(FiringData old, ArrayList<WeaponInstance> otherUnitWeapons) {
        return new FiringData(old.otherData, old.thisData, otherUnitWeapons, old.level);
    }

    public void updateTempHP(Unit thisUnit, Unit otherUnit) {
        thisUnit.firingTempHP = thisData.hitPoints;
        thisUnit.shieldFiringTempHP = thisData.shieldHP;
        otherUnit.firingTempHP = otherData.hitPoints;
        otherUnit.shieldFiringTempHP = otherData.shieldHP;
        if (thisData.weaponAmmo != -1)
            thisUnit.getAmmoWeapon().ammo = thisData.weaponAmmo;
        if (otherData.weaponAmmo != -1)
            otherUnit.getAmmoWeapon().ammo = otherData.weaponAmmo;
    }

    public FiringData setThisPos(Point pos) {
        thisData.pos = pos;
        return this;
    }

    public FiringData setOtherPos(Point pos) {
        otherData.pos = pos;
        return this;
    }
}
