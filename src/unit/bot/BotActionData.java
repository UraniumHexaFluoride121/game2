package unit.bot;

import level.Level;
import unit.Unit;
import unit.action.Action;

import java.awt.*;
import java.util.function.Supplier;

public class BotActionData {
    public final Action action, secondaryAction;
    public final Point target, secondaryTarget;
    public final Unit unit;
    public final float value;

    public static Supplier<BotActionData> move(Level l, int fromX, int fromY, int toX, int toY) {
        return () -> new BotActionData(Action.MOVE, null, new Point(toX, toY), null, l.getUnit(new Point(fromX, fromY)), 10);
    }

    public static Supplier<BotActionData> capture(Level l, int x, int y) {
        return () -> new BotActionData(Action.CAPTURE, null, null, null, l.getUnit(new Point(x, y)), 10);
    }

    public static Supplier<BotActionData> end() {
        return () -> null;
    }

    public BotActionData(Action action, Action secondaryAction, Point target, Point secondaryTarget, Unit unit, float value) {
        this.action = action;
        this.secondaryAction = secondaryAction;
        this.target = target;
        this.secondaryTarget = secondaryTarget;
        this.unit = unit;
        this.value = value;
    }
}
