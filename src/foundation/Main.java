package foundation;

import foundation.math.ObjPos;
import foundation.tick.Tick;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Main {
    public static final Tick TICK = new Tick();

    public static MainPanel window = new MainPanel();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::init);
    }

    public static final int BLOCKS_X = 60, MIN_BLOCKS_Y = 25; //The minimum number of blocks that have to be able to fit on the screen

    public static void init() {
        window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        window.addKeyListener(window);
        window.addMouseListener(window);
        window.addMouseWheelListener(window);
        MainPanel.DEVICE_WINDOW_SIZE = new ObjPos(Toolkit.getDefaultToolkit().getScreenSize().width, Toolkit.getDefaultToolkit().getScreenSize().height);
        //MainPanel.DEVICE_WINDOW_SIZE = new ObjPos(1200, 500);
        MainPanel.RENDER_WINDOW_SIZE = MainPanel.DEVICE_WINDOW_SIZE.copy();
        float blockSize = Math.min(MainPanel.RENDER_WINDOW_SIZE.x / (BLOCKS_X * 16), MainPanel.RENDER_WINDOW_SIZE.y / (MIN_BLOCKS_Y * 16)); //The screen-size height and width of a texture pixel

        window.requestFocus();
        if (MainPanel.RENDER_WINDOW_SIZE.x / BLOCKS_X < MainPanel.RENDER_WINDOW_SIZE.y / MIN_BLOCKS_Y) {
            window.setUndecorated(true);

            window.setResizable(false);
            window.pack();
            GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(window);
        } else {
            window.setResizable(false);
            window.pack();
            MainPanel.RENDER_WINDOW_SIZE.multiply(0.95f, 0.95f); //If we can't full-screen it, we should add some margin to the window
            blockSize = (int) Math.min(MainPanel.RENDER_WINDOW_SIZE.x / (BLOCKS_X * 16), MainPanel.RENDER_WINDOW_SIZE.y / (MIN_BLOCKS_Y * 16)); //Recalculate texture size with the margin in mind
            MainPanel.RENDER_WINDOW_SIZE.set(blockSize * 16 * BLOCKS_X, MainPanel.RENDER_WINDOW_SIZE.y); //set the final size of the render box

            Insets insets = window.getInsets();
            //set screen size plus insets. There shouldn't be a problem with adding insets since we have margin.
            window.setSize((int) (blockSize * 16 * BLOCKS_X + insets.left + insets.right), (int) MainPanel.RENDER_WINDOW_SIZE.y + insets.top + insets.bottom);
            MainPanel.INSETS_OFFSET = new ObjPos(insets.left, insets.top);
            MainPanel.windowTransform.translate(insets.left, insets.top);
        }
        MainPanel.BLOCK_DIMENSIONS = new ObjPos(BLOCKS_X, MainPanel.RENDER_WINDOW_SIZE.y / (blockSize * 16));

        //scale, flip and lower the render coordinate system.
        //This scales it from one render coordinate unit being the size of a screen pixel to
        //being the size of a block, and sets the origin of the coordinate system to be in the
        //bottom left corner instead of the top left. We also flip the y-axis so that y+ is up instead of down.
        MainPanel.windowTransform.scale(blockSize * 16, -blockSize * 16);
        MainPanel.windowTransform.translate(0, -MainPanel.BLOCK_DIMENSIONS.y);

        window.setVisible(true);
        window.createBufferStrategy(2);

        window.init();

        TICK.start();
    }
}