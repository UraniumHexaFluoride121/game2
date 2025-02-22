package unit.weapon;

import unit.Unit;
import unit.info.UnitCharacteristicValue;
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
    public HashMap<DamageType, UnitCharacteristicValue> damageTypes = new HashMap<>();
    public final WeaponType weaponType;
    public String rangeText = "1 Tile";
    public boolean counterattack = true;

    public WeaponTemplate(ProjectileType projectileType, WeaponType weaponType) {
        this.projectileType = projectileType;
        this.weaponType = weaponType;
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

    public WeaponTemplate addDamageType(DamageType type, UnitCharacteristicValue value) {
        damageTypes.put(type, value);
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

    public WeaponTemplate noCounterattack() {
        counterattack = false;
        return this;
    }

    public WeaponTemplate firingRange(int range) {
        tilesInFiringRange = u -> range(u, range);
        rangeText = "1 - " + range + " Tiles";
        return this;
    }

    public WeaponTemplate firingRange(int minRange, int maxRange) {
        tilesInFiringRange = u -> {
            HashSet<Point> tiles = range(u, maxRange);
            tiles.removeAll(range(u, minRange - 1));
            return tiles;
        };
        rangeText = minRange + " - " + maxRange + " Tiles";
        return this;
    }

    public static HashSet<Point> range(Unit u, int range) {
        return u.selector().tilesInRadius(u.pos, range);
    }
}
