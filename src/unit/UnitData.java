package unit;

import foundation.MainPanel;
import level.Level;
import network.PacketReceiver;
import network.PacketWriter;
import network.Writable;
import render.anim.timer.SineAnimation;
import save.LoadedFromSave;
import unit.action.Action;
import unit.stats.StatManager;
import unit.stats.UnitStatManager;
import unit.type.UnitType;

import java.awt.*;
import java.io.*;
import java.util.HashSet;
import java.util.function.BiConsumer;

public class UnitData implements Writable, Serializable, LoadedFromSave {
    @Serial
    private static final long serialVersionUID = 1L;
    public transient UnitType type;
    public final String typeName;
    public final UnitTeam team;
    public Point pos;
    public int captureProgress = -1;
    public boolean stealthMode = false, mining = false, visibleInStealthMode = true;
    public int ammo;
    public transient HashSet<Action> performedActions = new HashSet<>();
    public final HashSet<String> performedActionNames = new HashSet<>();
    public float hitPoints, shieldHP, lowestHP;
    public float renderHP, shieldRenderHP;

    public UnitData(UnitType type, UnitTeam team, Point pos) {
        this.type = type;
        typeName = type.getInternalName();
        this.team = team;
        this.pos = pos;
    }

    public void init(StatManager<?> stats) {
        init(stats.maxHP(), stats.maxShieldHP(), stats.ammoCapacity());
    }

    public void init() {
        init(type.hitPoints, type.shieldHP, type.ammoCapacity);
    }

    public void init(float maxHP, float maxShieldHP, int ammoCapacity) {
        hitPoints = maxHP;
        renderHP = hitPoints;
        lowestHP = hitPoints;
        shieldHP = maxShieldHP;
        shieldRenderHP = shieldHP;
        ammo = ammoCapacity;
    }

    public UnitData(DataInputStream reader) throws IOException {
        pos = PacketReceiver.readPoint(reader);
        type = UnitType.read(reader);
        typeName = type.getInternalName();
        team = PacketReceiver.readEnum(UnitTeam.class, reader);
        renderHP = reader.readFloat();
        hitPoints = renderHP;
        shieldRenderHP = reader.readFloat();
        shieldHP = shieldRenderHP;
        lowestHP = reader.readFloat();
        captureProgress = reader.readInt();
        PacketReceiver.readCollection(performedActions, () -> Action.getByName(reader.readUTF()), reader);
        ammo = reader.readInt();
        performedActions.forEach(a -> performedActionNames.add(a.getInternalName()));
        stealthMode = reader.readBoolean();
        mining = reader.readBoolean();
    }

    public UnitData copy() {
        return new UnitData(this);
    }

    private UnitData(UnitData other) {
        type = other.type;
        typeName = other.typeName;
        team = other.team;
        pos = other.pos;
        captureProgress = other.captureProgress;
        stealthMode = other.stealthMode;
        mining = other.mining;
        visibleInStealthMode = other.visibleInStealthMode;
        ammo = other.ammo;
        performedActions = new HashSet<>(other.performedActions);
        performedActionNames.addAll(other.performedActionNames);
        hitPoints = other.hitPoints;
        shieldHP = other.shieldHP;
        lowestHP = other.lowestHP;
        renderHP = other.renderHP;
        shieldRenderHP = other.shieldRenderHP;
    }

    public boolean hasPerformedAction(Action action) {
        return performedActions.contains(action);
    }

    public void addPerformedAction(Action action) {
        performedActions.add(action);
        performedActionNames.add(action.getInternalName());
    }

    public void clearPerformedActions() {
        performedActions.clear();
        performedActionNames.clear();
    }

    public void setShieldHP(float newHP, float max, BiConsumer<Float, Boolean> addDamageUI) {
        addDamageUI.accept(newHP - shieldRenderHP, true);
        newHP = Math.clamp(newHP, 0, max);
        shieldRenderHP = newHP;
        shieldHP = shieldRenderHP;
    }

    public void setHP(float newHP, float max, BiConsumer<Float, Boolean> addDamageUI) {
        addDamageUI.accept(newHP - renderHP, false);
        newHP = Math.clamp(newHP, 0, max);
        hitPoints = newHP;
        renderHP = hitPoints;
        lowestHP = Math.min(lowestHP, hitPoints);
    }

    public Unit getUnit(Level level, boolean requestDataOnFail) {
        Unit u = null;
        for (Unit unit : level.unitSet) {
            if (unit.data.pos.equals(pos)) {
                u = unit;
            }
        }
        if (u == null || u.data.team != team || u.data.type != type) {
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

    public float shipBobbingX(SineAnimation anim) {
        return anim.normalisedProgress() / 6 * type.getBobbingAmount();
    }

    public float shipBobbingY(SineAnimation anim) {
        return anim.normalisedProgress() / 4 * type.getBobbingAmount();
    }

    @Override
    public void write(DataOutputStream w) throws IOException {
        PacketWriter.writePoint(pos, w);
        type.write(w);
        PacketWriter.writeEnum(team, w);
        w.writeFloat(renderHP);
        w.writeFloat(shieldRenderHP);
        w.writeFloat(lowestHP);
        w.writeInt(captureProgress);
        PacketWriter.writeCollection(performedActions, v -> w.writeUTF(v.toString()), w);
        w.writeInt(ammo);
        w.writeBoolean(stealthMode);
        w.writeBoolean(mining);
    }

    @Override
    public void load() {
        performedActions = new HashSet<>();
        performedActionNames.forEach(n -> performedActions.add(Action.getByName(n)));
        type = UnitType.getTypeByName(typeName);
    }
}