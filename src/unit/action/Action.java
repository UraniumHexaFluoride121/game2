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
    public static final BasicStroke ICON_STROKE_NARROW = new BasicStroke(0.15f * SCALING, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 500);
    public static final BasicStroke ICON_STROKE_NARROW_NON_SCALED = new BasicStroke(0.15f / ACTION_BUTTON_SIZE, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 500);
    public static final BasicStroke ICON_STROKE_EXTRA_NARROW_NON_SCALED = new BasicStroke(0.04f / ACTION_BUTTON_SIZE, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 500);
    public static final Color ICON_COLOUR = new Color(214, 214, 214);
    public static final Color ICON_COLOUR_UNUSABLE = new Color(195, 195, 195);

    public static final Color
            MOVE_ACTION_HIGHLIGHT = new Color(122, 210, 248, 26),
            FIRE_ACTION_HIGHLIGHT = new Color(248, 122, 122, 26);

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
            "larger ship classes generally costing more to move.", 2),
            FIRE = new Action("FIRE", "Fire", RED, RED_UNUSABLE, FIRE_ACTION_HIGHLIGHT, true, g -> {
                g.setStroke(ICON_STROKE_NARROW);
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
                g.setStroke(ICON_STROKE_NARROW_NON_SCALED);
                GameRenderer.renderScaled(ACTION_BUTTON_SIZE, g, () -> {
                    g.draw(ActionShapes.FLAG);
                    g.fill(ActionShapes.FLAG);
                });
            }, "This action only appears when over the top of an enemy structure that can be captured. Capturing " +
                    "takes several turns, and each time you're attacked during a capture (not including counterattacks), " +
                    "your capture progress gets reduced. Capturing an enemy base leads to that team being eliminated.", -1),
            SHIELD_REGEN = new Action("SHIELD_REGEN", "Regenerate Shield", LIGHT_BLUE, LIGHT_BLUE_UNUSABLE, FIRE_ACTION_HIGHLIGHT, false, g -> {
                g.setStroke(ICON_STROKE_EXTRA_NARROW_NON_SCALED);
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
                    "are also unable to capture structures, regardless of whether or not they're in stealth mode.", -2);

    private final String name, displayName;
    private final ActionColour colour, unusableColour;
    public final Color tileColour;
    private final Renderable iconImageRenderer;
    private final boolean scaled;
    public final String infoText;
    private final int order;

    private final HashMap<ButtonState, Renderable> defaultIcons = new HashMap<>(), unusableIcons = new HashMap<>(), disabledIcons = new HashMap<>();

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
        for (ButtonState state : ButtonState.values()) {
            defaultIcons.put(state, Renderable.renderImage(createImage(state, ActionIconType.ENABLED), false, false, -1));
            unusableIcons.put(state, Renderable.renderImage(createImage(state, ActionIconType.UNUSABLE), false, false, -1));
            disabledIcons.put(state, Renderable.renderImage(createImage(state, ActionIconType.DISABLED), false, false, -1));
        }
    }

    public void render(Graphics2D g, ActionData data) {
        float offset = (ACTION_BUTTON_SIZE) / 2;
        g.translate(-offset, -offset);
        switch (data.type) {
            case ENABLED -> defaultIcons.get(data.clickHandler.state).render(g);
            case UNUSABLE -> unusableIcons.get(ButtonState.DEFAULT).render(g);
            case DISABLED -> disabledIcons.get(ButtonState.DEFAULT).render(g);
        }
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
        );g.setColor(switch (type) {
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

    private BufferedImage createImage(ButtonState state, ActionIconType type) {
        BufferedImage image = Renderable.renderToImage(Renderable.createImage(ACTION_BUTTON_SIZE, ACTION_BUTTON_SIZE, BufferedImage.TYPE_INT_ARGB), g -> {
            renderIcon(g, type, state);
        });
        Graphics2D g = image.createGraphics();
        new RescaleOp(stateBrightness(type == ActionIconType.UNUSABLE ? ButtonState.DEFAULT : state), 0, g.getRenderingHints()).filter(image, image);
        g.dispose();
        return image;
    }

    private static float stateBrightness(ButtonState state) {
        return switch (state) {
            case DEFAULT -> 1;
            case HOVER -> 1.1f;
            case PRESSED -> 0.85f;
            case SELECTED -> 0.75f;
        };
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
