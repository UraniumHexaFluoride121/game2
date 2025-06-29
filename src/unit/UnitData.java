package unit;

import foundation.MainPanel;
import level.Level;
import network.PacketReceiver;
import network.PacketWriter;
import network.Writable;
import save.LoadedFromSave;
import unit.action.Action;
import unit.type.UnitType;
import unit.weapon.FiringData;
import unit.weapon.WeaponInstance;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;

public class UnitData implements Writable, Serializable, LoadedFromSave {
    @Serial
    private static final long serialVersionUID = 1L;
    public Point pos;
    public transient UnitType type;
    public final String typeName;
    public final UnitTeam team;
    public float hitPoints, shieldHP, lowestHP;
    public final int captureProgress;
    public transient HashSet<Action> performedActions = new HashSet<>();
    public final HashSet<String> performedActionNames = new HashSet<>();
    public int weaponAmmo = -1;
    public final boolean stealthMode, mining;

    public UnitData(Unit unit) {
        pos = unit.getPos();
        type = unit.type;
        typeName = type.getInternalName();
        team = unit.team;
        hitPoints = unit.firingTempHP;
        shieldHP = unit.shieldFiringTempHP;
        lowestHP = unit.lowestHP;
        captureProgress = unit.getCaptureProgress();
        performedActions.addAll(unit.getPerformedActions());
        performedActions.forEach(a -> performedActionNames.add(a.getInternalName()));
        if (unit.stats.consumesAmmo())
            weaponAmmo = unit.ammo;
        stealthMode = unit.stealthMode;
        mining = unit.mining;
    }

    public UnitData(DataInputStream reader) throws IOException {
        pos = PacketReceiver.readPoint(reader);
        type = UnitType.read(reader);
        typeName = type.getInternalName();
        team = PacketReceiver.readEnum(UnitTeam.class, reader);
        hitPoints = reader.readFloat();
        shieldHP = reader.readFloat();
        lowestHP = reader.readFloat();
        captureProgress = reader.readInt();
        PacketReceiver.readCollection(performedActions, () -> Action.getByName(reader.readUTF()), reader);
        weaponAmmo = reader.readInt();
        performedActions.forEach(a -> performedActionNames.add(a.getInternalName()));
        stealthMode = reader.readBoolean();
        mining = reader.readBoolean();
    }

    @Override
    public void write(DataOutputStream w) throws IOException {
        PacketWriter.writePoint(pos, w);
        type.write(w);
        PacketWriter.writeEnum(team, w);
        w.writeFloat(hitPoints);
        w.writeFloat(shieldHP);
        w.writeFloat(lowestHP);
        w.writeInt(captureProgress);
        PacketWriter.writeCollection(performedActions, v -> w.writeUTF(v.toString()), w);
        w.writeInt(weaponAmmo);
        w.writeBoolean(stealthMode);
        w.writeBoolean(mining);
    }

    public Unit getUnit(Level level, boolean requestDataOnFail) {
        Unit u = null;
        for (Unit unit : level.unitSet) {
            if (unit.pos.equals(pos)) {
                u = unit;
            }
        }
        if (u == null || u.team != team || u.type != type) {
            u = new Unit(type, team, pos, level);
            level.forceAddUnit(u);
            if (requestDataOnFail)
                MainPanel.addTaskAfterAnimBlock(() -> {
                    MainPanel.client.requestLevelData();
                });
        }
        u.updateFromData(this);
        return u;
    }

    @Override
    public void load() {
        performedActions = new HashSet<>();
        performedActionNames.forEach(n -> performedActions.add(Action.getByName(n)));
        type = UnitType.getTypeByName(typeName);
    }
}
