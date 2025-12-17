package foundation.input;

import foundation.Deletable;
import foundation.Main;
import foundation.MainPanel;
import foundation.math.ObjPos;
import render.RenderOrder;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

public class ButtonRegister implements IButtonRegister, Deletable {
    private final Set<RegisteredButtonInputReceiver>
            qRegister = ConcurrentHashMap.newKeySet(),
            qRemove = ConcurrentHashMap.newKeySet();
    private final TreeMap<RenderOrder, TreeMap<Integer, HashSet<RegisteredButtonInputReceiver>>> buttons = new TreeMap<>(Comparator.reverseOrder());
    private boolean deleted = false;

    public ButtonRegister() {
    }

    @Override
    public synchronized void register(RegisteredButtonInputReceiver r) {
        if (deleted)
            return;
        qRegister.add(r);
    }

    @Override
    public synchronized void remove(RegisteredButtonInputReceiver r) {
        if (deleted)
            return;
        qRemove.add(r);
    }

    public void input(boolean pressed, InputType type, Function<Point, ObjPos> mousePositionTransformer) {
        Point p = Main.window.getMousePosition();
        if (p != null) {
            acceptInput(mousePositionTransformer.apply(p), type, pressed, false);
        }
    }

    public void input(boolean pressed, InputType type) {
        Point p = Main.window.getMousePosition();
        if (p != null) {
            ObjPos pos = new ObjPos(p);
            pos.subtract(MainPanel.INSETS_OFFSET);
            pos.scaleToBlocks().flipY().addY(MainPanel.BLOCK_DIMENSIONS.y); //Scale to world block grid
            acceptInput(pos, type, pressed, false);
        }
    }

    private synchronized void processQueued() {
        qRegister.forEach(b -> {
            if (!buttons.containsKey(b.getButtonOrder()))
                buttons.put(b.getButtonOrder(), new TreeMap<>(Comparator.reverseOrder()));
            TreeMap<Integer, HashSet<RegisteredButtonInputReceiver>> order = buttons.get(b.getButtonOrder());
            if (!order.containsKey(b.getZOrder()))
                order.put(b.getZOrder(), new HashSet<>());
            order.get(b.getZOrder()).add(b);
        });
        qRegister.clear();
        qRemove.forEach(b -> {
            buttons.get(b.getButtonOrder()).get(b.getZOrder()).remove(b);
        });
        qRemove.clear();
    }

    @Override
    public synchronized boolean acceptInput(ObjPos pos, InputType type, boolean pressed, boolean alreadyBlocked) {
        processQueued();
        AtomicBoolean blocked = new AtomicBoolean(alreadyBlocked);
        buttons.forEach((_, zSet) -> zSet.forEach((_, set) -> set.forEach(b -> {
            boolean inside = b.posInside(pos, type);
            if (pressed)
                b.buttonPressed(pos, inside, blocked.get(), type);
            else
                b.buttonReleased(pos, inside, blocked.get(), type);
            if (inside && b.blocking(type))
                blocked.set(true);
        })));
        return blocked.get();
    }

    public static void renderOffset(ObjPos pos, Graphics2D g, Consumer<Graphics2D> render) {
        renderOffset(pos.x, pos.y, g, render);
    }

    public static void renderOffset(float x, float y, Graphics2D g, Consumer<Graphics2D> render) {
        AffineTransform prev = g.getTransform();
        g.translate(x, y);
        render.accept(g);
        g.setTransform(prev);
    }

    @Override
    public synchronized void delete() {
        deleted = true;
        processQueued();
        HashSet<Deletable> deletables = new HashSet<>();
        buttons.forEach((_, zSet) -> zSet.forEach((_, set) -> {
            set.forEach(r -> {
                if (r instanceof Deletable d)
                    deletables.add(d);
            });
            set.clear();
        }));
        buttons.clear();
        deletables.forEach(Deletable::delete);
    }
}
