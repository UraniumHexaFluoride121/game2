package render.anim.unit;

import foundation.tick.Tickable;
import render.Renderable;

public interface AnimRenderable extends Renderable, Tickable {
    AnimType type();
    int zOrder();
}
