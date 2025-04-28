package level.tutorial;

import level.Level;
import level.tutorial.sequence.TutorialSequence;
import level.tutorial.sequence.event.TutorialEvent;
import render.AbstractRenderElement;
import unit.action.Action;
import unit.bot.BotActionData;

import java.awt.*;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Supplier;

public abstract class TutorialManager {
    public final static Color BLUE_HIGHLIGHT = new Color(60, 144, 227),
            RED_HIGHLIGHT = new Color(227, 60, 60),
            GREEN_HIGHLIGHT = new Color(102, 227, 60);
    private static boolean isTutorial = false;
    public static TutorialLevel level = null;
    public static final HashSet<TutorialElement> disabledElements = new HashSet<>(), permanentlyEnabledElements = new HashSet<>();
    public static final HashSet<Action> allowedActions = new HashSet<>();
    public static final HashSet<Point> selectableTiles = new HashSet<>();
    public static final HashMap<Action, HashSet<Point>> actionTiles = new HashMap<>();
    public static final HashMap<String, AbstractRenderElement> renderElements = new HashMap<>();
    public static final TutorialSequence sequence = new TutorialSequence();
    public static final ArrayDeque<Supplier<BotActionData>> forcedBotActions = new ArrayDeque<>();

    public static void startTutorial(TutorialLevel level) {
        TutorialManager.level = level;
        disabledElements.clear();
        permanentlyEnabledElements.clear();
        allowedActions.clear();
        selectableTiles.clear();
        clearActionTiles();
        renderElements.clear();
        isTutorial = true;
    }

    public static void clearActionTiles() {
        Action.forEach(a -> {
            actionTiles.put(a, new HashSet<>());
        });
    }

    public static void endTutorial() {
        level = null;
        isTutorial = false;
    }

    public static boolean isTutorial() {
        return isTutorial;
    }

    public static void createSequence(Level l) {
        sequence.setSequence(level.sequence.apply(l));
        sequence.start();
    }

    public static void incrementSequence() {
        sequence.incrementSequence();
    }

    public static void deleteSequence() {
        sequence.deleteSequence();
        renderElements.clear();
        forcedBotActions.clear();
    }

    public static void disableElement(Level l, TutorialElement element) {
        if (isEnabled(element) && !permanentlyEnabledElements.contains(element)) {
            disabledElements.add(element);
            element.onDisable.accept(l);
        }
    }

    public static void enableElement(Level l, TutorialElement element) {
        if (isDisabled(element)) {
            disabledElements.remove(element);
            element.onEnable.accept(l);
        }
    }

    public static void permanentlyEnableElement(Level l, TutorialElement element) {
        enableElement(l, element);
        permanentlyEnabledElements.add(element);
    }

    public static boolean isDisabled(TutorialElement element) {
        return isTutorial() && disabledElements.contains(element);
    }

    public static boolean isEnabled(TutorialElement element) {
        return !isDisabled(element);
    }

    public static void acceptEvent(TutorialEvent e) {
        if (isTutorial())
            sequence.acceptEvent(e);
    }

    public static boolean actionEnabled(Action action) {
        return !isTutorial() || (isEnabled(TutorialElement.ACTIONS) && allowedActions.contains(action));
    }

    public static boolean tileNotSelectable(Point pos) {
        return isTutorial() && (isDisabled(TutorialElement.TILE_SELECTION) || !(selectableTiles.contains(pos) || selectableTiles.isEmpty()));
    }

    public static boolean actionTileNotSelectable(Point pos, Action action) {
        return isTutorial() && (isDisabled(TutorialElement.ACTION_TILE_SELECTION) || !(actionTiles.get(action).contains(pos) || actionTiles.get(action).isEmpty()));
    }
}
