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
import render.types.text.TextAlign;
import render.types.text.UIMLTextBoxRenderElement;

import java.util.function.Supplier;

public class BlockingTextBox implements TutorialSequenceElement, EventConsumer {
    private UIMLTextBoxRenderElement textBox;
    private Supplier<UIMLTextBoxRenderElement> textBoxSupplier;
    private TutorialEventListener[] eventListener;
    private int index = 0;

    public static BlockingTextBox onMap(Level l, BoxSize size, float x, float y, TextAlign textAlign, String text, TutorialEventListener... eventListener) {
        return new BlockingTextBox(l, x, y, size, textAlign, text, true, eventListener);
    }

    public static BlockingTextBox onUI(Level l, BoxSize size, float x, float y, TextAlign textAlign, String text, TutorialEventListener... eventListener) {
        return new BlockingTextBox(l, x, y, size, textAlign, text, false, eventListener);
    }

    private BlockingTextBox(Level l, float x, float y, BoxSize boxSize, TextAlign textAlign, String text, boolean map, TutorialEventListener... eventListener) {
        this.eventListener = eventListener;
        ObjPos pos = map ? Tile.getFractionalRenderPos(x, y) : new ObjPos(x, y);
        textBoxSupplier = () -> new UIMLTextBoxRenderElement(map ? l.levelRenderer.mainRenderer : l.levelRenderer.levelUIRenderer, map ? RenderOrder.TUTORIAL_UI : RenderOrder.TUTORIAL_LEVEL_UI,
                pos.x - boxSize.size.x / 2, pos.y, boxSize.size.x, boxSize.size.y, 0.7f, textAlign, box ->
                box.setColourTheme(UIColourTheme.LIGHT_BLUE_OPAQUE_CENTER), false)
                .setText(text);
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
