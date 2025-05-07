package unit.type;

import foundation.math.ObjPos;
import level.tile.TileType;
import render.level.FiringRenderer;
import unit.ShipClass;
import unit.action.Action;
import unit.info.AttributeData;
import unit.info.UnitCharacteristic;
import unit.info.UnitCharacteristicValue;
import unit.weapon.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static unit.info.AttributeData.*;

public class FighterType extends UnitType {
    public static final FighterType
            FIGHTER = new FighterType("fighter", "Fighter", 8, 7f, 3.5f, type -> switch (type) {
        case EMPTY -> 1f;
        case NEBULA -> 1.8f;
        case DENSE_NEBULA -> 2f;
        case ASTEROIDS -> 2.4f;
    }, type -> switch (type) {
        case EMPTY -> 1f;
        case NEBULA -> 1.7f;
        case DENSE_NEBULA -> 100f;
        case ASTEROIDS -> 1.5f;
    }, new Action[]{
            Action.FIRE, Action.MOVE
    }, 1, 15, list -> {
        WeaponTemplate w = new WeaponTemplate(ProjectileType.FIGHTER_PLASMA, WeaponType.PLASMA);
        float s = 5.3f;
        w.addDamageType(DamageType.FIGHTER, UnitCharacteristicValue.GOOD);
        w.addDamageType(DamageType.CORVETTE, UnitCharacteristicValue.LOW_MODERATE);
        w.addDamageType(DamageType.CRUISER, UnitCharacteristicValue.LOW);
        w.addDamageType(DamageType.CAPITAL_SHIP, UnitCharacteristicValue.NONE_LOW);
        w.addDamageType(DamageType.SHIELD, UnitCharacteristicValue.HIGH_MAX);
        w.addData("fighter", new AttackData(3.8f, s));
        w.addData("bomber", new AttackData(3.2f, s));
        w.addData("scout", new AttackData(3.9f, s));
        w.addData("corvette", new AttackData(1.8f, s));
        w.addData("defender", new AttackData(1.6f, s));
        w.addData("artillery", new AttackData(2.1f, s));
        w.addData("supply", new AttackData(1.6f, s));
        w.addData("cruiser", new AttackData(1.0f, s));
        w.addData("miner", new AttackData(1.2f, s));
        list.add(w);
    }, map -> {
        map.put(UnitCharacteristic.DEFENCE, UnitCharacteristicValue.NONE_LOW);
        map.put(UnitCharacteristic.SPEED, UnitCharacteristicValue.HIGH_MAX);
        map.put(UnitCharacteristic.FIREPOWER, UnitCharacteristicValue.LOW);
        map.put(UnitCharacteristic.VIEW_RANGE, UnitCharacteristicValue.GOOD);
        map.put(UnitCharacteristic.FIRING_RANGE, UnitCharacteristicValue.LOW);
    }, (map, perTurnMap) -> {
        map.put(Action.CAPTURE, 4);
        map.put(Action.FIRE, 7);
    }, new AttributeData[]{
            HIGH_MOVEMENT_SPEED, QUICK_ASTEROID_FIELD, ANTI_FIGHTER, ANTI_SHIELD,
            CARRIER_LOADING,
            INEFFECTIVE_AGAINST_LARGE
    }, FiringRenderer.THREE_UNITS, "Basic fighter-class unit. Excels in dogfights with other fighter-class units, as wll as taking down enemy shields. " +
            "Its high speed makes this unit great for capturing structures, and when combined with its above average view distance, it makes for a decent improvised scout unit. " +
            "It is, however, mostly useless for destroying larger units."),

    BOMBER = new FighterType("bomber", "Bomber", 7, 6f, 3.5f, type -> switch (type) {
        case EMPTY -> 1f;
        case NEBULA -> 1.9f;
        case DENSE_NEBULA -> 2.1f;
        case ASTEROIDS -> 2.5f;
    }, type -> switch (type) {
        case EMPTY -> 1f;
        case NEBULA -> 1.7f;
        case DENSE_NEBULA -> 100f;
        case ASTEROIDS -> 1.5f;
    }, new Action[]{
            Action.FIRE, Action.MOVE
    }, 3, 15, list -> {
        WeaponTemplate w1 = new WeaponTemplate(ProjectileType.BOMBER_MISSILE, WeaponType.EXPLOSIVE).consumeAmmo(1).runAnim();
        float s1 = 0.4f;
        w1.addDamageType(DamageType.FIGHTER, UnitCharacteristicValue.LOW);
        w1.addDamageType(DamageType.CORVETTE, UnitCharacteristicValue.MODERATE_GOOD);
        w1.addDamageType(DamageType.CRUISER, UnitCharacteristicValue.GOOD_HIGH);
        w1.addDamageType(DamageType.CAPITAL_SHIP, UnitCharacteristicValue.MAX);
        w1.addDamageType(DamageType.SHIELD, UnitCharacteristicValue.NONE_LOW);
        w1.addData("fighter", new AttackData(0.8f, s1));
        w1.addData("bomber", new AttackData(0.7f, s1));
        w1.addData("scout", new AttackData(0.6f, s1));
        w1.addData("corvette", new AttackData(2.7f, s1));
        w1.addData("defender", new AttackData(2.6f, s1));
        w1.addData("artillery", new AttackData(2.8f, s1));
        w1.addData("supply", new AttackData(2.5f, s1));
        w1.addData("cruiser", new AttackData(5.2f, s1));
        w1.addData("miner", new AttackData(5.7f, s1));
        list.add(w1);
        WeaponTemplate w2 = new WeaponTemplate(ProjectileType.BOMBER_PLASMA, WeaponType.PLASMA);
        float s2 = 5.0f;
        w2.addDamageType(DamageType.FIGHTER, UnitCharacteristicValue.MODERATE_GOOD);
        w2.addDamageType(DamageType.CORVETTE, UnitCharacteristicValue.LOW_MODERATE);
        w2.addDamageType(DamageType.CRUISER, UnitCharacteristicValue.LOW);
        w2.addDamageType(DamageType.CAPITAL_SHIP, UnitCharacteristicValue.NONE_LOW);
        w2.addDamageType(DamageType.SHIELD, UnitCharacteristicValue.HIGH);
        w2.addData("fighter", new AttackData(3.2f, s2));
        w2.addData("bomber", new AttackData(2.6f, s2));
        w2.addData("scout", new AttackData(3.4f, s2));
        w2.addData("corvette", new AttackData(1.6f, s2));
        w2.addData("defender", new AttackData(1.5f, s2));
        w2.addData("artillery", new AttackData(1.9f, s2));
        w2.addData("supply", new AttackData(1.5f, s2));
        w2.addData("cruiser", new AttackData(1.0f, s2));
        w2.addData("miner", new AttackData(1.2f, s2));
        list.add(w2);
    }, map -> {
        map.put(UnitCharacteristic.DEFENCE, UnitCharacteristicValue.LOW);
        map.put(UnitCharacteristic.SPEED, UnitCharacteristicValue.HIGH);
        map.put(UnitCharacteristic.FIREPOWER, UnitCharacteristicValue.GOOD);
        map.put(UnitCharacteristic.VIEW_RANGE, UnitCharacteristicValue.GOOD);
        map.put(UnitCharacteristic.FIRING_RANGE, UnitCharacteristicValue.LOW);
    }, (map, perTurnMap) -> {
        map.put(Action.CAPTURE, 4);
        map.put(Action.FIRE, 8);
    }, new AttributeData[]{
            HIGH_MOVEMENT_SPEED, QUICK_ASTEROID_FIELD, ANTI_CAPITAL_SHIP_MISSILES,
            CARRIER_LOADING,
            MAIN_GUN_LIMITED_AMMO
    }, FiringRenderer.THREE_UNITS, "This special variant of the Fighter unit comes equipped with missiles that deal high damage against cruiser-class units and capital ships. " +
            "It only has an ammo capacity of one, making resupply an especially important consideration when using this unit. Besides the missile weapon, it has a slightly weaker version of " +
            "the plasma gun that the regular Fighter has, as well as being slightly slower and more vulnerable than the regular Fighter."),

    SCOUT = new FighterType("scout", "Scout", 5, 8f, 5f, type -> switch (type) {
        case EMPTY -> 1f;
        case NEBULA -> 1.8f;
        case DENSE_NEBULA -> 6f;
        case ASTEROIDS -> 2.4f;
    }, type -> switch (type) {
        case EMPTY -> 1f;
        case NEBULA -> 1.7f;
        case DENSE_NEBULA -> 100f;
        case ASTEROIDS -> 1.5f;
    }, new Action[]{
            Action.FIRE, Action.MOVE, Action.STEALTH
    }, 1, 15, list -> {
        WeaponTemplate w = new WeaponTemplate(ProjectileType.SCOUT_PLASMA, WeaponType.PLASMA);
        float s = 2.8f;
        w.addDamageType(DamageType.FIGHTER, UnitCharacteristicValue.LOW_MODERATE);
        w.addDamageType(DamageType.CORVETTE, UnitCharacteristicValue.NONE_LOW);
        w.addDamageType(DamageType.CRUISER, UnitCharacteristicValue.NONE_LOW);
        w.addDamageType(DamageType.CAPITAL_SHIP, UnitCharacteristicValue.NONE_LOW);
        w.addDamageType(DamageType.SHIELD, UnitCharacteristicValue.MODERATE_GOOD);
        w.addData("fighter", new AttackData(1.8f, s));
        w.addData("bomber", new AttackData(1.6f, s));
        w.addData("scout", new AttackData(2.2f, s));
        w.addData("corvette", new AttackData(0.4f, s));
        w.addData("defender", new AttackData(0.4f, s));
        w.addData("artillery", new AttackData(0.5f, s));
        w.addData("supply", new AttackData(0.4f, s));
        w.addData("cruiser", new AttackData(0.3f, s));
        w.addData("miner", new AttackData(0.4f, s));
        list.add(w);
    }, map -> {
        map.put(UnitCharacteristic.DEFENCE, UnitCharacteristicValue.NONE);
        map.put(UnitCharacteristic.SPEED, UnitCharacteristicValue.MAX);
        map.put(UnitCharacteristic.FIREPOWER, UnitCharacteristicValue.NONE_LOW);
        map.put(UnitCharacteristic.VIEW_RANGE, UnitCharacteristicValue.HIGH_MAX);
        map.put(UnitCharacteristic.FIRING_RANGE, UnitCharacteristicValue.LOW);
    }, (map, perTurnMap) -> {
        map.put(Action.FIRE, 4);
        map.put(Action.STEALTH, 10);
        perTurnMap.put(Action.STEALTH, 2);
    }, new AttributeData[]{
            HIGH_MOVEMENT_SPEED, QUICK_ASTEROID_FIELD, HIGH_VIEW_RANGE,
            CARRIER_LOADING, STEALTH_INSTEAD_OF_CAPTURE,
            INEFFECTIVE_AGAINST_ALL, LOW_HP
    }, FiringRenderer.THREE_UNITS, "The scout is a purpose-built unit that specialises in reconnaissance. It has exceptional movement speed and view range, and comes equipped with the stealth ability, " +
            "allowing the unit to stay hidden while performing reconnaissance. While it does have weapons, their firepower leaves much to be desired, and are not intended for frontline use. Can be used in a pinch to " +
            "take down shields, or to destroy a low HP enemy.")
            .modify(UnitType::noCapture);

    FighterType(String name, String displayName, float hitPoints, float maxMovement, float maxViewRange, Function<TileType, Float> tileMovementCostFunction, Function<TileType, Float> tileViewRangeCostFunction, Action[] actions, int firingAnimFrames, float firingAnimUnitWidth, Consumer<ArrayList<WeaponTemplate>> weaponGenerator, Consumer<TreeMap<UnitCharacteristic, UnitCharacteristicValue>> unitCharacteristicSetter, BiConsumer<HashMap<Action, Integer>, HashMap<Action, Integer>> actionCostSetter, AttributeData[] infoAttributes, Supplier<ObjPos[]> firingPositions, String description) {
        super(name, displayName, hitPoints, maxMovement, maxViewRange, tileMovementCostFunction, tileViewRangeCostFunction, actions, firingAnimFrames, firingAnimUnitWidth, weaponGenerator, unitCharacteristicSetter, actionCostSetter, infoAttributes, firingPositions, description);
    }

    @Override
    public float damageReduction(TileType type) {
        return switch (type) {
            case EMPTY -> 1f;
            case NEBULA -> 0.88f;
            case DENSE_NEBULA -> 0.82f;
            case ASTEROIDS -> 0.72f;
        };
    }

    @Override
    protected ShipClass getShipClass() {
        return ShipClass.FIGHTER;
    }

    @Override
    public float movementCostMultiplier() {
        return 1;
    }

    @Override
    public float movementFixedCost() {
        return 0;
    }

    @Override
    public FighterType modify(Consumer<UnitType> action) {
        super.modify(action);
        return this;
    }
}
