package singleplayer;

import foundation.MainPanel;
import foundation.WeightedSelector;
import foundation.math.ObjPos;
import foundation.math.RandomHandler;
import level.*;
import level.tile.TileType;
import network.NetworkState;
import save.GameSave;
import save.LoadedFromSave;
import singleplayer.card.Card;
import singleplayer.card.CardGenerationGroup;
import unit.TileMapDisplayable;
import unit.UnitMapDataConsumer;
import unit.UnitTeam;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
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
    public HashMap<UnitTeam, TeamData> teamData = new HashMap<>();

    public SingleplayerState(StartingLoadout startingLoadout, GameplaySettings settings, GameDifficulty difficulty) {
        this.startingLoadout = startingLoadout;
        playerLoadout = startingLoadout.loadout.copy();
        this.settings = settings;
        this.difficulty = difficulty;
        enemyLoadout = RandomHandler.randomFromArray(EnemyStartingLoadout.values());
    }

    public void startLevel() {
        long seed = new Random().nextLong();
        TeamData thisTeam = new TeamData(false, PlayerTeam.A);
        if (!teamData.containsKey(UnitTeam.BLUE)) { //is this the first level created?
            if (startingLoadout.card != null) {
                thisTeam.addCard(startingLoadout.card);
            }
            teamData.put(UnitTeam.BLUE, thisTeam);
            teamData.put(UnitTeam.RED, new TeamData(true, PlayerTeam.B));
        } else {
            teamData.replaceAll((team, data) -> {
                TeamData newTeam = new TeamData(data.bot, data.playerTeam);
                data.forEachCard(newTeam::addCard);
                return newTeam;
            });
        }

        HashMap<UnitTeam, UnitLoadout> loadouts = new HashMap<>();
        loadouts.put(UnitTeam.BLUE, playerLoadout);
        loadouts.put(UnitTeam.RED, enemyLoadout.getLoadout(difficulty.enemyStartingPoints));

        MainPanel.startNewLevel(this, () -> new Level(null, seed, 28, 18, settings, NetworkState.LOCAL, difficulty.botDifficulty.difficulty)
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
        teamData.values().forEach(TeamData::load);
    }

    public SingleplayerState copy() {
        SingleplayerState state = new SingleplayerState(startingLoadout, settings, difficulty);
        state.stars = stars;
        state.enemyLoadout = enemyLoadout;
        state.playerLoadout = playerLoadout.copy();
        state.teamData = teamData;
        state.teamData.replaceAll((team, data) -> data.copy());
        return state;
    }

    public SingleplayerState save(Level level, String name) {
        SingleplayerState state = copy();
        state.save = new GameSave(level, name);
        return state;
    }

    public Card generateCard(UnitTeam team, WeightedSelector<CardGenerationGroup> generationGroup, int targetCost, int allowedError) {
        return teamData.get(team).attributeHandler.generateCard(teamData.get(team), generationGroup, targetCost, allowedError);
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
