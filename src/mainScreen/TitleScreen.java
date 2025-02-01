package mainScreen;

import foundation.MainPanel;
import foundation.input.ButtonOrder;
import foundation.input.ButtonRegister;
import foundation.input.InputReceiver;
import foundation.input.InputType;
import render.GameRenderer;
import render.RenderOrder;
import render.Renderable;
import render.renderables.RenderElement;
import render.texture.ResourceLocation;
import render.ui.UIColourTheme;
import render.ui.implementation.UIPlayerBoxes;
import render.ui.types.*;

import java.awt.*;

public class TitleScreen implements Renderable, InputReceiver {
    private static final Renderable TITLE_SCREEN_IMAGE = Renderable.renderImage(new ResourceLocation("title_screen.png"), false, true, 60, true);
    private final GameRenderer renderer = new GameRenderer(MainPanel.windowTransform, null);
    private final ButtonRegister buttonRegister = new ButtonRegister();
    private final UIButton newGame, multiplayer, connectToLan;
    private final UITabSwitcher newGameTabs;

    public TitleScreen() {
        new RenderElement(renderer, RenderOrder.TITLE_SCREEN_BACKGROUND, TITLE_SCREEN_IMAGE);
        new RenderElement(renderer, RenderOrder.TITLE_SCREEN_BUTTON_BACKGROUND, new UIBox(12, 18).setColourTheme(UIColourTheme.LIGHT_BLUE_TRANSPARENT_CENTER).translate(Renderable.right() - 16, 2));
        newGame = new UIButton(renderer, buttonRegister, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                Renderable.right() - 15, Renderable.top() / 2 - 1 - 4 * 0, 10, 3, 1.8f, true, () -> {
            getMultiplayer().deselect();
            getConnectToLan().deselect();
        }).setText("New Game").setBold().noDeselect().setColourTheme(UIColourTheme.GREEN_SELECTED);
        multiplayer = new UIButton(renderer, buttonRegister, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                Renderable.right() - 15, Renderable.top() / 2 - 1 - 4 * 1, 10, 3, 1.6f, true, () -> {
            getNewGame().deselect();
            getConnectToLan().deselect();
            getNewGameTabs().setEnabled(true);
        }).setOnDeselect(() -> {
            getNewGameTabs().setEnabled(false);
        }).setText("Multiplayer").setBold().noDeselect().setColourTheme(UIColourTheme.GREEN_SELECTED);
        connectToLan = new UIButton(renderer, buttonRegister, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                Renderable.right() - 15, Renderable.top() / 2 - 1 - 4 * 2, 10, 3, 1.2f, true, () -> {
            getNewGame().deselect();
            getMultiplayer().deselect();
        }).setText("Connect to LAN").setBold().noDeselect().setColourTheme(UIColourTheme.GREEN_SELECTED);

        newGameTabs = new UITabSwitcher(renderer, buttonRegister, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                Renderable.right() - 37, 3.5f, 18, 15)
                .addTab(4.65f, "Map Layout", (r, b) -> {
                    UIButton seed = new UIButton(r, b, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                            1, 12, 7, 1.5f, 1f, true)
                            .setColourTheme(UIColourTheme.GREEN_SELECTED).noDeselect().select().setText("From seed").setBold();

                    UINumberSelector widthSelector = new UINumberSelector(r, b, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                            1, 7.5f, 1.5f, 2.5f, 10, 35, 15);
                    RenderElement widthLabel = new RenderElement(r, RenderOrder.TITLE_SCREEN_BUTTONS,
                            new UITextLabel(6.5f, 1, false).setTextLeftBold()
                                    .updateTextLeft("Level width").setLeftOffset(0.5f)
                                    .translate(1, 9.5f)
                    );
                    UINumberSelector heightSelector = new UINumberSelector(r, b, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
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
                    UITextInputBox enterSeedBox = new UITextInputBox(r, b, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS,
                            2, 1, 13, 3, 1.5f, true, 15, InputType::isDigit);
                    enterSeedBox.setColourTheme(UIColourTheme.GREEN_SELECTED).setBold();
                }).addTab(3.5f, "Players", (r, b) -> {
                    new UIScrollSurface(r, b, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS, 0.5f, 0.5f, 17, 14, (r2, b2) -> {
                        new UIPlayerBoxes(r2, b2, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS, 0, 0);
                    }).setScrollMax(6);
                }).setEnabled(false);
    }

    public UIButton getNewGame() {
        return newGame;
    }

    public UIButton getConnectToLan() {
        return connectToLan;
    }

    public UIButton getMultiplayer() {
        return multiplayer;
    }

    public UITabSwitcher getNewGameTabs() {
        return newGameTabs;
    }

    @Override
    public void render(Graphics2D g) {
        renderer.render(g);
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