package foundation;

import foundation.input.InputReceiver;
import foundation.input.InputType;
import foundation.math.ObjPos;
import foundation.tick.RegisteredTickable;
import foundation.tick.TickOrder;
import level.Level;
import mainScreen.TitleScreen;
import render.anim.LerpAnimation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class MainPanel extends JFrame implements KeyListener, MouseListener, MouseWheelListener, RegisteredTickable {
    public static AffineTransform windowTransform = new AffineTransform();

    public static ObjPos DEVICE_WINDOW_SIZE; //the physical screen size, in pixels
    public static ObjPos RENDER_WINDOW_SIZE; //the size of the render box, in pixels
    public static ObjPos BLOCK_DIMENSIONS; //the size of the render box, in blocks
    public static ObjPos INSETS_OFFSET = new ObjPos(); //The offset added to account for frame borders in windowed mode

    public static Level activeLevel;
    public static TitleScreen titleScreen;

    public static InputReceiver activeInputReceiver = null;

    public static LerpAnimation fadeScreen = new LerpAnimation(0.5f);

    public static boolean controlHeld = false;

    public void init() {
        fadeScreen.setReversed(true);
        fadeScreen.finish();
        titleScreen = new TitleScreen();
        titleScreen.init();
        activeInputReceiver = titleScreen;
        //activeLevel = new Level(Level.allSeparate(), 1);
        //activeInputReceiver = activeLevel.levelRenderer;
        registerTickable();
    }

    public static void startNewLevel(Supplier<Level> levelCreator) {
        Level.EXECUTOR.submit(() -> {
            fadeScreen.setReversed(false);
            activeInputReceiver = null;
            Level level = levelCreator.get();
            level.init();
            activeLevel = level;
            activeInputReceiver = level.levelRenderer;
            while (!level.rendered) {
                try {
                    TimeUnit.MILLISECONDS.sleep(3);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            fadeScreen.setReversed(true);
        });
    }

    @Override
    public void paintComponents(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        if (activeLevel != null) {
            activeLevel.render(g2d);
        } else {
            titleScreen.render(g2d);
        }
        g2d.setColor(new Color(0, 0, 0, fadeScreen.normalisedProgress()));
        g2d.fillRect(0, 0, RENDER_WINDOW_SIZE.xInt(), RENDER_WINDOW_SIZE.yInt());
    }

    private static final Vector<Runnable> tasks = new Vector<>();

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public synchronized void keyPressed(KeyEvent e) {
        tasks.add(() -> {
            if (e.getKeyCode() == KeyEvent.VK_CONTROL)
                controlHeld = true;
            if (activeInputReceiver != null)
                activeInputReceiver.acceptPressed(InputType.getInputType(e));
        });
    }

    @Override
    public synchronized void keyReleased(KeyEvent e) {
        tasks.add(() -> {
            if (e.getKeyCode() == KeyEvent.VK_CONTROL)
                controlHeld = false;
            if (activeInputReceiver != null)
                activeInputReceiver.acceptReleased(InputType.getInputType(e));
        });
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public synchronized void mousePressed(MouseEvent e) {
        tasks.add(() -> {
            if (activeInputReceiver != null)
                activeInputReceiver.acceptPressed(InputType.getInputType(e));
        });
    }

    @Override
    public synchronized void mouseReleased(MouseEvent e) {
        tasks.add(() -> {
            if (activeInputReceiver != null)
                activeInputReceiver.acceptReleased(InputType.getInputType(e));
        });
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        tasks.add(() -> {
            for (int i = 0; i < e.getScrollAmount(); i++) {
                if (activeInputReceiver != null)
                    activeInputReceiver.acceptPressed(InputType.getInputType(e));
            }
        });
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public TickOrder getTickOrder() {
        return TickOrder.MAIN;
    }

    @Override
    public synchronized void tick(float deltaTime) {
        tasks.forEach(Runnable::run);
        tasks.clear();
    }

    @Override
    public void delete() {
        removeTickable();
    }
}
