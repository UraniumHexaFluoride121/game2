package unit.type;

import foundation.math.ObjPos;
import level.energy.EnergyManager;
import render.level.FiringRenderer;
import render.save.SerializationProxy;
import unit.ShipClass;
import unit.action.Action;
import unit.info.UnitCharacteristic;
import unit.info.UnitCharacteristicValue;
import unit.stats.attribute.UnitAttribute;
import unit.stats.modifiers.groups.MovementModifier;
import unit.stats.modifiers.types.Modifier;
import unit.stats.modifiers.types.ModifierCategory;
import unit.weapon.ProjectileType;
import unit.weapon.WeaponTemplate;

import java.io.ObjectStreamException;
import java.io.Serial;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static unit.stats.modifiers.groups.WeaponDamageModifier.*;

public class CorvetteType extends UnitType {
    public static final CorvetteType FRIGATE = new CorvetteType("frigate", "Frigate", 110, 10, 5f, 3.5f, new Action[]{
            Action.FIRE, Action.MOVE
    }, 1, 17, 1, 1, 3.5f, list -> {
        WeaponTemplate w = new WeaponTemplate(ProjectileType.CORVETTE_CANNON);
        w
                .classModifier(ShipClass.FIGHTER, WEAPON_WEAKNESS)
                .classModifier(ShipClass.CORVETTE, WEAPON_STRENGTH)
                .classModifier(ShipClass.CRUISER, WEAPON_STRENGTH);
        list.add(w);
    }, map -> {
        map.put(UnitCharacteristic.DEFENCE, UnitCharacteristicValue.MODERATE);
        map.put(UnitCharacteristic.SPEED, UnitCharacteristicValue.GOOD);
        map.put(UnitCharacteristic.FIREPOWER, UnitCharacteristicValue.MODERATE);
        map.put(UnitCharacteristic.VIEW_RANGE, UnitCharacteristicValue.GOOD);
        map.put(UnitCharacteristic.FIRING_RANGE, UnitCharacteristicValue.LOW);
    }, (map, perTurnMap) -> {
        map.put(Action.FIRE, 10);
    }, FiringRenderer.THREE_UNITS, "Standard " + ShipClass.CORVETTE.getClassName().toLowerCase() + " unit with moderate speed and armour. Excellent for destroying other corvette-class units."),

    DEFENDER = new CorvetteType("defender", "Defender", 140, 8, 4.5f, 2.5f, new Action[]{
            Action.FIRE, Action.MOVE, Action.SHIELD_REGEN
    }, 1, 22, 1, 1, 4, list -> {
        WeaponTemplate w = new WeaponTemplate(ProjectileType.DEFENDER_PLASMA);
        w
                .classModifier(ShipClass.FIGHTER, WEAPON_STRENGTH)
                .classModifier(ShipClass.CORVETTE, NORMAL_STRENGTH)
                .classModifier(ShipClass.CRUISER, WEAPON_WEAKNESS);
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
        map.put(Action.FIRE, 12);
        map.put(Action.SHIELD_REGEN, 8);
    }, FiringRenderer.TWO_UNITS, "This unit is primarily designed to support larger units in destroying " + ShipClass.FIGHTER.getClassNamePlural().toLowerCase() + " enemies using high-power plasma guns. " +
            "Features an advanced shield system with decent durability, but has reduced view range and " + ModifierCategory.MOVEMENT_SPEED_DISPLAY.getName().toLowerCase() + ".")
            .addShield(3, 1.5f, 27)
            .modify(u -> u.setAttributes(UnitAttribute.DEFENCE_NETWORK)),

    ARTILLERY = new CorvetteType("artillery", "Artillery", 130, 8, 4.5f, 3.5f, new Action[]{
            Action.FIRE, Action.MOVE
    }, 1, 16, 2, 3, 3.5f, list -> {
        WeaponTemplate w = new WeaponTemplate(ProjectileType.ARTILLERY_MISSILE);
        w
                .classModifier(ShipClass.FIGHTER, WEAPON_WEAKNESS)
                .classModifier(ShipClass.CORVETTE, NORMAL_STRENGTH)
                .classModifier(ShipClass.CRUISER, WEAPON_STRENGTH);
        w.noCounterattack();
        list.add(w);
    }, map -> {
        map.put(UnitCharacteristic.DEFENCE, UnitCharacteristicValue.MODERATE);
        map.put(UnitCharacteristic.SPEED, UnitCharacteristicValue.MODERATE);
        map.put(UnitCharacteristic.FIREPOWER, UnitCharacteristicValue.GOOD_HIGH);
        map.put(UnitCharacteristic.VIEW_RANGE, UnitCharacteristicValue.LOW_MODERATE);
        map.put(UnitCharacteristic.FIRING_RANGE, UnitCharacteristicValue.GOOD);
    }, (map, perTurnMap) -> {
        map.put(Action.FIRE, 12);
    }, FiringRenderer.THREE_UNITS, "This unit serves as a medium-range missile platform, with limited ammo capacity. " +
            "As a ranged unit, it doesn't receive counterattacks, while also not being able to counterattack enemies when attacked. It also " +
            "has reduced armour, and in general, should not be used on the frontline without support.")
            .modify(u -> u.setAmmoCapacity(3).useArticleAn()),

    SUPPLY = new CorvetteType("supply", "Supply Unit", 100, 10, 5.5f, 3.5f, new Action[]{
            Action.MOVE, Action.REPAIR, Action.RESUPPLY
    }, 1, 22, 1, 1, 0, list -> {
    }, map -> {
        map.put(UnitCharacteristic.DEFENCE, UnitCharacteristicValue.MODERATE_GOOD);
        map.put(UnitCharacteristic.SPEED, UnitCharacteristicValue.GOOD);
        map.put(UnitCharacteristic.VIEW_RANGE, UnitCharacteristicValue.GOOD);
        map.put(UnitCharacteristic.REPAIR, UnitCharacteristicValue.MODERATE);
    }, (map, perTurnMap) -> {
        map.put(Action.REPAIR, 12);
        map.put(Action.RESUPPLY, 8);
    }, FiringRenderer.TWO_UNITS, "This unit's primary goal is to support other units with its repair and resupply actions. " +
            "Having one of these is almost a necessity when operating units with limited ammo, and its repair action, although it costs quite a bit of " + EnergyManager.displayName + ", " +
            "can be a useful asset in many situations. Keep in mind that this unit does not feature any kind of weaponry to defend itself with.")
            .modify(u -> u.setRepair(3));

    CorvetteType(String name, String displayName, int value, float hitPoints, float maxMovement, float maxViewRange, Action[] actions, int firingAnimFrames, float firingAnimUnitWidth, int minRange, int maxRange, float damage, Consumer<ArrayList<WeaponTemplate>> weaponGenerator, Consumer<TreeMap<UnitCharacteristic, UnitCharacteristicValue>> unitCharacteristicSetter, BiConsumer<HashMap<Action, Integer>, HashMap<Action, Integer>> actionCostSetter, Supplier<ObjPos[]> firingPositions, String description) {
        super(name, displayName, value, hitPoints, maxMovement, maxViewRange, actions, firingAnimFrames, firingAnimUnitWidth, minRange, maxRange, damage, weaponGenerator, unitCharacteristicSetter, actionCostSetter, firingPositions, description);
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
    protected ShipClass getShipClass() {
        return ShipClass.CORVETTE;
    }

    @Override
    public CorvetteType modify(Consumer<UnitType> action) {
        super.modify(action);
        return this;
    }

    @Serial
    private Object writeReplace() throws ObjectStreamException {
        return new SerializationProxy<>(UnitType.class, this);
    }
}
