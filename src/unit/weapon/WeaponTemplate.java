package unit.weapon;

import level.Level;
import level.tile.TileSet;
import unit.UnitData;
import unit.info.UnitCharacteristicValue;
import unit.type.UnitType;

import java.util.HashMap;
import java.util.function.BiFunction;

public class WeaponTemplate {
    public boolean requiresAmmo = false;
    public boolean runsAnim = false;
    public int ammoCapacity = 0;
    public final HashMap<UnitType, AttackData> data = new HashMap<>();
    public final ProjectileType projectileType;
    public BiFunction<UnitData, Level, TileSet> tilesInFiringRange = (u, l) -> range(u, l, 1);
    public HashMap<DamageType, UnitCharacteristicValue> damageTypes = new HashMap<>();
    public final WeaponType weaponType;
    public String rangeText = "1 Tile";
    public boolean counterattack = true;

    public WeaponTemplate(ProjectileType projectileType, WeaponType weaponType) {
        this.projectileType = projectileType;
        this.weaponType = weaponType;
        damageTypes.put(DamageType.CAPITAL_SHIP, UnitCharacteristicValue.NONE);
    }

    public WeaponTemplate consumeAmmo(int capacity) {
        requiresAmmo = true;
        ammoCapacity = capacity;
        return this;
    }

    public WeaponTemplate addData(String unitName, AttackData attackData) {
        data.put(UnitType.getTypeByName(unitName), attackData);
        switch (unitName) {
            case "fighter" -> damageTypes.put(DamageType.FIGHTER, getDamageTypeValue(attackData.damage, DamageType.FIGHTER));
            case "corvette" -> damageTypes.put(DamageType.CORVETTE, getDamageTypeValue(attackData.damage, DamageType.CORVETTE));
            case "cruiser" -> damageTypes.put(DamageType.CRUISER, getDamageTypeValue(attackData.damage, DamageType.CRUISER));
        }
        damageTypes.put(DamageType.SHIELD, getDamageTypeValue(data.values().stream().map(a -> a.shieldDamage).reduce(Float::sum).get() / data.size(), DamageType.SHIELD));
        return this;
    }

    public WeaponTemplate setTilesInFiringRange(BiFunction<UnitData, Level, TileSet> tilesInFiringRange) {
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
        tilesInFiringRange = (u, l) -> range(u, l, range);
        rangeText = "1 - " + range + " Tiles";
        return this;
    }

    public WeaponTemplate firingRange(int minRange, int maxRange) {
        tilesInFiringRange = (u, l) -> {
            TileSet tiles = range(u, l, maxRange);
            tiles.removeAll(range(u, l, minRange - 1));
            return tiles;
        };
        rangeText = minRange + " - " + maxRange + " Tiles";
        return this;
    }

    public static TileSet range(UnitData u, Level l, int range) {
        return TileSet.tilesInRadius(u.pos, range, l);
    }

    public static UnitCharacteristicValue getDamageTypeValue(float damage, DamageType type) {
        return switch (type) {
            case FIGHTER -> getFighterDamageType(damage);
            case CORVETTE -> getCorvetteDamageType(damage);
            case CRUISER -> getCruiserDamageType(damage);
            case CAPITAL_SHIP -> getCapitalShipDamageType(damage);
            case SHIELD -> getShieldDamageType(damage);
        };
    }

    public static UnitCharacteristicValue getFighterDamageType(float damage) {
        if (damage == 0f)
            return UnitCharacteristicValue.NONE;
        else if (damage <= 0.5f)
            return UnitCharacteristicValue.NONE_LOW;
        else if (damage <= 1.0f)
            return UnitCharacteristicValue.LOW;
        else if (damage <= 1.8f)
            return UnitCharacteristicValue.LOW_MODERATE;
        else if (damage <= 2.5f)
            return UnitCharacteristicValue.MODERATE;
        else if (damage <= 3.3f)
            return UnitCharacteristicValue.MODERATE_GOOD;
        else if (damage <= 4.2f)
            return UnitCharacteristicValue.GOOD;
        else if (damage <= 5.0f)
            return UnitCharacteristicValue.GOOD_HIGH;
        else if (damage <= 5.8f)
            return UnitCharacteristicValue.HIGH;
        else if (damage <= 6.9f)
            return UnitCharacteristicValue.HIGH_MAX;
        else
            return UnitCharacteristicValue.MAX;
    }

    public static UnitCharacteristicValue getCorvetteDamageType(float damage) {
        if (damage == 0f)
            return UnitCharacteristicValue.NONE;
        else if (damage <= 0.5f)
            return UnitCharacteristicValue.NONE_LOW;
        else if (damage <= 1.0f)
            return UnitCharacteristicValue.LOW;
        else if (damage <= 1.8f)
            return UnitCharacteristicValue.LOW_MODERATE;
        else if (damage <= 2.5f)
            return UnitCharacteristicValue.MODERATE;
        else if (damage <= 3.3f)
            return UnitCharacteristicValue.MODERATE_GOOD;
        else if (damage <= 4.2f)
            return UnitCharacteristicValue.GOOD;
        else if (damage <= 5.0f)
            return UnitCharacteristicValue.GOOD_HIGH;
        else if (damage <= 5.8f)
            return UnitCharacteristicValue.HIGH;
        else if (damage <= 6.9f)
            return UnitCharacteristicValue.HIGH_MAX;
        else
            return UnitCharacteristicValue.MAX;
    }

    public static UnitCharacteristicValue getCruiserDamageType(float damage) {
        if (damage == 0f)
            return UnitCharacteristicValue.NONE;
        else if (damage <= 0.5f)
            return UnitCharacteristicValue.NONE_LOW;
        else if (damage <= 1.1f)
            return UnitCharacteristicValue.LOW;
        else if (damage <= 1.9f)
            return UnitCharacteristicValue.LOW_MODERATE;
        else if (damage <= 2.7f)
            return UnitCharacteristicValue.MODERATE;
        else if (damage <= 3.6f)
            return UnitCharacteristicValue.MODERATE_GOOD;
        else if (damage <= 4.5f)
            return UnitCharacteristicValue.GOOD;
        else if (damage <= 5.5f)
            return UnitCharacteristicValue.GOOD_HIGH;
        else if (damage <= 6.6f)
            return UnitCharacteristicValue.HIGH;
        else if (damage <= 7.9f)
            return UnitCharacteristicValue.HIGH_MAX;
        else
            return UnitCharacteristicValue.MAX;
    }

    public static UnitCharacteristicValue getCapitalShipDamageType(float damage) {
        if (damage == 0f)
            return UnitCharacteristicValue.NONE;
        else if (damage <= 0.6f)
            return UnitCharacteristicValue.NONE_LOW;
        else if (damage <= 1.4f)
            return UnitCharacteristicValue.LOW;
        else if (damage <= 2.3f)
            return UnitCharacteristicValue.LOW_MODERATE;
        else if (damage <= 3.2f)
            return UnitCharacteristicValue.MODERATE;
        else if (damage <= 4.2f)
            return UnitCharacteristicValue.MODERATE_GOOD;
        else if (damage <= 5.3f)
            return UnitCharacteristicValue.GOOD;
        else if (damage <= 6.5f)
            return UnitCharacteristicValue.GOOD_HIGH;
        else if (damage <= 7.4f)
            return UnitCharacteristicValue.HIGH;
        else if (damage <= 8.9f)
            return UnitCharacteristicValue.HIGH_MAX;
        else
            return UnitCharacteristicValue.MAX;
    }

    public static UnitCharacteristicValue getShieldDamageType(float damage) {
        if (damage == 0f)
            return UnitCharacteristicValue.NONE;
        else if (damage <= 0.4f)
            return UnitCharacteristicValue.NONE_LOW;
        else if (damage <= 0.8f)
            return UnitCharacteristicValue.LOW;
        else if (damage <= 1.5f)
            return UnitCharacteristicValue.LOW_MODERATE;
        else if (damage <= 2.1f)
            return UnitCharacteristicValue.MODERATE;
        else if (damage <= 2.8f)
            return UnitCharacteristicValue.MODERATE_GOOD;
        else if (damage <= 3.6f)
            return UnitCharacteristicValue.GOOD;
        else if (damage <= 4.3f)
            return UnitCharacteristicValue.GOOD_HIGH;
        else if (damage <= 5.0f)
            return UnitCharacteristicValue.HIGH;
        else if (damage <= 5.9f)
            return UnitCharacteristicValue.HIGH_MAX;
        else
            return UnitCharacteristicValue.MAX;
    }
}
