package render.types.box;

import foundation.Deletable;
import foundation.input.*;
import foundation.math.HitBox;
import foundation.math.ObjPos;
import render.AbstractRenderElement;
import render.OrderedRenderable;
import render.RenderOrder;
import render.RenderRegister;
import render.types.text.TooltipManager;

import java.awt.*;
import java.util.ArrayList;

public class UIDisplayBoxButtonHandler extends AbstractRenderElement implements RegisteredButtonInputReceiver, Deletable {
    private ButtonRegister buttonRegister;
    private final ButtonOrder buttonOrder;
    private UIDisplayBox displayBox;
    private final ArrayList<DisplayBoxElement> elements = new ArrayList<>();

    public UIDisplayBoxButtonHandler(RenderRegister<OrderedRenderable> renderer, ButtonRegister buttonRegister, RenderOrder order, ButtonOrder buttonOrder, UIDisplayBox displayBox) {
        super(renderer, order);
        this.buttonRegister = buttonRegister;
        this.buttonOrder = buttonOrder;
        if (buttonRegister != null) {
            buttonRegister.register(this);
        }
        this.displayBox = displayBox;
        displayBox.addOnUpdate(this::update);
    }

    public void update() {
        for (DisplayBoxElement e : elements) {
            e.update(displayBox);
        }
    }

    public ButtonClickHandler add(boolean staySelected, int index, int columnIndex) {
        DisplayBoxElement e = new DisplayBoxElement(displayBox, staySelected, index, columnIndex, InputType.MOUSE_LEFT, true);
        elements.add(e);
        return e.getClickHandler();
    }

    public ButtonClickHandler add(boolean staySelected) {
        DisplayBoxElement e = new DisplayBoxElement(displayBox, staySelected, -1, 0, InputType.MOUSE_LEFT, true);
        elements.add(e);
        return e.getClickHandler();
    }

    public TooltipManager addTooltip(int index, int columnIndex, boolean button) {
        DisplayBoxElement e = new DisplayBoxElement(displayBox, false, index, columnIndex, InputType.MOUSE_LEFT, button);
        if (button)
            e.getClickHandler().setOnClick(e.getManager()::forceShowTooltip);
        elements.add(e);
        return e.getManager();
    }

    public TooltipManager addTooltip(boolean button) {
        DisplayBoxElement e = new DisplayBoxElement(displayBox, false, -1, 0, InputType.MOUSE_LEFT, button);
        if (button)
            e.getClickHandler().setOnClick(e.getManager()::forceShowTooltip);
        elements.add(e);
        return e.getManager();
    }

    public ButtonClickHandler getClickHandler(int index, int columnIndex) {
        return getElement(index, columnIndex).getClickHandler();
    }

    public ButtonClickHandler getClickHandler() {
        return getElement().getClickHandler();
    }

    public TooltipManager getTooltip(int index, int columnIndex) {
        return getElement(index, columnIndex).getManager();
    }

    public TooltipManager getTooltip() {
        return getElement().getManager();
    }

    private DisplayBoxElement getElement(int index, int columnIndex) {
        for (DisplayBoxElement e : elements) {
            if (e.index == index && e.columnIndex == columnIndex)
                return e;
        }
        throw new RuntimeException();
    }

    private DisplayBoxElement getElement() {
        for (DisplayBoxElement e : elements) {
            if (e.index == -1)
                return e;
        }
        throw new RuntimeException();
    }

    @Override
    public void render(Graphics2D g) {
        if (isEnabled())
            for (DisplayBoxElement e : elements) {
                e.getManager().render(g);
            }
    }

    @Override
    public boolean posInside(ObjPos pos, InputType type) {
        if (!isEnabled())
            return false;
        for (DisplayBoxElement element : elements) {
            if (element.box != null && element.box.isPositionInside(pos))
                return true;
        }
        return false;
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
        if (isEnabled()) {
            for (DisplayBoxElement e : elements) {
                if (e.box == null)
                    continue;
                boolean insideBox = inside && e.box.isPositionInside(pos);
                e.getClickHandler().buttonPressed(pos, insideBox, blocked, type);
                if (insideBox && (type == InputType.MOUSE_OVER || type == e.clickHandler.clickInput))
                    blocked = true;
            }
            blocking = blocked;
        }
    }

    @Override
    public void buttonReleased(ObjPos pos, boolean inside, boolean blocked, InputType type) {
        if (isEnabled()) {
            for (DisplayBoxElement e : elements) {
                if (e.box == null)
                    continue;
                boolean insideBox = inside && e.box.isPositionInside(pos);
                e.getClickHandler().buttonReleased(pos, insideBox, blocked, type);
                if (insideBox && type == e.clickHandler.clickInput)
                    blocked = true;
            }
            blocking = blocked;
        }
    }

    @Override
    public void delete() {
        super.delete();
        if (buttonRegister != null) {
            buttonRegister.remove(this);
            buttonRegister = null;
        }
        displayBox = null;
        elements.forEach(DisplayBoxElement::delete);
    }

    private static class DisplayBoxElement implements TooltipHolder, Deletable {
        public HitBox box;
        public ButtonClickHandler clickHandler;
        public TooltipManager tooltipManager = new TooltipManager(this);
        public int index, columnIndex;

        private DisplayBoxElement(UIDisplayBox displayBox, boolean staySelected, int index, int columnIndex, InputType inputType, boolean bindClickHandler) {
            this.index = index;
            this.columnIndex = columnIndex;
            clickHandler = new ButtonClickHandler(inputType, staySelected);
            if (bindClickHandler) {
                if (index == -1) {
                    displayBox.setClickHandler(clickHandler);
                } else {
                    displayBox.setClickHandler(clickHandler, index, columnIndex);
                }
            }
            update(displayBox);
        }

        public void update(UIDisplayBox displayBox) {
            if (index == -1) {
                box = displayBox.getHitBox();
            } else {
                box = displayBox.getElementRenderBox(index, columnIndex);
            }
        }

        @Override
        public void delete() {
            clickHandler.delete();
            clickHandler = null;
        }

        @Override
        public ButtonClickHandler getClickHandler() {
            return clickHandler;
        }

        @Override
        public TooltipManager getManager() {
            return tooltipManager;
        }
    }
}
