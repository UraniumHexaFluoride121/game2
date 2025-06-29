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
import static unit.stats.modifiers.WeaponDamageModifier.STRENGTH_1;
import static unit.stats.modifiers.WeaponDamageModifier.STRENGTH_2;

public class CruiserType extends UnitType {
    public static final CruiserType CRUISER = new CruiserType("cruiser", "Cruiser", 14, 4f, 3f, new Action[]{
            Action.FIRE, Action.MOVE
    }, 1, 25, 1, 1, 4, list -> {
        WeaponTemplate w = new WeaponTemplate(ProjectileType.CRUISER_RAILGUN);
        w
                .classModifier(ShipClass.FIGHTER, WEAKNESS_1)
                .classModifier(ShipClass.CORVETTE, STRENGTH_1)
                .classModifier(ShipClass.CRUISER, STRENGTH_2)
                .classModifier(ShipClass.CAPITAL_SHIP, NORMAL_STRENGTH);
        list.add(w);
    }, map -> {
        map.put(UnitCharacteristic.DEFENCE, UnitCharacteristicValue.GOOD_HIGH);
        map.put(UnitCharacteristic.SPEED, UnitCharacteristicValue.MODERATE);
        map.put(UnitCharacteristic.FIREPOWER, UnitCharacteristicValue.GOOD);
        map.put(UnitCharacteristic.VIEW_RANGE, UnitCharacteristicValue.MODERATE);
        map.put(UnitCharacteristic.FIRING_RANGE, UnitCharacteristicValue.LOW);
    }, (map, perTurnMap) -> {
        map.put(Action.CAPTURE, 10);
        map.put(Action.FIRE, 10);
    }, new AttributeData[]{
            ANTI_CORVETTE, ANTI_CRUISER,
            NO_ASTEROID_FIELD, INEFFECTIVE_AGAINST_FIGHTER
    }, FiringRenderer.TWO_UNITS, "The base variant of the cruiser class. Comes with strong armour and a powerful armour-piercing railgun. This railgun performs " +
            "well against armoured units, especially other cruiser-class units."),

    BATTLECRUISER = new CruiserType("battlecruiser", "Battlecruiser", 12, 4.3f, 3f, new Action[]{
            Action.FIRE, Action.MOVE, Action.SHIELD_REGEN
    }, 1, 25, 1, 1, 4, list -> {
        WeaponTemplate w1 = new WeaponTemplate(ProjectileType.BATTLECRUISER_CANNON);
        w1
                .classModifier(ShipClass.FIGHTER, NORMAL_STRENGTH)
                .classModifier(ShipClass.CORVETTE, STRENGTH_2)
                .classModifier(ShipClass.CRUISER, NORMAL_STRENGTH)
                .classModifier(ShipClass.CAPITAL_SHIP, WEAKNESS_2);
        list.add(w1);
        WeaponTemplate w2 = new WeaponTemplate(ProjectileType.BATTLECRUISER_PLASMA);
        w2
                .classModifier(ShipClass.FIGHTER, STRENGTH_2)
                .classModifier(ShipClass.CORVETTE, NORMAL_STRENGTH)
                .classModifier(ShipClass.CRUISER, WEAKNESS_2)
                .classModifier(ShipClass.CAPITAL_SHIP, WEAKNESS_3);
        list.add(w2);
    }, map -> {
        map.put(UnitCharacteristic.DEFENCE, UnitCharacteristicValue.GOOD_HIGH);
        map.put(UnitCharacteristic.SPEED, UnitCharacteristicValue.MODERATE);
        map.put(UnitCharacteristic.FIREPOWER, UnitCharacteristicValue.GOOD);
        map.put(UnitCharacteristic.VIEW_RANGE, UnitCharacteristicValue.MODERATE);
        map.put(UnitCharacteristic.FIRING_RANGE, UnitCharacteristicValue.LOW);
    }, (map, perTurnMap) -> {
        map.put(Action.CAPTURE, 10);
        map.put(Action.FIRE, 10);
        map.put(Action.SHIELD_REGEN, 12);
    }, new AttributeData[]{
            ANTI_CORVETTE, ANTI_FIGHTER, HAS_SHIELD,
            NO_ASTEROID_FIELD, INEFFECTIVE_AGAINST_LARGE
    }, FiringRenderer.TWO_UNITS_BACK, "This heavily armed cruiser comes equipped with multiple weapons systems that makes " +
            "it effective for destroying both fighter-class and corvette-class units. It also features a shield system which improves its defensive capabilities.")
            .modify(u -> u.addShield(2, 0.8f, 33)),

    MINER = new CruiserType("miner", "Mining Unit", 6, 4.5f, 2.8f, new Action[]{
            Action.MOVE, Action.MINE
    }, 1, 32, 1, 1, 0, list -> {
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
    }, FiringRenderer.ONE_UNIT, "While it doesn't have any weapons, this unit plays a crucial role in any successful fleet operations. " +
            "When placed on an asteroid field, using the Mine action will begin extracting " + EnergyManager.displayName + " over several turns, increasing income, until " +
            "the asteroid field is depleted. Lacks the level of armour plating seen on other cruisers, which has the side effect of reducing " + EnergyManager.displayName + " cost when moving.") {
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

    CruiserType(String name, String displayName, float hitPoints, float maxMovement, float maxViewRange, Action[] actions, int firingAnimFrames, float firingAnimUnitWidth, int minRange, int maxRange, float damage, Consumer<ArrayList<WeaponTemplate>> weaponGenerator, Consumer<TreeMap<UnitCharacteristic, UnitCharacteristicValue>> unitCharacteristicSetter, BiConsumer<HashMap<Action, Integer>, HashMap<Action, Integer>> actionCostSetter, AttributeData[] infoAttributes, Supplier<ObjPos[]> firingPositions, String description) {
        super(name, displayName, hitPoints, maxMovement, maxViewRange, actions, firingAnimFrames, firingAnimUnitWidth, minRange, maxRange, damage, weaponGenerator, unitCharacteristicSetter, actionCostSetter, infoAttributes, firingPositions, description);
    }

    @Override
    public void addModifiers(ArrayList<Modifier> list) {
        list.add(MovementModifier.NO_ASTEROID_FIELDS);
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
}
