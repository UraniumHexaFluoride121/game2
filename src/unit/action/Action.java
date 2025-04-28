package unit.action;

import foundation.NamedEnum;
import foundation.input.ButtonState;
import foundation.math.StaticHitBox;
import level.energy.EnergyManager;
import render.GameRenderer;
import render.Renderable;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static render.Renderable.*;
import static unit.action.ActionColour.*;

public class Action implements NamedEnum, Serializable {
    private static final HashMap<String, Action> names = new HashMap<>();

    public static final float ACTION_BUTTON_SIZE = 2f;
    public static final float ROUNDING = 1, BORDER = 0.15f;

    public static final BasicStroke ICON_STROKE = new BasicStroke(0.2f * SCALING, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 500);
    public static final BasicStroke ICON_STROKE_NARROW_1 = new BasicStroke(0.15f * SCALING, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 500);
    public static final BasicStroke ICON_STROKE_NARROW_1_NON_SCALED = new BasicStroke(0.15f / ACTION_BUTTON_SIZE, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 500);
    public static final BasicStroke ICON_STROKE_NARROW_2_NON_SCALED = new BasicStroke(0.08f / ACTION_BUTTON_SIZE, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 500);
    public static final BasicStroke ICON_STROKE_NARROW_3_NON_SCALED = new BasicStroke(0.04f / ACTION_BUTTON_SIZE, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 500);
    public static final Color ICON_COLOUR = new Color(214, 214, 214);
    public static final Color ICON_COLOUR_UNUSABLE = new Color(195, 195, 195);

    public static final Color
            MOVE_ACTION_HIGHLIGHT = new Color(122, 210, 248, 26),
            FIRE_ACTION_HIGHLIGHT = new Color(248, 122, 122, 26),
            REPAIR_ACTION_HIGHLIGHT = new Color(135, 246, 105, 33),
            RESUPPLY_ACTION_HIGHLIGHT = new Color(246, 190, 105, 33);

    public static final Action
            MOVE = new Action("MOVE", "Move", BLUE, BLUE, MOVE_ACTION_HIGHLIGHT, true, g -> {
        g.drawLine(
                scale(0.37f), scale(0.37f),
                scale(0.75f), scale(0.75f)
        );
        g.drawLine(
                scale(0.25f), scale(0.25f),
                scale(0.25f), scale(0.25f)
        );
        g.drawLine(
                scale(0.45f), scale(0.75f),
                scale(0.75f), scale(0.75f)
        );
        g.drawLine(
                scale(0.75f), scale(0.45f),
                scale(0.75f), scale(0.75f)
        );
    }, "Allows you to move this unit to nearby tiles. The distance a unit can move each turn " +
            "depends primarily on the speed of the unit, but, hard to navigate terrain, such as nebulae or " +
            "asteroid fields, can limit movement. The " + EnergyManager.displayName + " cost is per-tile, with " +
            "larger ship classes generally costing more to move. By default, the lowest cost path is used when moving a unit, " +
            "but you can hold CTRL when dragging the mouse to the destination tile to trace out a path of your choice.", 2),
            FIRE = new Action("FIRE", "Fire", RED, RED_UNUSABLE, FIRE_ACTION_HIGHLIGHT, true, g -> {
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
            CAPTURE = new Action("CAPTURE", "Capture", DARK_GREEN, DARK_GREEN, FIRE_ACTION_HIGHLIGHT, false, g -> {
                g.setStroke(ICON_STROKE_NARROW_1_NON_SCALED);
                GameRenderer.renderScaled(ACTION_BUTTON_SIZE, g, () -> {
                    g.draw(ActionShapes.FLAG);
                    g.fill(ActionShapes.FLAG);
                });
            }, "This action only appears when over the top of an enemy structure that can be captured. Capturing " +
                    "takes several turns, and each time you're attacked during a capture (not including counterattacks), " +
                    "your capture progress gets reduced. Capturing an enemy base leads to that player being eliminated.", -21),
            SHIELD_REGEN = new Action("SHIELD_REGEN", "Regenerate Shield", LIGHT_BLUE, LIGHT_BLUE_UNUSABLE, FIRE_ACTION_HIGHLIGHT, false, g -> {
                g.setStroke(ICON_STROKE_NARROW_3_NON_SCALED);
                GameRenderer.renderScaled(ACTION_BUTTON_SIZE, g, () -> {
                    g.draw(ActionShapes.SHIELD);
                    g.fill(ActionShapes.SHIELD);
                    g.scale(.8, .8);
                    g.translate(.15, .12);
                    g.draw(ActionShapes.SHIP);
                    g.fill(ActionShapes.SHIP);
                });
            }, "Regenerate a portion of this unit's shield. While expensive in terms of " + EnergyManager.displayName + ", " +
                    "having shield HP provides several advantages over regular HP. Most notably, it allows the unit to " +
                    "take damage without suffering a loss in firepower, as damage is based only on regular HP, and remains " +
                    "unaffected when losing shield HP.", 0),
            STEALTH = new Action("STEALTH", "Stealth", YELLOW, YELLOW_UNUSABLE, FIRE_ACTION_HIGHLIGHT, false, g -> {
                GameRenderer.renderScaled(ACTION_BUTTON_SIZE, g, () -> {
                    ActionShapes.stealthIcon(g);
                });
            }, "Toggle stealth mode. While in stealth mode, the ship will be hidden from " +
                    "enemies, unless directly adjacent to an enemy unit. The ship also loses the " +
                    "ability to fire, and each ship kept in stealth mode costs a small amount of " +
                    EnergyManager.displayName + " at the end of each turn, on top of the fixed cost " +
                    "to enter stealth mode in the first place. This is visible in the form of a reduction in " +
                    EnergyManager.displayName + " income. Not only that, ships that have this ability " +
                    "are also unable to capture structures, regardless of whether or not they're in stealth mode.", -20),
            MINE = new Action("MINE", "Mine", PURPLE, PURPLE_UNUSABLE, REPAIR_ACTION_HIGHLIGHT, false, g -> {
                GameRenderer.renderScaled(ACTION_BUTTON_SIZE, g, () -> {
                    g.fill(ActionShapes.ANTIMATTER);
                });
            }, "Mine asteroid fields for " + EnergyManager.displayName + ". Mining increases income, meaning that " +
                    "the additional " + EnergyManager.displayName + " is credited at the start of each turn as long as this unit is mining. " +
                    "Each turn that this unit is mining for, the asteroid field has its " + EnergyManager.displayName + " depleted, until the asteroid " +
                    "field is gone. The number of turns remaining until an asteroid field is depleted can be seen in the tile info " +
                    "in the bottom right when the tile is selected, unless the tile is outside of view range. Moving the unit interrupts mining.", -30),
            REPAIR = new Action("REPAIR", "Repair", GREEN, GREEN_UNUSABLE, REPAIR_ACTION_HIGHLIGHT, true, g -> {
                GameRenderer.renderScaled(ACTION_BUTTON_SIZE, g, () -> {
                    g.fill(ActionShapes.PLUS);
                });
            }, "Repair some of the HP of an allied unit. You must be adjacent to the unit that needs repairs, " +
                    "and HP cannot go above the max HP for the repaired unit.", -10),
            RESUPPLY = new Action("RESUPPLY", "Resupply", BROWN, BROWN_UNUSABLE, RESUPPLY_ACTION_HIGHLIGHT, false, g -> {
                g.setStroke(ICON_STROKE_NARROW_1_NON_SCALED);
                GameRenderer.renderScaled(ACTION_BUTTON_SIZE, g, () -> {
                    g.draw(ActionShapes.SUPPLY);
                });
            }, "Resupply the ammunition of an allied unit. You must be adjacent to the unit that needs resupplying.", -5);

    private final String name, displayName;
    private final ActionColour colour, unusableColour;
    public final Color tileColour;
    private final Renderable iconImageRenderer;
    private final boolean scaled;
    public final String infoText;
    private final int order;

    public static final StaticHitBox buttonBox = new StaticHitBox(ACTION_BUTTON_SIZE / 2, -ACTION_BUTTON_SIZE / 2, -ACTION_BUTTON_SIZE / 2, ACTION_BUTTON_SIZE / 2);

    public Action(String name, String displayName, ActionColour colour, ActionColour unusableColour, Color tileColour, boolean scaled, Renderable iconImageRenderer, String infoText, int order) {
        this.displayName = displayName;
        this.unusableColour = unusableColour;
        this.infoText = infoText;
        this.order = order;
        names.put(name, this);
        this.name = name;
        this.colour = colour;
        this.tileColour = tileColour;
        this.scaled = scaled;
        this.iconImageRenderer = iconImageRenderer;
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
        g.setStroke(ICON_STROKE);
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

    public static Action getByName(String name) {
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

    @Override
    public String toString() {
        return name;
    }

    @Override
    public String getName() {
        return displayName;
    }

    public String getInternalName() {
        return name;
    }
}
