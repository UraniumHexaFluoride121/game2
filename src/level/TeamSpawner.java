package level;

import foundation.math.MathUtil;
import foundation.math.ObjPos;
import foundation.math.RandomType;
import level.structure.StructureType;
import level.tile.TileModifier;
import level.tile.TileSet;
import level.tile.TileType;
import mainScreen.StructureGenerationPreset;
import singleplayer.UnitLoadout;
import unit.Unit;
import unit.UnitTeam;
import unit.type.UnitType;

import java.awt.*;
import java.util.*;
import java.util.List;

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
    private StructureGenerationPreset structures;

    public static TeamSpawner fromLoadouts(HashMap<UnitTeam, UnitLoadout> loadouts, StructureGenerationPreset structures) {
        HashMap<UnitTeam, ArrayList<UnitType>> units = new HashMap<>();
        for (Map.Entry<UnitTeam, UnitLoadout> entry : loadouts.entrySet()) {
            units.put(entry.getKey(), new ArrayList<>());
            for (UnitLoadout.UnitElement unitCount : entry.getValue().unitCounts) {
                for (int i = 0; i < unitCount.count; i++) {
                    units.get(entry.getKey()).add(unitCount.type);
                }
            }
        }
        return new TeamSpawner(units, structures);
    }

    public TeamSpawner(HashMap<UnitTeam, ArrayList<UnitType>> units, StructureGenerationPreset structures) {
        this.units = units;
        this.structures = structures;
    }

    public boolean generateTeams(Level level) {
        ObjPos[] teamPositions = level.random.randomFromArray(switch (level.playerCount()) {
            case 3 -> THREE_TEAMS;
            case 4 -> FOUR_TEAMS;
            default -> TWO_TEAMS;
        }, RandomType.BASE_SPAWNING);

        Point[] basePositions = new Point[level.playerCount()];
        for (int i = 0; i < level.playerCount(); i++) {
            basePositions[i] = teamPositions[i].copy().multiply(level.tilesX, level.tilesY).add(
                            MathUtil.randFloatBetween(-0.1f, 0.1f, level.random.getDoubleSupplier(RandomType.BASE_SPAWNING)),
                            MathUtil.randFloatBetween(-0.1f, 0.1f, level.random.getDoubleSupplier(RandomType.BASE_SPAWNING)))
                    .roundToPoint();
        }
        level.random.randomise(basePositions, RandomType.BASE_SPAWNING);
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
        TileSet allStructures = new TileSet(level.tilesX, level.tilesY);
        allStructures.addAll(List.of(basePositions));
        int c = structures.capturedCount();
        for (int i = 0; i < basePositions.length; i++) {
            UnitTeam team = UnitTeam.ORDERED_TEAMS[i];
            int size = 1;
            while (true) {
                TileSet positions = TileSet.tilesInRadius(basePositions[i], size, level);
                positions.removeAll(allStructures);
                if (positions.size() * 0.7f > c) {
                    TileSet expandedPositions = TileSet.tilesInRadius(basePositions[i], size + 2, level);
                    expandedPositions.removeAll(allStructures);
                    ArrayList<Point> spawnPoints = level.random.randomSelection(expandedPositions.stream().toList(), c, RandomType.STRUCTURE_SPAWNING);
                    ArrayList<StructureType> types = structures.getList(false);
                    for (int j = 0; j < spawnPoints.size(); j++) {
                        level.getTile(spawnPoints.get(j)).setStructure(types.get(j), team);
                        allStructures.add(spawnPoints.get(j));
                    }
                    break;
                }
                size++;
            }
        }
        ObjPos middle = new ObjPos(level.tilesX / 2f, level.tilesY / 2f);
        structures.neutralMap.forEach((type, count) -> {
            boolean b = count / basePositions.length > 1;
            int ringCount = b ? count / (basePositions.length * 2) : count / basePositions.length;
            int centerCount = b ? count % (basePositions.length * 2) : count % basePositions.length;
            float angle = (float) (level.random.generateFloat(RandomType.STRUCTURE_SPAWNING) * Math.PI * 2);
            for (int i = 0; i < (b ? 2 * basePositions.length : basePositions.length); i++) {
                Point p = middle.copy().add(new ObjPos(MathUtil.randFloatBetween(0.25f, 0.65f, level.random.getDoubleSupplier(RandomType.STRUCTURE_SPAWNING))).rotate((float) (angle + (Math.PI * 2 / basePositions.length / (b ? 2 : 1)) * i)).multiply(level.tilesX / 2f, level.tilesY / 2f)).roundToPoint();
                int size = 1;
                while (true) {
                    TileSet positions = TileSet.tilesInRadius(p, size, level);
                    positions.removeAll(allStructures);
                    if (positions.size() * 0.25f > ringCount) {
                        ArrayList<Point> spawnPoints = level.random.randomSelection(positions.stream().toList(), ringCount, RandomType.STRUCTURE_SPAWNING);
                        for (int j = 0; j < ringCount; j++) {
                            level.getTile(spawnPoints.get(j)).setStructure(type, null);
                            allStructures.add(spawnPoints.get(j));
                        }
                        break;
                    }
                    size++;
                }
            }
            int size = 1;
            while (true) {
                TileSet positions = TileSet.tilesInRadius(middle.roundToPoint(), size, level);
                positions.removeAll(allStructures);
                if (positions.size() * 0.25f > centerCount) {
                    ArrayList<Point> spawnPoints = level.random.randomSelection(positions.stream().toList(), centerCount, RandomType.STRUCTURE_SPAWNING);
                    for (int j = 0; j < centerCount; j++) {
                        level.getTile(spawnPoints.get(j)).setStructure(type, null);
                        allStructures.add(spawnPoints.get(j));
                    }
                    break;
                }
                size++;
            }
        });
        for (int i = 0; i < basePositions.length; i++) {
            UnitTeam team = UnitTeam.ORDERED_TEAMS[i];
            ArrayList<Point> spawnPoints = level.random.randomise(unitPositions.get(team), RandomType.TEAM_SPAWNING);
            for (int j = 0; j < spawnPoints.size(); j++) {
                level.addUnit(new Unit(units.get(team).get(j), team, spawnPoints.get(j), level), true);
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
