package render.level;

import foundation.MainPanel;
import foundation.input.ButtonOrder;
import foundation.input.ButtonRegister;
import foundation.input.InputType;
import foundation.input.OnButtonInput;
import level.Level;
import level.tutorial.TutorialManager;
import network.NetworkState;
import render.*;
import render.level.tile.RenderElement;
import render.save.UISaveBox;
import render.UIColourTheme;
import render.types.text.UITextLabel;
import render.types.input.button.UIButton;
import render.types.input.UITextInputBox;
import render.types.container.LevelUIContainer;
import render.types.container.UIContainer;
import save.GameSave;

import static render.level.info.UnitInfoScreen.*;

public class PauseMenu extends LevelUIContainer<Level> {
    private static final float WIDTH = 18, HEIGHT = 2.5f, Y_OFFSET = -5;
    private UIContainer saveContainer;
    public UITextInputBox saveFileNameBox;
    private UIButton saveButton, saveGame;
    private boolean botPlaying = false;
    private UISaveBox<GameSave> saveBox;

    public PauseMenu(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, ButtonOrder buttonOrder, Level level) {
        super(register, buttonRegister, order, buttonOrder, 0, 0, level);
        addRenderables((r, b) -> {
            new OnButtonInput(b, ButtonOrder.PAUSE_MENU, t -> t == InputType.ESCAPE, () -> setEnabled(false));
            new RenderElement(r, RenderOrder.PAUSE_MENU_BACKGROUND, g -> {
                g.setColor(FULL_SCREEN_MENU_BACKGROUND_COLOUR);
                g.fillRect(0, 0, (int) Math.ceil(Renderable.right()), (int) Math.ceil(Renderable.top()));
                if (botPlaying != level.bots.get(level.getActiveTeam())) {
                    botPlaying = !botPlaying;
                    updateSaveGameButton();
                }
            });
            newButton(r, b, 0, WIDTH / 2 - 0.25f, 0, false)
                    .setColourTheme(UIColourTheme.DEEP_RED).setText("Exit").setOnClick(() -> {
                        MainPanel.addTask(MainPanel::toTitleScreen);
                    });
            newButton(r, b, 0, WIDTH / 2 - 0.25f, WIDTH / 2 + 0.25f, false)
                    .setColourTheme(UIColourTheme.DEEP_GREEN).setText("Continue").setOnClick(() -> {
                        setEnabled(false);
                    });
            saveGame = newButton(r, b, 1, true)
                    .setOnClick(() -> {
                        saveContainer.setEnabled(true);
                    }).noDeselect().setOnDeselect(() -> {
                        saveContainer.setEnabled(false);
                    });
            saveContainer = new UIContainer(r, b, RenderOrder.PAUSE_MENU, ButtonOrder.PAUSE_MENU, Renderable.right() / 2 + WIDTH / 2, Renderable.top() / 2 - 12);
            saveContainer.addRenderables((r2, b2) -> {
                saveBox = new UISaveBox<>(r2, b2, RenderOrder.PAUSE_MENU, ButtonOrder.PAUSE_MENU,
                        2, 0, 15, 14, 1.5f, MainPanel.levelSaves);
                new RenderElement(r2, RenderOrder.PAUSE_MENU_BACKGROUND,
                        new UITextLabel(10.5f, 1.3f, false).setTextCenterBold().updateTextCenter("Saved files").translate(4f, 14.6f),
                        new UITextLabel(10.5f, 1f, false).setTextCenterBold().updateTextCenter("Enter save name:").translate(4f, 21.6f)
                );
                saveFileNameBox = new UITextInputBox(r2, b2, RenderOrder.PAUSE_MENU, ButtonOrder.PAUSE_MENU,
                        2.5f, 19, 14, 2, 1, true, -1, InputType::isFileNameChar);
                saveFileNameBox.setOnChanged(this::updateSaveButton).setBold().setColourTheme(UIColourTheme.GREEN_SELECTED);
                saveButton = new UIButton(r2, b2, RenderOrder.PAUSE_MENU, ButtonOrder.PAUSE_MENU, (15 - 8) / 2f + 2, 17.3f, 8, 1.2f, 1, false)
                        .setText("Save").setBold().setColourTheme(UIColourTheme.GRAYED_OUT).setClickEnabled(false).setOnClick(() -> {
                            String name = saveFileNameBox.getText();
                            MainPanel.levelSaves.addSave(new GameSave(level, name), name);
                            saveButton.setColourTheme(UIColourTheme.DEEP_GREEN).setText("Saved!").setClickEnabled(false);
                            updateSaves();
                        });
            });
            updateSaveGameButton();
        });
    }

    private UIButton newButton(GameRenderer r, ButtonRegister b, int i, float width, float xOffset, boolean staySelected) {
        return new UIButton(r, b, RenderOrder.PAUSE_MENU, ButtonOrder.PAUSE_MENU, Renderable.right() / 2 - WIDTH / 2 + xOffset, Renderable.top() / 2 + i * HEIGHT + Y_OFFSET, width, HEIGHT - 0.5f, 1f, staySelected)
                .setBold().setBoxCorner(0.65f);
    }

    private UIButton newButton(GameRenderer r, ButtonRegister b, int i, boolean staySelected) {
        return newButton(r, b, i, WIDTH, 0, staySelected);
    }

    public void updateSaveGameButton() {
        saveGame.setClickEnabled(false);
        saveGame.setColourTheme(UIColourTheme.GRAYED_OUT);
        if (TutorialManager.isTutorial()) {
            saveGame.setText("Cannot save during tutorial");
            return;
        }
        if (level.networkState == NetworkState.CLIENT) {
            saveGame.setText("Only the host can save");
            return;
        }
        if (botPlaying) {
            saveGame.setText("Cannot save while bot is playing");
            saveContainer.setEnabled(false);
            return;
        }
        saveGame.setText("Save");
        saveGame.setClickEnabled(true);
        saveGame.setColourTheme(UIColourTheme.LIGHT_BLUE);
    }

    private void updateSaveButton() {
        if (saveFileNameBox.getText().isEmpty()) {
            saveButton.setColourTheme(UIColourTheme.GRAYED_OUT)
                    .setText("Save").setClickEnabled(false);
        } else if (MainPanel.levelSaves.containsSave(saveFileNameBox.getText())) {
            saveButton.setColourTheme(UIColourTheme.DEEP_YELLOW)
                    .setText("Overwrite Save").setClickEnabled(true);
        } else {
            saveButton.setColourTheme(UIColourTheme.LIGHT_BLUE)
                    .setText("Save").setClickEnabled(true);
        }
    }

    public void updateSaves() {
        saveBox.updateSaves(false);
    }

    @Override
    public UIContainer setEnabled(boolean enabled) {
        updateSaves();
        if (!enabled) {
            updateSaveButton();
            saveContainer.setEnabled(false);
            saveGame.deselect();
            super.setEnabled(false);
        } else {
            MainPanel.addTaskAfterAnimBlock(this::enableAfterAnimBlock);
        }
        return this;
    }

    private void enableAfterAnimBlock() {
        super.setEnabled(true);
    }

    @Override
    public boolean blocking(InputType type) {
        return true;
    }

    @Override
    public void delete() {
        super.delete();
        saveFileNameBox = null;
        saveContainer = null;
        saveButton = null;
        saveGame = null;
        saveBox = null;
    }
}
