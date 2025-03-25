package render.level;

import foundation.MainPanel;
import foundation.input.ButtonOrder;
import foundation.input.ButtonRegister;
import level.Level;
import level.PlayerTeam;
import render.OrderedRenderable;
import render.RenderOrder;
import render.RenderRegister;
import render.Renderable;
import render.anim.LerpAnimation;
import render.level.tile.RenderElement;
import render.texture.ResourceLocation;
import render.types.container.LevelUIContainer;
import render.types.input.button.UIButton;
import render.types.container.UIContainer;
import render.types.text.UITextLabel;
import unit.UnitTeam;

public class GameEndScreen extends LevelUIContainer<Level> {
    private final LerpAnimation overlayTimer = new LerpAnimation(1);
    private boolean renderablesAdded = false;

    public GameEndScreen(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, Level level) {
        super(register, buttonRegister, RenderOrder.END_SCREEN, ButtonOrder.END_SCREEN, 0, 0, level);
        addRenderables((r, b) -> {
            new RenderElement(r, RenderOrder.TITLE_SCREEN_BACKGROUND,
                    g -> {
                        if (!renderablesAdded && overlayTimer.normalisedProgress() > 0.5f) {
                            createScreen();
                            renderablesAdded = true;
                        }
                        overlayTimer.renderOverlay(g, LerpAnimation::triangleProgress);
                    }
            );
        });
    }

    private void createScreen() {
        Renderable titleScreenImage = Renderable.renderImage(new ResourceLocation("title_screen.png"), false, true, 60, true);
        addRenderables((r, b) -> {
            PlayerTeam surviving = level.survivingPlayerTeam();
            new RenderElement(r, RenderOrder.TITLE_SCREEN_BACKGROUND,
                    titleScreenImage,
                    new UITextLabel(30, 3, false).setTextLeftBold().setLeftOffset(1)
                            .updateTextLeft(level.initialPlayerTeams.get(level.getThisTeam()) == surviving ?
                                    "Victory" : "Defeat").translate(2, Renderable.top() - 4),
                    new UITextLabel(12, 1.5f, false).setTextCenterBold()
                            .updateTextCenter("Team " + surviving.getName() + " wins").translate(3, Renderable.top() - 6)
            );
            int i = 0;
            for (UnitTeam team : UnitTeam.ORDERED_TEAMS) {
                if (level.initialPlayerTeams.containsKey(team) && level.initialPlayerTeams.get(team) == surviving) {
                    new RenderElement(r, RenderOrder.TITLE_SCREEN_BACKGROUND,
                            new UITextLabel(8, 1f, false).setTextCenterBold().setLabelColour(team.uiColour)
                                    .updateTextCenter(team.getName()).translate(4.5f, Renderable.top() - 7.5f - i * 1.2f)
                    ).setZOrder(1);
                    i++;
                }
            }
            new UIButton(r, b, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS, Renderable.right() / 2 - 5, 2, 10, 2, 1.5f, false)
                    .setText("Main Menu").setBold().setOnClick(() -> MainPanel.addTask(MainPanel::toTitleScreen));
        });
    }

    @Override
    public UIContainer setEnabled(boolean enabled) {
        if (enabled)
            overlayTimer.startTimer();
        return super.setEnabled(enabled);
    }
}
