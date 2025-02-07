package render.texture;

import foundation.math.MathUtil;
import foundation.math.ObjPos;
import level.Level;
import render.*;
import render.anim.ImageSequenceAnim;
import render.anim.LerpAnimation;
import render.anim.ExpAnimation;
import render.anim.SineAnimation;
import render.ui.implementation.UIHitPointBar;
import unit.Unit;
import unit.weapon.Projectile;
import unit.weapon.ProjectileSpawner;
import unit.weapon.WeaponInstance;

import java.awt.*;
import java.util.ArrayList;
import java.util.function.Supplier;

public class FiringRenderer extends AbstractRenderElement {
    private ImageRenderer leftImage, rightImage;
    private static final Color SEPARATOR_COLOUR = new Color(39, 39, 39);
    private static final Color SEPARATOR_BORDER_COLOUR = new Color(69, 69, 69);
    private static final BasicStroke SEPARATOR_STROKE = Renderable.sharpCornerStroke(2f), SEPARATOR_TEAM_STROKE = Renderable.sharpCornerStroke(0.3f);
    private WeaponInstance leftWeapon, rightWeapon;
    private Unit leftUnit, rightUnit, attackingUnit, attackedUnit;
    private UnitRenderer[] leftUnitRenderer, rightUnitRenderer;
    private UIHitPointBar hitPointBarLeft, hitPointBarRight;
    private final ArrayList<Projectile> leftProjectiles = new ArrayList<>(), rightProjectiles = new ArrayList<>();
    private final LerpAnimation shootTimerAttacking = new LerpAnimation(1.7f), shootTimerAttacked = new LerpAnimation(2.2f), endTimer = new LerpAnimation(1);
    private boolean firingLeft = false, firingRight = false, finished = false, leftHit = false, rightHit = false;
    private Level level;
    private LerpAnimation overlayTimer = new LerpAnimation(1);

    public FiringRenderer(RenderRegister<OrderedRenderable> register, RenderOrder order, Level level) {
        super(register, order);
        this.level = level;
        renderable = g -> {
            if (!showLevel()) {
                if (!firingLeft && (leftUnit == attackingUnit ? shootTimerAttacking : shootTimerAttacked).finished()) {
                    firingLeft = true;
                    for (UnitRenderer unit : leftUnitRenderer) {
                        unit.spawnProjectile();
                    }
                }
                if (!firingRight && (rightUnit == attackingUnit ? shootTimerAttacking : shootTimerAttacked).finished()) {
                    firingRight = true;
                    for (UnitRenderer unit : rightUnitRenderer) {
                        unit.spawnProjectile();
                    }
                }
                renderBackground(false, leftImage, g);
                renderBackground(true, rightImage, g);
                renderUnits(false, leftUnitRenderer, g);
                renderUnits(true, rightUnitRenderer, g);
                renderProjectiles(false, g);
                renderProjectiles(true, g);
                renderUnitExplosions(false, leftUnitRenderer, g);
                renderUnitExplosions(true, rightUnitRenderer, g);
                renderHPBar(false, g);
                renderHPBar(true, g);
                g.setStroke(SEPARATOR_STROKE);
                g.setColor(SEPARATOR_COLOUR);
                g.drawLine(30, 0, 30, (int) Renderable.top() + 1);
                g.setStroke(SEPARATOR_TEAM_STROKE);
                g.setColor(SEPARATOR_BORDER_COLOUR);
                g.drawLine(29, 0, 29, (int) Renderable.top() + 1);
                g.drawLine(31, 0, 31, (int) Renderable.top() + 1);
            }
            g.setColor(new Color(0, 0, 0, 1 - Math.abs(overlayTimer.normalisedProgress() * 2 - 1)));
            g.fillRect(0, 0, 60, (int) Renderable.top() + 1);
            if (!rightHit) {
                for (Projectile projectile : leftProjectiles) {
                    if (projectile.exploding()) {
                        for (UnitRenderer unit : rightUnitRenderer) {
                            unit.onHit();
                        }
                        hitPointBarRight.setFill(rightUnit.firingTempHP, 1, 0.5f);
                        rightHit = true;
                        break;
                    }
                }
            }
            if (!leftHit) {
                for (Projectile projectile : rightProjectiles) {
                    if (projectile.exploding()) {
                        for (UnitRenderer unit : leftUnitRenderer) {
                            unit.onHit();
                        }
                        hitPointBarLeft.setFill(leftUnit.firingTempHP, 1, 0.5f);
                        leftHit = true;
                        break;
                    }
                }
            }
            if (!finished && firingLeft && firingRight) {
                for (UnitRenderer unit : leftUnitRenderer) {
                    if (unit.spawner == null)
                        continue;
                    if (!unit.spawner.finished())
                        return;
                }
                for (UnitRenderer unit : rightUnitRenderer) {
                    if (unit.spawner == null)
                        continue;
                    if (!unit.spawner.finished())
                        return;
                }
                for (Projectile projectile : leftProjectiles) {
                    if (!projectile.finished())
                        return;
                }
                for (Projectile projectile : rightProjectiles) {
                    if (!projectile.finished())
                        return;
                }
                endTimer.startTimer();
                finished = true;
            }
            if (finished && endTimer.finished()) {
                if (!overlayTimer.reversed()) {
                    overlayTimer.setReversed(true);
                    overlayTimer.startTimer();
                }
                if (overlayTimer.finished()) {
                    leftImage = null;
                    rightImage = null;
                    leftUnitRenderer = null;
                    rightUnitRenderer = null;
                    level.levelRenderer.endFiring(leftUnit, rightUnit);
                    leftUnit = null;
                    rightUnit = null;
                    attackedUnit = null;
                    attackingUnit = null;
                }
            }
        };
    }

    public boolean showLevel() {
        return overlayTimer.normalisedProgress() < 0.5f;
    }

    private void renderBackground(boolean right, ImageRenderer background, Graphics2D g) {
        GameRenderer.renderTransformed(g, () -> {
            if (right) {
                g.translate(60, 0);
                g.scale(-1, 1);
            }
            background.render(g, 30);
        });
    }

    private void renderUnits(boolean right, UnitRenderer[] units, Graphics2D g) {
        GameRenderer.renderTransformed(g, () -> {
            if (right) {
                g.translate(60, 0);
                g.scale(-1, 1);
            }
            for (UnitRenderer unit : units) {
                GameRenderer.renderOffset(unit.x, unit.y, g, () -> {
                    unit.render(g);
                });
            }
        });
    }

    private void renderProjectiles(boolean right, Graphics2D g) {
        GameRenderer.renderTransformed(g, () -> {
            if (right) {
                g.translate(60, 0);
                g.scale(-1, 1);
            }
            for (Projectile projectile : right ? rightProjectiles : leftProjectiles) {
                projectile.render(g);
            }
        });
    }

    private void renderUnitExplosions(boolean right, UnitRenderer[] units, Graphics2D g) {
        GameRenderer.renderTransformed(g, () -> {
            if (right) {
                g.translate(60, 0);
                g.scale(-1, 1);
            }
            for (UnitRenderer unit : units) {
                GameRenderer.renderOffset(unit.x, unit.y, g, () -> {
                    unit.renderExplosions(g);
                });
            }
        });
    }

    private void renderHPBar(boolean right, Graphics2D g) {
        GameRenderer.renderTransformed(g, () -> {
            if (right) {
                g.translate(60, 0);
                g.scale(-1, 1);
            }
            g.translate(1, 1);
            if (right) {
                hitPointBarRight.render(g);
            } else {
                hitPointBarLeft.render(g);
            }
        });
    }

    public void start(Unit a, Unit b, WeaponInstance weaponA, WeaponInstance weaponB) {
        attackingUnit = a;
        attackedUnit = b;
        boolean aIsLeft;
        if (a.pos.x > b.pos.x) {
            aIsLeft = false;
        } else if (a.pos.x < b.pos.x) {
            aIsLeft = true;
        } else
            aIsLeft = a.pos.y <= b.pos.y;
        leftUnit = aIsLeft ? a : b;
        rightUnit = aIsLeft ? b : a;
        leftWeapon = aIsLeft ? weaponA : weaponB;
        rightWeapon = aIsLeft ? weaponB : weaponA;
        ObjPos[] leftPositions = leftUnit.type.firingPositions.get();
        ObjPos[] rightPositions = rightUnit.type.firingPositions.get();
        int leftUnitCount = (int) Math.ceil((leftUnit.hitPoints / leftUnit.type.hitPoints) * leftPositions.length);
        int rightUnitCount = (int) Math.ceil((rightUnit.hitPoints / rightUnit.type.hitPoints) * rightPositions.length);
        int leftUnitCountRemaining = (int) Math.ceil((leftUnit.firingTempHP / leftUnit.type.hitPoints) * leftPositions.length);
        int rightUnitCountRemaining = (int) Math.ceil((rightUnit.firingTempHP / rightUnit.type.hitPoints) * rightPositions.length);
        leftUnitRenderer = new UnitRenderer[leftUnitCount];
        rightUnitRenderer = new UnitRenderer[rightUnitCount];
        for (int i = 0; i < leftUnitCount; i++) {
            ImageCounter image = new CachedImageCounter(leftUnit.type.firingSequenceLeft.get(leftUnit.team));
            leftUnitRenderer[i] = new UnitRenderer(image, leftWeapon, leftUnit.getFireAnimState(leftWeapon), leftProjectiles, i >= leftUnitCountRemaining, leftUnit == attackingUnit, leftPositions[i].x, leftPositions[i].y);
        }
        for (int i = 0; i < rightUnitCount; i++) {
            ImageCounter image = new CachedImageCounter(rightUnit.type.firingSequenceLeft.get(rightUnit.team));
            rightUnitRenderer[i] = new UnitRenderer(image, rightWeapon, rightUnit.getFireAnimState(rightWeapon), rightProjectiles, i >= rightUnitCountRemaining, rightUnit == attackingUnit, rightPositions[i].x, rightPositions[i].y);
        }
        leftImage = level.getTile(leftUnit.pos).type.firingTexturesLeft.getRandomImage();
        rightImage = level.getTile(rightUnit.pos).type.firingTexturesRight.getRandomImage();
        shootTimerAttacking.startTimer();
        firingLeft = false;
        shootTimerAttacked.startTimer();
        firingRight = false;
        finished = false;
        leftHit = false;
        rightHit = false;
        leftProjectiles.clear();
        rightProjectiles.clear();
        overlayTimer.setReversed(false);
        overlayTimer.startTimer();
        hitPointBarLeft = new UIHitPointBar(0.2f, 20, 1.5f, 0.2f, leftUnit).setFill(leftUnit.hitPoints);
        hitPointBarRight = new UIHitPointBar(0.2f, 20, 1.5f, 0.2f, rightUnit).setFill(rightUnit.hitPoints);
        preRender();
    }

    @Override
    public void delete() {
        super.delete();
        leftUnit = null;
        rightUnit = null;
        attackedUnit = null;
        attackingUnit = null;
        leftProjectiles.clear();
        rightProjectiles.clear();
        leftUnitRenderer = null;
        rightUnitRenderer = null;
        leftWeapon = null;
        rightWeapon = null;
        leftImage = null;
        rightImage = null;
        level = null;
    }

    public static Supplier<ObjPos[]> THREE_UNITS = () -> new ObjPos[]{
            new ObjPos(17, Renderable.top() * 0.5f),
            new ObjPos(14, Renderable.top() * 0.8f),
            new ObjPos(12, Renderable.top() * 0.2f)
    };

    private static class UnitRenderer implements Renderable {
        private ImageSequenceAnim explosion = null;
        private final ImageCounter unit;
        private final WeaponInstance weapon;
        private final WeaponInstance.FireAnimState fireState;
        private ProjectileSpawner spawner = null;
        private LerpAnimation explosionTimer = null;
        private final SineAnimation animX = new SineAnimation(MathUtil.randFloatBetween(5, 8, Math::random), MathUtil.randFloatBetween(0, 360, Math::random));
        private final SineAnimation animY = new SineAnimation(MathUtil.randFloatBetween(5, 8, Math::random), MathUtil.randFloatBetween(0, 360, Math::random));
        private final ExpAnimation forwardAnim = new ExpAnimation(0.99f, 1.5f);
        private final ArrayList<Projectile> projectiles;
        private final boolean toBeDestroyed, isAttacking;
        private boolean enabled = true, exploding = false;
        private final float x, y;
        private LerpAnimation shakeDuration = null;

        private UnitRenderer(ImageCounter unit, WeaponInstance weapon, WeaponInstance.FireAnimState fireAnimState, ArrayList<Projectile> projectiles, boolean toBeDestroyed, boolean isAttacking, float x, float y) {
            this.unit = unit;
            this.weapon = weapon;
            this.fireState = fireAnimState;
            this.projectiles = projectiles;
            this.toBeDestroyed = toBeDestroyed;
            this.isAttacking = isAttacking;
            this.x = x;
            this.y = y;
            if (fireAnimState == WeaponInstance.FireAnimState.EMPTY)
                unit.end();
        }

        @Override
        public void render(Graphics2D g) {
            if (!enabled && explosionTimer.finished()) {
                if (!exploding) {
                    explosion = new ImageSequenceAnim(ImageSequenceGroup.EXPLOSION.getRandomSequence(), 30, 1);
                    exploding = true;
                }
                return;
            }
            if (spawner != null) {
                ArrayList<Projectile> spawnedProjectiles = spawner.getSpawnedProjectiles(x + xOffset(), y + yOffset());
                if (fireState == WeaponInstance.FireAnimState.FIRE)
                    unit.increment(spawnedProjectiles.size());
                projectiles.addAll(spawnedProjectiles);
            }
            if (shakeDuration != null && !shakeDuration.finished() && (int) (shakeDuration.normalisedProgress() * 10) % 2 == 0) {
                g.translate(0.2, 0.2);
            }
            g.translate(xOffset(), yOffset());
            unit.render(g, 15);
        }

        public void renderExplosions(Graphics2D g) {
            if (exploding) {
                g.translate(xOffset(), yOffset());
                explosion.render(g);
            }
        }

        private float xOffset() {
            return animX.normalisedProgress() + (1 - forwardAnim.normalisedProgress()) * -15;
        }

        private float yOffset() {
            return animY.normalisedProgress();
        }

        public void spawnProjectile() {
            if (weapon == null || (toBeDestroyed && !isAttacking))
                return;
            spawner = weapon.projectileType.spawner();
        }

        public void onHit() {
            if (toBeDestroyed) {
                enabled = false;
                explosionTimer = new LerpAnimation(MathUtil.randFloatBetween(0.3f, 0.7f, Math::random));
            }
            shakeDuration = new LerpAnimation(MathUtil.randFloatBetween(0.4f, 0.6f, Math::random));
            shakeDuration.startTimer(MathUtil.randFloatBetween(0f, 0.2f, Math::random));
        }
    }
}
