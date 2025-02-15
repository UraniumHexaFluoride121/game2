package unit.type;

import foundation.math.ObjPos;
import level.tile.TileType;
import render.texture.FiringRenderer;
import unit.ShipClass;
import unit.action.Action;
import unit.info.AttributeData;
import unit.info.UnitCharacteristic;
import unit.info.UnitCharacteristicValue;
import unit.weapon.AttackData;
import unit.weapon.ProjectileType;
import unit.weapon.WeaponTemplate;

import java.util.ArrayList;
import java.util.HashMap;
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
        WeaponTemplate w = new WeaponTemplate(ProjectileType.FIGHTER_PLASMA);
        float s = 5f;
        w.addData("fighter", new AttackData(3.8f, s));
        w.addData("bomber", new AttackData(3.2f, s));
        w.addData("corvette", new AttackData(1.8f, s));
        w.addData("defender", new AttackData(1.6f, s));
        list.add(w);
    }, map -> {
        map.put(UnitCharacteristic.DEFENCE, UnitCharacteristicValue.NONE_LOW);
        map.put(UnitCharacteristic.SPEED, UnitCharacteristicValue.HIGH_MAX);
        map.put(UnitCharacteristic.FIREPOWER, UnitCharacteristicValue.LOW);
        map.put(UnitCharacteristic.VIEW_RANGE, UnitCharacteristicValue.GOOD);
        map.put(UnitCharacteristic.FIRING_RANGE, UnitCharacteristicValue.LOW);
        map.put(UnitCharacteristic.SHIELD, UnitCharacteristicValue.NONE);
    }, new AttributeData[]{
            HIGH_MOVEMENT_SPEED, QUICK_ASTEROID_FIELD, ANTI_FIGHTER, ANTI_SHIELD,
            CARRIER_LOADING,
            INEFFECTIVE_AGAINST_LARGE
    }, FiringRenderer.THREE_UNITS),

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
        WeaponTemplate w1 = new WeaponTemplate(ProjectileType.BOMBER_MISSILE).consumeAmmo(1).runAnim();
        float s1 = 0.4f;
        w1.addData("fighter", new AttackData(0.8f, s1));
        w1.addData("bomber", new AttackData(0.7f, s1));
        w1.addData("corvette", new AttackData(2.4f, s1));
        w1.addData("defender", new AttackData(3.2f, s1));
        list.add(w1);
        WeaponTemplate w2 = new WeaponTemplate(ProjectileType.BOMBER_PLASMA);
        float s2 = 4.4f;
        w2.addData("fighter", new AttackData(3.2f, s2));
        w2.addData("bomber", new AttackData(2.6f, s2));
        w2.addData("corvette", new AttackData(1.6f, s2));
        w2.addData("defender", new AttackData(1.5f, s2));
        list.add(w2);
    }, map -> {
        map.put(UnitCharacteristic.DEFENCE, UnitCharacteristicValue.LOW);
        map.put(UnitCharacteristic.SPEED, UnitCharacteristicValue.HIGH);
        map.put(UnitCharacteristic.FIREPOWER, UnitCharacteristicValue.GOOD);
        map.put(UnitCharacteristic.VIEW_RANGE, UnitCharacteristicValue.MODERATE);
        map.put(UnitCharacteristic.FIRING_RANGE, UnitCharacteristicValue.LOW);
        map.put(UnitCharacteristic.SHIELD, UnitCharacteristicValue.NONE);
    }, new AttributeData[]{
            HIGH_MOVEMENT_SPEED, QUICK_ASTEROID_FIELD, ANTI_CAPITAL_SHIP_MISSILES,
            CARRIER_LOADING,
            MAIN_GUN_LIMITED_AMMO
    }, FiringRenderer.THREE_UNITS);

    FighterType(String name, String displayName, float hitPoints, float maxMovement, float maxViewRange, Function<TileType, Float> tileMovementCostFunction, Function<TileType, Float> tileViewRangeCostFunction, Action[] actions, int firingAnimFrames, float firingAnimUnitWidth, Consumer<ArrayList<WeaponTemplate>> weaponGenerator, Consumer<HashMap<UnitCharacteristic, UnitCharacteristicValue>> unitCharacteristicSetter, AttributeData[] infoAttributes, Supplier<ObjPos[]> firingPositions) {
        super(name, displayName, hitPoints, maxMovement, maxViewRange, tileMovementCostFunction, tileViewRangeCostFunction, actions, firingAnimFrames, firingAnimUnitWidth, weaponGenerator, unitCharacteristicSetter, infoAttributes, firingPositions);
    }

    @Override
    protected Function<TileType, Float> getDamageReduction() {
        return type -> switch (type) {
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
}
