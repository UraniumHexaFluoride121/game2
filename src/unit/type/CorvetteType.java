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
        WeaponTemplate w = new WeaponTemplate(ProjectileType.CORVETTE_CANNON);
        float s = 3.4f;
        w.addData("fighter", new AttackData(1.6f, s));
        w.addData("bomber", new AttackData(1.4f, s));
        w.addData("corvette", new AttackData(5.2f, s));
        w.addData("defender", new AttackData(4.8f, s));
        list.add(w);
    }, map -> {
        map.put(UnitCharacteristic.DEFENCE, UnitCharacteristicValue.MODERATE);
        map.put(UnitCharacteristic.SPEED, UnitCharacteristicValue.GOOD);
        map.put(UnitCharacteristic.FIREPOWER, UnitCharacteristicValue.MODERATE);
        map.put(UnitCharacteristic.VIEW_RANGE, UnitCharacteristicValue.MODERATE);
        map.put(UnitCharacteristic.FIRING_RANGE, UnitCharacteristicValue.LOW);
        map.put(UnitCharacteristic.SHIELD, UnitCharacteristicValue.NONE);
    }, new AttributeData[]{
        ANTI_CORVETTE, BALANCED,
                SLOW_ASTEROID_FIELD, CARRIER_LOADING,
                INEFFECTIVE_AGAINST_FIGHTER, INEFFECTIVE_AGAINST_SHIELDS
    }, FiringRenderer.THREE_UNITS),

    DEFENDER = new CorvetteType("defender", "Defender", 10, 4f, 3f, type -> switch (type) {
        case EMPTY -> 1f;
        case NEBULA -> 1.5f;
        case DENSE_NEBULA -> 1.7f;
        case ASTEROIDS -> 4f;
    }, type -> switch (type) {
        case EMPTY -> 1f;
        case NEBULA -> 1.5f;
        case DENSE_NEBULA -> 100f;
        case ASTEROIDS -> 1.3f;
    }, new Action[]{
        Action.FIRE, Action.MOVE, Action.SHIELD_REGEN
    }, 1, 22, list -> {
        WeaponTemplate w = new WeaponTemplate(ProjectileType.DEFENDER_PLASMA);
        float s = 5.5f;
        w.addData("fighter", new AttackData(6.2f, s));
        w.addData("bomber", new AttackData(5.8f, s));
        w.addData("corvette", new AttackData(2.7f, s));
        w.addData("defender", new AttackData(2.5f, s));
        list.add(w);
    }, map -> {
        map.put(UnitCharacteristic.DEFENCE, UnitCharacteristicValue.GOOD_HIGH);
        map.put(UnitCharacteristic.SPEED, UnitCharacteristicValue.MODERATE);
        map.put(UnitCharacteristic.FIREPOWER, UnitCharacteristicValue.MODERATE);
        map.put(UnitCharacteristic.VIEW_RANGE, UnitCharacteristicValue.LOW_MODERATE);
        map.put(UnitCharacteristic.FIRING_RANGE, UnitCharacteristicValue.LOW);
        map.put(UnitCharacteristic.SHIELD, UnitCharacteristicValue.MODERATE);
    }, new AttributeData[]{
        ANTI_FIGHTER, ANTI_SHIELD, HAS_SHIELD,
                SLOW_ASTEROID_FIELD, CARRIER_LOADING,
                INEFFECTIVE_AGAINST_LARGE, LOW_VIEW_RANGE
    }, FiringRenderer.TWO_UNITS).addShield(4, 1.5f, 27);

    CorvetteType(String name, String displayName, float hitPoints, float maxMovement, float maxViewRange, Function<TileType, Float> tileMovementCostFunction, Function<TileType, Float> tileViewRangeCostFunction, Action[] actions, int firingAnimFrames, float firingAnimUnitWidth, Consumer<ArrayList<WeaponTemplate>> weaponGenerator, Consumer<HashMap<UnitCharacteristic, UnitCharacteristicValue>> unitCharacteristicSetter, AttributeData[] infoAttributes, Supplier<ObjPos[]> firingPositions) {
        super(name, displayName, hitPoints, maxMovement, maxViewRange, tileMovementCostFunction, tileViewRangeCostFunction, actions, firingAnimFrames, firingAnimUnitWidth, weaponGenerator, unitCharacteristicSetter, infoAttributes, firingPositions);
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
    protected Function<TileType, Float> getDamageReduction() {
        return type -> switch (type) {
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
