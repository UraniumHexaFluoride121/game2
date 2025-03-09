package mainScreen;

import foundation.MainPanel;
import foundation.input.*;
import level.Level;
import level.TeamSpawner;
import network.NetworkState;
import render.GameRenderer;
import render.RenderOrder;
import render.Renderable;
import render.renderables.RenderElement;
import render.texture.ResourceLocation;
import render.ui.UIColourTheme;
import render.ui.types.*;
import save.GameSave;
import save.SaveManager;
import unit.UnitTeam;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import static mainScreen.UIPlayerShipSettings.*;
import static render.ui.UIColourTheme.*;

public class TitleScreen implements Renderable, InputReceiver {
    private static final Renderable TITLE_SCREEN_IMAGE = Renderable.renderImage(new ResourceLocation("title_screen.png"), false, true, 60, true);
    private final GameRenderer renderer = new GameRenderer(MainPanel.windowTransform, null);
    private final ButtonRegister buttonRegister = new ButtonRegister();
    private UIButton newGame, multiplayer, connectToLan, loadGame;

    public UITabSwitcher newGameTabs;
    public UIScrollSurface playerBoxScrollWindow;
    public UIButton startLocalGame, startLanGame;
    public UITextDisplayBox gameCannotBeStarted;
    public UIPlayerBoxes playerBoxes;
    public UITextInputBox enterSeedBox;
    public UINumberSelector widthSelector, heightSelector;

    public UITextInputBox enterIPBox;
    public RenderElement enterIPLabel;
    public UIButton connectButton;
    public UIButton[] colourSelectorButtons;
    public UIButton joinButton;
    public RenderElement colourSelectorBox;
    public UIContainer connectContainer;

    public UIContainer playerShipSettingsContainer;
    public UIPlayerShipSettings playerShipSettings;
    public UIButton pasteSettingsButton;

    public UIContainer loadContainer;
    public UIScrollSurface loadScrollSurface;
    public ArrayList<UIContainer> loadSaveFiles = new ArrayList<>();
    public UIButton loadGameLocally, loadGameToLan;
    public GameSave selectedSave = null;
    public UITextLabel loadLabel;

    public TitleScreen() {
    }

    public void init() {
        new UIButton(renderer, buttonRegister, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS, 0.5f, Renderable.top() - 2.5f, 8, 2, 1.4f, false, () -> System.exit(0))
                .setText("Quit Game").setColourTheme(UIColourTheme.DEEP_RED).setBold();
        new RenderElement(renderer, RenderOrder.TITLE_SCREEN_BACKGROUND, TITLE_SCREEN_IMAGE);
        new RenderElement(renderer, RenderOrder.TITLE_SCREEN_BUTTON_BACKGROUND, new UIBox(12, 17).setColourTheme(UIColourTheme.LIGHT_BLUE_TRANSPARENT_CENTER).translate(Renderable.right() - 16, 3));
        newGame = new UIButton(renderer, buttonRegister, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                Renderable.right() - 15, 16 - 4 * 0, 10, 3, 1.8f, true, () -> {
            multiplayer.deselect();
            connectToLan.deselect();
            loadGame.deselect();
        }).setText("New Game").setBold().noDeselect().setColourTheme(UIColourTheme.GREEN_SELECTED);

        multiplayer = new UIButton(renderer, buttonRegister, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                Renderable.right() - 15, 16 - 4 * 1, 10, 3, 1.6f, true, () -> {
            newGame.deselect();
            connectToLan.deselect();
            loadGame.deselect();
            boolean verifyTeams = playerBoxes.getVerifyTeams();
            newGameTabs.setEnabled(true);
            startLocalGame.setEnabled(verifyTeams);
            startLanGame.setEnabled(verifyTeams);
            gameCannotBeStarted.setEnabled(!verifyTeams);
        }).setOnDeselect(() -> {
            newGameTabs.setEnabled(false);
            startLocalGame.setEnabled(false);
            startLanGame.setEnabled(false);
            gameCannotBeStarted.setEnabled(false);
        }).setText("Multiplayer").setBold().noDeselect().setColourTheme(UIColourTheme.GREEN_SELECTED);

        connectContainer = new UIContainer(renderer, buttonRegister, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS, 0, 0).addRenderables((r, b) -> {
            enterIPBox = new UITextInputBox(r, b, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                    Renderable.right() - 36, 14, 16, 2, .7f, true, 39,
                    InputType::isIPChar);
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
                    Renderable.right() - 32.5f, 5.2f, 9f, 1.5f, 0.8f, false, () -> {
                MainPanel.client.sendJoinRequest(selectedTeam);
            }).setText("Join").setBold().setEnabled(false);
            colourSelectorBox = new RenderElement(r, RenderOrder.TITLE_SCREEN_BUTTON_BACKGROUND,
                    new UIBox(16, 6.8f).setColourTheme(UIColourTheme.LIGHT_BLUE_TRANSPARENT_CENTER)
                            .translate(Renderable.right() - 36, 4.2f)
            );
            colourSelectorBox.setEnabled(false);
            new OnButtonInput(b, ButtonOrder.MAIN_BUTTONS, t -> t == InputType.ENTER, () -> {
                if (joinButton.isEnabled())
                    joinButton.select();
                else if (enterIPBox.isEnabled() && !connectButton.getText().equals("Connected!"))
                    connectButton.select();
            });
        }).setEnabled(false);
        createColourSelectorButtons();

        connectToLan = new UIButton(renderer, buttonRegister, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                Renderable.right() - 15, 16 - 4 * 2, 10, 3, 1.2f, true, () -> {
            newGame.deselect();
            multiplayer.deselect();
            loadGame.deselect();
            connectContainer.setEnabled(true);
            updateColourSelectorVisibility();
        }).setOnDeselect(() -> {
            connectContainer.setEnabled(false);
            updateColourSelectorVisibility();
        }).setText("Connect to LAN").setBold().noDeselect().setColourTheme(UIColourTheme.GREEN_SELECTED);

        startLocalGame = new UIButton(renderer, buttonRegister, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                Renderable.right() - 37 + 1, 3.5f - 2, 7, 1.5f, 0.7f, false, () -> {
            MainPanel.startNewLevel(this::getNewLocalMultiplayerLevel);
        }).setText("Start game locally").setBold().setEnabled(false);
        startLanGame = new UIButton(renderer, buttonRegister, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                Renderable.right() - 37 + 9, 3.5f - 2, 8, 1.5f, 0.7f, false, () -> {
            MainPanel.startNewLevel(this::getNewServerMultiplayerLevel);
        }).setText("Start game on LAN").setBold().setEnabled(false);
        gameCannotBeStarted = new UITextDisplayBox(renderer, RenderOrder.TITLE_SCREEN_BUTTONS,
                Renderable.right() - 37 + 1, 3.5f - 2, 16, 1.5f, 0.7f)
                .setBold().setEnabled(false).setColourTheme(UIColourTheme.DEEP_RED);
        newGameTabs = new UITabSwitcher(renderer, buttonRegister, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                Renderable.right() - 37, 3.5f, 18, 15)
                .addTab(4.65f, "Map Layout", (r, b) -> {
                    UIButton seed = new UIButton(r, b, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                            1, 12, 7, 1.5f, 1f, true)
                            .setColourTheme(UIColourTheme.GREEN_SELECTED).noDeselect().select().setText("From seed").setBold();
                    widthSelector = new UINumberSelector(r, b, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                            1, 7.5f, 1.5f, 2.5f, 10, 45, 20);
                    RenderElement widthLabel = new RenderElement(r, RenderOrder.TITLE_SCREEN_BUTTONS,
                            new UITextLabel(6.5f, 1, false).setTextLeftBold()
                                    .updateTextLeft("Level width").setLeftOffset(0.5f)
                                    .translate(1, 9.5f)
                    );
                    heightSelector = new UINumberSelector(r, b, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                            9, 7.5f, 1.5f, 2.5f, 7, 25, 12);
                    RenderElement heightLabel = new RenderElement(r, RenderOrder.TITLE_SCREEN_BUTTONS,
                            new UITextLabel(6.5f, 1, false).setTextLeftBold()
                                    .updateTextLeft("Level height").setLeftOffset(0.35f)
                                    .translate(9, 9.5f)
                    );
                    RenderElement enterSeedLabel = new RenderElement(r, RenderOrder.TITLE_SCREEN_BUTTONS,
                            new UITextLabel(15, 1, true, 0.2f).setTextLeftBold().setLeftOffset(0.1f).updateTextLeft("Enter seed:")
                                    .translate(1, 5)
                    );
                    enterSeedBox = new UITextInputBox(r, b, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                            2, 1, 13, 3, 1.5f, true, 15, InputType::isDigit);
                    enterSeedBox.setColourTheme(UIColourTheme.GREEN_SELECTED).setBold();
                }).addTab(3.5f, "Players", (r, b) -> {
                    playerBoxScrollWindow = new UIScrollSurface(r, b, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS, 0.5f, 0.5f, 17, 14, (r2, b2) -> {
                        playerBoxes = new UIPlayerBoxes(r2, b2, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS, 0, 0);
                    });
                    playerShipSettingsContainer = new UIContainer(r, b, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS, -20, 0, (r2, b2) -> {
                        new RenderElement(r2, RenderOrder.TITLE_SCREEN_BUTTON_BACKGROUND, new UIBox(18, Renderable.top() - TOP_MARGIN)
                                .setColourTheme(LIGHT_BLUE_OPAQUE_CENTER)
                                .centerOnly())
                                .translate(0, 4);
                        new RenderElement(r2, RenderOrder.TITLE_SCREEN_BUTTONS, new UIBox(18, Renderable.top() - TOP_MARGIN)
                                .setColourTheme(LIGHT_BLUE_OPAQUE_CENTER)
                                .borderOnly())
                                .translate(0, 4).setZOrder(1);
                        new UIScrollSurface(r2, b2, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS, 0, 4, 17, Renderable.top() - TOP_MARGIN, (r3, b3) -> {
                            playerShipSettings = new UIPlayerShipSettings(r3, b3, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS);
                        }).setScrollMax(playerShipSettings.scrollDistance() - (Renderable.top() - TOP_MARGIN));
                        new UIButton(r2, b2, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS, 0, 2, 8.5f, 1.5f, 0.9f, false, () -> {
                            UIPlayerShipSettings.clipboardPreset = playerShipSettings.getCurrentPreset();
                            boolean canPaste = UIPlayerShipSettings.clipboardPreset != null;
                            pasteSettingsButton.setClickEnabled(canPaste).setColourTheme(canPaste ? LIGHT_BLUE_OPAQUE_CENTER_LIGHT : GRAYED_OUT_OPAQUE);
                        }).setText("Copy Settings").setBold().setColourTheme(LIGHT_BLUE_OPAQUE_CENTER_LIGHT);
                        pasteSettingsButton = new UIButton(r2, b2, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS, 9.5f, 2, 8.5f, 1.5f, 0.9f, false, () -> {
                            if (clipboardPreset == null)
                                return;
                            playerShipSettings.loadPresetForCurrentTeam(UIPlayerShipSettings.clipboardPreset);
                        }).setText("Paste Settings").setBold().setColourTheme(GRAYED_OUT_OPAQUE);
                        new UIButton(r2, b2, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS, 0, 0, 18, 1.5f, 0.9f, false, () -> {
                            playerShipSettings.loadPreset(playerShipSettings.getCurrentPreset());
                        }).setText("Copy settings to all players").setBold().setColourTheme(LIGHT_BLUE_OPAQUE_CENTER_LIGHT);
                        new UIButton(r2, b2, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS, 0, -2, 18, 1.5f, 0.9f, false, () -> {
                            playerShipSettings.loadPreset(UIPlayerShipSettings.DEFAULT_PRESET);
                        }).setText("Reset all settings to default").setBold().setColourTheme(LIGHT_BLUE_OPAQUE_CENTER_LIGHT);
                    }).setEnabled(false);
                }).setEnabled(false);

        loadGame = new UIButton(renderer, buttonRegister, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                Renderable.right() - 15, 16 - 4 * 3, 10, 3, 1.55f, true, () -> {
            multiplayer.deselect();
            connectToLan.deselect();
            newGame.deselect();
            loadContainer.setEnabled(true);
            updateSaveFiles();
            updateLoadButtons();
        }).setOnDeselect(() -> {
            loadContainer.setEnabled(false);
            updateLoadButtons();
        }).setText("Load Game").setBold().noDeselect().setColourTheme(UIColourTheme.GREEN_SELECTED);
        loadContainer = new UIContainer(renderer, buttonRegister, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                Renderable.right() - 30, 2).addRenderables((r, b) -> {
            loadLabel = new UITextLabel(12.3f, 1, true)
                    .setTextLeftBold();
            new RenderElement(r, RenderOrder.TITLE_SCREEN_BUTTON_BACKGROUND,
                    new UIBox(12, 15).setColourTheme(UIColourTheme.LIGHT_BLUE_TRANSPARENT_CENTER).translate(0, 2),
                    loadLabel.translate(-.3f, 17.5f)
            );
            loadScrollSurface = new UIScrollSurface(r, b, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                    0, 2, 12, 15, false, (r2, b2) -> {
            });
            loadGameLocally = new UIButton(r, b, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS, 0, 0.3f, 5.75f, 1.2f, .8f, false)
                    .setText("Load locally").setBoxCorner(0.35f).setBold().setOnClick(() -> {
                        MainPanel.startNewLevel(() -> loadLevel(selectedSave, false));
                    });
            loadGameToLan = new UIButton(r, b, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS, 6.25f, 0.3f, 5.75f, 1.2f, .8f, false)
                    .setText("Load to LAN").setBoxCorner(0.35f).setBold().setOnClick(() -> {
                        MainPanel.startNewLevel(() -> loadLevel(selectedSave, true));
                    });
            ;
        });
        loadContainer.setEnabled(false);
    }

    public void reset() {
        multiplayer.deselect();
        connectToLan.deselect();
        newGame.deselect();
        enterIPBox.deselect();
        connectButton.setText("Connect").setColourTheme(UIColourTheme.LIGHT_BLUE);
        enterSeedBox.deselect();
        newGameTabs.selectTab(0);
        updateColourSelectorVisibility();
        updateSaveFiles();
    }

    private void createColourSelectorButtons() {
        colourSelectorButtons = new UIButton[UnitTeam.ORDERED_TEAMS.length];
        float width = 3.2f, spacing = (14 - width * 4) / 3;
        for (int i = 0; i < colourSelectorButtons.length; i++) {
            int finalI = i;
            connectContainer.addRenderables((r, b) -> {
                colourSelectorButtons[finalI] = new UIButton(r, b, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                        Renderable.right() - 35 + (width + spacing) * (finalI % 4), 8.875f - 1.7f * (finalI / 4), width, 1.3f, 0.6f, true, () -> {
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
                    if (MainPanel.client.teamClientIDs.containsKey(team))
                        colourSelectorButtons[i].setColourTheme(team.unavailableColour)
                                .setText("Taken");
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

    private Level getNewLocalMultiplayerLevel() {
        long seed = enterSeedBox.getText().isEmpty() ? new Random().nextLong() : Long.parseLong(enterSeedBox.getText());
        return new Level(playerBoxes.getTeams(), seed, widthSelector.getValue(), heightSelector.getValue(), NetworkState.LOCAL).generateDefaultTerrain(new TeamSpawner().setUnits(playerShipSettings.getUnits()));
    }

    private Level getNewServerMultiplayerLevel() {
        long seed = enterSeedBox.getText().isEmpty() ? new Random().nextLong() : Long.parseLong(enterSeedBox.getText());
        return new Level(playerBoxes.getTeams(), seed, widthSelector.getValue(), heightSelector.getValue(), NetworkState.SERVER).generateDefaultTerrain(new TeamSpawner().setUnits(playerShipSettings.getUnits()));
    }

    private Level loadLevel(GameSave save, boolean server) {
        Level level = new Level(new HashMap<>(save.teams), save.seed, save.levelWidth, save.levelHeight, server ? NetworkState.SERVER : NetworkState.LOCAL);
        save.loadLevel(level);
        return level;
    }

    private void updateSaveFiles() {
        loadSaveFiles.forEach(UIContainer::delete);
        loadSaveFiles.clear();
        selectedSave = null;
        ArrayList<UIButton> buttons = new ArrayList<>();
        loadScrollSurface.addRenderables((r, b) -> {
            AtomicInteger i = new AtomicInteger();
            SaveManager.forEachSave((n, s) -> {
                loadSaveFiles.add(new UIContainer(r, b, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS, 1, -2f * (i.get() + 1))
                        .addRenderables((r2, b2) -> {
                            UIButton button = new UIButton(r2, b2, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS, 0, 0, 8, 1.5f, 0.8f, true)
                                    .setText(n).setBold().setBoxCorner(0.3f).setColourTheme(UIColourTheme.GREEN_SELECTED).noDeselect();
                            button.setOnClick(() -> {
                                selectSave(s, button, buttons);
                            });
                            buttons.add(button);
                            new UIShapeButton(r2, b2, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS, 8.5f, 0, 1.5f, 1.5f, false)
                                    .setColourTheme(DEEP_RED).setShape(UIShapeButton::smallX).setBoxCorner(0.3f).setOnClick(() -> {
                                        SaveManager.removeSave(n);
                                        MainPanel.addTask(this::updateSaveFiles);
                                    });
                        }));
                i.getAndIncrement();
            });
        });
        loadScrollSurface.setScrollMax(loadSaveFiles.size() * 2f - loadScrollSurface.height);
        updateLoadButtons();
    }

    private void selectSave(GameSave save, UIButton button, ArrayList<UIButton> buttons) {
        selectedSave = save;
        buttons.forEach(b -> {
            if (b != button)
                b.deselect();
        });
        updateLoadButtons();
    }

    private void updateLoadButtons() {
        if (selectedSave != null && loadContainer.isEnabled()) {
            loadGameToLan.setEnabled(true);
            loadGameLocally.setEnabled(true);
        } else {
            loadGameToLan.setEnabled(false);
            loadGameLocally.setEnabled(false);
        }
        loadLabel.updateTextLeft(loadSaveFiles.isEmpty() ? "No save files detected" : "Saved games");
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