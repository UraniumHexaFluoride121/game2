package foundation;

import foundation.input.InputReceiver;
import foundation.input.InputType;
import foundation.math.ObjPos;
import foundation.tick.RegisteredTickable;
import foundation.tick.TickOrder;
import level.AbstractLevel;
import level.Level;
import level.structure.StructureType;
import level.tile.TileType;
import level.tutorial.TutorialLevel;
import level.tutorial.TutorialManager;
import mainScreen.TitleScreen;
import network.Client;
import render.GameRenderer;
import render.RenderOrder;
import render.Renderable;
import render.UIColourTheme;
import render.anim.LerpAnimation;
import render.level.tile.RenderElement;
import render.texture.BackgroundTexture;
import render.texture.ImageSequenceGroup;
import render.texture.ResourceLocation;
import render.types.UIHitPointBar;
import render.types.text.FixedTextRenderer;
import render.types.text.TextAlign;
import render.types.text.UITextLabel;
import render.types.text.UITooltip;
import save.GameSave;
import save.MapSave;
import save.SaveManager;
import unit.bot.BotTileDataType;
import unit.type.UnitType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class MainPanel extends JFrame implements KeyListener, MouseListener, MouseWheelListener, RegisteredTickable {
    //TODO: Set to true when not testing
    public static final boolean CREATE_SERVER_AND_CLIENT_CONNECTIONS = true;

    public static final BotTileDataType BOT_DEBUG_RENDER = null;
    public static final boolean BOT_DEBUG_RENDER_UNIT = true;

    public static final SaveManager<GameSave> levelSaves = new SaveManager<>("saves.sav");
    public static final SaveManager<MapSave> mapSaves = new SaveManager<>("tutorial-maps.sav");
    public static final SaveManager<MapSave> tutorialMaps = new SaveManager<>("tutorial/tutorial-maps.sav");

    public static AffineTransform windowTransform = new AffineTransform();

    public static ObjPos DEVICE_WINDOW_SIZE; //the physical screen size, in pixels
    public static ObjPos RENDER_WINDOW_SIZE; //the size of the render box, in pixels
    public static ObjPos BLOCK_DIMENSIONS; //the size of the render box, in blocks
    public static ObjPos INSETS_OFFSET = new ObjPos(); //The offset added to account for frame borders in windowed mode

    public static Client client = null;

    public static AbstractLevel<?, ?> activeLevel;
    public static TitleScreen titleScreen;

    public static InputReceiver activeInputReceiver = null;

    public static LerpAnimation fadeScreen = new LerpAnimation(0.5f);

    private static final Renderable LOAD_SCREEN_IMAGE = Renderable.renderImage(new ResourceLocation("load_screen.png"), false, true, 60, true);
    private static final GameRenderer loadRenderer = new GameRenderer(MainPanel.windowTransform, null);
    private static final FixedTextRenderer loadText = new FixedTextRenderer("Initializing...", 1f, UITextLabel.TEXT_COLOUR_DARK)
            .setTextAlign(TextAlign.CENTER).setBold(true);
    private static final UIHitPointBar loadBar = new UIHitPointBar(0.1f, 16, 1, 0.15f, 1, UIColourTheme.LIGHT_BLUE).setRounding(0.5f);
    private static boolean loadBarEnabled = false;

    public static boolean controlHeld = false, shiftHeld = false;
    public static boolean loaded = false, loadFadeComplete = false;
    public static ObjPos lastUIMousePos = new ObjPos();
    public static final ArrayList<Renderable> generatedTooltipRenderers = new ArrayList<>();

    public void init() {
        new RenderElement(loadRenderer, RenderOrder.TITLE_SCREEN_BACKGROUND, LOAD_SCREEN_IMAGE);
        Renderable loadBarTransformed = loadBar.translate(Renderable.right() / 2 - 8, 0.5f);
        new RenderElement(loadRenderer, RenderOrder.TITLE_SCREEN_BUTTONS, loadText.translate(Renderable.right() / 2, 2),
                g -> {
                    if (loadBarEnabled)
                        loadBarTransformed.render(g);
                });
        fadeScreen.setReversed(true);
        fadeScreen.finish();
        Level.EXECUTOR.submit(() -> {
            loadText.updateText("Loading game saves...");
            levelSaves.loadSavesExternal();
            mapSaves.loadSavesExternal();
            tutorialMaps.loadSavesInternal();
            loadText.updateText("Loading units...");
            UnitType.initAll();
            loadText.updateText("Loading main menu...");
            titleScreen = new TitleScreen();
            titleScreen.init();
            activeInputReceiver = titleScreen;
            registerTickable();
            loadText.updateText("Loading background textures...");
            BackgroundTexture.init();
            loadText.updateText("Loading tiles...");
            TileType.init();
            loadText.updateText("Loading projectiles...");
            ImageSequenceGroup.init();
            loadText.updateText("Loading structures...");
            StructureType.init();
            loadText.updateText("Loading complete!");
            fadeScreen.setReversed(false);
            loaded = true;
        });
    }

    public static Level getActiveLevel() {
        return (Level) activeLevel;
    }

    public static void setLoadBarEnabled(boolean enabled) {
        setLoadBarProgress(0);
        loadBarEnabled = enabled;
    }

    public static void setLoadBarProgress(float progress) {
        loadBar.setFill(progress);
    }

    public static void startNewLevel(TutorialLevel tutorialLevel, Supplier<AbstractLevel<?, ?>> levelCreator) {
        TutorialManager.startTutorial(tutorialLevel);
        createLevel(levelCreator);
    }

    public static void startNewLevel(Supplier<AbstractLevel<?, ?>> levelCreator) {
        TutorialManager.endTutorial();
        createLevel(levelCreator);
    }

    private static void createLevel(Supplier<AbstractLevel<?, ?>> levelCreator) {
        new Thread(() -> {
            fadeScreen.setReversed(false);
            activeInputReceiver = null;
            AbstractLevel<?, ?> level = levelCreator.get();
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
        }).start();
    }

    public static void startNewLevel(Supplier<Level> levelCreator, Consumer<Level> onLevelCreated) {
        TutorialManager.endTutorial();
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
            onLevelCreated.accept(level);
        });
    }

    public static void removeClient() {
        if (client != null)
            client.delete();
        client = null;
    }

    public static boolean startClient(String ip) {
        client = new Client(ip);
        if (client.failed) {
            client = null;
            return false;
        }
        return true;
    }

    public static void clientDisconnect() {
        addTask(MainPanel::toTitleScreen);
    }

    public static void toTitleScreen() {
        removeClient();
        if (activeLevel != null) {
            activeLevel.delete();
            activeLevel = null;
        }
        activeInputReceiver = titleScreen;
        titleScreen.reset();
    }

    @Override
    public void paintComponents(Graphics g) {
        generatedTooltipRenderers.clear();
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        Point p = getMousePosition();
        if (p != null)
            lastUIMousePos = new ObjPos(p).subtract(INSETS_OFFSET).scaleToBlocks().flipY().addY(MainPanel.BLOCK_DIMENSIONS.y);

        if (loadFadeComplete) {
            if (activeLevel != null && (fadeScreen.reversed() || fadeScreen.finished())) {
                activeLevel.render(g2d);
            } else {
                titleScreen.render(g2d);
            }
        } else {
            if (loaded && fadeScreen.finished()) {
                fadeScreen.setReversed(true);
                loadFadeComplete = true;
            }
            loadRenderer.render(g2d);
        }
        float a = fadeScreen.normalisedProgress();
        if (a != 0) {
            g2d.setColor(new Color(0, 0, 0, a));
            g2d.fillRect(0, 0, RENDER_WINDOW_SIZE.xInt(), RENDER_WINDOW_SIZE.yInt());
        }
    }

    private static final Vector<Runnable> tasksQ = new Vector<>(), noAnimBlockTasksQ = new Vector<>();
    private static final Vector<Runnable> tasks = new Vector<>(), noAnimBlockTasks = new Vector<>();

    public static void addTask(Runnable task) {
        tasksQ.add(task);
    }

    public static void addTaskAfterAnimBlock(Runnable task) {
        noAnimBlockTasksQ.add(task);
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public synchronized void keyPressed(KeyEvent e) {
        tasks.add(() -> {
            if (e.getKeyCode() == KeyEvent.VK_CONTROL)
                controlHeld = true;
            if (e.getKeyCode() == KeyEvent.VK_SHIFT)
                shiftHeld = true;
            if (activeInputReceiver != null)
                activeInputReceiver.acceptPressed(InputType.getInputType(e));
        });
    }

    @Override
    public synchronized void keyReleased(KeyEvent e) {
        tasks.add(() -> {
            if (e.getKeyCode() == KeyEvent.VK_CONTROL)
                controlHeld = false;
            if (e.getKeyCode() == KeyEvent.VK_SHIFT)
                shiftHeld = false;
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
            if (activeInputReceiver != null) {
                activeInputReceiver.acceptPressed(InputType.getScrollInput(e));
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
        if (!loaded)
            return;
        tasksQ.removeIf(r -> {
            tasks.add(r);
            return true;
        });
        tasks.removeIf(r -> {
            r.run();
            return true;
        });

        noAnimBlockTasksQ.removeIf(r -> {
            noAnimBlockTasks.add(r);
            return true;
        });
        if (activeLevel == null)
            noAnimBlockTasks.clear();
        else {
            if (!activeLevel.levelRenderer.runningAnim()) {
                AtomicBoolean blocked = new AtomicBoolean(false);
                noAnimBlockTasks.removeIf(task -> {
                    if (blocked.get() || activeLevel.levelRenderer.runningAnim()) {
                        blocked.set(true);
                        return false;
                    }
                    task.run();
                    return true;
                });
            }
        }
    }

    @Override
    public void delete() {
        removeTickable();
    }
}
