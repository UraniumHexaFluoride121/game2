package unit.weapon;

import render.Renderable;
import render.anim.ImageSequenceAnim;
import render.texture.ImageRenderer;
import render.texture.ImageSequenceGroup;
import render.texture.ResourceLocation;

import java.awt.*;
import java.util.ArrayList;
import java.util.function.Supplier;

public class ProjectileType implements Renderable {
    private static final ImageRenderer INFO_RAIL_GUN_BULLET = ImageRenderer.renderImageCentered(new ResourceLocation("projectile/rail_gun_bullet/bullet_info.png"), true, true);
    private static final ImageRenderer INFO_BULLET = ImageRenderer.renderImageCentered(new ResourceLocation("projectile/bullet/bullet_info.png"), true, true);
    private static final ImageRenderer INFO_MISSILE = ImageRenderer.renderImageCentered(new ResourceLocation("projectile/missile/missile_info.png"), true, true);
    private static final ImageRenderer INFO_PLASMA = ImageRenderer.renderImageCentered(new ResourceLocation("projectile/plasma/plasma_info.png"), true, true);

    public static final ProjectileType FIGHTER_PLASMA = new ProjectileType("projectile/plasma/plasma_1.png", 2,
            INFO_PLASMA, 90, 5, 0, .5f, 0, ImageSequenceGroup.PLASMA_HIT, null, () -> {
        ArrayList<ProjectileSpawnPoint> spawnPoints = new ArrayList<>();
        spawnPoints.add(new ProjectileSpawnPoint(x(550), y(170), 0));
        spawnPoints.add(new ProjectileSpawnPoint(x(677), y(546), 150));
        spawnPoints.add(new ProjectileSpawnPoint(x(550), y(170), 300));
        spawnPoints.add(new ProjectileSpawnPoint(x(677), y(546), 450));
        return spawnPoints;
    }), BOMBER_MISSILE = new ProjectileType("projectile/missile/bomber_missile.png", 5,
            INFO_MISSILE, 70, 13, 0, .5f, 0, ImageSequenceGroup.EXPLOSION, null, () -> {
        ArrayList<ProjectileSpawnPoint> spawnPoints = new ArrayList<>();
        spawnPoints.add(new ProjectileSpawnPoint(x(555), y(245), 0));
        spawnPoints.add(new ProjectileSpawnPoint(x(643), y(534), 400));
        return spawnPoints;
    }), BOMBER_PLASMA = new ProjectileType("projectile/plasma/plasma_1.png", 2,
            INFO_PLASMA, 90, 5, 0, .5f, 0, ImageSequenceGroup.PLASMA_HIT, null, () -> {
        ArrayList<ProjectileSpawnPoint> spawnPoints = new ArrayList<>();
        spawnPoints.add(new ProjectileSpawnPoint(x(681), y(278), 0));
        spawnPoints.add(new ProjectileSpawnPoint(x(720), y(459), 150));
        spawnPoints.add(new ProjectileSpawnPoint(x(681), y(278), 300));
        spawnPoints.add(new ProjectileSpawnPoint(x(720), y(459), 450));
        return spawnPoints;
    }), SCOUT_PLASMA = new ProjectileType("projectile/plasma/plasma_1.png", 2,
            INFO_PLASMA, 90, 5, 0, .5f, 0, ImageSequenceGroup.PLASMA_HIT, null, () -> {
        ArrayList<ProjectileSpawnPoint> spawnPoints = new ArrayList<>();
        spawnPoints.add(new ProjectileSpawnPoint(x(808), y(270), 0));
        spawnPoints.add(new ProjectileSpawnPoint(x(858), y(420), 150));
        spawnPoints.add(new ProjectileSpawnPoint(x(808), y(270), 300));
        spawnPoints.add(new ProjectileSpawnPoint(x(808), y(420), 450));
        return spawnPoints;
    }), CORVETTE_CANNON = new ProjectileType("projectile/bullet/bullet_1.png", 2,
            INFO_BULLET, 90, 10, 6, .5f, .3f, ImageSequenceGroup.BULLET_HIT, ImageSequenceGroup.EXPLOSION, () -> {
        ArrayList<ProjectileSpawnPoint> spawnPoints = new ArrayList<>();
        spawnPoints.add(new ProjectileSpawnPoint(x(620), y(310), 0));
        spawnPoints.add(new ProjectileSpawnPoint(x(622), y(258), 100));
        spawnPoints.add(new ProjectileSpawnPoint(x(832), y(462), 350));
        spawnPoints.add(new ProjectileSpawnPoint(x(932), y(432), 450));
        return spawnPoints;
    }), DEFENDER_PLASMA = new ProjectileType("projectile/plasma/plasma_1.png", 2,
            INFO_PLASMA, 90, 5, 0, .5f, 0, ImageSequenceGroup.PLASMA_HIT, null, () -> {
        ArrayList<ProjectileSpawnPoint> spawnPoints = new ArrayList<>();
        spawnPoints.add(new ProjectileSpawnPoint(x(800), y(333), 0));
        spawnPoints.add(new ProjectileSpawnPoint(x(808), y(233), 150));
        spawnPoints.add(new ProjectileSpawnPoint(x(888), y(484), 300));
        spawnPoints.add(new ProjectileSpawnPoint(x(914), y(381), 450));
        return spawnPoints;
    }), ARTILLERY_MISSILE = new ProjectileType("projectile/missile/bomber_missile.png", 8,
            INFO_MISSILE, 70, 13, 0, .5f, 0, ImageSequenceGroup.EXPLOSION, null, () -> {
        ArrayList<ProjectileSpawnPoint> spawnPoints = new ArrayList<>();
        spawnPoints.add(new ProjectileSpawnPoint(x(936), y(401), 0));
        spawnPoints.add(new ProjectileSpawnPoint(x(940), y(287), 400));
        return spawnPoints;
    }).renderBehind(), CRUISER_RAIL_GUN = new ProjectileType("projectile/rail_gun_bullet/bullet_1.png", 2,
            INFO_RAIL_GUN_BULLET, 120, 10, 6, .5f, .3f, ImageSequenceGroup.BULLET_HIT, null, () -> {
        ArrayList<ProjectileSpawnPoint> spawnPoints = new ArrayList<>();
        spawnPoints.add(new ProjectileSpawnPoint(x(690), y(357), 0));
        spawnPoints.add(new ProjectileSpawnPoint(x(900), y(470), 100));
        spawnPoints.add(new ProjectileSpawnPoint(x(690), y(357), 350));
        spawnPoints.add(new ProjectileSpawnPoint(x(900), y(470), 450));
        return spawnPoints;
    });

    private final Renderable image;
    public final ImageRenderer infoImage;
    public final float velocity;
    private final float hitWidth, spawnWidth, hitTime, spawnTime;
    private final ImageSequenceGroup hitSequence, spawnSequence;
    private final Supplier<ArrayList<ProjectileSpawnPoint>> getSpawnPoints;
    public boolean renderBehind = false;

    public ProjectileType(String path, float projectileWidth, ImageRenderer infoImage, float velocity, float hitWidth, float spawnWidth, float hitTime, float spawnTime, ImageSequenceGroup hitSequence, ImageSequenceGroup spawnSequence, Supplier<ArrayList<ProjectileSpawnPoint>> getSpawnPoints) {
        this.infoImage = infoImage;
        this.velocity = velocity;
        this.hitWidth = hitWidth;
        this.spawnWidth = spawnWidth;
        this.hitTime = hitTime;
        this.spawnTime = spawnTime;
        this.hitSequence = hitSequence;
        this.spawnSequence = spawnSequence;
        this.getSpawnPoints = getSpawnPoints;
        image = Renderable.renderImageCentered(new ResourceLocation(path), true, projectileWidth, true);
    }

    private ProjectileType renderBehind() {
        renderBehind = true;
        return this;
    }

    @Override
    public void render(Graphics2D g) {
        image.render(g);
    }

    public ProjectileSpawner spawner() {
        return new ProjectileSpawner(this, getSpawnPoints);
    }

    private static float x(float pixel) {
        return pixel / 1000 - 1 / 2f;
    }

    private static float y(float pixel) {
        return pixel / 1000 - 1 / 2f * 0.7f;
    }

    public ImageSequenceAnim hitAnim() {
        return new ImageSequenceAnim(hitSequence.getRandomSequence(), hitWidth, hitTime);
    }

    public ImageSequenceAnim spawnAnim() {
        if (spawnSequence == null)
            return null;
        return new ImageSequenceAnim(spawnSequence.getRandomSequence(), spawnWidth, spawnTime);
    }
}
