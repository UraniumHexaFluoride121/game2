package level;

import foundation.math.MathUtil;
import foundation.math.ObjPos;
import foundation.math.RandomType;
import level.tile.TileModifier;
import level.tile.TileSet;
import level.tile.TileType;
import unit.Unit;
import unit.UnitTeam;
import unit.type.UnitType;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class TeamSpawner {
    private static final ObjPos[][] TWO_TEAMS = new ObjPos[][]{
            new ObjPos[]{
                    new ObjPos(0.15f, 0.5f),
                    new ObjPos(0.85f, 0.5f)
            },
            new ObjPos[]{
                    new ObjPos(0.15f, 0.15f),
                    new ObjPos(0.85f, 0.85f)
            },
            new ObjPos[]{
                    new ObjPos(0.15f, 0.85f),
                    new ObjPos(0.85f, 0.15f)
            }
    };
    private static final ObjPos[][] THREE_TEAMS = new ObjPos[][]{
            new ObjPos[]{
                    new ObjPos(0.25f, 0.2f),
                    new ObjPos(0.75f, 0.2f),
                    new ObjPos(0.5f, 0.8f)
            },
            new ObjPos[]{
                    new ObjPos(0.25f, 0.8f),
                    new ObjPos(0.75f, 0.8f),
                    new ObjPos(0.5f, 0.2f)
            },
            new ObjPos[]{
                    new ObjPos(0.23f, 0.15f),
                    new ObjPos(0.78f, 0.5f),
                    new ObjPos(0.23f, 0.85f)
            },
            new ObjPos[]{
                    new ObjPos(0.78f, 0.15f),
                    new ObjPos(0.23f, 0.5f),
                    new ObjPos(0.78f, 0.85f)
            }
    };
    private static final ObjPos[][] FOUR_TEAMS = new ObjPos[][]{
            new ObjPos[]{
                    new ObjPos(0.2f, 0.5f),
                    new ObjPos(0.8f, 0.5f),
                    new ObjPos(0.5f, 0.85f),
                    new ObjPos(0.5f, 0.15f)
            },
            new ObjPos[]{
                    new ObjPos(0.15f, 0.15f),
                    new ObjPos(0.15f, 0.85f),
                    new ObjPos(0.85f, 0.15f),
                    new ObjPos(0.85f, 0.85f)
            }
    };

    private HashMap<UnitTeam, ArrayList<UnitType>> units;

    public TeamSpawner(HashMap<UnitTeam, ArrayList<UnitType>> units) {
        this.units = units;
    }

    public TeamSpawner() {
        units = new HashMap<>();
    }

    public boolean generateTeams(Level level) {
        ObjPos[] teamPositions = level.random.randomFromArray(switch (level.playerCount()) {
            case 3 -> THREE_TEAMS;
            case 4 -> FOUR_TEAMS;
            default -> TWO_TEAMS;
        }, RandomType.STRUCTURE_SPAWNING);

        Point[] basePositions = new Point[level.playerCount()];
        for (int i = 0; i < level.playerCount(); i++) {
            basePositions[i] = teamPositions[i].copy().multiply(level.tilesX, level.tilesY).add(
                            MathUtil.randFloatBetween(-0.1f, 0.1f, level.random.getDoubleSupplier(RandomType.STRUCTURE_SPAWNING)),
                            MathUtil.randFloatBetween(-0.1f, 0.1f, level.random.getDoubleSupplier(RandomType.STRUCTURE_SPAWNING)))
                    .roundToPoint();
        }
        level.random.randomise(basePositions, RandomType.STRUCTURE_SPAWNING);
        HashSet<Point> connectedPoints = new HashSet<>();
        HashMap<UnitTeam, Point> basePositionsByTeam = new HashMap<>();
        HashMap<UnitTeam, ArrayList<Point>> unitPositions = new HashMap<>();
        HashSet<Point> allSpawnPoints = new HashSet<>();
        for (int i = 0; i < basePositions.length; i++) {
            UnitTeam team = UnitTeam.ORDERED_TEAMS[i];
            basePositionsByTeam.put(team, basePositions[i]);
            connectedPoints.add(basePositions[i]);
            int radius = 1;
            TileSet tiles;
            int prevTileSize = -1;
            do {
                tiles = TileSet.tilesInRadiusDeterministic(basePositions[i], radius, level);
                tiles.removeIf(t -> level.getTile(t).type == TileType.ASTEROIDS || allSpawnPoints.contains(t));
                radius++;
                if (prevTileSize == tiles.size())
                    return false;
                prevTileSize = tiles.size();
            } while (tiles.size() * 0.7f < units.get(team).size());
            ArrayList<Point> spawnPoints = level.random.randomSelection(tiles.stream().toList(), units.get(team).size(), RandomType.TEAM_SPAWNING);
            connectedPoints.addAll(spawnPoints);
            unitPositions.put(team, spawnPoints);
            allSpawnPoints.addAll(spawnPoints);
        }
        if (level.getTile(basePositions[0]).type == TileType.ASTEROIDS)
            return false;
        TileSet dfsResult = TileSet.all(level).m(level, t -> t.tileFilter(TileModifier.tileOfType(TileType.ASTEROIDS).negate()).tilesInRange(basePositions[0], _ -> 0f, 1));
        for (Point point : connectedPoints) {
            if (!dfsResult.contains(point))
                return false;
        }
        for (int i = 0; i < basePositions.length; i++) {
            UnitTeam team = UnitTeam.ORDERED_TEAMS[i];
            ArrayList<Point> spawnPoints = level.random.randomise(unitPositions.get(team), RandomType.TEAM_SPAWNING);
            for (int j = 0; j < spawnPoints.size(); j++) {
                level.addUnit(new Unit(units.get(team).get(j), team, spawnPoints.get(j), level));
            }
            level.setBasePositions(basePositionsByTeam);
        }
        return true;
    }

    public TeamSpawner setUnits(HashMap<UnitTeam, ArrayList<UnitType>> units) {
        this.units = units;
        return this;
    }
}
