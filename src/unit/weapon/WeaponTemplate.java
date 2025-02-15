package unit.weapon;

import unit.Unit;
import unit.type.UnitType;

import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Function;

public class WeaponTemplate {
    public boolean requiresAmmo = false;
    public boolean runsAnim = false;
    public int ammoCapacity = 0;
    public final HashMap<UnitType, AttackData> data = new HashMap<>();
    public final ProjectileType projectileType;
    public Function<Unit, HashSet<Point>> tilesInFiringRange = u -> range(u, 1);

    public WeaponTemplate(ProjectileType projectileType) {
        this.projectileType = projectileType;
    }

    public WeaponTemplate consumeAmmo(int capacity) {
        requiresAmmo = true;
        ammoCapacity = capacity;
        return this;
    }

    public WeaponTemplate addData(String unitName, AttackData attackData) {
        data.put(UnitType.getTypeByName(unitName), attackData);
        return this;
    }

    public WeaponTemplate setTilesInFiringRange(Function<Unit, HashSet<Point>> tilesInFiringRange) {
        this.tilesInFiringRange = tilesInFiringRange;
        return this;
    }

    public WeaponTemplate runAnim() {
        runsAnim = true;
        return this;
    }

    public WeaponTemplate firingRange(int range) {
        tilesInFiringRange = u -> range(u, range);
        return this;
    }

    public static HashSet<Point> range(Unit u, int range) {
        return u.selector().withEnemyUnits(u.selector().tilesInRadius(u.pos, range), u);
    }
}
