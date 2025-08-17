package level.tutorial;

import foundation.NamedEnum;
import foundation.math.MathUtil;
import level.GameplaySettings;
import level.Level;
import level.PlayerTeam;
import level.structure.StructureType;
import level.tile.TileType;
import level.tutorial.sequence.BoxSize;
import level.tutorial.sequence.action.*;
import level.tutorial.sequence.event.*;
import render.HorizontalAlign;
import render.Renderable;
import render.UIColourTheme;
import render.VerticalAlign;
import render.anim.sequence.KeyframeFunction;
import render.level.info.UITileInfo;
import render.types.box.UIDisplayBox;
import render.types.box.display.tutorial.TutorialMapElement;
import render.types.box.display.tutorial.TutorialMapText;
import render.types.box.display.tutorial.TutorialMapUnit;
import render.types.box.display.tutorial.TutorialMouseKeyframe;
import render.types.text.TextRenderer;
import render.types.tutorial.TutorialScreen;
import unit.UnitTeam;
import unit.action.Action;
import unit.action.ActionIconType;
import unit.bot.BotActionData;
import unit.stats.ModifierCategory;
import unit.type.CorvetteType;
import unit.type.FighterType;

import java.awt.*;
import java.util.function.Function;

import static level.tutorial.TutorialElement.*;
import static level.tutorial.TutorialManager.*;
import static render.types.text.StyleElement.*;

public enum TutorialLevel implements NamedEnum {
    INTRODUCTION("Introduction", "tutorial-intro",
            "Learn the basics of controlling units.",
            new TutorialLevelData(
                    new GameplaySettings(false, true)
            ).addPlayer(PlayerTeam.A, false).addPlayer(PlayerTeam.B, true),
            l -> new TutorialSequenceElement[]{
                    new TutorialAction(() -> {
                        l.levelRenderer.energyManager.setEnabled(false);
                        l.levelRenderer.mapButton.setEnabled(false);
                        l.levelRenderer.turnBox.setEnabled(false);
                        l.levelRenderer.endTurn.setEnabled(false);
                        l.levelRenderer.uiUnitInfo.infoButton.setEnabled(false);
                        l.levelRenderer.uiUnitInfo.viewFiringRange.setEnabled(false);
                        l.levelRenderer.uiUnitInfo.viewEffectiveness.setEnabled(false);
                    }),
                    ModifyElements.disable(l, END_TURN, TILE_SELECTION),
                    CameraMove.toTile(l, 1, 4),
                    TutorialHighlight.tile(l, BLUE_HIGHLIGHT, 1, 4),
                    ContinueTextBox.onMap(l, BoxSize.MEDIUM, 2, 4.7f, HorizontalAlign.LEFT,
                            "This is your [BLUE]Base[NO_COLOUR], which serves as the starting point for your units."),

                    CameraMove.toTile(l, 2, 4),
                    TutorialHighlight.tiles(l, BLUE_HIGHLIGHT, new Point(1, 5), new Point(2, 4)),
                    ContinueTextBox.onMap(l, BoxSize.MEDIUM, 1.6f, 6.4f, HorizontalAlign.LEFT,
                            "These are your [BLUE]units[NO_COLOUR]. For this tutorial, you've been provided with two [BLUE]" + FighterType.INTERCEPTOR.getName() + "[NO_COLOUR] units."),

                    ModifyElements.enable(l, TILE_SELECTION),
                    TutorialHighlight.disable(l),
                    BlockingTextBox.onMap(l, BoxSize.SMALL, 1.5f, 6f, HorizontalAlign.LEFT,
                            "[BLUE]Select[NO_COLOUR] one of the " + FighterType.INTERCEPTOR.getPluralName() + " by [BLUE]left-clicking it[NO_COLOUR].",
                            UnitSelectListener.ofTeam(UnitTeam.BLUE)),

                    ContinueTextBox.onMap(l, BoxSize.MEDIUM, 1.6f, 6.4f, HorizontalAlign.LEFT,
                            "Below the unit you can see [BLUE]two icons[NO_COLOUR]. These are the [BLUE]actions[NO_COLOUR] that the unit is able to perform."),

                    TutorialScreen.create(l, TutorialScreen.NORMAL_WIDTH, (box, level) -> {
                        box.setWidthMargin(0.5f);
                        box.addText(1.2f, HorizontalAlign.LEFT, "Moving Units");
                        box.addSpace(0.3f, 0);
                        box.addText(0.7f, HorizontalAlign.LEFT, "Each unit can be moved once per turn by using the " + Action.MOVE.colouredName(NO_COLOUR, true) + " action.");
                        box.addSpace(0.8f, 0);
                        box.addTutorialMap(HorizontalAlign.LEFT, 20, 10, TutorialMapElement.TILE_SIZE_MEDIUM, 7, 0, map -> {
                            map.addTile(-2, -1, TileType.EMPTY);
                            map.addTile(-2, 0, TileType.NEBULA).addUnit(FighterType.INTERCEPTOR, UnitTeam.BLUE);
                            map.addTile(-2, 1, TileType.NEBULA);
                            map.addTile(-1, -1, TileType.EMPTY);
                            map.addTile(-1, 0, TileType.EMPTY);
                            map.addTile(-1, 1, TileType.NEBULA);
                            map.addTile(0, -1, TileType.EMPTY);
                            map.addTile(0, 0, TileType.NEBULA);
                            map.addTile(0, 1, TileType.ASTEROIDS);
                            map.addTile(1, -1, TileType.EMPTY);
                            map.addTile(1, 0, TileType.EMPTY);
                            map.addTile(1, 1, TileType.ASTEROIDS);
                            map.addTile(2, 0, TileType.EMPTY);
                            map.addMouseParticle(
                                    TutorialMouseKeyframe.tile(0, 0, 1, 0, 0,
                                            KeyframeFunction.lerp(), KeyframeFunction.pow(1.5f)),
                                    TutorialMouseKeyframe.tile(1f, -2, 0, 0.2f, 0.2f,
                                                    KeyframeFunction.lerp(), KeyframeFunction.lerp())
                                            .setOnClick(0.3f, () -> map.setSelectedTile(-2, 0)
                                                    .actionSelector().setActionState(Action.FIRE, ActionIconType.UNUSABLE)),
                                    TutorialMouseKeyframe.delayUntil(1.6f),
                                    TutorialMouseKeyframe.actionSelector(2.5f, -2, 0, -0.05f, 0.1f, 2, 1,
                                                    KeyframeFunction.lerp(), KeyframeFunction.lerp())
                                            .setOnClick(0.3f, () -> map.selectMoveAction(-2, 0)),
                                    TutorialMouseKeyframe.delayUntil(3.2f, KeyframeFunction.lerp(), KeyframeFunction.pow(0.5f)),
                                    TutorialMouseKeyframe.tile(4.5f, 2, 0, -0.23f, 0.15f,
                                            null, null).setOnClick(0.4f, map::moveUnit)
                            );
                            map.finalise();
                        });
                        UIDisplayBox moveInstructions = new UIDisplayBox(0, 0, 10, 10, b -> b.setColourTheme(UIColourTheme.LIGHT_BLUE_BOX), false);
                        box.addBox(moveInstructions, HorizontalAlign.CENTER, 1, true);
                        box.setElementLeftMarginToElement(1, 0, 0, 4, HorizontalAlign.RIGHT, 0.5f);
                        box.setColumnTopMarginToElement(1, 0, 4, VerticalAlign.TOP);
                        box.setColumnVerticalAlign(1, VerticalAlign.TOP);
                        String c = MODIFIER_MOVEMENT_SPEED.display;
                        moveInstructions.addText(0.7f, HorizontalAlign.LEFT, "1. Select the unit to be moved.\n\n" +
                                "2. Select the " + Action.MOVE.colouredIconName(NO_COLOUR, false) + " action.\n\n" +
                                "3. Select one of the " + c + "highlighted tiles[NO_COLOUR] to move to.");
                        moveInstructions.setColumnVerticalAlign(0, VerticalAlign.TOP);
                        box.addSpace(1f, 0);
                        box.addText(1, HorizontalAlign.LEFT, ModifierCategory.MOVEMENT_SPEED_DISPLAY.getName());
                        box.addSpace(0.3f, 0);
                        String maxMovement = MathUtil.floatToString(FighterType.INTERCEPTOR.maxMovement);
                        box.addText(0.7f, HorizontalAlign.LEFT, "The " + c + "blue highlight[NO_COLOUR] that appears when the action is " +
                                "selected shows the tiles that are in " + c + "move range[NO_COLOUR]. " +
                                "Each unit has a maximum " + ModifierCategory.MOVEMENT_SPEED_DISPLAY.colouredName(NO_COLOUR, true) + ", and each tile the unit moves over " +
                                "has a " + ModifierCategory.MOVEMENT_COST_DISPLAY.colouredName(NO_COLOUR, true) + " that depends on the type of tile.\n\n" +

                                "The " + FighterType.INTERCEPTOR.getName() + " unit used in this example has a maximum " + ModifierCategory.MOVEMENT_SPEED_DISPLAY.getName().toLowerCase() + " of " +
                                c + maxMovement + ModifierCategory.MOVEMENT_SPEED_DISPLAY.icon() + "[NO_COLOUR].");
                        box.addSpace(0.6f, 0);
                        box.addTutorialMap(HorizontalAlign.CENTER, 26, 6, TutorialMapElement.TILE_SIZE_MEDIUM, 6, 0, map -> {
                            map.addTile(-3, 0, TileType.EMPTY).addUnit(FighterType.INTERCEPTOR, UnitTeam.BLUE);
                            map.addTile(-2, 0, TileType.EMPTY);
                            map.addTile(-1, 0, TileType.EMPTY);
                            map.addTile(0, 0, TileType.EMPTY);
                            map.addTile(1, 0, TileType.EMPTY);
                            map.addTile(2, 0, TileType.EMPTY);
                            map.addTile(3, 0, TileType.EMPTY);
                            map.addText(new TutorialMapText(map, 26 / 2, 0.5f, new TextRenderer(null, 1).setBold(true).setTextAlign(HorizontalAlign.CENTER), null, t -> {
                                t.updateText(MathUtil.floatToString(map.getTilePathCost().orElse(0f)) + " / " + maxMovement + ModifierCategory.MOVEMENT_SPEED_DISPLAY.icon());
                            }));
                            map.onResetTask(() -> {
                                map.selectMoveAction(-3, 0);
                            });
                            map.addMouseParticle(
                                    TutorialMouseKeyframe.tile(1, -3, 0, 0.2f, 0.2f,
                                            KeyframeFunction.lerp(), KeyframeFunction.pow(1.5f)),
                                    TutorialMouseKeyframe.tile(5, 3, 0, -0.2f, 0.2f,
                                            KeyframeFunction.lerp(), KeyframeFunction.lerp())
                            );
                            map.finalise();
                        });
                        box.addSpace(0.5f, 0);
                        box.addTutorialMap(HorizontalAlign.CENTER, 26, 6, TutorialMapElement.TILE_SIZE_MEDIUM, 6, 0, map -> {
                            map.addTile(-3, 0, TileType.NEBULA).addUnit(FighterType.INTERCEPTOR, UnitTeam.BLUE);
                            map.addTile(-2, 0, TileType.NEBULA);
                            map.addTile(-1, 0, TileType.NEBULA);
                            map.addTile(0, 0, TileType.NEBULA);
                            map.addTile(1, 0, TileType.NEBULA);
                            map.addTile(2, 0, TileType.NEBULA);
                            map.addTile(3, 0, TileType.NEBULA);
                            map.addText(new TutorialMapText(map, 26 / 2, 0.5f, new TextRenderer(null, 1).setBold(true).setTextAlign(HorizontalAlign.CENTER), null, t -> {
                                t.updateText(MathUtil.floatToString(map.getTilePathCost().orElse(0f)) + " / " + maxMovement + ModifierCategory.MOVEMENT_SPEED_DISPLAY.icon());
                            }));
                            map.onResetTask(() -> {
                                map.selectMoveAction(-3, 0);
                            });
                            map.addMouseParticle(
                                    TutorialMouseKeyframe.tile(1, -3, 0, 0.2f, 0.2f,
                                            KeyframeFunction.lerp(), KeyframeFunction.pow(1.5f)),
                                    TutorialMouseKeyframe.tile(5, 3, 0, -0.2f, 0.2f,
                                            KeyframeFunction.lerp(), KeyframeFunction.lerp())
                            );
                            map.finalise();
                        });
                        box.addSpace(0.5f, 0);
                        box.addText(0.7f, HorizontalAlign.LEFT, "In the first example, the unit is moving through " + c + TileType.EMPTY.getName().toLowerCase() +
                                " tiles[NO_COLOUR], which have a " + ModifierCategory.MOVEMENT_COST_DISPLAY.getName().toLowerCase() + " of " + c + "just " +
                                MathUtil.floatToString(TileType.EMPTY.moveCost) + ModifierCategory.MOVEMENT_SPEED_DISPLAY.icon() + "[NO_COLOUR], while in the second example, the unit is moving through " +
                                c + TileType.NEBULA.getName().toLowerCase() +
                                " tiles[NO_COLOUR], which have a " + c + "higher " + ModifierCategory.MOVEMENT_COST_DISPLAY.getName().toLowerCase() + " of " +
                                MathUtil.floatToString(TileType.NEBULA.moveCost) + ModifierCategory.MOVEMENT_SPEED_DISPLAY.icon() + "[NO_COLOUR].\n\n" +

                                "The " + c + ModifierCategory.MOVEMENT_COST_DISPLAY.getName().toLowerCase() + "[NO_COLOUR] of a given tile can be found by using the " + c + "tile info screen[NO_COLOUR] in the " + c + "bottom right corner[NO_COLOUR] whenever a tile is selected.\n\n" +

                                c + "Note:[NO_COLOUR] the tile which the unit is currently standing on " + c + "does not count[NO_COLOUR] toward the final " +
                                ModifierCategory.MOVEMENT_COST_DISPLAY.getName().toLowerCase() + "."
                        );

                        box.addSpace(2f, 0);
                        box.addText(1, HorizontalAlign.LEFT, "Movement Modifiers");
                        box.addSpace(0.3f, 0);
                        box.addText(0.7f, HorizontalAlign.LEFT, "When moving a unit, certain " + c + "modifiers[NO_COLOUR] may come into play that affect the " + ModifierCategory.MOVEMENT_COST_DISPLAY.getName().toLowerCase() +
                                " of different tile types. By default, " + c + "each type of unit[NO_COLOUR] has a modifier that affects how they move through " + c + TileType.ASTEROIDS.getName().toLowerCase() +
                                " tiles[NO_COLOUR], depending on the size of the unit.\n\n" +
                                "The modifiers in effect are prominently displayed for the " + c + "active unit[NO_COLOUR] while the " + c + Action.MOVE.getName().toLowerCase() + " action[NO_COLOUR] is selected.");
                    }),
                    BlockingTextBox.onMap(l, BoxSize.MEDIUM, 2f, 5.7f, HorizontalAlign.LEFT,
                            "Try moving one of your units",
                            ActionListener.perform(Action.MOVE)),
                    ModifyElements.disable(l, ACTIONS),
                    BlockingTextBox.onUI(l, BoxSize.MEDIUM, 20, 20, HorizontalAlign.LEFT,
                            "Great! Now that you can move units, it's also important to be able to move the camera. " +
                                    "[BLUE]Right-click + drag[NO_COLOUR], or alternatively, move the mouse to the edge of the screen to move the camera. " +
                                    "Try moving the camera around the level.", CameraMoveListener.distance(15)),
                    CameraMove.toTile(l, 7, 5),
                    ModifyElements.disable(l, CAMERA_MOVEMENT),
                    BlockingTextBox.onMap(l, BoxSize.MEDIUM, 7, 6.7f, HorizontalAlign.LEFT,
                            "Here we see the [RED]enemy base[NO_COLOUR], along with one of their units. [BLUE]Select the enemy unit[NO_COLOUR] to see more info about it.",
                            TileSelectListener.tile(6, 5)),
                    ModifyElements.disable(l, TILE_DESELECTION, TILE_SELECTION),
                    ContinueTextBox.onUI(l, BoxSize.LARGE, 20, 5f, HorizontalAlign.LEFT,
                            "With the unit selected, you get access to the [BLUE]unit info screen[NO_COLOUR] on the left. At the top, you can see that it's an " +
                                    FighterType.INTERCEPTOR.getName().toLowerCase() + " unit, just like the two allied units.\n\nThere are also boxes marked " +
                                    "[GREEN]hull[NO_COLOUR], [BLUE]engines[NO_COLOUR] and [RED]weapons[NO_COLOUR]. These display the [BLUE]most important characteristics[NO_COLOUR] of the unit.\n\nEach box can be " +
                                    "[BLUE]hovered over[NO_COLOUR] for more info."),
                    ContinueTextBox.onUI(l, BoxSize.LARGE, 36, 5f, HorizontalAlign.LEFT,
                            "Then there's the [BLUE]tile info screen[NO_COLOUR] on the right. It has information about the [BLUE]selected tile[NO_COLOUR].\n\nSimilar to the unit info screen, the boxes can be hovered over for more info."),
                    ContinueTextBox.onMap(l, BoxSize.MEDIUM, 7, 6.7f, HorizontalAlign.LEFT,
                            "To win, you're going to have to [RED]attack[NO_COLOUR] this enemy unit."),
                    TutorialScreen.create(l, TutorialScreen.NORMAL_WIDTH, (box, level) -> {
                        box.setWidthMargin(0.5f);
                        box.addText(1.2f, HorizontalAlign.LEFT, "Attacking Enemy Units");
                        box.addSpace(0.3f, 0);
                        box.addText(0.7f, HorizontalAlign.LEFT, "Just like with the " + Action.MOVE.getName().toLowerCase() + " action, [BLUE]each unit[NO_COLOUR] can be attack [BLUE]once per turn[NO_COLOUR] by using the " + Action.FIRE.colouredName(NO_COLOUR, true) + " action.\n\n" +
                                "In general, each unit can use each of its actions once per turn, in whichever order you choose.");
                        box.addSpace(0.8f, 0);
                        box.addTutorialMap(HorizontalAlign.LEFT, 20, 10, TutorialMapElement.TILE_SIZE_MEDIUM, 13, 0, map -> {
                            map.addTile(-2, -1, TileType.EMPTY);
                            map.addTile(-2, 0, TileType.NEBULA);
                            map.addTile(-2, 1, TileType.NEBULA);
                            map.addTile(-1, -1, TileType.EMPTY);
                            map.addTile(-1, 0, TileType.NEBULA).addUnit(FighterType.INTERCEPTOR, UnitTeam.BLUE).onReset(TutorialMapUnit::restoreHP);
                            map.addTile(-1, 1, TileType.NEBULA);
                            map.addTile(0, -1, TileType.EMPTY);
                            map.addTile(0, 0, TileType.EMPTY);
                            map.addTile(0, 1, TileType.ASTEROIDS);
                            map.addTile(1, -1, TileType.EMPTY);
                            map.addTile(1, 0, TileType.EMPTY).addUnit(FighterType.INTERCEPTOR, UnitTeam.RED).onReset(TutorialMapUnit::restoreHP);
                            map.addTile(1, 1, TileType.ASTEROIDS);
                            map.addTile(2, 0, TileType.EMPTY);
                            map.addText(new TutorialMapText(map, 10, 9, null, HorizontalAlign.CENTER, t -> {
                                t.updateText("Most units can only attack [BLUE]adjacent[NO_COLOUR] enemies.");
                                t.task(3.5f, TutorialMapText.setText("The unit must be [BLUE]moved closer[NO_COLOUR] before attacking."));
                                t.task(7, TutorialMapText.setText("Once [BLUE]in range[NO_COLOUR], you can [RED]attack[NO_COLOUR]."));
                            }, TutorialMapText::doNothing));
                            map.addMouseParticle(
                                    TutorialMouseKeyframe.tile(0f, -1, 0, 0.2f, 0.2f,
                                                    KeyframeFunction.lerp(), KeyframeFunction.lerp())
                                            .setOnClick(0.4f, () -> map.setSelectedTile(-1, 0)
                                                    .actionSelector().setActionState(Action.FIRE, ActionIconType.UNUSABLE)),
                                    TutorialMouseKeyframe.delayUntil(0.8f),
                                    TutorialMouseKeyframe.actionSelector(1.2f, -1, 0, 0.05f, 0.1f, 2, 0,
                                            KeyframeFunction.lerp(), KeyframeFunction.lerp()),
                                    TutorialMouseKeyframe.delayUntil(2.5f),
                                    TutorialMouseKeyframe.actionSelector(3f, -1, 0, -0.05f, 0.1f, 2, 1,
                                            KeyframeFunction.pow(1.4f), KeyframeFunction.lerp()).setOnClick(0.3f, () -> map.selectMoveAction(-1, 0)),
                                    TutorialMouseKeyframe.delayUntil(3.5f),
                                    TutorialMouseKeyframe.tile(4.2f, 0, 0, -0.2f, -0.1f, KeyframeFunction.lerp(), KeyframeFunction.lerp())
                                            .setOnClick(0.5f, map::moveUnit),
                                    TutorialMouseKeyframe.delayUntil(5.6f).setOnClick(0, () -> map.setSelectedTile(0, 0)
                                            .actionSelector().setActionState(Action.MOVE, ActionIconType.DISABLED)),
                                    TutorialMouseKeyframe.actionSelector(6.4f, 0, 0, 0.05f, 0.1f, 2, 0,
                                            KeyframeFunction.lerp(), KeyframeFunction.lerp()).setOnClick(0.3f, () -> map.selectFireAction(0, 0)),
                                    TutorialMouseKeyframe.delayUntil(7),
                                    TutorialMouseKeyframe.tile(7.7f, 1, 0, -0.07f, -0.15f, KeyframeFunction.lerp(), KeyframeFunction.lerp())
                                            .setOnClick(0.5f, () -> map.attackUnit(0, 0, 1, 0))
                            );
                            map.finalise();
                        });
                        UIDisplayBox fireInstructions = new UIDisplayBox(0, 0, 10, 10, b -> b.setColourTheme(UIColourTheme.LIGHT_BLUE_BOX), false);
                        box.addBox(fireInstructions, HorizontalAlign.CENTER, 1, true)
                                .setElementLeftMarginToElement(1, 0, 0, 4, HorizontalAlign.RIGHT, 0.5f)
                                .setColumnTopMarginToElement(1, 0, 4, VerticalAlign.TOP)
                                .setColumnVerticalAlign(1, VerticalAlign.TOP);
                        fireInstructions.addText(0.7f, HorizontalAlign.LEFT, "1. [BLUE]Move the unit[NO_COLOUR] to a tile adjacent to the enemy.\n\n" +
                                "2. Select the " + Action.FIRE.colouredIconName(NO_COLOUR, false) + " action.\n\n" +
                                "3. Select the [BLUE]enemy unit[NO_COLOUR] to attack.");
                        fireInstructions.setColumnVerticalAlign(0, VerticalAlign.TOP);
                        box.addSpace(1f, 0);
                        box.addText(1, HorizontalAlign.LEFT, "Unit HP");
                        box.addSpace(0.3f, 0);
                        box.addText(0.7f, HorizontalAlign.LEFT, "As you've probably noticed by now, each unit has a number in the [BLUE]bottom-right[NO_COLOUR] corner of its tile. " +
                                "This is its [BLUE]remaining HP[NO_COLOUR]. Note that it is always [BLUE]rounded up[NO_COLOUR], and that a more precise value is available in each unit's [BLUE]info screen[NO_COLOUR].\n\n" +
                                "In the example above, you can see the [BLUE]damage taken[NO_COLOUR] by each unit from the attack with the [RED]damage indicators[NO_COLOUR] that appear next to the HP number.");
                        box.addSpace(1.5f, 0);
                        box.addText(1, HorizontalAlign.LEFT, "Dealing Damage");
                        box.addSpace(0.3f, 0);
                        box.addText(0.7f, HorizontalAlign.LEFT, "There are a [BLUE]few factors[NO_COLOUR] that influence the damage dealt to an enemy unit.\n\n" +
                                "Firstly, the [RED]base weapon damage[BLUE]. This depends on which [BLUE]type of unit[NO_COLOUR] is attacking, and can be seen in the [BLUE]unit info screen]. " +
                                FighterType.INTERCEPTOR.getPluralName() + " have a base damage of [RED]" + MathUtil.floatToString(FighterType.INTERCEPTOR.damage) + ModifierCategory.DAMAGE.icon() + "[NO_COLOUR].\n\n" +
                                "Secondly, a unit with [BLUE]less HP remaining[NO_COLOUR] will deal [RED]less damage[NO_COLOUR], depending on how much HP it has lost. A [BLUE]severely damaged[NO_COLOUR] unit may do as little as " +
                                "[BLUE]25%[NO_COLOUR] of its normal damage.");
                        box.addSpace(0.5f, 0);
                        box.addTutorialMap(HorizontalAlign.LEFT, 14.25f, 10, TutorialMapElement.TILE_SIZE_MEDIUM, 8, 0, map -> {
                            map.addTile(-1, 0, TileType.EMPTY);
                            map.addTile(-1, 1, TileType.DENSE_NEBULA);
                            map.addTile(-1, -1, TileType.EMPTY);
                            map.addTile(0, 0, TileType.EMPTY).addUnit(FighterType.INTERCEPTOR, UnitTeam.BLUE).onReset(TutorialMapUnit::restoreHP);
                            map.addTile(0, 1, TileType.NEBULA);
                            map.addTile(0, -1, TileType.EMPTY);
                            map.addTile(1, 0, TileType.EMPTY).addUnit(FighterType.INTERCEPTOR, UnitTeam.RED).onReset(TutorialMapUnit::restoreHP);
                            map.addText(new TutorialMapText(map, 14.25f / 2, 9, new TextRenderer("A unit with [GREEN]max HP[NO_COLOUR] deals [GREEN]full damage[NO_COLOUR]", 0.6f).setTextAlign(HorizontalAlign.CENTER).setBold(true), TutorialMapText::doNothing, TutorialMapText::doNothing));
                            map.addMouseParticle(
                                    TutorialMouseKeyframe.tile(0, 0, 0, 0.17f, -0.1f, KeyframeFunction.lerp(), KeyframeFunction.lerp())
                                            .setOnClick(0.7f, () -> map.setSelectedTile(0, 0)),
                                    TutorialMouseKeyframe.delayUntil(1),
                                    TutorialMouseKeyframe.actionSelector(2, 0, 0, 0.05f, 0.1f, 2, 0,
                                            KeyframeFunction.lerp(), KeyframeFunction.lerp()).setOnClick(0.3f, () -> map.selectFireAction(0, 0)),
                                    TutorialMouseKeyframe.delayUntil(2.6f),
                                    TutorialMouseKeyframe.tile(3.4f, 1, 0, -0.07f, -0.15f, KeyframeFunction.lerp(), KeyframeFunction.lerp())
                                            .setOnClick(0.5f, () -> map.attackUnit(0, 0, 1, 0))
                            );
                            map.finalise();
                        });
                        box.addTutorialMap(HorizontalAlign.RIGHT, 14.25f, 10, TutorialMapElement.TILE_SIZE_MEDIUM, 8, 2, map -> {
                            map.addTile(-1, 0, TileType.EMPTY);
                            map.addTile(-1, 1, TileType.DENSE_NEBULA);
                            map.addTile(-1, -1, TileType.EMPTY);
                            map.addTile(0, 0, TileType.EMPTY).addUnit(FighterType.INTERCEPTOR, UnitTeam.BLUE).onReset(u -> u.restoreHP(4));
                            map.addTile(0, 1, TileType.NEBULA);
                            map.addTile(0, -1, TileType.EMPTY);
                            map.addTile(1, 0, TileType.EMPTY).addUnit(FighterType.INTERCEPTOR, UnitTeam.RED).onReset(TutorialMapUnit::restoreHP);
                            map.addText(new TutorialMapText(map, 14.25f / 2, 9, new TextRenderer("A [RED]damaged[NO_COLOUR] unit deals [RED]reduced damage[NO_COLOUR]", 0.6f).setTextAlign(HorizontalAlign.CENTER).setBold(true), TutorialMapText::doNothing, TutorialMapText::doNothing));
                            map.addMouseParticle(
                                    TutorialMouseKeyframe.tile(0, 0, 0, 0.17f, -0.1f, KeyframeFunction.lerp(), KeyframeFunction.lerp())
                                            .setOnClick(0.7f, () -> map.setSelectedTile(0, 0)),
                                    TutorialMouseKeyframe.delayUntil(1),
                                    TutorialMouseKeyframe.actionSelector(2, 0, 0, 0.05f, 0.1f, 2, 0,
                                            KeyframeFunction.lerp(), KeyframeFunction.lerp()).setOnClick(0.3f, () -> map.selectFireAction(0, 0)),
                                    TutorialMouseKeyframe.delayUntil(2.6f),
                                    TutorialMouseKeyframe.tile(3.4f, 1, 0, -0.07f, -0.15f, KeyframeFunction.lerp(), KeyframeFunction.lerp())
                                            .setOnClick(0.5f, () -> map.attackUnit(0, 0, 1, 0))
                            );
                            map.finalise();
                        });
                        box.setColumnTopMarginToElement(2, 0, 14, VerticalAlign.TOP)
                                .setColumnVerticalAlign(2, VerticalAlign.TOP);
                        box.addSpace(1f, 0);
                        box.addText(1, HorizontalAlign.LEFT, "Modifiers");
                        box.addSpace(0.3f, 0);
                        box.addText(0.7f, HorizontalAlign.LEFT, "Similar to [BLUE]movement modifiers[NO_COLOUR], attacks can also have [BLUE]modifiers[NO_COLOUR]. While other modifiers do exist, " +
                                "[BLUE]most attacks[NO_COLOUR] only have two: the [BLUE]weapon effectiveness modifier[NO_COLOUR] (more on that later), and the [BLUE]tile modifier[NO_COLOUR].\n\n" +
                                "Different [BLUE]tile types[NO_COLOUR] have different modifiers, typically [BLUE]reducing[NO_COLOUR] the damage received by the unit standing on it.\n\n" +
                                "The effect of the tile modifier can be seen in the [RED]" + ModifierCategory.INCOMING_DAMAGE.getName() + "[NO_COLOUR] box in the [BLUE]tile info screen[NO_COLOUR].");
                        box.addSpace(0.5f, 0);
                        box.addTutorialMap(HorizontalAlign.LEFT, 14.25f, 10, TutorialMapElement.TILE_SIZE_MEDIUM, 8, 0, map -> {
                            map.addTile(-1, 0, TileType.EMPTY);
                            map.addTile(-1, 1, TileType.DENSE_NEBULA);
                            map.addTile(-1, -1, TileType.EMPTY);
                            map.addTile(0, 0, TileType.EMPTY).addUnit(FighterType.INTERCEPTOR, UnitTeam.BLUE).onReset(TutorialMapUnit::restoreHP);
                            map.addTile(0, 1, TileType.NEBULA);
                            map.addTile(0, -1, TileType.EMPTY);
                            map.addTile(1, 0, TileType.EMPTY).addUnit(FighterType.INTERCEPTOR, UnitTeam.RED).onReset(TutorialMapUnit::restoreHP);
                            map.addText(new TutorialMapText(map, 14.25f / 2, 9, new TextRenderer("The enemy on an [BLUE]" + TileType.EMPTY.getName().toLowerCase() + " tile[NO_COLOUR] receives [GREEN]full damage[NO_COLOUR]", 0.55f).setTextAlign(HorizontalAlign.CENTER).setBold(true), TutorialMapText::doNothing, TutorialMapText::doNothing));
                            map.addMouseParticle(
                                    TutorialMouseKeyframe.tile(0, 0, 0, 0.17f, -0.1f, KeyframeFunction.lerp(), KeyframeFunction.lerp())
                                            .setOnClick(0.7f, () -> map.setSelectedTile(0, 0)),
                                    TutorialMouseKeyframe.delayUntil(1),
                                    TutorialMouseKeyframe.actionSelector(2, 0, 0, 0.05f, 0.1f, 2, 0,
                                            KeyframeFunction.lerp(), KeyframeFunction.lerp()).setOnClick(0.3f, () -> map.selectFireAction(0, 0)),
                                    TutorialMouseKeyframe.delayUntil(2.6f),
                                    TutorialMouseKeyframe.tile(3.4f, 1, 0, -0.07f, -0.15f, KeyframeFunction.lerp(), KeyframeFunction.lerp())
                                            .setOnClick(0.5f, () -> map.attackUnit(0, 0, 1, 0))
                            );
                            map.finalise();
                        });
                        box.addTutorialMap(HorizontalAlign.RIGHT, 14.25f, 10, TutorialMapElement.TILE_SIZE_MEDIUM, 8, 3, map -> {
                            map.addTile(-1, 0, TileType.EMPTY);
                            map.addTile(-1, 1, TileType.DENSE_NEBULA);
                            map.addTile(-1, -1, TileType.EMPTY);
                            map.addTile(0, 0, TileType.EMPTY).addUnit(FighterType.INTERCEPTOR, UnitTeam.BLUE).onReset(TutorialMapUnit::restoreHP);
                            map.addTile(0, 1, TileType.NEBULA);
                            map.addTile(0, -1, TileType.EMPTY);
                            map.addTile(1, 0, TileType.NEBULA).addUnit(FighterType.INTERCEPTOR, UnitTeam.RED).onReset(TutorialMapUnit::restoreHP);
                            map.addText(new TutorialMapText(map, 14.25f / 2, 9, new TextRenderer("The enemy on a [BLUE]" + TileType.NEBULA.getName().toLowerCase() + " tile[NO_COLOUR] receives [RED]reduced damage[NO_COLOUR]", 0.5f).setTextAlign(HorizontalAlign.CENTER).setBold(true), TutorialMapText::doNothing, TutorialMapText::doNothing));
                            map.addMouseParticle(
                                    TutorialMouseKeyframe.tile(0, 0, 0, 0.17f, -0.1f, KeyframeFunction.lerp(), KeyframeFunction.lerp())
                                            .setOnClick(0.7f, () -> map.setSelectedTile(0, 0)),
                                    TutorialMouseKeyframe.delayUntil(1),
                                    TutorialMouseKeyframe.actionSelector(2, 0, 0, 0.05f, 0.1f, 2, 0,
                                            KeyframeFunction.lerp(), KeyframeFunction.lerp()).setOnClick(0.3f, () -> map.selectFireAction(0, 0)),
                                    TutorialMouseKeyframe.delayUntil(2.6f),
                                    TutorialMouseKeyframe.tile(3.4f, 1, 0, -0.07f, -0.15f, KeyframeFunction.lerp(), KeyframeFunction.lerp())
                                            .setOnClick(0.5f, () -> map.attackUnit(0, 0, 1, 0))
                            );
                            map.finalise();
                        });
                        box.setColumnTopMarginToElement(3, 0, 20, VerticalAlign.TOP)
                                .setColumnVerticalAlign(3, VerticalAlign.TOP);
                        box.addSpace(1f, 0);
                        box.addText(1, HorizontalAlign.LEFT, "Counterattacks");
                        box.addSpace(0.3f, 0);
                        box.addText(0.7f, HorizontalAlign.LEFT, "In all the examples above, it is [BLUE]not only the enemy[NO_COLOUR] that takes damage, but also the [BLUE]allied unit[NO_COLOUR]. Whenever attacking, the " +
                                "enemy will retaliate with a [RED]counterattack[NO_COLOUR].\n\nThe only difference between the initial attack and the counterattack is the [BLUE]order they are calculated[NO_COLOUR].\n\n" +
                                "The counterattack is performed [BLUE]after[NO_COLOUR] the enemy unit has [BLUE]taken damage[NO_COLOUR] from the initial attack and has been [BLUE]weakened[NO_COLOUR]. You therefore gain an [BLUE]advantage[NO_COLOUR] by attacking first.\n\n" +
                                "When counterattacking, the enemy unit will use its [BLUE]own weapons[NO_COLOUR] and [BLUE]damage modifiers[NO_COLOUR], so be careful with which enemies you choose to engage. " +
                                "Positioning [BLUE]allied units[NO_COLOUR] on tiles which reduce [RED]" + ModifierCategory.INCOMING_DAMAGE.getName().toLowerCase() + "[NO_COLOUR] is generally a good idea.");
                    }),
                    ContinueTextBox.onMap(l, BoxSize.MEDIUM, 4, 6.5f, HorizontalAlign.LEFT,
                            "Send an " + FighterType.INTERCEPTOR.getName() + " to attack the enemy unit."),

                    TutorialHighlight.tiles(l, GREEN_HIGHLIGHT, new Point(5, 6)),
                    ModifyElements.enable(l, ACTION_TILE_SELECTION, TILE_SELECTION),
                    AllowedActionTiles.only(Action.MOVE, 5, 6),
                    BlockingTextBox.onMap(l, BoxSize.MEDIUM, 4, 6.8f, HorizontalAlign.LEFT,
                            "Most units can only attack enemies on [RED]adjacent tiles[NO_COLOUR].\n\nLeft-click the highlighted tile to move the selected unit in range to attack.",
                            ActionListener.perform(Action.MOVE)),

                    TutorialHighlight.disable(l),
                    BlockingAction.waitFor(ActionListener.complete(Action.MOVE)),

                    ModifyElements.disable(l, TILE_SELECTION),
                    ContinueTextBox.onMap(l, BoxSize.MEDIUM, 2, 6, HorizontalAlign.LEFT,
                            "You can see that the Move action is now greyed out.\n\nEach unit can only perform each of its actions [BLUE]once per turn[NO_COLOUR]."),

                    AllowedActions.only(Action.FIRE),
                    AllowedActionTiles.only(Action.FIRE, 6, 5),
                    BlockingTextBox.onMap(l, BoxSize.MEDIUM, 2, 6, HorizontalAlign.LEFT,
                            "Now that the enemy unit is in range, select the Fire action, then select the unit to attack it.",
                            ActionListener.perform(Action.FIRE)),

                    BlockingAction.waitFor(ActionListener.complete(Action.FIRE)),
                    ModifyElements.enable(l, TILE_SELECTION),
                    AllowedTiles.only(5, 6),
                    TileSelect.tile(l, 5, 6),

                    TutorialUI.onMap(l, "unitHPHighlight")
                            .rectangle(5.18f, 6.01f, 0.8f, 1, GREEN_HIGHLIGHT, TutorialUI.StrokeWidth.NARROW)
                            .rectangle(6.18f, 5.01f, 0.8f, 1, GREEN_HIGHLIGHT, TutorialUI.StrokeWidth.NARROW),
                    ContinueTextBox.onMap(l, BoxSize.MEDIUM, 2, 6, HorizontalAlign.LEFT,
                            "There is a number in the bottom right corner of each unit. This number shows the [GREEN]remaining HP[NO_COLOUR] of the unit, rounded up."),
                    ContinueTextBox.onMap(l, BoxSize.MEDIUM, 2, 6, HorizontalAlign.LEFT,
                            "Fighter units start with " + ((int) (FighterType.INTERCEPTOR.hitPoints)) + " HP, and, as you can see, [BLUE]both the attacking unit and " +
                                    "the enemy unit[NO_COLOUR] have taken some damage.\n\nNotice that the enemy unit took more damage than the attacking one."),
                    ContinueTextBox.onMap(l, BoxSize.MEDIUM, 2, 6, HorizontalAlign.LEFT,
                            "This is because after you attack a unit, the now weakened enemy will [BLUE]counterattack[NO_COLOUR] with whatever strength it has left.\n\n" +
                                    "[RED]The less HP a unit has, the less damage it will be able to do.[NO_COLOUR]"),

                    TutorialUI.remove("unitHPHighlight"),
                    TutorialUI.onUI(l, "unitInfoHP")
                            .rectangle(6, 4.75f, 10, 1.4f, GREEN_HIGHLIGHT, TutorialUI.StrokeWidth.NARROW),
                    ContinueTextBox.onUI(l, BoxSize.MEDIUM, 20, 4, HorizontalAlign.LEFT,
                            "You can also view the HP of the selected unit on the panel to the left, this time with one decimal place of precision."),

                    TutorialUI.remove("unitInfoHP"),

                    ModifyElements.enable(l, CAMERA_MOVEMENT, ACTIONS, TILE_SELECTION, TILE_DESELECTION),
                    AllowedActions.only(Action.MOVE),
                    AllowedTiles.all(),
                    AllowedActionTiles.only(Action.MOVE, new Point(5, 5), new Point(5, 4)),
                    TutorialHighlight.tiles(l, GREEN_HIGHLIGHT, new Point(5, 5), new Point(5, 4)),
                    SequenceTextBox.onMap(l, BoxSize.MEDIUM, 2, 6, HorizontalAlign.LEFT,
                            "Move the other Fighter to the one of the highlighted tiles and attack the enemy.\n\nTo move the camera, use [BLUE]right-click + drag[NO_COLOUR], or move the mouse to the edge of the screen.",
                            BlockingAction.waitFor(ActionListener.complete(Action.MOVE)),

                            TutorialHighlight.disable(l),
                            AllowedActions.only(Action.FIRE),
                            AllowedActionTiles.all(),
                            BlockingAction.waitFor(ActionListener.complete(Action.FIRE))
                    ),
                    new TutorialAction(() -> {
                        l.levelRenderer.turnBox.setEnabled(true);
                        l.levelRenderer.endTurn.setEnabled(true);
                    }),
                    TutorialUI.onUI(l, "endTurnHighlight")
                            .rectangle(8f, Renderable.top() - 3, 10, 3, GREEN_HIGHLIGHT, TutorialUI.StrokeWidth.NORMAL),
                    ModifyElements.enable(l, END_TURN, ACTION_DESELECT),
                    BlockingTextBox.onUI(l, BoxSize.MEDIUM, 7, Renderable.top() - 10, HorizontalAlign.LEFT,
                            "Now that you've performed all the actions you can, it's time to end this turn.\n\nThe enemy will now attack, but by attacking first, you've gained the upper hand.",
                            TurnListener.start()),
                    TutorialUI.remove("endTurnHighlight"),

                    BlockingAction.waitFor(TurnListener.start(UnitTeam.BLUE)),

                    AllowedActions.all(),
                    BlockingTextBox.onMap(l, BoxSize.MEDIUM, 2, 6, HorizontalAlign.LEFT,
                            "Use what you've learnt to destroy the remaining enemy units.",
                            ActionListener.perform()),

                    new EndTutorial()
            }),
    FOW("Tile Types", "tutorial-tile-types",
            "This tutorial covers the fog of war mechanic, tile types and capturing structures.",
            new TutorialLevelData(
                    new GameplaySettings(true, true)
            ).addPlayer(PlayerTeam.A, false).addPlayer(PlayerTeam.B, true),
            l -> new TutorialSequenceElement[]{
                    ModifyElements.disable(l, TILE_DESELECTION, TILE_SELECTION, CAMERA_MOVEMENT, ACTIONS, ACTION_DESELECT, ACTION_TILE_SELECTION, END_TURN, VIEW_FIRING_RANGE, VIEW_EFFECTIVENESS),
                    ContinueTextBox.onMap(l, BoxSize.MEDIUM, -3, 5, HorizontalAlign.LEFT,
                            "Welcome to the second tutorial, about fog of war, tile types, and structure capturing."),
                    ContinueTextBox.onMap(l, BoxSize.MEDIUM, -3, 5, HorizontalAlign.LEFT,
                            "As you can quite clearly see, many of the tiles are greyed out this time. That's because of the [BLUE]fog of war[NO_COLOUR] setting, which is enabled by default.\n\nExpect to see it in most future tutorials."),

                    TutorialHighlight.visibleTiles(l, GREEN_HIGHLIGHT),
                    ContinueTextBox.onMap(l, BoxSize.MEDIUM, -3, 5, HorizontalAlign.LEFT,
                            "The highlighted tiles are the ones that are visible. Each unit has a [BLUE]view range[NO_COLOUR] in which tiles are visible.\n\nTiles not visible will hide the positions of enemy units."),

                    TutorialHighlight.fowTiles(l, RED_HIGHLIGHT),
                    ContinueTextBox.onMap(l, BoxSize.MEDIUM, -3, 5, HorizontalAlign.LEFT,
                            "That means that there could be enemy units on any of these tiles that aren't visible.\n\nBefore you can attack, you'll need to find the enemy units."),

                    TutorialHighlight.radius(l, RED_HIGHLIGHT, 8, 3, 1),
                    CameraMove.toTile(l, 8, 3),
                    ContinueTextBox.onMap(l, BoxSize.LARGE, 5, 5, HorizontalAlign.LEFT,
                            "We do, however, know where the enemy base is, as [BLUE]structures[NO_COLOUR] are always visible regardless of fog of war.\n\n" +
                                    "We also know that units spawn close to the base, so we can expect the enemy units to be somewhere in the highlighted area."),

                    TutorialHighlight.disable(l),
                    CameraMove.toTile(l, 3, 3),
                    ContinueTextBox.onMap(l, BoxSize.LARGE, 2, 1, HorizontalAlign.LEFT,
                            "As you've probably noticed, not all tiles are the same. Different tile types have different properties, one of them being visibility, " +
                                    "which affects a unit's ability to see through the tile.\n\nFor example, a unit that can see through 3 empty tiles might only be able to see through 2 nebula tiles."),

                    ModifyElements.enable(l, TILE_SELECTION),
                    AllowedTiles.only(4, 3),
                    TutorialHighlight.tile(l, BLUE_HIGHLIGHT, 4, 3),
                    BlockingTextBox.onMap(l, BoxSize.MEDIUM, 2, 1.5f, HorizontalAlign.LEFT,
                            "Select the highlighted tile to see more info about it.",
                            TileSelectListener.any()),

                    TutorialUI.onUI(l, "tileName")
                            .rectangle(Renderable.right() - 8f, 17.65f, 15, 1.5f, GREEN_HIGHLIGHT, TutorialUI.StrokeWidth.NARROW),
                    ContinueTextBox.onUI(l, BoxSize.MEDIUM, Renderable.right() - 23, 16, HorizontalAlign.LEFT,
                            "Here you can see the type of tile that is selected, in this case a [BLUE]nebula tile[NO_COLOUR]."),
                    TutorialUI.remove("tileName"),

                    TutorialUI.onUI(l, "tileVisibility")
                            .rectangle(Renderable.right() - 8f, 3 * UITileInfo.BAR_SPACING + UITileInfo.INITIAL_BAR_POS + 0.1f, 13.5f, 1.7f, GREEN_HIGHLIGHT, TutorialUI.StrokeWidth.NARROW),
                    ContinueTextBox.onUI(l, BoxSize.LARGE, Renderable.right() - 25, 3, HorizontalAlign.LEFT,
                            "This bar shows the [BLUE]visibility[NO_COLOUR] of the tile type.\n\nLow visibility is good for keeping your units hidden, but remember that it's not just " +
                                    "about the tile the unit is on, but also which tiles lie between you and your enemy that matter for staying hidden."),
                    TutorialUI.remove("tileVisibility"),

                    TutorialUI.onUI(l, "tileDefence")
                            .rectangle(Renderable.right() - 8f, 2 * UITileInfo.BAR_SPACING + UITileInfo.INITIAL_BAR_POS + 0.1f, 13.5f, 1.7f, GREEN_HIGHLIGHT, TutorialUI.StrokeWidth.NARROW),
                    ContinueTextBox.onUI(l, BoxSize.LARGE, Renderable.right() - 25, 3, HorizontalAlign.LEFT,
                            "Here you can see the [BLUE]defence[NO_COLOUR] bonus that units receive when on this tile. The defence bonus is a " +
                                    "damage reduction when being attacked or counterattacked.\n\nIt is therefore ideal to attack " +
                                    "from tiles with high defence to reduce the damage of the subsequent counterattack, and to reduce the damage when it's the " +
                                    "enemy's turn to retaliate."),
                    TutorialUI.remove("tileDefence"),

                    TutorialUI.onUI(l, "tileMovement")
                            .rectangle(Renderable.right() - 8f, 1 * UITileInfo.BAR_SPACING + UITileInfo.INITIAL_BAR_POS + 0.1f, 13.5f, 1.7f, GREEN_HIGHLIGHT, TutorialUI.StrokeWidth.NARROW),
                    ContinueTextBox.onUI(l, BoxSize.LARGE, Renderable.right() - 25, 3, HorizontalAlign.LEFT,
                            "This value represents the ease with which units can move through the tile, the [BLUE]movement speed[NO_COLOUR]. Moving a unit through tiles with low movement " +
                                    "will reduce the distance the unit can travel.\n\nHigh movement " +
                                    "tiles are ideal when moving units across long distances."),
                    TutorialUI.remove("tileMovement"),

                    ContinueTextBox.onUI(l, BoxSize.MEDIUM, Renderable.right() - 25, 8, HorizontalAlign.LEFT,
                            "As you can see, nebula tiles are good for hiding, while also having a small defence bonus."),

                    TutorialHighlight.tile(l, BLUE_HIGHLIGHT, 3, 3),
                    AllowedTiles.only(3, 3),
                    TileSelect.tile(l, 3, 3),
                    ContinueTextBox.onUI(l, BoxSize.MEDIUM, Renderable.right() - 25, 8, HorizontalAlign.LEFT,
                            "This [BLUE]empty tile[NO_COLOUR] right next to it is almost the opposite. With no defence and high visibility, its main advantage is its low movement cost."),

                    TutorialHighlight.tile(l, BLUE_HIGHLIGHT, 4, 2),
                    AllowedTiles.only(4, 2),
                    TileSelect.tile(l, 4, 2),
                    ContinueTextBox.onMap(l, BoxSize.LARGE, 0, 1, HorizontalAlign.LEFT,
                            "For the [BLUE]asteroid field tile[NO_COLOUR], while it does have a slightly lower visibility than an empty tile, its main advantage is its high defence bonus.\n\n" +
                                    "While small units like the Fighters in this tutorial can move through asteroid fields without much difficulty, larger units either can't move through them at all, or can only move one tile at a time."),

                    TutorialHighlight.tile(l, BLUE_HIGHLIGHT, 5, 5),
                    AllowedTiles.only(5, 5),
                    TileSelect.tile(l, 5, 5),
                    CameraMove.toTile(l, 5, 5),
                    ContinueTextBox.onMap(l, BoxSize.LARGE, 5, 2, HorizontalAlign.LEFT,
                            "Lastly, the [BLUE]dense nebula tile[NO_COLOUR]. These appear scattered throughout regular nebula tiles, and have a defence bonus somewhere between a regular nebula and an asteroid field.\n\n" +
                                    "Most importantly, they have a special property. No matter how good the view distance is for a given unit, the only way for it to see inside a dense nebula is if the unit is [BLUE]directly adjacent[NO_COLOUR] to it."),

                    ContinueTextBox.onMap(l, BoxSize.MEDIUM, 5, 3, HorizontalAlign.LEFT,
                            "We can use this property to our advantage in this situation. By moving a Fighter to this tile, it'll reveal the enemy positions without risking detection, preventing the enemy from attacking first on the next turn."),
                    SequenceTextBox.onMap(l, BoxSize.SMALL, 5, 4, HorizontalAlign.LEFT,
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

                    ContinueTextBox.onMap(l, BoxSize.MEDIUM, 5, 3, HorizontalAlign.LEFT,
                            "Enemy spotted!\n\nWhile you could attack the enemy with your other Fighter, it'll only lead to the enemy retaliating two on one next turn."),

                    ModifyElements.actionCameraNoDeselect(l),
                    TutorialHighlight.tile(l, GREEN_HIGHLIGHT, 4, 6),
                    AllowedActionTiles.only(Action.MOVE, 4, 6),
                    BlockingTextBox.onMap(l, BoxSize.MEDIUM, 5, 3, HorizontalAlign.LEFT,
                            "Instead, move the other Fighter to the highlighted tile, away from the enemy.\n\nWe'll wait until next turn to attack.",
                            ActionListener.perform(Action.MOVE)),

                    TutorialHighlight.disable(l),

                    BlockingAction.waitFor(ActionListener.complete()),
                    TileSelect.deselect(l),
                    ModifyElements.endTurn(l),

                    BlockingTextBox.onMap(l, BoxSize.MEDIUM, 0, 7, HorizontalAlign.LEFT,
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

                    ContinueTextBox.onMap(l, BoxSize.MEDIUM, 0, 5, HorizontalAlign.LEFT,
                            "The enemy could not find the Fighter units, and decided to start capturing the base instead!"),
                    ContinueTextBox.onMap(l, BoxSize.MEDIUM, 0, 5, HorizontalAlign.LEFT,
                            "Most units can [DARK_GREEN]capture[NO_COLOUR] by simply being on the same tile as an enemy structure. Capturing takes multiple turns, as shown by the [BLUE]progress bar[NO_COLOUR] above the enemy unit that's currently capturing."),
                    ContinueTextBox.onMap(l, BoxSize.LARGE, 0, 5, HorizontalAlign.LEFT,
                            "Once the progress bar fills, the enemy gains control over the structure, or the structure is destroyed, depending on the type of structure.\n\n" +
                                    "As for the base structure, not only is it destroyed, the player gets eliminated if it is successfully captured.\n\n" +
                                    "You must stop the capture of your base at any cost!"),
                    ContinueTextBox.onMap(l, BoxSize.MEDIUM, 0, 5, HorizontalAlign.LEFT,
                            "To stop a capture, simply [RED]attack[NO_COLOUR] the capturing unit.\n\n" +
                                    "Each time the capturing unit is attacked, or performs an attack, the capture progress bar is reduced by one turn."),

                    TutorialHighlight.tile(l, GREEN_HIGHLIGHT, 0, 4),
                    CameraMove.toTile(l, 0, 4),
                    ContinueTextBox.onMap(l, BoxSize.MEDIUM, 1, 7, HorizontalAlign.LEFT,
                            "Move a Fighter unit here to attack the capturing unit. By attacking from an asteroid field, you'll also receive less damage when counterattacked."),

                    ModifyElements.actionCameraNoDeselect(l),
                    AllowedActionTiles.only(Action.MOVE, 0, 4),
                    CameraMove.toTile(l, 5, 5),
                    TutorialHighlight.tiles(l, GREEN_HIGHLIGHT, new Point(0, 4), new Point(5, 5)),

                    BlockingTextBox.onMap(l, BoxSize.MEDIUM, 5, 6.3f, HorizontalAlign.LEFT,
                            "Select this unit and move it to the highlighted asteroid field tile.",
                            ActionListener.select()),

                    CameraMove.toTile(l, 0, 4),
                    ModifyElements.disableAll(l),
                    ModifyElements.enable(l, ACTION_DESELECT),
                    TutorialHighlight.tile(l, RED_HIGHLIGHT, 0, 4),

                    BlockingTextBox.onMap(l, BoxSize.LARGE, -3, 5.5f, HorizontalAlign.LEFT,
                            "You can see that the tile is just out of range.\n\n" +
                                    "After selecting an action, you can go back by using the [BLUE]Exit Action[NO_COLOUR] button below the End Turn button, or by using the [BLUE]Escape key[NO_COLOUR], but remember that you cannot undo actions that have been completed.\n\n" +
                                    "Exit the move action so you can select the other Fighter that is in range.",
                            ActionListener.deselect()),

                    ModifyElements.action(l, true),
                    TutorialHighlight.tile(l, GREEN_HIGHLIGHT, 0, 4),

                    BlockingTextBox.onMap(l, BoxSize.MEDIUM, 1, 7, HorizontalAlign.LEFT,
                            "Move the other Fighter to the highlighted asteroid field tile instead.",
                            ActionListener.perform()),

                    TutorialHighlight.disable(l),
                    AllowedTiles.only(0, 4),

                    BlockingAction.waitFor(ActionListener.complete()),
                    BlockingTextBox.onMap(l, BoxSize.SMALL, 1, 6, HorizontalAlign.LEFT,
                            "Attack the capturing enemy unit.",
                            ActionListener.perform()),

                    BlockingAction.waitFor(ActionListener.complete()),
                    ContinueTextBox.onMap(l, BoxSize.MEDIUM, 1, 6, HorizontalAlign.LEFT,
                            "You can see that the capture progress went down after the attack."),

                    CameraMove.toTile(l, 5, 5),
                    AllowedTiles.only(5, 5),
                    AllowedActionTiles.only(Action.MOVE, 8, 3),
                    TutorialHighlight.tile(l, GREEN_HIGHLIGHT, 8, 3),

                    BlockingTextBox.onMap(l, BoxSize.MEDIUM, 5, 6.3f, HorizontalAlign.LEFT,
                            "It may not be the best move, but for the sake of the tutorial, move the remaining Fighter unit to the enemy base to begin capturing it.",
                            ActionListener.perform()),
                    TutorialHighlight.disable(l),
                    BlockingAction.waitFor(ActionListener.complete()),
                    ModifyElements.action(l, true),
                    ModifyElements.forceTileSelect(l, 8, 3),
                    AllowedTiles.only(8, 3),
                    AllowedActions.only(Action.CAPTURE),


                    BlockingTextBox.onMap(l, BoxSize.MEDIUM, 5, 4, HorizontalAlign.LEFT,
                            "Select the unit, and capture. The capture action appears only when the unit is on an enemy structure.",
                            ActionListener.perform()),
                    ModifyElements.disableAll(l),


                    ContinueTextBox.onMap(l, BoxSize.LARGE, 5, 4, HorizontalAlign.LEFT,
                            "It takes " + StructureType.BASE.captureSteps + " turns to capture a base, and on each of those turns you have to perform the capture action; [BLUE]it is not automatic[NO_COLOUR]. " +
                                    "Moving the capturing unit away from the structure will [RED]reset[NO_COLOUR] the progress.\n\n" +
                                    "Players always have visibility over tiles with allied structures, meaning that the enemy can see the capture progress of their structures, even if there are no units nearby with view range over the area."),
                    ContinueTextBox.onMap(l, BoxSize.MEDIUM, 5, 4, HorizontalAlign.LEFT,
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
                    new GameplaySettings(true, true)
            ).addPlayer(PlayerTeam.A, false).addPlayer(PlayerTeam.B, true),
            l -> new TutorialSequenceElement[]{
                    ModifyElements.disableAll(l),
                    CameraMove.toTile(l, 7, 1),
                    ContinueTextBox.onUI(l, BoxSize.LARGE, 11, 18, HorizontalAlign.LEFT,
                            "Welcome to the third tutorial. So far, the only units we've come across have been Fighter units. As you can see, this is no longer the case.\n\n" +
                                    "Each ship belongs to a ship class. There are [BLUE]four different classes[NO_COLOUR]: fighters, corvettes, cruisers and capital ships."),
                    ContinueTextBox.onUI(l, BoxSize.EXTRA_EXTRA_LARGE, 11, 18, HorizontalAlign.LEFT,
                            "[BLUE]Fighters[NO_COLOUR] are small, agile units, which usually have weak weaponry.\n\n" +
                                    "[BLUE]Corvettes[NO_COLOUR] are slightly larger and slower to allow for more powerful weapons, and come in many unique variants.\n\n" +
                                    "Even larger than that, [BLUE]cruiser-class[NO_COLOUR] units often feature heavy armour to protect themselves from smaller units.\n\n" +
                                    "The largest units, [BLUE]capital ships[NO_COLOUR], have the largest guns and strongest armour, often including shields or other specialised equipment."),
                    TutorialHighlight.tile(l, BLUE_HIGHLIGHT, 7, 2),
                    ContinueTextBox.onUI(l, BoxSize.MEDIUM, 11, 18, HorizontalAlign.LEFT,
                            "This [BLUE]Fighter unit[NO_COLOUR][NO_COLOUR][NO_COLOUR] is, of course, a fighter-class unit, but it is not the only one of its kind."),
                    TutorialHighlight.tiles(l, BLUE_HIGHLIGHT, new Point(6, 1), new Point(7, 1)),
                    ContinueTextBox.onUI(l, BoxSize.MEDIUM, 11, 18, HorizontalAlign.LEFT,
                            "These are [BLUE]Bomber units[NO_COLOUR]. They are also fighter-class units, but are slightly weaker and slower than the Fighter unit is.\n\n" +
                                    "They do, however, have a powerful missile weapon that we'll get to later."),
                    TutorialHighlight.tile(l, BLUE_HIGHLIGHT, 6, 0),
                    ModifyElements.tileSelect(l, false),
                    AllowedTiles.only(6, 0),
                    BlockingTextBox.onUI(l, BoxSize.MEDIUM, 11, 18, HorizontalAlign.LEFT,
                            "Finally, this is an [BLUE]Artillery unit[NO_COLOUR]. It is a ranged corvette-class unit. Start by selecting this unit.",
                            TileSelectListener.any()),
                    TutorialHighlight.disable(l),
                    TutorialUI.onUI(l, "viewRange").rectangle(8.25f, 12.25f, 2, 2, GREEN_HIGHLIGHT, TutorialUI.StrokeWidth.NARROW),
                    ModifyElements.viewFiringRange(l, true),
                    BlockingTextBox.onUI(l, BoxSize.MEDIUM, 11, 18, HorizontalAlign.LEFT,
                            "To see the range that this unit has, click the red button highlighted on the panel below.",
                            UIListener.select(UIElement.VIEW_FIRING_RANGE)),
                    TutorialUI.remove("viewRange"),
                    BlockingTextBox.onUI(l, BoxSize.MEDIUM, 11, 18, HorizontalAlign.LEFT,
                            "Highlighted in red you can see the tiles that the selected unit is in [RED]firing range[NO_COLOUR] of. Click anywhere on the screen to exit firing range view.",
                            UIListener.deselect(UIElement.VIEW_FIRING_RANGE)),
                    ModifyElements.disableAll(l),
                    CameraMove.toTile(l, 5, 3),
                    ContinueTextBox.onMap(l, BoxSize.MEDIUM, 8, 4, HorizontalAlign.LEFT,
                            "Now with that out of the way, we can get to moving the units.\n\nYou can see that the enemy base is in the top left corner of the map, outside of our view range."),
                    TutorialHighlight.radius(l, RED_HIGHLIGHT, 5, 4, 1),
                    ModifyElements.enable(l, TILE_DESELECTION),
                    TileSelect.deselect(l),
                    ContinueTextBox.onMap(l, BoxSize.MEDIUM, 8, 4, HorizontalAlign.LEFT,
                            "The enemy will most likely move their units to this region on the next turn. We could hide our units in the nebula below this region to ambush them."),
                    ModifyElements.moveUnit(l, false, false, 7, 2, 5, 2),
                    BlockingTextBox.onMap(l, BoxSize.MEDIUM, 8, 4, HorizontalAlign.LEFT,
                            "Move the Fighter unit to the front of the nebula. It has the longest view range, and by moving it to the front, we can see further out of the nebula.",
                            ActionListener.perform()),
                    TutorialHighlight.disable(l),
                    BlockingAction.waitFor(ActionListener.complete()),
                    TileSelect.deselect(l),
                    ModifyElements.moveUnit(l, false, true, 6, 0, 5, 0),
                    BlockingTextBox.onMap(l, BoxSize.MEDIUM, 8, 4, HorizontalAlign.LEFT,
                            "The Artillery unit, due to its longer range, can be placed further back. Move it to the highlighted tile.",
                            ActionListener.perform()),
                    TutorialHighlight.disable(l),
                    BlockingAction.waitFor(ActionListener.complete()),
                    TileSelect.deselect(l),
                    ModifyElements.moveUnit(l, false, true, new Point[]{new Point(6, 1), new Point(7, 1)}, new Point[]{new Point(4, 1), new Point(4, 0)}),
                    BlockingTextBox.onMap(l, BoxSize.MEDIUM, 1, 0, HorizontalAlign.LEFT,
                            "Finally, move the Bomber units to the highlighted positions.",
                            ActionListener.perform(), ActionListener.perform()),
                    TutorialHighlight.disable(l),
                    BlockingAction.waitFor(ActionListener.complete()),
                    TileSelect.deselect(l),
                    ModifyElements.endTurn(l),
                    BlockingTextBox.onUI(l, BoxSize.SMALL_MEDIUM, 8, Renderable.top() - 8, HorizontalAlign.LEFT,
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
                    ContinueTextBox.onMap(l, BoxSize.MEDIUM, 8, 4, HorizontalAlign.LEFT,
                            "The enemy has deployed Cruisers against us!\n\n" +
                                    "[BLUE]Cruiser units[NO_COLOUR] are the base configuration of the cruiser class, in the same way that Fighter units are the basic variant of the fighter class."),
                    ModifyElements.tileSelect(l, false, 5, 2),
                    BlockingTextBox.onMap(l, BoxSize.MEDIUM, 8, 4, HorizontalAlign.LEFT,
                            "Select the Fighter unit to see what kind damage we can do with it.",
                            TileSelectListener.any()),
                    TutorialHighlight.disable(l),
                    ModifyElements.viewEffectiveness(l, false),
                    TutorialUI.onUI(l, "viewEffectiveness").rectangle(6.25f, 12.25f, 2, 2, GREEN_HIGHLIGHT, TutorialUI.StrokeWidth.NARROW),
                    BlockingTextBox.onUI(l, BoxSize.MEDIUM, 11, 18, HorizontalAlign.LEFT,
                            "To see the effectiveness of the weapons on this unit, click the orange button highlighted below.",
                            UIListener.select(UIElement.VIEW_EFFECTIVENESS)),
                    TutorialUI.remove("viewEffectiveness"),
                    ContinueTextBox.onMap(l, BoxSize.LARGE, 8, 4, HorizontalAlign.LEFT,
                            "Each enemy now has its tile coloured based on weapon effectiveness. This does not always correspond to damage dealt, as it does not take into account unit HP.\n\n" +
                                    "As you know, lower HP means lower damage dealt, but that does not change the base characteristics of the weapons, which is what you're seeing now."),
                    ContinueTextBox.onMap(l, BoxSize.LARGE, 8, 4, HorizontalAlign.LEFT,
                            "It does, however, give us a rough estimate of the damage dealt by a full HP unit, which all of our units currently are.\n\n" +
                                    "Yellow means [YELLOW]low damage[NO_COLOUR] (usually around 1 HP), while red means [RED]high damage[NO_COLOUR] (often more than half the unit's HP). Blue, not seen here, means [BLUE]no damage[NO_COLOUR] at all."),
                    ContinueTextBox.onMap(l, BoxSize.LARGE, 8, 4, HorizontalAlign.LEFT,
                            "You can see by the orange colour that the Fighter can do [ORANGE]moderate damage[NO_COLOUR] to the enemy Fighter which is next to the base.\n\n" +
                                    "When it comes to the Cruisers however, the yellow colour shows that this unit is [YELLOW]almost useless[NO_COLOUR] against them."),
                    ModifyElements.tileSelect(l, false, new Point(4, 1), new Point(4, 0)),
                    ModifyElements.viewEffectiveness(l, true).add(false),
                    BlockingTextBox.onMap(l, BoxSize.MEDIUM, 8, 4, HorizontalAlign.LEFT,
                            "Select one of the Bomber units instead to see what damage they can do.", TileSelectListener.any()),
                    TutorialHighlight.disable(l),
                    ModifyElements.viewEffectiveness(l, false),
                    BlockingTextBox.onMap(l, BoxSize.SMALL_MEDIUM, 8, 4, HorizontalAlign.LEFT,
                            "Click the orange button to view weapon effectiveness.", UIListener.select(UIElement.VIEW_EFFECTIVENESS)),
                    ContinueTextBox.onMap(l, BoxSize.LARGE, 8, 4, HorizontalAlign.LEFT,
                            "As you can see, Bombers perform well not only against the [ORANGE]Fighter unit[NO_COLOUR], but also against the [ORANGE]Cruisers[NO_COLOUR].\n\n" +
                                    "This is because they have two weapons. The first is a plasma gun, effective at destroying fighter-class units. The Fighter we looked " +
                                    "at before has similar gun, which is why both that unit and this unit are effective at destroying the enemy Fighter."),
                    ContinueTextBox.onMap(l, BoxSize.MEDIUM, 8, 4, HorizontalAlign.LEFT,
                            "The Bomber unit also has a missile weapon, which is most powerful when used against [RED]capital ships[NO_COLOUR].\n\n" +
                                    "It does, however, still perform well against [ORANGE]cruiser-class[NO_COLOUR] ships."),
                    ContinueTextBox.onMap(l, BoxSize.EXTRA_LARGE, 8, 4, HorizontalAlign.LEFT,
                            "Each of the four unit classes has a corresponding weapon type that counters it. Here's a list showing which weapon type counters which unit class, in order of unit size:\n\n" +
                                    "Plasma > Fighter-class units\n\n" +
                                    "Cannon > Corvette-class units\n\n" +
                                    "Railgun > Cruiser-class units\n\n" +
                                    "Explosive > Capital ships\n\n"),
                    ContinueTextBox.onMap(l, BoxSize.LARGE, 8, 4, HorizontalAlign.LEFT,
                            "Furthermore, weapons often (but not always) perform moderately well against " +
                                    "enemies one size class up or down from the class that the weapon is most effective against.\n\n" +
                                    "This is why the Bombers' missile weapons can do damage against cruiser-class units, even though explosives " +
                                    "are meant for countering capital ships."),
                    ContinueTextBox.onMap(l, BoxSize.EXTRA_LARGE, 8, 4, HorizontalAlign.LEFT,
                            "Keep in mind that not all weapons of a given type are created equal. Some are stronger than others.\n\n" +
                                    "A weak plasma gun might be [ORANGE]moderately effective[NO_COLOUR] against fighters, and [YELLOW]useless[NO_COLOUR] against other classes\n\nA strong " +
                                    "plasma gun, on the other hand, may be [RED]highly effective[NO_COLOUR] against fighter-class units, [ORANGE]moderately effective[NO_COLOUR] against the corvette class, " +
                                    "and [YELLOW]useless[NO_COLOUR] against any unit larger than a corvette."),
                    ContinueTextBox.onMap(l, BoxSize.LARGE, 8, 4, HorizontalAlign.LEFT,
                            "You don't necessarily have to remember the exact details of the weapon each unit carries, what's important is that " +
                                    "that each unit has one, or sometimes several classes it [RED]counters effectively[NO_COLOUR], and possibly has other classes that it's [ORANGE]moderately effective[NO_COLOUR] against.\n\n" +
                                    "[BLUE]You can, and should, use the orange button to view weapon effectiveness.[NO_COLOUR]"),
                    ModifyElements.tileSelect(l, false, new Point(6, 3), new Point(5, 4)),
                    ModifyElements.viewEffectiveness(l, true).add(false),
                    SequenceTextBox.onMap(l, BoxSize.LARGE, 8, 4, HorizontalAlign.LEFT,
                            "Now, we know that the Bomber is effective against the Cruisers, but we should also check whether the Cruisers are effective " +
                                    "against the Bombers before going to attack.\n\nSelect one of the Cruisers and view its weapon effectiveness.",
                            BlockingAction.waitFor(TileSelectListener.any()),
                            TutorialHighlight.disable(l),
                            BlockingAction.waitFor(UIListener.select(UIElement.VIEW_EFFECTIVENESS))
                    ),
                    ModifyElements.viewEffectiveness(l, false),
                    ContinueTextBox.onMap(l, BoxSize.MEDIUM, 8, 4, HorizontalAlign.LEFT,
                            "We see that the enemy Cruiser is [YELLOW]not effective[NO_COLOUR] against our fighter-class units, meaning that the Bombers can attack without taking much damage in return."),

                    ModifyElements.moveUnit(l, true, true, new Point[]{new Point(4, 0), new Point(4, 1)}, new Point[]{new Point(7, 3)}),
                    ModifyElements.viewEffectiveness(l, true).add(false),
                    SequenceTextBox.onMap(l, BoxSize.MEDIUM, 8, 5, HorizontalAlign.LEFT,
                            "Move a Bomber to this asteroid field and attack.",
                            BlockingAction.waitFor(ActionListener.complete()),
                            TutorialHighlight.disable(l),
                            ModifyElements.forceTileSelect(l, 7, 3),
                            ModifyElements.attack(l, true, 7, 3),
                            BlockingAction.waitFor(ActionListener.perform())
                    ),
                    BlockingAction.waitFor(ActionListener.complete()),
                    CameraMove.toTile(l, 7, 3),
                    SequenceTextBox.onMap(l, BoxSize.LARGE, 8, 4, HorizontalAlign.LEFT,
                            "You can see that the enemy Cruiser took significantly more damage than the Bomber, which came out almost unscathed.\n\n" +
                                    "The Bomber's missile weapon does, however, have one major drawback.\n\n" +
                                    "Select the Bomber, then select the button to view the Bomber's weapon effectiveness.",
                            ModifyElements.tileSelect(l, false, 7, 3),
                            BlockingAction.waitFor(TileSelectListener.any()),
                            TutorialHighlight.disable(l),
                            ModifyElements.viewEffectiveness(l, false),
                            BlockingAction.waitFor(UIListener.select(UIElement.VIEW_EFFECTIVENESS))
                    ),
                    ContinueTextBox.onMap(l, BoxSize.EXTRA_LARGE, 5, 0, HorizontalAlign.LEFT,
                            "You can see that the Cruisers are now coloured yellow, meaning that the Bomber is no longer effective against them.\n\n" +
                                    "This is due to the missile weapon having [BLUE]limited ammo capacity[NO_COLOUR], just one round of ammo in this case.\n\n" +
                                    "Since the missile weapon is out of ammo, attacking the Cruisers now will lead to the Bomber resorting to using its plasma gun, which is much less effective."),
                    TutorialUI.onUI(l, "ammoLabel")
                            .rectangle(1 + 9.4f / 2 + 0.5f / 2, 5 - 1 - 0.5f / 2, 9.4f + 0.5f, 1 + 0.5f, GREEN_HIGHLIGHT, TutorialUI.StrokeWidth.NARROW),
                    ContinueTextBox.onMap(l, BoxSize.EXTRA_LARGE, 5, 0, HorizontalAlign.LEFT,
                            "The remaining ammo can be seen on the panel to the left, where you can see that the ammo is at 0 / 1.\n\n" +
                                    "Units can have at most one weapon that uses ammo, and using the unit's other weapons will not consume any ammo.\n\n" +
                                    "If none of the weapons for a unit consume ammo, the ammo counter will not display a number."),
                    TutorialUI.remove("ammoLabel"),
                    TutorialUI.onUI(l, "unitInfo").rectangle(10.25f, 12.25f, 2, 2, GREEN_HIGHLIGHT, TutorialUI.StrokeWidth.NARROW),
                    ContinueTextBox.onMap(l, BoxSize.EXTRA_LARGE, 5, 0, HorizontalAlign.LEFT,
                            "When attacking an enemy, the most effective weapon not out of ammo will automatically be selected. Attacking a Fighter using " +
                                    "a Bomber, for example, will use the plasma gun, even if the missile is available.\n\n" +
                                    "To view the weapons a unit has, [BLUE]open the unit info screen[NO_COLOUR] using the button in the top right of the panel to the left. " +
                                    "Then, navigate to the weapons tab, and click a weapon to view info about it. There, you can see the range, ammo usage, and effectiveness of " +
                                    "each weapon."),
                    TutorialUI.remove("unitInfo"),
                    ModifyElements.viewEffectiveness(l, true).add(true),
                    ModifyElements.moveUnit(l, true, true, new Point[]{new Point(4, 0), new Point(4, 1)}, new Point[]{new Point(4, 3)}),
                    CameraMove.toTile(l, 5, 3),
                    SequenceTextBox.onMap(l, BoxSize.LARGE, 8, 4, HorizontalAlign.LEFT,
                            "That info screen also contains many other useful things about the selected unit, for example view range, " + ModifierCategory.MOVEMENT_SPEED_DISPLAY.getName().toLowerCase() + " and any special features the unit may have.\n\n" +
                                    "Back to the game, it's time to move the other Bomber that still has ammo to attack. Move it to the highlighted tile, and attack the Cruiser.",
                            BlockingAction.waitFor(ActionListener.complete()),
                            TutorialHighlight.disable(l),
                            ModifyElements.forceTileSelect(l, 4, 3),
                            ModifyElements.attack(l, true, 4, 3),
                            BlockingAction.waitFor(ActionListener.perform())
                    ),
                    BlockingAction.waitFor(ActionListener.complete()),
                    CameraMove.toTile(l, 5, 2),
                    ModifyElements.moveUnit(l, true, true, 5, 2, 6, 0),
                    SequenceTextBox.onMap(l, BoxSize.MEDIUM, 8, 2, HorizontalAlign.LEFT,
                            "The Fighter unit, which isn't of much use against the enemy Cruisers, can be used to defend the base from the enemy Fighter.\n\n" +
                                    "Move the Fighter and attack.",
                            BlockingAction.waitFor(ActionListener.complete()),
                            TutorialHighlight.disable(l),
                            ModifyElements.forceTileSelect(l, 6, 0),
                            ModifyElements.attack(l, true, 6, 0),
                            BlockingAction.waitFor(ActionListener.perform())
                    ),
                    BlockingAction.waitFor(ActionListener.complete()),
                    ContinueTextBox.onMap(l, BoxSize.EXTRA_LARGE, 1.3f, 1.5f, HorizontalAlign.LEFT,
                            "The Artillery unit has a [BLUE]ranged missile weapon[NO_COLOUR], meaning that it is effective against Cruisers, similar to the Bomber unit. Unlike the Bomber unit, it has an " +
                                    "ammo capacity of " + CorvetteType.ARTILLERY.ammoCapacity + ".\n\n" +
                                    "Ranged units have a special property. [BLUE]They do not receive counterattacks[NO_COLOUR] when attacking enemies, even if the enemy is also a ranged unit.\n\n" +
                                    "They are, however, [BLUE]not able to defend themselves[NO_COLOUR] with a counterattack when attacked by an enemy."),
                    ModifyElements.moveUnit(l, true, true, 5, 0, 5, 2),
                    SequenceTextBox.onMap(l, BoxSize.MEDIUM, 1.3f, 1.5f, HorizontalAlign.LEFT,
                            "Move the Artillery into the dense nebula, and attack one of the Cruisers. The dense nebula will " +
                                    "hide the defenceless unit from the enemies next turn.",
                            BlockingAction.waitFor(ActionListener.complete()),
                            TutorialHighlight.disable(l),
                            ModifyElements.forceTileSelect(l, 5, 2),
                            ModifyElements.attack(l, true, 5, 2),
                            AllowedActionTiles.only(Action.FIRE, new Point(5, 4), new Point(6, 3)),
                            BlockingAction.waitFor(ActionListener.perform())
                    ),
                    BlockingAction.waitFor(ActionListener.complete()),
                    ModifyElements.endTurn(l),
                    BlockingTextBox.onUI(l, BoxSize.SMALL_MEDIUM, 8, Renderable.top() - 8, HorizontalAlign.LEFT,
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


                    ContinueTextBox.onMap(l, BoxSize.MEDIUM, 1, 3, HorizontalAlign.LEFT,
                            "As you saw during the enemy's turn, the Bomber units were not able to do much damage to the Cruisers when they counterattacked. " +
                                    "We need to be able to [RESUPPLY]resupply[NO_COLOUR] the Bombers."),
                    ContinueTextBox.onMap(l, BoxSize.LARGE, 1, 3, HorizontalAlign.LEFT,
                            "There are several ways to replenish ammo, but for this tutorial, we'll only cover one of them.\n\n" +
                                    "At the start of your turn, if a unit is on the [BLUE]same tile as an allied base structure[NO_COLOUR], it gets all its ammo resupplied.\n\n" +
                                    "Not only that, it also regains some HP."),
                    ModifyElements.moveUnit(l, true, true, new Point[]{new Point(4, 3), new Point(7, 3)}, new Point[]{new Point(8, 1)}),
                    BlockingTextBox.onMap(l, BoxSize.MEDIUM, 1, 3, HorizontalAlign.LEFT,
                            "Move one of the Bombers to the base so that it gets resupplied at the start of the next turn.",
                            ActionListener.perform()),
                    TutorialHighlight.disable(l),
                    BlockingAction.waitFor(ActionListener.complete()),
                    ContinueTextBox.onMap(l, BoxSize.MEDIUM, 4, 2, HorizontalAlign.LEFT,
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
