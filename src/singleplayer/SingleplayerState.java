package singleplayer;

import foundation.MainPanel;
import foundation.math.ObjPos;
import foundation.math.RandomHandler;
import level.*;
import level.tile.TileType;
import network.NetworkState;
import save.GameSave;
import save.LoadedFromSave;
import unit.TileMapDisplayable;
import unit.UnitMapDataConsumer;
import unit.UnitTeam;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Random;
import java.util.function.BiConsumer;

public class SingleplayerState implements Serializable, LoadedFromSave, TileMapDisplayable {
    @Serial
    private static final long serialVersionUID = 1L;

    public final StartingLoadout startingLoadout;
    public EnemyStartingLoadout enemyLoadout;
    public UnitLoadout playerLoadout;
    public int stars = 0;
    public final GameplaySettings settings;
    public final GameDifficulty difficulty;
    public GameSave save = null;

    public SingleplayerState(StartingLoadout startingLoadout, GameplaySettings settings, GameDifficulty difficulty) {
        this.startingLoadout = startingLoadout;
        playerLoadout = startingLoadout.loadout.copy();
        this.settings = settings;
        this.difficulty = difficulty;
        enemyLoadout = RandomHandler.randomFromArray(EnemyStartingLoadout.values());
    }

    public void startLevel() {
        long seed = new Random().nextLong();
        HashMap<UnitTeam, TeamData> teamData = new HashMap<>();
        TeamData thisTeam = new TeamData(false, PlayerTeam.A);
        if (startingLoadout.card != null)
            thisTeam.cards.add(startingLoadout.card);
        teamData.put(UnitTeam.BLUE, thisTeam);
        teamData.put(UnitTeam.RED, new TeamData(true, PlayerTeam.B));

        HashMap<UnitTeam, UnitLoadout> loadouts = new HashMap<>();
        loadouts.put(UnitTeam.BLUE, playerLoadout);
        loadouts.put(UnitTeam.RED, enemyLoadout.getLoadout(difficulty.enemyStartingPoints));

        MainPanel.startNewLevel(this, () -> new Level(teamData, seed, 20, 12, settings, NetworkState.LOCAL, difficulty.botDifficulty.difficulty)
                .generateDefaultTerrain(
                        TeamSpawner.fromLoadouts(loadouts,
                                RandomHandler.randomFromArray(StructureGenerationType.values()).getPreset(Math::random))
                )
        );
    }

    @Override
    public void load() {
        if (save != null)
            save.load();
    }

    public SingleplayerState copy() {
        SingleplayerState state = new SingleplayerState(startingLoadout, settings, difficulty);
        state.stars = stars;
        state.enemyLoadout = enemyLoadout;
        return state;
    }

    public SingleplayerState save(Level level, String name) {
        save = new GameSave(level, name);
        return this;
    }

    @Override
    public void forEachUnitMapData(UnitMapDataConsumer action) {
        save.forEachUnitMapData(action);
    }

    @Override
    public ObjPos getTileBound() {
        return save.getTileBound();
    }

    @Override
    public void forEachMapStructure(BiConsumer<ObjPos, UnitTeam> action) {
        save.forEachMapStructure(action);
    }

    @Override
    public void forEachMapTileFoW(BiConsumer<ObjPos, Boolean> action) {
        save.forEachMapTileFoW(action);
    }

    @Override
    public int tilesX() {
        return save.tilesX();
    }

    @Override
    public int tilesY() {
        return save.tilesY();
    }

    @Override
    public TileType getTileType(int x, int y) {
        return save.getTileType(x, y);
    }
}
