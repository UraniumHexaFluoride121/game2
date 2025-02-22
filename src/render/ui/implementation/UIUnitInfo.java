package render.ui.implementation;

import foundation.input.ButtonOrder;
import foundation.input.ButtonRegister;
import foundation.input.InputType;
import foundation.math.MathUtil;
import foundation.math.ObjPos;
import foundation.math.StaticHitBox;
import level.Level;
import render.*;
import render.renderables.HexagonBorder;
import render.renderables.HighlightTileRenderer;
import render.renderables.RenderElement;
import render.ui.UIColourTheme;
import render.ui.types.LevelUIContainer;
import render.ui.types.UIBox;
import render.ui.types.UIShapeButton;
import render.ui.types.UITextLabel;
import unit.Unit;
import unit.UnitPose;
import unit.action.Action;
import unit.weapon.WeaponInstance;

import java.awt.*;
import java.util.HashSet;

import static unit.Unit.*;

public class UIUnitInfo extends LevelUIContainer {
    private final UITextLabel title = new UITextLabel(11f, 0.9f, true).setTextLeftBold();
    private final UITextLabel hp = new UITextLabel(9.4f, 1, false);
    private final UITextLabel ammo = new UITextLabel(9.4f, 1, false);
    private final UITextLabel shield = new UITextLabel(9.4f, 1, false);
    private final UIBox box = new UIBox(11, 14);
    private final StaticHitBox hitBox = StaticHitBox.createFromOriginAndSize(0.5f, 0.5f, 11, 14);
    private Level level;
    public boolean showFiringRange = false;

    public UIUnitInfo(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, ButtonOrder buttonOrder, Level level) {
        super(register, buttonRegister, order, buttonOrder, 0, 0, level);
        this.level = level;
        hp.updateTextLeft("HP:").setTextLeftBold().setTextRightBold();
        ammo.updateTextLeft("Ammo:").setTextLeftBold().setTextRightBold();
        shield.updateTextLeft("Shield HP:").setTextLeftBold().setTextRightBold();
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
                        hp.updateTextRight(MathUtil.floatToString(unit.hitPoints, 1));
                        hp.render(g);
                        if (unit.type.shieldHP != 0) {
                            g.translate(0, -1);
                            shield.updateTextRight(MathUtil.floatToString(unit.shieldHP, 1));
                            shield.render(g);
                        }
                        if (!unit.weapons.isEmpty()) {
                            g.translate(0, -1);
                            WeaponInstance weapon = unit.getAmmoWeapon();
                            ammo.setRightOffset(weapon == null ? 0.4f : 0);
                            ammo.updateTextRight(weapon == null ? "--" : weapon.ammo + "/" + weapon.ammoCapacity);
                            ammo.setLabelColour(weapon == null || weapon.ammo != 0 ? UIColourTheme.LIGHT_BLUE : UIColourTheme.RED);
                            ammo.render(g);
                        }
                    });
                });
            }).setZOrder(-1);
            new UIShapeButton(r, b, RenderOrder.LEVEL_UI, ButtonOrder.LEVEL_UI, 9.5f, 12.5f, 1.5f, 1.5f, false)
                    .setShape(UIShapeButton::i).drawShape(0.25f).setBoxCorner(0.3f).setOnClick(() -> {
                        Unit unit = level.selectedUnit;
                        if (unit != null)
                            level.levelRenderer.unitInfoScreen.enable(unit);
                    });
            new UIShapeButton(r, b, RenderOrder.LEVEL_UI, ButtonOrder.LEVEL_UI, 7.5f, 12.5f, 1.5f, 1.5f, true)
                    .setShape(UIShapeButton::target).drawShape(0.12f).setColourTheme(UIColourTheme.RED).setBoxCorner(0.3f).setOnClick(() -> {
                        Unit unit = level.selectedUnit;
                        if (unit == null)
                            return;
                        HashSet<Point> tiles = unit.tilesInFiringRange(false);
                        level.levelRenderer.highlightTileRenderer = new HighlightTileRenderer(Action.FIRE.tileColour, tiles, level);
                        level.levelRenderer.unitTileBorderRenderer = new HexagonBorder(tiles, FIRE_TILE_BORDER_COLOUR);
                        showFiringRange = true;
                    }).setOnDeselect(this::closeFiringRangeView).toggleMode();
        });
    }

    public void closeFiringRangeView() {
        level.closeBorderRenderer();
        showFiringRange = false;
    }

    @Override
    public boolean isEnabled() {
        return level.selectedUnit != null && super.isEnabled();
    }

    @Override
    public void delete() {
        super.delete();
        renderable = null;
        level = null;
    }

    @Override
    public boolean posInsideLevelOffset(ObjPos pos) {
        return hitBox.isPositionInside(pos);
    }

    @Override
    public boolean blocking(InputType type) {
        return super.blocking(type) || type.isMouseInput();
    }
}
