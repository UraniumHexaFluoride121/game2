package render.save;

import foundation.MainPanel;
import foundation.input.ButtonRegister;
import render.OrderedRenderable;
import render.RenderOrder;
import render.RenderRegister;
import render.UIColourTheme;
import render.types.input.button.UIButton;
import render.types.container.UIContainer;
import render.types.input.button.UIShapeButton;
import save.LoadedFromSave;
import save.SaveManager;

import java.util.function.Consumer;

public class UISaveElement<T extends LoadedFromSave> extends UIContainer {
    public final float width, height;
    public final String saveName;
    public final SaveManager<T> saveManager;
    private Runnable onRemoved = null;
    public UIButton button;

    public UISaveElement(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, float x, float y, float width, float height, String saveName, SaveManager<T> saveManager) {
        super(register, buttonRegister, order, x, y);
        this.width = width;
        this.height = height;
        this.saveName = saveName;
        this.saveManager = saveManager;
        addRenderables((r, b) -> {
            button = new UIButton(r, b, RenderOrder.PAUSE_MENU, 0, 0, width - height - 0.5f, height, 0.8f, true)
                    .setText(saveName).setColourTheme(UIColourTheme.GREEN_SELECTED).noDeselect().setBold().setBoxCorner(0.3f).setClickEnabled(false);
            new UIShapeButton(r, b, RenderOrder.PAUSE_MENU, width - height, 0, height, height, false)
                    .setColourTheme(UIColourTheme.DEEP_RED).setShape(UIShapeButton::smallX).setBoxCorner(0.3f).setOnClick(() -> {
                        saveManager.removeSave(saveName);
                        if (onRemoved != null)
                            MainPanel.addTask(onRemoved);
                    });
        });
    }

    public UISaveElement<T> enableButton(Consumer<UIButton> editAction) {
        button.setClickEnabled(true);
        editAction.accept(button);
        return this;
    }

    public UISaveElement<T> setOnRemoved(Runnable onRemoved) {
        this.onRemoved = onRemoved;
        return this;
    }

    @Override
    public void delete() {
        super.delete();
        onRemoved = null;
        button = null;
    }
}