package unit.action;

import foundation.input.ButtonState;
import foundation.math.StaticHitBox;
import render.Renderable;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.util.HashMap;

public class Action {
    public static final float ACTION_BUTTON_SIZE = 2f;
    public static final float ROUNDING = 1, BORDER = 0.15f;

    public static final BasicStroke ICON_STROKE = new BasicStroke(0.2f * Renderable.SCALING, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 500);
    public static final BasicStroke ICON_STROKE_NARROW = new BasicStroke(0.15f * Renderable.SCALING, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 500);
    public static final Color ICON_COLOUR = new Color(214, 214, 214);

    public static final Color
            MOVE_ACTION_HIGHLIGHT = new Color(122, 210, 248, 26),
            FIRE_ACTION_HIGHLIGHT = new Color(248, 122, 122, 26);

    public static final Action
            MOVE = new Action("MOVE", ActionColour.BLUE, MOVE_ACTION_HIGHLIGHT, true, g -> {
        g.drawLine(
                (int) (ACTION_BUTTON_SIZE * 0.37f * Renderable.SCALING),
                (int) (ACTION_BUTTON_SIZE * 0.37f * Renderable.SCALING),
                (int) (ACTION_BUTTON_SIZE * 0.75f * Renderable.SCALING),
                (int) (ACTION_BUTTON_SIZE * 0.75f * Renderable.SCALING)
        );
        g.drawLine(
                (int) (ACTION_BUTTON_SIZE * 0.25f * Renderable.SCALING),
                (int) (ACTION_BUTTON_SIZE * 0.25f * Renderable.SCALING),
                (int) (ACTION_BUTTON_SIZE * 0.25f * Renderable.SCALING),
                (int) (ACTION_BUTTON_SIZE * 0.25f * Renderable.SCALING)
        );
        g.drawLine(
                (int) (ACTION_BUTTON_SIZE * 0.45f * Renderable.SCALING),
                (int) (ACTION_BUTTON_SIZE * 0.75f * Renderable.SCALING),
                (int) (ACTION_BUTTON_SIZE * 0.75f * Renderable.SCALING),
                (int) (ACTION_BUTTON_SIZE * 0.75f * Renderable.SCALING)
        );
        g.drawLine(
                (int) (ACTION_BUTTON_SIZE * 0.75f * Renderable.SCALING),
                (int) (ACTION_BUTTON_SIZE * 0.45f * Renderable.SCALING),
                (int) (ACTION_BUTTON_SIZE * 0.75f * Renderable.SCALING),
                (int) (ACTION_BUTTON_SIZE * 0.75f * Renderable.SCALING)
        );
    }), FIRE = new Action("FIRE", ActionColour.RED, FIRE_ACTION_HIGHLIGHT, true, g -> {
        g.setStroke(ICON_STROKE_NARROW);
        g.drawOval(
                (int) (ACTION_BUTTON_SIZE * 0.25f * Renderable.SCALING),
                (int) (ACTION_BUTTON_SIZE * 0.25f * Renderable.SCALING),
                (int) (ACTION_BUTTON_SIZE * 0.5f * Renderable.SCALING),
                (int) (ACTION_BUTTON_SIZE * 0.5f * Renderable.SCALING)
        );
        g.drawLine(
                (int) (ACTION_BUTTON_SIZE * 0.2f * Renderable.SCALING),
                (int) (ACTION_BUTTON_SIZE * 0.5f * Renderable.SCALING),
                (int) (ACTION_BUTTON_SIZE * 0.38f * Renderable.SCALING),
                (int) (ACTION_BUTTON_SIZE * 0.5f * Renderable.SCALING)
        );
        g.drawLine(
                (int) (ACTION_BUTTON_SIZE * 0.8f * Renderable.SCALING),
                (int) (ACTION_BUTTON_SIZE * 0.5f * Renderable.SCALING),
                (int) (ACTION_BUTTON_SIZE * 0.62f * Renderable.SCALING),
                (int) (ACTION_BUTTON_SIZE * 0.5f * Renderable.SCALING)
        );
        g.drawLine(
                (int) (ACTION_BUTTON_SIZE * 0.5f * Renderable.SCALING),
                (int) (ACTION_BUTTON_SIZE * 0.2f * Renderable.SCALING),
                (int) (ACTION_BUTTON_SIZE * 0.5f * Renderable.SCALING),
                (int) (ACTION_BUTTON_SIZE * 0.38f * Renderable.SCALING)
        );
        g.drawLine(
                (int) (ACTION_BUTTON_SIZE * 0.5f * Renderable.SCALING),
                (int) (ACTION_BUTTON_SIZE * 0.8f * Renderable.SCALING),
                (int) (ACTION_BUTTON_SIZE * 0.5f * Renderable.SCALING),
                (int) (ACTION_BUTTON_SIZE * 0.62f * Renderable.SCALING)
        );
    });

    private final String name;
    private final ActionColour colour;
    public final Color tileColour;
    private final Renderable iconImageRenderer;
    private final boolean scaled;

    private final HashMap<ButtonState, Renderable> defaultIcons = new HashMap<>(), disabledIcons = new HashMap<>();

    public static final StaticHitBox buttonBox = new StaticHitBox(ACTION_BUTTON_SIZE / 2, -ACTION_BUTTON_SIZE / 2, -ACTION_BUTTON_SIZE / 2, ACTION_BUTTON_SIZE / 2);

    public Action(String name, ActionColour colour, Color tileColour, boolean scaled, Renderable iconImageRenderer) {
        this.name = name;
        this.colour = colour;
        this.tileColour = tileColour;
        this.scaled = scaled;
        this.iconImageRenderer = iconImageRenderer;
        for (ButtonState state : ButtonState.values()) {
            defaultIcons.put(state, Renderable.renderImage(createImage(state, true), false, false, -1));
        }
        for (ButtonState state : ButtonState.values()) {
            disabledIcons.put(state, Renderable.renderImage(createImage(state, false), false, false, -1));
        }
    }

    public void render(Graphics2D g, ActionData data) {
        float offset = (ACTION_BUTTON_SIZE) / 2;
        g.translate(-offset, -offset);
        if (data.enabled) {
            defaultIcons.get(data.clickHandler.state).render(g);
        } else {
            disabledIcons.get(ButtonState.DEFAULT).render(g);
        }
        g.translate(offset, offset);
    }

    private BufferedImage createImage(ButtonState state, boolean enabled) {
        BufferedImage image = Renderable.renderToImage(Renderable.createImage(ACTION_BUTTON_SIZE, ACTION_BUTTON_SIZE, BufferedImage.TYPE_INT_ARGB), g -> {
            g.scale(1d / Renderable.SCALING, 1d / Renderable.SCALING);
            g.setColor(enabled ? colour.border : ActionColour.DISABLED.border);
            g.fillRoundRect(
                    0,
                    0,
                    (int) (ACTION_BUTTON_SIZE * Renderable.SCALING),
                    (int) (ACTION_BUTTON_SIZE * Renderable.SCALING),
                    (int) (Renderable.SCALING * ROUNDING),
                    (int) (Renderable.SCALING * ROUNDING)
            );
            g.setColor(enabled ? colour.background : ActionColour.DISABLED.background);
            float border = BORDER * stateBorderScale(state);
            g.fillRoundRect(
                    (int) (border * Renderable.SCALING),
                    (int) (border * Renderable.SCALING),
                    (int) ((ACTION_BUTTON_SIZE - border * 2) * Renderable.SCALING),
                    (int) ((ACTION_BUTTON_SIZE - border * 2) * Renderable.SCALING),
                    (int) (Renderable.SCALING * (ROUNDING - border * 2)),
                    (int) (Renderable.SCALING * (ROUNDING - border * 2))
            );
            g.setStroke(ICON_STROKE);
            g.setColor(ICON_COLOUR);
            if (scaled)
                iconImageRenderer.render(g);
            g.scale(Renderable.SCALING, Renderable.SCALING);
            if (!scaled)
                iconImageRenderer.render(g);
        });
        Graphics2D g = image.createGraphics();
        new RescaleOp(stateBrightness(state), 0, g.getRenderingHints()).filter(image, image);
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

    @Override
    public String toString() {
        return name;
    }
}
