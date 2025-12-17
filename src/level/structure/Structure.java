package level.structure;

import level.Level;
import level.tile.Tile;
import network.PacketReceiver;
import network.PacketWriter;
import network.Writable;
import render.anim.unit.AnimType;
import render.anim.unit.ObjectExplosion;
import render.texture.ImageRenderer;
import unit.UnitTeam;

import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Structure implements Writable {
    private ObjectExplosion destroyedExplosion = null;
    public final Point pos;
    public final StructureType type;
    public UnitTeam team;
    public ImageRenderer renderer;
    public boolean canBeCaptured, exploding = false;
    public final StructureStats stats;

    public Structure(Point pos, StructureType type, UnitTeam team) {
        this.pos = pos;
        this.type = type;
        canBeCaptured = type.canBeCapturedByDefault;
        stats = new StructureStats(type);
        setTeam(team);
    }

    public Structure(DataInputStream reader) throws IOException {
        pos = PacketReceiver.readPoint(reader);
        type = PacketReceiver.readEnum(StructureType.class, reader);
        stats = new StructureStats(type);
        boolean neutral = reader.readBoolean();
        setTeam(neutral ? null : PacketReceiver.readEnum(UnitTeam.class, reader));
        canBeCaptured = reader.readBoolean();
    }

    public Structure setTeam(UnitTeam team) {
        this.team = team;
        renderer = type.getImage(team);
        return this;
    }

    public void explode(Tile parent, Level level) {
        if (!exploding) {
            exploding = true;
            level.levelRenderer.animHandler.registerAnim(new ObjectExplosion(parent.renderPosCentered, 0.8f, false, AnimType.TILE_TERRAIN));
            level.levelRenderer.animHandler.tasks.addTask(0.3f, parent::removeStructure);
        }
    }

    public boolean exploding() {
        return exploding;
    }

    @Override
    public void write(DataOutputStream w) throws IOException {
        PacketWriter.writePoint(pos, w);
        PacketWriter.writeEnum(type, w);
        w.writeBoolean(team == null);
        if (team != null)
            PacketWriter.writeEnum(team, w);
        w.writeBoolean(canBeCaptured);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Structure s) {
            return pos.equals(s.pos) && type == s.type && team == s.team;
        }
        return false;
    }

    @Override
    public String toString() {
        return "[" + type + ", " + team + ", " + pos + "]";
    }
}
