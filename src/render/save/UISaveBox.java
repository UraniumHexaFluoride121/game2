package render.save;

import foundation.input.ButtonOrder;
import foundation.input.ButtonRegister;
import render.OrderedRenderable;
import render.RenderOrder;
import render.RenderRegister;
import render.level.tile.RenderElement;
import render.UIColourTheme;
import render.types.box.UIBox;
import render.types.container.UIContainer;
import render.types.container.UIElementScrollSurface;
import save.LoadedFromSave;
import save.SaveManager;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class UISaveBox<T extends LoadedFromSave> extends UIContainer {
    private final float width, height, elementHeight;
    private UIElementScrollSurface<UISaveElement<T>> scrollSurface;
    private final SaveManager<T> saveManager;
    private String selectedName = null;
    private boolean clickEnabled = false;
    private Consumer<Boolean> onClickOrUpdate = null;

    public UISaveBox(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, ButtonOrder buttonOrder, float x, float y, float width, float height, float elementHeight, SaveManager<T> saveManager) {
        super(register, buttonRegister, order, buttonOrder, x, y);
        this.height = height;
        this.width = width;
        this.elementHeight = elementHeight;
        this.saveManager = saveManager;
        addRenderables((r, b) -> {
            new RenderElement(r, RenderOrder.LEVEL_UI, new UIBox(width, height).setColourTheme(UIColourTheme.LIGHT_BLUE_TRANSPARENT_CENTER).centerOnly()).setZOrder(-1);
            scrollSurface = new UIElementScrollSurface<>(r, b, RenderOrder.LEVEL_UI, ButtonOrder.LEVEL_UI,
                    0, 0, width, height, false, count -> count * (elementHeight + 0.5f) + 0.5f);
            scrollSurface.setScrollSpeed(0.2f);
            new RenderElement(r, RenderOrder.LEVEL_UI, new UIBox(width, height).setColourTheme(UIColourTheme.LIGHT_BLUE_TRANSPARENT_CENTER).borderOnly()).setZOrder(1);
        });
        updateSaves(false);
    }

    public UISaveBox<T> enableClick() {
        clickEnabled = true;
        scrollSurface.forEach(e -> e.enableButton(b -> b.setOnClick(() -> {
            scrollSurface.forEach(e2 -> {
                if (e != e2)
                    e2.button.deselect();
            });
            selectedName = e.saveName;
            if (onClickOrUpdate != null)
                onClickOrUpdate.accept(false);
        })));
        return this;
    }

    public ArrayList<UISaveElement<T>> getElements() {
        return scrollSurface.getElements();
    }

    public T getSelected() {
        return saveManager.gameSaves.get(selectedName);
    }

    public boolean hasSelectedSave() {
        return selectedName != null;
    }

    public UISaveBox<T> setOnClickOrUpdate(Consumer<Boolean> onClickOrUpdate) {
        this.onClickOrUpdate = onClickOrUpdate;
        return this;
    }

    public void updateSaves(boolean fromUpdate) {
        AtomicBoolean hasSelected = new AtomicBoolean(false);
        scrollSurface.clear();
        saveManager.forEachSave((name, save) -> {
            scrollSurface.addElement((r, b, i) -> new UISaveElement<>(r, b, RenderOrder.LEVEL_UI, ButtonOrder.LEVEL_UI,
                    .5f, -(elementHeight + 0.5f) * (i + 1), width - 1, elementHeight, name, saveManager)
                    .setOnRemoved(() -> updateSaves(false)));
            if (name.equals(selectedName)) {
                scrollSurface.getLast().button.select();
                hasSelected.set(true);
            }
        });
        if (!hasSelected.get())
            selectedName = null;
        if (clickEnabled)
            enableClick();
        if (onClickOrUpdate != null)
            onClickOrUpdate.accept(fromUpdate);
    }

    public void addSave(T save, String name) {
        saveManager.addSave(save, name);
        updateSaves(false);
    }

    @Override
    public void delete() {
        super.delete();
        scrollSurface = null;
        onClickOrUpdate = null;
    }
}
