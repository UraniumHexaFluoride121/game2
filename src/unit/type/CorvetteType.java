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
import unit.stats.Modifier;
import unit.stats.ModifierCategory;
import unit.stats.modifiers.MovementModifier;
import unit.weapon.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static unit.info.AttributeData.*;
import static unit.stats.modifiers.WeaponDamageModifier.*;
import static unit.stats.modifiers.WeaponDamageModifier.WEAKNESS_2;
import static unit.stats.modifiers.WeaponDamageModifier.WEAKNESS_3;

public class CorvetteType extends UnitType {
    public static final CorvetteType CORVETTE = new CorvetteType("corvette", "Corvette", 10, 5f, 3.5f, new Action[]{
            Action.FIRE, Action.MOVE
    }, 1, 17, 1, 1, 3.5f, list -> {
        WeaponTemplate w = new WeaponTemplate(ProjectileType.CORVETTE_CANNON);
        w
                .classModifier(ShipClass.FIGHTER, WEAKNESS_1)
                .classModifier(ShipClass.CORVETTE, STRENGTH_3)
                .classModifier(ShipClass.CRUISER, STRENGTH_1)
                .classModifier(ShipClass.CAPITAL_SHIP, WEAKNESS_1);
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
    }, FiringRenderer.THREE_UNITS, "Standard corvette-class unit with moderate speed and armour. Excellent for destroying other corvette-class units."),

    DEFENDER = new CorvetteType("defender", "Defender", 8, 4.5f, 2.5f, new Action[]{
            Action.FIRE, Action.MOVE, Action.SHIELD_REGEN
    }, 1, 22, 1, 1, 4, list -> {
        WeaponTemplate w = new WeaponTemplate(ProjectileType.DEFENDER_PLASMA);
        w
                .classModifier(ShipClass.FIGHTER, STRENGTH_3)
                .classModifier(ShipClass.CORVETTE, STRENGTH_1)
                .classModifier(ShipClass.CRUISER, WEAKNESS_2)
                .classModifier(ShipClass.CAPITAL_SHIP, WEAKNESS_3);
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
    }, FiringRenderer.TWO_UNITS, "This unit is primarily designed to support larger units in destroying fighter-class enemies using high-power plasma guns. " +
            "Features an advanced shield system with decent durability, but has reduced view range and " + ModifierCategory.MOVEMENT_SPEED_DISPLAY.getName().toLowerCase() + ".")
            .addShield(3, 1, 27),

    ARTILLERY = new CorvetteType("artillery", "Artillery", 8, 4.5f, 3.5f, new Action[]{
            Action.FIRE, Action.MOVE
    }, 1, 16, 2, 3, 3.5f, list -> {
        WeaponTemplate w = new WeaponTemplate(ProjectileType.ARTILLERY_MISSILE);
        w
                .classModifier(ShipClass.FIGHTER, WEAKNESS_2)
                .classModifier(ShipClass.CORVETTE, NORMAL_STRENGTH)
                .classModifier(ShipClass.CRUISER, STRENGTH_1)
                .classModifier(ShipClass.CAPITAL_SHIP, STRENGTH_2);
        w.noCounterattack();
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
            INEFFECTIVE_AGAINST_SMALL, LIMITED_AMMO
    }, FiringRenderer.THREE_UNITS, "This unit serves as a medium-range missile platform, with limited ammo capacity. " +
            "As a ranged unit, it doesn't receive counterattacks, while also not being able to counterattack enemies when attacked. It also " +
            "has reduced armour, and in general, should not be used on the frontline without support.")
            .modify(u -> u.setAmmoCapacity(3)),

    SUPPLY = new CorvetteType("supply", "Supply Unit", 10, 5.5f, 3.5f, new Action[]{
            Action.MOVE, Action.REPAIR, Action.RESUPPLY
    }, 1, 22, 1, 1, 0, list -> {
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

    CorvetteType(String name, String displayName, float hitPoints, float maxMovement, float maxViewRange, Action[] actions, int firingAnimFrames, float firingAnimUnitWidth, int minRange, int maxRange, float damage, Consumer<ArrayList<WeaponTemplate>> weaponGenerator, Consumer<TreeMap<UnitCharacteristic, UnitCharacteristicValue>> unitCharacteristicSetter, BiConsumer<HashMap<Action, Integer>, HashMap<Action, Integer>> actionCostSetter, AttributeData[] infoAttributes, Supplier<ObjPos[]> firingPositions, String description) {
        super(name, displayName, hitPoints, maxMovement, maxViewRange, actions, firingAnimFrames, firingAnimUnitWidth, minRange, maxRange, damage, weaponGenerator, unitCharacteristicSetter, actionCostSetter, infoAttributes, firingPositions, description);
    }

    @Override
    public CorvetteType addShield(float shieldHP, float shieldRegen, float firingAnimShieldWidth) {
        super.addShield(shieldHP, shieldRegen, firingAnimShieldWidth);
        return this;
    }

    @Override
    public void addModifiers(ArrayList<Modifier> list) {
        list.add(MovementModifier.NORMAL_ASTEROID_FIELDS);
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
