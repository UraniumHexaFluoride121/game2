package render.texture;

import level.Level;
import render.Renderable;

public class BackgroundTexture {
    private static final ResourceLocation
            RL_NORMAL_1_L1 = new ResourceLocation("background/bg_normal_1_layer_1.png"),
            RL_NORMAL_1_L2 = new ResourceLocation("background/bg_normal_1_layer_2.png"),
            RL_NORMAL_1_L3 = new ResourceLocation("background/bg_normal_1_layer_3.png"),
            RL_NORMAL_1_L4 = new ResourceLocation("background/bg_normal_1_layer_4.png");

    public static BackgroundTexture[] NORMAL_1;

    static {
        Level.EXECUTOR.submit(() -> {
             NORMAL_1 = new BackgroundTexture[]{
                    new BackgroundTexture(2, 0.15f, RL_NORMAL_1_L1),
                    new BackgroundTexture(1.6f, 0.3f, RL_NORMAL_1_L2),
                    new BackgroundTexture(1.5f, 0.4f, RL_NORMAL_1_L3),
                    new BackgroundTexture(1, 0.7f, RL_NORMAL_1_L4)
            };
        });
    }

    public final float cameraMultiplier;

    public Renderable renderable;

    public BackgroundTexture(float scale, float cameraMultiplier, ResourceLocation resource) {
        this.cameraMultiplier = cameraMultiplier;
        renderable = Renderable.renderImage(resource, true, true, 250 / scale, false);
    }
}
