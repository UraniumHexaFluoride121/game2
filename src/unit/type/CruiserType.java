package unit.type;

import foundation.math.ObjPos;
import level.energy.EnergyManager;
import level.tile.TileType;
import render.level.FiringRenderer;
import render.save.SerializationProxy;
import unit.ShipClass;
import unit.action.Action;
import unit.info.UnitCharacteristic;
import unit.info.UnitCharacteristicValue;
import unit.stats.modifiers.groups.MovementModifier;
import unit.stats.modifiers.types.Modifier;
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

public class CruiserType extends UnitType {
    public static final CruiserType BATTLECRUISER = new CruiserType("battlecruiser", "Battlecruiser", 135, 14, 4f, 3f, new Action[]{
            Action.FIRE, Action.MOVE
    }, 1, 25, 1, 1, 4, list -> {
        WeaponTemplate w = new WeaponTemplate(ProjectileType.CRUISER_RAILGUN);
        w
                .classModifier(ShipClass.FIGHTER, WEAPON_WEAKNESS)
                .classModifier(ShipClass.CORVETTE, WEAPON_WEAKNESS)
                .classModifier(ShipClass.CRUISER, WEAPON_STRENGTH);
        list.add(w);
    }, map -> {
        map.put(UnitCharacteristic.DEFENCE, UnitCharacteristicValue.GOOD_HIGH);
        map.put(UnitCharacteristic.SPEED, UnitCharacteristicValue.MODERATE);
        map.put(UnitCharacteristic.FIREPOWER, UnitCharacteristicValue.GOOD);
        map.put(UnitCharacteristic.VIEW_RANGE, UnitCharacteristicValue.MODERATE);
        map.put(UnitCharacteristic.FIRING_RANGE, UnitCharacteristicValue.LOW);
    }, (map, perTurnMap) -> {
        map.put(Action.FIRE, 10);
    }, FiringRenderer.TWO_UNITS, "The base variant of the " + ShipClass.CRUISER.getClassName().toLowerCase() + ". Comes with strong armour and a powerful armour-piercing railgun. This railgun performs " +
            "well against armoured units, especially other " + ShipClass.CRUISER.getClassName().toLowerCase() + " units."),

    LIGHT_CRUISER = new CruiserType("light_cruiser", "Light Cruiser", 160, 12, 4.3f, 3f, new Action[]{
            Action.FIRE, Action.MOVE, Action.SHIELD_REGEN
    }, 1, 25, 1, 1, 4, list -> {
        WeaponTemplate w1 = new WeaponTemplate(ProjectileType.BATTLECRUISER_CANNON);
        w1
                .classModifier(ShipClass.FIGHTER, NORMAL_STRENGTH)
                .classModifier(ShipClass.CORVETTE, WEAPON_STRENGTH)
                .classModifier(ShipClass.CRUISER, NORMAL_STRENGTH);
        list.add(w1);
        WeaponTemplate w2 = new WeaponTemplate(ProjectileType.BATTLECRUISER_PLASMA);
        w2
                .classModifier(ShipClass.FIGHTER, WEAPON_STRENGTH)
                .classModifier(ShipClass.CORVETTE, NORMAL_STRENGTH)
                .classModifier(ShipClass.CRUISER, WEAPON_WEAKNESS);
        list.add(w2);
    }, map -> {
        map.put(UnitCharacteristic.DEFENCE, UnitCharacteristicValue.GOOD_HIGH);
        map.put(UnitCharacteristic.SPEED, UnitCharacteristicValue.MODERATE);
        map.put(UnitCharacteristic.FIREPOWER, UnitCharacteristicValue.GOOD);
        map.put(UnitCharacteristic.VIEW_RANGE, UnitCharacteristicValue.MODERATE);
        map.put(UnitCharacteristic.FIRING_RANGE, UnitCharacteristicValue.LOW);
    }, (map, perTurnMap) -> {
        map.put(Action.FIRE, 10);
        map.put(Action.SHIELD_REGEN, 6);
    }, FiringRenderer.TWO_UNITS_BACK, "This heavily armed " + ShipClass.CRUISER.getName().toLowerCase() + " comes equipped with multiple weapons systems that makes " +
            "it effective for destroying both " + ShipClass.FIGHTER.getClassName().toLowerCase() + " and " + ShipClass.CRUISER.getClassName().toLowerCase() + " units. It also features a shield system which improves its defensive capabilities.")
            .modify(u -> u.addShield(2, 1f, 33)),

    MINER = new CruiserType("miner", "Mining Unit", 15, 6, 4.5f, 2.8f, new Action[]{
            Action.MOVE, Action.MINE
    }, 1, 32, 1, 1, 0, list -> {
    }, map -> {
        map.put(UnitCharacteristic.DEFENCE, UnitCharacteristicValue.LOW);
        map.put(UnitCharacteristic.SPEED, UnitCharacteristicValue.MODERATE_GOOD);
        map.put(UnitCharacteristic.VIEW_RANGE, UnitCharacteristicValue.LOW_MODERATE);
    }, (map, perTurnMap) -> {
        perTurnMap.put(Action.MINE, -12);
    }, FiringRenderer.ONE_UNIT, "While it doesn't have any weapons, this unit plays a crucial role in any successful fleet operations. " +
            "When placed on " + TileType.ASTEROIDS.getNameArticle().toLowerCase() + ", using the Mine action will begin extracting " + EnergyManager.displayName + " over several turns, increasing income, until " +
            "the " + TileType.ASTEROIDS.getName().toLowerCase() + " is depleted. Lacks the level of armour plating seen on other " + ShipClass.CRUISER.getPluralName().toLowerCase() + ", which has the side effect of reducing " + EnergyManager.displayName + " cost when moving.") {
        @Override
        public float movementCostMultiplier() {
            return 1;
        }

        @Override
        public void addModifiers(ArrayList<Modifier> list) {
            super.addModifiers(list);
            list.set(list.indexOf(MovementModifier.NO_ASTEROID_FIELDS), MovementModifier.MINING_ASTEROID_FIELDS);
        }
    };

    CruiserType(String name, String displayName, int value, float hitPoints, float maxMovement, float maxViewRange, Action[] actions, int firingAnimFrames, float firingAnimUnitWidth, int minRange, int maxRange, float damage, Consumer<ArrayList<WeaponTemplate>> weaponGenerator, Consumer<TreeMap<UnitCharacteristic, UnitCharacteristicValue>> unitCharacteristicSetter, BiConsumer<HashMap<Action, Integer>, HashMap<Action, Integer>> actionCostSetter, Supplier<ObjPos[]> firingPositions, String description) {
        super(name, displayName, value, hitPoints, maxMovement, maxViewRange, actions, firingAnimFrames, firingAnimUnitWidth, minRange, maxRange, damage, weaponGenerator, unitCharacteristicSetter, actionCostSetter, firingPositions, description);
    }

    @Override
    public void addModifiers(ArrayList<Modifier> list) {
        list.add(MovementModifier.NO_ASTEROID_FIELDS);
    }

    @Override
    protected ShipClass getShipClass() {
        return ShipClass.CRUISER;
    }

    @Override
    public float movementCostMultiplier() {
        return 2;
    }

    @Override
    public CruiserType modify(Consumer<UnitType> action) {
        super.modify(action);
        return this;
    }

    @Serial
    private Object writeReplace() throws ObjectStreamException {
        return new SerializationProxy<>(UnitType.class, this);
    }
}
