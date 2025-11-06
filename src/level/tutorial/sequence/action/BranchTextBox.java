package level.tutorial.sequence.action;

import foundation.MainPanel;
import foundation.math.ObjPos;
import level.Level;
import level.tile.Tile;
import level.tutorial.TutorialManager;
import level.tutorial.sequence.BoxSize;
import level.tutorial.sequence.SequenceHolder;
import level.tutorial.sequence.TutorialSequence;
import level.tutorial.sequence.event.EventConsumer;
import level.tutorial.sequence.event.TutorialEvent;
import level.tutorial.sequence.event.TutorialEventListener;
import render.HorizontalAlign;
import render.RenderOrder;
import render.UIColourTheme;
import render.types.box.UIDisplayBoxRenderElement;

import java.util.function.Supplier;

public class BranchTextBox implements TutorialSequenceElement, EventConsumer, SequenceHolder {
    private UIDisplayBoxRenderElement textBox;
    private Supplier<UIDisplayBoxRenderElement> textBoxSupplier;
    private final TutorialSequence tutorialSequence = new TutorialSequence();
    private TutorialEventListener trigger, skip;
    private boolean sequence = false;

    public static BranchTextBox onMap(Level l, BoxSize size, float x, float y, HorizontalAlign textAlign, String text, TutorialEventListener trigger, TutorialEventListener skip, TutorialSequenceElement... sequence) {
        return new BranchTextBox(l, x, y, size, textAlign, text, true, trigger, skip, sequence);
    }

    public static BranchTextBox onUI(Level l, BoxSize size, float x, float y, HorizontalAlign textAlign, String text, TutorialEventListener trigger, TutorialEventListener skip, TutorialSequenceElement... sequence) {
        return new BranchTextBox(l, x, y, size, textAlign, text, false, trigger, skip, sequence);
    }

    private BranchTextBox(Level l, float x, float y, BoxSize boxSize, HorizontalAlign textAlign, String text, boolean map, TutorialEventListener trigger, TutorialEventListener skip, TutorialSequenceElement... sequence) {
        this.trigger = trigger;
        this.skip = skip;
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
        tutorialSequence.deleteSequence();
        trigger = null;
        skip = null;
    }

    @Override
    public void accept(TutorialEvent e) {
        if (sequence) {
            tutorialSequence.acceptEvent(e);
            return;
        }
        if (trigger.test(e)) {
            sequence = true;
            tutorialSequence.start();
            MainPanel.addTask(textBox::delete);
        } else if (skip.test(e)) {
            TutorialManager.incrementSequence();
        }
    }

    @Override
    public boolean incrementSequence() {
        return !sequence || tutorialSequence.incrementSequence();
    }
}
