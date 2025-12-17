package level.tutorial.sequence.action;

import foundation.MainPanel;
import foundation.math.ObjPos;
import level.Level;
import level.tile.Tile;
import level.tutorial.sequence.BoxSize;
import level.tutorial.sequence.SequenceHolder;
import level.tutorial.sequence.TutorialSequence;
import level.tutorial.sequence.event.EventConsumer;
import level.tutorial.sequence.event.TutorialEvent;
import level.tutorial.sequence.event.TutorialEventListener;
import render.RenderOrder;
import render.UIColourTheme;
import render.HorizontalAlign;
import render.types.box.UIDisplayBoxRenderElement;

import java.util.function.Supplier;

public class SequenceTextBox implements EventConsumer, SequenceHolder {
    private UIDisplayBoxRenderElement textBox;
    private Supplier<UIDisplayBoxRenderElement> textBoxSupplier;
    private final TutorialSequence tutorialSequence = new TutorialSequence();

    public static SequenceTextBox onMap(Level l, BoxSize size, float x, float y, HorizontalAlign textAlign, String text, TutorialSequenceElement... sequence) {
        return new SequenceTextBox(l, x, y, size, textAlign, text, true, sequence);
    }

    public static SequenceTextBox onUI(Level l, BoxSize size, float x, float y, HorizontalAlign textAlign, String text, TutorialSequenceElement... sequence) {
        return new SequenceTextBox(l, x, y, size, textAlign, text, false, sequence);
    }

    private SequenceTextBox(Level l, float x, float y, BoxSize boxSize, HorizontalAlign textAlign, String text, boolean map, TutorialSequenceElement... sequence) {
        tutorialSequence.setSequence(sequence);
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
    public TutorialEventListener[] getListeners() {
        return new TutorialEventListener[0];
    }

    @Override
    public void start() {
        textBox = textBoxSupplier.get();
        tutorialSequence.start();
    }

    @Override
    public void end() {
        MainPanel.addTask(textBox::delete);
    }

    @Override
    public void delete() {
        textBoxSupplier = null;
        textBox = null;
        tutorialSequence.deleteSequence();
    }

    @Override
    public void accept(TutorialEvent e) {
        tutorialSequence.acceptEvent(e);
    }

    @Override
    public boolean incrementSequence() {
        return tutorialSequence.incrementSequence();
    }
}
