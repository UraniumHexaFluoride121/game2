package unit.weapon;

import unit.UnitData;

public class WeaponInstance {
    public final ProjectileType projectileType;
    public final WeaponTemplate template;

    public WeaponInstance(WeaponTemplate template) {
        this.template = template;
        projectileType = template.projectileType;
    }

    public float getDamageAgainst(FiringData firingData) {
        float damageLeft = firingData.thisUnit.stats.baseDamage();
        float shieldDamage = getShieldDamage(firingData, damageLeft);
        damageLeft = newDamageLeft(damageLeft, shieldDamage, firingData.otherData.shieldHP);
        float hullDamage = getHullDamage(firingData, damageLeft);
        return hullDamage + Math.min(firingData.otherData.shieldHP, shieldDamage);
    }

    public float getHullDamage(FiringData firingData, float damageLeft) {
        return damageLeft * firingData.getDamageMultiplier(this) * firingData.unitDamageMultiplier();
    }

    public float getShieldDamage(FiringData firingData, float damageLeft) {
        return damageLeft * firingData.getShieldDamageMultiplier(this) * firingData.unitDamageMultiplier();
    }

    private float newDamageLeft(float damageLeft, float damageDealt, float maxAvailable) {
        if (damageDealt == 0)
            return 0;
        return damageLeft * (1 - Math.min(damageDealt, maxAvailable) / damageDealt);
    }

    public void fire(FiringData firingData) {
        if (firingData.thisUnit.stats.consumesAmmo())
            firingData.thisData.ammo--;
        float damageLeft = firingData.thisUnit.stats.baseDamage();
        float shieldDamage = getShieldDamage(firingData, damageLeft);
        damageLeft = newDamageLeft(damageLeft, shieldDamage, firingData.otherData.shieldHP);
        float hullDamage = getHullDamage(firingData, damageLeft);

        UnitData otherData = firingData.otherData;
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
