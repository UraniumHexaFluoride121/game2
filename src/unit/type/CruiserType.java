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
        WeaponTemplate w = new WeaponTemplate(ProjectileType.CRUISER_RAIL_GUN, WeaponType.RAIL_GUN);
        float s = 2.2f;
        w.addDamageType(DamageType.FIGHTER, UnitCharacteristicValue.LOW_MODERATE);
        w.addDamageType(DamageType.CORVETTE, UnitCharacteristicValue.GOOD_HIGH);
        w.addDamageType(DamageType.CRUISER, UnitCharacteristicValue.HIGH);
        w.addDamageType(DamageType.CAPITAL_SHIP, UnitCharacteristicValue.MODERATE_GOOD);
        w.addDamageType(DamageType.SHIELD, UnitCharacteristicValue.MODERATE_GOOD);
        w.addData("fighter", new AttackData(1.3f, s));
        w.addData("bomber", new AttackData(1.2f, s));
        w.addData("scout", new AttackData(1.0f, s));
        w.addData("corvette", new AttackData(4.6f, s));
        w.addData("defender", new AttackData(4.3f, s));
        w.addData("artillery", new AttackData(5.0f, s));
        w.addData("cruiser", new AttackData(6.8f, s));
        list.add(w);
    }, map -> {
        map.put(UnitCharacteristic.DEFENCE, UnitCharacteristicValue.GOOD_HIGH);
        map.put(UnitCharacteristic.SPEED, UnitCharacteristicValue.MODERATE);
        map.put(UnitCharacteristic.FIREPOWER, UnitCharacteristicValue.GOOD);
        map.put(UnitCharacteristic.VIEW_RANGE, UnitCharacteristicValue.MODERATE);
        map.put(UnitCharacteristic.FIRING_RANGE, UnitCharacteristicValue.LOW);
        map.put(UnitCharacteristic.SHIELD, UnitCharacteristicValue.NONE);
    }, (map, perTurnMap) -> {
        map.put(Action.CAPTURE, 10);
        map.put(Action.FIRE, 12);
    }, new AttributeData[]{
            ANTI_CORVETTE, ANTI_CRUISER,
            CARRIER_LOADING,
            NO_ASTEROID_FIELD, INEFFECTIVE_AGAINST_FIGHTER
    }, FiringRenderer.TWO_UNITS);

    CruiserType(String name, String displayName, float hitPoints, float maxMovement, float maxViewRange, Function<TileType, Float> tileMovementCostFunction, Function<TileType, Float> tileViewRangeCostFunction, Action[] actions, int firingAnimFrames, float firingAnimUnitWidth, Consumer<ArrayList<WeaponTemplate>> weaponGenerator, Consumer<HashMap<UnitCharacteristic, UnitCharacteristicValue>> unitCharacteristicSetter, BiConsumer<HashMap<Action, Integer>, HashMap<Action, Integer>> actionCostSetter, AttributeData[] infoAttributes, Supplier<ObjPos[]> firingPositions) {
        super(name, displayName, hitPoints, maxMovement, maxViewRange, tileMovementCostFunction, tileViewRangeCostFunction, actions, firingAnimFrames, firingAnimUnitWidth, weaponGenerator, unitCharacteristicSetter, actionCostSetter, infoAttributes, firingPositions);
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
}
