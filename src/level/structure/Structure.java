package level.structure;

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
    public final Point pos;
    public final StructureType type;
    public UnitTeam team;
    public ImageRenderer renderer;

    public Structure(Point pos, StructureType type, UnitTeam team) {
        this.pos = pos;
        this.type = type;
        setTeam(team);
    }

    public Structure(DataInputStream reader) throws IOException {
        pos = PacketReceiver.readPoint(reader);
        type = PacketReceiver.readEnum(StructureType.class, reader);
        setTeam(PacketReceiver.readEnum(UnitTeam.class, reader));
    }

    public Structure setTeam(UnitTeam team) {
        this.team = team;
        renderer = type.getRenderer(team);
        return this;
    }

    @Override
    public void write(DataOutputStream w) throws IOException {
        PacketWriter.writePoint(pos, w);
        PacketWriter.writeEnum(type, w);
        PacketWriter.writeEnum(team, w);
    }

    @Override
    public String toString() {
        return "[" + type + ", " + team + ", " + pos + "]";
    }
}
