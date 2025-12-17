package unit.weapon;

import unit.UnitLike;

import java.awt.*;
import java.util.HashMap;
import java.util.function.Function;

public class DamageHandler {
    private final HashMap<Point, DamageProcessData> damageReceivers = new HashMap<>();

    public void add(UnitLike<?> unit) {
        damageReceivers.put(unit.data.pos, new DamageProcessData(unit));
    }

    public DamageProcessData get(Point p) {
        return damageReceivers.get(p);
    }

    public void move(Point from, Point to) {
        damageReceivers.put(to, get(from));
        damageReceivers.remove(from);
    }

    public void realise(Function<Point, UnitLike<?>> getUnit) {
        damageReceivers.forEach((point, data) -> {
            UnitLike<?> u = getUnit.apply(point);
            u.data.hitPoints = data.hitPoints;
            u.data.shieldHP = data.shieldHP;
        });
    }

    public void applyDamage(Function<Point, UnitLike<?>> getUnit) {
        damageReceivers.forEach(((point, data) -> {
            UnitLike<?> u = getUnit.apply(point);
            if (data.wasDamaged)
                u.applyDamage(this);
        }));
    }
}
