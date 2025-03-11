package render.ui.types;

import foundation.input.*;
import foundation.math.HitBox;
import foundation.math.ObjPos;
import foundation.math.StaticHitBox;
import render.*;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.function.BiConsumer;

import static render.Renderable.*;

public class UIScrollSurface extends AbstractRenderElement implements RegisteredButtonInputReceiver {
    public final float x, y, width, height;
    private final Rectangle2D clip;
    private ButtonOrder buttonOrder;
    private float scrollAmount = 0, scrollMax = 0, scrollSpeed = 0.75f;
    private ButtonRegister buttonRegister, internal = new ButtonRegister();
    private GameRenderer renderer = new GameRenderer(new AffineTransform(), null);
    private final boolean inverted;

    public UIScrollSurface(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, ButtonOrder buttonOrder, float x, float y, float width, float height, BiConsumer<GameRenderer, ButtonRegister> elementsRenderer) {
        this(register, buttonRegister, order, buttonOrder, x, y, width, height, true, elementsRenderer);
    }

    public UIScrollSurface(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, ButtonOrder buttonOrder, float x, float y, float width, float height, boolean inverted, BiConsumer<GameRenderer, ButtonRegister> elementsRenderer) {
        super(register, order);
        this.buttonRegister = buttonRegister;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.buttonOrder = buttonOrder;
        this.inverted = inverted;
        if (buttonRegister != null)
            buttonRegister.register(this);
        clip = new Rectangle2D.Float(0, 0, width, height);
        if (inverted) {
            renderable = g -> {
                Shape prevClip = g.getClip();
                GameRenderer.renderOffset(x, y, g, () -> {
                    g.clip(clip);
                    g.scale(1, -1);
                    g.translate(0, -scrollAmount - height);
                    renderer.render(g);
                });
                g.setClip(prevClip);
            };
        } else {
            renderable = g -> {
                Shape prevClip = g.getClip();
                GameRenderer.renderOffset(x, y, g, () -> {
                    g.clip(clip);
                    g.translate(0, scrollAmount + height);
                    renderer.render(g);
                });
                g.setClip(prevClip);
            };
        }
        elementsRenderer.accept(renderer, internal);
    }

    public UIScrollSurface addRenderables(BiConsumer<GameRenderer, ButtonRegister> createRenderer) {
        createRenderer.accept(renderer, internal);
        return this;
    }

    public UIScrollSurface setScrollMax(float scrollMax) {
        this.scrollMax = Math.max(0, scrollMax);
        scrollAmount = Math.clamp(scrollAmount, 0, this.scrollMax);
        return this;
    }

    public UIScrollSurface setScrollAmount(float amount) {
        scrollAmount = Math.clamp(amount, 0, scrollMax);
        return this;
    }

    public UIScrollSurface setScrollSpeed(float scrollSpeed) {
        this.scrollSpeed = scrollSpeed;
        return this;
    }

    private static final Color SCROLL_BAR_COLOUR = new Color(149, 149, 149, 152);
    private static final Color SCROLL_BAR_COLOUR_SELECTED = new Color(108, 108, 108, 152);

    private ObjPos prevPos = null;
    private HitBox scrollBarBox = null;
    private float barSize, totalSize;

    public UIScrollSurface addScrollBar(float margin, float width, float offset) {
        totalSize = height - margin * 2;
        barSize = height / (scrollMax + height) * totalSize;
        BasicStroke stroke = Renderable.roundedStroke(width * SCALING);
        scrollBarBox = StaticHitBox.createFromOriginAndSize(x + this.width + offset - width / 2, y + margin, width, height - margin * 2);
        renderable = renderable.andThen(g -> {
            if (scrollMax == 0)
                return;
            barSize = height / (scrollMax + height) * totalSize;
            float barOffset = scrollAmount / (scrollMax + height) * totalSize;
            GameRenderer.renderOffset(x + this.width + offset, y + height - margin, g, () -> {
                g.setStroke(stroke);
                g.setColor(prevPos == null ? SCROLL_BAR_COLOUR : SCROLL_BAR_COLOUR_SELECTED);
                GameRenderer.renderScaled(1f / SCALING, g, () -> {
                    g.drawLine(0, -(int) ((barOffset) * SCALING), 0, -(int) ((barOffset + barSize) * SCALING));
                });
            });
        });
        return this;
    }

    @Override
    public boolean posInside(ObjPos pos) {
        return clip.contains(pos.x - x, pos.y - y);
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
        if (!isEnabled())
            return;
        if (inside)
            blocking = internal.acceptInput(getPos(pos), type, true, blocked);
        else
            blocking = internal.acceptInput(new ObjPos(-1000, -1000), type, true, blocked);
        if (!blocking && inside && !blocked && type instanceof ScrollInputType s) {
            if (s.up)
                scrollAmount = Math.clamp(scrollAmount - scrollSpeed * s.scrollAmount, 0, scrollMax);
            else
                scrollAmount = Math.clamp(scrollAmount + scrollSpeed * s.scrollAmount, 0, scrollMax);
        }
        if (scrollBarBox != null && !blocked && !blocking && type == InputType.MOUSE_LEFT && scrollBarBox.isPositionInside(pos)) {
            prevPos = pos;
        }
        if (prevPos != null && type == InputType.MOUSE_OVER) {
            if (scrollMax == 0)
                return;
            setScrollAmount(scrollAmount - (pos.y - prevPos.y) / (totalSize - barSize) * scrollMax);
            prevPos = pos;
        }
    }

    private ObjPos getPos(ObjPos pos) {
        if (inverted)
            return pos.copy().subtract(x, y).multiply(1, -1).add(0, scrollAmount + height);
        else
            return pos.copy().subtract(x, y + height + scrollAmount);
    }

    @Override
    public void buttonReleased(ObjPos pos, boolean inside, boolean blocked, InputType type) {
        if (!isEnabled())
            return;
        if (inside)
            blocking = internal.acceptInput(getPos(pos), type, false, blocked);
        else
            blocking = internal.acceptInput(new ObjPos(-1000, -1000), type, false, blocked);
        if (type == InputType.MOUSE_LEFT)
            prevPos = null;
    }

    @Override
    public void delete() {
        super.delete();
        if (buttonRegister != null) {
            buttonRegister.remove(this);
            buttonRegister = null;
        }
        internal.delete();
        renderer.delete();
    }
}
