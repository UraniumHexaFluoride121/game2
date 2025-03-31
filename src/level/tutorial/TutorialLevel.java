package level.tutorial;

import foundation.NamedEnum;
import level.GameplaySettings;
import level.Level;
import level.PlayerTeam;
import level.tutorial.sequence.BoxSize;
import level.tutorial.sequence.action.*;
import level.tutorial.sequence.event.ActionListener;
import level.tutorial.sequence.event.TurnListener;
import level.tutorial.sequence.event.UnitSelectListener;
import render.Renderable;
import render.types.text.TextAlign;
import unit.UnitTeam;
import unit.action.Action;
import unit.type.FighterType;

import java.awt.*;
import java.util.function.Function;

import static level.tutorial.TutorialElement.*;
import static level.tutorial.TutorialManager.*;

public enum TutorialLevel implements NamedEnum {
    FIRING("Introduction", "tutorial-firing",
            "Learn the basics of controlling units.",
            new TutorialLevelData(
                    new GameplaySettings(false)
            ).addPlayer(PlayerTeam.A, false).addPlayer(PlayerTeam.B, true),
            l -> new TutorialSequenceElement[]{
                    ModifyElements.disable(l, TILE_SELECTION, CAMERA_MOVEMENT, ACTIONS, ACTION_TILE_SELECTION, ACTION_DESELECT, END_TURN, VIEW_FIRING_RANGE),
                    CameraMove.toTile(l, 3, 4),
                    ContinueTextBox.onMap(l, BoxSize.MEDIUM, 3, 4, TextAlign.CENTER,
                            "Welcome to the first tutorial. This is a turn-based strategy game, and your goal is to eliminate all enemy units."),

                    CameraMove.toTile(l, 1, 4),
                    TutorialHighlight.tile(l, BLUE_HIGHLIGHT, 1, 4),
                    ContinueTextBox.onMap(l, BoxSize.MEDIUM, 2, 4.7f, TextAlign.LEFT,
                            "This is your Base, which serves as the starting point for your units."),

                    CameraMove.toTile(l, 2, 4),
                    TutorialHighlight.tiles(l, BLUE_HIGHLIGHT, new Point(1, 5), new Point(2, 4)),
                    ContinueTextBox.onMap(l, BoxSize.MEDIUM, 1.6f, 6.4f, TextAlign.LEFT,
                            "These are your units. For this tutorial, you've been provided with two Fighter units."),

                    ModifyElements.enable(l, TILE_SELECTION),
                    TutorialHighlight.disable(l),
                    BlockingTextBox.onMap(l, BoxSize.SMALL, 1.5f, 6f, TextAlign.CENTER,
                            "Select one of the Fighters by left-clicking it.",
                            UnitSelectListener.ofTeam(UnitTeam.BLUE)),

                    ModifyElements.disable(l, TILE_SELECTION),
                    ContinueTextBox.onMap(l, BoxSize.MEDIUM, 1.6f, 6.4f, TextAlign.LEFT,
                            "Below the unit you can see two icons. These are the actions that the unit is able to perform."),
                    ContinueTextBox.onMap(l, BoxSize.MEDIUM_TALL, 1.6f, 6.4f, TextAlign.LEFT,
                            "The red one to the left is the Fire action, which is currently unavailable as there are no enemies in range.\n\nThis can be seen by hovering over the action."),
                    ModifyElements.enable(l, ACTIONS),
                    AllowedActions.only(Action.MOVE),
                    BlockingTextBox.onMap(l, BoxSize.MEDIUM, 1.6f, 6.4f, TextAlign.LEFT,
                            "The other action on the right is the Move action. Try selecting it now.",
                            ActionListener.select(Action.MOVE)),
                    ContinueTextBox.onMap(l, BoxSize.MEDIUM, 1.6f, 6.4f, TextAlign.LEFT,
                            "Highlighted in blue you can see all the tiles that the selected Fighter can be moved to."),

                    TutorialHighlight.tiles(l, RED_HIGHLIGHT, new Point(6, 4), new Point(6, 5)),
                    CameraMove.toTile(l, 6, 5),
                    ContinueTextBox.onMap(l, BoxSize.SMALL, 4, 5.5f, TextAlign.CENTER,
                            "These are the enemy units."),

                    TutorialHighlight.tiles(l, GREEN_HIGHLIGHT, new Point(5, 6)),
                    ModifyElements.enable(l, ACTION_TILE_SELECTION, TILE_SELECTION),
                    AllowedActionTiles.only(Action.MOVE, 5, 6),
                    BlockingTextBox.onMap(l, BoxSize.MEDIUM, 4, 6.8f, TextAlign.LEFT,
                            "Most units can only attack enemies on adjacent tiles. Left-click the highlighted tile to move the selected unit in range to attack.",
                            ActionListener.perform(Action.MOVE)),

                    TutorialHighlight.disable(l),
                    BlockingAction.waitFor(ActionListener.complete(Action.MOVE)),

                    ModifyElements.disable(l, TILE_SELECTION),
                    ContinueTextBox.onMap(l, BoxSize.MEDIUM_TALL, 2, 6, TextAlign.LEFT,
                            "You can see that the Move action is now greyed out.\n\nEach unit can only perform each of its actions once per turn."),

                    AllowedActions.only(Action.FIRE),
                    AllowedActionTiles.only(Action.FIRE, 6, 5),
                    BlockingTextBox.onMap(l, BoxSize.MEDIUM, 2, 6, TextAlign.LEFT,
                            "Now that the enemy unit is in range, select the Fire action, then select the unit to attack it.",
                            ActionListener.perform(Action.FIRE)),

                    BlockingAction.waitFor(ActionListener.complete(Action.FIRE)),
                    ModifyElements.enable(l, TILE_SELECTION),
                    AllowedTiles.only(5, 6),
                    TileSelect.tile(l, 5, 6),

                    TutorialUI.onMap(l, "unitHPHighlight")
                            .rectangle(5.18f, 6.01f, 0.8f, 1, GREEN_HIGHLIGHT, TutorialUI.StrokeWidth.NARROW)
                            .rectangle(6.18f, 5.01f, 0.8f, 1, GREEN_HIGHLIGHT, TutorialUI.StrokeWidth.NARROW),
                    ContinueTextBox.onMap(l, BoxSize.MEDIUM, 2, 6, TextAlign.LEFT,
                            "There is a number in the bottom right corner of each unit. This number shows the remaining HP of the unit, rounded up."),
                    ContinueTextBox.onMap(l, BoxSize.MEDIUM_TALL, 2, 6, TextAlign.LEFT,
                            "Fighter units start with " + ((int) (FighterType.FIGHTER.hitPoints)) + " HP, and, as you can see, both the attacking unit and " +
                                    "the enemy unit have taken some damage.\n\nNotice that the enemy unit took much more damage than the attacking one."),
                    ContinueTextBox.onMap(l, BoxSize.MEDIUM_TALL, 2, 6, TextAlign.LEFT,
                            "This is because after you attack a unit, the now weakened enemy will counterattack with whatever strength it has left.\n\n" +
                                    "The less HP a unit has, the less damage it will be able to do."),

                    TutorialUI.remove("unitHPHighlight"),
                    TutorialUI.onUI(l, "unitInfoHP")
                            .rectangle(6, 4.75f, 10, 1.4f, GREEN_HIGHLIGHT, TutorialUI.StrokeWidth.NARROW),
                    ContinueTextBox.onUI(l, BoxSize.MEDIUM, 20, 4, TextAlign.LEFT,
                            "You can also view the HP of the selected unit on the panel to the left, this time with one decimal place of precision."),

                    TutorialUI.remove("unitInfoHP"),

                    ModifyElements.enable(l, CAMERA_MOVEMENT, ACTIONS, TILE_SELECTION),
                    AllowedActions.only(Action.MOVE),
                    AllowedTiles.all(),
                    AllowedActionTiles.only(Action.MOVE, new Point(5, 5), new Point(5, 4)),
                    TutorialHighlight.tiles(l, GREEN_HIGHLIGHT, new Point(5, 5), new Point(5, 4)),
                    SequenceTextBox.onMap(l, BoxSize.MEDIUM_TALL, 2, 6, TextAlign.LEFT,
                            "Move the other Fighter to the one of the highlighted tiles and attack the enemy.\n\nTo move the camera, use right-click + drag, or move the mouse to the edge of the screen.",
                            BlockingAction.waitFor(ActionListener.complete(Action.MOVE)),

                            TutorialHighlight.disable(l),
                            AllowedActions.only(Action.FIRE),
                            AllowedActionTiles.all(),
                            BlockingAction.waitFor(ActionListener.complete(Action.FIRE))
                    ),
                    TutorialUI.onUI(l, "endTurnHighlight")
                            .rectangle(8f, Renderable.top() - 3, 10, 3, GREEN_HIGHLIGHT, TutorialUI.StrokeWidth.NORMAL),
                    ModifyElements.enable(l, END_TURN, ACTION_DESELECT),
                    BlockingTextBox.onUI(l, BoxSize.MEDIUM_TALL, 7, Renderable.top() - 10, TextAlign.LEFT,
                            "Now that you've performed all the actions you can, it's time to end this turn.\n\nThe enemy will now attack, but by attacking first, you've gained the upper hand.",
                            TurnListener.start()),

                    BlockingAction.waitFor(TurnListener.start(UnitTeam.BLUE)),

                    TutorialUI.remove("endTurnHighlight"),
                    AllowedActions.all(),
                    BlockingTextBox.onMap(l, BoxSize.MEDIUM, 2, 6, TextAlign.LEFT,
                            "Use what you've learnt to destroy the remaining enemy units.",
                            ActionListener.perform()),

                    new EndTutorial()
            });

    public final String displayName, saveFileName, description;
    public final TutorialLevelData levelData;
    public final Function<Level, TutorialSequenceElement[]> sequence;

    TutorialLevel(String displayName, String saveFileName, String description, TutorialLevelData levelData, Function<Level, TutorialSequenceElement[]> sequence) {
        this.displayName = displayName;
        this.saveFileName = saveFileName;
        this.description = description;
        this.levelData = levelData;
        this.sequence = sequence;
    }

    @Override
    public String getName() {
        return displayName;
    }
}
