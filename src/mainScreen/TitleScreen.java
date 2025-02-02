package mainScreen;

import foundation.MainPanel;
import foundation.input.ButtonOrder;
import foundation.input.ButtonRegister;
import foundation.input.InputReceiver;
import foundation.input.InputType;
import level.Level;
import render.GameRenderer;
import render.RenderOrder;
import render.Renderable;
import render.renderables.RenderElement;
import render.texture.ResourceLocation;
import render.ui.UIColourTheme;
import render.ui.implementation.UIPlayerBoxes;
import render.ui.types.*;

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

    public TitleScreen() {
    }

    public void init() {
        new RenderElement(renderer, RenderOrder.TITLE_SCREEN_BACKGROUND, TITLE_SCREEN_IMAGE);
        new RenderElement(renderer, RenderOrder.TITLE_SCREEN_BUTTON_BACKGROUND, new UIBox(12, 18).setColourTheme(UIColourTheme.LIGHT_BLUE_TRANSPARENT_CENTER).translate(Renderable.right() - 16, 2));
        newGame = new UIButton(renderer, buttonRegister, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                Renderable.right() - 15, Renderable.top() / 2 - 1 - 4 * 0, 10, 3, 1.8f, true, () -> {
            multiplayer.deselect();
            connectToLan.deselect();
        }).setText("New Game").setBold().noDeselect().setColourTheme(UIColourTheme.GREEN_SELECTED);
        multiplayer = new UIButton(renderer, buttonRegister, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                Renderable.right() - 15, Renderable.top() / 2 - 1 - 4 * 1, 10, 3, 1.6f, true, () -> {
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
        connectToLan = new UIButton(renderer, buttonRegister, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                Renderable.right() - 15, Renderable.top() / 2 - 1 - 4 * 2, 10, 3, 1.2f, true, () -> {
            newGame.deselect();
            multiplayer.deselect();
        }).setText("Connect to LAN").setBold().noDeselect().setColourTheme(UIColourTheme.GREEN_SELECTED);

        startLocalGame = new UIButton(renderer, buttonRegister, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                Renderable.right() - 37 + 1, 3.5f - 2, 7, 1.5f, 0.7f, false, () -> {
            MainPanel.startNewLevel(this::getNewMultiplayerLevel);
        }).setText("Start game locally").setBold().setEnabled(false);
        startLanGame = new UIButton(renderer, buttonRegister, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                Renderable.right() - 37 + 9, 3.5f - 2, 8, 1.5f, 0.7f, false, () -> {
            MainPanel.startNewLevel(this::getNewMultiplayerLevel);
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
                            1, 7.5f, 1.5f, 2.5f, 10, 35, 15);
                    RenderElement widthLabel = new RenderElement(r, RenderOrder.TITLE_SCREEN_BUTTONS,
                            new UITextLabel(6.5f, 1, false).setTextLeftBold()
                                    .updateTextLeft("Level width").setLeftOffset(0.5f)
                                    .translate(1, 9.5f)
                    );
                    heightSelector = new UINumberSelector(r, b, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                            9, 7.5f, 1.5f, 2.5f, 7, 20, 10);
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

    private Level getNewMultiplayerLevel() {
        long seed = enterSeedBox.s.isEmpty() ? new Random().nextLong() : Long.parseLong(enterSeedBox.s.toString());
        return new Level(playerBoxes.getTeams(), seed, widthSelector.getValue(), heightSelector.getValue());
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