package unit.action;

import foundation.input.ButtonState;
import foundation.math.HitBox;
import level.energy.EnergyManager;
import render.GameRenderer;
import render.Renderable;
import render.UIColourTheme;
import render.save.SerializationProxy;
import render.save.SerializedByProxy;
import render.types.text.StyleElement;
import render.types.text.TextRenderable;
import unit.stats.ColouredIconName;

import java.awt.*;
import java.io.ObjectStreamException;
import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static render.Renderable.*;
import static unit.action.ActionColour.*;

public class Action implements ColouredIconName, SerializedByProxy, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private static final HashMap<String, Action> names = new HashMap<>();

    public static final float ACTION_BUTTON_SIZE = 2f;
    public static final float ROUNDING = 1, BORDER = 0.15f;

    public static final BasicStroke ICON_STROKE_NON_SCALED = Renderable.roundedStroke(0.2f / ACTION_BUTTON_SIZE);
    public static final BasicStroke ICON_STROKE_NARROW_1 = Renderable.roundedStroke(0.15f * SCALING);
    public static final Color ICON_COLOUR = new Color(214, 214, 214);
    public static final Color ICON_COLOUR_UNUSABLE = new Color(195, 195, 195);

    public static final Color
            MOVE_ACTION_HIGHLIGHT = new Color(122, 210, 248, 26),
            FIRE_ACTION_HIGHLIGHT = new Color(248, 122, 122, 26),
            REPAIR_ACTION_HIGHLIGHT = new Color(135, 246, 105, 33),
            RESUPPLY_ACTION_HIGHLIGHT = new Color(246, 190, 105, 33);

    public static final Object MOVE_FROM = new Object();
    public static final Action
            MOVE = new Action("MOVE", "Move", " " + TextRenderable.MOVE_ICON.display, StyleElement.MODIFIER_MOVEMENT_SPEED, BLUE, BLUE, MOVE_ACTION_HIGHLIGHT, false, g -> {
        g.setStroke(ICON_STROKE_NON_SCALED);
        GameRenderer.renderScaled(ACTION_BUTTON_SIZE, g, () -> {
            g.draw(ActionShapes.MOVE);
        });
    }, "Allows you to move this unit to nearby tiles. The distance a unit can move each turn " +
            "depends primarily on the speed of the unit, but, hard to navigate terrain, such as nebulae or " +
            "asteroid fields, can limit movement. The " + EnergyManager.displayName + " cost is per-tile, with " +
            "larger ship classes generally costing more to move. By default, the lowest cost path is used when moving a unit, " +
            "but you can hold CTRL when dragging the mouse to the destination tile to trace out a path of your choice.", 2),
            FIRE = new Action("FIRE", "Fire", TextRenderable.DAMAGE_ICON.display, StyleElement.MODIFIER_DAMAGE, RED, RED_UNUSABLE, FIRE_ACTION_HIGHLIGHT, true, g -> {
                g.setStroke(ICON_STROKE_NARROW_1);
                g.drawOval(
                        scale(0.25f), scale(0.25f),
                        scale(0.5f), scale(0.5f)
                );
                g.drawLine(
                        scale(0.2f), scale(0.5f),
                        scale(0.38f), scale(0.5f)
                );
                g.drawLine(
                        scale(0.8f), scale(0.5f),
                        scale(0.62f), scale(0.5f)
                );
                g.drawLine(
                        scale(0.5f), scale(0.2f),
                        scale(0.5f), scale(0.38f)
                );
                g.drawLine(
                        scale(0.5f), scale(0.8f),
                        scale(0.5f), scale(0.62f)
                );
            }, "Fire at enemy units to deal damage. Damage dealt is proportional to the unit's remaining HP. " +
                    "With the exception of ranged units, firing at an enemy will lead to being counterattacked, but, " +
                    "attacking first gives you the upper hand as you'll weaken the enemy before the counterattack. " +
                    "Being on terrain with high defence reduces all incoming damage.", 1),
            CAPTURE = new Action("CAPTURE", "Capture", " " + TextRenderable.CAPTURE_ICON.display, StyleElement.DARK_GREEN, DARK_GREEN, DARK_GREEN, FIRE_ACTION_HIGHLIGHT, false, g -> {
                GameRenderer.renderScaled(ACTION_BUTTON_SIZE, g, () -> {
                    g.fill(ActionShapes.FLAG);
                });
            }, "This action only appears when over the top of an enemy structure that can be captured. Capturing " +
                    "takes several turns, and each time you're attacked during a capture (not including counterattacks), " +
                    "your capture progress gets reduced. Capturing an enemy base leads to that player being eliminated.", -21),
            SHIELD_REGEN = new Action("SHIELD_REGEN", "Regenerate Shield", " " + TextRenderable.SHIELD_REGEN_ICON.display, StyleElement.MODIFIER_SHIELD_HP, LIGHT_BLUE, LIGHT_BLUE_UNUSABLE, FIRE_ACTION_HIGHLIGHT, false, g -> {
                GameRenderer.renderScaled(ACTION_BUTTON_SIZE, g, () -> {
                    g.fill(ActionShapes.SHIELD_REGEN);
                });
            }, "Regenerate a portion of this unit's shield. While expensive in terms of " + EnergyManager.displayName + ", " +
                    "having shield HP provides several advantages over regular HP. Most notably, it allows the unit to " +
                    "take damage without suffering a loss in firepower, as damage is based only on regular HP, and remains " +
                    "unaffected when losing shield HP.", 0),
            STEALTH = new Action("STEALTH", "Stealth", " " + TextRenderable.STEALTH_ICON.display, StyleElement.YELLOW, YELLOW, YELLOW_UNUSABLE, FIRE_ACTION_HIGHLIGHT, false, g -> {
                GameRenderer.renderScaled(ACTION_BUTTON_SIZE, g, () -> {
                    g.fill(ActionShapes.STEALTH);
                });
            }, "Toggle stealth mode. While in stealth mode, the ship will be hidden from " +
                    "enemies, unless directly adjacent to an enemy unit. The ship also loses the " +
                    "ability to fire, and each ship kept in stealth mode costs a small amount of " +
                    EnergyManager.displayName + " at the end of each turn, on top of the fixed cost " +
                    "to enter stealth mode in the first place. This is visible in the form of a reduction in " +
                    EnergyManager.displayName + " income. Not only that, ships that have this ability " +
                    "are also unable to capture structures, regardless of whether or not they're in stealth mode.", -20),
            MINE = new Action("MINE", "Mine", TextRenderable.ENERGY_ICON.display, StyleElement.MODIFIER_MINING, PURPLE, PURPLE_UNUSABLE, REPAIR_ACTION_HIGHLIGHT, false, g -> {
                GameRenderer.renderScaled(ACTION_BUTTON_SIZE, g, () -> {
                    g.fill(ActionShapes.ENERGY);
                });
            }, "Mine asteroid fields for " + EnergyManager.displayName + ". Mining increases income, meaning that " +
                    "the additional " + EnergyManager.displayName + " is credited at the start of each turn as long as this unit is mining. " +
                    "Each turn that this unit is mining for, the asteroid field has its " + EnergyManager.displayName + " depleted, until the asteroid " +
                    "field is gone. The number of turns remaining until an asteroid field is depleted can be seen in the tile info " +
                    "in the bottom right when the tile is selected, unless the tile is outside of view range. Moving the unit interrupts mining.", -30),
            REPAIR = new Action("REPAIR", "Repair", " " + TextRenderable.REPAIR_ICON.display, StyleElement.MODIFIER_REPAIR, GREEN, GREEN_UNUSABLE, REPAIR_ACTION_HIGHLIGHT, false, g -> {
                GameRenderer.renderScaled(ACTION_BUTTON_SIZE, g, () -> {
                    g.fill(ActionShapes.REPAIR);
                });
            }, "Repair some of the HP of an allied unit. You must be adjacent to the unit that needs repairs, " +
                    "and HP cannot go above the max HP for the repaired unit.", -10),
            RESUPPLY = new Action("RESUPPLY", "Resupply", " " + TextRenderable.RESUPPLY_ICON.display, StyleElement.RESUPPLY, BROWN, BROWN_UNUSABLE, RESUPPLY_ACTION_HIGHLIGHT, false, g -> {
                GameRenderer.renderScaled(ACTION_BUTTON_SIZE, g, () -> {
                    g.fill(ActionShapes.SUPPLY);
                });
            }, "Resupply the ammunition of an allied unit. You must be adjacent to the unit that needs resupplying.", -5);

    private final String name, displayName, textIcon;
    private transient final StyleElement textColour;
    private transient final ActionColour colour, unusableColour;
    public transient final Color tileColour;
    public transient final UIColourTheme colourTheme;
    private transient final Renderable iconImageRenderer;
    private transient final boolean scaled;
    public transient final String infoText;
    private transient final int order;

    public static final HitBox buttonBox = new HitBox(ACTION_BUTTON_SIZE / 2, -ACTION_BUTTON_SIZE / 2, -ACTION_BUTTON_SIZE / 2, ACTION_BUTTON_SIZE / 2);

    public Action(String name, String displayName, String textIcon, StyleElement textColour, ActionColour colour, ActionColour unusableColour, Color tileColour, boolean scaled, Renderable iconImageRenderer, String infoText, int order) {
        this.displayName = displayName;
        this.textIcon = textIcon;
        this.textColour = textColour;
        this.unusableColour = unusableColour;
        this.infoText = infoText;
        this.order = order;
        names.put(name, this);
        this.name = name;
        this.colour = colour;
        this.tileColour = tileColour;
        this.scaled = scaled;
        this.iconImageRenderer = iconImageRenderer;
        colourTheme = UIColourTheme.createBoxTheme(textColour);
    }

    public void render(Graphics2D g, ActionData data) {
        float offset = (ACTION_BUTTON_SIZE) / 2;
        g.translate(-offset, -offset);
        renderIcon(g, data.type,
                switch (data.type) {
                    case ENABLED -> data.clickHandler.state;
                    case UNUSABLE, DISABLED -> ButtonState.DEFAULT;
                });
        g.translate(offset, offset);
    }

    public void renderIcon(Graphics2D g, ActionIconType type, ButtonState state) {
        g.scale(1d / SCALING, 1d / SCALING);
        g.setColor(switch (type) {
            case ENABLED -> colour.border;
            case UNUSABLE -> unusableColour.border;
            case DISABLED -> ActionColour.DISABLED.border;
        });
        g.fillRoundRect(
                0,
                0,
                (int) (ACTION_BUTTON_SIZE * SCALING),
                (int) (ACTION_BUTTON_SIZE * SCALING),
                (int) (SCALING * ROUNDING),
                (int) (SCALING * ROUNDING)
        );
        g.setColor(switch (type) {
            case ENABLED -> colour.background;
            case UNUSABLE -> unusableColour.background;
            case DISABLED -> ActionColour.DISABLED.background;
        });
        float border = BORDER * stateBorderScale(state);
        g.fillRoundRect(
                (int) (border * SCALING),
                (int) (border * SCALING),
                (int) ((ACTION_BUTTON_SIZE - border * 2) * SCALING),
                (int) ((ACTION_BUTTON_SIZE - border * 2) * SCALING),
                (int) (SCALING * (ROUNDING - border * 2)),
                (int) (SCALING * (ROUNDING - border * 2))
        );
        g.setColor(type == ActionIconType.UNUSABLE ? ICON_COLOUR_UNUSABLE : ICON_COLOUR);
        if (scaled)
            iconImageRenderer.render(g);
        g.scale(SCALING, SCALING);
        if (!scaled)
            iconImageRenderer.render(g);
    }

    private static float stateBorderScale(ButtonState state) {
        return switch (state) {
            case DEFAULT -> 1f;
            case HOVER -> 1.5f;
            case PRESSED -> 1.25f;
            case SELECTED -> 1;
        };
    }

    public static Action valueOf(String name) {
        for (Map.Entry<String, Action> entry : names.entrySet()) {
            if (entry.getKey().equals(name))
                return entry.getValue();
        }
        return null;
    }

    private static int scale(float v) {
        return (int) (v * ACTION_BUTTON_SIZE * SCALING);
    }

    public int getOrder() {
        return order;
    }

    public static void forEach(Consumer<Action> action) {
        for (Action a : names.values()) {
            action.accept(a);
        }
    }

    public static void forEachTutorialTileGroup(Consumer<Object> action) {
        for (Action a : names.values()) {
            action.accept(a);
        }
        action.accept(MOVE_FROM);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public String getName() {
        return displayName;
    }

    @Override
    public String getInternalName() {
        return name;
    }

    @Override
    public String colour() {
        return textColour.display;
    }

    @Serial
    private Object writeReplace() throws ObjectStreamException {
        return new SerializationProxy<>(Action.class, this);
    }

    @Override
    public String getIcon() {
        return textIcon;
    }
}
