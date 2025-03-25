package render.types.text;

import level.Level;
import render.*;
import render.anim.PowAnimation;
import render.anim.LerpAnimation;
import unit.UnitTeam;

public class UITextNotification extends AbstractRenderElement {
    private final UITextLabel label;
    private Level level;
    private boolean enabled = false;
    private String text = null;
    private final float width, height;
    private PowAnimation anim = new PowAnimation(0.3f, 1.5f);
    private LerpAnimation holdTimer = new LerpAnimation(0.8f);
    private HoldState holdState = HoldState.FINISHED;

    public UITextNotification(RenderRegister<OrderedRenderable> register, RenderOrder order, Level level, float width, float height) {
        super(register, order);
        this.level = level;
        this.width = width;
        this.height = height;
        label = new UITextLabel(width, height, true, 0.5f).setTextLeftBold();
        renderable = g -> {
            if (!enabled)
                return;
            label.updateLabelWidth(width * anim.normalisedProgress());
            label.updateTextLeft(text);
            switch (holdState) {
                case STARTED -> {
                    if (anim.finished()) {
                        holdState = HoldState.HELD;
                        holdTimer.startTimer();
                    }
                }
                case HELD -> {
                    if (holdTimer.finished()) {
                        holdState = HoldState.FINISHED;
                        anim.setReversed(true);
                        anim.startTimer();
                    }
                }
                case FINISHED -> {
                    if (anim.finished()) {
                        anim.setReversed(false);
                        level.levelRenderer.removeAnimBlock(this);
                        enabled = false;
                    }
                }
            }
            GameRenderer.renderOffset(Renderable.right() / 2 - width / 2, Renderable.top() / 2 - height / 2, g, () -> label.render(g));
        };
    }

    public void start(String text, UnitTeam team) {
        holdState = HoldState.STARTED;
        this.text = text;
        level.levelRenderer.registerAnimBlock(this);
        anim.startTimer();
        enabled = true;
        label.setLabelColour(team.uiColour);
    }

    @Override
    public void delete() {
        super.delete();
        level = null;
    }

    private enum HoldState {
        STARTED,
        HELD,
        FINISHED
    }
}
