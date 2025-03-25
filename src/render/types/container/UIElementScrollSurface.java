package render.types.container;

import foundation.input.ButtonOrder;
import foundation.input.ButtonRegister;
import render.*;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Function;

public class UIElementScrollSurface<T extends AbstractRenderElement> extends UIScrollSurface {
    private final ArrayList<T> elements = new ArrayList<>();
    private final Function<Integer, Float> scrollMaxFunction;

    public UIElementScrollSurface(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, ButtonOrder buttonOrder, float x, float y, float width, float height, boolean inverted, Function<Integer, Float> scrollMaxFunction) {
        super(register, buttonRegister, order, buttonOrder, x, y, width, height, inverted, (r, b) -> {
        });
        this.scrollMaxFunction = scrollMaxFunction;
    }

    public void addElement(IndexedElementFunction<T> function) {
        elements.add(function.create(renderer, internal, elements.size()));
        setScrollMax(scrollMaxFunction.apply(elements.size()) - height);
    }

    public UIElementScrollSurface<T> addElements(int count, IndexedElementFunction<T> function) {
        for (int i = 0; i < count; i++) {
            addElement(function);
        }
        return this;
    }

    public void forEach(Consumer<T> action) {
        elements.forEach(action);
    }

    public void replaceAllElements(int count, IndexedElementFunction<T> function) {
        clear();
        for (int i = 0; i < count; i++) {
            addElement(function);
        }
    }

    public void clear() {
        elements.forEach(AbstractRenderElement::delete);
        elements.clear();
        setScrollMax(scrollMaxFunction.apply(0) - height);
    }

    public ArrayList<T> getElements() {
        return elements;
    }

    @FunctionalInterface
    public interface IndexedElementFunction<T extends AbstractRenderElement> {
        T create(GameRenderer r, ButtonRegister b, int i);
    }
}
