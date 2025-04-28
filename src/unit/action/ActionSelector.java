package unit.action;

import foundation.Deletable;
import foundation.input.ButtonOrder;
import foundation.input.InputType;
import foundation.input.RegisteredButtonInputReceiver;
import foundation.math.ObjPos;
import level.energy.EnergyCostDisplay;
import level.tile.TileType;
import render.GameRenderer;
import render.Renderable;
import render.types.text.FixedTextRenderer;
import render.types.text.TextAlign;
import render.UIColourTheme;
import render.types.box.UIBox;
import unit.Unit;
import unit.UnitData;

import java.awt.*;
import java.util.Comparator;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static render.types.text.UITextLabel.*;
import static unit.action.Action.*;

public class ActionSelector implements Renderable, Deletable, RegisteredButtonInputReceiver {
    public static final float BORDER_OFFSET = 0.4f, ACTION_SPACING = 0.4f;
    public static final BasicStroke BORDER_STROKE = new BasicStroke(0.2f * SCALING, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 500);
    public static final Color BACKGROUND_COLOUR = new Color(0, 0, 0, 150), BORDER_COLOUR = new Color(90, 90, 90);

    public TreeMap<Action, ActionData> actionMap = new TreeMap<>(Comparator.comparingInt(Action::getOrder));
    private final EnergyCostDisplay energyCostDisplay = new EnergyCostDisplay(false), energyCostPerTurnDisplay = new EnergyCostDisplay(true);
    private Supplier<Boolean> isVisible;

    private final UIBox actionUnusableBox = new UIBox(4f, 1).setCorner(.25f).setColourTheme(UIColourTheme.DARK_GRAY);
    private final FixedTextRenderer actionUnusableText = new FixedTextRenderer("Unavailable", .6f, TEXT_COLOUR)
            .setBold(true).setTextAlign(TextAlign.CENTER);

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
                if (d.type == ActionIconType.ENABLED) {
                    Optional<Integer> perTurnActionCost = unit.getPerTurnActionCost(a);
                    perTurnActionCost.ifPresent(perTurnCost -> {
                        energyCostPerTurnDisplay.setCost(unit.removeActionEnergyCost(a) ? perTurnCost : -perTurnCost, unit.getLevel());
                        energyCostPerTurnDisplay.renderToEnergyManager(unit.getLevel());
                        GameRenderer.renderOffset(0, -4.5f, g, () -> {
                            energyCostPerTurnDisplay.render(g);
                        });
                    });
                    unit.getActionCost(a).ifPresent(cost -> {
                        if (unit.removeActionEnergyCost(a))
                            return;
                        energyCostDisplay.setCost(-cost, unit.getLevel());
                        energyCostDisplay.renderToEnergyManager(unit.getLevel());
                        GameRenderer.renderOffset(0, perTurnActionCost.isPresent() ? -5.8f : -4.5f, g, () -> {
                            energyCostDisplay.render(g);
                        });
                    });
                } else if (d.type == ActionIconType.UNUSABLE) {
                    GameRenderer.renderOffset(-2f, -4.5f, g, () -> {
                        actionUnusableBox.render(g);
                        g.translate(2f, 0.27f);
                        actionUnusableText.render(g);
                    });
                }
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
            actionMap.get(FIRE).type = !unit.tilesInFiringRange(unit.getLevel().currentVisibility, new UnitData(unit), true).isEmpty() &&
                    !unit.stealthMode ? ActionIconType.ENABLED : unit.hasPerformedAction(FIRE) ? ActionIconType.DISABLED : ActionIconType.UNUSABLE;
        }
        if (actionMap.containsKey(MOVE)) {
            actionMap.get(MOVE).type = ActionIconType.ENABLED;
        }
        if (actionMap.containsKey(STEALTH)) {
            actionMap.get(STEALTH).type = ActionIconType.ENABLED;
        }
        if (actionMap.containsKey(MINE)) {
            actionMap.get(MINE).type = unit.getLevel().getTile(unit.pos).type == TileType.ASTEROIDS ? ActionIconType.ENABLED : ActionIconType.UNUSABLE;
        }
        if (actionMap.containsKey(REPAIR)) {
            actionMap.get(REPAIR).type = unit.getRepairTiles(unit.pos).isEmpty() ? ActionIconType.UNUSABLE : ActionIconType.ENABLED;
        }
        if (actionMap.containsKey(RESUPPLY)) {
            actionMap.get(RESUPPLY).type = unit.getResupplyTiles(unit.pos).isEmpty() ? ActionIconType.UNUSABLE : ActionIconType.ENABLED;
        }
        if (actionMap.containsKey(SHIELD_REGEN)) {
            actionMap.get(SHIELD_REGEN).type = unit.shieldHP < unit.type.shieldHP ? ActionIconType.ENABLED : ActionIconType.UNUSABLE;
        }
        if (unit.canCapture()) {
            addActionEnabled(CAPTURE, unit);
            actionMap.get(CAPTURE).type = !unit.hasPerformedAction(CAPTURE) ? ActionIconType.ENABLED : ActionIconType.DISABLED;
        } else {
            removeAction(CAPTURE);
        }
        actionMap.forEach((a, d) -> {
            if (unit.hasPerformedAction(a))
                d.type = ActionIconType.DISABLED;
        });
    }

    public void addAction(Action action, Unit unit) {
        ActionData data = new ActionData(ActionIconType.DISABLED);
        actionMap.putIfAbsent(action, data);
        data.init(() -> {
            if (data.type == ActionIconType.ENABLED && unit.getLevel().levelRenderer.energyManager.canAfford(unit, action, false))
                unit.onActionSelect(action);
        });
    }

    public void addActionEnabled(Action action, Unit unit) {
        ActionData data = new ActionData(ActionIconType.ENABLED);
        actionMap.putIfAbsent(action, data);
        data.init(() -> {
            if (data.type == ActionIconType.ENABLED && unit.getLevel().levelRenderer.energyManager.canAfford(unit, action, false))
                unit.onActionSelect(action);
        });
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
    public boolean posInside(ObjPos pos, InputType type) {
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
            d.clickHandler.buttonPressed(pos, isInsideAction(i.get(), pos), blocked, type);
            i.getAndIncrement();
        });
    }

    @Override
    public void buttonReleased(ObjPos pos, boolean inside, boolean blocked, InputType type) {
        if (!isVisible.get())
            return;
        actionMap.forEach((a, d) -> {
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
