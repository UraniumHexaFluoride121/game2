package render.types.tutorial;

import foundation.MainPanel;
import foundation.input.InputType;
import level.Level;
import level.tutorial.sequence.action.TutorialSequenceElement;
import render.HorizontalAlign;
import render.RenderOrder;
import render.Renderable;
import render.UIColourTheme;
import render.level.info.UnitInfoScreen;
import render.types.UIFullScreenColour;
import render.types.box.UIDisplayBox;
import render.types.box.UIDisplayBoxRenderElement;
import render.types.container.LevelUIContainer;
import render.types.container.UIContainer;
import render.types.container.UIScrollSurface;
import render.types.input.button.UIButton;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class TutorialScreen extends LevelUIContainer<Level> {
    public static final float NORMAL_WIDTH = 40;
    public final float width;
    private UIScrollSurface scrollSurface;
    private UIButton continueButton = null;

    public static TutorialScreenElement create(Level l, float width, BiConsumer<UIDisplayBox, Level> boxConsumer) {
        return new TutorialScreenElement(l, width, boxConsumer);
    }

    public TutorialScreen(Level l, float width, HorizontalAlign textAlign, BiConsumer<UIDisplayBox, Level> boxConsumer, Runnable onContinue) {
        super(l.levelRenderer.levelUIRenderer, l.buttonRegister, RenderOrder.TUTORIAL_INFO_SCREEN, 0, 0, l);
        this.width = width;
        addRenderables((r, b) -> {
            new UIFullScreenColour(r, RenderOrder.TUTORIAL_INFO_SCREEN, UnitInfoScreen.FULL_SCREEN_MENU_BACKGROUND_COLOUR)
                    .setZOrder(-100);
            scrollSurface = new UIScrollSurface(r, b, RenderOrder.TUTORIAL_INFO_SCREEN, 0, 0, Renderable.right(), Renderable.top(), false, (r2, b2) -> {
                UIDisplayBoxRenderElement element = new UIDisplayBoxRenderElement(r2, RenderOrder.TUTORIAL_INFO_SCREEN, Renderable.right() / 2, 0, width, -1, box -> {
                    box.setColourTheme(UIColourTheme.LIGHT_BLUE_BOX_DARK);
                }, false);
                boxConsumer.accept(element.box, l);
                element.box.setHorizontalAlign(HorizontalAlign.CENTER).addOnUpdate(() -> {
                    UIDisplayBox box = element.box;
                    float maxHeight = Renderable.top() * 0.8f;
                    float y;
                    if (box.height > maxHeight) {
                        y = -Renderable.top() / 2 - box.height + maxHeight / 2;
                        box.setY(y);
                    } else {
                        y = -Renderable.top() / 2 - box.height / 2;
                        box.setY(y);
                    }
                    if (continueButton != null) {
                        continueButton.delete();
                    }
                    continueButton = new UIButton(r2, b2, RenderOrder.TUTORIAL_INFO_SCREEN, Renderable.right() / 2 - 2, y - 2, 4, 1.5f, 0.8f, false)
                            .setBold().setText("Continue").setColourTheme(UIColourTheme.DEEP_GREEN).setOnClick(onContinue);
                });
            });
        });
    }

    @Override
    public void delete() {
        super.delete();
        scrollSurface = null;
        continueButton = null;
    }

    public static class TutorialScreenElement implements TutorialSequenceElement {
        private UIContainer textBox;
        private Supplier<UIContainer> textBoxSupplier;

        private TutorialScreenElement(Level l, float width, BiConsumer<UIDisplayBox, Level> boxConsumer) {
            textBoxSupplier = () -> new TutorialScreen(l, width, HorizontalAlign.CENTER, boxConsumer, TutorialSequenceElement::next);
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

    @Override
    public boolean blocking(InputType type) {
        return true;
    }
}
