package mainScreen;

import foundation.MainPanel;
import foundation.input.*;
import level.GameplaySettings;
import level.Level;
import level.LevelEditor;
import level.TeamSpawner;
import level.tutorial.TutorialLevel;
import level.tutorial.TutorialLevelData;
import network.NetworkState;
import render.*;
import render.level.map.MapUI;
import render.level.tile.RenderElement;
import render.level.ui.TooltipRenderer;
import render.save.UISaveBox;
import render.texture.ResourceLocation;
import render.types.box.UIBox;
import render.types.box.UIDisplayBoxRenderElement;
import render.types.container.UIContainer;
import render.types.container.UIElementScrollSurface;
import render.types.container.UIScrollSurface;
import render.types.container.UITabSwitcher;
import render.types.input.UIEnumSelector;
import render.types.input.UINumberSelector;
import render.types.input.UITextInputBox;
import render.types.input.button.UIButton;
import render.types.text.*;
import save.GameSave;
import save.MapSave;
import unit.UnitTeam;
import unit.bot.BotDifficulty;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import static mainScreen.UIPlayerShipSettings.*;
import static render.UIColourTheme.*;

public class TitleScreen implements Renderable, InputReceiver {
    public static final float MAIN_BUTTON_Y_OFFSET = 2, MAIN_BUTTON_HEIGHT = 2.4f, MAIN_BUTTON_SPACING = 0.7f, MAIN_BUTTON_COUNT = 6, MAIN_BUTTON_B0X_HEIGHT = MAIN_BUTTON_COUNT * MAIN_BUTTON_HEIGHT + (MAIN_BUTTON_COUNT + 1) * MAIN_BUTTON_SPACING;
    private static Renderable titleScreenImage;
    private final GameRenderer renderer = new GameRenderer(MainPanel.windowTransform, null);
    private final ButtonRegister buttonRegister = new ButtonRegister();
    private UIButton tutorialButton, newGame, multiplayer, connectToLan, loadGame, levelEditorButton;
    private final ArrayList<UIButton> allMainButtons = new ArrayList<>();

    public UITabSwitcher multiplayerTabs;
    public UIScrollSurface playerBoxScrollWindow;
    public UIButton startLocalGame, startLanGame;
    public UITextDisplayBox gameCannotBeStarted;
    public UIPlayerBoxes playerBoxes;
    public UITextInputBox enterSeedBox;
    public UINumberSelector widthSelector, heightSelector;
    public UIButton editStructures;
    public StructureGenerationSettings structureGenerationSettings;

    public UITextInputBox enterIPBox;
    public RenderElement enterIPLabel;
    public UIButton connectButton, joinButton;
    public UIButton[] colourSelectorButtons;
    public RenderElement colourSelectorBox;
    public UIContainer connectContainer;

    public UIContainer playerShipSettingsContainer;
    public UIPlayerShipSettings playerShipSettings;
    public UIButton pasteSettingsButton;

    public UIContainer loadContainer;
    public UIButton loadGameLocally, loadGameToLan;
    public UITextLabel loadLabel;
    public UISaveBox<GameSave> loadBox;

    public UIEnumSelector<BotDifficulty> botDifficultySelector;
    public UIButton toggleFoW, showFiringAnim;

    public UIContainer levelEditorContainer;
    public UINumberSelector levelEditorWidthSelector, levelEditorHeightSelector, levelEditorPlayerSelector;
    public UISaveBox<MapSave> mapSaveBox, loadCustomBox;
    public UIButton mapLoadButton;
    public MapUI customMapPreview = null, loadMapPreview = null, loadGamePreview = null;
    public boolean customMap = false;

    public UIContainer tutorialContainer;
    public TutorialLevel selectedTutorialLevel = null;

    public TitleScreen() {
    }

    public void init() {
        titleScreenImage = Renderable.renderImage(new ResourceLocation("title_screen.png"), false, true, 60, true);
        new UIButton(renderer, buttonRegister, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS, 0.5f, Renderable.top() - 2.5f, 8, 2, 1.4f, false, () -> System.exit(0))
                .setText("Quit").setColourTheme(UIColourTheme.DEEP_RED).setBold()
                .tooltip(t -> t.add(6, AbstractUITooltip.light(), "Exit to desktop"));
        UIConfirm uiConfirmQuit = new UIConfirm(renderer, RenderOrder.CONFIRM_UI, null).setTextSize(1).modifyBox(box -> box.setColourTheme(LIGHT_BLUE_FULLY_OPAQUE_CENTER)).setTextConfirm("Back").setTextCancel("Quit");
        buttonRegister.register(uiConfirmQuit);
        new OnButtonInput(buttonRegister, ButtonOrder.MAIN_BUTTONS_BACK, type -> type == InputType.ESCAPE, () -> uiConfirmQuit.makeVisible("Are you sure you want to quit?", uiConfirmQuit::makeInvisible, () -> System.exit(0)));
        new RenderElement(renderer, RenderOrder.TITLE_SCREEN_BACKGROUND, titleScreenImage);
        new RenderElement(renderer, RenderOrder.TITLE_SCREEN_BUTTON_BACKGROUND, new UIBox(10 + MAIN_BUTTON_SPACING * 2, MAIN_BUTTON_B0X_HEIGHT).setColourTheme(UIColourTheme.LIGHT_BLUE_TRANSPARENT_CENTER).translate(Renderable.right() - 15 - MAIN_BUTTON_SPACING, MAIN_BUTTON_Y_OFFSET));
        tutorialButton = new UIButton(renderer, buttonRegister, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                Renderable.right() - 15, mainButtonHeight(0), 10, MAIN_BUTTON_HEIGHT, 1.2f, true, () -> {
            for (UIButton button : allMainButtons) {
                if (button != tutorialButton)
                    button.deselect();
            }
            tutorialContainer.setEnabled(true);
        }).setOnDeselect(() -> {
                    tutorialContainer.setEnabled(false);
                }).setText("Tutorial").setBold().noDeselect().setColourTheme(UIColourTheme.GREEN_SELECTED)
                .tooltip(t -> t.add(11, AbstractUITooltip.light(), "Learn the game through tutorials. Highly recommended for beginners."));

        newGame = new UIButton(renderer, buttonRegister, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                Renderable.right() - 15, mainButtonHeight(1), 10, MAIN_BUTTON_HEIGHT, 1.2f, true, () -> {
            for (UIButton button : allMainButtons) {
                if (button != newGame)
                    button.deselect();
            }
        }).setText("Singleplayer").setBold().noDeselect().setColourTheme(UIColourTheme.GREEN_SELECTED)
                .tooltip(t -> t.add(9, AbstractUITooltip.light(), "Start new singleplayer campaign. Coming soon."));

        multiplayer = new UIButton(renderer, buttonRegister, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                Renderable.right() - 15, mainButtonHeight(2), 10, MAIN_BUTTON_HEIGHT, 1.2f, true, () -> {
            for (UIButton button : allMainButtons) {
                if (button != multiplayer)
                    button.deselect();
            }
            multiplayerTabs.setEnabled(true);
            playerBoxes.verifyTeams();
        }).setOnDeselect(() -> {
                    multiplayerTabs.setEnabled(false);
                    startLocalGame.setEnabled(false);
                    startLanGame.setEnabled(false);
                    gameCannotBeStarted.setEnabled(false);
                }).setText("Multiplayer").setBold().noDeselect().setColourTheme(UIColourTheme.GREEN_SELECTED)
                .tooltip(t -> t.add(9, AbstractUITooltip.light(), "Start new local or online multiplayer game"));

        connectToLan = new UIButton(renderer, buttonRegister, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                Renderable.right() - 15, mainButtonHeight(3), 10, MAIN_BUTTON_HEIGHT, 1.2f, true, () -> {
            for (UIButton button : allMainButtons) {
                if (button != connectToLan)
                    button.deselect();
            }
            connectContainer.setEnabled(true);
            updateColourSelectorVisibility();
        }).setOnDeselect(() -> {
                    connectContainer.setEnabled(false);
                    updateColourSelectorVisibility();
                }).setText("Join Game").setBold().noDeselect().setColourTheme(UIColourTheme.GREEN_SELECTED)
                .tooltip(t -> t.add(9, AbstractUITooltip.light(), "Join an existing online game on the local network"));

        loadGame = new UIButton(renderer, buttonRegister, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                Renderable.right() - 15, mainButtonHeight(4), 10, MAIN_BUTTON_HEIGHT, 1.2f, true, () -> {
            for (UIButton button : allMainButtons) {
                if (button != loadGame)
                    button.deselect();
            }
            loadContainer.setEnabled(true);
            updateSaveFiles();
            updateLoadButtons();
        }).setOnDeselect(() -> {
                    loadContainer.setEnabled(false);
                    updateLoadButtons();
                }).setText("Load Game").setBold().noDeselect().setColourTheme(UIColourTheme.GREEN_SELECTED)
                .tooltip(t -> t.add(9, AbstractUITooltip.light(), "Load a previously saved game"));

        levelEditorButton = new UIButton(renderer, buttonRegister, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                Renderable.right() - 15, mainButtonHeight(5), 10, MAIN_BUTTON_HEIGHT, 1.2f, true, () -> {
            for (UIButton button : allMainButtons) {
                if (button != levelEditorButton)
                    button.deselect();
            }
            levelEditorContainer.setEnabled(true);
        }).setOnDeselect(() -> {
                    levelEditorContainer.setEnabled(false);
                }).setText("Map Editor").setBold().noDeselect().setColourTheme(UIColourTheme.GREEN_SELECTED)
                .tooltip(t -> t.add(9, AbstractUITooltip.light(), "Create custom maps for use in multiplayer mode"));

        allMainButtons.add(tutorialButton);
        allMainButtons.add(newGame);
        allMainButtons.add(multiplayer);
        allMainButtons.add(connectToLan);
        allMainButtons.add(loadGame);
        allMainButtons.add(levelEditorButton);

        tutorialContainer = new UIContainer(renderer, buttonRegister, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS, Renderable.right() - 28, 4).addRenderables((r, b) -> {
            new RenderElement(r, RenderOrder.TITLE_SCREEN_BACKGROUND,
                    new UIBox(10, 14).setColourTheme(LIGHT_BLUE_TRANSPARENT_CENTER),
                    new UITextLabel(10.3f, 1, true).setTextLeftBold().updateTextLeft("Select tutorial:").translate(-0.3f, 14.5f));
            UIDisplayBoxRenderElement textBox = new UIDisplayBoxRenderElement(r, RenderOrder.TITLE_SCREEN_BUTTONS, -10, 2, 8, 10, box -> box.setColourTheme(LIGHT_BLUE_BOX_DARK), false);
            textBox.addText(0.7f, HorizontalAlign.LEFT, null).setEnabled(false);
            UIButton startButton = new UIButton(r, b, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS, 1.5f, -2.5f, 7, 1.5f, 1.2f, false)
                    .setBold().setText("Start").setClickEnabled(false).setColourTheme(GRAYED_OUT).setOnClick(() -> {
                        MainPanel.startNewLevel(selectedTutorialLevel, () -> loadTutorialLevel(MainPanel.tutorialMaps.gameSaves.get(selectedTutorialLevel.saveFileName), selectedTutorialLevel.levelData));
                    });
            UIElementScrollSurface<UIButton> tutorialButtons = new UIElementScrollSurface<>(r, b, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                    0, 0, 10, 14, false, count -> count * 2f + 0.5f);
            tutorialButtons.addElements(TutorialLevel.values().length, (r2, b2, i) -> {
                TutorialLevel level = TutorialLevel.values()[i];
                UIButton button = new UIButton(r2, b2, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS, 1, -2 * (i + 1), 8, 1.5f, 0.8f, true)
                        .setText(level.getName()).setColourTheme(GREEN_SELECTED).setBold().noDeselect();
                return button.setOnClick(() -> {
                    tutorialButtons.forEach(other -> {
                        if (button != other)
                            other.deselect();
                    });
                    textBox.setEnabled(true);
                    textBox.box.setText(level.description);
                    startButton.setClickEnabled(true).setColourTheme(LIGHT_BLUE);
                    selectedTutorialLevel = level;
                });
            });
        }).setEnabled(false);

        connectContainer = new UIContainer(renderer, buttonRegister, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS, 0, 0).addRenderables((r, b) -> {
            enterIPBox = new UITextInputBox(r, b, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                    Renderable.right() - 36, 14, 16, 2, .7f, true, -1,
                    InputType::isIPChar);
            enterIPBox.tooltip(t -> t.add(10, AbstractUITooltip.light(), "Enter the local IP address of the device hosting the game"));
            enterIPBox.setBold().setColourTheme(UIColourTheme.GREEN_SELECTED);
            enterIPLabel = new RenderElement(r, RenderOrder.TITLE_SCREEN_BUTTONS,
                    new UITextLabel(7.5f, 1, false).setTextCenterBold()
                            .updateTextCenter("Enter Server IP")
                            .translate(Renderable.right() - 31.8f, 16.5f)
            );
            connectButton = new UIButton(r, b, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                    Renderable.right() - 32.5f, 11.5f, 9f, 1.5f, 0.8f, false, () -> {
                Level.EXECUTOR.submit(() -> {
                    if (connectButton.getText().equals("Connecting..."))
                        return;
                    connectButton.setColourTheme(UIColourTheme.YELLOW);
                    connectButton.setText("Connecting...");
                    MainPanel.removeClient();
                    updateColourSelectorVisibility();
                    boolean success = MainPanel.startClient(enterIPBox.getText());
                    connectButton.setColourTheme(success ? UIColourTheme.GREEN : UIColourTheme.DEEP_RED);
                    connectButton.setText(success ? "Connected!" : "Connection Failed");
                    updateColourSelectorVisibility();
                });
            }).setText("Connect").setBold();
            joinButton = new UIButton(r, b, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                    Renderable.right() - 32.5f, 3.5f, 9f, 1.5f, 0.8f, false, () -> {
                MainPanel.client.sendJoinRequest(selectedTeam);
            }).setText("Join").setBold().setEnabled(false);
            colourSelectorBox = new RenderElement(r, RenderOrder.TITLE_SCREEN_BUTTON_BACKGROUND,
                    new UIBox(16, 7.8f).setColourTheme(UIColourTheme.LIGHT_BLUE_TRANSPARENT_CENTER),
                    new TextRenderer("Select a team colour:", 0.8f, UITextLabel.TEXT_COLOUR).setBold(true).setTextAlign(HorizontalAlign.LEFT)
                            .translate(0.4f, 6.8f)
            ).translate(Renderable.right() - 36, 2.7f);
            colourSelectorBox.setEnabled(false);
            new OnButtonInput(b, ButtonOrder.MAIN_BUTTONS, t -> t == InputType.ENTER, () -> {
                if (joinButton.isEnabled())
                    joinButton.select();
                else if (enterIPBox.isEnabled() && !connectButton.getText().equals("Connected!"))
                    connectButton.select();
            });
        }).setEnabled(false);
        createColourSelectorButtons();

        startLocalGame = new UIButton(renderer, buttonRegister, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                Renderable.right() - 37 + 1, 3.5f - 2, 7, 1.5f, 0.7f, false, () -> {
            if (multiplayerTabs.lastTabSelected()) {
                if (customMap)
                    MainPanel.startNewLevel(() -> loadCustomLevel(loadCustomBox.getSelected(), false));
                else
                    MainPanel.startNewLevel(this::getNewLocalMultiplayerLevel);
            } else {
                if (!multiplayerTabs.firstTabSelected()) {
                    multiplayerTabs.selectTab(multiplayerTabs.getSelectedTab() - 1);
                    updateMultiplayerConnectButtons();
                }
            }
        }).setBold().setEnabled(false).tooltip(t -> t.add(10, AbstractUITooltip.light(), null));
        startLanGame = new UIButton(renderer, buttonRegister, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                Renderable.right() - 37 + 9, 3.5f - 2, 8, 1.5f, 0.7f, false, () -> {
            if (multiplayerTabs.lastTabSelected()) {
                if (customMap)
                    MainPanel.startNewLevel(() -> loadCustomLevel(loadCustomBox.getSelected(), true));
                else
                    MainPanel.startNewLevel(this::getNewServerMultiplayerLevel);
            } else {
                multiplayerTabs.selectTab(multiplayerTabs.getSelectedTab() + 1);
                updateMultiplayerConnectButtons();
            }
        }).setBold().setEnabled(false).tooltip(t -> t.add(13, AbstractUITooltip.light(), null));
        gameCannotBeStarted = new UITextDisplayBox(renderer, RenderOrder.TITLE_SCREEN_BUTTONS,
                Renderable.right() - 37 + 1, 3.5f - 2, 16, 1.5f, 0.7f)
                .setBold().setEnabled(false).setColourTheme(UIColourTheme.DEEP_RED);
        multiplayerTabs = new UITabSwitcher(renderer, buttonRegister, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                Renderable.right() - 37, 3.5f, 18, 15)
                .addTab(4.65f, "Map Layout", (r, b) -> {
                    UIContainer seedContainer = new UIContainer(r, b, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS, 0, 0)
                            .addRenderables((r2, b2) -> {
                                structureGenerationSettings = new StructureGenerationSettings(r2, b2, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS, -21, -2);
                                structureGenerationSettings.setEnabled(false);
                                widthSelector = new UINumberSelector(r2, b2, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                                        1.75f, 9f, 1f, 1.5f, Level.MIN_WIDTH, Level.MAX_WIDTH, 20).setCorner(0.35f)
                                        .setOnChanged(() -> playerBoxes.verifyTeams()).tooltip(t -> t.add(-1, AbstractUITooltip.light(), "Map width"));
                                heightSelector = new UINumberSelector(r2, b2, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                                        1.75f, 6f, 1f, 1.5f, Level.MIN_HEIGHT, Level.MAX_HEIGHT, 12).setCorner(0.35f)
                                        .setOnChanged(() -> playerBoxes.verifyTeams()).tooltip(t -> t.add(-1, AbstractUITooltip.light(), "Map height"));
                                new RenderElement(r2, RenderOrder.TITLE_SCREEN_BUTTONS,
                                        new UITextLabel(6f, 1, false, 0, 0.8f).setTextCenterBold()
                                                .updateTextCenter("Generation").translate(9.2f, 10.5f),
                                        new UITextLabel(4f, 1, false, 0, 0.8f).setTextCenterBold()
                                                .updateTextCenter("Width:").translate(1.75f, 10.5f),
                                        new UITextLabel(4f, 1, false, 0, 0.8f).setTextCenterBold()
                                                .updateTextCenter("Height:").translate(1.75f, 7.5f),
                                        new UITextLabel(14.5f, 1, true, 0.2f).setTextLeftBold().setLeftOffset(0.1f)
                                                .updateTextLeft("Enter seed:").translate(1.5f, 4f)
                                );
                                new RenderElement(r2, RenderOrder.TITLE_SCREEN_BACKGROUND,
                                        new UIBox(widthSelector.totalWidth() + 1.5f, 6.3f).setColourTheme(LIGHT_BLUE_BOX_DARK).translate(1, 5.5f),
                                        new UIBox(16 - (widthSelector.totalWidth() + 1.5f) - 1, 6.3f).setColourTheme(LIGHT_BLUE_BOX_DARK).translate(1 + widthSelector.totalWidth() + 1.5f + 1, 5.5f)
                                );
                                editStructures = new UIButton(r2, b2, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                                        9f, 9f, 7f, 1f, 0.7f, true).setBoxCorner(0.35f)
                                        .setBold().setText("Edit structures").toggleMode().noDeselect().setColourTheme(GREEN_SELECTED)
                                        .setOnClick(() -> structureGenerationSettings.setEnabled(true))
                                        .setOnDeselect(() -> structureGenerationSettings.setEnabled(false));
                                new UIButton(r2, b2, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                                        9f, 7.5f, 7f, 1f, 0.7f, true).setBoxCorner(0.35f)
                                        .setBold().setText("Coming soon").toggleMode().noDeselect().setColourTheme(GRAYED_OUT);

                                enterSeedBox = new UITextInputBox(r2, b2, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                                        2, 1f, 14, 2.5f, 1.5f, true, 15, InputType::isDigit);
                                enterSeedBox.setColourTheme(GREEN_SELECTED).setBold().tooltip(t -> t.add(12, AbstractUITooltip.light(), "Enter a number to use as a seed. The map generated by a given seed is deterministic. Certain game settings can, however, alter map generation. Leave blank for a random seed."));
                            });
                    UIContainer customContainer = new UIContainer(r, b, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS, 0, 0).setEnabled(false);
                    customContainer.addRenderables((r2, b2) -> {
                        RenderElement noMapsAvailableText = new RenderElement(r2, RenderOrder.TITLE_SCREEN_BUTTONS,
                                new TextRenderer("No custom maps available", 0.7f, UITextLabel.TEXT_COLOUR)
                                        .setItalic(true).setTextAlign(HorizontalAlign.CENTER).translate(-13.5f + 12 / 2f, 11),
                                new TextRenderer("Use the Map Editor to create", 0.7f, UITextLabel.TEXT_COLOUR)
                                        .setItalic(true).setTextAlign(HorizontalAlign.CENTER).translate(-13.5f + 12 / 2f, 9),
                                new TextRenderer("custom maps", 0.7f, UITextLabel.TEXT_COLOUR)
                                        .setItalic(true).setTextAlign(HorizontalAlign.CENTER).translate(-13.5f + 12 / 2f, 8)
                        );
                        new RenderElement(r2, RenderOrder.TITLE_SCREEN_BUTTONS,
                                new UITextLabel(12.3f, 1, true).setTextLeftBold()
                                        .updateTextLeft("Select custom map:").setLeftOffset(0.1f)
                                        .translate(-13.7f, 12.8f),
                                new UIBox(16, 11).setColourTheme(LIGHT_BLUE_BOX_DARK).translate(1, 0.7f)
                        );
                        loadCustomBox = new UISaveBox<>(r2, b2, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                                -13.5f, 1.5f, 12, 11, 1.5f, MainPanel.mapSaves)
                                .enableClick();
                        loadCustomBox.setOnClickOrUpdate(fromUpdate -> {
                            noMapsAvailableText.setEnabled(loadCustomBox.getElements().isEmpty());
                            if (customMapPreview != null)
                                customMapPreview.delete();
                            if (loadCustomBox.hasSelectedSave()) {
                                customContainer.addRenderables((r3, b3) -> {
                                    MapSave map = loadCustomBox.getSelected();
                                    customMapPreview = MapUI.box(r3, RenderOrder.TITLE_SCREEN_BUTTONS, map, 16, 11, 0.15f);
                                    customMapPreview.translate(1, 0.7f).setZOrder(1);
                                    playerBoxes.setPlayerCount(map.playerCount);
                                });
                            }
                            playerBoxes.verifyTeams();
                            if (!fromUpdate)
                                updateSaveFiles();
                        });
                    });
                    UIButton seed = new UIButton(r, b, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                            1, 12.5f, 7.5f, 1.5f, 1f, true)
                            .setColourTheme(GREEN_SELECTED).noDeselect().select().setText("Generated").setBold()
                            .tooltip(t -> t.add(-1, AbstractUITooltip.light(), "Use a procedurally generated map"));
                    UIButton custom = new UIButton(r, b, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                            9.5f, 12.5f, 7.5f, 1.5f, 1f, true)
                            .setColourTheme(GREEN_SELECTED).noDeselect().setText("Custom").setBold()
                            .tooltip(t -> t.add(-1, AbstractUITooltip.light(), "Use a custom map created in the Map Editor"));
                    seed.setOnClick(() -> {
                        custom.deselect();
                        seedContainer.setEnabled(true);
                        customContainer.setEnabled(false);
                        customMap = false;
                        playerBoxes.unlockPlayers();
                    });
                    custom.setOnClick(() -> {
                        seed.deselect();
                        seedContainer.setEnabled(false);
                        customContainer.setEnabled(true);
                        customMap = true;
                        playerBoxes.lockPlayers();
                        playerBoxes.verifyTeams();
                    });
                }).addTab(3.5f, "Players", (r, b) -> {
                    playerBoxScrollWindow = new UIScrollSurface(r, b, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS, 0.5f, 0.5f, 17, 14, (r2, b2) -> {
                        playerBoxes = new UIPlayerBoxes(r2, b2, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS, 0, 0);
                    }).addScrollBar(0.4f, 0.4f, -0.2f);
                    playerShipSettingsContainer = new UIContainer(r, b, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS, -20, 0).addRenderables((r2, b2) -> {
                        new RenderElement(r2, RenderOrder.TITLE_SCREEN_BUTTON_BACKGROUND, new UIBox(18, Renderable.top() - TOP_MARGIN)
                                .setColourTheme(LIGHT_BLUE_BOX_DARK)
                                .centerOnly())
                                .translate(0, 4);
                        new RenderElement(r2, RenderOrder.TITLE_SCREEN_BUTTONS, new UIBox(18, Renderable.top() - TOP_MARGIN)
                                .setColourTheme(LIGHT_BLUE_BOX_DARK)
                                .borderOnly())
                                .translate(0, 4).setZOrder(1);
                        new UIScrollSurface(r2, b2, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS, 0, 4, 17, Renderable.top() - TOP_MARGIN, (r3, b3) -> {
                            playerShipSettings = new UIPlayerShipSettings(r3, b3, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS);
                        }).setScrollMax(playerShipSettings.scrollDistance() - (Renderable.top() - TOP_MARGIN)).addScrollBar(.6f, .4f, .5f).setScrollSpeed(0.4f);
                        new UIButton(r2, b2, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS, 0, 2, 8.5f, 1.5f, 0.9f, false, () -> {
                            clipboardPreset = playerShipSettings.getCurrentPreset();
                            boolean canPaste = clipboardPreset != null;
                            pasteSettingsButton.setClickEnabled(canPaste).setColourTheme(canPaste ? LIGHT_BLUE_BOX : GRAYED_OUT_OPAQUE);
                        }).setText("Copy Settings").setBold().setColourTheme(LIGHT_BLUE_BOX);
                        pasteSettingsButton = new UIButton(r2, b2, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS, 9.5f, 2, 8.5f, 1.5f, 0.9f, false, () -> {
                            if (clipboardPreset == null)
                                return;
                            playerShipSettings.loadPresetForCurrentTeam(clipboardPreset);
                            playerBoxes.verifyTeams();
                        }).setText("Paste Settings").setBold().setColourTheme(GRAYED_OUT_OPAQUE);
                        new UIButton(r2, b2, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS, 0, 0, 18, 1.5f, 0.9f, false, () -> {
                            playerShipSettings.loadPreset(playerShipSettings.getCurrentPreset());
                            playerBoxes.verifyTeams();
                        }).setText("Copy settings to all players").setBold().setColourTheme(LIGHT_BLUE_BOX);
                        new UIButton(r2, b2, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS, 0, -2, 18, 1.5f, 0.9f, false, () -> {
                            playerShipSettings.loadPreset(DEFAULT_PRESET);
                            playerBoxes.verifyTeams();
                        }).setText("Reset all settings to default").setBold().setColourTheme(LIGHT_BLUE_BOX);
                    }).setEnabled(false);
                }).addTab(4, "Gameplay", (r, b) -> {
                    new RenderElement(r, RenderOrder.TITLE_SCREEN_BUTTONS,
                            new UITextLabel(7f, 1, false, 0, 0.7f).setTextCenterBold()
                                    .updateTextCenter("Bot Difficulty:")
                                    .translate(0.7f, 13.5f),
                            new UITextLabel(7f, 1, false, 0, 0.7f).setTextCenterBold()
                                    .updateTextCenter("Fog of War:")
                                    .translate(0.7f, 13.5f - 1.5f * 1),
                            new UITextLabel(7f, 1, false, 0, 0.7f).setTextCenterBold()
                                    .updateTextCenter("Firing Animation:")
                                    .translate(0.7f, 13.5f - 1.5f * 2)
                    );
                    botDifficultySelector = new UIEnumSelector<>(r, b, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                            9, 13.5f, 1f, 4.5f, BotDifficulty.class, BotDifficulty.VERY_EASY).setCorner(0.3f)
                            .tooltip(t -> t.add(-1, AbstractUITooltip.light(), "Sets the difficulty for all bots"));
                    toggleFoW = new UIButton(r, b, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                            9, 13.5f - 1.5f * 1, botDifficultySelector.totalWidth(), 1f, 0.8f, false)
                            .noDeselect().setBold().setBoxCorner(0.3f).setOnClick(() -> {
                                if (toggleFoW.getText().equals("Enabled")) {
                                    toggleFoW.setColourTheme(DEEP_RED);
                                    toggleFoW.setText("Disabled");
                                } else {
                                    toggleFoW.setColourTheme(ALWAYS_GREEN_SELECTED);
                                    toggleFoW.setText("Enabled");
                                }
                            }).tooltip(t -> t.add(-1, AbstractUITooltip.light(), "If disabled, tiles will always be visible"));
                    toggleFoW.select();
                    showFiringAnim = new UIButton(r, b, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                            9, 13.5f - 1.5f * 2, botDifficultySelector.totalWidth(), 1f, 0.8f, false)
                            .noDeselect().setBold().setBoxCorner(0.3f).setOnClick(() -> {
                                if (showFiringAnim.getText().equals("Enabled")) {
                                    showFiringAnim.setColourTheme(DEEP_RED);
                                    showFiringAnim.setText("Disabled");
                                } else {
                                    showFiringAnim.setColourTheme(ALWAYS_GREEN_SELECTED);
                                    showFiringAnim.setText("Enabled");
                                }
                            }).tooltip(t -> t.add(-1, AbstractUITooltip.light(), "If disabled, the animation when firing will not be shown"));
                    showFiringAnim.select();
                }).setOnNewTabSelected(() -> {
                    updateMultiplayerConnectButtons();
                    editStructures.deselect();
                }).setEnabled(false);

        loadContainer = new UIContainer(renderer, buttonRegister, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                Renderable.right() - 30, 2).addRenderables((r, b) -> {
            loadLabel = new UITextLabel(14.3f, 1, true)
                    .setTextLeftBold();
            new RenderElement(r, RenderOrder.TITLE_SCREEN_BUTTON_BACKGROUND,
                    loadLabel.translate(-2.3f, 17.5f)
            );
            loadGameLocally = new UIButton(r, b, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS, -1, 0.3f, 5.75f, 1.2f, .8f, false)
                    .setText("Load locally").setBoxCorner(0.35f).setBold().setOnClick(() -> {
                        MainPanel.startNewLevel(() -> loadLevel(loadBox.getSelected(), false));
                    });
            loadGameToLan = new UIButton(r, b, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS, 5.25f, 0.3f, 5.75f, 1.2f, .8f, false)
                    .setText("Load to LAN").setBoxCorner(0.35f).setBold().setOnClick(() -> {
                        MainPanel.startNewLevel(() -> loadLevel(loadBox.getSelected(), true));
                    });
            UIContainer mapViewContainer = new UIContainer(r, b, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                    -28.5f, 1.5f).addRenderables((r2, b2) -> {
                new RenderElement(r2, RenderOrder.TITLE_SCREEN_BACKGROUND,
                        new UIBox(25, 16).setColourTheme(LIGHT_BLUE_BOX_DARK)
                );
            });
            loadBox = new UISaveBox<>(r, b, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                    -2, 2, 14, 15, 1.5f, MainPanel.levelSaves).enableClick()
                    .setOnClickOrUpdate(fromUpdate -> {
                        updateLoadButtons();
                        if (loadBox.hasSelectedSave()) {
                            if (loadGamePreview != null)
                                loadGamePreview.delete();
                            mapViewContainer.addRenderables((r2, b2) -> {
                                loadGamePreview = MapUI.box(r2, RenderOrder.TITLE_SCREEN_BUTTONS, loadBox.getSelected(), 25, 16, 0.15f);
                            }).setEnabled(true);
                        } else {
                            mapViewContainer.setEnabled(false);
                        }
                        if (!fromUpdate)
                            updateSaveFiles();
                    });
        });
        loadContainer.setEnabled(false);

        levelEditorContainer = new UIContainer(renderer, buttonRegister, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                Renderable.right() - 18, 4).addRenderables((r, b) -> {
            new RenderElement(r, RenderOrder.TITLE_SCREEN_BACKGROUND, new UIBox(12, 6f).setColourTheme(LIGHT_BLUE_TRANSPARENT_CENTER).translate(-12, -1.5f));
            levelEditorWidthSelector = new UINumberSelector(r, b, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                    -5, 2.5f, 1f, 1.5f, Level.MIN_WIDTH, Level.MAX_WIDTH, 20).setCorner(0.3f);
            levelEditorHeightSelector = new UINumberSelector(r, b, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                    -5, 1f, 1f, 1.5f, Level.MIN_HEIGHT, Level.MAX_HEIGHT, 12).setCorner(0.3f);
            levelEditorPlayerSelector = new UINumberSelector(r, b, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                    -5, -.5f, 1f, 1.5f, 2, UnitTeam.ORDERED_TEAMS.length, 2).setCorner(0.3f);
            new RenderElement(r, RenderOrder.TITLE_SCREEN_BUTTONS,
                    new UITextLabel(5.5f, 1, false).setTextCenterBold()
                            .updateTextCenter("Width:")
                            .translate(-11.5f, 2.5f),
                    new UITextLabel(5.5f, 1, false).setTextCenterBold()
                            .updateTextCenter("Height:")
                            .translate(-11.5f, 1f),
                    new UITextLabel(5.5f, 1, false).setTextCenterBold()
                            .updateTextCenter("Players:")
                            .translate(-11.5f, -.5f),
                    new UITextLabel(10f, 1, false).setTextCenterBold()
                            .updateTextCenter("Create new map")
                            .translate(-11.25f, 5f),
                    new UITextLabel(10f, 1, false).setTextCenterBold()
                            .updateTextCenter("Select map to load")
                            .translate(-11.25f, 19.5f)
            );
            new UIButton(r, b, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                    2 - 12, -3.5f, 8, 1.5f, 1.2f, false)
                    .setText("Create Map").setBold().setOnClick(() -> {
                        MainPanel.startNewLevel(this::getNewLevelEditor);
                    });
            mapLoadButton = new UIButton(r, b, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                    2 - 12, 7f, 8, 1.5f, 1.2f, false)
                    .setText("Load Map").setBold().setOnClick(() -> {
                        MainPanel.startNewLevel(() -> mapSaveBox.getSelected().createLevelEditor());
                    });
            UIContainer mapViewContainer = new UIContainer(r, b, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                    -40, 1).addRenderables((r2, b2) -> {
                new RenderElement(r2, RenderOrder.TITLE_SCREEN_BACKGROUND,
                        new UIBox(25, 16).setColourTheme(LIGHT_BLUE_BOX_DARK)
                );
            });
            mapSaveBox = new UISaveBox<>(r, b, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                    -12, 9, 12, 10, 1.5f, MainPanel.mapSaves)
                    .enableClick().setOnClickOrUpdate(fromUpdate -> {
                        if (mapSaveBox.hasSelectedSave()) {
                            mapLoadButton.setClickEnabled(true).setColourTheme(LIGHT_BLUE).setText("Load Map");
                            if (loadMapPreview != null)
                                loadMapPreview.delete();
                            mapViewContainer.addRenderables((r2, b2) -> {
                                loadMapPreview = MapUI.box(r2, RenderOrder.TITLE_SCREEN_BUTTONS, mapSaveBox.getSelected(), 25, 16, 0.15f);
                            }).setEnabled(true);
                        } else {
                            mapLoadButton.setClickEnabled(false).setColourTheme(GRAYED_OUT).setText("Load Map");
                            mapViewContainer.setEnabled(false);
                        }
                        if (!fromUpdate)
                            updateSaveFiles();
                    });
            mapSaveBox.updateSaves(false);
        }).setEnabled(false);
        new TooltipRenderer(renderer, RenderOrder.TOOLTIP);
        reset();
    }

    public void reset() {
        multiplayer.deselect();
        connectToLan.deselect();
        newGame.deselect();
        enterIPBox.deselect();
        connectButton.setText("Connect").setColourTheme(UIColourTheme.LIGHT_BLUE);
        enterSeedBox.deselect();
        multiplayerTabs.selectTab(0);
        updateColourSelectorVisibility();
        updateSaveFiles();
        updateMultiplayerConnectButtons();
    }

    public float mainButtonHeight(int index) {
        return MAIN_BUTTON_B0X_HEIGHT - (MAIN_BUTTON_SPACING + MAIN_BUTTON_HEIGHT) * (index + 1) + MAIN_BUTTON_Y_OFFSET;
    }

    public void updateMultiplayerConnectButtons() {
        if (multiplayerTabs.lastTabSelected()) {
            startLocalGame.setText("Start game locally").tooltip(TooltipManager::show).tooltip(t -> t.setText("Start the game with local play. Players take turns playing on the host device."))
                    .setColourTheme(LIGHT_BLUE).setTextSize(0.7f);
            startLanGame.setText("Start game on LAN").tooltip(TooltipManager::show).tooltip(t -> t.setText("Start the game with online play. " +
                    "All other players must join from separate devices using this device's local IP address. " +
                    "Only players connected to the same network can join.")).setTextSize(0.7f);
        } else {
            startLocalGame.setText("Back").tooltip(TooltipManager::hide).setColourTheme(multiplayerTabs.getSelectedTab() == 0 ? GRAYED_OUT : LIGHT_BLUE).setTextSize(1.2f);
            startLanGame.setText("Next").tooltip(TooltipManager::hide).setTextSize(1.2f);
        }
    }

    private void createColourSelectorButtons() {
        colourSelectorButtons = new UIButton[UnitTeam.ORDERED_TEAMS.length];
        float width = 3.2f, spacing = (14 - width * 4) / 3;
        for (int i = 0; i < colourSelectorButtons.length; i++) {
            int finalI = i;
            connectContainer.addRenderables((r, b) -> {
                colourSelectorButtons[finalI] = new UIButton(r, b, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                        Renderable.right() - 35 + (width + spacing) * (finalI % 4), 7.5f - 1.7f * (finalI / 4), width, 1.3f, 0.6f, true, () -> {
                    deselectOtherColourSelectors(colourSelectorButtons[finalI]);
                    selectedTeam = UnitTeam.ORDERED_TEAMS[finalI];
                    updateColourSelectorVisibility();
                });
            });
            colourSelectorButtons[i].noDeselect().setEnabled(false).setBold();
        }
    }

    public UnitTeam selectedTeam = null;

    public void updateColourSelectorVisibility() {
        if (MainPanel.client != null && MainPanel.client.connected && connectContainer.isEnabled()) {
            for (UIButton colourSelectorButton : colourSelectorButtons) {
                colourSelectorButton.setEnabled(true);
            }
            for (int i = 0; i < colourSelectorButtons.length; i++) {
                if (i < MainPanel.client.playerCount) {
                    UnitTeam team = UnitTeam.ORDERED_TEAMS[i];
                    colourSelectorButtons[i].setEnabled(true);
                    if (MainPanel.client.teamClientIDs.containsKey(team) || MainPanel.client.bots.getOrDefault(team, false))
                        colourSelectorButtons[i].setColourTheme(team.unavailableColour)
                                .setText(MainPanel.client.bots.getOrDefault(team, false) ? "Bot" : "Taken");
                    else
                        colourSelectorButtons[i].setColourTheme(team.uiColour)
                                .setText(team == selectedTeam ? "Selected" : "Available");
                } else {
                    colourSelectorButtons[i].setEnabled(false);
                }
            }
            if (selectedTeam != null && !colourSelectorButtons[selectedTeam.order].getText().equals("Selected")) {
                colourSelectorButtons[selectedTeam.order].deselect();
                selectedTeam = null;
            }
            joinButton.setEnabled(selectedTeam != null);
            colourSelectorBox.setEnabled(true);
        } else {
            joinButton.setEnabled(false);
            colourSelectorBox.setEnabled(false);
            for (UIButton colourSelectorButton : colourSelectorButtons) {
                colourSelectorButton.setEnabled(false);
            }
        }
    }

    private void deselectOtherColourSelectors(UIButton thisButton) {
        for (UIButton colourSelectorButton : colourSelectorButtons) {
            if (colourSelectorButton != thisButton)
                colourSelectorButton.deselect();
        }
    }

    private LevelEditor getNewLevelEditor() {
        return new LevelEditor(levelEditorWidthSelector.getValue(), levelEditorHeightSelector.getValue(), new Random().nextLong(), levelEditorPlayerSelector.getValue());
    }

    private Level getNewLocalMultiplayerLevel() {
        long seed = enterSeedBox.getText().isEmpty() ? new Random().nextLong() : Long.parseLong(enterSeedBox.getText());
        return new Level(playerBoxes.getTeams(), seed, widthSelector.getValue(), heightSelector.getValue(), playerBoxes.getBots(), new GameplaySettings(this),
                NetworkState.LOCAL, botDifficultySelector.getValue().difficulty)
                .generateDefaultTerrain(new TeamSpawner(playerShipSettings.getUnits(), structureGenerationSettings.getPreset()));
    }

    private Level getNewServerMultiplayerLevel() {
        long seed = enterSeedBox.getText().isEmpty() ? new Random().nextLong() : Long.parseLong(enterSeedBox.getText());
        return new Level(playerBoxes.getTeams(), seed, widthSelector.getValue(), heightSelector.getValue(), playerBoxes.getBots(), new GameplaySettings(this),
                NetworkState.SERVER, botDifficultySelector.getValue().difficulty)
                .generateDefaultTerrain(new TeamSpawner(playerShipSettings.getUnits(), structureGenerationSettings.getPreset()));
    }

    private Level loadLevel(GameSave save, boolean server) {
        Level level = new Level(new HashMap<>(save.teams), save.seed, save.levelWidth, save.levelHeight, save.bots, save.gameplaySettings,
                server ? NetworkState.SERVER : NetworkState.LOCAL, save.botDifficulty);
        save.loadLevel(level);
        return level;
    }

    private Level loadCustomLevel(MapSave save, boolean server) {
        Level level = new Level(playerBoxes.getTeams(), save.seed, save.levelWidth, save.levelHeight, playerBoxes.getBots(), new GameplaySettings(this),
                server ? NetworkState.SERVER : NetworkState.LOCAL, botDifficultySelector.getValue().difficulty);
        save.loadLevel(level);
        return level;
    }

    private Level loadTutorialLevel(MapSave save, TutorialLevelData tutorialData) {
        Level level = new Level(tutorialData.teams, save.seed, save.levelWidth, save.levelHeight, tutorialData.bots, tutorialData.settings,
                NetworkState.LOCAL, tutorialData.botDifficulty);
        save.loadLevel(level);
        return level;
    }

    private void updateSaveFiles() {
        MainPanel.addTask(() -> {
            loadBox.updateSaves(true);
            mapSaveBox.updateSaves(true);
            loadCustomBox.updateSaves(true);
            updateLoadButtons();
        });
    }

    private void updateLoadButtons() {
        if (loadBox.hasSelectedSave() && loadContainer.isEnabled()) {
            loadGameToLan.setEnabled(true);
            loadGameLocally.setEnabled(true);
        } else {
            loadGameToLan.setEnabled(false);
            loadGameLocally.setEnabled(false);
        }
        loadLabel.updateTextLeft(loadBox.getElements().isEmpty() ? "No save files detected" : "Saved games");
    }

    @Override
    public void render(Graphics2D g) {
        renderer.render(g);
        if (MainPanel.activeInputReceiver == this)
            buttonRegister.input(true, InputType.MOUSE_OVER);
    }

    @Override
    public void acceptPressed(InputType type) {
        buttonRegister.input(true, type);
    }

    @Override
    public void acceptReleased(InputType type) {
        buttonRegister.input(false, type);
    }
}