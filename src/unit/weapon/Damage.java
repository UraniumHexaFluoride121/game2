package unit.weapon;

import foundation.math.MathUtil;

import java.awt.*;

public class Damage {
    public final float amount, shieldMultiplier, hullMultiplier;
    public final DamageType type;
    public final Point origin, target;

    public Damage(float amount, float shieldMultiplier, float hullMultiplier, DamageType type, Point origin, Point target) {
        this.amount = amount;
        this.shieldMultiplier = shieldMultiplier;
        this.hullMultiplier = hullMultiplier;
        this.type = type;
        this.origin = origin;
        this.target = target;
    }

    public Damage remaining(float amount) {
        return new Damage(amount, shieldMultiplier, hullMultiplier, type, origin, target);
    }

    public Damage divide(float v) {
        return new Damage(amount / v, shieldMultiplier, hullMultiplier, type, origin, target);
    }

    public Damage ofType(DamageType type) {
        return new Damage(amount, shieldMultiplier, hullMultiplier, type, origin, target);
    }

    public Damage target(Point target) {
        return new Damage(amount, shieldMultiplier, hullMultiplier, type, origin, target);
    }

    @Override
    public String toString() {
        return "{" + MathUtil.floatToString(amount) + " damage, " + MathUtil.floatToString(hullMultiplier) + "x hull, " + MathUtil.floatToString(shieldMultiplier) + "x shield, " + type + ", (" + origin.x + ", " + origin.y + ") -> (" + target.x + ", " + target.y + ")}";
    }
}
