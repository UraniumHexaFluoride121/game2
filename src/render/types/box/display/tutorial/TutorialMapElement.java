package render.types.box.display.tutorial;

import foundation.input.ButtonClickHandler;
import foundation.input.InputType;
import foundation.math.HitBox;
import foundation.math.MathUtil;
import foundation.math.ObjPos;
import level.tile.AbstractTileSelector;
import level.tile.TileSet;
import level.tile.TileType;
import render.GameRenderer;
import render.HorizontalAlign;
import render.Renderable;
import render.UIColourTheme;
import render.anim.sequence.AnimSequence;
import render.anim.sequence.AnimValue;
import render.anim.sequence.KeyframeFunction;
import render.anim.timer.DeltaTime;
import render.anim.timer.LerpAnimation;
import render.anim.timer.SineAnimation;
import render.anim.unit.AnimHandler;
import render.anim.unit.AnimType;
import render.anim.unit.AttackArrow;
import render.level.tile.HexagonBorder;
import render.level.tile.HexagonRenderer;
import render.particle.Particle;
import render.particle.ParticleBehaviour;
import render.particle.ParticleEmitter;
import render.types.box.UIBox;
import render.types.box.UIDisplayBox;
import render.types.box.display.BoxElement;
import unit.Unit;
import unit.UnitTeam;
import unit.action.Action;
import unit.weapon.FiringData;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.util.*;
import java.util.function.Function;

import static level.Level.*;
import static level.tile.Tile.*;

public class TutorialMapElement extends BoxElement {
    private static final UIColourTheme THEME = new UIColourTheme(new Color(128, 128, 128), new Color(25, 22, 22));
    private static final Stroke stroke = Renderable.roundedStroke(0.2f);
    private static final Stroke fowStroke = Renderable.roundedStroke(0.1f);
    private static final Color borderColour = UIColourTheme.setAlpha(BORDER_COLOUR, 0.2f);

    public static final float DEG_30 = (float) Math.toRadians(30);

    public static final float TILE_SIZE_SMALL = 2f, TILE_SIZE_MEDIUM = 3f, TILE_SIZE_LARGE = 4.5f;
    private final AnimHandler animHandler = new AnimHandler();
    private final ParticleEmitter mousePointer = new ParticleEmitter();

    private final Path2D.Float borderPath = new Path2D.Float();
    private final DeltaTime deltaTime = new DeltaTime();
    private final HexagonRenderer hexagonRenderer, selectedRenderer, hoverRenderer;
    private final HorizontalAlign align;
    public final float width, height, size, lifetime;
    public boolean enabled = true;
    private final UIBox box;
    private final UIDisplayBox popupBox;
    private final AnimSequence popupSequence = new AnimSequence().addKeyframe(0, 0, KeyframeFunction.lerp());
    private final HashSet<TutorialMapTile> tiles = new HashSet<>();
    private final HashMap<Integer, HashMap<Integer, TutorialMapTile>> coordinateTiles = new HashMap<>();
    private Particle mouseParticle = null;
    private Point selectedTile = null;
    private final HashMap<Runnable, Float> actions = new HashMap<>();
    private final TutorialActionSelector actionSelector = new TutorialActionSelector();
    private TutorialTilePath tilePath = null;
    private HexagonBorder hexagonBorder = null, fowBorder = null;
    private Color hexagonBorderFill = null;
    private Collection<Point> hexagonBorderPoints = null;
    private Runnable onResetTask = null;
    private ArrayList<TutorialMapText> textRenderers = new ArrayList<>();
    private boolean useTargetSelector = false;

    private final SineAnimation targetAnim = new SineAnimation(2, 90);
    private final LerpAnimation time;

    public final HashSet<TutorialMapUnit> allUnits = new HashSet<>();

    private boolean fow = false;
    private UnitTeam fowTeam = null;

    public TutorialMapElement(float initialMargin, HorizontalAlign align, float width, float height, float tileSize, float lifetime) {
        super(initialMargin);
        this.lifetime = lifetime;
        this.align = align;
        this.width = width;
        this.height = height;
        time = new LerpAnimation(lifetime);
        box = new UIBox(width, height).setColourTheme(THEME).setCorner(1.2f);
        popupBox = new UIDisplayBox(0, 0, width / 2, 1, box -> box.setColourTheme(UIColourTheme.LIGHT_BLUE_BOX), true)
                .addText(0.7f, HorizontalAlign.CENTER, null);
        size = tileSize;
        hexagonRenderer = new HexagonRenderer(TILE_SIZE, false, 0.2f, BORDER_COLOUR).rotate(TILE_SIZE);
        selectedRenderer = new HexagonRenderer(TILE_SIZE, false, 0.25f, SELECTED_COLOUR).rotate(TILE_SIZE);
        hoverRenderer = new HexagonRenderer(false, HIGHLIGHT_STROKE_WIDTH, MOUSE_OVER_COLOUR);
        for (int i = 0; i < 6; i++) {
            hoverRenderer.addSegment(HexagonRenderer.hexagonSegment(TILE_SIZE, TILE_SIZE * 0.08f, false, i - 0.2f, i + 0.2f));
        }
        hoverRenderer.rotate(TILE_SIZE);
    }

    public TutorialMapTile addTile(int x, int y, TileType type) {
        ObjPos renderPos = getRenderPos(x, y);
        AffineTransform t = new AffineTransform();
        t.translate(renderPos.x, renderPos.y);
        Shape transformedShape = t.createTransformedShape(hexagonRenderer.getPath());
        borderPath.append(transformedShape, false);

        TutorialMapTile tile = new TutorialMapTile(x, y, type, transformedShape, this);
        tiles.add(tile);
        if (!coordinateTiles.containsKey(x))
            coordinateTiles.put(x, new HashMap<>());
        coordinateTiles.get(x).put(y, tile);
        return tile;
    }

    public AnimSequence newSequence() {
        return new AnimSequence(1 / lifetime);
    }

    public void addMouseParticle(TutorialMouseKeyframe... keyframes) {
        AnimSequence x = newSequence(), y = newSequence();
        ArrayList<Float> clickTimes = new ArrayList<>();
        ObjPos prev = null;
        for (TutorialMouseKeyframe keyframe : keyframes) {
            ObjPos pos = keyframe.pos == null ? prev : keyframe.pos;
            x.addKeyframe(keyframe.time, pos.x, keyframe.typeX);
            y.addKeyframe(keyframe.time, pos.y, keyframe.typeY);
            if (keyframe.click) {
                float clickTime = keyframe.time + keyframe.clickDelay;
                clickTimes.add(clickTime);
                if (keyframe.onClick != null)
                    actions.put(keyframe.onClick, clickTime);
            }
            if (keyframe.pos != null)
                prev = keyframe.pos;
        }
        addMouseParticle(x, y, clickTimes.toArray(new Float[0]));
    }

    public void addMouseParticle(AnimValue x, AnimValue y, Float... clickTimes) {
        AnimSequence clickScaling = newSequence()
                .addKeyframe(0, 1, KeyframeFunction.lerp());
        for (float clickTime : clickTimes) {
            clickScaling.addKeyframe(clickTime - 0.1f, 1, KeyframeFunction.lerp())
                    .addKeyframe(clickTime, 0.8f, KeyframeFunction.lerp())
                    .addKeyframe(clickTime + 0.1f, 1, KeyframeFunction.lerp());
        }
        clickScaling.endSequence(1, 1);
        Color c = new Color(70, 155, 255, 221);
        actions.put(() -> {
            Particle particle = new Particle(lifetime,
                    ParticleBehaviour.setPosition(x, y),
                    ParticleBehaviour.staticColour(c),
                    ParticleBehaviour.ring(clickScaling.andThen(AnimValue.scaledValue(0.7f)), 0.15f)
            );
            mouseParticle = particle;
            mousePointer.addParticle(particle);
        }, 0f);
    }

    public void addPopup(float time, float duration, String text) {
        popupSequence.addKeyframe(time, 0, KeyframeFunction.lerp())
                .addKeyframe(time + 0.5f, 1, KeyframeFunction.lerp())
                .addKeyframe(time + 0.5f + duration, 1, KeyframeFunction.lerp())
                .addKeyframe(time + 0.5f + duration + 0.5f, 0, KeyframeFunction.lerp());
        actions.put(() -> {
            popupBox.setText(text);
        }, time);
    }

    public void enableFoW(UnitTeam team) {
        fowTeam = team;
        fow = true;
    }

    public void calculateFoW(UnitTeam team) {
        TileSet visible = new TileSet(null);
        allUnits.forEach(u -> {
            if (u.data.team == team) {
                TileSet t = new TileSet(null, tiles.stream().map(m -> m.pos).toList())
                        .m(null, m -> m.tilesInRangeStatic(u.data.pos, p -> getTile(p).type.concealment, u.stats.maxViewRange()));
                t.addAll(TileSet.tilesInRadius(u.data.pos, 1, null));
                visible.addAll(t);
            }
        });
        visible.removeIf(t -> getTile(t) == null);
        tiles.forEach(t -> {
            t.fow = !visible.contains(t.pos);
        });
        fowBorder = new HexagonBorder(visible.stream().map(p -> new Point(p.y, -p.x)).toList(), FOW_TILE_BORDER_COLOUR);
    }

    public void calculateFoW() {
        if (fow)
            calculateFoW(fowTeam);
    }

    public TutorialMapElement setSelectedTile(int x, int y) {
        selectedTile = new Point(x, y);
        TutorialMapTile tile = getTile(x, y);
        if (tile.unit != null)
            actionSelector.setUnit(tile.unit, tile.structure != null && tile.structure.team != tile.unit.data.team);
        return this;
    }

    public TutorialMapTile getTile(int x, int y) {
        if (!coordinateTiles.containsKey(x))
            return null;
        return coordinateTiles.get(x).get(y);
    }

    public TutorialMapTile getTile(Point pos) {
        return getTile(pos.x, pos.y);
    }

    public TutorialMapElement deselectTile() {
        selectedTile = null;
        actionSelector.setEnabled(false);
        return this;
    }

    public TutorialMapElement setHexagonBorder(Color border, Color fill, Collection<Point> points) {
        hexagonBorderPoints = points;
        hexagonBorder = new HexagonBorder(hexagonBorderPoints.stream().map(p -> new Point(p.y, -p.x)).toList(), border);
        hexagonBorderFill = fill;
        return this;
    }

    public TutorialMapElement clearHexagonBorder() {
        hexagonBorder = null;
        return this;
    }

    public TutorialMapElement createTilePath(int fromX, int fromY) {
        tilePath = new TutorialTilePath(new Point(fromX, fromY), hexagonBorderPoints);
        return this;
    }

    public TutorialMapElement setHoverColour(Color c) {
        hoverRenderer.setColor(c);
        return this;
    }

    public TutorialMapElement moveUnit() {
        if (tilePath == null)
            return this;
        getTile(tilePath.getOrigin()).unit.setMovePath(tilePath.getAnimPath(p -> false));
        clearHexagonBorder();
        tilePath = null;
        setHoverColour(MOUSE_OVER_COLOUR);
        return this;
    }

    public TutorialMapElement onResetTask(Runnable onResetTask) {
        this.onResetTask = onResetTask;
        return this;
    }

    public TutorialMapElement addSingleTask(float time, Runnable task) {
        animHandler.tasks.addTask(time, task);
        return this;
    }

    public TutorialMapElement addRepeatedTask(float time, Runnable task) {
        actions.put(task, time);
        return this;
    }

    public TutorialMapElement addText(TutorialMapText text) {
        textRenderers.add(text);
        return this;
    }

    public float getTileCost(Point pos) {
        return getTile(new Point(-pos.y, pos.x)).type.moveCost;
    }

    public TutorialActionSelector actionSelector() {
        return actionSelector;
    }

    public static void transformCoordinateSystem(Graphics2D g) {
        g.translate(TILE_SIZE * SIN_60_DEG, -TILE_SIZE / 2);
        g.rotate(DEG_30 * 3);
    }

    public TutorialMapElement selectMoveAction(int unitX, int unitY) {
        setHexagonBorder(Unit.MOVE_TILE_BORDER_COLOUR, Action.MOVE_ACTION_HIGHLIGHT, tilesWithinDistance(new Point(unitX, unitY),
                getTile(unitX, unitY).unit.data.type.maxMovement))
                .deselectTile().createTilePath(unitX, unitY).setHoverColour(Unit.MOVE_TILE_BORDER_COLOUR);
        return this;
    }

    public TutorialMapElement selectFireAction(int unitX, int unitY) {
        setHexagonBorder(Unit.FIRE_TILE_BORDER_COLOUR, Action.FIRE_ACTION_HIGHLIGHT, tilesWithinFiringRange(new Point(unitX, unitY), true))
                .deselectTile();
        useTargetSelector = true;
        return this;
    }

    public TutorialMapElement capture(int x, int y) {
        return capture(new Point(x, y));
    }

    public TutorialMapElement capture() {
        return capture(selectedTile);
    }

    public TutorialMapElement capture(Point pos) {
        TutorialMapTile tile = getTile(pos);
        if (!tile.capturing)
            tile.startCapture(tile.unit.data.team);
        tile.incrementProgress();
        deselectTile();
        return this;
    }

    public TutorialMapElement attackUnit(int fromX, int fromY, int toX, int toY) {
        clearHexagonBorder();
        useTargetSelector = false;
        TutorialMapUnit u1 = getTile(fromX, fromY).unit, u2 = getTile(toX, toY).unit;
        if (u1 == null || u2 == null) return this;
        FiringData firingData = u1.getFiringResult(u2).firingData();
        AttackArrow.createAttackArrow(getCenteredRenderPos(fromX, fromY), getCenteredRenderPos(toX, toY), u2.data.shieldRenderHP > 0, true, animHandler, animHandler, () -> {
            firingData.realiseEffects(p -> getTile(p).unit);
            u1.postFiringOther(u2, firingData.handler);
        });
        return this;
    }

    public Set<Point> tilesWithinDistance(Point pos, float moveCost) {
        return AbstractTileSelector.tilesInRangeCostMapStatic(pos, tiles.stream().map(t -> t.pos).toList(),
                p -> getTile(p).type.moveCost, moveCost).keySet();
    }

    public TileSet tilesWithinFiringRange(Point pos, boolean enemiesOnly) {
        TutorialMapUnit u = getTile(pos).unit;
        if (u == null)
            return new TileSet(null);
        TileSet points = TileSet.tilesInRadius(pos, u.data.type.minRange, u.data.type.maxRange, null);
        if (enemiesOnly)
            points.removeIf(p -> {
                TutorialMapTile tile = getTile(p);
                if (tile == null)
                    return true;
                TutorialMapUnit enemy = tile.unit;
                return enemy == null || enemy.data.team == u.data.team;
            });
        return points;
    }

    public Optional<Float> getTilePathCost(Function<Point, Float> perTileCost) {
        if (tilePath == null)
            return Optional.empty();
        return Optional.of(tilePath.getPathCost(perTileCost));
    }

    public Optional<Float> getTilePathCost() {
        return getTilePathCost(p -> getTile(-p.y, p.x).type.moveCost);
    }

    @Override
    public float width() {
        return width;
    }

    @Override
    public float height() {
        return height;
    }

    @Override
    public HorizontalAlign align() {
        return align;
    }

    private float xOffset() {
        return switch (align) {
            case LEFT -> 0;
            case CENTER -> -width() / 2;
            case RIGHT -> -width();
        };
    }

    private float yOffset() {
        return -height;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setIsEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public HitBox renderBox() {
        return HitBox.createFromOriginAndSize(xOffset(), yOffset(),
                width(), height());
    }

    @Override
    public void setClickHandler(ButtonClickHandler clickHandler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void render(Graphics2D g) {
        float dT = deltaTime.get();
        while (dT > 0.01f) {
            mousePointer.tick(0.01f);
            animHandler.tick(0.01f);
            dT -= 0.01f;
        }
        mousePointer.tick(dT);
        animHandler.tick(dT);
        GameRenderer.renderOffset(xOffset(), yOffset(), g, () -> {
            box.render(g);
            GameRenderer.renderOffsetScaled(width() / 2, height() / 2, size / TILE_SIZE, g, () -> {
                tiles.forEach(map -> map.renderTerrain(g));
                g.setStroke(fow ? fowStroke : stroke);
                g.setColor(borderColour);
                g.draw(borderPath);
                if (fow && fowBorder != null) {
                    GameRenderer.renderTransformed(g, () -> {
                        transformCoordinateSystem(g);
                        fowBorder.render(g);
                    });
                }
                if (tilePath != null)
                    tilePath.render(g);
                animHandler.render(g, AnimType.BELOW_UNIT);
                tiles.forEach(t -> t.render(g));
                tiles.forEach(t -> t.renderCaptureBar(g));
                animHandler.render(g, AnimType.ABOVE_UNIT);
                TutorialMapTile hoverTile = null;
                if (mouseParticle != null) {
                    ObjPos pos = mouseParticle.offset;
                    TutorialMapTile tile = hexagon(pos);
                    if (tile != null) {
                        if (hexagonBorderPoints == null || hexagonBorderPoints.contains(tile.pos)) {
                            hoverTile = tile;
                            if (!useTargetSelector)
                                renderTile(g, hoverRenderer, TILE_SIZE, tile.renderPos);
                        }
                        if (tilePath != null)
                            tilePath.setShortestPath(tile.pos, this::getTileCost);
                    }
                    actionSelector.buttonPressed(pos, actionSelector.posInside(pos, InputType.MOUSE_OVER), false, InputType.MOUSE_OVER);
                }
                if (hexagonBorder != null) {
                    GameRenderer.renderTransformed(g, () -> {
                        transformCoordinateSystem(g);
                        hexagonBorder.renderFill(g, hexagonBorderFill);
                        hexagonBorder.render(g);
                    });
                }
                if (hoverTile != null && useTargetSelector) {
                    Unit.renderTarget(g, targetAnim, hoverTile.centeredRenderPos);
                }
                if (selectedTile != null) {
                    ObjPos pos = getRenderPos(selectedTile);
                    renderTile(g, selectedRenderer, TILE_SIZE, pos);
                }
                GameRenderer.renderOffset(actionSelector.getRenderPos(), g, () -> {
                    actionSelector.render(g);
                });
                mousePointer.render(g);
            });
            textRenderers.forEach(t -> t.render(g));
            if (popupSequence.keyframeCount() != 1 &&
                    !MathUtil.equal(popupSequence.getValue(time.timeElapsed()), 0, 0.01f)) {
                Shape clip = g.getClip();
                GameRenderer.renderScaled(1f / SCALING, g, () -> {
                    g.clip(box.getShape());
                });
                GameRenderer.renderOffset(width / 2 - (popupBox.width / 2), MathUtil.lerp(-1.2f, 1, popupSequence.getValue(time.timeElapsed())), g, () -> {
                    popupBox.render(g);
                });
                g.setClip(clip);
            }
        });
    }

    public void finalise() {
        actions.put(this::reset, lifetime);
        reset();
    }

    private void reset() {
        useTargetSelector = false;
        targetAnim.startTimer();
        time.startTimer();
        setHoverColour(MOUSE_OVER_COLOUR);
        selectedTile = null;
        actionSelector.reset();
        hexagonBorder = null;
        fowBorder = null;
        tilePath = null;
        actions.forEach((r, t) -> animHandler.tasks.addTask(t, r));
        allUnits.forEach(TutorialMapUnit::reset);
        textRenderers.forEach(TutorialMapText::reset);
        tiles.forEach(TutorialMapTile::reset);
        if (onResetTask != null)
            onResetTask.run();
        if (fow)
            calculateFoW(fowTeam);
    }

    @Override
    public boolean maxWidth() {
        return false;
    }

    @Override
    public void delete() {
        super.delete();
        tiles.forEach(TutorialMapTile::delete);
        tiles.clear();
        coordinateTiles.clear();
        animHandler.delete();
        mousePointer.delete();
        mouseParticle = null;
        actions.clear();
        actionSelector.delete();
        allUnits.forEach(TutorialMapUnit::delete);
        allUnits.clear();
        onResetTask = null;
        textRenderers.forEach(TutorialMapText::delete);
        textRenderers.clear();
    }

    public TutorialMapTile hexagon(ObjPos pos) {
        for (TutorialMapTile tile : tiles) {
            if (tile.hexagonShape.contains(pos.x, pos.y))
                return tile;
        }
        return null;
    }

    public static ObjPos getRenderPos(Point pos) {
        return getRenderPos(pos.x, pos.y);
    }

    public static ObjPos getRenderPos(int x, int y) {
        return getCenteredRenderPos(x, y).addY(-SIN_60_DEG * TILE_SIZE / 2);
    }

    public static ObjPos getCenteredRenderPos(Point pos) {
        return getCenteredRenderPos(pos.x, pos.y);
    }

    public static ObjPos getCenteredRenderPos(int x, int y) {
        return new ObjPos(x * SIN_60_DEG * TILE_SIZE + ((y % 2) != 0 ? SIN_60_DEG * TILE_SIZE / 2 : 0), y * TILE_SIZE * 1.5f / 2);
    }
}
