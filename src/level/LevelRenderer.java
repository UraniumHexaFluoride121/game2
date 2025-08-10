package level;

import foundation.MainPanel;
import foundation.input.InputType;
import foundation.math.ObjPos;
import level.energy.EnergyManager;
import level.tile.Tile;
import level.tutorial.TutorialElement;
import level.tutorial.TutorialManager;
import level.tutorial.sequence.event.EventAnim;
import render.*;
import render.level.FiringRenderer;
import render.level.GameEndScreen;
import render.level.LocalNextPlayerScreen;
import render.level.PauseMenu;
import render.level.info.*;
import render.level.map.LevelMapUI;
import render.level.tile.RenderElement;
import render.level.ui.UIDamage;
import render.level.ui.UIEndTurn;
import render.level.ui.UITurnBox;
import render.types.input.button.LevelUIButton;
import render.types.input.button.LevelUIShapeButton;
import render.types.input.button.UIButton;
import render.types.input.button.UIShapeButton;
import render.types.text.AbstractUITooltip;
import render.types.text.UITextNotification;
import unit.Unit;
import unit.UnitTeam;
import unit.action.Action;
import unit.weapon.WeaponInstance;

import java.awt.*;
import java.util.HashMap;

public class LevelRenderer extends AbstractLevelRenderer<Level> {
    private final GameRenderer firingAnimRenderer;

    public FiringRenderer firingRenderer;
    public UIUnitInfo uiUnitInfo;

    public LevelRenderer(Level level) {
        super(level);
        firingAnimRenderer = new GameRenderer(MainPanel.windowTransform, null);
    }

    public UIConfirm confirm;
    public UITextNotification onNextTurn, onTeamEliminated;
    public UITurnBox turnBox;
    public UIDamage damageUI;
    public UIDamageModifiers damageModifiers;
    public UIMovementModifiers movementModifiers;
    public UIWeaponEffectivenessInfo weaponEffectivenessInfo;
    public UIEndTurn endTurn;
    public UITileInfo tileInfo;
    public UIButton exitActionButton;
    public UnitInfoScreen unitInfoScreen;
    public DamageModifierInfo damageModifierInfo;
    public MovementModifierInfo movementModifierInfo;
    public EnergyManager energyManager;
    public UIShapeButton pauseMenuButton, mapButton;
    public PauseMenu pauseMenu;
    public LevelMapUI mapUI;
    public GameEndScreen endScreen;
    public LocalNextPlayerScreen nextPlayerScreen;

    @Override
    public void createRenderers() {
        super.createRenderers();
        if (MainPanel.BOT_DEBUG_RENDER != null) {
            new RenderElement(mainRenderer, RenderOrder.TILE_HIGHLIGHT, g -> {
                if (level.bots.get(level.getActiveTeam())) {
                    if (MainPanel.BOT_DEBUG_RENDER_UNIT) {
                        if (level.botHandlerMap.get(level.getActiveTeam()).unitDebugData != null)
                            level.botHandlerMap.get(level.getActiveTeam()).unitDebugData.render(g);
                    } else
                        level.botHandlerMap.get(level.getActiveTeam()).tileData.get(MainPanel.BOT_DEBUG_RENDER).render(g);
                }
            });
        }

        //Action render below units
        new RenderElement(mainRenderer, RenderOrder.ACTION_BELOW_UNITS, g -> {
            level.unitSet.forEach(u -> u.renderActionBelowUnits(g));
        });

        //Units
        new RenderElement(mainRenderer, RenderOrder.TILE_UNITS, g -> {
            level.unitSet.forEach(u -> {
                if (!u.isRenderFoW())
                    u.renderTile(g);
            });
        });

        //Capture bar
        new RenderElement(mainRenderer, RenderOrder.CAPTURE_BAR, g -> {
            for (Tile tile : level.tileSelector.tileSet) {
                tile.renderCaptureBar(g, level);
            }
        });

        //Action render below units
        new RenderElement(mainRenderer, RenderOrder.ACTION_ABOVE_UNITS, g -> {
            level.unitSet.forEach(u -> u.renderActionAboveUnits(g));
        });

        //Action UI
        new RenderElement(mainRenderer, RenderOrder.ACTION_SELECTOR, g -> {
            if (level.selectedUnit != null)
                level.selectedUnit.renderActions(g);
        });

        //Damage UI
        damageUI = new UIDamage(mainRenderer, RenderOrder.DAMAGE_UI);
        damageModifiers = new UIDamageModifiers(levelUIRenderer, level.buttonRegister, level);
        damageModifiers.setZOrder(-5);
        movementModifiers = new UIMovementModifiers(levelUIRenderer, level.buttonRegister, level);
        movementModifiers.setZOrder(-5);
        weaponEffectivenessInfo = new UIWeaponEffectivenessInfo(levelUIRenderer, level.buttonRegister, level);
        weaponEffectivenessInfo.setZOrder(-5).setEnabled(false);

        new RenderElement(mainRenderer, RenderOrder.ENERGY_COST_INDICATOR, g -> {
            level.unitSet.forEach(u -> u.renderEnergyCostIndicator(g));
        });

        uiUnitInfo = new UIUnitInfo(levelUIRenderer, level.buttonRegister, RenderOrder.LEVEL_UI, level);

        tileInfo = new UITileInfo(levelUIRenderer, level.buttonRegister, RenderOrder.LEVEL_UI, Renderable.right() - 15, .5f, level);
        tileInfo.setEnabled(false);

        endTurn = new UIEndTurn(levelUIRenderer, RenderOrder.LEVEL_UI, level);
        level.buttonRegister.register(endTurn);

        confirm = new UIConfirm(levelUIRenderer, RenderOrder.CONFIRM_UI, level);
        level.buttonRegister.register(confirm);

        onNextTurn = new UITextNotification(levelUIRenderer, RenderOrder.LEVEL_UI, level, 20, 3);
        onTeamEliminated = new UITextNotification(levelUIRenderer, RenderOrder.LEVEL_UI, level, 25, 2f);
        onTeamEliminated.translate(0, -3);
        turnBox = new UITurnBox(levelUIRenderer, RenderOrder.LEVEL_UI, level);
        level.buttonRegister.register(turnBox);

        exitActionButton = new LevelUIButton(levelUIRenderer, level.buttonRegister, RenderOrder.LEVEL_UI,
                0.5f, Renderable.top() - 4.5f, 7, 1.5f, 0.9f, false, level, () -> level.tileSelector.deselectAction()) {
            @Override
            public boolean isEnabled() {
                return super.isEnabled() && TutorialManager.isEnabled(TutorialElement.ACTION_DESELECT);
            }
        }
                .setText("Exit Action").setBold().setColourTheme(UIColourTheme.DEEP_RED).setEnabled(false);

        firingRenderer = new FiringRenderer(firingAnimRenderer, RenderOrder.BACKGROUND, level);

        mapButton = new LevelUIShapeButton(levelUIRenderer, level.buttonRegister, RenderOrder.LEVEL_UI,
                36.5f, Renderable.top() - 2.5f, 2, 2, false, level)
                .setShape(UIShapeButton::map).setOnClick(() -> {
                    mapUI.setEnabled(true);
                    mapUI.update();
                }).tooltip(t -> t.add(-1, AbstractUITooltip.dark(), "Open map"));
        mapUI = new LevelMapUI(levelUIRenderer, level.buttonRegister, level);
        mapUI.setEnabled(false);

        endScreen = new GameEndScreen(levelUIRenderer, level.buttonRegister, level);
        endScreen.setEnabled(false);

        nextPlayerScreen = new LocalNextPlayerScreen(levelUIRenderer, level.buttonRegister, level);
        nextPlayerScreen.setEnabled(false);

        unitInfoScreen = new UnitInfoScreen(levelUIRenderer, level.buttonRegister, level);
        unitInfoScreen.setEnabled(false);
        damageModifierInfo = new DamageModifierInfo(levelUIRenderer, level.buttonRegister, level);
        movementModifierInfo = new MovementModifierInfo(levelUIRenderer, level.buttonRegister, level);

        energyManager = new EnergyManager(levelUIRenderer, level.buttonRegister, RenderOrder.LEVEL_UI, Renderable.right() / 2 - 5, Renderable.top() - 3.5f, level);

        pauseMenuButton = new LevelUIShapeButton(levelUIRenderer, level.buttonRegister, RenderOrder.LEVEL_UI,
                0.5f, Renderable.top() - 2.5f, 2, 2, false, level)
                .setShape(UIShapeButton::threeLines).drawShape(0.15f).setOnClick(() -> {
                    pauseMenu.setEnabled(true);
                });
        pauseMenu = new PauseMenu(levelUIRenderer, level.buttonRegister, RenderOrder.PAUSE_MENU, level);
        pauseMenu.setEnabled(false);
    }

    @Override
    protected void renderSelectedTileHighlight(Tile tile, Graphics2D g) {
        if (level.hasActiveAction())
            return;
        super.renderSelectedTileHighlight(tile, g);
    }

    @Override
    protected void renderMouseHoverTileHighlight(Tile tile, Graphics2D g) {
        Action action = level.getActiveAction();
        Color c = null;
        if (action == Action.MOVE) {
            c = level.selectedUnit.movePathValid() ? Unit.MOVE_TILE_BORDER_COLOUR : Unit.INVALID_MOVE_TILE_BORDER_COLOUR;
        } else if (action == Action.RESUPPLY) {
            c = Unit.RESUPPLY_TILE_FLASH;
        } else if (action == Action.REPAIR) {
            c = Unit.REPAIR_TILE_BORDER_COLOUR;
        }
        if (c != null)
            tile.renderTile(g, c, Tile.SEGMENTED_BORDER_HIGHLIGHT_RENDERER);
        else
            super.renderMouseHoverTileHighlight(tile, g);
    }

    @Override
    protected boolean renderMouseOverTile() {
        return level.getActiveAction() != Action.FIRE;
    }

    @Override
    public void acceptPressed(InputType type) {
        if (!isFiring())
            super.acceptPressed(type);
    }

    @Override
    public void acceptReleased(InputType type) {
        super.acceptReleased(type);
    }

    public final HashMap<UnitTeam, ObjPos> lastCameraPos = new HashMap<>();

    public void setLastCameraPos(UnitTeam team) {
        if (team == null)
            return;
        lastCameraPos.put(team, getCameraInterpBlockPos(cameraPosition.copy()));
    }

    public void useLastCameraPos(UnitTeam team, boolean instant) {
        ObjPos pos = lastCameraPos.get(team);
        if (pos == null)
            return;
        if (instant)
            setCameraBlockPosInstant(pos);
        else
            setCameraInterpBlockPos(pos);
    }

    @Override
    public void delete() {
        super.delete();
        firingAnimRenderer.delete();
    }

    @Override
    public void render(Graphics2D g) {
        if (!isFiring() || firingRenderer.showLevel()) {
            super.render(g);
        }
        if (isFiring()) {
            firingAnimRenderer.render(g);
        }
    }

    private boolean isFiring = false;

    public boolean isFiring() {
        return isFiring;
    }

    public void beginFiring(Unit attacking, Unit defending, WeaponInstance weaponA, WeaponInstance weaponB) {
        isFiring = true;
        registerAnimBlock(firingAnimRenderer);
        firingRenderer.start(attacking, defending, weaponA, weaponB);
    }

    public void endFiring(Unit attacking, Unit defending) {
        isFiring = false;
        attacking.postFiring(defending, true, true);
        defending.postFiring(attacking, false, true);
        defending.tileFlash(Unit.ATTACK_TILE_FLASH);
        removeAnimBlock(firingAnimRenderer);
    }

    @Override
    public void animStateUpdate(boolean running) {
        TutorialManager.acceptEvent(new EventAnim(level, running));
    }
}
