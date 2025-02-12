package level.structure;

import foundation.math.ObjPos;
import render.GameRenderer;
import render.Renderable;
import render.anim.ImageSequenceAnim;
import render.texture.ImageSequenceGroup;

import java.awt.*;
import java.util.ArrayList;

import static level.tile.Tile.*;

public class ObjectExplosion implements Renderable {
    private final ObjPos center;
    private final ImageSequenceAnim anim1, anim2, anim3;
    private final ImageSequenceAnim[] anims;


    public ObjectExplosion(ObjPos center, float time) {
        this.center = center;
        ArrayList<ImageSequenceAnim> animList = new ArrayList<>();
        anim1 = new ImageSequenceAnim(ImageSequenceGroup.EXPLOSION.getRandomSequence(), TILE_SIZE, time);
        animList.add(anim1);
        anim2 = new ImageSequenceAnim(ImageSequenceGroup.EXPLOSION.getRandomSequence(), TILE_SIZE, time, time * 0.8f);
        animList.add(anim2);
        anim3 = new ImageSequenceAnim(ImageSequenceGroup.EXPLOSION.getRandomSequence(), TILE_SIZE, time, time * 1.6f);
        animList.add(anim3);
        anims = new ImageSequenceAnim[3];
        for (int i = 0; i < 3; i++) {
            anims[i] = animList.remove((int) (Math.random() * animList.size()));
        }
    }

    public boolean finished() {
        return anim3.finished();
    }

    @Override
    public void render(Graphics2D g) {
        GameRenderer.renderOffset(center, g, () -> {
            GameRenderer.renderOffset(TILE_SIZE * 0.1f, TILE_SIZE * -0.15f, g, () -> {
                anims[0].render(g);
            });
            GameRenderer.renderOffset(0, TILE_SIZE * 0.15f, g, () -> {
                anims[1].render(g);
            });
            GameRenderer.renderOffset(TILE_SIZE * -0.1f, TILE_SIZE * -0.05f, g, () -> {
                anims[2].render(g);
            });
        });
    }
}