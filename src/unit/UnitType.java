package unit;

import foundation.math.ObjPos;
import level.tile.TileType;
import render.Renderable;
import render.texture.*;
import unit.action.Action;
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

public enum UnitType {
    FIGHTER("fighter", "Fighter", 8, 7f, 3.5f, type -> switch (type) {
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
    }, 1, list -> {
        WeaponTemplate w = new WeaponTemplate(ProjectileType.FIGHTER_PLASMA);
        w.addData("fighter", new AttackData(3.8f));
        w.addData("bomber", new AttackData(3.2f));
        list.add(w);
    }, FiringRenderer.THREE_UNITS),
    BOMBER("bomber", "Bomber", 7, 5.5f, 3.5f, type -> switch (type) {
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
    }, 3, list -> {
        WeaponTemplate w1 = new WeaponTemplate(ProjectileType.BOMBER_MISSILE).consumeAmmo(1).runAnim();
        w1.addData("fighter", new AttackData(0.8f));
        w1.addData("bomber", new AttackData(0.7f));
        list.add(w1);
        WeaponTemplate w2 = new WeaponTemplate(ProjectileType.BOMBER_PLASMA);
        w2.addData("fighter", new AttackData(3.2f));
        w2.addData("bomber", new AttackData(2.6f));
        list.add(w2);
    }, FiringRenderer.THREE_UNITS);

    static {
        generateWeapons();
    }

    private final HashMap<UnitTeam, HashMap<UnitPose, Renderable>> tileRenderers = new HashMap<>();
    private final HashMap<UnitTeam, HashMap<UnitPose, BufferedImage>> images = new HashMap<>();
    public final String name, displayName;
    public final float hitPoints, maxMovement, maxViewRange;
    public final Function<TileType, Float> tileMovementCostFunction, tileViewRangeCostFunction;
    public final Function<TileType, Float> damageReduction;
    public final Action[] actions;
    public final int firingAnimFrames;
    public final ArrayList<WeaponTemplate> weapons = new ArrayList<>();
    private final Consumer<ArrayList<WeaponTemplate>> weaponGenerator;
    public final Supplier<ObjPos[]> firingPositions;
    public final HashMap<UnitTeam, ImageSequence> firingSequenceLeft = new HashMap<>();
    public final HashMap<UnitTeam, ImageSequence> firingSequenceRight = new HashMap<>();

    public static final UnitType[] ORDERED_UNIT_TYPES = new UnitType[]{
            FIGHTER, BOMBER
    };

    UnitType(String name, String displayName, float hitPoints, float maxMovement, float maxViewRange, Function<TileType, Float> tileMovementCostFunction, Function<TileType, Float> tileViewRangeCostFunction, Function<TileType, Float> damageReduction, Action[] actions, int firingAnimFrames, Consumer<ArrayList<WeaponTemplate>> weaponGenerator, Supplier<ObjPos[]> firingPositions) {
        this.name = name;
        this.displayName = displayName;
        this.hitPoints = hitPoints;
        this.maxMovement = maxMovement;
        this.maxViewRange = maxViewRange;
        this.tileMovementCostFunction = tileMovementCostFunction;
        this.tileViewRangeCostFunction = tileViewRangeCostFunction;
        this.damageReduction = damageReduction;
        this.actions = actions;
        this.firingAnimFrames = firingAnimFrames;
        this.weaponGenerator = weaponGenerator;
        this.firingPositions = firingPositions;
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
}
