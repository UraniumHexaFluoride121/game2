package render.types.box.display.tutorial;

import foundation.Deletable;
import foundation.math.HexagonalDirection;
import foundation.math.ObjPos;
import render.Renderable;
import render.anim.timer.SineAnimation;
import render.anim.unit.AnimTilePath;
import render.types.text.DynamicTextRenderer;
import unit.UnitData;
import unit.UnitLike;
import unit.UnitPose;
import unit.UnitTeam;
import unit.stats.StatManager;
import unit.type.UnitType;
import unit.weapon.FiringData;

import java.awt.*;
import java.util.function.Consumer;

import static level.tile.Tile.*;

public class TutorialMapUnit extends UnitLike<StatManager<TutorialMapUnit>> implements Renderable, Deletable {
    private final SineAnimation idleAnimX, idleAnimY;
    private ObjPos renderPos;
    private TutorialMapElement map;
    private AnimTilePath movePath = null;
    private final Point startPos;
    private Consumer<TutorialMapUnit> onReset = u -> {};

    public TutorialMapUnit(UnitType type, UnitTeam team, Point pos, TutorialMapElement map) {
        super(new UnitData(type, team, pos), new StatManager<>());
        stats.init(this);
        data.init(stats);
        this.map = map;
        startPos = pos;
        renderPos = TutorialMapElement.getRenderPos(pos);
        idleAnimX = new SineAnimation((5f + (float) Math.random()) * data.type.getBobbingRate(), (float) Math.random() * 360);
        idleAnimY = new SineAnimation((7f + (float) Math.random()) * data.type.getBobbingRate(), (float) Math.random() * 360);
    }

    public void setMovePath(AnimTilePath movePath) {
        this.movePath = movePath;
    }

    @Override
    public void render(Graphics2D g) {
        if (movePath != null && movePath.finished()) {
            moveUnit(movePath.getLastTile(), true);
            movePath = null;
        }
        HexagonalDirection direction = null;
        if (movePath != null) {
            direction = movePath.getDirection();
        }
        renderUnit(g, (direction != null) ? direction.counterClockwise().getPose() : UnitPose.FORWARD, data.type.shieldHP != 0);
        renderDamageUIs(g);
    }

    public void moveUnit(Point pos, boolean transform) {
        if (map.getTile(data.pos).unit == this)
            map.getTile(data.pos).unit = null;
        data.pos = transform ? new Point(-pos.y, pos.x) : pos;
        map.getTile(data.pos).unit = this;
        renderPos = TutorialMapElement.getRenderPos(data.pos);
    }

    public TutorialMapUnit onReset(Consumer<TutorialMapUnit> onReset) {
        this.onReset = onReset;
        return this;
    }

    public TutorialMapUnit restoreHP() {
        return restoreHP(stats.maxHP());
    }

    public TutorialMapUnit restoreHP(float hitPoints) {
        data.hitPoints = hitPoints;
        data.renderHP = hitPoints;
        return this;
    }

    public void reset() {
        movePath = null;
        moveUnit(startPos, false);
        onReset.accept(this);
    }

    @Override
    public void renderUnitPose(Graphics2D g, UnitPose pose, UnitData data) {
        if (pose == UnitPose.DOWN_RIGHT || pose == UnitPose.FORWARD) {
            super.renderUnitPose(g, UnitPose.FORWARD, data);
            return;
        }
        g.rotate(TutorialMapElement.DEG_30, TILE_SIZE / 2, TILE_SIZE / 2 * SIN_60_DEG);
        super.renderUnitPose(g, pose, data);
    }

    @Override
    public ObjPos getShipRenderOffset() {
        return new ObjPos(data.shipBobbingX(idleAnimX), data.shipBobbingY(idleAnimY));
    }

    @Override
    public ObjPos getRenderPos() {
        if (movePath != null && !movePath.finished()) {
            return movePath.getPos().copy().subtract((TILE_SIZE * SIN_60_DEG + TILE_SIZE) / 2, TILE_SIZE / 2 * SIN_60_DEG).perpendicular();
        }
        return renderPos;
    }

    private final DynamicTextRenderer textHP = createHPText(), textShieldHP = createShieldHPText();

    @Override
    public DynamicTextRenderer getHPText() {
        return textHP;
    }

    @Override
    public DynamicTextRenderer getShieldHPText() {
        return textShieldHP;
    }

    @Override
    public float textXOffset() {
        return TILE_SIZE * 0.35f;
    }

    @Override
    public float textYOffset() {
        return TILE_SIZE * 0.22f;
    }

    @Override
    public FiringData getCurrentFiringData(UnitLike<?> otherUnit) {
        return new FiringData(this, otherUnit, p -> map.getTile(p).type);
    }

    @Override
    public void onDestroyed(UnitLike<?> destroyedBy) {

    }

    @Override
    public void delete() {
        textHP.delete();
        textShieldHP.delete();
        map = null;
    }
}
