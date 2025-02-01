package unit.weapon;

import render.Renderable;
import render.anim.ImageSequenceAnim;
import render.texture.ImageSequenceGroup;
import render.texture.ResourceLocation;

import java.awt.*;
import java.util.ArrayList;
import java.util.function.Supplier;

public class ProjectileType implements Renderable {
    public static final ProjectileType FIGHTER_PLASMA = new ProjectileType("projectile/plasma/plasma_1.png", 2,
            90, 5, ImageSequenceGroup.PLASMA_HIT, () -> {
        ArrayList<ProjectileSpawnPoint> spawnPoints = new ArrayList<>();
        spawnPoints.add(new ProjectileSpawnPoint(x(550), y(170), 0));
        spawnPoints.add(new ProjectileSpawnPoint(x(677), y(546), 150));
        spawnPoints.add(new ProjectileSpawnPoint(x(550), y(170), 300));
        spawnPoints.add(new ProjectileSpawnPoint(x(677), y(546), 450));
        return spawnPoints;
    }), BOMBER_MISSILE = new ProjectileType("projectile/missile/bomber_missile.png", 5,
            70, 13, ImageSequenceGroup.EXPLOSION, () -> {
        ArrayList<ProjectileSpawnPoint> spawnPoints = new ArrayList<>();
        spawnPoints.add(new ProjectileSpawnPoint(x(555), y(245), 0));
        spawnPoints.add(new ProjectileSpawnPoint(x(643), y(534), 400));
        return spawnPoints;
    }), BOMBER_PLASMA = new ProjectileType("projectile/plasma/plasma_1.png", 2,
            90, 5, ImageSequenceGroup.PLASMA_HIT, () -> {
        ArrayList<ProjectileSpawnPoint> spawnPoints = new ArrayList<>();
        spawnPoints.add(new ProjectileSpawnPoint(x(681), y(278), 0));
        spawnPoints.add(new ProjectileSpawnPoint(x(720), y(459), 150));
        spawnPoints.add(new ProjectileSpawnPoint(x(681), y(278), 300));
        spawnPoints.add(new ProjectileSpawnPoint(x(720), y(459), 450));
        return spawnPoints;
    });

    private final Renderable image;
    public final float velocity;
    private final float hitWidth;
    private final ImageSequenceGroup hitSequence;
    private final Supplier<ArrayList<ProjectileSpawnPoint>> getSpawnPoints;

    public ProjectileType(String path, float projectileWidth, float velocity, float hitWidth, ImageSequenceGroup hitSequence, Supplier<ArrayList<ProjectileSpawnPoint>> getSpawnPoints) {
        this.velocity = velocity;
        this.hitWidth = hitWidth;
        this.hitSequence = hitSequence;
        this.getSpawnPoints = getSpawnPoints;
        image = Renderable.renderImageCentered(new ResourceLocation(path), true, projectileWidth, true);
    }

    @Override
    public void render(Graphics2D g) {
        image.render(g);
    }

    public ProjectileSpawner spawner() {
        return new ProjectileSpawner(this, getSpawnPoints);
    }

    private static float x(float pixel) {
        return pixel / 1000 * 15 - 15 / 2f;
    }

    private static float y(float pixel) {
        return pixel / 1000 * 15 - 15 / 2f * 0.7f;
    }

    public ImageSequenceAnim hitAnim() {
        return new ImageSequenceAnim(hitSequence.getRandomSequence(), hitWidth, 0.5f);
    }
}
