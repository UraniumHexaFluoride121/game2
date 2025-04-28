package level.tutorial;

import foundation.NamedEnum;
import level.GameplaySettings;
import level.Level;
import level.PlayerTeam;
import level.structure.StructureType;
import level.tutorial.sequence.BoxSize;
import level.tutorial.sequence.action.*;
import level.tutorial.sequence.event.*;
import render.Renderable;
import render.level.info.UITileInfo;
import render.types.text.TextAlign;
import unit.UnitTeam;
import unit.action.Action;
import unit.bot.BotActionData;
import unit.type.CorvetteType;
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
                    ModifyElements.disableAll(l),
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

                    ModifyElements.enable(l, CAMERA_MOVEMENT, ACTIONS, TILE_SELECTION, TILE_DESELECTION),
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
            }),
    FOW("Tile Types", "tutorial-tile-types",
            "This tutorial covers the fog of war mechanic, tile types and capturing structures.",
            new TutorialLevelData(
                    new GameplaySettings(true)
            ).addPlayer(PlayerTeam.A, false).addPlayer(PlayerTeam.B, true),
            l -> new TutorialSequenceElement[]{
                    ModifyElements.disable(l, TILE_DESELECTION, TILE_SELECTION, CAMERA_MOVEMENT, ACTIONS, ACTION_DESELECT, ACTION_TILE_SELECTION, END_TURN, VIEW_FIRING_RANGE, VIEW_EFFECTIVENESS),
                    ContinueTextBox.onMap(l, BoxSize.MEDIUM, -3, 5, TextAlign.LEFT,
                            "Welcome to the second tutorial, about fog of war, tile types, and action costs."),
                    ContinueTextBox.onMap(l, BoxSize.MEDIUM_TALL, -3, 5, TextAlign.LEFT,
                            "As you can quite clearly see, many of the tiles are greyed out this time. That's because of the fog of war setting, which is enabled by default.\n\nExpect to see it in most future tutorials."),

                    TutorialHighlight.visibleTiles(l, GREEN_HIGHLIGHT),
                    ContinueTextBox.onMap(l, BoxSize.MEDIUM_TALL, -3, 5, TextAlign.LEFT,
                            "The highlighted tiles are the ones that are visible. Each unit has a view range in which tiles are visible.\n\nTiles not visible will hide the positions of enemy units."),

                    TutorialHighlight.fowTiles(l, RED_HIGHLIGHT),
                    ContinueTextBox.onMap(l, BoxSize.MEDIUM_TALL, -3, 5, TextAlign.LEFT,
                            "That means that there could be enemy units on any of these tiles that aren't visible.\n\nBefore you can attack, you'll need to find the enemy units."),

                    TutorialHighlight.radius(l, RED_HIGHLIGHT, 8, 3, 1),
                    CameraMove.toTile(l, 8, 3),
                    ContinueTextBox.onMap(l, BoxSize.LARGE, 5, 5, TextAlign.LEFT,
                            "We do, however, know where the enemy base is, as structures are always visible regardless of fog of war.\n\n" +
                                    "We also know that units spawn close to the base, so we can expect the enemy units to be somewhere in the highlighted area."),

                    TutorialHighlight.disable(l),
                    CameraMove.toTile(l, 3, 3),
                    ContinueTextBox.onMap(l, BoxSize.LARGE, 2, 1, TextAlign.LEFT,
                            "As you've probably noticed, not all tiles are the same. Different tile types have different properties, one of them being visibility, " +
                                    "which affects a unit's ability to see through the tile.\n\nFor example, a unit that can see through 3 empty tiles might only be able to see through 2 nebula tiles."),

                    ModifyElements.enable(l, TILE_SELECTION),
                    AllowedTiles.only(4, 3),
                    TutorialHighlight.tile(l, BLUE_HIGHLIGHT, 4, 3),
                    BlockingTextBox.onMap(l, BoxSize.MEDIUM, 2, 1.5f, TextAlign.LEFT,
                            "Select the highlighted tile to see more info about it.",
                            TileSelectListener.any()),

                    TutorialUI.onUI(l, "tileName")
                            .rectangle(Renderable.right() - 8f, 17.65f, 15, 1.5f, GREEN_HIGHLIGHT, TutorialUI.StrokeWidth.NARROW),
                    ContinueTextBox.onUI(l, BoxSize.MEDIUM, Renderable.right() - 23, 16, TextAlign.LEFT,
                            "Here you can see the type of tile that is selected, in this case a nebula tile."),
                    TutorialUI.remove("tileName"),

                    TutorialUI.onUI(l, "tileVisibility")
                            .rectangle(Renderable.right() - 8f, 3 * UITileInfo.BAR_SPACING + UITileInfo.INITIAL_BAR_POS + 0.1f, 13.5f, 1.7f, GREEN_HIGHLIGHT, TutorialUI.StrokeWidth.NARROW),
                    ContinueTextBox.onUI(l, BoxSize.LARGE, Renderable.right() - 25, 3, TextAlign.LEFT,
                            "This bar shows the visibility of the tile type.\n\nLow visibility is good for keeping your units hidden, but remember that it's not just " +
                                    "about the tile the unit is on, but also which tiles lie between you and your enemy that matter for staying hidden."),
                    TutorialUI.remove("tileVisibility"),

                    TutorialUI.onUI(l, "tileDefence")
                            .rectangle(Renderable.right() - 8f, 2 * UITileInfo.BAR_SPACING + UITileInfo.INITIAL_BAR_POS + 0.1f, 13.5f, 1.7f, GREEN_HIGHLIGHT, TutorialUI.StrokeWidth.NARROW),
                    ContinueTextBox.onUI(l, BoxSize.LARGE, Renderable.right() - 25, 3, TextAlign.LEFT,
                            "Here you can see the defence bonus that units receive when on this tile. The defence bonus is a " +
                                    "damage reduction when being attacked or counterattacked.\n\nIt is therefore ideal to always be attacking " +
                                    "from tiles with high defence to reduce the damage of the subsequent counterattack, and to reduce the damage when it's the " +
                                    "enemy's turn to retaliate."),
                    TutorialUI.remove("tileDefence"),

                    TutorialUI.onUI(l, "tileMovement")
                            .rectangle(Renderable.right() - 8f, 1 * UITileInfo.BAR_SPACING + UITileInfo.INITIAL_BAR_POS + 0.1f, 13.5f, 1.7f, GREEN_HIGHLIGHT, TutorialUI.StrokeWidth.NARROW),
                    ContinueTextBox.onUI(l, BoxSize.LARGE, Renderable.right() - 25, 3, TextAlign.LEFT,
                            "This one represents the ease with which units can move through the tile. Moving a unit through tiles with low movement " +
                                    "will reduce the distance the unit can travel, and also increase the cost to move the unit (more on that later).\n\nHigh movement " +
                                    "tiles are ideal when moving units across long distances."),
                    TutorialUI.remove("tileMovement"),

                    ContinueTextBox.onUI(l, BoxSize.MEDIUM, Renderable.right() - 25, 8, TextAlign.LEFT,
                            "As you can see, nebula tiles are good for hiding, while also having a small defence bonus."),

                    TutorialHighlight.tile(l, BLUE_HIGHLIGHT, 3, 3),
                    AllowedTiles.only(3, 3),
                    TileSelect.tile(l, 3, 3),
                    ContinueTextBox.onUI(l, BoxSize.MEDIUM, Renderable.right() - 25, 8, TextAlign.LEFT,
                            "This empty tile right next to it is almost the opposite. With no defence and high visibility, its main advantage is its low movement cost."),

                    TutorialHighlight.tile(l, BLUE_HIGHLIGHT, 4, 2),
                    AllowedTiles.only(4, 2),
                    TileSelect.tile(l, 4, 2),
                    ContinueTextBox.onMap(l, BoxSize.LARGE, 0, 1, TextAlign.LEFT,
                            "For the asteroid field tile, while it does have a slightly lower visibility than an empty tile, its main advantage is its high defence bonus.\n\n" +
                                    "While small units like the Fighters in this tutorial can move through asteroid fields without much difficulty, larger units either can't move through them at all, or can only move one tile at a time."),

                    TutorialHighlight.tile(l, BLUE_HIGHLIGHT, 5, 5),
                    AllowedTiles.only(5, 5),
                    TileSelect.tile(l, 5, 5),
                    CameraMove.toTile(l, 5, 5),
                    ContinueTextBox.onMap(l, BoxSize.LARGE, 5, 2, TextAlign.LEFT,
                            "Lastly, the dense nebula tile. These appear scattered throughout regular nebula tiles, and have a defence bonus somewhere between a regular nebula and an asteroid field.\n\n" +
                                    "Most importantly, they have a special property. No matter how good the view distance is for a given unit, the only way for it to see inside a dense nebula is if the unit is directly adjacent to it."),

                    ContinueTextBox.onMap(l, BoxSize.MEDIUM_TALL, 5, 3, TextAlign.LEFT,
                            "We can use this property to our advantage in this situation. By moving a Fighter to this tile, it'll reveal the enemy positions without risking detection, preventing the enemy from attacking first on the next turn."),
                    SequenceTextBox.onMap(l, BoxSize.SMALL, 5, 4, TextAlign.LEFT,
                            "Move one of the fighters to the dense nebula tile.",
                            TutorialHighlight.tile(l, GREEN_HIGHLIGHT, 5, 5),
                            ModifyElements.actionCameraNoDeselect(l),
                            TileSelect.deselect(l),
                            AllowedTiles.all(),
                            AllowedActions.all(),
                            AllowedActionTiles.only(Action.MOVE, 5, 5),
                            BlockingAction.waitFor(ActionListener.perform(Action.MOVE)),
                            TutorialHighlight.disable(l)
                    ),
                    BlockingAction.waitFor(ActionListener.complete()),
                    ModifyElements.disableAll(l),

                    ContinueTextBox.onMap(l, BoxSize.MEDIUM_TALL, 5, 3, TextAlign.LEFT,
                            "Enemy spotted!\n\nWhile you could attack the enemy with your other Fighter, it'll only lead to the enemy retaliating two on one next turn."),

                    ModifyElements.actionCameraNoDeselect(l),
                    TutorialHighlight.tile(l, GREEN_HIGHLIGHT, 4, 6),
                    AllowedActionTiles.only(Action.MOVE, 4, 6),
                    BlockingTextBox.onMap(l, BoxSize.MEDIUM_TALL, 5, 3, TextAlign.LEFT,
                            "Instead, move the other Fighter to the highlighted tile, away from the enemy.\n\nWe'll wait until next turn to attack.",
                            ActionListener.perform(Action.MOVE)),

                    TutorialHighlight.disable(l),

                    BlockingAction.waitFor(ActionListener.complete()),
                    TileSelect.deselect(l),
                    ModifyElements.endTurn(l),

                    BlockingTextBox.onMap(l, BoxSize.MEDIUM, 0, 7, TextAlign.LEFT,
                            "Great! Now that both Fighters are outside of the enemy's view range, we can end the turn.",
                            TurnListener.start()),

                    BotActions.addActions(
                            BotActionData.move(l, 8, 4, 3, 4),
                            BotActionData.move(l, 7, 3, 1, 4),
                            BotActionData.capture(l, 1, 4),
                            BotActionData.end()
                    ),
                    BlockingAction.waitFor(TurnListener.start(UnitTeam.BLUE)),
                    BlockingAction.waitFor(AnimStateListener.ended()),

                    ContinueTextBox.onMap(l, BoxSize.MEDIUM, 0, 5, TextAlign.LEFT,
                            "The enemy could not find the Fighter units, and decided to start capturing the base instead!"),
                    ContinueTextBox.onMap(l, BoxSize.MEDIUM_TALL, 0, 5, TextAlign.LEFT,
                            "Most units can capture by simply being on the same tile as an enemy structure. Capturing takes multiple turns, as shown by the progress bar above the enemy unit that's currently capturing."),
                    ContinueTextBox.onMap(l, BoxSize.LARGE, 0, 5, TextAlign.LEFT,
                            "Once the progress bar fills, the enemy gains control over the structure, or the structure is destroyed, depending on the type of structure.\n\n" +
                                    "As for the base structure, not only is it destroyed, the player gets eliminated if it is successfully captured.\n\n" +
                                    "You must stop the capture of your base at any cost!"),
                    ContinueTextBox.onMap(l, BoxSize.MEDIUM_TALL, 0, 5, TextAlign.LEFT,
                            "To stop a capture, simply attack the capturing unit.\n\n" +
                                    "Each time the capturing unit is attacked, or if it itself performs an attack, the capture progress bar is reduced by one turn."),

                    TutorialHighlight.tile(l, GREEN_HIGHLIGHT, 0, 4),
                    CameraMove.toTile(l, 0, 4),
                    ContinueTextBox.onMap(l, BoxSize.MEDIUM, 1, 7, TextAlign.LEFT,
                            "Move a Fighter unit here to attack the capturing unit. By attacking from an asteroid field, you'll also receive less damage when counterattacked."),

                    ModifyElements.actionCameraNoDeselect(l),
                    AllowedActionTiles.only(Action.MOVE, 0, 4),
                    CameraMove.toTile(l, 5, 5),
                    TutorialHighlight.tiles(l, GREEN_HIGHLIGHT, new Point(0, 4), new Point(5, 5)),

                    BlockingTextBox.onMap(l, BoxSize.MEDIUM, 5, 6.3f, TextAlign.LEFT,
                            "Select this unit and move it to the highlighted asteroid field tile.",
                            ActionListener.select()),

                    CameraMove.toTile(l, 0, 4),
                    ModifyElements.disableAll(l),
                    ModifyElements.enable(l, ACTION_DESELECT),
                    TutorialHighlight.disable(l),

                    BlockingTextBox.onMap(l, BoxSize.LARGE, -3, 6, TextAlign.LEFT,
                            "You can see that the tile is just out of range.\n\n" +
                                    "After selecting an action, you can go back by using the Exit Action button below the End Turn button, or by using the Escape key, but remember that you cannot undo actions that have been completed.\n\n" +
                                    "Exit the move action so you can select the other Fighter that is in range.",
                            ActionListener.deselect()),

                    ModifyElements.action(l, true),
                    TutorialHighlight.tile(l, GREEN_HIGHLIGHT, 0, 4),

                    BlockingTextBox.onMap(l, BoxSize.MEDIUM, 1, 7, TextAlign.LEFT,
                            "Move the other Fighter to the highlighted asteroid field tile.",
                            ActionListener.perform()),

                    TutorialHighlight.disable(l),
                    AllowedTiles.only(0, 4),

                    BlockingAction.waitFor(ActionListener.complete()),
                    BlockingTextBox.onMap(l, BoxSize.SMALL, 1, 6, TextAlign.LEFT,
                            "Attack the capturing enemy unit.",
                            ActionListener.perform()),

                    BlockingAction.waitFor(ActionListener.complete()),
                    ContinueTextBox.onMap(l, BoxSize.MEDIUM, 1, 6, TextAlign.LEFT,
                            "You can see that the capture progress went down after the attack."),

                    CameraMove.toTile(l, 5, 5),
                    AllowedTiles.only(5, 5),
                    AllowedActionTiles.only(Action.MOVE, 8, 3),
                    TutorialHighlight.tile(l, GREEN_HIGHLIGHT, 8, 3),

                    BlockingTextBox.onMap(l, BoxSize.MEDIUM, 5, 6.3f, TextAlign.LEFT,
                            "It may not be the best move, but for the sake of the tutorial, move the remaining Fighter unit to the enemy base to begin capturing it.",
                            ActionListener.perform()),
                    TutorialHighlight.disable(l),
                    BlockingAction.waitFor(ActionListener.complete()),
                    ModifyElements.action(l, true),
                    AllowedTiles.only(8, 3),
                    AllowedActions.only(Action.CAPTURE),


                    BlockingTextBox.onMap(l, BoxSize.MEDIUM, 5, 4, TextAlign.LEFT,
                            "Select the unit, and capture. The capture action appears only when the unit is on an enemy structure.",
                            ActionListener.perform()),
                    ModifyElements.disableAll(l),


                    ContinueTextBox.onMap(l, BoxSize.LARGE, 5, 4, TextAlign.LEFT,
                            "It takes " + StructureType.BASE.captureSteps + " turns to capture a base, and on each of those turns you have to perform the capture action; it is not automatic. " +
                                    "Moving the capturing unit away from the structure will reset the progress.\n\n" +
                                    "Players always have visibility over tiles with allied structures, meaning that the enemy can see the capture progress of their structures, even if there are no units nearby with view range over the area."),
                    ContinueTextBox.onMap(l, BoxSize.MEDIUM, 5, 4, TextAlign.LEFT,
                            "It is likely that the enemy will come after your capturing unit. It's up to you now to win the game."),

                    AllowedActions.all(),
                    ModifyElements.action(l, true),
                    ModifyElements.enable(l, END_TURN),
                    AllowedTiles.all(),
                    AllowedActionTiles.all(),

                    new EndTutorial()
            }),
    WEAPONS("Ship Classes", "tutorial-weapons",
            "This tutorial introduces different ship classes, weapon types as well as ranged units.",
            new TutorialLevelData(
                    new GameplaySettings(true)
            ).addPlayer(PlayerTeam.A, false).addPlayer(PlayerTeam.B, true),
            l -> new TutorialSequenceElement[]{
                    ModifyElements.disableAll(l),
                    CameraMove.toTile(l, 7, 1),
                    ContinueTextBox.onUI(l, BoxSize.LARGE, 11, 18, TextAlign.LEFT,
                            "Welcome to the third tutorial. So far, the only units we've come across have been Fighter units. As you can see, this is no longer the case.\n\n" +
                                    "Each ship belongs to a ship class. There are four different classes: fighters, corvettes, cruisers and capital ships."),
                    TutorialHighlight.tile(l, BLUE_HIGHLIGHT, 7, 2),
                    ContinueTextBox.onUI(l, BoxSize.MEDIUM, 11, 18, TextAlign.LEFT,
                            "This Fighter unit is, of course, a fighter-class unit, but it is not the only one of its kind."),
                    TutorialHighlight.tiles(l, BLUE_HIGHLIGHT, new Point(6, 1), new Point(7, 1)),
                    ContinueTextBox.onUI(l, BoxSize.MEDIUM_TALL, 11, 18, TextAlign.LEFT,
                            "These are Bomber units. They are also fighter-class units, but are slightly weaker and slower than the Fighter unit is.\n\n" +
                                    "They do, however, have a powerful missile weapon that we'll get to later."),
                    TutorialHighlight.tile(l, BLUE_HIGHLIGHT, 6, 0),
                    ModifyElements.tileSelect(l, false),
                    AllowedTiles.only(6, 0),
                    BlockingTextBox.onUI(l, BoxSize.MEDIUM, 11, 18, TextAlign.LEFT,
                            "Finally, this is an Artillery unit. It is a ranged corvette-class unit. Start by selecting this unit.",
                            TileSelectListener.any()),
                    TutorialHighlight.disable(l),
                    TutorialUI.onUI(l, "viewRange").rectangle(8.25f, 12.25f, 2, 2, GREEN_HIGHLIGHT, TutorialUI.StrokeWidth.NARROW),
                    ModifyElements.viewFiringRange(l, true),
                    BlockingTextBox.onUI(l, BoxSize.MEDIUM, 11, 18, TextAlign.LEFT,
                            "To see the range that this unit has, click the red button highlighted on the panel below.",
                            UIListener.select(UIElement.VIEW_FIRING_RANGE)),
                    TutorialUI.remove("viewRange"),
                    BlockingTextBox.onUI(l, BoxSize.MEDIUM, 11, 18, TextAlign.LEFT,
                            "Highlighted in red you can see the tiles that the selected unit is in firing range of. Click anywhere on the screen to exit firing range view.",
                            UIListener.deselect(UIElement.VIEW_FIRING_RANGE)),
                    ModifyElements.disableAll(l),
                    CameraMove.toTile(l, 5, 3),
                    ContinueTextBox.onMap(l, BoxSize.MEDIUM_TALL, 8, 4, TextAlign.LEFT,
                            "Now with that out of the way, we can get to moving the units.\n\nYou can see that the enemy base is in the top left corner of the map, outside of our view range."),
                    TutorialHighlight.radius(l, RED_HIGHLIGHT, 5, 4, 1),
                    ModifyElements.enable(l, TILE_DESELECTION),
                    TileSelect.deselect(l),
                    ContinueTextBox.onMap(l, BoxSize.MEDIUM_TALL, 8, 4, TextAlign.LEFT,
                            "The enemy will most likely move their units to this region on the next turn. We could hide our units in the nebula below this region to ambush them."),
                    ModifyElements.moveUnit(l, false, 7, 2, 5, 2),
                    BlockingTextBox.onMap(l, BoxSize.MEDIUM_TALL, 8, 4, TextAlign.LEFT,
                            "Move the Fighter unit to the front of the nebula. It has the longest view range, and by moving it to the front, we can see further out of the nebula.",
                            ActionListener.perform()),
                    TutorialHighlight.disable(l),
                    BlockingAction.waitFor(ActionListener.complete()),
                    TileSelect.deselect(l),
                    ModifyElements.moveUnit(l, false, 6, 0, 5, 0),
                    BlockingTextBox.onMap(l, BoxSize.MEDIUM, 8, 4, TextAlign.LEFT,
                            "The Artillery unit, due to its longer range, can be placed further back. Move it to the highlighted tile.",
                            ActionListener.perform()),
                    TutorialHighlight.disable(l),
                    BlockingAction.waitFor(ActionListener.complete()),
                    TileSelect.deselect(l),
                    ModifyElements.moveUnit(l, false, new Point[]{new Point(6, 1), new Point(7, 1)}, new Point[]{new Point(4, 1), new Point(4, 0)}),
                    BlockingTextBox.onMap(l, BoxSize.MEDIUM, 1, 0, TextAlign.LEFT,
                            "Finally, move the Bomber units to the highlighted positions.",
                            ActionListener.perform(), ActionListener.perform()),
                    TutorialHighlight.disable(l),
                    BlockingAction.waitFor(ActionListener.complete()),
                    TileSelect.deselect(l),
                    ModifyElements.endTurn(l),
                    BlockingTextBox.onMap(l, BoxSize.SMALL_MEDIUM, 1, 3, TextAlign.LEFT,
                            "All the units have been moved, you can now end the turn.",
                            TurnListener.start()),
                    ModifyElements.disableAll(l),
                    BotActions.addActions(
                            BotActionData.move(l, 3, 4, 7, 1),
                            BotActionData.move(l, 3, 5, 6, 3),
                            BotActionData.move(l, 2, 4, 5, 4),
                            BotActionData.end()
                    ),
                    BlockingAction.waitFor(TurnListener.start()),
                    BlockingAction.waitFor(AnimStateListener.ended()),
                    CameraMove.toTile(l, 6, 3),
                    TutorialHighlight.tiles(l, RED_HIGHLIGHT, new Point(6, 3), new Point(5, 4)),
                    ContinueTextBox.onMap(l, BoxSize.MEDIUM_TALL, 8, 4, TextAlign.LEFT,
                            "The enemy has deployed Cruisers against us!\n\n" +
                                    "Cruiser units are the base configuration of the cruiser class, in the same way that Fighter units are the basic variant of the fighter class."),
                    ModifyElements.tileSelect(l, false, 5, 2),
                    BlockingTextBox.onMap(l, BoxSize.MEDIUM, 8, 4, TextAlign.LEFT,
                            "Select the Fighter unit to see what kind damage we can do with it.",
                            TileSelectListener.any()),
                    TutorialHighlight.disable(l),
                    ModifyElements.viewEffectiveness(l, false),
                    TutorialUI.onUI(l, "viewEffectiveness").rectangle(6.25f, 12.25f, 2, 2, GREEN_HIGHLIGHT, TutorialUI.StrokeWidth.NARROW),
                    BlockingTextBox.onUI(l, BoxSize.MEDIUM, 11, 18, TextAlign.LEFT,
                            "To see the effectiveness of the weapons on this unit, click the orange button highlighted below.",
                            UIListener.select(UIElement.VIEW_EFFECTIVENESS)),
                    TutorialUI.remove("viewEffectiveness"),
                    ContinueTextBox.onMap(l, BoxSize.LARGE, 8, 4, TextAlign.LEFT,
                            "Each enemy now has its tile coloured based on weapon effectiveness. This does not always correspond to damage dealt, as it does not take into account unit HP.\n\n" +
                                    "As you know, lower HP means lower damage dealt, but that does not change the base characteristics of the weapons, which is what you're seeing now."),
                    ContinueTextBox.onMap(l, BoxSize.LARGE, 8, 4, TextAlign.LEFT,
                            "It does, however, give us a rough estimate of the damage dealt by a full HP unit, which all of our units currently are.\n\n" +
                                    "Yellow means low damage (usually around 1 HP), while red means high damage (often more than half a unit's HP). Blue, not seen here, means no damage at all."),
                    ContinueTextBox.onMap(l, BoxSize.LARGE, 8, 4, TextAlign.LEFT,
                            "You can see by the orange colour that the Fighter can do moderate damage to the enemy Fighter which is next to the base.\n\n" +
                                    "When it comes to the Cruisers however, the yellow colour shows that this unit is almost useless against them."),
                    ModifyElements.tileSelect(l, false, new Point(4, 1), new Point(4, 0)),
                    ModifyElements.viewEffectiveness(l, true).add(false),
                    BlockingTextBox.onMap(l, BoxSize.MEDIUM, 8, 4, TextAlign.LEFT,
                            "Select one of the Bomber units instead to see what damage they can do.", TileSelectListener.any()),
                    TutorialHighlight.disable(l),
                    ModifyElements.viewEffectiveness(l, false),
                    BlockingTextBox.onMap(l, BoxSize.SMALL_MEDIUM, 8, 4, TextAlign.LEFT,
                            "Click the orange button to view weapon effectiveness.", UIListener.select(UIElement.VIEW_EFFECTIVENESS)),
                    ContinueTextBox.onMap(l, BoxSize.LARGE, 8, 4, TextAlign.LEFT,
                            "As you can see, Bombers perform well not only against the Fighter unit, but also against the Cruisers.\n\n" +
                                    "This is because they have two weapons. The first is a plasma gun, effective at destroying fighter-class units. The Fighter we looked " +
                                    "at before has similar gun, which is why both that unit and this unit are effective at destroying the enemy Fighter."),
                    ContinueTextBox.onMap(l, BoxSize.MEDIUM_TALL, 8, 4, TextAlign.LEFT,
                            "The Bomber unit also has a missile weapon, which is most powerful when used against capital ships.\n\n" +
                                    "It does, however, still perform well against cruiser-class ships."),
                    ContinueTextBox.onMap(l, BoxSize.EXTRA_LARGE, 8, 4, TextAlign.LEFT,
                            "Each of the four unit classes has a corresponding weapon type that counters it. Here's a list showing which weapon type counters which unit class, in order of unit size:\n\n" +
                                    "Plasma > Fighter-class units\n\n" +
                                    "Cannon > Corvette-class units\n\n" +
                                    "Rail Gun > Cruiser-class units\n\n" +
                                    "Explosive > Capital ships\n\n"),
                    ContinueTextBox.onMap(l, BoxSize.LARGE, 8, 4, TextAlign.LEFT,
                            "Furthermore, weapons often (but not always) perform moderately well against " +
                                    "enemies one size class up or down from the class that the weapon is most effective against.\n\n" +
                                    "This is why the Bombers' missile weapons can do damage against cruiser-class units, even though explosives " +
                                    "are meant for countering capital ships."),
                    ContinueTextBox.onMap(l, BoxSize.EXTRA_LARGE, 8, 4, TextAlign.LEFT,
                            "Keep in mind that not all weapons of a given type are created equal. Some are stronger than others.\n\n" +
                                    "A weak plasma gun might be moderately effective against fighters, and useless against other classes\n\nA strong " +
                                    "plasma gun, on the other hand, may be highly effective against fighter-class units, moderately effective against the corvette class, " +
                                    "and useless against any unit larger than a corvette."),
                    ContinueTextBox.onMap(l, BoxSize.LARGE, 8, 4, TextAlign.LEFT,
                            "You don't necessarily have to remember the exact details of the weapon each unit carries, what's important is that " +
                                    "that each unit has one, or sometimes several classes it counters effectively, and possibly has other classes that it's moderately effective against.\n\n" +
                                    "You can, and should, use the orange button to view weapon effectiveness."),
                    ModifyElements.tileSelect(l, false, new Point(6, 3), new Point(5, 4)),
                    ModifyElements.viewEffectiveness(l, true).add(false),
                    SequenceTextBox.onMap(l, BoxSize.LARGE, 8, 4, TextAlign.LEFT,
                            "Now, we know that the Bomber is effective against the Cruisers, but we should also check whether the Cruisers are effective " +
                                    "against the Bombers before going to attack.\n\nSelect one of the Cruisers and view its weapon effectiveness.",
                            BlockingAction.waitFor(TileSelectListener.any()),
                            TutorialHighlight.disable(l),
                            BlockingAction.waitFor(UIListener.select(UIElement.VIEW_EFFECTIVENESS))
                    ),
                    ModifyElements.viewEffectiveness(l, false),
                    ContinueTextBox.onMap(l, BoxSize.MEDIUM, 8, 4, TextAlign.LEFT,
                            "We see that the enemy Cruiser is not effective against our fighter-class units, meaning that the Bombers can attack without taking much damage in return."),

                    ModifyElements.moveUnit(l, true, new Point[]{new Point(4, 0), new Point(4, 1)}, new Point[]{new Point(7, 3)}),
                    ModifyElements.viewEffectiveness(l, true).add(false),
                    SequenceTextBox.onMap(l, BoxSize.MEDIUM, 8, 5, TextAlign.LEFT,
                            "Move a Bomber to this asteroid field and attack.",
                            TutorialHighlight.disable(l),
                            BlockingAction.waitFor(ActionListener.complete()),
                            ModifyElements.forceTileSelect(l, 7, 3),
                            ModifyElements.attack(l, true, 7, 3),
                            BlockingAction.waitFor(ActionListener.perform())
                    ),
                    BlockingAction.waitFor(ActionListener.complete()),
                    CameraMove.toTile(l, 7, 3),
                    ModifyElements.viewEffectiveness(l, false).add(false),
                    BlockingTextBox.onMap(l, BoxSize.LARGE, 8, 4, TextAlign.LEFT,
                            "You can see that the enemy Cruiser took significantly more damage than the Bomber, which came out almost unscathed.\n\n" +
                                    "The Bomber's missile weapon does, however, have one major drawback.\n\n" +
                                    "Select the Bomber, then select the button to view the Bomber's weapon effectiveness.", UIListener.select(UIElement.VIEW_EFFECTIVENESS)),
                    ContinueTextBox.onMap(l, BoxSize.EXTRA_LARGE, 5, 0, TextAlign.LEFT,
                            "You can see that the Cruisers are now coloured yellow, meaning that the Bomber is no longer effective against them.\n\n" +
                                    "This is due to the missile weapon having limited ammo capacity, just one round of ammo in this case.\n\n" +
                                    "Since the missile weapon is out of ammo, attacking the Cruisers now will lead to the Bomber resorting to using its plasma gun, which is much less effective."),
                    TutorialUI.onUI(l, "ammoLabel")
                            .rectangle(1 + 9.4f / 2 + 0.5f / 2, 5 - 1 - 0.5f / 2, 9.4f + 0.5f, 1 + 0.5f, GREEN_HIGHLIGHT, TutorialUI.StrokeWidth.NARROW),
                    ContinueTextBox.onMap(l, BoxSize.EXTRA_LARGE, 5, 0, TextAlign.LEFT,
                            "The remaining ammo can be seen on the panel to the left, where you can see that the ammo is at 0 / 1.\n\n" +
                                    "Units can have at most one weapon that uses ammo, and using the unit's other weapons will not consume any ammo.\n\n" +
                                    "If none of the weapons for a unit consume ammo, the ammo counter will display \"--\" instead."),
                    TutorialUI.remove("ammoLabel"),
                    TutorialUI.onUI(l, "unitInfo").rectangle(10.25f, 12.25f, 2, 2, GREEN_HIGHLIGHT, TutorialUI.StrokeWidth.NARROW),
                    ContinueTextBox.onMap(l, BoxSize.EXTRA_LARGE, 5, 0, TextAlign.LEFT,
                            "When attacking an enemy, the most effective weapon not out of ammo will automatically be selected. Attacking a Fighter using " +
                                    "a Bomber, for example, will use the plasma gun, even if the missile is available.\n\n" +
                                    "To view the weapons a unit has, open the unit info screen using the button in the top right of the panel to the left. " +
                                    "Then, navigate to the weapons tab, and click a weapon to view info about it. There, you can see the range, ammo usage, and effectiveness of " +
                                    "each weapon."),
                    TutorialUI.remove("unitInfo"),
                    ModifyElements.viewEffectiveness(l, true).add(true),
                    ModifyElements.moveUnit(l, true, new Point[]{new Point(4, 0), new Point(4, 1)}, new Point[]{new Point(4, 3)}),
                    CameraMove.toTile(l, 5, 3),
                    SequenceTextBox.onMap(l, BoxSize.LARGE, 8, 4, TextAlign.LEFT,
                            "That info screen also contains many other useful things about the selected unit, for example view range, move distance and any special features the unit may have.\n\n" +
                                    "Back to the game, it's time to move the other Bomber that still has ammo to attack. Move it to the highlighted tile, and attack the Cruiser.",
                            TutorialHighlight.disable(l),
                            BlockingAction.waitFor(ActionListener.complete()),
                            ModifyElements.forceTileSelect(l, 4, 3),
                            ModifyElements.attack(l, true, 4, 3),
                            BlockingAction.waitFor(ActionListener.perform())
                    ),
                    BlockingAction.waitFor(ActionListener.complete()),
                    CameraMove.toTile(l, 5, 2),
                    ModifyElements.moveUnit(l, true, 5, 2, 6, 0),
                    SequenceTextBox.onMap(l, BoxSize.MEDIUM_TALL, 8, 2, TextAlign.LEFT,
                            "The Fighter unit, which isn't of much use against the enemy Cruisers, can be used to defend the base from the enemy Fighter.\n\n" +
                                    "Move the Fighter and attack.",
                            TutorialHighlight.disable(l),
                            BlockingAction.waitFor(ActionListener.complete()),
                            ModifyElements.forceTileSelect(l, 6, 0),
                            ModifyElements.attack(l, true, 6, 0),
                            BlockingAction.waitFor(ActionListener.perform())
                    ),
                    BlockingAction.waitFor(ActionListener.complete()),
                    ContinueTextBox.onMap(l, BoxSize.EXTRA_LARGE, 1, 1.5f, TextAlign.LEFT,
                            "The Artillery unit has a ranged missile weapon, meaning that it is effective against Cruisers, similar to the Bomber unit. Unlike the Bomber unit, it has an " +
                                    "ammo capacity of " + CorvetteType.ARTILLERY.weapons.getFirst().ammoCapacity + ".\n\n" +
                                    "Ranged units have a special property. They do not receive counterattacks when attacking enemies, even if the enemy is also a ranged unit.\n\n" +
                                    "They are, however, not able to defend themselves with a counterattack when attacked by an enemy."),
                    ModifyElements.moveUnit(l, true, 5, 0, 5, 2),
                    SequenceTextBox.onMap(l, BoxSize.MEDIUM, 1, 1.5f, TextAlign.LEFT,
                            "Move the Artillery into the dense nebula, and attack one of the Cruisers. The dense nebula will " +
                                    "hide the defenceless unit from the enemies next turn.",
                            TutorialHighlight.disable(l),
                            BlockingAction.waitFor(ActionListener.complete()),
                            ModifyElements.forceTileSelect(l, 5, 2),
                            ModifyElements.attack(l, true, 5, 2),
                            AllowedActionTiles.only(Action.FIRE, new Point(5, 4), new Point(6, 3)),
                            BlockingAction.waitFor(ActionListener.perform())
                    ),
                    BlockingAction.waitFor(ActionListener.complete()),
                    ModifyElements.endTurn(l),
                    BlockingTextBox.onMap(l, BoxSize.SMALL_MEDIUM, 0, 4, TextAlign.LEFT,
                            "We've done all we can, it's time to end the turn.",
                            TurnListener.start()),
                    ModifyElements.disableAll(l),
                    BotActions.addActions(
                            BotActionData.move(l, 7, 1, 8, 2),
                            BotActionData.attack(l, 8, 2, 7, 3),
                            BotActionData.move(l, 6, 3, 7, 4),
                            BotActionData.attack(l, 7, 4, 7, 3),
                            BotActionData.attack(l, 5, 4, 4, 3),
                            BotActionData.end()
                    ),
                    BlockingAction.waitFor(TurnListener.start()),
                    BlockingAction.waitFor(AnimStateListener.ended()),


                    ContinueTextBox.onMap(l, BoxSize.MEDIUM_TALL, 1, 3, TextAlign.LEFT,
                                    "As you saw during the enemy's turn, the Bomber units were not able to do much damage to the Cruisers when they counterattacked. " +
                                            "We need to be able to resupply the Bombers."),
                    ContinueTextBox.onMap(l, BoxSize.LARGE, 1, 3, TextAlign.LEFT,
                                    "There are several ways to replenish ammo, but for this tutorial, we'll only cover one of them.\n\n" +
                                            "At the start of your turn, if a unit is on the same tile as an allied base structure, it gets all its ammo resupplied.\n\n" +
                                            "Not only that, it also regains some HP."),
                    ModifyElements.moveUnit(l, true, new Point[]{new Point(4, 3), new Point(7, 3)}, new Point[]{new Point(8, 1)}),
                    BlockingTextBox.onMap(l, BoxSize.MEDIUM, 1, 3, TextAlign.LEFT,
                            "Move one of the Bombers to the base so that it gets resupplied at the start of the next turn.",
                            ActionListener.perform()),
                    TutorialHighlight.disable(l),
                    BlockingAction.waitFor(ActionListener.complete()),
                    ContinueTextBox.onMap(l, BoxSize.MEDIUM_TALL, 4, 2, TextAlign.LEFT,
                            "That's it for this tutorial, use the weapon effectiveness view to make the best use of your units, and resupply ammo at the base when needed.\n\n" +
                                    "Good luck."),
                    ModifyElements.enableAll(l),
                    AllowedTiles.all(),
                    AllowedActionTiles.all(),
                    AllowedActions.all(),
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
