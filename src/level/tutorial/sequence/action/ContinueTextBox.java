package level.tutorial.sequence.action;

import foundation.MainPanel;
import foundation.math.ObjPos;
import level.Level;
import level.tile.Tile;
import level.tutorial.sequence.BoxSize;
import render.types.container.UIContainer;
import render.types.text.TextAlign;
import render.types.tutorial.LevelTutorialContinueTextBox;
import render.types.tutorial.TutorialContinueTextBox;

import java.util.function.Supplier;

public class ContinueTextBox implements TutorialSequenceElement {
    private UIContainer textBox;
    private Supplier<UIContainer> textBoxSupplier;

    public static ContinueTextBox onMap(Level l, BoxSize size, float x, float y, TextAlign textAlign, String text) {
        return new ContinueTextBox(l, x, y, size, textAlign, text, true);
    }

    public static ContinueTextBox onUI(Level l, BoxSize size, float x, float y, TextAlign textAlign, String text) {
        return new ContinueTextBox(l, x, y, size, textAlign, text, false);
    }

    private ContinueTextBox(Level l, float x, float y, BoxSize boxSize, TextAlign textAlign, String text, boolean map) {
        ObjPos pos = map ? Tile.getFractionalRenderPos(x, y) : new ObjPos(x, y);
        if (map)
            textBoxSupplier = () -> new TutorialContinueTextBox(l, pos.x, pos.y, boxSize.width, textAlign, text, TutorialSequenceElement::next);
        else
            textBoxSupplier = () -> new LevelTutorialContinueTextBox(l, pos.x, pos.y, boxSize.width, textAlign, text, TutorialSequenceElement::next);
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
    }
}
