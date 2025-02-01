package render.ui.types;

import foundation.Deletable;
import foundation.input.*;
import foundation.math.ObjPos;
import foundation.math.StaticHitBox;
import render.*;
import render.renderables.text.FixedTextRenderer;
import render.renderables.text.TextAlign;
import render.ui.UIColourTheme;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.function.BiConsumer;

public class UITabSwitcher extends AbstractRenderElement implements RegisteredButtonInputReceiver {
    public final float x, y, height, width;
    private final UIBox box;
    private ButtonRegister buttonRegister;
    private final ButtonOrder buttonOrder;

    public int selectedTab = 0;

    private final ArrayList<Tab> tabs = new ArrayList<>();

    public UITabSwitcher(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, ButtonOrder buttonOrder, float x, float y, float width, float height) {
        super(register, order);
        this.x = x;
        this.y = y;
        this.height = height;
        this.width = width;
        this.buttonRegister = buttonRegister;
        this.buttonOrder = buttonOrder;
        if (buttonRegister != null) {
            buttonRegister.register(this);
        }
        box = new UIBox(width, height).setColourTheme(UIColourTheme.LIGHT_BLUE_TRANSPARENT_CENTER);
        renderable = g -> {
            if (!enabled)
                return;
            GameRenderer.renderOffset(x, y, g, () -> {
                tabs.forEach(t -> {
                    if (t.index != selectedTab)
                        t.render(g);
                });
                if (!tabs.isEmpty())
                    tabs.get(selectedTab).render(g);
                box.render(g);
                if (!tabs.isEmpty())
                    tabs.get(selectedTab).renderer.render(g);
            });
        };
    }

    public UITabSwitcher addTab(float width, String text, BiConsumer<GameRenderer, ButtonRegister> tabElementsRenderer) {
        float x = tabs.isEmpty() ? 0.7f : tabs.getLast().x + tabs.getLast().width;
        Tab t = new Tab(x, height, width, 1, 0.7f, tabs.size(), text, this);
        tabs.add(t);
        if (tabs.size() == 1)
            t.select();
        tabElementsRenderer.accept(t.renderer, t.buttonRegister);
        return this;
    }

    public UITabSwitcher setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        return this;
    }

    @Override
    public boolean posInside(ObjPos pos) {
        return enabled;
    }

    private boolean blocking = false;

    @Override
    public boolean blocking(InputType type) {
        return blocking;
    }

    @Override
    public ButtonOrder getButtonOrder() {
        return buttonOrder;
    }

    @Override
    public void buttonPressed(ObjPos pos, boolean inside, boolean blocked, InputType type) {
        if (!enabled || tabs.isEmpty())
            return;
        blocking = tabs.get(selectedTab).buttonRegister.acceptInput(pos.copy().subtract(x, y), type, true);
    }

    @Override
    public void buttonReleased(ObjPos pos, boolean inside, boolean blocked, InputType type) {
        if (!enabled || tabs.isEmpty())
            return;
        blocking = tabs.get(selectedTab).buttonRegister.acceptInput(pos.copy().subtract(x, y), type, false);
    }

    @Override
    public void delete() {
        super.delete();
        tabs.forEach(Tab::delete);
        if (buttonRegister != null) {
            buttonRegister.remove(this);
            buttonRegister = null;
        }
        tabs.clear();
    }

    private static class Tab implements Renderable, RegisteredButtonInputReceiver, Deletable {
        public ButtonRegister buttonRegister;
        public GameRenderer renderer;
        public final StaticHitBox hitBox;
        public final UIBox box;
        public final ButtonClickHandler clickHandler;
        public final float x, y, width, height, textSize;
        public final FixedTextRenderer text;
        public UITabSwitcher parent;
        public final int index;

        private Tab(float x, float y, float width, float height, float textSize, int index, String s, UITabSwitcher parent) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.textSize = textSize;
            this.parent = parent;
            this.index = index;
            renderer = new GameRenderer(new AffineTransform(), null);
            buttonRegister = new ButtonRegister();
            if (parent.buttonRegister != null)
                parent.buttonRegister.register(this);
            clickHandler = new ButtonClickHandler(InputType.MOUSE_LEFT, true, () -> {
                parent.selectedTab = index;
                parent.tabs.forEach(t -> {
                    if (t.index != parent.selectedTab)
                        t.deselect();
                });
            }).noDeselect();
            hitBox = StaticHitBox.createFromOriginAndSize(x + parent.x, y + parent.y, width, height);
            box = new UIBox(width, height, 0.4f, UIBox.BoxShape.RECTANGLE_TOP_CORNERS_CUT).setClickHandler(clickHandler).setColourTheme(UIColourTheme.GREEN_SELECTED_TAB);
            text = new FixedTextRenderer(s, textSize, UITextLabel.TEXT_COLOUR).setTextAlign(TextAlign.CENTER).setBold(true);
        }

        public void select() {
            clickHandler.select();
        }

        public void deselect() {
            clickHandler.deselect();
        }

        @Override
        public void render(Graphics2D g) {
            GameRenderer.renderOffset(x, y, g, () -> {
                box.render(g);
                g.translate(width / 2, height / 2 - textSize * 0.75 / 2);
                text.render(g);
            });
        }

        @Override
        public boolean posInside(ObjPos pos) {
            return hitBox.isPositionInside(pos);
        }

        @Override
        public boolean blocking(InputType type) {
            return type.isMouseInput();
        }

        @Override
        public ButtonOrder getButtonOrder() {
            return parent.buttonOrder;
        }

        @Override
        public void buttonPressed(ObjPos pos, boolean inside, boolean blocked, InputType type) {
            clickHandler.buttonPressed(pos, inside, blocked, type);
        }

        @Override
        public void buttonReleased(ObjPos pos, boolean inside, boolean blocked, InputType type) {
            clickHandler.buttonReleased(pos, inside, blocked, type);
        }

        @Override
        public void delete() {
            clickHandler.delete();
            box.setClickHandler(null);
            if (parent.buttonRegister != null)
                parent.buttonRegister.remove(this);
            renderer.delete();
            buttonRegister.delete();
        }
    }
}
