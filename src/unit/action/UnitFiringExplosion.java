package unit.action;

import foundation.Deletable;
import foundation.math.ObjPos;
import render.GameRenderer;
import render.Renderable;
import render.anim.ImageSequenceAnim;
import render.texture.ImageSequenceGroup;
import unit.Unit;

import java.awt.*;
import java.util.ArrayList;

import static level.tile.Tile.*;

public class UnitFiringExplosion implements Renderable, Deletable {
    private final ObjPos center;
    private final ImageSequenceAnim
            anim1 = new ImageSequenceAnim(ImageSequenceGroup.EXPLOSION.getRandomSequence(), TILE_SIZE, .5f),
            anim2 = new ImageSequenceAnim(ImageSequenceGroup.EXPLOSION.getRandomSequence(), TILE_SIZE, .5f, 0.4f),
            anim3 = new ImageSequenceAnim(ImageSequenceGroup.EXPLOSION.getRandomSequence(), TILE_SIZE, .5f, 0.8f);
    private final ImageSequenceAnim[] anims;

    private Unit a, b;

    public UnitFiringExplosion(ObjPos center, Unit a, Unit b) {
        this.center = center;
        this.a = a;
        this.b = b;
        ArrayList<ImageSequenceAnim> animList = new ArrayList<>();
        animList.add(anim1);
        animList.add(anim2);
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

    @Override
    public void delete() {
        a.postFiring(b);
        b.postFiring(a);
        a = null;
        b = null;
    }
}