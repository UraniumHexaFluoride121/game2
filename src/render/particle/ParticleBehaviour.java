package render.particle;

import java.awt.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public record ParticleBehaviour(BiConsumer<Particle, Float> tick, BiConsumer<Particle, Graphics2D> render, Consumer<Particle> initialisation) {
    private static final BiConsumer<Particle, Float> emptyTick = (p, dT) -> {
    };
    private static final BiConsumer<Particle, Graphics2D> emptyRender = (p, g) -> {
    };
    private static final Consumer<Particle> emptyInitialisation = p -> {
    };

    public static ParticleBehaviour onTick(BiConsumer<Particle, Float> tick) {
        return new ParticleBehaviour(tick, emptyRender, emptyInitialisation);
    }

    public static ParticleBehaviour onRender(BiConsumer<Particle, Graphics2D> render) {
        return new ParticleBehaviour(emptyTick, render, emptyInitialisation);
    }

    public static ParticleBehaviour onInitialisation(Consumer<Particle> initialisation) {
        return new ParticleBehaviour(emptyTick, emptyRender, initialisation);
    }
}
