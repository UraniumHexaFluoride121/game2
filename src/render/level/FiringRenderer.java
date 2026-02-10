package render.level;

import foundation.math.MathUtil;
import foundation.math.ObjPos;
import level.Level;
import render.*;
import render.anim.timer.ExpAnimation;
import render.anim.sequence.ImageSequenceAnim;
import render.anim.timer.LerpAnimation;
import render.anim.timer.SineAnimation;
import render.texture.*;
import render.UIColourTheme;
import render.types.UIHitPointBar;
import unit.Unit;
import unit.type.UnitType;
import unit.weapon.DamageHandler;
import unit.weapon.Projectile;
import unit.weapon.ProjectileSpawner;
import unit.weapon.WeaponInstance;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.function.Supplier;

public class FiringRenderer extends AbstractRenderElement {
    private ImageRenderer leftImage, rightImage;
    private static final Color SEPARATOR_COLOUR = new Color(39, 39, 39);
    private static final Color SEPARATOR_BORDER_COLOUR = new Color(69, 69, 69);
    private static final BasicStroke SEPARATOR_STROKE = Renderable.sharpCornerStroke(2f), SEPARATOR_TEAM_STROKE = Renderable.sharpCornerStroke(0.3f);
    private WeaponInstance leftWeapon, rightWeapon;
    private Unit leftUnit, rightUnit, attackingUnit, defendingUnit;
    private UnitRenderer[] leftUnitRenderer, rightUnitRenderer;
    private UIHitPointBar hitPointBarLeft, hitPointBarRight;
    private UIHitPointBar hitPointBarBorderLeft, hitPointBarBorderRight;
    private UIHitPointBar shieldHitPointBarLeft, shieldHitPointBarRight;
    private final ArrayList<Projectile> leftProjectiles = new ArrayList<>(), rightProjectiles = new ArrayList<>();
    private final LerpAnimation shootTimerAttacking = new LerpAnimation(1.7f), shootTimerAttacked = new LerpAnimation(2.2f), endTimer = new LerpAnimation(0.5f);
    private boolean firingLeft = false, firingRight = false, finished = false, leftHit = false, rightHit = false, rightShieldStarted = false, leftShieldStarted = false;
    private Level level;
    private LerpAnimation overlayTimer = new LerpAnimation(1);
    private DamageHandler handler;

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
                renderProjectilesBack(false, g);
                renderProjectilesBack(true, g);
                renderUnits(false, leftUnitRenderer, g);
                renderUnits(true, rightUnitRenderer, g);
                renderProjectilesFront(false, g);
                renderProjectilesFront(true, g);
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
                        shieldHitPointBarRight.setFill(rightUnit.data.shieldHP, rightShieldBarTime, 0.6f);
                        rightHit = true;
                        break;
                    }
                }
            }
            if (rightHit && !rightShieldStarted && shieldHitPointBarRight.finished()) {
                rightShieldStarted = true;
                hitPointBarRight.setFill(rightUnit.data.hitPoints, 1 - rightShieldBarTime, 0.6f);
            }
            if (!leftHit) {
                for (Projectile projectile : rightProjectiles) {
                    if (projectile.exploding()) {
                        for (UnitRenderer unit : leftUnitRenderer) {
                            unit.onHit();
                        }
                        shieldHitPointBarLeft.setFill(leftUnit.data.shieldHP, leftShieldBarTime, 0.6f);
                        leftHit = true;
                        break;
                    }
                }
            }
            if (leftHit && !leftShieldStarted && shieldHitPointBarLeft.finished()) {
                leftShieldStarted = true;
                hitPointBarLeft.setFill(leftUnit.data.hitPoints, 1 - leftShieldBarTime, 0.6f);
            }
            for (UnitRenderer unitRenderer : leftUnitRenderer) {
                unitRenderer.renderShield = !shieldHitPointBarLeft.empty();
            }
            for (UnitRenderer unitRenderer : rightUnitRenderer) {
                unitRenderer.renderShield = !shieldHitPointBarRight.empty();
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
                    level.levelRenderer.endFiring(attackingUnit, defendingUnit, handler);
                    leftUnit = null;
                    rightUnit = null;
                    defendingUnit = null;
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
        Shape prevClip = g.getClip();
        GameRenderer.renderTransformed(g, () -> {
            if (right) {
                g.translate(60, 0);
                g.scale(-1, 1);
            }
            g.clip(new Rectangle2D.Float(0, 0, Renderable.right() / 2, Renderable.top()));
            for (UnitRenderer unit : units) {
                GameRenderer.renderOffset(unit.x, unit.y, g, () -> {
                    unit.render(g);
                });
            }
        });
        g.setClip(prevClip);
    }

    private void renderProjectilesBack(boolean right, Graphics2D g) {
        GameRenderer.renderTransformed(g, () -> {
            if (right) {
                g.translate(60, 0);
                g.scale(-1, 1);
            }
            for (Projectile projectile : right ? rightProjectiles : leftProjectiles) {
                if (projectile.type.renderBehind && !projectile.pastHalfway())
                    projectile.render(g);
            }
        });
    }

    private void renderProjectilesFront(boolean right, Graphics2D g) {
        GameRenderer.renderTransformed(g, () -> {
            if (right) {
                g.translate(60, 0);
                g.scale(-1, 1);
            }
            for (Projectile projectile : right ? rightProjectiles : leftProjectiles) {
                if (!projectile.type.renderBehind || projectile.pastHalfway())
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
            Shape prevClip = g.getClip();
            if (right) {
                if (!shieldHitPointBarRight.empty()) {
                    hitPointBarBorderRight.setColour(UIColourTheme.LIGHT_BLUE);
                    hitPointBarBorderRight.render(g);
                    shieldHitPointBarRight.render(g);
                    g.clip(Renderable.inverseShape(shieldHitPointBarRight.getBarClip()));
                } else {
                    hitPointBarBorderRight.setColour(rightUnit.data.team.uiColour);
                    hitPointBarBorderRight.render(g);
                }
                hitPointBarRight.render(g);
            } else {
                if (!shieldHitPointBarLeft.empty()) {
                    hitPointBarBorderLeft.setColour(UIColourTheme.LIGHT_BLUE);
                    hitPointBarBorderLeft.render(g);
                    shieldHitPointBarLeft.render(g);
                    g.clip(Renderable.inverseShape(shieldHitPointBarLeft.getBarClip()));
                } else {
                    hitPointBarBorderLeft.setColour(leftUnit.data.team.uiColour);
                    hitPointBarBorderLeft.render(g);
                }
                hitPointBarLeft.render(g);
            }
            g.setClip(prevClip);
        });
    }

    private float leftShieldBarTime = 0, rightShieldBarTime = 0;

    public void start(Unit attacking, Unit defending, WeaponInstance weaponA, WeaponInstance weaponB, DamageHandler handler) {
        this.handler = handler;
        attackingUnit = attacking;
        defendingUnit = defending;
        boolean aIsLeft;
        if (attacking.data.pos.x > defending.data.pos.x) {
            aIsLeft = false;
        } else if (attacking.data.pos.x < defending.data.pos.x) {
            aIsLeft = true;
        } else
            aIsLeft = attacking.data.pos.y <= defending.data.pos.y;
        leftUnit = aIsLeft ? attacking : defending;
        rightUnit = aIsLeft ? defending : attacking;
        leftWeapon = aIsLeft ? weaponA : weaponB;
        rightWeapon = aIsLeft ? weaponB : weaponA;
        leftShieldBarTime = (leftUnit.data.shieldRenderHP - leftUnit.data.shieldHP) / (leftUnit.data.renderHP - leftUnit.data.hitPoints + leftUnit.data.shieldRenderHP - leftUnit.data.shieldHP);
        rightShieldBarTime = (rightUnit.data.shieldRenderHP - rightUnit.data.shieldHP) / (rightUnit.data.renderHP - rightUnit.data.hitPoints + rightUnit.data.shieldRenderHP - rightUnit.data.shieldHP);
        ObjPos[] leftPositions = leftUnit.data.type.firingPositions.get();
        ObjPos[] rightPositions = rightUnit.data.type.firingPositions.get();
        int leftUnitCount = (int) Math.ceil((leftUnit.data.renderHP / leftUnit.stats.maxHP()) * leftPositions.length);
        int rightUnitCount = (int) Math.ceil((rightUnit.data.renderHP / rightUnit.stats.maxHP()) * rightPositions.length);
        int leftUnitCountRemaining = (int) Math.ceil((leftUnit.data.hitPoints / leftUnit.stats.maxHP()) * leftPositions.length);
        int rightUnitCountRemaining = (int) Math.ceil((rightUnit.data.hitPoints / rightUnit.stats.maxHP()) * rightPositions.length);
        leftUnitRenderer = new UnitRenderer[leftUnitCount];
        rightUnitRenderer = new UnitRenderer[rightUnitCount];
        for (int i = 0; i < leftUnitCount; i++) {
            ImageCounter image = new CachedImageCounter(leftUnit.data.type.getFiringImage(leftUnit.data.team, true, leftUnit.data.stealthMode));
            leftUnitRenderer[i] = new UnitRenderer(image, leftWeapon, leftUnit.getFireAnimState(leftWeapon), leftProjectiles, i >= leftUnitCountRemaining, leftUnit == attackingUnit, leftPositions[i].x, leftPositions[i].y, leftUnit.data.type);
        }
        for (int i = 0; i < rightUnitCount; i++) {
            ImageCounter image = new CachedImageCounter(rightUnit.data.type.getFiringImage(rightUnit.data.team, false, rightUnit.data.stealthMode));
            rightUnitRenderer[i] = new UnitRenderer(image, rightWeapon, rightUnit.getFireAnimState(rightWeapon), rightProjectiles, i >= rightUnitCountRemaining, rightUnit == attackingUnit, rightPositions[i].x, rightPositions[i].y, rightUnit.data.type);
        }
        leftImage = level.getTile(leftUnit.data.pos).type.firingTexturesLeft.getRandomImage();
        rightImage = level.getTile(rightUnit.data.pos).type.firingTexturesRight.getRandomImage();
        shootTimerAttacking.startTimer();
        firingLeft = false;
        shootTimerAttacked.startTimer();
        firingRight = false;
        finished = false;
        leftHit = false;
        rightHit = false;
        rightShieldStarted = false;
        leftShieldStarted = false;
        leftProjectiles.clear();
        rightProjectiles.clear();
        overlayTimer.setReversed(false);
        overlayTimer.startTimer();
        hitPointBarLeft = new UIHitPointBar(0.2f, 20, 1.5f, 0.2f, leftUnit).setFill(leftUnit.data.renderHP).barOnly();
        hitPointBarBorderLeft = new UIHitPointBar(0.2f, 20, 1.5f, 0.2f, leftUnit).setFill(leftUnit.data.renderHP).borderOnly();
        shieldHitPointBarLeft = new UIHitPointBar(0.2f, 20, 1.5f, 0.2f, (int) leftUnit.stats.maxShieldHP(), UIColourTheme.LIGHT_BLUE).barOnly().setFill(leftUnit.data.shieldRenderHP);
        hitPointBarRight = new UIHitPointBar(0.2f, 20, 1.5f, 0.2f, rightUnit).setFill(rightUnit.data.renderHP).barOnly();
        hitPointBarBorderRight = new UIHitPointBar(0.2f, 20, 1.5f, 0.2f, rightUnit).setFill(rightUnit.data.renderHP).borderOnly();
        shieldHitPointBarRight = new UIHitPointBar(0.2f, 20, 1.5f, 0.2f, (int) rightUnit.stats.maxShieldHP(), UIColourTheme.LIGHT_BLUE).barOnly().setFill(rightUnit.data.shieldRenderHP);
        preRender();
    }

    @Override
    public void delete() {
        super.delete();
        leftUnit = null;
        rightUnit = null;
        defendingUnit = null;
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
        handler = null;
    }

    public static Supplier<ObjPos[]> THREE_UNITS = () -> new ObjPos[]{
            new ObjPos(17, Renderable.top() * 0.5f),
            new ObjPos(14, Renderable.top() * 0.8f),
            new ObjPos(12, Renderable.top() * 0.2f)
    };

    public static Supplier<ObjPos[]> TWO_UNITS = () -> new ObjPos[]{
            new ObjPos(14, Renderable.top() * 0.7f),
            new ObjPos(17, Renderable.top() * 0.3f)
    };

    public static Supplier<ObjPos[]> TWO_UNITS_BACK = () -> new ObjPos[]{
            new ObjPos(12, Renderable.top() * 0.7f),
            new ObjPos(15, Renderable.top() * 0.3f)
    };

    public static Supplier<ObjPos[]> ONE_UNIT = () -> new ObjPos[]{
            new ObjPos(15, Renderable.top() * 0.5f),
    };

    public static float estimatedAnimationTime(boolean showAnim) {
        return showAnim ? 7.5f : 2f;
    }

    private static class UnitRenderer implements Renderable {
        private final ImageSequenceAnim shield;
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
        private final LerpAnimation shieldTimer;
        private final UnitType type;
        public boolean renderShield = false;

        private UnitRenderer(ImageCounter unit, WeaponInstance weapon, WeaponInstance.FireAnimState fireAnimState, ArrayList<Projectile> projectiles, boolean toBeDestroyed, boolean isAttacking, float x, float y, UnitType type) {
            this.unit = unit;
            this.weapon = weapon;
            this.fireState = fireAnimState;
            this.projectiles = projectiles;
            this.toBeDestroyed = toBeDestroyed;
            this.isAttacking = isAttacking;
            this.x = x;
            this.y = y;
            this.type = type;
            shield = new ImageSequenceAnim(CachedImageSequence.SHIELD.get(), type.firingAnimShieldWidth, (float) (0.1f + Math.random() * 0.07f)).renderLastWhenFinished();
            shield.finish();
            if (fireAnimState == WeaponInstance.FireAnimState.EMPTY)
                unit.end();
            shieldTimer = new LerpAnimation((float) (0.25f + Math.random() * 0.1f));
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
                ArrayList<Projectile> spawnedProjectiles = spawner.getSpawnedProjectiles(x + xOffset(), y + yOffset(), type.firingAnimUnitWidth);
                if (fireState == WeaponInstance.FireAnimState.FIRE)
                    unit.increment(spawnedProjectiles.size());
                projectiles.addAll(spawnedProjectiles);
            }
            if (shakeDuration != null && !shakeDuration.finished()) {
                if (shieldTimer.finished()) {
                    shieldTimer.startTimer();
                    shield.start();
                }
                if ((int) (shakeDuration.normalisedProgress() * 10) % 2 == 0) {
                    g.translate(0.2, 0.2);
                }
            }
            g.translate(xOffset(), yOffset());
            unit.render(g, type.firingAnimUnitWidth);
            if (renderShield)
                shield.render(g);
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
