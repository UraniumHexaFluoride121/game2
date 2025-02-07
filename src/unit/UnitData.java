package unit;

import foundation.MainPanel;
import level.Level;
import network.PacketReceiver;
import network.PacketWriter;
import network.Writable;
import unit.action.Action;

import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

public class UnitData implements Writable {
    public final Point pos;
    public final UnitType type;
    public final UnitTeam team;
    public final float hitPoints;
    public final HashSet<Action> performedActions = new HashSet<>();
    public final ArrayList<Integer> weaponAmmo = new ArrayList<>();

    public UnitData(Unit unit) {
        pos = unit.getPos();
        type = unit.type;
        team = unit.team;
        hitPoints = unit.hitPoints;
        performedActions.addAll(unit.performedActions);
        unit.weapons.forEach(w -> {
            weaponAmmo.add(w.ammo);
        });
    }

    public UnitData(DataInputStream reader) throws IOException {
        pos = PacketReceiver.readPoint(reader);
        type = PacketReceiver.readEnum(UnitType.class, reader);
        team = PacketReceiver.readEnum(UnitTeam.class, reader);
        hitPoints = reader.readFloat();
        PacketReceiver.readCollection(performedActions, () -> Action.getByName(reader.readUTF()), reader);
        PacketReceiver.readCollection(weaponAmmo, reader::readInt, reader);
    }

    @Override
    public void write(DataOutputStream w) throws IOException {
        PacketWriter.writePoint(pos, w);
        PacketWriter.writeEnum(type, w);
        PacketWriter.writeEnum(team, w);
        w.writeFloat(hitPoints);
        PacketWriter.writeCollection(performedActions, v -> w.writeUTF(v.toString()), w);
        PacketWriter.writeCollection(weaponAmmo, w::writeInt, w);
    }

    public Unit getUnit(Level level) {
        Unit u = null;
        for (Unit unit : level.unitSet) {
            if (unit.pos.equals(pos)) {
                u = unit;
            }
        }
        if (u == null || u.team != team || u.type != type) {
            u = new Unit(type, team, pos, level);
            level.forceAddUnit(u);
            MainPanel.addTaskAfterAnimBlock(() -> {
                MainPanel.client.requestLevelData();
            });
        }
        u.updateFromData(this);
        return u;
    }
}
