package render.level.info;

import foundation.input.ButtonOrder;
import foundation.input.ButtonRegister;
import foundation.input.InputType;
import foundation.input.OnButtonInput;
import foundation.math.MathUtil;
import foundation.math.ObjPos;
import foundation.math.StaticHitBox;
import level.Level;
import level.tile.TileModifier;
import level.tile.TileSet;
import level.tutorial.TutorialElement;
import level.tutorial.TutorialManager;
import level.tutorial.sequence.event.EventUIDeselect;
import level.tutorial.sequence.event.EventUISelect;
import level.tutorial.sequence.event.UIElement;
import render.*;
import render.level.tile.MultiColourHighlight;
import render.level.tile.RenderElement;
import render.types.box.UIBox;
import render.types.container.LevelUIContainer;
import render.types.input.button.UIShapeButton;
import render.types.text.UITextLabel;
import render.types.text.UITooltip;
import unit.Unit;
import unit.UnitData;
import unit.UnitPose;
import unit.action.Action;
import unit.weapon.WeaponInstance;

import java.awt.*;

import static unit.Unit.*;

public class UIUnitInfo extends LevelUIContainer<Level> {
    private final UITextLabel title = new UITextLabel(11f, 0.9f, true).setTextLeftBold();
    private final UITextLabel hp = new UITextLabel(9.4f, 1, false);
    private final UITextLabel ammo = new UITextLabel(9.4f, 1, false);
    private final UITextLabel shield = new UITextLabel(9.4f, 1, false);
    private final UITextLabel shieldRegen = new UITextLabel(9.4f, 1, false);
    private final UITextLabel repair = new UITextLabel(9.4f, 1, false);
    private final UIBox box = new UIBox(11, 14).setColourTheme(UIColourTheme.LIGHT_BLUE_OPAQUE_CENTER_LIGHT);
    private final StaticHitBox hitBox = StaticHitBox.createFromOriginAndSize(0.5f, 0.5f, 11, 14);
    private Level level;
    public UIShapeButton viewFiringRange, viewEffectiveness;
    private MultiColourHighlight rangeHighlight = null, effectivenessHighlight = null;

    public UIUnitInfo(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, ButtonOrder buttonOrder, Level level) {
        super(register, buttonRegister, order, buttonOrder, 0, 0, level);
        this.level = level;
        hp.updateTextLeft("HP:").setTextLeftBold().setTextRightBold();
        ammo.updateTextLeft("Ammo:").setTextLeftBold().setTextRightBold();
        shield.updateTextLeft("Shield HP:").setTextLeftBold().setTextRightBold();
        shieldRegen.updateTextLeft("Sh. Regen:").setTextLeftBold().setTextRightBold();
        repair.updateTextLeft("Repair:").setTextLeftBold().setTextRightBold();
        addRenderables((r, b) -> {
            new RenderElement(r, RenderOrder.LEVEL_UI, g -> {
                GameRenderer.renderTransformed(g, () -> {
                    Unit unit = level.selectedUnit;
                    g.translate(0.5, 0.5);
                    box.render(g);
                    GameRenderer.renderOffset(0, 4, g, () -> {
                        Renderable.renderImage(unit.type.getImage(unit.team, UnitPose.INFO), false, true, 11).render(g);
                    });
                    GameRenderer.renderOffset(-0.2f, 14.5f, g, () -> {
                        title.updateTextLeft(unit.type.getName());
                        title.render(g);
                    });
                    GameRenderer.renderOffset(0.5f, 4.5f, g, () -> {
                        hp.updateTextRight(MathUtil.floatToString(unit.hitPoints, 1) + " / " + MathUtil.floatToString(unit.type.hitPoints, 1));
                        hp.render(g);
                        if (unit.type.shieldHP != 0) {
                            g.translate(0, -1);
                            shield.updateTextRight(MathUtil.floatToString(unit.shieldHP, 1) + " / " + MathUtil.floatToString(unit.type.shieldHP, 1));
                            shield.render(g);
                        }
                        if (unit.type.canPerformAction(Action.SHIELD_REGEN)) {
                            g.translate(0, -1);
                            shieldRegen.updateTextRight("+" + MathUtil.floatToString(unit.type.shieldRegen, 1) + " HP");
                            shieldRegen.render(g);
                        }
                        if (!unit.weapons.isEmpty()) {
                            g.translate(0, -1);
                            WeaponInstance weapon = unit.getAmmoWeapon();
                            ammo.setRightOffset(weapon == null ? 0.4f : 0);
                            ammo.updateTextRight(weapon == null ? "--" : weapon.ammo + "/" + weapon.ammoCapacity);
                            ammo.setLabelColour(weapon == null || weapon.ammo != 0 ? UIColourTheme.LIGHT_BLUE : UIColourTheme.RED);
                            ammo.render(g);
                        }
                        if (unit.type.canPerformAction(Action.REPAIR)) {
                            g.translate(0, -1);
                            repair.updateTextRight("+" + MathUtil.floatToString(unit.type.repair, 1) + " HP");
                            repair.render(g);
                        }
                    });
                });
            }).setZOrder(-1);
            new UIShapeButton(r, b, RenderOrder.LEVEL_UI, ButtonOrder.LEVEL_UI, 9.5f, 12.5f, 1.5f, 1.5f, false)
                    .setShape(UIShapeButton::i).drawShape(0.25f).setBoxCorner(0.3f).setOnClick(() -> {
                        Unit unit = level.selectedUnit;
                        if (unit != null)
                            level.levelRenderer.unitInfoScreen.enable(unit);
                    }).tooltip(t -> t.add(-1, UITooltip.dark(), "Click to view detailed unit info"));
            viewFiringRange = new UIShapeButton(r, b, RenderOrder.LEVEL_UI, ButtonOrder.LEVEL_UI, 7.5f, 12.5f, 1.5f, 1.5f, true) {
                @Override
                public boolean selectEnabled() {
                    return TutorialManager.isEnabled(TutorialElement.ACTION_DESELECT) && TutorialManager.isEnabled(TutorialElement.VIEW_FIRING_RANGE);
                }

                @Override
                public boolean deselectEnabled() {
                    return TutorialManager.isEnabled(TutorialElement.VIEW_FIRING_RANGE_DESELECT);
                }
            }
                    .setShape(UIShapeButton::target).drawShape(0.12f).setColourTheme(UIColourTheme.RED).setBoxCorner(0.3f).setOnClick(() -> {
                        Unit unit = level.selectedUnit;
                        if (unit == null || level.getActiveTeam() != level.getThisTeam())
                            return;
                        closeEffectivenessView();
                        level.endAction();
                        TileSet tiles = unit.tilesInFiringRange(level.currentVisibility, new UnitData(unit), false);
                        MultiColourHighlight highlight = new MultiColourHighlight(tiles, p -> Action.FIRE.tileColour, 1, FIRE_TILE_BORDER_COLOUR);
                        highlight.setOnFinish(() -> {
                            level.levelRenderer.removeTileHighlight(highlight);
                            if (highlight == rangeHighlight)
                                viewFiringRange.deselect();
                        });
                        rangeHighlight = highlight;
                        level.levelRenderer.registerTileHighlight(highlight, true);
                        TutorialManager.acceptEvent(new EventUISelect(level, UIElement.VIEW_FIRING_RANGE));
                    }).setOnDeselect(this::closeFiringRangeView).toggleMode()
                    .tooltip(t -> t.add(-1, UITooltip.dark(), "Click to view unit firing range"));
            viewEffectiveness = new UIShapeButton(r, b, RenderOrder.LEVEL_UI, ButtonOrder.LEVEL_UI, 5.5f, 12.5f, 1.5f, 1.5f, true) {
                @Override
                public boolean selectEnabled() {
                    return TutorialManager.isEnabled(TutorialElement.ACTION_DESELECT) && TutorialManager.isEnabled(TutorialElement.VIEW_EFFECTIVENESS);
                }

                @Override
                public boolean deselectEnabled() {
                    return TutorialManager.isEnabled(TutorialElement.VIEW_EFFECTIVENESS_DESELECT);
                }
            }
                    .setShape(box -> UIShapeButton.targetRotated(box, 45)).drawShape(0.12f).setColourTheme(UIColourTheme.ORANGE).setBoxCorner(0.3f).setOnClick(() -> {
                        Unit unit = level.selectedUnit;
                        if (unit == null || level.getActiveTeam() != level.getThisTeam())
                            return;
                        closeFiringRangeView();
                        level.endAction();
                        TileSet tiles = TileSet.all(level).m(level, t -> t.unitFilter(TileModifier.withVisibleEnemies(unit.team, level)));
                        MultiColourHighlight highlight = new MultiColourHighlight(tiles, p -> unit.getWeaponEffectivenessAgainst(level.getUnit(p)).getDamageColour(), 0.25f, new Color(0, 0, 0, 0));
                        highlight.setOnFinish(() -> {
                            level.levelRenderer.removeTileHighlight(highlight);
                            if (highlight == effectivenessHighlight)
                                viewEffectiveness.deselect();
                        });
                        effectivenessHighlight = highlight;
                        level.levelRenderer.registerTileHighlight(highlight, true);
                        TutorialManager.acceptEvent(new EventUISelect(level, UIElement.VIEW_EFFECTIVENESS));
                    }).setOnDeselect(this::closeEffectivenessView).toggleMode()
                    .tooltip(t -> t.add(12, UITooltip.dark(), "Click to view this unit's weapon effectiveness against enemies. Colour ranges between yellow for low damage and red for high damage. Blue colour means no damage."));
            new OnButtonInput(b, ButtonOrder.LEVEL_UI, type -> type.isCharInput, type -> {
                switch (type.c) {
                    case 'c' -> {
                        if (viewFiringRange.isSelected())
                            viewFiringRange.deselect();
                        else
                            viewFiringRange.select();
                    }
                    case 'x' -> {
                        if (viewEffectiveness.isSelected())
                            viewEffectiveness.deselect();
                        else
                            viewEffectiveness.select();
                    }
                    default -> {
                        viewEffectiveness.deselect();
                        viewFiringRange.deselect();
                    }
                }
            });
        });
    }

    public void closeFiringRangeView() {
        if (rangeHighlight != null && viewFiringRange.deselectEnabled()) {
            rangeHighlight.close();
            TutorialManager.acceptEvent(new EventUIDeselect(level, UIElement.VIEW_FIRING_RANGE));
        }
    }

    public void closeEffectivenessView() {
        if (effectivenessHighlight != null && viewEffectiveness.deselectEnabled()) {
            effectivenessHighlight.close();
            TutorialManager.acceptEvent(new EventUIDeselect(level, UIElement.VIEW_EFFECTIVENESS));
        }
    }

    @Override
    public boolean isEnabled() {
        return level.selectedUnit != null && level.selectedUnit.renderVisible() && super.isEnabled();
    }

    @Override
    public void delete() {
        super.delete();
        renderable = null;
        level = null;
        viewFiringRange = null;
    }

    @Override
    public boolean posInsideLevelOffset(ObjPos pos) {
        return hitBox.isPositionInside(pos);
    }

    @Override
    public boolean blocking(InputType type) {
        return super.blocking(type) || type.isMouseInput();
    }

    public boolean showFiringRange() {
        return rangeHighlight != null && !rangeHighlight.finished();
    }

    public boolean showEffectiveness() {
        return effectivenessHighlight != null && !effectivenessHighlight.finished();
    }
}
