package level.structure;

import foundation.math.ObjPos;
import level.tile.Tile;
import network.PacketReceiver;
import network.PacketWriter;
import network.Writable;
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
    public boolean canBeCaptured;

    public Structure(Point pos, StructureType type, UnitTeam team) {
        this.pos = pos;
        this.type = type;
        canBeCaptured = type.canBeCapturedByDefault;
        setTeam(team);
    }

    public Structure(DataInputStream reader) throws IOException {
        pos = PacketReceiver.readPoint(reader);
        type = PacketReceiver.readEnum(StructureType.class, reader);
        setTeam(PacketReceiver.readEnum(UnitTeam.class, reader));
        canBeCaptured = reader.readBoolean();
    }

    public Structure setTeam(UnitTeam team) {
        this.team = team;
        renderer = type.getRenderer(team);
        return this;
    }

    public void renderExplosion(Graphics2D g, Tile parent) {
        if (destroyedExplosion == null)
            return;
        destroyedExplosion.render(g);
        if (destroyedExplosion.finished())
            parent.removeStructure();
    }

    public void explode() {
        if (destroyedExplosion == null)
            destroyedExplosion = new ObjectExplosion(new ObjPos(), 0.2f);
    }

    public boolean exploding() {
        return destroyedExplosion != null;
    }

    @Override
    public void write(DataOutputStream w) throws IOException {
        PacketWriter.writePoint(pos, w);
        PacketWriter.writeEnum(type, w);
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
