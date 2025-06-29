package unit.type;

import foundation.math.ObjPos;
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
import unit.weapon.ProjectileType;
import unit.weapon.WeaponTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static unit.info.AttributeData.*;
import static unit.stats.modifiers.WeaponDamageModifier.*;

public class FighterType extends UnitType {
    public static final FighterType
            FIGHTER = new FighterType("fighter", "Fighter", 8, 7f, 3.8f, new Action[]{
            Action.FIRE, Action.MOVE
    }, 1, 15, 1, 1, 3, list -> {
        WeaponTemplate w = new WeaponTemplate(ProjectileType.FIGHTER_PLASMA);
        w
                .classModifier(ShipClass.FIGHTER, STRENGTH_2)
                .classModifier(ShipClass.CORVETTE, NORMAL_STRENGTH)
                .classModifier(ShipClass.CRUISER, WEAKNESS_2)
                .classModifier(ShipClass.CAPITAL_SHIP, WEAKNESS_3);
        list.add(w);
    }, map -> {
        map.put(UnitCharacteristic.DEFENCE, UnitCharacteristicValue.NONE_LOW);
        map.put(UnitCharacteristic.SPEED, UnitCharacteristicValue.HIGH_MAX);
        map.put(UnitCharacteristic.FIREPOWER, UnitCharacteristicValue.LOW);
        map.put(UnitCharacteristic.VIEW_RANGE, UnitCharacteristicValue.GOOD_HIGH);
        map.put(UnitCharacteristic.FIRING_RANGE, UnitCharacteristicValue.LOW);
    }, (map, perTurnMap) -> {
        map.put(Action.CAPTURE, 4);
        map.put(Action.FIRE, 7);
    }, new AttributeData[]{
            HIGH_MOVEMENT_SPEED, QUICK_ASTEROID_FIELD, ANTI_FIGHTER, ANTI_SHIELD,
            CARRIER_LOADING,
            INEFFECTIVE_AGAINST_LARGE
    }, FiringRenderer.THREE_UNITS, "Basic fighter-class unit. Excels in dogfights with other fighter-class units. " +
            "Its high speed makes this unit great for capturing structures, and when combined with its above average view distance, it makes for a decent improvised scout unit. " +
            "It is, however, mostly useless for destroying larger units."),

    BOMBER = new FighterType("bomber", "Bomber", 7, 6f, 3f, new Action[]{
            Action.FIRE, Action.MOVE
    }, 3, 15, 1, 1, 4, list -> {
        WeaponTemplate w = new WeaponTemplate(ProjectileType.BOMBER_MISSILE).runAnim();
        w
                .classModifier(ShipClass.FIGHTER, WEAKNESS_2)
                .classModifier(ShipClass.CORVETTE, NORMAL_STRENGTH)
                .classModifier(ShipClass.CRUISER, STRENGTH_1)
                .classModifier(ShipClass.CAPITAL_SHIP, STRENGTH_2);
        list.add(w);
    }, map -> {
        map.put(UnitCharacteristic.DEFENCE, UnitCharacteristicValue.LOW);
        map.put(UnitCharacteristic.SPEED, UnitCharacteristicValue.HIGH);
        map.put(UnitCharacteristic.FIREPOWER, UnitCharacteristicValue.GOOD);
        map.put(UnitCharacteristic.VIEW_RANGE, UnitCharacteristicValue.MODERATE);
        map.put(UnitCharacteristic.FIRING_RANGE, UnitCharacteristicValue.LOW);
    }, (map, perTurnMap) -> {
        map.put(Action.CAPTURE, 4);
        map.put(Action.FIRE, 8);
    }, new AttributeData[]{
            HIGH_MOVEMENT_SPEED, QUICK_ASTEROID_FIELD, ANTI_CAPITAL_SHIP_MISSILES,
            CARRIER_LOADING,
            LIMITED_AMMO
    }, FiringRenderer.THREE_UNITS, "This special variant of the Fighter unit comes equipped with missiles that deal high damage against cruiser-class units and capital ships. " +
            "It only has an ammo capacity of one, making resupply an especially important consideration when using this unit. Compared to the regular Fighter, " +
            "it has less view distance and is more vulnerable than the regular Fighter.")
            .modify(u -> u.setAmmoCapacity(1)), //Change unit description if ammo capacity is changed

    SCOUT = new FighterType("scout", "Scout", 5, 8f, 5f, new Action[]{
            Action.FIRE, Action.MOVE, Action.STEALTH
    }, 1, 15, 1, 1, 2, list -> {
        WeaponTemplate w = new WeaponTemplate(ProjectileType.SCOUT_PLASMA);
        w
                .classModifier(ShipClass.FIGHTER, STRENGTH_2)
                .classModifier(ShipClass.CORVETTE, NORMAL_STRENGTH)
                .classModifier(ShipClass.CRUISER, WEAKNESS_2)
                .classModifier(ShipClass.CAPITAL_SHIP, WEAKNESS_3);
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
    }, FiringRenderer.THREE_UNITS, "The scout is a purpose-built unit that specialises in reconnaissance. It has exceptional " + ModifierCategory.MOVEMENT_SPEED_DISPLAY.getName().toLowerCase() + " and view range, and comes equipped with the stealth ability, " +
            "allowing the unit to stay hidden while performing reconnaissance. While it does have weapons, their firepower leaves much to be desired, and are not intended for frontline use. Can be used in a pinch to " +
            "destroy low HP enemies.")
            .modify(UnitType::noCapture);

    FighterType(String name, String displayName, float hitPoints, float maxMovement, float maxViewRange, Action[] actions, int firingAnimFrames, float firingAnimUnitWidth, int minRange, int maxRange, float damage, Consumer<ArrayList<WeaponTemplate>> weaponGenerator, Consumer<TreeMap<UnitCharacteristic, UnitCharacteristicValue>> unitCharacteristicSetter, BiConsumer<HashMap<Action, Integer>, HashMap<Action, Integer>> actionCostSetter, AttributeData[] infoAttributes, Supplier<ObjPos[]> firingPositions, String description) {
        super(name, displayName, hitPoints, maxMovement, maxViewRange, actions, firingAnimFrames, firingAnimUnitWidth, minRange, maxRange, damage, weaponGenerator, unitCharacteristicSetter, actionCostSetter, infoAttributes, firingPositions, description);
    }

    @Override
    public void addModifiers(ArrayList<Modifier> list) {
        list.add(MovementModifier.FAST_ASTEROID_FIELDS);
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
        return ShipClass.FIGHTER;
    }

    @Override
    public float movementCostMultiplier() {
        return 1;
    }

    @Override
    public FighterType modify(Consumer<UnitType> action) {
        super.modify(action);
        return this;
    }
}
