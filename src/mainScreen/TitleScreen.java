package mainScreen;

import foundation.MainPanel;
import foundation.input.ButtonOrder;
import foundation.input.ButtonRegister;
import foundation.input.InputReceiver;
import foundation.input.InputType;
import level.Level;
import network.NetworkState;
import render.GameRenderer;
import render.RenderOrder;
import render.Renderable;
import render.renderables.RenderElement;
import render.texture.ResourceLocation;
import render.ui.UIColourTheme;
import render.ui.implementation.UIPlayerBoxes;
import render.ui.types.*;
import unit.UnitTeam;

import java.awt.*;
import java.util.Random;

public class TitleScreen implements Renderable, InputReceiver {
    private static final Renderable TITLE_SCREEN_IMAGE = Renderable.renderImage(new ResourceLocation("title_screen.png"), false, true, 60, true);
    private final GameRenderer renderer = new GameRenderer(MainPanel.windowTransform, null);
    private final ButtonRegister buttonRegister = new ButtonRegister();
    private UIButton newGame, multiplayer, connectToLan;

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

    public TitleScreen() {
    }

    public void init() {
        new RenderElement(renderer, RenderOrder.TITLE_SCREEN_BACKGROUND, TITLE_SCREEN_IMAGE);
        new RenderElement(renderer, RenderOrder.TITLE_SCREEN_BUTTON_BACKGROUND, new UIBox(12, 18).setColourTheme(UIColourTheme.LIGHT_BLUE_TRANSPARENT_CENTER).translate(Renderable.right() - 16, 2));
        newGame = new UIButton(renderer, buttonRegister, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                Renderable.right() - 15, 16 - 4 * 0, 10, 3, 1.8f, true, () -> {
            multiplayer.deselect();
            connectToLan.deselect();
        }).setText("New Game").setBold().noDeselect().setColourTheme(UIColourTheme.GREEN_SELECTED);

        multiplayer = new UIButton(renderer, buttonRegister, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                Renderable.right() - 15, 16 - 4 * 1, 10, 3, 1.6f, true, () -> {
            newGame.deselect();
            connectToLan.deselect();
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

        enterIPBox = new UITextInputBox(renderer, buttonRegister, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                Renderable.right() - 36, 14, 16, 2, .7f, true, 39,
                InputType::isIPChar);
        enterIPBox.setBold().setColourTheme(UIColourTheme.GREEN_SELECTED).setEnabled(false);
        enterIPLabel = new RenderElement(renderer, RenderOrder.TITLE_SCREEN_BUTTONS,
                new UITextLabel(7.5f, 1, false).setTextCenterBold()
                        .updateTextCenter("Enter Server IP")
                        .translate(Renderable.right() - 31.8f, 16.5f)
        );
        enterIPLabel.setEnabled(false);
        connectButton = new UIButton(renderer, buttonRegister, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                Renderable.right() - 32.5f, 11.5f, 9f, 1.5f, 0.8f, false, () -> {
            Level.EXECUTOR.submit(() -> {
                if (connectButton.getText().equals("Connecting..."))
                    return;
                connectButton.setColourTheme(UIColourTheme.YELLOW);
                connectButton.setText("Connecting...");
                MainPanel.removeClient();
                updateColourSelectorVisibility();
                boolean success = MainPanel.startClient(enterIPBox.s.toString());
                connectButton.setColourTheme(success ? UIColourTheme.GREEN : UIColourTheme.DEEP_RED);
                connectButton.setText(success ? "Connected!" : "Connection Failed");
                updateColourSelectorVisibility();
            });
        }).setText("Connect").setBold();
        connectButton.setEnabled(false);
        createColourSelectorButtons();
        joinButton = new UIButton(renderer, buttonRegister, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                Renderable.right() - 32.5f, 5.2f, 9f, 1.5f, 0.8f, false, () -> {
            MainPanel.client.sendJoinRequest(selectedTeam);
        }).setText("Join").setBold().setEnabled(false);
        colourSelectorBox = new RenderElement(renderer, RenderOrder.TITLE_SCREEN_BUTTON_BACKGROUND,
                new UIBox(16, 6.8f).setColourTheme(UIColourTheme.LIGHT_BLUE_TRANSPARENT_CENTER)
                        .translate(Renderable.right() - 36, 4.2f)
        );
        colourSelectorBox.setEnabled(false);

        connectToLan = new UIButton(renderer, buttonRegister, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                Renderable.right() - 15, 16 - 4 * 2, 10, 3, 1.2f, true, () -> {
            newGame.deselect();
            multiplayer.deselect();
            enterIPBox.setEnabled(true);
            enterIPLabel.setEnabled(true);
            connectButton.setEnabled(true);
            updateColourSelectorVisibility();
        }).setOnDeselect(() -> {
            enterIPBox.setEnabled(false);
            enterIPLabel.setEnabled(false);
            connectButton.setEnabled(false);
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
                }).setEnabled(false);
    }

    public void reset() {
        multiplayer.deselect();
        connectToLan.deselect();
        newGame.deselect();
        enterIPBox.deselect();
        connectButton.setText("Connect").setColourTheme(UIColourTheme.LIGHT_BLUE);
        enterSeedBox.deselect();
        newGameTabs.selectTab(0);
    }

    private void createColourSelectorButtons() {
        colourSelectorButtons = new UIButton[UnitTeam.ORDERED_TEAMS.length];
        float width = 3.2f, spacing = (14 - width * 4) / 3;
        for (int i = 0; i < colourSelectorButtons.length; i++) {
            int finalI = i;
            colourSelectorButtons[i] = new UIButton(renderer, buttonRegister, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                    Renderable.right() - 35 + (width + spacing) * (i % 4), Renderable.top() / 2 - 8 - 1.7f * (i / 4), width, 1.3f, 0.6f, true, () -> {
                deselectOtherColourSelectors(colourSelectorButtons[finalI]);
                selectedTeam = UnitTeam.ORDERED_TEAMS[finalI];
                updateColourSelectorVisibility();
            });
            colourSelectorButtons[i].noDeselect().setEnabled(false).setBold();
        }
    }

    public UnitTeam selectedTeam = null;

    public void updateColourSelectorVisibility() {
        if (MainPanel.client != null && MainPanel.client.connected && connectButton.enabled) {
            for (UIButton colourSelectorButton : colourSelectorButtons) {
                colourSelectorButton.setEnabled(true);
            }
            for (int i = 0; i < colourSelectorButtons.length; i++) {
                if (i < MainPanel.client.playerCount) {
                    UnitTeam team = UnitTeam.ORDERED_TEAMS[i];
                    if (MainPanel.client.teamClientIDs.containsKey(team))
                        colourSelectorButtons[i].setColourTheme(team.unavailableColour)
                                .setText("Taken");
                    else
                        colourSelectorButtons[i].setColourTheme(team.uiColour)
                                .setText(team == selectedTeam ? "Selected" : "Available");
                } else {
                    colourSelectorButtons[i].setColourTheme(UIColourTheme.GRAYED_OUT)
                            .setText(null);
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
        long seed = enterSeedBox.s.isEmpty() ? new Random().nextLong() : Long.parseLong(enterSeedBox.s.toString());
        return new Level(playerBoxes.getTeams(), seed, widthSelector.getValue(), heightSelector.getValue(), NetworkState.LOCAL).generateDefaultTerrain();
    }

    private Level getNewServerMultiplayerLevel() {
        long seed = enterSeedBox.s.isEmpty() ? new Random().nextLong() : Long.parseLong(enterSeedBox.s.toString());
        return new Level(playerBoxes.getTeams(), seed, widthSelector.getValue(), heightSelector.getValue(), NetworkState.SERVER).generateDefaultTerrain();
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