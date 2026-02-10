package render;

import foundation.Deletable;
import foundation.Main;
import foundation.MainPanel;
import foundation.math.ObjPos;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class GameRenderer implements RenderRegister<OrderedRenderable>, Deletable {
    public final AffineTransform transform;
    private Supplier<AffineTransform> cameraTransform;
    private final Set<OrderedRenderable>
            qRegister = ConcurrentHashMap.newKeySet(),
            qRemove = ConcurrentHashMap.newKeySet();
    private final TreeMap<RenderOrder, TreeMap<Integer, HashSet<OrderedRenderable>>> renderables = new TreeMap<>();
    private boolean deleted = false;

    public GameRenderer(AffineTransform transform, Supplier<AffineTransform> cameraTransform) {
        this.transform = transform;
        this.cameraTransform = cameraTransform;
    }

    @Override
    public synchronized void register(OrderedRenderable r) {
        if (deleted)
            return;
        qRegister.add(r);
    }

    @Override
    public synchronized void remove(OrderedRenderable r) {
        if (deleted)
            return;
        qRemove.add(r);
    }

    private synchronized void processQueued() {
        qRegister.forEach(r -> {
            if (!renderables.containsKey(r.getRenderOrder()))
                renderables.put(r.getRenderOrder(), new TreeMap<>());
            TreeMap<Integer, HashSet<OrderedRenderable>> order = renderables.get(r.getRenderOrder());
            if (!order.containsKey(r.getZOrder()))
                order.put(r.getZOrder(), new HashSet<>());
            order.get(r.getZOrder()).add(r);
        });
        qRegister.clear();
        qRemove.forEach(r -> {
            renderables.get(r.getRenderOrder()).get(r.getZOrder()).remove(r);
        });
        qRemove.clear();
    }

    @Override
    public synchronized void render(Graphics2D g) {
        processQueued();
        AffineTransform prev = g.getTransform();
        g.transform(transform);
        if (cameraTransform != null)
            g.transform(cameraTransform.get());
        renderables.forEach((_, zSet) -> zSet.forEach((_, set) -> {
            set.forEach(r -> r.render(g));
        }));
        g.setTransform(prev);
    }

    public static Consumer<AffineTransform> renderScaledToBlocks() {
        double s = MainPanel.windowTransform.getScaleX();
        return t -> {
            t.scale(1 / s, 1 / s);
        };
    }

    public static Consumer<AffineTransform> renderScaledToScreen() {
        double s = MainPanel.windowTransform.getScaleX();
        return t -> {
            t.scale(s, s);
        };
    }

    public static float scaleFloatToBlocks(float v) {
        return v / MainPanel.RENDER_WINDOW_SIZE.x * Main.BLOCKS_X;
    }

    public static float scaleFloatToScreen(float v) {
        return v / Main.BLOCKS_X * MainPanel.RENDER_WINDOW_SIZE.x;
    }

    public static void renderOffset(ObjPos pos, Graphics2D g, Runnable render) {
        renderOffset(pos.x, pos.y, g, render);
    }

    public static void renderOffset(float x, float y, Graphics2D g, Runnable render) {
        AffineTransform prev = g.getTransform();
        g.translate(x, y);
        render.run();
        g.setTransform(prev);
    }

    public static void clipOffset(float x, float y, Shape clip, Graphics2D g, Runnable render) {
        Shape prevClip = g.getClip();
        AffineTransform prev = g.getTransform();
        g.translate(x, y);
        g.clip(clip);
        g.setTransform(prev);
        render.run();
        g.setClip(prevClip);
    }

    public static void renderOffsetScaled(ObjPos pos, float scale, Graphics2D g, Runnable render) {
        renderOffsetScaled(pos.x, pos.y, scale, g, render);
    }

    public static void renderOffsetScaled(float x, float y, float scale, Graphics2D g, Runnable render) {
        AffineTransform prev = g.getTransform();
        g.translate(x, y);
        g.scale(scale, scale);
        render.run();
        g.setTransform(prev);
    }

    public static void renderOffsetScaled(float x, float y, float sx, float sy, Graphics2D g, Runnable render) {
        AffineTransform prev = g.getTransform();
        g.translate(x, y);
        g.scale(sx, sy);
        render.run();
        g.setTransform(prev);
    }

    public static void renderScaled(float scale, Graphics2D g, Runnable render) {
        AffineTransform prev = g.getTransform();
        g.scale(scale, scale);
        render.run();
        g.setTransform(prev);
    }

    public static void renderScaledOrigin(float scale, float x, float y, Graphics2D g, Runnable render) {
        AffineTransform prev = g.getTransform();
        g.translate(x, y);
        g.scale(scale, scale);
        g.translate(-x, -y);
        render.run();
        g.setTransform(prev);
    }

    public static void renderScaled(float sx, float sy, Graphics2D g, Runnable render) {
        AffineTransform prev = g.getTransform();
        g.scale(sx, sy);
        render.run();
        g.setTransform(prev);
    }

    public static void renderTransformed(Graphics2D g, Runnable render) {
        AffineTransform prev = g.getTransform();
        render.run();
        g.setTransform(prev);
    }

    @Override
    public synchronized void delete() {
        deleted = true;
        processQueued();
        HashSet<Deletable> deletables = new HashSet<>();
        renderables.forEach((_, zSet) -> zSet.forEach((_, set) -> {
            set.forEach(r -> {
                if (r instanceof Deletable d)
                    deletables.add(d);
            });
            set.clear();
        }));
        cameraTransform = null;
        deletables.forEach(Deletable::delete);
    }
}
