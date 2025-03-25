package level.editor;

import foundation.math.ObjPos;
import render.Renderable;
import render.anim.SineAnimation;
import save.LoadedFromSave;
import unit.UnitPose;
import unit.UnitTeam;
import unit.type.UnitType;

import java.awt.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

import static level.tile.Tile.*;

public final class EditorUnitData implements Renderable, Serializable, LoadedFromSave {
    @Serial
    private static final long serialVersionUID = 1L;
    private final String type;
    private final UnitTeam team;
    public final ObjPos renderPos;

    public transient UnitType unitType;
    private transient SineAnimation idleAnimX, idleAnimY;

    public EditorUnitData(String type, UnitTeam team, ObjPos renderPos) {
        this.type = type;
        this.team = team;
        this.renderPos = renderPos;
        if (type == null)
            return;
        unitType = UnitType.getTypeByName(type);
        idleAnimX = new SineAnimation((5f + (float) Math.random()) * unitType.getBobbingRate(), (float) Math.random() * 360);
        idleAnimY = new SineAnimation((7f + (float) Math.random()) * unitType.getBobbingRate(), (float) Math.random() * 360);
    }

    @Override
    public void render(Graphics2D g) {
        g.translate(-TILE_SIZE / 2, 0);
        g.translate(idleAnimX.normalisedProgress() / 6 * unitType.getBobbingAmount(), idleAnimY.normalisedProgress() / 4 * unitType.getBobbingAmount());
        unitType.tileRenderer(team, UnitPose.FORWARD).render(g);
    }

    @Override
    public void load() {
        if (type == null)
            return;
        unitType = UnitType.getTypeByName(type);
        idleAnimX = new SineAnimation((5f + (float) Math.random()) * unitType.getBobbingRate(), (float) Math.random() * 360);
        idleAnimY = new SineAnimation((7f + (float) Math.random()) * unitType.getBobbingRate(), (float) Math.random() * 360);
    }

    public String type() {
        return type;
    }

    public UnitTeam team() {
        return team;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (EditorUnitData) obj;
        return Objects.equals(this.type, that.type) &&
                Objects.equals(this.team, that.team);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, team);
    }

    @Override
    public String toString() {
        return "EditorUnitData[" +
                "type=" + type + ", " +
                "team=" + team + ']';
    }

}
