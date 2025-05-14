package render.level;

import foundation.input.ButtonOrder;
import foundation.input.ButtonRegister;
import foundation.input.InputType;
import level.Level;
import render.*;
import render.anim.LerpAnimation;
import render.level.tile.RenderElement;
import render.types.container.LevelUIContainer;
import render.types.input.button.UIButton;
import unit.UnitTeam;

import java.awt.*;

public class LocalNextPlayerScreen extends LevelUIContainer<Level> {
    private LerpAnimation timer = new LerpAnimation(0.5f);
    private UIButton text, continueButton;
    private boolean bot;

    public LocalNextPlayerScreen(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, Level level) {
        super(register, buttonRegister, RenderOrder.INFO_SCREEN, ButtonOrder.INFO_SCREEN, 0, 0, level);
        addRenderables((r, b) -> {
            new RenderElement(r, RenderOrder.INFO_SCREEN_BACKGROUND, g -> {
                g.setColor(new Color(0, 0, 0, timer.normalisedProgress()));
                g.fillRect(0, 0, (int) (Renderable.right() + 1), (int) (Renderable.top() + 1));
                boolean f = timer.finished();
                text.setEnabled(f);
                continueButton.setEnabled(f);
                if (f && timer.reversed()) {
                    setEnabled(false);
                }
            });
            text = new UIButton(r, b, RenderOrder.INFO_SCREEN, ButtonOrder.INFO_SCREEN, Renderable.right() / 2 - 16, Renderable.top() / 2 + 8, 32, 4, 2.4f, false)
                    .setClickEnabled(false).setBold();
            continueButton = new UIButton(r, b, RenderOrder.INFO_SCREEN, ButtonOrder.INFO_SCREEN, Renderable.right() / 2 - 7, Renderable.top() / 2 - 10, 14, 4, 2.4f, false)
                    .setText("Continue").setBold().setOnClick(this::disable);
        });
        timer.setReversed(true);
    }

    public void enable(UnitTeam nextTeam) {
        boolean bot = level.bots.get(nextTeam);
        this.bot = bot;
        if (timer.reversed()) {
            timer.setReversed(false);
            timer.startTimer();
        }
        text.setText(nextTeam.getName() + (bot ? " bot is playing" : " is ready to play"))
                .setColourTheme(nextTeam.uiColour);
        continueButton.setClickEnabled(!bot);
        continueButton.setColourTheme(bot ? UIColourTheme.GRAYED_OUT : UIColourTheme.LIGHT_BLUE);
        setEnabled(true);
    }

    private void disable() {
        timer.setReversed(true);
        if (bot)
            level.preEndTurn();
        else
            level.endTurn();
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled() && !(timer.finished() && timer.reversed());
    }

    @Override
    public boolean blocking(InputType type) {
        return true;
    }

    @Override
    public void delete() {
        super.delete();
        text = null;
        continueButton = null;
    }
}
