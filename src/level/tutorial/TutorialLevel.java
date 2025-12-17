package level.tutorial;

import foundation.NamedEnum;
import foundation.math.MathUtil;
import level.GameplaySettings;
import level.Level;
import level.PlayerTeam;
import level.energy.EnergyManager;
import level.structure.StructureType;
import level.tile.TileSet;
import level.tile.TileType;
import level.tutorial.sequence.BoxSize;
import level.tutorial.sequence.action.*;
import level.tutorial.sequence.event.*;
import level.tutorial.sequence.predicates.SelectedUnit;
import level.tutorial.sequence.predicates.TileState;
import level.tutorial.sequence.predicates.UnitState;
import render.HorizontalAlign;
import render.Renderable;
import render.UIColourTheme;
import render.VerticalAlign;
import render.anim.sequence.KeyframeFunction;
import render.level.info.UIUnitInfo;
import render.types.box.UIDisplayBox;
import render.types.box.display.tutorial.*;
import render.types.text.MultiLineTextBox;
import render.types.text.TextRenderer;
import render.types.tutorial.TutorialScreen;
import unit.ShipClass;
import unit.UnitTeam;
import unit.action.Action;
import unit.action.ActionIconType;
import unit.bot.BotActionData;
import unit.stats.modifiers.types.ModifierCategory;
import unit.type.CorvetteType;
import unit.type.CruiserType;
import unit.type.FighterType;
import unit.weapon.WeaponEffectiveness;

import java.awt.*;
import java.util.function.Function;

import static level.tutorial.TutorialElement.*;
import static level.tutorial.TutorialManager.*;
import static render.types.text.StyleElement.*;

public enum TutorialLevel implements NamedEnum {
    INTRODUCTION("Introduction", "tutorial-intro",
            new TutorialLevelData(
                    new GameplaySettings(false, true)
            ).addPlayer(PlayerTeam.A, false).addPlayer(PlayerTeam.B, true),
            l -> new TutorialSequenceElement[]{
                    new TutorialAction(() -> {
                        l.levelRenderer.energyManager.setEnabled(false);
                        l.levelRenderer.mapButton.setEnabled(false);
                        l.levelRenderer.turnBox.setEnabled(false);
                        l.levelRenderer.endTurn.setEnabled(false);
                        l.levelRenderer.uiUnitInfo.viewFiringRange.setEnabled(false);
                        l.levelRenderer.uiUnitInfo.viewEffectiveness.setEnabled(false);
                    }),
                    ModifyElements.disable(l, END_TURN, TILE_SELECTION, ACTIONS, TUTORIAL_INCOME_DISABLED),
                    CameraMove.toTile(l, 1, 4),
                    TutorialHighlight.tile(l, BLUE_HIGHLIGHT, 1, 4),
                    ContinueTextBox.onMap(l, BoxSize.MEDIUM, 2, 4.7f, HorizontalAlign.LEFT,
                            "This is your [BLUE]Base[NO_COLOUR], which serves as the starting point for your units."),

                    CameraMove.toTile(l, 2, 4),
                    TutorialHighlight.tiles(l, BLUE_HIGHLIGHT, new Point(1, 5), new Point(2, 4)),
                    ContinueTextBox.onMap(l, BoxSize.MEDIUM, 1.6f, 6.4f, HorizontalAlign.LEFT,
                            "These are your [BLUE]units[NO_COLOUR]. For this tutorial, you've been provided with two [BLUE]" + FighterType.INTERCEPTOR.getPluralName() + "[NO_COLOUR]."),

                    ModifyElements.enable(l, TILE_SELECTION),
                    TutorialHighlight.disable(l),
                    BlockingTextBox.onMap(l, BoxSize.SMALL, 1.5f, 6f, HorizontalAlign.LEFT,
                            "[BLUE]Select[NO_COLOUR] one of the " + FighterType.INTERCEPTOR.getPluralName() + " by [BLUE]left-clicking it[NO_COLOUR].",
                            UnitSelectListener.ofTeam(UnitTeam.BLUE)),

                    ContinueTextBox.onMap(l, BoxSize.MEDIUM, 1.6f, 6.4f, HorizontalAlign.LEFT,
                            "Below the unit you can see [BLUE]two icons[NO_COLOUR]. These are the [BLUE]actions[NO_COLOUR] that the unit is able to perform."),

                    TutorialScreen.create(l, TutorialScreen.NORMAL_WIDTH, (box, level) -> {
                        box.setWidthMargin(0.5f);
                        box.addParagraph("Moving Units",
                                        "Each unit can be moved once per turn by using the " + Action.MOVE.colouredName(NO_COLOUR, true) + " action.")
                                .mainHeader().defaultBottomSpacing().finalise();
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
                        String maxMovement = MathUtil.floatToString(FighterType.INTERCEPTOR.maxMovement);
                        box.addParagraph(ModifierCategory.MOVEMENT_SPEED_DISPLAY.getName(), "The " + c + "blue highlight[NO_COLOUR] that appears when the action is " +
                                        "selected shows the tiles that are in " + c + "move range[NO_COLOUR]. " +
                                        "Each unit has a maximum " + ModifierCategory.MOVEMENT_SPEED_DISPLAY.colouredName(NO_COLOUR, true) + ", and each tile the unit moves over " +
                                        "has " + ModifierCategory.MOVEMENT_COST_DISPLAY.getArticle().toLowerCase() + ModifierCategory.MOVEMENT_COST_DISPLAY.colouredName(NO_COLOUR, true) + " that depends on the type of tile.\n\n" +

                                        "The " + FighterType.INTERCEPTOR.getName() + " unit used in this example has a maximum " + ModifierCategory.MOVEMENT_SPEED_DISPLAY.getName().toLowerCase() + " of " +
                                        c + maxMovement + ModifierCategory.MOVEMENT_SPEED_DISPLAY.icon() + "[NO_COLOUR].")
                                .defaultBottomSpacing().finalise();
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
                                " tiles[NO_COLOUR], which have " + ModifierCategory.MOVEMENT_COST_DISPLAY.getNameArticle().toLowerCase() + " of " + c + "just " +
                                MathUtil.floatToString(TileType.EMPTY.moveCost) + ModifierCategory.MOVEMENT_SPEED_DISPLAY.icon() + "[NO_COLOUR], while in the second example, the unit is moving through " +
                                c + TileType.NEBULA.getName().toLowerCase() +
                                " tiles[NO_COLOUR], which have a " + c + "higher " + ModifierCategory.MOVEMENT_COST_DISPLAY.getName().toLowerCase() + " of " +
                                MathUtil.floatToString(TileType.NEBULA.moveCost) + ModifierCategory.MOVEMENT_SPEED_DISPLAY.icon() + "[NO_COLOUR].\n\n" +

                                "The " + c + ModifierCategory.MOVEMENT_COST_DISPLAY.getName().toLowerCase() + "[NO_COLOUR] of a given tile can be found by using the " + c + "tile info screen[NO_COLOUR] in the " + c + "bottom right corner[NO_COLOUR] whenever a tile is selected.\n\n" +

                                c + "Note:[NO_COLOUR] the tile which the unit is currently standing on " + c + "does not count[NO_COLOUR] toward the final " +
                                ModifierCategory.MOVEMENT_COST_DISPLAY.getName().toLowerCase() + "."
                        );

                        box.addParagraph("Movement Modifiers", "When moving a unit, certain " + c + "modifiers[NO_COLOUR] may come into play that affect the " + ModifierCategory.MOVEMENT_COST_DISPLAY.getName().toLowerCase() +
                                        " of different tile types. By default, " + c + "each type of unit[NO_COLOUR] has a modifier that affects how they move through " + c + TileType.ASTEROIDS.getName().toLowerCase() +
                                        " tiles[NO_COLOUR], depending on the size of the unit.\n\n" +
                                        "The modifiers in effect are prominently displayed for the " + c + "active unit[NO_COLOUR] while the " + c + Action.MOVE.getName().toLowerCase() + " action[NO_COLOUR] is selected.")
                                .setTopSpacing(2).finalise();
                    }),
                    ModifyElements.enable(l, ACTIONS),
                    BlockingTextBox.onMap(l, BoxSize.MEDIUM, 2f, 5.7f, HorizontalAlign.LEFT,
                            "Try moving one of your units",
                            ActionListener.perform(Action.MOVE)),
                    ModifyElements.disable(l, ACTIONS),
                    BlockingTextBox.onUI(l, BoxSize.MEDIUM, 20, 20, HorizontalAlign.LEFT,
                            "Great! Now that you can move units, it's also important to be able to move the camera. " +
                                    "[BLUE]Right-click + drag[NO_COLOUR], or alternatively, move the mouse to the edge of the screen to move the camera. " +
                                    "Try moving the camera around the level.", CameraMoveListener.distance(15)),
                    ContinueTextBox.onUI(l, BoxSize.MEDIUM, 20, 20, HorizontalAlign.LEFT,
                            "Great! Now that you can move units, it's also important to be able to move the camera. " +
                                    "[BLUE]Right-click + drag[NO_COLOUR], or alternatively, move the mouse to the edge of the screen to move the camera. " +
                                    "Try moving the camera around the level."),
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
                        box.addParagraph("Attacking Enemy Units", "Just like with the " + Action.MOVE.getName().toLowerCase() + " action, [BLUE]each unit[NO_COLOUR] can attack [BLUE]once per turn[NO_COLOUR] by using the " + Action.FIRE.colouredName(NO_COLOUR, true) + " action.\n\n" +
                                        "In general, each unit can use each of its actions once per turn, in whichever order you choose.")
                                .mainHeader().defaultBottomSpacing().finalise();
                        box.addTutorialMap(HorizontalAlign.LEFT, 20, 10, TutorialMapElement.TILE_SIZE_MEDIUM, 14.5f, 0, map -> {
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
                        box.addParagraph("Unit HP", "Each unit has a number in the [BLUE]bottom-right[NO_COLOUR] corner of its tile. " +
                                        "This is its [BLUE]remaining HP[NO_COLOUR]. Note that it is always [BLUE]rounded up[NO_COLOUR], and that a more precise value is available in each unit's [BLUE]info screen[NO_COLOUR].\n\n" +
                                        "In the example above, you can see the [BLUE]damage taken[NO_COLOUR] by each unit from the attack with the [RED]damage indicators[NO_COLOUR] that appear next to the HP number.")
                                .finalise();
                        box.addParagraph("Dealing Damage", "There are a [BLUE]few factors[NO_COLOUR] that influence the damage dealt to an enemy unit.\n\n" +
                                        "Firstly, the [RED]base weapon damage[NO_COLOUR]. This depends on which [BLUE]type of unit[NO_COLOUR] is attacking, and can be seen in the [BLUE]unit info screen[NO_COLOUR]. " +
                                        FighterType.INTERCEPTOR.getPluralName() + " have a base damage of [RED]" + MathUtil.floatToString(FighterType.INTERCEPTOR.damage) + ModifierCategory.DAMAGE.icon() + "[NO_COLOUR].\n\n" +
                                        "Secondly, a unit with [BLUE]less HP remaining[NO_COLOUR] will deal [RED]less damage[NO_COLOUR], depending on how much HP it has lost. A [BLUE]severely damaged[NO_COLOUR] unit may do as little as " +
                                        "[BLUE]25%[NO_COLOUR] of its normal damage.")
                                .setTopSpacing(1.5f).defaultBottomSpacing().finalise();
                        box.addTutorialMap(HorizontalAlign.LEFT, 14.25f, 10, TutorialMapElement.TILE_SIZE_MEDIUM, 10, 0, map -> {
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
                        box.addTutorialMap(HorizontalAlign.RIGHT, 14.25f, 10, TutorialMapElement.TILE_SIZE_MEDIUM, 10, 2, map -> {
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
                        box.addParagraph("Modifiers", "Similar to [BLUE]movement modifiers[NO_COLOUR], attacks can also have [BLUE]modifiers[NO_COLOUR]. The " +
                                        "[BLUE]main ones[NO_COLOUR] are the [BLUE]" + ModifierCategory.WEAPON_EFFECTIVENESS.getName().toLowerCase() + " modifier[NO_COLOUR] (more on that later), and the [BLUE]tile modifier[NO_COLOUR].\n\n" +
                                        "Different [BLUE]tile types[NO_COLOUR] have different modifiers, typically [BLUE]reducing[NO_COLOUR] the damage received by the unit standing on it.\n\n" +
                                        "The effect of the tile modifier can be seen in the [RED]" + ModifierCategory.INCOMING_DAMAGE.getName() + "[NO_COLOUR] box in the [BLUE]tile info screen[NO_COLOUR].")
                                .defaultBottomSpacing().finalise();
                        box.addTutorialMap(HorizontalAlign.LEFT, 14.25f, 10, TutorialMapElement.TILE_SIZE_MEDIUM, 10, 0, map -> {
                            map.addTile(-1, 0, TileType.EMPTY);
                            map.addTile(-1, 1, TileType.DENSE_NEBULA);
                            map.addTile(-1, -1, TileType.EMPTY);
                            map.addTile(0, 0, TileType.EMPTY).addUnit(FighterType.INTERCEPTOR, UnitTeam.BLUE).onReset(TutorialMapUnit::restoreHP);
                            map.addTile(0, 1, TileType.NEBULA);
                            map.addTile(0, -1, TileType.EMPTY);
                            map.addTile(1, 0, TileType.EMPTY).addUnit(FighterType.INTERCEPTOR, UnitTeam.RED).onReset(TutorialMapUnit::restoreHP);
                            map.addText(new TutorialMapText(map, 14.25f / 2, 9, new TextRenderer("The enemy on " + TileType.EMPTY.getArticle().toLowerCase() + "[BLUE]" + TileType.EMPTY.getName().toLowerCase() + " tile[NO_COLOUR] receives [GREEN]full damage[NO_COLOUR]", 0.55f).setTextAlign(HorizontalAlign.CENTER).setBold(true), TutorialMapText::doNothing, TutorialMapText::doNothing));
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
                        box.addTutorialMap(HorizontalAlign.RIGHT, 14.25f, 10, TutorialMapElement.TILE_SIZE_MEDIUM, 10, 3, map -> {
                            map.addTile(-1, 0, TileType.EMPTY);
                            map.addTile(-1, 1, TileType.DENSE_NEBULA);
                            map.addTile(-1, -1, TileType.EMPTY);
                            map.addTile(0, 0, TileType.EMPTY).addUnit(FighterType.INTERCEPTOR, UnitTeam.BLUE).onReset(TutorialMapUnit::restoreHP);
                            map.addTile(0, 1, TileType.NEBULA);
                            map.addTile(0, -1, TileType.EMPTY);
                            map.addTile(1, 0, TileType.NEBULA).addUnit(FighterType.INTERCEPTOR, UnitTeam.RED).onReset(TutorialMapUnit::restoreHP);
                            map.addText(new TutorialMapText(map, 14.25f / 2, 9, new TextRenderer("The enemy on " + TileType.NEBULA.getArticle().toLowerCase() + "[BLUE]" + TileType.NEBULA.getName().toLowerCase() + " tile[NO_COLOUR] receives [RED]reduced damage[NO_COLOUR]", 0.5f).setTextAlign(HorizontalAlign.CENTER).setBold(true), TutorialMapText::doNothing, TutorialMapText::doNothing));
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
                        box.addParagraph("Counterattacks", "In all the examples above, it is [BLUE]not only the enemy[NO_COLOUR] that takes damage, but also the [BLUE]allied unit[NO_COLOUR]. Whenever attacking, the " +
                                        "enemy will retaliate with a [RED]counterattack[NO_COLOUR].\n\nThe only difference between the initial attack and the counterattack is the [BLUE]order they are calculated[NO_COLOUR].\n\n" +
                                        "The counterattack is performed [BLUE]after[NO_COLOUR] the enemy unit has [BLUE]taken damage[NO_COLOUR] from the initial attack and has been [BLUE]weakened[NO_COLOUR]. You therefore gain an [BLUE]advantage[NO_COLOUR] by attacking first.\n\n" +
                                        "When counterattacking, the enemy unit will use its [BLUE]own weapons[NO_COLOUR] and [BLUE]damage modifiers[NO_COLOUR], so be careful with which enemies you choose to engage. " +
                                        "Positioning [BLUE]allied units[NO_COLOUR] on tiles which reduce [RED]" + ModifierCategory.INCOMING_DAMAGE.getName().toLowerCase() + "[NO_COLOUR] is generally a good idea.")
                                .finalise();
                    }),
                    ModifyElements.enable(l, CAMERA_MOVEMENT, TILE_SELECTION, TILE_DESELECTION, ACTIONS),
                    AllowedActionTiles.only(Action.MOVE, TileSet.tilesInRadius(new Point(6, 5), 1, 1, null).toArray(new Point[0])),
                    BranchTextBox.onMap(l, BoxSize.MEDIUM, 4, 6.5f, HorizontalAlign.LEFT,
                            "Send " + FighterType.INTERCEPTOR.getNameArticle().toLowerCase() + " to attack the enemy unit.", ActionListener.tileSelectAnyExcept(Action.MOVE, TileSet.tilesInRadius(new Point(6, 5), 1, 1, null).toArray(new Point[0])), ActionListener.perform(Action.FIRE),
                            TutorialHighlight.tiles(l, GREEN_HIGHLIGHT, TileSet.tilesInRadius(new Point(6, 5), 1, 1, null).toArray(new Point[0])),
                            TileSelect.deselectAction(l),
                            BranchTextBox.onMap(l, BoxSize.MEDIUM, 4, 6.5f, HorizontalAlign.LEFT, "Send " + FighterType.INTERCEPTOR.getArticle().toLowerCase() + FighterType.INTERCEPTOR.getName() + " to attack the enemy unit.\n\n" +
                                            "The unit must be moved to a tile [BLUE]adjacent to the enemy unit[NO_COLOUR] before it can attack.", ActionListener.perform(Action.MOVE), ActionListener.perform(Action.FIRE),
                                    TutorialHighlight.disable(l), BlockingTextBox.onMap(l, BoxSize.MEDIUM, 4, 6.5f, HorizontalAlign.LEFT, "Send " + FighterType.INTERCEPTOR.getArticle().toLowerCase() + FighterType.INTERCEPTOR.getName() + " to attack the enemy unit.\n\n" +
                                            "The unit must be moved to a tile [BLUE]adjacent to the enemy unit[NO_COLOUR] before it can attack.", ActionListener.perform(Action.FIRE)))),
                    TutorialHighlight.disable(l),
                    AllowedActionTiles.all(),
                    BlockingAction.waitFor(ActionListener.complete(Action.FIRE)),

                    new TutorialAction(() -> {
                        l.levelRenderer.turnBox.setEnabled(true);
                        l.levelRenderer.endTurn.setEnabled(true);
                    }),
                    ModifyElements.enable(l, END_TURN),
                    BlockingTextBox.onUI(l, BoxSize.MEDIUM, 7, Renderable.top() - 10, HorizontalAlign.LEFT,
                            "[BLUE]End the turn[NO_COLOUR] once all the actions you wanted to perform have been completed.",
                            TurnListener.start()),
                    ContinueTextBox.onUI(l, BoxSize.MEDIUM, 7, Renderable.top() - 10, HorizontalAlign.LEFT,
                            "That's the end of this tutorial. It's now up to you to win by destroying all the enemy units."),
                    new EndTutorial()
            }),
    FOW("Unit Types", "tutorial-unit",
            new TutorialLevelData(
                    new GameplaySettings(true, true)
            ).addPlayer(PlayerTeam.A, false).addPlayer(PlayerTeam.B, true),
            l -> new TutorialSequenceElement[]{
                    new TutorialAction(() -> {
                        l.levelRenderer.energyManager.setEnabled(false);
                        l.levelRenderer.mapButton.setEnabled(false);
                    }),
                    ModifyElements.disable(l, TILE_SELECTION, CAMERA_MOVEMENT, ACTIONS, END_TURN, TUTORIAL_INCOME_DISABLED),
                    ContinueTextBox.onMap(l, BoxSize.MEDIUM, -3, 5, HorizontalAlign.LEFT,
                            "Welcome to the [BLUE]second tutorial[NO_COLOUR], about unit types and fog of war."),
                    ContinueTextBox.onMap(l, BoxSize.MEDIUM, -3, 5, HorizontalAlign.LEFT,
                            "As you can quite clearly see, many of the tiles are greyed out this time. That's because of the [BLUE]fog of war[NO_COLOUR] setting, which is enabled by default."),

                    TutorialScreen.create(l, TutorialScreen.NORMAL_WIDTH, (box, level) -> {
                        box.setWidthMargin(0.5f);
                        box.addParagraph("Fog of War", "When the fog of war setting is [BLUE]enabled[NO_COLOUR], tiles that aren't [BLUE]close to your units[NO_COLOUR] will be darkened. " +
                                        "[BLUE]Enemy units[NO_COLOUR] on such tiles are [BLUE]hidden[NO_COLOUR].")
                                .mainHeader().defaultBottomSpacing().finalise();
                        box.addTutorialMap(HorizontalAlign.CENTER, 25, 10, TutorialMapElement.TILE_SIZE_MEDIUM, 7, 0, map -> {
                            map.addTile(-3, -1, TileType.ASTEROIDS);
                            map.addTile(-3, 0, TileType.EMPTY).addUnit(FighterType.INTERCEPTOR, UnitTeam.BLUE);
                            map.addTile(-3, 1, TileType.EMPTY);
                            map.addTile(-2, -1, TileType.ASTEROIDS);
                            map.addTile(-2, 0, TileType.EMPTY);
                            map.addTile(-2, 1, TileType.EMPTY);
                            map.addTile(-1, -1, TileType.EMPTY);
                            map.addTile(-1, 0, TileType.EMPTY);
                            map.addTile(-1, 1, TileType.EMPTY);
                            map.addTile(0, -1, TileType.EMPTY);
                            map.addTile(0, 0, TileType.EMPTY);
                            map.addTile(0, 1, TileType.EMPTY);
                            map.addTile(1, -1, TileType.EMPTY);
                            map.addTile(1, 0, TileType.EMPTY);
                            map.addTile(1, 1, TileType.DENSE_NEBULA);
                            map.addTile(2, -1, TileType.EMPTY);
                            map.addTile(2, 0, TileType.NEBULA);
                            map.addTile(2, 1, TileType.NEBULA).addUnit(FighterType.INTERCEPTOR, UnitTeam.RED);
                            map.addTile(3, 0, TileType.NEBULA);
                            map.enableFoW(UnitTeam.BLUE);
                            map.addMouseParticle(
                                    TutorialMouseKeyframe.tile(0, -3, 0, 0.2f, 0.15f, KeyframeFunction.lerp(), KeyframeFunction.lerp())
                                            .setOnClick(1, () -> map.setSelectedTile(-3, 0)),
                                    TutorialMouseKeyframe.delayUntil(1.5f),
                                    TutorialMouseKeyframe.actionSelector(2, -3, 0, -0.04f, 0.03f, 2, 1, KeyframeFunction.lerp(), KeyframeFunction.lerp())
                                            .setOnClick(0.3f, () -> map.selectMoveAction(-3, 0)),
                                    TutorialMouseKeyframe.delayUntil(2.7f, KeyframeFunction.pow(1.3f), KeyframeFunction.pow(0.7f)),
                                    TutorialMouseKeyframe.tile(4, 2, 0, -0.15f, 0.2f, KeyframeFunction.lerp(), KeyframeFunction.lerp())
                                            .setOnClick(0.3f, map::moveUnit)
                            );
                            map.finalise();
                        });
                        box.addParagraph(ModifierCategory.VIEW_RANGE.getName(), "Each unit type has a different " + ModifierCategory.VIEW_RANGE.colouredName(NO_COLOUR, true) +
                                        ", which determines how far it can [BLUE]reveal tiles[NO_COLOUR].\n\n" +
                                        "Some tiles are [BLUE]harder to reveal[NO_COLOUR], which depends on the tiles' " + ModifierCategory.CONCEALMENT.colouredName(NO_COLOUR, true) + " value. This determines how much " +
                                        ModifierCategory.VIEW_RANGE.colouredName(NO_COLOUR, true) + " is [BLUE]blocked[NO_COLOUR] by the tile.\n\n" +
                                        "A unit's " + ModifierCategory.VIEW_RANGE.getName().toLowerCase() + " value can be found in the [GREEN]Hull[NO_COLOUR] tab of the [BLUE]unit info screen[NO_COLOUR] in the bottom left, after the unit has been selected. " +
                                        "A tile's " + ModifierCategory.CONCEALMENT.getName().toLowerCase() + " value can be found in the [BLUE]tile info screen[NO_COLOUR] in the bottom right whenever a tile is selected.")
                                .finalise();
                        box.addParagraph("Example", FighterType.INTERCEPTOR.getNameArticle(false) +
                                        " has " + ModifierCategory.VIEW_RANGE.getNameArticle().toLowerCase() + " of " + ModifierCategory.VIEW_RANGE.colour() + MathUtil.floatToString(FighterType.INTERCEPTOR.maxViewRange) + ModifierCategory.VIEW_RANGE.icon() + "[NO_COLOUR], and " +
                                        TileType.EMPTY.getNameArticle().toLowerCase() + " tile has " + ModifierCategory.CONCEALMENT.getNameArticle().toLowerCase() + " of " + ModifierCategory.CONCEALMENT.colour() + MathUtil.floatToString(TileType.EMPTY.concealment) + ModifierCategory.CONCEALMENT.icon() + "[NO_COLOUR].\n\n" +
                                        "This means that the " + FighterType.INTERCEPTOR.getName() + " can reveal [BLUE]up to " + (int) (FighterType.INTERCEPTOR.maxViewRange / TileType.EMPTY.concealment) + " " + TileType.EMPTY.getName().toLowerCase() +
                                        " tiles[NO_COLOUR] in front of it, with the next tile being enough to block all the " + ModifierCategory.VIEW_RANGE.getName().toLowerCase() + ".")
                                .finalise();
                        box.addSpace(0.5f, 0);
                        box.addTutorialMap(HorizontalAlign.CENTER, 23, 7, TutorialMapElement.TILE_SIZE_LARGE, 0, 0, map -> {
                            map.addTile(-2, 0, TileType.EMPTY).addUnit(FighterType.INTERCEPTOR, UnitTeam.BLUE);
                            map.addTile(-1, 0, TileType.EMPTY);
                            map.addTile(0, 0, TileType.EMPTY);
                            map.addTile(1, 0, TileType.EMPTY);
                            map.addTile(2, 0, TileType.EMPTY);
                            map.enableFoW(UnitTeam.BLUE);
                            map.finalise();
                        });
                        box.addSpace(1f, 0);
                        box.addText(0.7f, HorizontalAlign.LEFT, "With [BLUE]" + TileType.NEBULA.getName().toLowerCase() + " tiles[NO_COLOUR], that have an increased " + ModifierCategory.CONCEALMENT.getName().toLowerCase() + " of " +
                                ModifierCategory.CONCEALMENT.colour() + MathUtil.floatToString(TileType.NEBULA.concealment) + ModifierCategory.CONCEALMENT.icon() + "[NO_COLOUR], the unit can [BLUE]only " +
                                "reveal " + (int) (FighterType.INTERCEPTOR.maxViewRange / TileType.NEBULA.concealment) + " tiles[NO_COLOUR] ahead.");
                        box.addSpace(0.5f, 0);
                        box.addTutorialMap(HorizontalAlign.CENTER, 23, 7, TutorialMapElement.TILE_SIZE_LARGE, 0, 0, map -> {
                            map.addTile(-2, 0, TileType.NEBULA).addUnit(FighterType.INTERCEPTOR, UnitTeam.BLUE);
                            map.addTile(-1, 0, TileType.NEBULA);
                            map.addTile(0, 0, TileType.NEBULA);
                            map.addTile(1, 0, TileType.NEBULA);
                            map.addTile(2, 0, TileType.NEBULA);
                            map.enableFoW(UnitTeam.BLUE);
                            map.finalise();
                        });
                        box.addSpace(1f, 0);
                        box.addText(0.7f, HorizontalAlign.LEFT, "[BLUE]" + TileType.DENSE_NEBULA.getNameFirstUpper() + " tiles[NO_COLOUR] have a special property: they can " +
                                "only be revealed by [BLUE]directly adjacent units[NO_COLOUR], no matter the " + ModifierCategory.VIEW_RANGE.colouredName(NO_COLOUR, true) + " of other nearby units.");
                        box.addSpace(0.5f, 0);
                        box.addTutorialMap(HorizontalAlign.CENTER, 23, 7, TutorialMapElement.TILE_SIZE_LARGE, 0, 0, map -> {
                            map.addTile(-2, 0, TileType.DENSE_NEBULA).addUnit(FighterType.INTERCEPTOR, UnitTeam.BLUE);
                            map.addTile(-1, 0, TileType.DENSE_NEBULA);
                            map.addTile(0, 0, TileType.DENSE_NEBULA);
                            map.addTile(1, 0, TileType.DENSE_NEBULA);
                            map.addTile(2, 0, TileType.DENSE_NEBULA);
                            map.enableFoW(UnitTeam.BLUE);
                            map.finalise();
                        });
                    }),

                    TutorialHighlight.visibleTiles(l, GREEN_HIGHLIGHT),
                    ContinueTextBox.onMap(l, BoxSize.MEDIUM, -3, 5, HorizontalAlign.LEFT,
                            "The highlighted tiles are currently being [BLUE]revealed[NO_COLOUR] by the allied units. Each unit has a " + ModifierCategory.VIEW_RANGE.colouredName(NO_COLOUR, true) + " in which tiles are visible.\n\n" +
                                    "Tiles not visible will [BLUE]hide the positions[NO_COLOUR] of enemy units."),

                    TutorialHighlight.fowTiles(l, RED_HIGHLIGHT),
                    ContinueTextBox.onMap(l, BoxSize.MEDIUM, -3, 5, HorizontalAlign.LEFT,
                            "That means that there could be enemy units on any of these tiles that aren't visible.\n\nBefore you can attack, you'll need to [BLUE]find the enemy units[NO_COLOUR]."),

                    TutorialHighlight.radius(l, RED_HIGHLIGHT, 9, 3, 1),
                    CameraMove.toTile(l, 8, 3),
                    ContinueTextBox.onMap(l, BoxSize.LARGE, 5, 5, HorizontalAlign.LEFT,
                            "We do, however, know where the enemy base is, as [BLUE]structures[NO_COLOUR] are always visible regardless of fog of war.\n\n" +
                                    "We also know that units spawn close to the base, so we can expect the enemy units to be somewhere in the highlighted area."),

                    TutorialHighlight.disable(l),
                    CameraMove.toTile(l, 2, 5),
                    ModifyElements.enable(l, CAMERA_MOVEMENT, TILE_SELECTION, ACTIONS),
                    BlockingTextBox.onMap(l, BoxSize.LARGE, 5, 7, HorizontalAlign.LEFT,
                            "Move one of your units [BLUE]close to the enemy base[NO_COLOUR] to find the enemy units.",
                            ActionListener.perform(Action.MOVE)),
                    BlockingAction.waitFor(ActionListener.complete(Action.MOVE)),
                    BranchAction.skipIfBranch(l,
                            TileState.any(
                                    TileState.hasUnit(UnitState.ofTeam(UnitTeam.RED))
                                            .and(TileState.tileVisible())),
                            SubSequence.sequence(
                                    ModifyElements.disable(l, BOT),
                                    ModifyElements.enable(l, END_TURN),
                                    BlockingTextBox.onMap(l, BoxSize.LARGE, 5, 7, HorizontalAlign.LEFT,
                                            "Try moving a different unit. The unit must be moved [BLUE]close enough[NO_COLOUR] to see the enemy units.\n\n" +
                                                    "You may [BLUE]end the turn[NO_COLOUR] if you run out of moves, allowing you to continue on the next turn.",
                                            TileFoWListener.withUnit(l, UnitState.exists().and(UnitState.ofTeam(UnitTeam.RED))))
                            )
                    ),
                    ModifyElements.enable(l, BOT),
                    ModifyElements.disable(l, END_TURN, ACTIONS),
                    ContinueTextBox.onMap(l, BoxSize.LARGE, 5, 7, HorizontalAlign.LEFT,
                            "Instead of using " + FighterType.INTERCEPTOR.getPluralName() + ", this enemy has deployed [BLUE]" +
                                    CorvetteType.FRIGATE.getPluralName() + "[NO_COLOUR] against you."),

                    TutorialScreen.create(l, TutorialScreen.NORMAL_WIDTH, (box, level) -> {
                        box.setWidthMargin(0.5f);
                        box.addParagraph("Unit Classes",
                                        "While there are many different types of units, each type belongs to [BLUE]one of four classes[NO_COLOUR]. Each class has a [BLUE]unique icon[NO_COLOUR].")
                                .mainHeader().defaultBottomSpacing().finalise();
                        UIDisplayBox classes = new UIDisplayBox(0, 0, 5, 15, b -> b.setColourTheme(UIColourTheme.LIGHT_BLUE_BOX), false);
                        classes.addText(2, HorizontalAlign.CENTER, ShipClass.FIGHTER.icon.display);
                        classes.addSpace(0.2f, 0);
                        classes.addText(0.7f, HorizontalAlign.CENTER, ShipClass.FIGHTER.getName());
                        classes.addSpace(1.0f, 0);
                        classes.addText(2, HorizontalAlign.CENTER, ShipClass.CORVETTE.icon.display);
                        classes.addSpace(0.2f, 0);
                        classes.addText(0.7f, HorizontalAlign.CENTER, ShipClass.CORVETTE.getName());
                        classes.addSpace(1.0f, 0);
                        classes.addText(2, HorizontalAlign.CENTER, ShipClass.CRUISER.icon.display);
                        classes.addSpace(0.2f, 0);
                        classes.addText(0.7f, HorizontalAlign.CENTER, ShipClass.CRUISER.getName());
                        classes.addSpace(0.5f, 0);
                        box.addBox(classes, HorizontalAlign.LEFT, 0, false);
                        int boxIndex = box.getLastIndex(0);
                        box.addParagraph(null, "On the left you can see the four classes, from [BLUE]smallest to largest[NO_COLOUR].\n\n" +
                                        "Smaller units typically have " +
                                        "[GREEN]increased " + ModifierCategory.MOVEMENT_SPEED_DISPLAY.getName().toLowerCase() + "[NO_COLOUR] and " +
                                        "[GREEN]greater " + ModifierCategory.VIEW_RANGE.getName().toLowerCase() + "[NO_COLOUR], at the cost of " +
                                        "[RED]low " + ModifierCategory.NON_MAX_HP.getName() + "[NO_COLOUR] and " +
                                        "[RED]reduced firepower[NO_COLOUR].\n\n" +

                                        "Larger units are the opposite, prioritising [GREEN]durability[NO_COLOUR] and [GREEN]firepower[NO_COLOUR]. They are also " +
                                        "generally [RED]more expensive[NO_COLOUR] to operate.\n\n" +

                                        "The main [BLUE]characteristics[NO_COLOUR] of each class are:\n\n" +
                                        "[BLUE]" + ShipClass.FIGHTER.icon.display + ShipClass.FIGHTER.getPluralName() + "[NO_COLOUR]: Fast, high " + ModifierCategory.VIEW_RANGE.getName().toLowerCase() + " and low operating cost, but weak.\n\n" +
                                        "[BLUE]" + ShipClass.CORVETTE.icon.display + ShipClass.CORVETTE.getPluralName() + "[NO_COLOUR]: A good balance between speed and firepower. Moderate operating cost.\n\n" +
                                        "[BLUE]" + ShipClass.CRUISER.icon.display + ShipClass.CRUISER.getPluralName() + "[NO_COLOUR]: Primary focus on durability and firepower while giving up speed. High operating cost.\n\n"
                                )
                                .setColumn(1).setTopSpacing(0.5f).setDynamicWidth().finalise().forEachElement(i -> {
                                    box.setElementLeftMarginToElement(1, i, 0, boxIndex, HorizontalAlign.RIGHT, 0.5f);
                                });
                        box.setColumnTopMarginToElement(1, 0, boxIndex, VerticalAlign.TOP);
                        box.setColumnBottomMarginToElement(1, 0, boxIndex, VerticalAlign.BOTTOM);
                        box.setColumnVerticalAlign(1, VerticalAlign.TOP);
                        box.addParagraph(ModifierCategory.WEAPON_EFFECTIVENESS.getNameFirstUpper() + " modifiers",
                                        "When attacking, [BLUE]damage modifiers[NO_COLOUR] may be applied affecting damage dealt when performing an attack. " +
                                                "The main ones are the [BLUE]tile modifier[NO_COLOUR] (mentioned in an earlier tutorial), and the [BLUE]" +
                                                ModifierCategory.WEAPON_EFFECTIVENESS.getName().toLowerCase() + " modifier[NO_COLOUR]. This one affects " +
                                                "the damage dealt depending on the [BLUE]class[NO_COLOUR] of the enemy unit.\n\n" +
                                                "The " + ModifierCategory.WEAPON_EFFECTIVENESS.getName().toLowerCase() + " of " + FighterType.INTERCEPTOR.getArticle().toLowerCase() + "[BLUE]" + FighterType.INTERCEPTOR.getName() +
                                                " unit[NO_COLOUR] can be summarised as " + WeaponEffectiveness.effectivenessSummary(NO_COLOUR, c -> WeaponEffectiveness.againstClass(FighterType.INTERCEPTOR, c)) + ". " +
                                                "The colour of each class symbol describes whether the " + FighterType.INTERCEPTOR.getName() + " will deal " +
                                                WeaponEffectiveness.STRONG.textColourGray().display + "increased[NO_COLOUR], " +
                                                WeaponEffectiveness.NORMAL.textColourGray().display + "normal[NO_COLOUR], or " +
                                                WeaponEffectiveness.WEAK.textColourGray().display + "reduced[NO_COLOUR] damage when attacking that class.\n\n" +
                                                "This summary can be seen in the bottom left [BLUE]unit info screen[NO_COLOUR] whenever a unit is selected.")
                                .finalise();
                        box.addTutorialMap(HorizontalAlign.LEFT, 14.25f, 10, TutorialMapElement.TILE_SIZE_MEDIUM, 10, 0, map -> {
                            map.addTile(-1, 0, TileType.EMPTY);
                            map.addTile(-1, 1, TileType.DENSE_NEBULA);
                            map.addTile(-1, -1, TileType.EMPTY);
                            map.addTile(0, 0, TileType.EMPTY).addUnit(FighterType.INTERCEPTOR, UnitTeam.BLUE).onReset(TutorialMapUnit::restoreHP);
                            map.addTile(0, 1, TileType.NEBULA);
                            map.addTile(0, -1, TileType.EMPTY);
                            map.addTile(1, 0, TileType.EMPTY).addUnit(FighterType.INTERCEPTOR, UnitTeam.RED).onReset(TutorialMapUnit::restoreHP);
                            map.addText(new TutorialMapText(map, 14.25f / 2, 9.3f,
                                    new MultiLineTextBox(0, 0, 10, 0.5f, HorizontalAlign.CENTER)
                                            .updateText(FighterType.INTERCEPTOR.getNameArticle() + " attacking a [BLUE]" + ShipClass.FIGHTER.getClassName().toLowerCase() + "[NO_COLOUR] unit deals [GREEN]increased damage[NO_COLOUR]"),
                                    TutorialMapText::doNothing, TutorialMapText::doNothing));
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
                        int leftBoxIndex = box.getLastIndex(0);
                        box.addTutorialMap(HorizontalAlign.RIGHT, 14.25f, 10, TutorialMapElement.TILE_SIZE_MEDIUM, 10, 2, map -> {
                            map.addTile(-1, 0, TileType.EMPTY);
                            map.addTile(-1, 1, TileType.DENSE_NEBULA);
                            map.addTile(-1, -1, TileType.EMPTY);
                            map.addTile(0, 0, TileType.EMPTY).addUnit(FighterType.INTERCEPTOR, UnitTeam.BLUE).onReset(TutorialMapUnit::restoreHP);
                            map.addTile(0, 1, TileType.NEBULA);
                            map.addTile(0, -1, TileType.EMPTY);
                            map.addTile(1, 0, TileType.EMPTY).addUnit(CruiserType.BATTLECRUISER, UnitTeam.RED).onReset(TutorialMapUnit::restoreHP);
                            map.addText(new TutorialMapText(map, 14.25f / 2, 9.3f,
                                    new MultiLineTextBox(0, 0, 10, 0.5f, HorizontalAlign.CENTER)
                                            .updateText(FighterType.INTERCEPTOR.getNameArticle() + " attacking a [BLUE]" + ShipClass.CRUISER.getClassName().toLowerCase() + "[NO_COLOUR] unit deals [RED]reduced damage[NO_COLOUR]"),
                                    TutorialMapText::doNothing, TutorialMapText::doNothing));
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
                        box.setColumnTopMarginToElement(2, 0, leftBoxIndex, VerticalAlign.TOP)
                                .setColumnVerticalAlign(2, VerticalAlign.TOP);
                        box.addParagraph(null, FighterType.INTERCEPTOR.getPluralName() + " have a base damage of [RED]" + MathUtil.floatToString(FighterType.INTERCEPTOR.damage) + ModifierCategory.DAMAGE.icon() + "[NO_COLOUR]. " +
                                        "On the left example, the damage dealt against an enemy [BLUE]" + ShipClass.FIGHTER.icon.display + FighterType.INTERCEPTOR.getName() + "[NO_COLOUR] is [GREEN]greater[NO_COLOUR] than the base damage, while on the right, when attacking a " +
                                        "[BLUE]" + ShipClass.CRUISER.icon.display + CruiserType.BATTLECRUISER.getName() + "[NO_COLOUR], it is [RED]less[NO_COLOUR] than the base damage.\n\n" +
                                        "Keep in mind that each unit has different classes it is effective against. For the " + FighterType.INTERCEPTOR.getName() + " it's " +
                                        WeaponEffectiveness.effectivenessSummary(NO_COLOUR, c -> WeaponEffectiveness.againstClass(FighterType.INTERCEPTOR, c)) + " but for " + CorvetteType.FRIGATE.getArticle().toLowerCase() + CorvetteType.FRIGATE.getName() + " it's instead " +
                                        WeaponEffectiveness.effectivenessSummary(NO_COLOUR, c -> WeaponEffectiveness.againstClass(CorvetteType.FRIGATE, c)) + ".\n\n")
                                .finalise();
                    }),
                    BranchAction.skipIfBranch(l,
                            SelectedUnit.is(UnitState.ofTeam(UnitTeam.BLUE)),
                            BlockingTextBox.onMap(l, BoxSize.MEDIUM, 5, 7, HorizontalAlign.LEFT,
                                    "Select one of your units", UnitSelectListener.ofTeam(UnitTeam.BLUE))
                    ),
                    ModifyElements.disable(l, TILE_SELECTION, TILE_DESELECTION),

                    TutorialUI.onUI(l, "unitName")
                            .rectangle(11 / 2f + 0.5f, UIUnitInfo.HEIGHT + 0.75f, 12, 1.5f, GREEN_HIGHLIGHT, TutorialUI.StrokeWidth.NARROW),
                    ContinueTextBox.onUI(l, BoxSize.MEDIUM, 19, UIUnitInfo.HEIGHT - 2, HorizontalAlign.LEFT,
                            "Here you can see the [BLUE]name[NO_COLOUR] of the unit, along with its [BLUE]unit class[NO_COLOUR] symbol."),
                    TutorialUI.remove("unitName"),
                    ContinueTextBox.onUI(l, BoxSize.MEDIUM, 19, 3, HorizontalAlign.LEFT,
                            "On the left, under the [RED]weapons[NO_COLOUR] tab, you can see the [BLUE]" + ModifierCategory.WEAPON_EFFECTIVENESS.getName().toLowerCase() + "[NO_COLOUR] modifiers this unit has when attacking each class."),
                    ContinueTextBox.onUI(l, BoxSize.MEDIUM, 23, 10, HorizontalAlign.LEFT,
                            "Now that we're done covering [BLUE]fog of war[NO_COLOUR] and [BLUE]unit classes[NO_COLOUR], it's up to you to finish off the enemy. Good luck."),
                    ModifyElements.enable(l, TILE_SELECTION, TILE_DESELECTION, ACTIONS, END_TURN),
                    new EndTutorial()
            }),
    ENERGY(EnergyManager.displayName, "tutorial-energy",
            new TutorialLevelData(
                    new GameplaySettings(true, true)
            ).addPlayer(PlayerTeam.A, false).addPlayer(PlayerTeam.B, true),
            l -> new TutorialSequenceElement[]{
                    CameraMove.toTile(l, 2, 2),
                    ModifyElements.disable(l, ACTIONS, END_TURN),
                    TutorialScreen.create(l, TutorialScreen.NORMAL_WIDTH, (box, level) -> {
                        box.setWidthMargin(0.5f);
                        box.addParagraph(EnergyManager.displayName,
                                        "Each [BLUE]action[NO_COLOUR] costs a certain amount of " + EnergyManager.colouredDisplay(NO_COLOUR, false) + " to perform. " +
                                                "This is a resource you'll have to [BLUE]manage carefully[NO_COLOUR], as when you run out, you will no longer be able to perform actions.\n\n" +
                                                "Each action has a [BLUE]different cost[NO_COLOUR], depending on the specific action and type of unit performing it. Using the " + Action.FIRE.colouredIconName(NO_COLOUR, false) + " " +
                                                "action with " + FighterType.INTERCEPTOR.getArticle().toLowerCase() + FighterType.INTERCEPTOR.getName() + " costs " +
                                                EnergyManager.colouredAmount(NO_COLOUR, FighterType.INTERCEPTOR.getActionCost(Action.FIRE).orElse(0)) +
                                                ", but it costs " + EnergyManager.colouredAmount(NO_COLOUR, CorvetteType.FRIGATE.getActionCost(Action.FIRE).orElse(0)) +
                                                " for " + CorvetteType.FRIGATE.getPluralName() + ".\n\n" +

                                                "In general, it costs [BLUE]more[NO_COLOUR] to perform actions on larger unit classes. This is the [BLUE]operating cost[NO_COLOUR] that was mentioned in the last tutorial.")
                                .mainHeader().defaultBottomSpacing().finalise();

                        box.addTutorialMap(HorizontalAlign.LEFT, 14.25f, 10, TutorialMapElement.TILE_SIZE_MEDIUM, 10, 0, map -> {
                            map.addTile(-1, 0, TileType.EMPTY);
                            map.addTile(-1, 1, TileType.DENSE_NEBULA);
                            map.addTile(-1, -1, TileType.EMPTY);
                            map.addTile(0, 0, TileType.EMPTY).addUnit(FighterType.INTERCEPTOR, UnitTeam.BLUE).onReset(TutorialMapUnit::restoreHP);
                            map.addTile(0, 1, TileType.NEBULA);
                            map.addTile(0, -1, TileType.EMPTY);
                            map.addTile(1, 0, TileType.EMPTY).addUnit(FighterType.INTERCEPTOR, UnitTeam.RED).onReset(TutorialMapUnit::restoreHP);
                            map.addText(new TutorialMapText(map, 14.25f / 2, 9.3f,
                                    new MultiLineTextBox(0, 0, 10, 0.5f, HorizontalAlign.CENTER)
                                            .updateText("[BLUE]Hovering over[NO_COLOUR] the " + Action.FIRE.colouredIconName(NO_COLOUR, false) + " action allows you to see the cost."),
                                    TutorialMapText::doNothing, TutorialMapText::doNothing));
                            map.addMouseParticle(
                                    TutorialMouseKeyframe.tile(0, 0, 0, 0.17f, -0.1f, KeyframeFunction.lerp(), KeyframeFunction.lerp())
                                            .setOnClick(0.7f, () -> map.setSelectedTile(0, 0)),
                                    TutorialMouseKeyframe.delayUntil(1),
                                    TutorialMouseKeyframe.actionSelector(2, 0, 0, 0.05f, 0.1f, 2, 0,
                                            KeyframeFunction.lerp(), KeyframeFunction.lerp())
                            );
                            map.finalise();
                        });
                        int leftBoxIndex = box.getLastIndex(0);
                        box.addTutorialMap(HorizontalAlign.RIGHT, 14.25f, 10, TutorialMapElement.TILE_SIZE_MEDIUM, 10, 1, map -> {
                            map.addTile(-1, 0, TileType.EMPTY);
                            map.addTile(-1, 1, TileType.DENSE_NEBULA);
                            map.addTile(-1, -1, TileType.EMPTY);
                            map.addTile(0, 0, TileType.EMPTY).addUnit(FighterType.INTERCEPTOR, UnitTeam.BLUE).onReset(TutorialMapUnit::restoreHP);
                            map.addTile(0, 1, TileType.NEBULA);
                            map.addTile(0, -1, TileType.EMPTY);
                            map.addTile(1, 0, TileType.EMPTY).addUnit(CruiserType.BATTLECRUISER, UnitTeam.RED).onReset(TutorialMapUnit::restoreHP);
                            map.addText(new TutorialMapText(map, 14.25f / 2, 9.3f,
                                    new MultiLineTextBox(0, 0, 10, 0.5f, HorizontalAlign.CENTER)
                                            .updateText("Hovering over the " + Action.MOVE.colouredIconName(NO_COLOUR, false) + " action doesn't show the cost. " +
                                                    "It's cost is [BLUE]calculated differently[NO_COLOUR] than normal."),
                                    TutorialMapText::doNothing, TutorialMapText::doNothing));
                            map.addMouseParticle(
                                    TutorialMouseKeyframe.tile(0, 0, 0, 0.17f, -0.1f, KeyframeFunction.lerp(), KeyframeFunction.lerp())
                                            .setOnClick(0.7f, () -> map.setSelectedTile(0, 0)),
                                    TutorialMouseKeyframe.delayUntil(1),
                                    TutorialMouseKeyframe.actionSelector(2, 0, 0, 0.05f, 0.1f, 2, 1,
                                            KeyframeFunction.lerp(), KeyframeFunction.lerp())
                            );
                            map.finalise();
                        });
                        box.setColumnTopMarginToElement(1, 0, leftBoxIndex, VerticalAlign.TOP)
                                .setColumnVerticalAlign(1, VerticalAlign.TOP);
                        box.addParagraph("Viewing action cost", "As seen in the example above on the left, the cost for most actions can be seen by [BLUE]hovering over the action button[NO_COLOUR].\n\n" +
                                        "The cost of the " + Action.MOVE.colouredIconName(NO_COLOUR, false) + " action, however, is only visible while [BLUE]selecting a tile to move to[NO_COLOUR], as its cost " +
                                        "is proportional to the " + ModifierCategory.MOVEMENT_SPEED_DISPLAY.colouredName(null, false) + ModifierCategory.MOVEMENT_SPEED_DISPLAY.icon() + "[NO_COLOUR], and thus " +
                                        "depends on the path the unit has to take.")
                                .finalise().defaultBottomSpacing();
                    }),
                    BlockingTextBox.onUI(l, BoxSize.LARGE, 25, Renderable.top() - 12, HorizontalAlign.LEFT,
                            "Above you can see the amount of " + EnergyManager.colouredDisplay(NO_COLOUR, false) + " you currently have, as well as your [BLUE]income[NO_COLOUR].\n\n" +
                                    "This amount gets " + EnergyManager.displayName.toLowerCase() + " credited to you at the [BLUE]start of each turn[NO_COLOUR].\n\n" +
                                    "Click the box above with the " + EnergyManager.displayName.toLowerCase() + " info.",
                            EnergyBoxListener.openIncomeBox()),
                    ContinueTextBox.onUI(l, BoxSize.LARGE, 17, Renderable.top() - 10, HorizontalAlign.LEFT,
                            "Here you can see the [BLUE]income sources[NO_COLOUR] that contribute to your income.\n\n" +
                                    "The [BLUE]" + StructureType.BASE.getName().toLowerCase() + " structure[NO_COLOUR] always provides a stable income, but there " +
                                    "are ways to [BLUE]increase it[NO_COLOUR]."),
                    CameraMove.toTile(l, 2, 1),
                    TutorialHighlight.tile(l, BLUE_HIGHLIGHT, 2, 1),
                    BlockingTextBox.onUI(l, BoxSize.SMALL, 15, 22, HorizontalAlign.LEFT,
                            "Select the [BLUE]" + StructureType.BASE.getName().toLowerCase() + "[NO_COLOUR] to view info about it.",
                            TileSelectListener.tile(2, 1)),
                    TutorialHighlight.disable(l),
                    ContinueTextBox.onUI(l, BoxSize.LARGE, 36, 5f, HorizontalAlign.LEFT,
                            "The [BLUE]Structure[NO_COLOUR] tab in the [BLUE]tile info screen[NO_COLOUR] to the right " +
                                    "shows which structure is on the current tile.\n\n[BLUE]Left-click[NO_COLOUR] or [BLUE]hover[NO_COLOUR] over the structure tab " +
                                    "to see [BLUE]detailed info[NO_COLOUR] about what the structure does, including its income."),
                    CameraMove.toTile(l, 5, 3),
                    TutorialHighlight.tile(l, BLUE_HIGHLIGHT, 5, 3),
                    ContinueTextBox.onMap(l, BoxSize.MEDIUM, 3, 4, HorizontalAlign.LEFT,
                            "This is a [BLUE]" + StructureType.REFINERY.getName() + "[NO_COLOUR] structure. Whichever team controls it gets additional income."),
                    TutorialHighlight.disable(l),

                    TutorialScreen.create(l, TutorialScreen.NORMAL_WIDTH, (box, level) -> {
                        box.setWidthMargin(0.5f);
                        String c = Action.CAPTURE.colour();
                        box.addParagraph("Capturing structures",
                                        "At the start of the game, each team will have always have a " + StructureType.BASE.getName().toLowerCase() + " structure. " +
                                                "There may be other structures on the map, sometimes [BLUE]neutral[NO_COLOUR] and sometimes [BLUE]belonging to a team[NO_COLOUR].\n\n" +

                                                "All of these structures can be " + c + "captured[NO_COLOUR] by moving a [BLUE]unit[NO_COLOUR] over the structure and using the " + Action.CAPTURE.colouredIconName(NO_COLOUR, false) + " action.")
                                .mainHeader().defaultBottomSpacing().finalise();

                        box.addTutorialMap(HorizontalAlign.CENTER, 25, 10, TutorialMapElement.TILE_SIZE_MEDIUM, 16, 0, map -> {
                            map.addTile(-3, -1, TileType.EMPTY);
                            map.addTile(-3, 0, TileType.NEBULA);
                            map.addTile(-3, 1, TileType.EMPTY);
                            map.addTile(-2, -1, TileType.EMPTY);
                            map.addTile(-2, 0, TileType.NEBULA).addUnit(FighterType.INTERCEPTOR, UnitTeam.BLUE);
                            map.addTile(-2, 1, TileType.NEBULA);
                            map.addTile(-1, -1, TileType.EMPTY);
                            map.addTile(-1, 0, TileType.NEBULA);
                            map.addTile(-1, 1, TileType.NEBULA);
                            map.addTile(0, -1, TileType.ASTEROIDS);
                            map.addTile(0, 0, TileType.EMPTY);
                            map.addTile(0, 1, TileType.EMPTY);
                            map.addTile(1, -1, TileType.ASTEROIDS);
                            map.addTile(1, 0, TileType.EMPTY);
                            map.addTile(1, 1, TileType.EMPTY);
                            map.addTile(2, -1, TileType.EMPTY);
                            TutorialMapTile tile = map.addTile(2, 0, TileType.EMPTY);
                            tile.addStructure(StructureType.REFINERY, null);
                            tile.forceCaptureSegments(2);
                            map.addTile(2, 1, TileType.EMPTY);
                            map.addTile(3, 0, TileType.EMPTY);
                            map.addMouseParticle(
                                    TutorialMouseKeyframe.tile(0, -2, 0, 0.2f, 0.15f, KeyframeFunction.lerp(), KeyframeFunction.lerp())
                                            .setOnClick(1, () -> map.setSelectedTile(-2, 0)),
                                    TutorialMouseKeyframe.delayUntil(1.5f),
                                    TutorialMouseKeyframe.actionSelector(2, -2, 0, -0.04f, 0.03f, 2, 1, KeyframeFunction.lerp(), KeyframeFunction.lerp())
                                            .setOnClick(0.3f, () -> map.selectMoveAction(-2, 0)),
                                    TutorialMouseKeyframe.delayUntil(2.7f, KeyframeFunction.pow(1.3f), KeyframeFunction.pow(0.7f)),
                                    TutorialMouseKeyframe.tile(4, 2, 0, -0.15f, 0.2f, KeyframeFunction.lerp(), KeyframeFunction.lerp())
                                            .setOnClick(0.3f, map::moveUnit),
                                    TutorialMouseKeyframe.delayUntil(5.5f).setOnClick(0.5f,
                                            () -> map.setSelectedTile(2, 0).actionSelector().setActionState(Action.MOVE, ActionIconType.DISABLED)),
                                    TutorialMouseKeyframe.delayUntil(6.5f),
                                    TutorialMouseKeyframe.actionSelector(7f, 2, 0, 0.15f, 0.12f, 3, 0, KeyframeFunction.lerp(), KeyframeFunction.lerp())
                                            .setOnClick(0.5f, map::capture)
                            );
                            map.addPopup(9f, 1.5f, "Next Turn");
                            map.addRepeatedTask(12, () -> map.capture(2, 0));
                            map.addRepeatedTask(13, () -> map.getTile(2, 0).finishCapture(UnitTeam.BLUE));
                            map.finalise();
                        });
                        box.addParagraph("The " + Action.CAPTURE.getName().toLowerCase() + " action",
                                        "When using the " + Action.CAPTURE.colouredIconName(NO_COLOUR, false) + " action, a [BLUE]bar[NO_COLOUR] appears over the structure. This shows the [BLUE]capture progress[NO_COLOUR]. The initial use of the action adds one capture point, " +
                                                "and each time you [BLUE]start your turn[NO_COLOUR], another point is added automatically. " +
                                                "Different structure types may require different [BLUE]numbers of turns[NO_COLOUR] to capture.\n\n" +

                                                "If you [RED]attack[NO_COLOUR] a capturing unit, the capturing unit [BLUE]loses one capture point[NO_COLOUR].\n\n" +

                                                "The " + Action.CAPTURE.colouredIconName(NO_COLOUR, false) + " action is [BLUE]not shown[NO_COLOUR] unless it [BLUE]is available[NO_COLOUR], which is when the active unit is on a structure that can be captured.\n\n" +

                                                "Also, it is a [BLUE]toggle action[NO_COLOUR], meaning that using it once [GREEN]starts capturing[NO_COLOUR], and using it a second time while already capturing will [RED]reset[NO_COLOUR] the current capture.\n\n" +

                                                "Moving a capturing unit away from its tile ends the capture.")
                                .defaultBottomSpacing().finalise();
                        box.addParagraph("Capturing the " + StructureType.BASE.getName(),
                                        "While most structures will provide a benefit when captured, the [BLUE]" + StructureType.BASE.getName().toLowerCase() + "[NO_COLOUR] is special. " +
                                                "Capturing the enemy " + StructureType.BASE.getName().toLowerCase() + " [RED]eliminates the entire team[NO_COLOUR] immediately.\n\n" +
                                                "You must [BLUE]protect[NO_COLOUR] your " + StructureType.BASE.getName().toLowerCase() + " [RED]at all costs[NO_COLOUR].")
                                .finalise();
                    }),
                    ModifyElements.enable(l, ACTIONS),
                    AllowedActionTiles.only(Action.MOVE, 5, 3),
                    BranchTextBox.onMap(l, BoxSize.MEDIUM, 3, 4, HorizontalAlign.LEFT,
                            "Move a unit to the [BLUE]" + StructureType.REFINERY.getName().toLowerCase() + "[NO_COLOUR] and start capturing.",
                            ActionListener.tileSelectAnyExcept(Action.MOVE, new Point(5, 3)),
                            ActionListener.perform(Action.CAPTURE),

                            TutorialHighlight.tile(l, GREEN_HIGHLIGHT, 5, 3),
                            SequenceTextBox.onMap(l, BoxSize.MEDIUM, 3, 4, HorizontalAlign.LEFT,
                                    "Move a unit to the [BLUE]" + StructureType.REFINERY.getName().toLowerCase() + "[NO_COLOUR] and start capturing.\n\n" +
                                            "The " + StructureType.REFINERY.getName().toLowerCase() + " structure is on the [GREEN]highlighted tile[NO_COLOUR].",
                                    BlockingAction.waitFor(ActionListener.perform(Action.MOVE)),
                                    TutorialHighlight.disable(l),
                                    BlockingAction.waitFor(ActionListener.perform(Action.CAPTURE))
                            )),
                    AllowedActionTiles.all(),
                    ModifyElements.enable(l, END_TURN),
                    ModifyElements.disable(l, ACTIONS),
                    BotActions.addActions(
                            BotActionData.move(l, 7, 6, 7, 5),
                            BotActionData.move(l, 8, 6, 7, 4),
                            BotActionData.move(l, 7, 7, 6, 5),
                            BotActionData.capture(l, 6, 5)
                    ),
                    BlockingTextBox.onMap(l, BoxSize.MEDIUM, 3, 4, HorizontalAlign.LEFT,
                            "[BLUE]End the turn[NO_COLOUR] to continue capturing on the next turn.",
                            TurnListener.start(UnitTeam.RED)),
                    BlockingAction.waitFor(TurnListener.start(UnitTeam.BLUE)),
                    ContinueTextBox.onMap(l, BoxSize.LARGE, 3, 4.3f, HorizontalAlign.LEFT,
                            "Now that the structure has been captured, you can see that your [BLUE]income has increased[NO_COLOUR]."),
                    ContinueTextBox.onMap(l, BoxSize.LARGE, 3, 4.3f, HorizontalAlign.LEFT,
                            "That's it for the main tutorial, you're now ready to fight real battles.\n\n" +
                                    "There is much more to see that was not covered here, so short a explainer will be provided whenever you encounter a new game mechanic."),
                    ModifyElements.enable(l, ACTIONS),
                    new EndTutorial()
            });

    public final String displayName, saveFileName;
    public final TutorialLevelData levelData;
    public final Function<Level, TutorialSequenceElement[]> sequence;

    TutorialLevel(String displayName, String saveFileName, TutorialLevelData levelData, Function<Level, TutorialSequenceElement[]> sequence) {
        this.displayName = displayName;
        this.saveFileName = saveFileName;
        this.levelData = levelData;
        this.sequence = sequence;
    }

    @Override
    public String getName() {
        return displayName;
    }
}