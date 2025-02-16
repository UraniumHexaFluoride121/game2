package unit.action;

import foundation.Deletable;
import foundation.input.ButtonOrder;
import foundation.input.InputType;
import foundation.input.RegisteredButtonInputReceiver;
import foundation.math.ObjPos;
import render.GameRenderer;
import render.Renderable;
import render.ui.implementation.EnergyCostDisplay;
import unit.Unit;

import java.awt.*;
import java.util.Comparator;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static unit.action.Action.*;

public class ActionSelector implements Renderable, Deletable, RegisteredButtonInputReceiver {
    public static final float BORDER_OFFSET = 0.4f, ACTION_SPACING = 0.4f;
    public static final BasicStroke BORDER_STROKE = new BasicStroke(0.2f * SCALING, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 500);
    public static final Color BACKGROUND_COLOUR = new Color(0, 0, 0, 150), BORDER_COLOUR = new Color(90, 90, 90);

    public TreeMap<Action, ActionData> actionMap = new TreeMap<>(Comparator.comparingInt(Action::getOrder));
    private final EnergyCostDisplay energyCostDisplay = new EnergyCostDisplay();
    private Supplier<Boolean> isVisible;

    private Unit unit;

    public ActionSelector(Supplier<Boolean> isVisible, Unit unit) {
        this.unit = unit;
        this.isVisible = isVisible;
    }

    public void setActions(Action[] actions, Unit unit) {
        actionMap.clear();
        for (Action action : actions) {
            addAction(action, unit);
        }
    }

    @Override
    public void render(Graphics2D g) {
        if (!isVisible.get())
            return;
        actionMap.forEach((a, d) -> {
            if (!d.clickHandler.isDefault()) {
                unit.type.getActionCost(a).ifPresent(cost -> {
                    energyCostDisplay.setCost(-cost, unit.getLevel());
                    energyCostDisplay.renderToEnergyManager(unit.getLevel());
                    GameRenderer.renderOffset(0, -4.5f, g, () -> {
                        energyCostDisplay.render(g);
                    });
                });
            }
        });
        g.translate(0, -ACTION_BUTTON_SIZE / 2 - BORDER_OFFSET * 2);
        g.scale(1d / SCALING, 1d / SCALING);
        g.setStroke(BORDER_STROKE);
        g.setColor(BACKGROUND_COLOUR);
        float xOrigin = (-ACTION_BUTTON_SIZE / 2 * actionMap.size() - ACTION_SPACING / 2 * (actionMap.size() - 1) - BORDER_OFFSET) * SCALING;
        float width = (ACTION_BUTTON_SIZE * actionMap.size() + ACTION_SPACING * (actionMap.size() - 1) + BORDER_OFFSET * 2) * SCALING;
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
        int i = 0;
        for (Action action : actionMap.keySet()) {
            g.translate((i - (actionMap.size() - 1) / 2f) * (ACTION_BUTTON_SIZE + ACTION_SPACING), 0);
            action.render(g, actionMap.get(action));
            g.translate(-(i - (actionMap.size() - 1) / 2f) * (ACTION_BUTTON_SIZE + ACTION_SPACING), 0);
            i++;
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
        if (actionMap.containsKey(SHIELD_REGEN)) {
            actionMap.get(SHIELD_REGEN).enabled = unit.shieldHP < unit.type.shieldHP;
        }
        if (unit.canCapture()) {
            addActionEnabled(CAPTURE, unit);
            actionMap.get(CAPTURE).enabled = !unit.hasPerformedAction(CAPTURE);
        } else {
            removeAction(CAPTURE);
        }
        actionMap.forEach((a, d) -> {
            if (unit.hasPerformedAction(a))
                d.enabled = false;
        });
    }

    public void addAction(Action action, Unit unit) {
        actionMap.putIfAbsent(action, new ActionData(() -> {
            if (unit.getLevel().levelRenderer.energyManager.canAfford(unit, action, false))
                unit.onActionSelect(action);
        }, false));
    }

    public void addActionEnabled(Action action, Unit unit) {
        actionMap.putIfAbsent(action, new ActionData(() -> {
            if (unit.getLevel().levelRenderer.energyManager.canAfford(unit, action, false))
                unit.onActionSelect(action);
        }, true));
    }

    public void removeAction(Action action) {
        ActionData d = actionMap.remove(action);
        if (d != null)
            d.clickHandler.delete();
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
        return new ObjPos().subtract(unit.getRenderPos()).subtract((i - (actionMap.size() - 1) / 2f) * (ACTION_BUTTON_SIZE + ACTION_SPACING), -ACTION_BUTTON_SIZE / 2 - BORDER_OFFSET * 2);
    }

    private boolean isInsideAction(int i, ObjPos pos) {
        return buttonBox.isPositionInside(pos.copy().add(actionOffset(i)));
    }

    @Override
    public boolean posInside(ObjPos pos) {
        if (!isVisible.get())
            return false;
        for (int i = 0; i < actionMap.size(); i++) {
            if (isInsideAction(i, pos))
                return true;
        }
        return false;
    }

    @Override
    public void buttonPressed(ObjPos pos, boolean inside, boolean blocked, InputType type) {
        if (!isVisible.get())
            return;
        AtomicInteger i = new AtomicInteger();
        actionMap.forEach((a, d) -> {
            if (d.enabled)
                d.clickHandler.buttonPressed(pos, isInsideAction(i.get(), pos), blocked, type);
            i.getAndIncrement();
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
        unit = null;
        actionMap.forEach((a, d) -> d.clickHandler.delete());
        actionMap.clear();
    }
}
