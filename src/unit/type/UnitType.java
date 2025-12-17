package unit.type;

import foundation.MainPanel;
import foundation.math.ObjPos;
import render.Renderable;
import render.save.SerializedByProxy;
import render.texture.*;
import unit.ShipClass;
import unit.UnitPose;
import unit.UnitTeam;
import unit.action.Action;
import unit.info.UnitCharacteristic;
import unit.info.UnitCharacteristicValue;
import unit.stats.Article;
import unit.stats.attribute.UnitAttribute;
import unit.stats.modifiers.types.Modifier;
import unit.stats.NameArticle;
import unit.weapon.WeaponTemplate;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static level.tile.Tile.*;
import static unit.type.CorvetteType.*;
import static unit.type.CruiserType.*;
import static unit.type.FighterType.*;

public abstract class UnitType implements NameArticle, SerializedByProxy, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private transient final HashMap<UnitTeam, HashMap<UnitPose, Renderable>> tileRenderers = new HashMap<>();
    private transient final HashMap<UnitTeam, HashMap<UnitPose, Renderable>> stealthTileRenderers = new HashMap<>();
    private transient final HashMap<UnitTeam, HashMap<UnitPose, BufferedImage>> images = new HashMap<>();
    private transient final HashMap<UnitTeam, HashMap<UnitPose, BufferedImage>> stealthImages = new HashMap<>();

    public transient boolean canCapture = true;
    private final String name, displayName;
    private transient Article article = Article.A;
    public transient final String description;
    public transient final ShipClass shipClass;
    public transient final int value;
    public transient final float hitPoints, maxMovement, maxViewRange;

    public transient UnitAttribute[] attributes = new UnitAttribute[0];
    public transient final Action[] actions;
    public transient final int firingAnimFrames;
    public transient float shieldHP = 0, shieldRegen = 0, firingAnimShieldWidth = 0;
    public transient float repair = 0;
    public transient final float firingAnimUnitWidth;

    public transient int ammoCapacity = 0;
    public transient final int minRange, maxRange;
    public transient final float damage;
    public transient final ArrayList<WeaponTemplate> weapons = new ArrayList<>();
    public transient final ArrayList<Modifier> modifiers = new ArrayList<>();
    private transient final Consumer<ArrayList<WeaponTemplate>> weaponGenerator;

    public transient final TreeMap<UnitCharacteristic, UnitCharacteristicValue> unitCharacteristics = new TreeMap<>();

    public transient final Supplier<ObjPos[]> firingPositions;

    public transient final HashMap<UnitTeam, ImageSequence> firingSequenceLeft = new HashMap<>();
    public transient final HashMap<UnitTeam, ImageSequence> firingSequenceRight = new HashMap<>();
    public transient final HashMap<UnitTeam, ImageSequence> firingSequenceLeftStealth = new HashMap<>();
    public transient final HashMap<UnitTeam, ImageSequence> firingSequenceRightStealth = new HashMap<>();
    private transient final HashMap<Action, Integer> actionCost = new HashMap<>(), perTurnActionCost = new HashMap<>();

    public transient ImageRenderer shieldRenderer = null;

    public static UnitType[] ORDERED_UNIT_TYPES;

    UnitType(String name, String displayName, int value, float hitPoints, float maxMovement, float maxViewRange, Action[] actions, int firingAnimFrames, float firingAnimUnitWidth, int minRange, int maxRange, float damage, Consumer<ArrayList<WeaponTemplate>> weaponGenerator, Consumer<TreeMap<UnitCharacteristic, UnitCharacteristicValue>> unitCharacteristicSetter, BiConsumer<HashMap<Action, Integer>, HashMap<Action, Integer>> actionCostSetter, Supplier<ObjPos[]> firingPositions, String description) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.value = value;
        this.hitPoints = hitPoints;
        this.maxMovement = maxMovement;
        this.maxViewRange = maxViewRange;
        this.actions = actions;
        this.firingAnimFrames = firingAnimFrames;
        this.firingAnimUnitWidth = firingAnimUnitWidth;
        this.minRange = minRange;
        this.maxRange = maxRange;
        this.damage = damage;
        this.weaponGenerator = weaponGenerator;
        this.firingPositions = firingPositions;
        shipClass = getShipClass();
        actionCostSetter.accept(actionCost, perTurnActionCost);
        unitCharacteristicSetter.accept(unitCharacteristics);
        addModifiers(modifiers);
    }

    public static void orderTypes() {
        ORDERED_UNIT_TYPES = new UnitType[]{
                INTERCEPTOR, BOMBER, SCOUT, FRIGATE, DEFENDER, ARTILLERY, SUPPLY, BATTLECRUISER, LIGHT_CRUISER, MINER
        };
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
        if (canPerformAction(Action.STEALTH)) {
            for (UnitTeam team : UnitTeam.values()) {
                stealthTileRenderers.put(team, new HashMap<>());
                stealthImages.put(team, new HashMap<>());
                for (UnitPose pose : UnitPose.values()) {
                    if (!pose.load)
                        continue;
                    ResourceLocation resource = new ResourceLocation("ships/" + name + "/" + team.s + "/" + pose.s + "_stealth.png");
                    BufferedImage image = AssetManager.getImage(resource, true);
                    stealthTileRenderers.get(team).put(pose, Renderable.renderImage(image, false, true, TILE_SIZE));
                    stealthImages.get(team).put(pose, image);
                }
                firingSequenceLeftStealth.put(team, new AsyncImageSequence("ships/" + name + "/" + team.s + "/firing_left_stealth", firingAnimFrames, true));
                firingSequenceRightStealth.put(team, new AsyncImageSequence("ships/" + name + "/" + team.s + "/firing_right_stealth", firingAnimFrames, true));
            }
        }
    }

    public UnitType addShield(float shieldHP, float shieldRegen, float firingAnimShieldWidth) {
        this.shieldHP = shieldHP;
        this.shieldRegen = shieldRegen;
        this.firingAnimShieldWidth = firingAnimShieldWidth;
        shieldRenderer = ImageRenderer.renderImage(new ResourceLocation("ships/" + name + "/shield.png"), true, true);
        return this;
    }

    public Renderable tileRenderer(UnitTeam team, UnitPose pose, boolean stealth) {
        return (stealth ? stealthTileRenderers : tileRenderers).get(team).get(pose);
    }

    public BufferedImage getImage(UnitTeam team, UnitPose pose, boolean stealth) {
        return (stealth ? stealthImages : images).get(team).get(pose);
    }

    public ImageSequence getFiringImage(UnitTeam team, boolean left, boolean stealth) {
        return (left ?
                (stealth ? firingSequenceLeftStealth : firingSequenceLeft) :
                (stealth ? firingSequenceRightStealth : firingSequenceRight)
        ).get(team);
    }

    public static UnitType valueOf(String name) {
        for (UnitType type : ORDERED_UNIT_TYPES) {
            if (type.name.equals(name))
                return type;
        }
        throw new RuntimeException("Unrecognised UnitType: " + name);
    }

    public static UnitType read(DataInputStream reader) throws IOException {
        return valueOf(reader.readUTF());
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

    public abstract void addModifiers(ArrayList<Modifier> list);

    protected abstract ShipClass getShipClass();

    public float getBobbingAmount() {
        return 1;
    }

    public float getBobbingRate() {
        return 1;
    }

    public abstract float movementCostMultiplier();

    protected UnitType noCapture() {
        canCapture = false;
        return this;
    }

    protected UnitType useArticleAn() {
        article = Article.AN;
        return this;
    }

    protected UnitType setRepair(float repair) {
        this.repair = repair;
        return this;
    }

    protected UnitType setAttributes(UnitAttribute... attributes) {
        this.attributes = attributes;
        return this;
    }

    protected UnitType modify(Consumer<UnitType> action) {
        action.accept(this);
        return this;
    }

    protected UnitType setAmmoCapacity(int ammoCapacity) {
        this.ammoCapacity = ammoCapacity;
        return this;
    }

    @Override
    public String getInternalName() {
        return name;
    }

    public boolean canPerformAction(Action a) {
        if (a == Action.CAPTURE)
            return canCapture;
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

    public String getPluralName() {
        return getName().replace(" Unit", "") + " Units";
    }

    @Override
    public Article getArticleEnum() {
        return article;
    }
}
