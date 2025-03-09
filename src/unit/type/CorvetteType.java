package unit.type;

import foundation.math.ObjPos;
import level.tile.TileType;
import render.texture.FiringRenderer;
import unit.ShipClass;
import unit.action.Action;
import unit.info.AttributeData;
import unit.info.UnitCharacteristic;
import unit.info.UnitCharacteristicValue;
import unit.weapon.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static unit.info.AttributeData.*;

public class CorvetteType extends UnitType {
    public static final CorvetteType CORVETTE = new CorvetteType("corvette", "Corvette", 10, 5f, 3.5f, type -> switch (type) {
        case EMPTY -> 1f;
        case NEBULA -> 1.7f;
        case DENSE_NEBULA -> 1.9f;
        case ASTEROIDS -> 5f;
    }, type -> switch (type) {
        case EMPTY -> 1f;
        case NEBULA -> 1.7f;
        case DENSE_NEBULA -> 100f;
        case ASTEROIDS -> 1.5f;
    }, new Action[]{
            Action.FIRE, Action.MOVE
    }, 1, 17, list -> {
        WeaponTemplate w = new WeaponTemplate(ProjectileType.CORVETTE_CANNON, WeaponType.CANNON);
        float s = 3.4f;
        w.addDamageType(DamageType.FIGHTER, UnitCharacteristicValue.LOW_MODERATE);
        w.addDamageType(DamageType.CORVETTE, UnitCharacteristicValue.HIGH_MAX);
        w.addDamageType(DamageType.CRUISER, UnitCharacteristicValue.GOOD);
        w.addDamageType(DamageType.CAPITAL_SHIP, UnitCharacteristicValue.LOW_MODERATE);
        w.addDamageType(DamageType.SHIELD, UnitCharacteristicValue.MODERATE);
        w.addData("fighter", new AttackData(1.6f, s));
        w.addData("bomber", new AttackData(1.4f, s));
        w.addData("scout", new AttackData(1.8f, s));
        w.addData("corvette", new AttackData(5.2f, s));
        w.addData("defender", new AttackData(4.8f, s));
        w.addData("artillery", new AttackData(5.2f, s));
        list.add(w);
    }, map -> {
        map.put(UnitCharacteristic.DEFENCE, UnitCharacteristicValue.MODERATE);
        map.put(UnitCharacteristic.SPEED, UnitCharacteristicValue.GOOD);
        map.put(UnitCharacteristic.FIREPOWER, UnitCharacteristicValue.MODERATE);
        map.put(UnitCharacteristic.VIEW_RANGE, UnitCharacteristicValue.MODERATE);
        map.put(UnitCharacteristic.FIRING_RANGE, UnitCharacteristicValue.LOW);
        map.put(UnitCharacteristic.SHIELD, UnitCharacteristicValue.NONE);
    }, (map, perTurnMap) -> {
        map.put(Action.CAPTURE, 6);
        map.put(Action.FIRE, 10);
    }, new AttributeData[]{
            ANTI_CORVETTE, BALANCED,
            SLOW_ASTEROID_FIELD, CARRIER_LOADING,
            INEFFECTIVE_AGAINST_FIGHTER, INEFFECTIVE_AGAINST_SHIELDS
    }, FiringRenderer.THREE_UNITS),

    DEFENDER = new CorvetteType("defender", "Defender", 10, 4f, 2.5f, type -> switch (type) {
        case EMPTY -> 1f;
        case NEBULA -> 1.5f;
        case DENSE_NEBULA -> 1.7f;
        case ASTEROIDS -> 4f;
    }, type -> switch (type) {
        case EMPTY -> 1f;
        case NEBULA -> 1.5f;
        case DENSE_NEBULA -> 100f;
        case ASTEROIDS -> 1.2f;
    }, new Action[]{
            Action.FIRE, Action.MOVE, Action.SHIELD_REGEN
    }, 1, 22, list -> {
        WeaponTemplate w = new WeaponTemplate(ProjectileType.DEFENDER_PLASMA, WeaponType.PLASMA);
        float s = 5.5f;
        w.addDamageType(DamageType.FIGHTER, UnitCharacteristicValue.HIGH);
        w.addDamageType(DamageType.CORVETTE, UnitCharacteristicValue.MODERATE);
        w.addDamageType(DamageType.CRUISER, UnitCharacteristicValue.LOW);
        w.addDamageType(DamageType.CAPITAL_SHIP, UnitCharacteristicValue.NONE_LOW);
        w.addDamageType(DamageType.SHIELD, UnitCharacteristicValue.HIGH_MAX);
        w.addData("fighter", new AttackData(6.2f, s));
        w.addData("bomber", new AttackData(5.8f, s));
        w.addData("scout", new AttackData(6.0f, s));
        w.addData("corvette", new AttackData(2.7f, s));
        w.addData("defender", new AttackData(2.5f, s));
        w.addData("artillery", new AttackData(2.9f, s));
        list.add(w);
    }, map -> {
        map.put(UnitCharacteristic.DEFENCE, UnitCharacteristicValue.GOOD_HIGH);
        map.put(UnitCharacteristic.SPEED, UnitCharacteristicValue.MODERATE);
        map.put(UnitCharacteristic.FIREPOWER, UnitCharacteristicValue.MODERATE);
        map.put(UnitCharacteristic.VIEW_RANGE, UnitCharacteristicValue.LOW_MODERATE);
        map.put(UnitCharacteristic.FIRING_RANGE, UnitCharacteristicValue.LOW);
        map.put(UnitCharacteristic.SHIELD, UnitCharacteristicValue.MODERATE);
    }, (map, perTurnMap) -> {
        map.put(Action.CAPTURE, 6);
        map.put(Action.FIRE, 12);
        map.put(Action.SHIELD_REGEN, 10);
    }, new AttributeData[]{
            ANTI_FIGHTER, ANTI_SHIELD, HAS_SHIELD,
            SLOW_ASTEROID_FIELD, CARRIER_LOADING,
            INEFFECTIVE_AGAINST_LARGE, LOW_VIEW_RANGE
    }, FiringRenderer.TWO_UNITS).addShield(4, 1.5f, 27),

    ARTILLERY = new CorvetteType("artillery", "Artillery", 8, 4f, 3.5f, type -> switch (type) {
        case EMPTY -> 1f;
        case NEBULA -> 1.5f;
        case DENSE_NEBULA -> 1.7f;
        case ASTEROIDS -> 4f;
    }, type -> switch (type) {
        case EMPTY -> 1f;
        case NEBULA -> 1.6f;
        case DENSE_NEBULA -> 100f;
        case ASTEROIDS -> 1.3f;
    }, new Action[]{
            Action.FIRE, Action.MOVE
    }, 1, 16, list -> {
        WeaponTemplate w = new WeaponTemplate(ProjectileType.ARTILLERY_MISSILE, WeaponType.EXPLOSIVE);
        w.consumeAmmo(3).firingRange(2, 4).noCounterattack();
        float s = 1.2f;
        w.addDamageType(DamageType.FIGHTER, UnitCharacteristicValue.LOW);
        w.addDamageType(DamageType.CORVETTE, UnitCharacteristicValue.MODERATE_GOOD);
        w.addDamageType(DamageType.CRUISER, UnitCharacteristicValue.HIGH);
        w.addDamageType(DamageType.CAPITAL_SHIP, UnitCharacteristicValue.HIGH_MAX);
        w.addDamageType(DamageType.SHIELD, UnitCharacteristicValue.LOW);
        w.addData("fighter", new AttackData(1.2f, s));
        w.addData("bomber", new AttackData(1.1f, s));
        w.addData("scout", new AttackData(1.2f, s));
        w.addData("corvette", new AttackData(3.5f, s));
        w.addData("defender", new AttackData(3.8f, s));
        w.addData("artillery", new AttackData(3.5f, s));
        list.add(w);
    }, map -> {
        map.put(UnitCharacteristic.DEFENCE, UnitCharacteristicValue.MODERATE);
        map.put(UnitCharacteristic.SPEED, UnitCharacteristicValue.MODERATE);
        map.put(UnitCharacteristic.FIREPOWER, UnitCharacteristicValue.GOOD_HIGH);
        map.put(UnitCharacteristic.VIEW_RANGE, UnitCharacteristicValue.LOW_MODERATE);
        map.put(UnitCharacteristic.FIRING_RANGE, UnitCharacteristicValue.GOOD);
        map.put(UnitCharacteristic.SHIELD, UnitCharacteristicValue.NONE);
    }, (map, perTurnMap) -> {
        map.put(Action.CAPTURE, 8);
        map.put(Action.FIRE, 16);
    }, new AttributeData[]{
            ANTI_CAPITAL_SHIP, RANGED_WEAPON,
            SLOW_ASTEROID_FIELD, CARRIER_LOADING,
            INEFFECTIVE_AGAINST_SMALL, MAIN_GUN_LIMITED_AMMO
    }, FiringRenderer.THREE_UNITS) {
        @Override
        public float movementCostMultiplier() {
            return 3;
        }
    };

    CorvetteType(String name, String displayName, float hitPoints, float maxMovement, float maxViewRange, Function<TileType, Float> tileMovementCostFunction, Function<TileType, Float> tileViewRangeCostFunction, Action[] actions, int firingAnimFrames, float firingAnimUnitWidth, Consumer<ArrayList<WeaponTemplate>> weaponGenerator, Consumer<HashMap<UnitCharacteristic, UnitCharacteristicValue>> unitCharacteristicSetter, BiConsumer<HashMap<Action, Integer>, HashMap<Action, Integer>> actionCostSetter, AttributeData[] infoAttributes, Supplier<ObjPos[]> firingPositions) {
        super(name, displayName, hitPoints, maxMovement, maxViewRange, tileMovementCostFunction, tileViewRangeCostFunction, actions, firingAnimFrames, firingAnimUnitWidth, weaponGenerator, unitCharacteristicSetter, actionCostSetter, infoAttributes, firingPositions);
    }

    @Override
    public CorvetteType addShield(float shieldHP, float shieldRegen, float firingAnimShieldWidth) {
        super.addShield(shieldHP, shieldRegen, firingAnimShieldWidth);
        return this;
    }

    @Override
    public float getBobbingRate() {
        return .75f;
    }

    @Override
    public float getBobbingAmount() {
        return .8f;
    }

    @Override
    public float movementCostMultiplier() {
        return 2;
    }

    @Override
    public float movementFixedCost() {
        return 2;
    }

    @Override
    public float damageReduction(TileType type) {
        return switch (type) {
            case EMPTY -> 1f;
            case NEBULA -> 0.88f;
            case DENSE_NEBULA -> 0.82f;
            case ASTEROIDS -> 0.76f;
        };
    }

    @Override
    protected ShipClass getShipClass() {
        return ShipClass.CORVETTE;
    }
}
