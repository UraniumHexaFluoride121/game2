package unit.action;

import foundation.Deletable;
import foundation.input.ButtonOrder;
import foundation.input.InputType;
import foundation.input.RegisteredButtonInputReceiver;
import foundation.math.ObjPos;
import render.Renderable;
import unit.Unit;

import java.awt.*;
import java.util.HashMap;
import java.util.function.Supplier;

import static unit.action.Action.*;

public class ActionSelector implements Renderable, Deletable, RegisteredButtonInputReceiver {
    public static final float BORDER_OFFSET = 0.4f, ACTION_SPACING = 0.4f;
    public static final BasicStroke BORDER_STROKE = new BasicStroke(0.2f * SCALING, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 500);
    public static final Color BACKGROUND_COLOUR = new Color(0, 0, 0, 150), BORDER_COLOUR = new Color(90, 90, 90);

    public HashMap<Action, ActionData> actionMap = new HashMap<>();
    public Action[] actions;
    private final Supplier<ObjPos> renderPos;
    private Supplier<Boolean> isVisible;

    public ActionSelector(Supplier<ObjPos> renderPos, Supplier<Boolean> isVisible) {
        this.renderPos = renderPos;
        this.isVisible = isVisible;
    }

    public void setActions(Action[] actions, Unit unit) {
        this.actions = actions;
        actionMap.clear();
        int i = 0;
        for (Action action : actions) {
            actionMap.putIfAbsent(action, new ActionData(i, () -> unit.onActionSelect(action), false));
            i++;
        }
    }

    @Override
    public void render(Graphics2D g) {
        if (!isVisible.get())
            return;
        g.translate(0, -ACTION_BUTTON_SIZE / 2 - BORDER_OFFSET * 2);
        g.scale(1d / SCALING, 1d / SCALING);
        g.setStroke(BORDER_STROKE);
        g.setColor(BACKGROUND_COLOUR);
        float xOrigin = (-ACTION_BUTTON_SIZE / 2 * actions.length - ACTION_SPACING / 2 * (actions.length - 1) - BORDER_OFFSET) * SCALING;
        float width = (ACTION_BUTTON_SIZE * actions.length + ACTION_SPACING * (actions.length - 1) + BORDER_OFFSET * 2) * SCALING;
        g.fillRoundRect(
                (int) xOrigin,
                (int) ((-ACTION_BUTTON_SIZE / 2 - BORDER_OFFSET) * SCALING),
                (int) width,
                (int) ((ACTION_BUTTON_SIZE + BORDER_OFFSET * 2) * SCALING),
                (int) ((ROUNDING + BORDER_OFFSET * 2) * SCALING),
                (int) ((ROUNDING + BORDER_OFFSET * 2) * SCALING)
        );
        g.setColor(BORDER_COLOUR);
        g.drawRoundRect(
                (int) xOrigin,
                (int) ((-ACTION_BUTTON_SIZE / 2 - BORDER_OFFSET) * SCALING),
                (int) width,
                (int) ((ACTION_BUTTON_SIZE + BORDER_OFFSET * 2) * SCALING),
                (int) ((ROUNDING + BORDER_OFFSET * 2) * SCALING),
                (int) ((ROUNDING + BORDER_OFFSET * 2) * SCALING)
        );
        g.scale(SCALING, SCALING);
        for (int i = 0; i < actions.length; i++) {
            g.translate((i - (actions.length - 1) / 2f) * (ACTION_BUTTON_SIZE + ACTION_SPACING), 0);
            actions[i].render(g, actionMap.get(actions[i]));
            g.translate(-(i - (actions.length - 1) / 2f) * (ACTION_BUTTON_SIZE + ACTION_SPACING), 0);
        }
        g.translate(0, ACTION_BUTTON_SIZE / 2 + BORDER_OFFSET * 2);
    }

    public void updateActions(Unit unit) {
        if (actionMap.containsKey(FIRE)) {
            actionMap.get(FIRE).enabled = !unit.tilesInFiringRange().isEmpty();
        }
        if (actionMap.containsKey(MOVE)) {
            actionMap.get(MOVE).enabled = true;
        }
        actionMap.forEach((a, d) -> {
            if (unit.performedActions.contains(a))
                d.enabled = false;
        });
    }

    @Override
    public boolean blocking(InputType type) {
        return (type == InputType.MOUSE_OVER || type == InputType.MOUSE_LEFT) && isVisible.get();
    }

    @Override
    public ButtonOrder getButtonOrder() {
        return ButtonOrder.ACTION_SELECTOR;
    }

    private ObjPos actionOffset(int i) {
        return new ObjPos().subtract(renderPos.get()).subtract((i - (actions.length - 1) / 2f) * (ACTION_BUTTON_SIZE + ACTION_SPACING), -ACTION_BUTTON_SIZE / 2 - BORDER_OFFSET * 2);
    }

    private boolean isInsideAction(int i, ObjPos pos) {
        return buttonBox.isPositionInside(pos.copy().add(actionOffset(i)));
    }

    @Override
    public boolean posInside(ObjPos pos) {
        if (!isVisible.get())
            return false;
        for (int i = 0; i < actions.length; i++) {
            if (isInsideAction(i, pos))
                return true;
        }
        return false;
    }

    @Override
    public void buttonPressed(ObjPos pos, boolean inside, boolean blocked, InputType type) {
        if (!isVisible.get())
            return;
        actionMap.forEach((a, d) -> {
            if (d.enabled)
                d.clickHandler.buttonPressed(pos, isInsideAction(d.index, pos), blocked, type);
        });
    }

    @Override
    public void buttonReleased(ObjPos pos, boolean inside, boolean blocked, InputType type) {
        if (!isVisible.get())
            return;
        actionMap.forEach((a, d) -> {
            if (d.enabled)
                d.clickHandler.buttonReleased(pos, inside, blocked, type);
        });
    }

    @Override
    public void delete() {
        isVisible = null;
        actionMap.forEach((a, d) -> d.clickHandler.delete());
        actionMap.clear();
        actions = null;
    }
}
