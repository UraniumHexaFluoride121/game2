package render.save;

import foundation.input.ButtonOrder;
import foundation.input.ButtonRegister;
import foundation.input.InputType;
import foundation.input.OnButtonInput;
import render.OrderedRenderable;
import render.RenderOrder;
import render.RenderRegister;
import render.Renderable;
import render.level.tile.RenderElement;
import render.UIColourTheme;
import render.types.UIFullScreenColour;
import render.types.text.UITextLabel;
import render.types.input.button.UIButton;
import render.types.input.UITextInputBox;
import render.types.container.UIContainer;
import save.LoadedFromSave;
import save.SaveManager;

import java.awt.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class UISaveMenu<T extends LoadedFromSave> extends UIContainer {
    private UISaveBox<T> saveBox;
    public UITextInputBox saveFileNameBox;
    private UIButton saveButton;
    private final SaveManager<T> saveManager;
    private Runnable onSave = null;

    public UISaveMenu(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, ButtonOrder buttonOrder, SaveManager<T> saveManager, Function<String, T> saveSupplier) {
        super(register, buttonRegister, order, buttonOrder, 0, 0);
        this.saveManager = saveManager;
        addRenderables((r, b) -> {
            new UIFullScreenColour(r, RenderOrder.PAUSE_MENU_BACKGROUND, new Color(0, 0, 0, 200));
            new OnButtonInput(b, ButtonOrder.PAUSE_MENU, t -> t == InputType.ESCAPE, () -> setEnabled(false));
            saveBox = new UISaveBox<>(r, b, RenderOrder.PAUSE_MENU, ButtonOrder.PAUSE_MENU,
                    Renderable.right() / 2 - 6, Renderable.top() / 2 - 12, 12, 14, 1.5f, saveManager);
            new RenderElement(r, RenderOrder.PAUSE_MENU,
                    new UITextLabel(10f, 1.3f, false).setTextCenterBold().updateTextCenter("Saved files").translate(Renderable.right() / 2 - 5.25f, Renderable.top() / 2 + 2.5f),
                    new UITextLabel(10f, 1f, false).setTextCenterBold().updateTextCenter("Enter save name:").translate(Renderable.right() / 2 - 5, Renderable.top() / 2 + 9.5f)
            );
            saveFileNameBox = new UITextInputBox(r, b, RenderOrder.PAUSE_MENU, ButtonOrder.PAUSE_MENU,
                    Renderable.right() / 2 - 6, Renderable.top() / 2 + 7, 12, 2, 1, true, -1, InputType::isFileNameChar);
            saveFileNameBox.setOnChanged(this::updateSaveButton).setBold().setColourTheme(UIColourTheme.GREEN_SELECTED);
            saveButton = new UIButton(r, b, RenderOrder.PAUSE_MENU, ButtonOrder.PAUSE_MENU, Renderable.right() / 2 - 4, Renderable.top() / 2 + 5.5f, 8, 1.2f, 1, false)
                    .setText("Save").setBold().setColourTheme(UIColourTheme.GRAYED_OUT).setClickEnabled(false).setOnClick(() -> {
                        String name = saveFileNameBox.getText();
                        saveBox.addSave(saveSupplier.apply(name), name);
                        saveButton.setColourTheme(UIColourTheme.DEEP_GREEN).setText("Saved!").setClickEnabled(false);
                        if (onSave != null)
                            onSave.run();
                    });
            new UIButton(r, b, RenderOrder.PAUSE_MENU, ButtonOrder.PAUSE_MENU, Renderable.right() / 2 - 4, Renderable.top() / 2 - 15f, 8, 2, 1.4f, false, () -> setEnabled(false))
                    .setText("Continue").setBold();
        });
    }

    public UISaveMenu<T> setOnSave(Runnable onSave) {
        this.onSave = onSave;
        return this;
    }

    private void updateSaveButton() {
        if (saveFileNameBox.getText().isEmpty()) {
            saveButton.setColourTheme(UIColourTheme.GRAYED_OUT)
                    .setText("Save").setClickEnabled(false);
        } else if (saveManager.containsSave(saveFileNameBox.getText())) {
            saveButton.setColourTheme(UIColourTheme.DEEP_YELLOW)
                    .setText("Overwrite Save").setClickEnabled(true);
        } else {
            saveButton.setColourTheme(UIColourTheme.LIGHT_BLUE)
                    .setText("Save").setClickEnabled(true);
        }
    }

    @Override
    public UIContainer setEnabled(boolean enabled) {
        if (!enabled)
            updateSaveButton();
        return super.setEnabled(enabled);
    }

    @Override
    public boolean blocking(InputType type) {
        return isEnabled();
    }

    @Override
    public void delete() {
        super.delete();
        saveBox = null;
        onSave = null;
    }
}
