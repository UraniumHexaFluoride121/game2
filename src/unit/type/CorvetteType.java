package unit.type;

import foundation.math.ObjPos;
import level.energy.EnergyManager;
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

public class CorvetteType extends UnitType {
    public static final CorvetteType CORVETTE = new CorvetteType("corvette", "Corvette", 10, 5f, 3.5f, new Action[]{
            Action.FIRE, Action.MOVE
    }, 1, 17, list -> {
        WeaponTemplate w = new WeaponTemplate(ProjectileType.CORVETTE_CANNON, WeaponType.CANNON);
        float s = 4.2f;
        w.addData("fighter", new AttackData(1.6f, s));
        w.addData("bomber", new AttackData(1.4f, s));
        w.addData("scout", new AttackData(1.8f, s));
        w.addData("corvette", new AttackData(5.8f, s));
        w.addData("defender", new AttackData(6.2f, s));
        w.addData("artillery", new AttackData(6.1f, s));
        w.addData("supply", new AttackData(5.7f, s));
        w.addData("cruiser", new AttackData(3.2f, s));
        w.addData("battlecruiser", new AttackData(3.4f, s));
        w.addData("miner", new AttackData(3.7f, s));
        list.add(w);
    }, map -> {
        map.put(UnitCharacteristic.DEFENCE, UnitCharacteristicValue.MODERATE);
        map.put(UnitCharacteristic.SPEED, UnitCharacteristicValue.GOOD);
        map.put(UnitCharacteristic.FIREPOWER, UnitCharacteristicValue.MODERATE);
        map.put(UnitCharacteristic.VIEW_RANGE, UnitCharacteristicValue.GOOD);
        map.put(UnitCharacteristic.FIRING_RANGE, UnitCharacteristicValue.LOW);
    }, (map, perTurnMap) -> {
        map.put(Action.CAPTURE, 6);
        map.put(Action.FIRE, 10);
    }, new AttributeData[]{
            ANTI_CORVETTE, BALANCED,
            SLOW_ASTEROID_FIELD, CARRIER_LOADING,
            INEFFECTIVE_AGAINST_FIGHTER, INEFFECTIVE_AGAINST_SHIELDS
    }, FiringRenderer.THREE_UNITS, "Standard corvette-class unit with moderate speed and armour. Excellent for destroying other corvette-class units, while also being good " +
            "in fights against cruisers."),

    DEFENDER = new CorvetteType("defender", "Defender", 8, 4.5f, 2.5f, new Action[]{
            Action.FIRE, Action.MOVE, Action.SHIELD_REGEN
    }, 1, 22, list -> {
        WeaponTemplate w = new WeaponTemplate(ProjectileType.DEFENDER_PLASMA, WeaponType.PLASMA);
        float s = 6.5f;
        w.addData("fighter", new AttackData(6.2f, s));
        w.addData("bomber", new AttackData(5.8f, s));
        w.addData("scout", new AttackData(6.0f, s));
        w.addData("corvette", new AttackData(2.7f, s));
        w.addData("defender", new AttackData(2.5f, s));
        w.addData("artillery", new AttackData(2.9f, s));
        w.addData("supply", new AttackData(2.4f, s));
        w.addData("cruiser", new AttackData(1.1f, s));
        w.addData("battlecruiser", new AttackData(1.2f, s));
        w.addData("miner", new AttackData(1.5f, s));
        list.add(w);
    }, map -> {
        map.put(UnitCharacteristic.DEFENCE, UnitCharacteristicValue.GOOD_HIGH);
        map.put(UnitCharacteristic.SPEED, UnitCharacteristicValue.MODERATE_GOOD);
        map.put(UnitCharacteristic.FIREPOWER, UnitCharacteristicValue.MODERATE);
        map.put(UnitCharacteristic.VIEW_RANGE, UnitCharacteristicValue.LOW_MODERATE);
        map.put(UnitCharacteristic.FIRING_RANGE, UnitCharacteristicValue.LOW);
        map.put(UnitCharacteristic.SHIELD, UnitCharacteristicValue.MODERATE_GOOD);
        map.put(UnitCharacteristic.SHIELD_REGEN, UnitCharacteristicValue.MODERATE_GOOD);
    }, (map, perTurnMap) -> {
        map.put(Action.CAPTURE, 6);
        map.put(Action.FIRE, 12);
        map.put(Action.SHIELD_REGEN, 10);
    }, new AttributeData[]{
            ANTI_FIGHTER, ANTI_SHIELD, HAS_SHIELD,
            SLOW_ASTEROID_FIELD, CARRIER_LOADING,
            INEFFECTIVE_AGAINST_LARGE, LOW_VIEW_RANGE
    }, FiringRenderer.TWO_UNITS, "This unit is primarily designed to support larger units in destroying fighters-class enemies using high-power plasma weapons. " +
            "Features an advanced shield system with decent durability, which works to protect against enemies with anti-corvette weaponry. Has reduced view range and movement speed.")
            .addShield(3, 1, 27),

    ARTILLERY = new CorvetteType("artillery", "Artillery", 8, 4.5f, 3.5f, new Action[]{
            Action.FIRE, Action.MOVE
    }, 1, 16, list -> {
        WeaponTemplate w = new WeaponTemplate(ProjectileType.ARTILLERY_MISSILE, WeaponType.EXPLOSIVE);
        w.consumeAmmo(3).firingRange(2, 3).noCounterattack();
        float s = 1.2f;
        w.addData("fighter", new AttackData(1.2f, s));
        w.addData("bomber", new AttackData(1.1f, s));
        w.addData("scout", new AttackData(1.2f, s));
        w.addData("corvette", new AttackData(3.5f, s));
        w.addData("defender", new AttackData(3.8f, s));
        w.addData("artillery", new AttackData(3.5f, s));
        w.addData("supply", new AttackData(3.4f, s));
        w.addData("cruiser", new AttackData(4.5f, s));
        w.addData("battlecruiser", new AttackData(4.2f, s));
        w.addData("miner", new AttackData(4.6f, s));
        list.add(w);
    }, map -> {
        map.put(UnitCharacteristic.DEFENCE, UnitCharacteristicValue.MODERATE);
        map.put(UnitCharacteristic.SPEED, UnitCharacteristicValue.MODERATE);
        map.put(UnitCharacteristic.FIREPOWER, UnitCharacteristicValue.GOOD_HIGH);
        map.put(UnitCharacteristic.VIEW_RANGE, UnitCharacteristicValue.LOW_MODERATE);
        map.put(UnitCharacteristic.FIRING_RANGE, UnitCharacteristicValue.GOOD);
    }, (map, perTurnMap) -> {
        map.put(Action.CAPTURE, 8);
        map.put(Action.FIRE, 12);
    }, new AttributeData[]{
            ANTI_CAPITAL_SHIP, RANGED_WEAPON,
            SLOW_ASTEROID_FIELD, CARRIER_LOADING,
            INEFFECTIVE_AGAINST_SMALL, MAIN_GUN_LIMITED_AMMO
    }, FiringRenderer.THREE_UNITS, "This unit serves as a medium range missile platform, with limited ammo capacity. " +
            "As a ranged unit, it doesn't receive counterattacks, while also not being able to counterattack enemies when attacked. It also " +
            "has reduced armour, and in general, should not be used on the frontline without support."),

    SUPPLY = new CorvetteType("supply", "Supply Unit", 10, 5.5f, 3.5f, new Action[]{
            Action.MOVE, Action.REPAIR, Action.RESUPPLY
    }, 1, 22, list -> {
    }, map -> {
        map.put(UnitCharacteristic.DEFENCE, UnitCharacteristicValue.MODERATE_GOOD);
        map.put(UnitCharacteristic.SPEED, UnitCharacteristicValue.GOOD);
        map.put(UnitCharacteristic.VIEW_RANGE, UnitCharacteristicValue.GOOD);
        map.put(UnitCharacteristic.REPAIR, UnitCharacteristicValue.MODERATE);
    }, (map, perTurnMap) -> {
        map.put(Action.CAPTURE, 6);
        map.put(Action.REPAIR, 12);
        map.put(Action.RESUPPLY, 8);
    }, new AttributeData[]{
            AttributeData.SUPPLY, REPAIR,
            SLOW_ASTEROID_FIELD, CARRIER_LOADING,
            NO_WEAPON
    }, FiringRenderer.TWO_UNITS, "This unit's primary goal is to support other units with its repair and resupply actions. " +
            "Having one of these is almost a necessity when operating units with limited ammo, and its repair action, although it costs quite a bit of " + EnergyManager.displayName + ", " +
            "can be a useful asset in many situations. Keep in mind that this unit does not feature any kind of weaponry to defend itself with.")
            .modify(u -> u.setRepair(3));

    CorvetteType(String name, String displayName, float hitPoints, float maxMovement, float maxViewRange, Action[] actions, int firingAnimFrames, float firingAnimUnitWidth, Consumer<ArrayList<WeaponTemplate>> weaponGenerator, Consumer<TreeMap<UnitCharacteristic, UnitCharacteristicValue>> unitCharacteristicSetter, BiConsumer<HashMap<Action, Integer>, HashMap<Action, Integer>> actionCostSetter, AttributeData[] infoAttributes, Supplier<ObjPos[]> firingPositions, String description) {
        super(name, displayName, hitPoints, maxMovement, maxViewRange, actions, firingAnimFrames, firingAnimUnitWidth, weaponGenerator, unitCharacteristicSetter, actionCostSetter, infoAttributes, firingPositions, description);
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
        return 0;
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
    public float moveCost(TileType type) {
        return switch (type) {
            case EMPTY -> 1f;
            case NEBULA -> 1.7f;
            case DENSE_NEBULA -> 1.9f;
            case ASTEROIDS -> maxMovement * 2 / 3;
        };
    }

    @Override
    public float viewRange(TileType type) {
        return switch (type) {
            case EMPTY -> 1f;
            case NEBULA -> 1.7f;
            case DENSE_NEBULA -> 100f;
            case ASTEROIDS -> 1.5f;
        };
    }

    @Override
    protected ShipClass getShipClass() {
        return ShipClass.CORVETTE;
    }

    @Override
    public CorvetteType modify(Consumer<UnitType> action) {
        super.modify(action);
        return this;
    }
}
