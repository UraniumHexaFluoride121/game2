package render.ui.implementation;

import level.Level;
import render.*;
import render.anim.PowAnimation;
import render.anim.LerpAnimation;
import render.ui.types.UITextLabel;
import unit.UnitTeam;

public class UIOnNextTurn extends AbstractRenderElement {
    private final UITextLabel label = new UITextLabel(20, 3, true, 0.5f).setTextLeftBold();
    private Level level;
    private boolean enabled = false;
    private String text = null;
    private PowAnimation anim = new PowAnimation(0.3f, 1.5f);
    private LerpAnimation holdTimer = new LerpAnimation(0.8f);
    private HOLD_STATE holdState = HOLD_STATE.FINISHED;

    public UIOnNextTurn(RenderRegister<OrderedRenderable> register, RenderOrder order, Level level) {
        super(register, order);
        this.level = level;
        renderable = g -> {
            if (!enabled)
                return;
            label.updateLabelWidth(20 * anim.normalisedProgress());
            label.updateTextLeft(text);
            switch (holdState) {
                case STARTED -> {
                    if (anim.finished()) {
                        holdState = HOLD_STATE.HELD;
                        holdTimer.startTimer();
                    }
                }
                case HELD -> {
                    if (holdTimer.finished()) {
                        holdState = HOLD_STATE.FINISHED;
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
            GameRenderer.renderOffset(Renderable.right() / 2 - 10, Renderable.top() / 2 - 3 / 2f, g, () -> label.render(g));
        };
    }

    public void start(String text, UnitTeam team) {
        holdState = HOLD_STATE.STARTED;
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

    private enum HOLD_STATE {
        STARTED,
        HELD,
        FINISHED
    }
}
