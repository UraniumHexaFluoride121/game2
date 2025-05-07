package unit.type;

import foundation.MainPanel;
import foundation.NamedEnum;
import foundation.math.ObjPos;
import level.tile.TileType;
import render.Renderable;
import render.texture.*;
import unit.ShipClass;
import unit.UnitPose;
import unit.UnitTeam;
import unit.action.Action;
import unit.info.AttributeData;
import unit.info.UnitCharacteristic;
import unit.info.UnitCharacteristicValue;
import unit.weapon.WeaponTemplate;

import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static level.tile.Tile.*;
import static unit.type.CorvetteType.*;
import static unit.type.CruiserType.*;
import static unit.type.FighterType.*;

public abstract class UnitType implements NamedEnum {
    private final HashMap<UnitTeam, HashMap<UnitPose, Renderable>> tileRenderers = new HashMap<>();
    private final HashMap<UnitTeam, HashMap<UnitPose, BufferedImage>> images = new HashMap<>();

    public boolean canCapture = true;
    private final String name, displayName;
    public final String description;
    public final ShipClass shipClass;
    public final float hitPoints, maxMovement, maxViewRange;

    public final Function<TileType, Float> tileMovementCostFunction, tileViewRangeCostFunction;

    public final Action[] actions;
    public final int firingAnimFrames;
    public float shieldHP = 0, shieldRegen = 0, firingAnimShieldWidth = 0;
    public float repair = 0;
    public final float firingAnimUnitWidth;

    public final ArrayList<WeaponTemplate> weapons = new ArrayList<>();
    private final Consumer<ArrayList<WeaponTemplate>> weaponGenerator;

    public final TreeMap<UnitCharacteristic, UnitCharacteristicValue> unitCharacteristics = new TreeMap<>();
    public final AttributeData[] infoAttributes;

    public final Supplier<ObjPos[]> firingPositions;

    public final HashMap<UnitTeam, ImageSequence> firingSequenceLeft = new HashMap<>();
    public final HashMap<UnitTeam, ImageSequence> firingSequenceRight = new HashMap<>();
    private final HashMap<Action, Integer> actionCost = new HashMap<>(), perTurnActionCost = new HashMap<>();

    public ImageRenderer shieldRenderer = null;

    public static final UnitType[] ORDERED_UNIT_TYPES = new UnitType[]{
            FIGHTER, BOMBER, SCOUT, CORVETTE, DEFENDER, ARTILLERY, SUPPLY, CRUISER, MINER
    };

    UnitType(String name, String displayName, float hitPoints, float maxMovement, float maxViewRange, Function<TileType, Float> tileMovementCostFunction, Function<TileType, Float> tileViewRangeCostFunction, Action[] actions, int firingAnimFrames, float firingAnimUnitWidth, Consumer<ArrayList<WeaponTemplate>> weaponGenerator, Consumer<TreeMap<UnitCharacteristic, UnitCharacteristicValue>> unitCharacteristicSetter, BiConsumer<HashMap<Action, Integer>, HashMap<Action, Integer>> actionCostSetter, AttributeData[] infoAttributes, Supplier<ObjPos[]> firingPositions, String description) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.hitPoints = hitPoints;
        this.maxMovement = maxMovement;
        this.maxViewRange = maxViewRange;
        this.tileMovementCostFunction = tileMovementCostFunction;
        this.tileViewRangeCostFunction = tileViewRangeCostFunction;
        this.actions = actions;
        this.firingAnimFrames = firingAnimFrames;
        this.firingAnimUnitWidth = firingAnimUnitWidth;
        this.weaponGenerator = weaponGenerator;
        this.infoAttributes = infoAttributes;
        this.firingPositions = firingPositions;
        shipClass = getShipClass();
        actionCostSetter.accept(actionCost, perTurnActionCost);
        unitCharacteristicSetter.accept(unitCharacteristics);
    }

    private void init() {
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

    public UnitType addShield(float shieldHP, float shieldRegen, float firingAnimShieldWidth) {
        this.shieldHP = shieldHP;
        this.shieldRegen = shieldRegen;
        this.firingAnimShieldWidth = firingAnimShieldWidth;
        shieldRenderer = ImageRenderer.renderImage(new ResourceLocation("ships/" + name + "/shield.png"), true, true);
        return this;
    }

    public Renderable tileRenderer(UnitTeam team, UnitPose pose) {
        return tileRenderers.get(team).get(pose);
    }

    public BufferedImage getImage(UnitTeam team, UnitPose pose) {
        return images.get(team).get(pose);
    }

    public static UnitType getTypeByName(String name) {
        for (UnitType type : ORDERED_UNIT_TYPES) {
            if (type.name.equals(name))
                return type;
        }
        throw new RuntimeException("Unrecognised UnitType: " + name);
    }

    public static UnitType read(DataInputStream reader) throws IOException {
        return getTypeByName(reader.readUTF());
    }

    public void write(DataOutputStream writer) throws IOException {
        writer.writeUTF(name);
    }

    public static void initAll() {
        MainPanel.setLoadBarEnabled(true);
        for (int i = 0; i < ORDERED_UNIT_TYPES.length; i++) {
            UnitType type = ORDERED_UNIT_TYPES[i];
            type.init();
            type.weaponGenerator.accept(type.weapons);
            MainPanel.setLoadBarProgress((i + 1f) / ORDERED_UNIT_TYPES.length);
        }
        MainPanel.setLoadBarEnabled(false);
    }

    public Optional<Integer> getActionCost(Action action) {
        if (actionCost.containsKey(action))
            return Optional.of(actionCost.get(action));
        return Optional.empty();
    }

    public Optional<Integer> getPerTurnActionCost(Action action) {
        if (perTurnActionCost.containsKey(action))
            return Optional.of(perTurnActionCost.get(action));
        return Optional.empty();
    }

    public abstract float damageReduction(TileType type);
    protected abstract ShipClass getShipClass();

    public float getBobbingAmount() {
        return 1;
    }

    public float getBobbingRate() {
        return 1;
    }

    public abstract float movementCostMultiplier();
    public abstract float movementFixedCost();

    public UnitType noCapture() {
        canCapture = false;
        return this;
    }

    public UnitType setRepair(float repair) {
        this.repair = repair;
        return this;
    }

    public UnitType modify(Consumer<UnitType> action) {
        action.accept(this);
        return this;
    }

    public String getInternalName() {
        return name;
    }

    public boolean canPerformAction(Action a) {
        if (a == Action.CAPTURE && !canCapture)
            return false;
        for (Action action : actions) {
            if (a == action)
                return true;
        }
        return getActionCost(a).isPresent() || getPerTurnActionCost(a).isPresent();
    }

    @Override
    public String getName() {
        return displayName;
    }
}
