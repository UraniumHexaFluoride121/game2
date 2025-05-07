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

public class CruiserType extends UnitType {
    public static final CruiserType CRUISER = new CruiserType("cruiser", "Cruiser", 12, 3f, 3f, type -> switch (type) {
        case EMPTY -> 1f;
        case NEBULA -> 1.3f;
        case DENSE_NEBULA -> 1.5f;
        case ASTEROIDS -> 100f;
    }, type -> switch (type) {
        case EMPTY -> 1f;
        case NEBULA -> 1.7f;
        case DENSE_NEBULA -> 100f;
        case ASTEROIDS -> 1.5f;
    }, new Action[]{
            Action.FIRE, Action.MOVE
    }, 1, 25, list -> {
        WeaponTemplate w = new WeaponTemplate(ProjectileType.CRUISER_RAILGUN, WeaponType.RAILGUN);
        float s = 3.0f;
        w.addDamageType(DamageType.FIGHTER, UnitCharacteristicValue.LOW_MODERATE);
        w.addDamageType(DamageType.CORVETTE, UnitCharacteristicValue.GOOD_HIGH);
        w.addDamageType(DamageType.CRUISER, UnitCharacteristicValue.HIGH);
        w.addDamageType(DamageType.CAPITAL_SHIP, UnitCharacteristicValue.MODERATE_GOOD);
        w.addDamageType(DamageType.SHIELD, UnitCharacteristicValue.GOOD);
        w.addData("fighter", new AttackData(1.3f, s));
        w.addData("bomber", new AttackData(1.2f, s));
        w.addData("scout", new AttackData(1.0f, s));
        w.addData("corvette", new AttackData(4.6f, s));
        w.addData("defender", new AttackData(4.3f, s));
        w.addData("artillery", new AttackData(5.0f, s));
        w.addData("supply", new AttackData(4.4f, s));
        w.addData("cruiser", new AttackData(6.8f, s));
        w.addData("miner", new AttackData(7.3f, s));
        list.add(w);
    }, map -> {
        map.put(UnitCharacteristic.DEFENCE, UnitCharacteristicValue.GOOD_HIGH);
        map.put(UnitCharacteristic.SPEED, UnitCharacteristicValue.MODERATE);
        map.put(UnitCharacteristic.FIREPOWER, UnitCharacteristicValue.GOOD);
        map.put(UnitCharacteristic.VIEW_RANGE, UnitCharacteristicValue.MODERATE);
        map.put(UnitCharacteristic.FIRING_RANGE, UnitCharacteristicValue.LOW);
    }, (map, perTurnMap) -> {
        map.put(Action.CAPTURE, 10);
        map.put(Action.FIRE, 12);
    }, new AttributeData[]{
            ANTI_CORVETTE, ANTI_CRUISER,
            NO_ASTEROID_FIELD, INEFFECTIVE_AGAINST_FIGHTER
    }, FiringRenderer.TWO_UNITS, "The base variant of the cruiser class. Comes with strong armour and a powerful armour-piercing railgun. This railgun performs " +
            "well against well-armoured units, especially other cruiser-class units."),

    MINER = new CruiserType("miner", "Mining Unit", 6, 4f, 2.8f, type -> switch (type) {
        case EMPTY -> 1f;
        case NEBULA -> 1.3f;
        case DENSE_NEBULA -> 1.5f;
        case ASTEROIDS -> 1.2f;
    }, type -> switch (type) {
        case EMPTY -> 1f;
        case NEBULA -> 1.7f;
        case DENSE_NEBULA -> 100f;
        case ASTEROIDS -> 1.5f;
    }, new Action[]{
            Action.MOVE, Action.MINE
    }, 1, 32, list -> {
    }, map -> {
        map.put(UnitCharacteristic.DEFENCE, UnitCharacteristicValue.LOW);
        map.put(UnitCharacteristic.SPEED, UnitCharacteristicValue.MODERATE_GOOD);
        map.put(UnitCharacteristic.VIEW_RANGE, UnitCharacteristicValue.LOW_MODERATE);
    }, (map, perTurnMap) -> {
        map.put(Action.CAPTURE, 8);
        perTurnMap.put(Action.MINE, -12);
    }, new AttributeData[]{
            MINING, QUICK_ASTEROID_FIELD,
            NO_WEAPON, LOW_HP
    }, FiringRenderer.ONE_UNIT, "While it doesn't have any weapons, this unit plays a crucial role in successful fleet operations. " +
            "When placed on an asteroid field, using the Mine action will begin extracting " + EnergyManager.displayName + " over several turns, increasing income, until " +
            "the asteroid field is depleted. Lacks the level of armour plating seen on other cruisers, which has the side effect of reducing " + EnergyManager.displayName + " cost when moving.") {
        @Override
        public float damageReduction(TileType type) {
            return type == TileType.ASTEROIDS ? 0.86f : super.damageReduction(type);
        }

        @Override
        public float movementFixedCost() {
            return 0;
        }

        @Override
        public float movementCostMultiplier() {
            return 1;
        }
    };

    CruiserType(String name, String displayName, float hitPoints, float maxMovement, float maxViewRange, Function<TileType, Float> tileMovementCostFunction, Function<TileType, Float> tileViewRangeCostFunction, Action[] actions, int firingAnimFrames, float firingAnimUnitWidth, Consumer<ArrayList<WeaponTemplate>> weaponGenerator, Consumer<TreeMap<UnitCharacteristic, UnitCharacteristicValue>> unitCharacteristicSetter, BiConsumer<HashMap<Action, Integer>, HashMap<Action, Integer>> actionCostSetter, AttributeData[] infoAttributes, Supplier<ObjPos[]> firingPositions, String description) {
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
        return ShipClass.CRUISER;
    }

    @Override
    public float movementCostMultiplier() {
        return 3;
    }

    @Override
    public float movementFixedCost() {
        return 1;
    }

    @Override
    public CruiserType modify(Consumer<UnitType> action) {
        super.modify(action);
        return this;
    }
}
