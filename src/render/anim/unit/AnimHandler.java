package render.anim.unit;

import foundation.Deletable;
import foundation.TimedTaskQueue;
import foundation.tick.Tickable;
import level.AbstractLevel;
import render.anim.timer.LerpAnimation;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Supplier;

public class AnimHandler implements Deletable, Tickable {
    private final HashMap<AnimType, ArrayList<AnimRenderable>> sortedRenderables = new HashMap<>();
    private final HashSet<AnimElement> anims = new HashSet<>();
    public final TimedTaskQueue tasks = new TimedTaskQueue();

    public AnimHandler() {
    }

    public AnimHandler registerAnim(Animation anim) {
        return registerAnim(anim, () -> {});
    }

    public AnimHandler registerAnim(Animation anim, Runnable onComplete) {
        AnimRenderable[] renderables = anim.getElements();
        anims.add(new AnimElement(anim, renderables, onComplete));
        for (AnimRenderable r : renderables) {
            if (!sortedRenderables.containsKey(r.type()))
                sortedRenderables.put(r.type(), new ArrayList<>());
            sortedRenderables.get(r.type()).add(r);
            sortedRenderables.get(r.type()).sort(Comparator.comparingInt(AnimRenderable::zOrder));
        }
        return this;
    }

    public AnimHandler queueAnim(Supplier<Animation> anim, float delay, Runnable onComplete) {
        tasks.addTask(delay, () -> registerAnim(anim.get(), onComplete));
        return this;
    }

    public AnimHandler queueAnimAfter(Supplier<Animation> anim, float delay, Runnable onComplete) {
        tasks.andThen(delay, () -> registerAnim(anim.get(), onComplete));
        return this;
    }

    public AnimHandler removeAnim(Animation anim) {
        anims.removeIf(e -> {
            if (e.anim == anim) {
                for (AnimRenderable renderable : e.renderables) {
                    sortedRenderables.get(renderable.type()).remove(renderable);
                }
                anim.delete();
                return true;
            }
            return false;
        });
        return this;
    }

    public AnimHandler animBlock(float time, AbstractLevel<?, ?> level) {
        level.levelRenderer.registerTimerBlock(new LerpAnimation(time), () -> {
        });
        return this;
    }

    @Override
    public void delete() {
        sortedRenderables.clear();
        anims.forEach(a -> a.anim.delete());
        anims.clear();
        tasks.delete();
    }

    public void render(Graphics2D g, AnimType type) {
        if (sortedRenderables.containsKey(type))
            sortedRenderables.get(type).forEach(a -> a.render(g));
    }

    private final ArrayList<Animation> qRemove = new ArrayList<>();

    @Override
    public void tick(float deltaTime) {
        sortedRenderables.forEach((type, list) -> list.forEach(a -> a.tick(deltaTime)));
        anims.forEach(e -> {
            if (e.anim.finished()) {
                e.onComplete.run();
                qRemove.add(e.anim);
            }
        });
        qRemove.forEach(this::removeAnim);
        tasks.tick(deltaTime);
    }

    private record AnimElement(Animation anim, AnimRenderable[] renderables, Runnable onComplete) {

    }
}
