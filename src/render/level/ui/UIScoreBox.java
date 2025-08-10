package render.level.ui;

import foundation.input.ButtonRegister;
import level.Level;
import level.PlayerTeam;
import render.OrderedRenderable;
import render.RenderOrder;
import render.RenderRegister;
import render.UIColourTheme;
import render.level.tile.RenderElement;
import render.types.box.UIBox;
import render.types.container.UIContainer;
import render.types.text.UITextLabel;

import static render.types.text.TextRenderable.*;

public class UIScoreBox extends UIContainer {
    public UIScoreBox(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, float x, float y, PlayerTeam team, Level level) {
        super(register, buttonRegister, order, x, y);
        addRenderables((r, b) -> {
            int damageScore = (int) level.getPlayerTeamScore(level::getDamageScore, team, false);
            int turnScore = (int) level.getPlayerTeamScore(level::getTurnScore, team, false);
            int unitsDestroyedScore = (int) level.getPlayerTeamScore(level::getUnitsDestroyedScore, team, false);
            new RenderElement(r, RenderOrder.INFO_SCREEN,
                    new UIBox(22, 8).setColourTheme(UIColourTheme.LIGHT_BLUE_BOX_DARK),
                    new UITextLabel(12, 1f, false)
                            .setTextLeftBold().updateTextLeft("Team Victory:")
                            .translate(1, 6.5f),
                    new UITextLabel(6, 1f, false)
                            .setTextRightBold().updateTextRight(Level.VICTORY_SCORE + " " + STAR.display)
                            .translate(14, 6.5f),
                    new UITextLabel(12, 1f, false)
                            .setTextLeftBold().updateTextLeft("Remaining HP:")
                            .setTextRightBold().updateTextRight((int) (level.getPlayerTeamScore(level::getTotalRemainingHP, team, true)) + " / " + (int) (level.getPlayerTeamScore(level::getTotalMaxHP, team, true)))
                            .translate(1, 6.5f - 1 * 1.5f),
                    new UITextLabel(6, 1f, false)
                            .setTextRightBold().updateTextRight(damageScore + " / " + Level.DAMAGE_SCORE_MAX + " " + STAR.display)
                            .translate(14, 6.5f - 1 * 1.5f),
                    new UITextLabel(12, 1f, false)
                            .setTextLeftBold().updateTextLeft("Turns Taken:")
                            .setTextRightBold().updateTextRight(String.valueOf(level.getTurn()))
                            .translate(1, 6.5f - 2 * 1.5f),
                    new UITextLabel(6, 1f, false)
                            .setTextRightBold().updateTextRight(turnScore + " / " + Level.TURN_SCORE_MAX + " " + STAR.display)
                            .translate(14, 6.5f - 2 * 1.5f),
                    new UITextLabel(12, 1f, false)
                            .setTextLeftBold().updateTextLeft("Units Destroyed:")
                            .setTextRightBold().updateTextRight(String.valueOf(level.getPlayerTeamScoreInt(level::getUnitsDestroyedByTeam, team, true)))
                            .translate(1, 6.5f - 3 * 1.5f),
                    new UITextLabel(6, 1f, false)
                            .setTextRightBold().updateTextRight(unitsDestroyedScore + " / " + Level.UNITS_DESTROYED_SCORE_MAX + " " + STAR.display)
                            .translate(14, 6.5f - 3 * 1.5f),
                    new UITextLabel(10, 1f, false)
                            .setTextLeftBold().updateTextLeft("Total:")
                            .setTextRightBold().updateTextRight((damageScore + turnScore + unitsDestroyedScore + 100) + " " + STAR.display)
                            .translate(22 / 2f - 10 / 2f, 6.5f - 4 * 1.5f)
            );
        });
    }
}
