package level.tutorial.sequence.action;

import foundation.MainPanel;
import foundation.math.ObjPos;
import level.Level;
import level.tile.Tile;
import level.tutorial.TutorialManager;
import level.tutorial.sequence.BoxSize;
import level.tutorial.sequence.event.EventConsumer;
import level.tutorial.sequence.event.TutorialEvent;
import level.tutorial.sequence.event.TutorialEventListener;
import render.RenderOrder;
import render.UIColourTheme;
import render.HorizontalAlign;
import render.types.box.UIDisplayBoxRenderElement;

import java.util.function.Supplier;

public class BlockingTextBox implements TutorialSequenceElement, EventConsumer {
    private UIDisplayBoxRenderElement textBox;
    private Supplier<UIDisplayBoxRenderElement> textBoxSupplier;
    private TutorialEventListener[] eventListener;
    private int index = 0;

    public static BlockingTextBox onMap(Level l, BoxSize size, float x, float y, HorizontalAlign textAlign, String text, TutorialEventListener... eventListener) {
        return new BlockingTextBox(l, x, y, size, textAlign, text, true, eventListener);
    }

    public static BlockingTextBox onUI(Level l, BoxSize size, float x, float y, HorizontalAlign textAlign, String text, TutorialEventListener... eventListener) {
        return new BlockingTextBox(l, x, y, size, textAlign, text, false, eventListener);
    }

    private BlockingTextBox(Level l, float x, float y, BoxSize boxSize, HorizontalAlign textAlign, String text, boolean map, TutorialEventListener... eventListener) {
        this.eventListener = eventListener;
        ObjPos pos = map ? Tile.getFractionalRenderPos(x, y) : new ObjPos(x, y);
        textBoxSupplier = () -> {
            UIDisplayBoxRenderElement element = new UIDisplayBoxRenderElement(map ? l.levelRenderer.mainRenderer : l.levelRenderer.levelUIRenderer, map ? RenderOrder.TUTORIAL_UI : RenderOrder.TUTORIAL_LEVEL_UI,
                    pos.x, pos.y, boxSize.width, -1, box ->
                    box.setColourTheme(UIColourTheme.LIGHT_BLUE_BOX_DARK), true)
                    .addText(0.7f, textAlign, text);
            element.box.setHorizontalAlign(HorizontalAlign.CENTER);
            return element;
        };
    }

    @Override
    public void start() {
        textBox = textBoxSupplier.get();
    }

    @Override
    public void end() {
        MainPanel.addTask(textBox::delete);
    }

    @Override
    public void delete() {
        textBoxSupplier = null;
        textBox = null;
        for (TutorialEventListener e : eventListener) {
            e.delete();
        }
        eventListener = null;
    }

    @Override
    public void accept(TutorialEvent e) {
        if (eventListener[index].test(e)) {
            index++;
            if (index >= eventListener.length)
                TutorialManager.incrementSequence();
        }
    }
}
