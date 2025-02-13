package unit.weapon;

import java.util.ArrayList;
import java.util.function.Supplier;

public class ProjectileSpawner {
    private final ProjectileType type;
    private final ArrayList<ProjectileSpawnPoint> getSpawnPoints;
    private final long startTime = System.currentTimeMillis();

    public ProjectileSpawner(ProjectileType type, Supplier<ArrayList<ProjectileSpawnPoint>> getSpawnPoints) {
        this.type = type;
        this.getSpawnPoints = getSpawnPoints.get();
    }

    public ArrayList<Projectile> getSpawnedProjectiles(float x, float y, float width) {
        ArrayList<Projectile> projectiles = new ArrayList<>();
        long currentTime = System.currentTimeMillis();
        getSpawnPoints.removeIf(spawnPoint -> {
            if (currentTime - startTime > spawnPoint.time()) {
                projectiles.add(new Projectile(type, x + spawnPoint.x() * width, y + spawnPoint.y() * width));
                return true;
            }
            return false;
        });
        return projectiles;
    }

    public boolean finished() {
        return getSpawnPoints.isEmpty();
    }
}
