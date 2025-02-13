package unit;

import foundation.NamedEnum;
import foundation.math.ObjPos;
import level.tile.TileType;
import render.Renderable;
import render.texture.*;
import unit.action.Action;
import unit.info.AttributeData;
import unit.info.UnitCharacteristic;
import unit.info.UnitCharacteristicValue;
import unit.weapon.AttackData;
import unit.weapon.ProjectileType;
import unit.weapon.WeaponTemplate;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static level.tile.Tile.*;
import static unit.info.AttributeData.*;

public enum UnitType implements NamedEnum {
    FIGHTER("fighter", "Fighter", ShipClass.FIGHTER, 8, 7f, 3.5f, type -> switch (type) {
        case EMPTY -> 1f;
        case NEBULA -> 1.8f;
        case DENSE_NEBULA -> 2f;
        case ASTEROIDS -> 2.4f;
    }, type -> switch (type) {
        case EMPTY -> 1f;
        case NEBULA -> 1.7f;
        case DENSE_NEBULA -> 100f;
        case ASTEROIDS -> 1.5f;
    }, type -> switch (type) {
        case EMPTY -> 1f;
        case NEBULA -> 0.88f;
        case DENSE_NEBULA -> 0.82f;
        case ASTEROIDS -> 0.72f;
    }, new Action[]{
            Action.FIRE, Action.MOVE
    }, 1, 15, 1, 1, list -> {
        WeaponTemplate w = new WeaponTemplate(ProjectileType.FIGHTER_PLASMA);
        w.addData("fighter", new AttackData(3.8f));
        w.addData("bomber", new AttackData(3.2f));
        w.addData("corvette", new AttackData(1.8f));
        list.add(w);
    }, map -> {
        map.put(UnitCharacteristic.DEFENCE, UnitCharacteristicValue.NONE_LOW);
        map.put(UnitCharacteristic.SPEED, UnitCharacteristicValue.HIGH_MAX);
        map.put(UnitCharacteristic.FIREPOWER, UnitCharacteristicValue.LOW);
        map.put(UnitCharacteristic.VIEW_RANGE, UnitCharacteristicValue.GOOD);
        map.put(UnitCharacteristic.FIRING_RANGE, UnitCharacteristicValue.LOW);
    }, new AttributeData[]{
            HIGH_MOVEMENT_SPEED, QUICK_ASTEROID_FIELD, ANTI_FIGHTER, ANTI_SHIELD,
            CARRIER_LOADING,
            INEFFECTIVE_AGAINST_LARGE
    }, FiringRenderer.THREE_UNITS),
    BOMBER("bomber", "Bomber", ShipClass.FIGHTER, 7, 6f, 3.5f, type -> switch (type) {
        case EMPTY -> 1f;
        case NEBULA -> 1.9f;
        case DENSE_NEBULA -> 2.1f;
        case ASTEROIDS -> 2.5f;
    }, type -> switch (type) {
        case EMPTY -> 1f;
        case NEBULA -> 1.7f;
        case DENSE_NEBULA -> 100f;
        case ASTEROIDS -> 1.5f;
    }, type -> switch (type) {
        case EMPTY -> 1f;
        case NEBULA -> 0.88f;
        case DENSE_NEBULA -> 0.82f;
        case ASTEROIDS -> 0.72f;
    }, new Action[]{
            Action.FIRE, Action.MOVE
    }, 3, 15, 1, 1, list -> {
        WeaponTemplate w1 = new WeaponTemplate(ProjectileType.BOMBER_MISSILE).consumeAmmo(1).runAnim();
        w1.addData("fighter", new AttackData(0.8f));
        w1.addData("bomber", new AttackData(0.7f));
        w1.addData("corvette", new AttackData(2.4f));
        list.add(w1);
        WeaponTemplate w2 = new WeaponTemplate(ProjectileType.BOMBER_PLASMA);
        w2.addData("fighter", new AttackData(3.2f));
        w2.addData("bomber", new AttackData(2.6f));
        w2.addData("corvette", new AttackData(1.6f));
        list.add(w2);
    }, map -> {
        map.put(UnitCharacteristic.DEFENCE, UnitCharacteristicValue.LOW);
        map.put(UnitCharacteristic.SPEED, UnitCharacteristicValue.HIGH);
        map.put(UnitCharacteristic.FIREPOWER, UnitCharacteristicValue.GOOD);
        map.put(UnitCharacteristic.VIEW_RANGE, UnitCharacteristicValue.MODERATE);
        map.put(UnitCharacteristic.FIRING_RANGE, UnitCharacteristicValue.LOW);
    }, new AttributeData[]{
            HIGH_MOVEMENT_SPEED, QUICK_ASTEROID_FIELD, ANTI_CAPITAL_SHIP_MISSILES,
            CARRIER_LOADING,
            MAIN_GUN_LIMITED_AMMO
    }, FiringRenderer.THREE_UNITS),
    CORVETTE("corvette", "Corvette", ShipClass.CORVETTE, 10, 5f, 3.5f, type -> switch (type) {
        case EMPTY -> 1f;
        case NEBULA -> 1.7f;
        case DENSE_NEBULA -> 1.9f;
        case ASTEROIDS -> 5f;
    }, type -> switch (type) {
        case EMPTY -> 1f;
        case NEBULA -> 1.7f;
        case DENSE_NEBULA -> 100f;
        case ASTEROIDS -> 1.5f;
    }, type -> switch (type) {
        case EMPTY -> 1f;
        case NEBULA -> 0.88f;
        case DENSE_NEBULA -> 0.82f;
        case ASTEROIDS -> 0.76f;
    }, new Action[]{
            Action.FIRE, Action.MOVE
    }, 1, 17, 0.75f, 0.8f, list -> {
        WeaponTemplate w = new WeaponTemplate(ProjectileType.CORVETTE_CANNON);
        w.addData("fighter", new AttackData(1.6f));
        w.addData("bomber", new AttackData(1.4f));
        w.addData("corvette", new AttackData(5.2f));
        list.add(w);
    }, map -> {
        map.put(UnitCharacteristic.DEFENCE, UnitCharacteristicValue.MODERATE);
        map.put(UnitCharacteristic.SPEED, UnitCharacteristicValue.GOOD);
        map.put(UnitCharacteristic.FIREPOWER, UnitCharacteristicValue.MODERATE);
        map.put(UnitCharacteristic.VIEW_RANGE, UnitCharacteristicValue.MODERATE);
        map.put(UnitCharacteristic.FIRING_RANGE, UnitCharacteristicValue.LOW);
    }, new AttributeData[]{
            ANTI_CORVETTE, BALANCED,
            SLOW_ASTEROID_FIELD, CARRIER_LOADING,
            INEFFECTIVE_AGAINST_FIGHTER, INEFFECTIVE_AGAINST_SHIELDS
    }, FiringRenderer.THREE_UNITS);

    static {
        generateWeapons();
    }

    private final HashMap<UnitTeam, HashMap<UnitPose, Renderable>> tileRenderers = new HashMap<>();
    private final HashMap<UnitTeam, HashMap<UnitPose, BufferedImage>> images = new HashMap<>();

    private final String name, displayName;
    public final ShipClass shipClass;
    public final float hitPoints, maxMovement, maxViewRange;

    public final Function<TileType, Float> tileMovementCostFunction, tileViewRangeCostFunction;
    public final Function<TileType, Float> damageReduction;

    public final Action[] actions;
    public final int firingAnimFrames;
    public final float firingAnimUnitWidth, bobbingRate, bobbingAmount;

    public final ArrayList<WeaponTemplate> weapons = new ArrayList<>();
    private final Consumer<ArrayList<WeaponTemplate>> weaponGenerator;

    public final HashMap<UnitCharacteristic, UnitCharacteristicValue> unitCharacteristics = new HashMap<>();
    private final Consumer<HashMap<UnitCharacteristic, UnitCharacteristicValue>> unitCharacteristicSetter;
    public final AttributeData[] infoAttributes;

    public final Supplier<ObjPos[]> firingPositions;

    public final HashMap<UnitTeam, ImageSequence> firingSequenceLeft = new HashMap<>();
    public final HashMap<UnitTeam, ImageSequence> firingSequenceRight = new HashMap<>();

    public static final UnitType[] ORDERED_UNIT_TYPES = new UnitType[]{
            FIGHTER, BOMBER, CORVETTE
    };

    UnitType(String name, String displayName, ShipClass shipClass, float hitPoints, float maxMovement, float maxViewRange, Function<TileType, Float> tileMovementCostFunction, Function<TileType, Float> tileViewRangeCostFunction, Function<TileType, Float> damageReduction, Action[] actions, int firingAnimFrames, float firingAnimUnitWidth, float bobbingRate, float bobbingAmount, Consumer<ArrayList<WeaponTemplate>> weaponGenerator, Consumer<HashMap<UnitCharacteristic, UnitCharacteristicValue>> unitCharacteristicSetter, AttributeData[] infoAttributes, Supplier<ObjPos[]> firingPositions) {
        this.name = name;
        this.displayName = displayName;
        this.shipClass = shipClass;
        this.hitPoints = hitPoints;
        this.maxMovement = maxMovement;
        this.maxViewRange = maxViewRange;
        this.tileMovementCostFunction = tileMovementCostFunction;
        this.tileViewRangeCostFunction = tileViewRangeCostFunction;
        this.damageReduction = damageReduction;
        this.actions = actions;
        this.firingAnimFrames = firingAnimFrames;
        this.firingAnimUnitWidth = firingAnimUnitWidth;
        this.bobbingRate = bobbingRate;
        this.bobbingAmount = bobbingAmount;
        this.weaponGenerator = weaponGenerator;
        this.unitCharacteristicSetter = unitCharacteristicSetter;
        this.infoAttributes = infoAttributes;
        this.firingPositions = firingPositions;
        unitCharacteristicSetter.accept(unitCharacteristics);
        for (UnitTeam team : UnitTeam.values()) {
            tileRenderers.put(team, new HashMap<>());
            images.put(team, new HashMap<>());
            for (UnitPose pose : UnitPose.values()) {
                if (!pose.load)
                    continue;
                ResourceLocation resource = new ResourceLocation("ships/" + name + "/" + team.s + "/" + pose.s + ".png");
                BufferedImage image = AssetManager.getImage(resource, true);
                tileRenderers.get(team).put(pose, Renderable.renderImage(image, false, true, TILE_SIZE));
                images.get(team).put(pose, image);
            }
            firingSequenceLeft.put(team, new AsyncImageSequence("ships/" + name + "/" + team.s + "/firing_left", firingAnimFrames, true));
            firingSequenceRight.put(team, new AsyncImageSequence("ships/" + name + "/" + team.s + "/firing_right", firingAnimFrames, true));
        }
    }

    public Renderable tileRenderer(UnitTeam team, UnitPose pose) {
        return tileRenderers.get(team).get(pose);
    }

    public BufferedImage getImage(UnitTeam team, UnitPose pose) {
        return images.get(team).get(pose);
    }

    public static UnitType getTypeByName(String name) {
        for (UnitType type : values()) {
            if (type.name.equals(name))
                return type;
        }
        throw new RuntimeException("Unrecognised UnitType: " + name);
    }

    public static void generateWeapons() {
        for (UnitType type : values()) {
            type.weaponGenerator.accept(type.weapons);
        }
    }

    @Override
    public String getName() {
        return displayName;
    }
}
