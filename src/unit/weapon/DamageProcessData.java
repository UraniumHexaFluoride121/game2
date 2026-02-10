package unit.weapon;

import unit.UnitLike;

import java.awt.*;
import java.util.HashMap;

public class DamageProcessData {
    public HashMap<DamageType, Float> damage = new HashMap<>();
    public float hitPoints, shieldHP;
    public Point destroyedBy = null;
    public boolean wasDamaged = false;

    public DamageProcessData(UnitLike<?> unit) {
        for (DamageType type : DamageType.values()) {
            damage.put(type, 0f);
        }
        hitPoints = unit.data.hitPoints;
        shieldHP = unit.data.shieldHP;
    }

    public boolean alive() {
        return hitPoints > 0;
    }

    public Damage applyDamage(Damage damage) {
        float shieldDamage;
        float amount = damage.amount;
        if (!damage.type.ignoreShield) {
            shieldDamage = amount * damage.shieldMultiplier;
            amount = newDamageLeft(amount, shieldDamage, shieldHP);
        } else
            shieldDamage = 0;
        float hullDamage = amount * damage.hullMultiplier;
        amount = newDamageLeft(amount, hullDamage, hitPoints);

        hitPoints = Math.max(0, hitPoints - hullDamage);
        if (hitPoints < 0.05f) {
            hitPoints = 0;
            destroyedBy = damage.origin;
        }
        shieldHP = Math.max(0, shieldHP - shieldDamage);
        if (shieldHP < 0.05f)
            shieldHP = 0;
        wasDamaged = true;
        return damage.remaining(amount);
    }

    public Damage displayDamageDealt(Damage damage) {
        float shieldDamage;
        float amount = damage.amount;
        if (!damage.type.ignoreShield) {
            shieldDamage = amount * damage.shieldMultiplier;
            amount = newDamageLeft(amount, shieldDamage, shieldHP);
        } else
            shieldDamage = 0;
        float hullDamage = amount * damage.hullMultiplier;
        return damage.remaining(Math.min(shieldHP, shieldDamage) + hullDamage);
    }

    private static float newDamageLeft(float damageLeft, float damageDealt, float maxAvailable) {
        if (damageDealt == 0)
            return 0;
        return damageLeft * (1 - Math.min(damageDealt, maxAvailable) / damageDealt);
    }
}
