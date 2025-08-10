package render.particle;

import java.awt.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public record ParticleModifier(BiConsumer<Particle, Float> tick, BiConsumer<Particle, Graphics2D> render, Consumer<Particle> initialisation) {
}
