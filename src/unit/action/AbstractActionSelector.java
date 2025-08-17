package unit.action;

import foundation.Deletable;
import foundation.input.InputType;
import foundation.input.RegisteredButtonInputReceiver;
import foundation.math.ObjPos;
import level.energy.EnergyCostDisplay;
import level.tile.Tile;
import render.*;
import render.types.box.UIBox;
import render.types.text.TextRenderer;
import unit.Unit;

import java.awt.*;
import java.util.Comparator;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static render.types.text.UITextLabel.*;
import static unit.action.Action.*;

public abstract class AbstractActionSelector implements Renderable, Deletable, RegisteredButtonInputReceiver {
    public static final float BORDER_OFFSET = 0.4f, ACTION_SPACING = 0.4f;
    public static final BasicStroke BORDER_STROKE = new BasicStroke(0.2f * SCALING, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 500);
    public static final Color BACKGROUND_COLOUR = new Color(0, 0, 0, 150), BORDER_COLOUR = new Color(90, 90, 90);

    public TreeMap<Action, ActionData> actionMap = new TreeMap<>(Comparator.comparingInt(Action::getOrder));
    private final EnergyCostDisplay energyCostDisplay = new EnergyCostDisplay(false), energyCostPerTurnDisplay = new EnergyCostDisplay(true);

    private final UIBox actionUnusableBox = new UIBox(4f, 1).setCorner(.5f).setColourTheme(UIColourTheme.DARK_GRAY);
    private final TextRenderer actionUnusableText = new TextRenderer("Unavailable", .6f, TEXT_COLOUR)
            .setBold(true).setTextAlign(HorizontalAlign.CENTER);

    protected Supplier<Boolean> isVisible;

    public AbstractActionSelector(Supplier<Boolean> isVisible) {
        this.isVisible = isVisible;
    }

    @Override
    public void render(Graphics2D g) {
        if (!isVisible.get())
            return;
        actionMap.forEach((a, d) -> {
            if (!d.clickHandler.isDefault()) {
                if (d.type == ActionIconType.ENABLED) {
                    Optional<Integer> perTurnActionCost = getPerTurnActionCost(a);
                    perTurnActionCost.ifPresent(perTurnCost -> {
                        setCost(energyCostPerTurnDisplay, a, removeActionEnergyCost(a) ? perTurnCost : -perTurnCost);
                        renderPerTurnCostDisplay(energyCostPerTurnDisplay);
                        GameRenderer.renderOffset(0, -4.5f, g, () -> {
                            energyCostPerTurnDisplay.render(g);
                        });
                    });
                    getActionCost(a).ifPresent(cost -> {
                        setCost(energyCostDisplay, a, -cost);
                        renderActionCostDisplay(energyCostDisplay);
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

    public void addAction(Action action, Unit unit) {
        ActionData data = new ActionData(ActionIconType.DISABLED);
        actionMap.putIfAbsent(action, data);
        if (unit != null)
            data.init(() -> {
                if (data.type == ActionIconType.ENABLED && unit.getLevel().levelRenderer.energyManager.canAfford(unit, action, false))
                    unit.onActionSelect(action);
            });
        else
            data.init(() -> {});
    }

    public void addActionEnabled(Action action, Unit unit) {
        ActionData data = new ActionData(ActionIconType.ENABLED);
        actionMap.putIfAbsent(action, data);
        if (unit != null)
            data.init(() -> {
                if (data.type == ActionIconType.ENABLED && unit.getLevel().levelRenderer.energyManager.canAfford(unit, action, false))
                    unit.onActionSelect(action);
            });
        else
            data.init(() -> {});
    }

    public void removeAction(Action action) {
        ActionData d = actionMap.remove(action);
        if (d != null)
            d.clickHandler.delete();
    }

    public void clear() {
        Action.forEach(this::removeAction);
    }

    @Override
    public boolean blocking(InputType type) {
        return (type == InputType.MOUSE_OVER || type == InputType.MOUSE_LEFT) && isVisible.get();
    }

    @Override
    public RenderOrder getButtonOrder() {
        return RenderOrder.ACTION_SELECTOR;
    }

    private ObjPos actionOffset(int i) {
        return new ObjPos().subtract(getRenderPos()).subtract((i - (actionMap.size() - 1) / 2f) * (ACTION_BUTTON_SIZE + ACTION_SPACING), -ACTION_BUTTON_SIZE / 2 - BORDER_OFFSET * 2);
    }

    public static ObjPos actionCenter(int actionCount, int i) {
        return new ObjPos((i - (actionCount - 1) / 2f) * (ACTION_BUTTON_SIZE + ACTION_SPACING), -ACTION_BUTTON_SIZE / 2 - BORDER_OFFSET * 2);
    }

    private boolean isInsideAction(int i, ObjPos pos) {
        return buttonBox.isPositionInside(pos.copy().add(actionOffset(i)));
    }

    protected abstract ObjPos getRenderPos();

    protected abstract Optional<Integer> getPerTurnActionCost(Action a);

    protected abstract Optional<Integer> getActionCost(Action a);

    protected abstract boolean removeActionEnergyCost(Action a);

    protected void renderPerTurnCostDisplay(EnergyCostDisplay display) {

    }

    protected void renderActionCostDisplay(EnergyCostDisplay display) {

    }

    protected abstract void setCost(EnergyCostDisplay display, Action a, int cost);

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
        actionMap.forEach((a, d) -> d.clickHandler.delete());
        actionMap.clear();
        energyCostDisplay.delete();
        energyCostPerTurnDisplay.delete();
    }
}
