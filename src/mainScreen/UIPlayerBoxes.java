package mainScreen;

import foundation.MainPanel;
import foundation.input.ButtonOrder;
import foundation.input.ButtonRegister;
import foundation.input.InputType;
import foundation.input.RegisteredButtonInputReceiver;
import foundation.math.ObjPos;
import level.PlayerTeam;
import render.*;
import render.UIColourTheme;
import render.types.box.UIBox;
import render.types.text.AbstractUITooltip;
import render.types.text.UITextDisplayBox;
import render.types.text.UITextLabel;
import render.types.input.button.UIButton;
import render.types.input.UIEnumSelector;
import render.types.input.button.UIShapeButton;
import unit.UnitTeam;

import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static render.UIColourTheme.*;

public class UIPlayerBoxes extends AbstractRenderElement implements RegisteredButtonInputReceiver {
    private static final int MAX_PLAYERS = UnitTeam.ORDERED_TEAMS.length;
    private static final float BOX_SIZE = 5.5f;
    private ButtonOrder buttonOrder;
    private ButtonRegister buttonRegister, internal = new ButtonRegister();
    private GameRenderer renderer = new GameRenderer(new AffineTransform(), null);
    private final float x, y;
    private final HashSet<PlayerBox> removed = new HashSet<>();

    private final ArrayList<PlayerBox> boxes = new ArrayList<>();
    private UIShapeButton plus;
    private boolean locked = false;

    public UIPlayerBoxes(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, ButtonOrder buttonOrder, float x, float y) {
        super(register, order);
        this.buttonOrder = buttonOrder;
        this.buttonRegister = buttonRegister;
        this.x = x;
        this.y = y;
        if (buttonRegister != null) {
            buttonRegister.register(this);
        }
        addBox();
        addBox();
        renderable = g -> {
            if (!removed.isEmpty()) {
                removed.forEach(PlayerBox::delete);
                removed.clear();
            }
            renderer.render(g);
            if (MainPanel.titleScreen.playerBoxScrollWindow != null)
                MainPanel.titleScreen.playerBoxScrollWindow.setScrollMax(getScrollDistance());
        };
    }

    public HashMap<UnitTeam, PlayerTeam> getTeams() {
        HashMap<UnitTeam, PlayerTeam> teamMap = new HashMap<>();
        boxes.forEach(b -> {
            teamMap.put(UnitTeam.ORDERED_TEAMS[b.index], b.playerTeamSelector.getValue());
        });
        return teamMap;
    }

    public HashMap<UnitTeam, Boolean> getBots() {
        HashMap<UnitTeam, Boolean> botMap = new HashMap<>();
        boxes.forEach(b -> {
            botMap.put(UnitTeam.ORDERED_TEAMS[b.index], b.isBot);
        });
        return botMap;
    }

    public void setPlayerCount(int count) {
        if (count > getTeamCount()) {
            addBox();
            setPlayerCount(count);
        } else if (count < getTeamCount()) {
            deletePlayer(getTeamCount() - 1);
            setPlayerCount(count);
        }
    }

    public void verifyTeams() {
        PlayerTeam team = null;
        boolean verified = false;
        boolean playerTeamsVerified = false;
        for (PlayerBox box : boxes) {
            if (team == null)
                team = box.playerTeamSelector.getValue();
            else if (box.playerTeamSelector.getValue() != team) {
                verified = true;
                playerTeamsVerified = true;
                break;
            }
        }
        if (boxes.size() <= 1)
            verified = false;
        TitleScreen t = MainPanel.titleScreen;
        boolean unitsVerified = t.playerShipSettings == null || t.playerShipSettings.verifyTeams();
        boolean noCustomMapSelected = t.customMap && !t.loadCustomBox.hasSelectedSave();
        boolean invalidMap = t.customMap && t.loadCustomBox.hasSelectedSave() && !t.loadCustomBox.getSelected().valid;
        boolean tooManyUnits = !t.customMap && t.playerShipSettings != null && t.widthSelector.getValue() * t.heightSelector.getValue() * 0.4f < t.playerShipSettings.unitCount();
        StructureGenerationPreset structurePreset = t.structureGenerationSettings.getPreset();
        boolean tooManyStructures = !t.customMap && structurePreset.capturedCount() * getTeamCount() + structurePreset.neutralCount() > t.widthSelector.getValue() * t.heightSelector.getValue() * 0.1f;
        if (!unitsVerified || noCustomMapSelected || invalidMap || tooManyUnits || tooManyStructures)
            verified = false;
        if (t.multiplayerTabs != null && t.multiplayerTabs.isEnabled()) {
            t.startLanGame.setEnabled(verified);
            t.startLocalGame.setEnabled(verified);
            UITextDisplayBox box = t.gameCannotBeStarted;
            box.setEnabled(!verified);
            if (noCustomMapSelected)
                box.setText("No custom map selected");
            else if (invalidMap)
                box.setText("The selected map has an invalid layout");
            else if (tooManyUnits)
                box.setText("Map is too small for this many units");
            else if (tooManyStructures)
                box.setText("Map is too small for this many structures");
            else if (boxes.size() <= 1)
                box.setText("At least 2 players are needed to start game");
            else if (!playerTeamsVerified)
                box.setText("At least 2 teams are needed to start game");
            else if (!unitsVerified)
                box.setText("Every player must start with at least one unit");
        }
    }

    public int getTeamCount() {
        return boxes.size();
    }

    private void addBox() {
        boxes.add(new PlayerBox(renderer, internal, RenderOrder.TITLE_SCREEN_BUTTONS, boxes.size(), x, y, this));
        updatePlus();
    }

    public void lockPlayers() {
        locked = true;
        if (MainPanel.titleScreen.loadCustomBox.hasSelectedSave())
            setPlayerCount(MainPanel.titleScreen.loadCustomBox.getSelected().playerCount);
        updatePlus();
        boxes.forEach(b -> b.updateLock(true));
    }

    public void unlockPlayers() {
        locked = false;
        updatePlus();
        boxes.forEach(b -> b.updateLock(false));
    }

    private void updatePlus() {
        verifyTeams();
        if (plus != null)
            plus.delete();
        if (boxes.size() != MAX_PLAYERS && !MainPanel.titleScreen.customMap)
            plus = new UIShapeButton(register, internal, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS, 5, boxes.size() * BOX_SIZE + (BOX_SIZE - 3) / 2f, 7, 3, false, this::addBox)
                    .setShape(UIShapeButton::plus).tooltip(t -> t.add(-1, AbstractUITooltip.light(), "Add player"));
    }

    public void deletePlayer(int index) {
        removed.add(boxes.remove(index));
        for (int i = 0; i < boxes.size(); i++) {
            boxes.get(i).update(i);
        }
        updatePlus();
    }

    public float getScrollDistance() {
        return ((boxes.size() == MAX_PLAYERS || MainPanel.titleScreen.customMap) ? boxes.size() : (boxes.size() + 1)) * BOX_SIZE - MainPanel.titleScreen.playerBoxScrollWindow.height;
    }

    public UnitTeam getEditShipsTeam() {
        for (PlayerBox b : boxes) {
            if (b.editShips.isSelected())
                return UnitTeam.ORDERED_TEAMS[b.index];
        }
        return null;
    }

    @Override
    public boolean posInside(ObjPos pos, InputType type) {
        return true;
    }

    private boolean blocking = false;

    @Override
    public boolean blocking(InputType type) {
        return blocking;
    }

    @Override
    public ButtonOrder getButtonOrder() {
        return buttonOrder;
    }

    @Override
    public void buttonPressed(ObjPos pos, boolean inside, boolean blocked, InputType type) {
        if (type == InputType.TAB_ON_SWITCH_TO) {
            boxes.forEach(b -> b.editShips.deselect());
            return;
        }
        if (!isEnabled())
            return;
        blocking = internal.acceptInput(pos.copy(), type, true, blocked);
    }

    @Override
    public void buttonReleased(ObjPos pos, boolean inside, boolean blocked, InputType type) {
        if (!isEnabled())
            return;
        blocking = internal.acceptInput(pos.copy(), type, false, blocked);
    }

    @Override
    public void delete() {
        super.delete();
        if (buttonRegister != null) {
            buttonRegister.remove(this);
            buttonRegister = null;
        }
        removed.clear();
        boxes.clear();
        renderer.delete();
    }

    private static class PlayerBox extends AbstractRenderElement implements RegisteredButtonInputReceiver {
        private final UITextDisplayBox numberBox;
        private final UIBox mainBox;
        private final UIShapeButton deleteButton;
        private UIButton editShips, enableBot;
        public final UIEnumSelector<PlayerTeam> playerTeamSelector;
        private ButtonRegister parent, internal = new ButtonRegister();
        private int index;
        private final float x, y;
        private final UITextLabel playerTeamLabel;
        public boolean isBot = false;

        public PlayerBox(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, int index, float x, float y, UIPlayerBoxes parentContainer) {
            super(register, order);
            this.x = x;
            this.y = y;
            numberBox = new UITextDisplayBox(null, RenderOrder.NONE, 0.1f, (BOX_SIZE - 2) / 2f, 2, 2, 1.7f)
                    .setBold();
            mainBox = new UIBox(13, BOX_SIZE - .5f, 0, UIBox.BoxShape.RECTANGLE);
            mainBox.translate(0, -2);
            playerTeamSelector = new UIEnumSelector<>(null, internal, RenderOrder.NONE, ButtonOrder.MAIN_BUTTONS, 4, 0.7f, 1.3f, 2, PlayerTeam.class, PlayerTeam.values()[index])
                    .setOnChanged(parentContainer::verifyTeams).tooltip(t -> t.add(9, AbstractUITooltip.light(), "Players on the same team are allies, share map vision and win together."));
            editShips = new UIButton(null, internal, RenderOrder.NONE, ButtonOrder.MAIN_BUTTONS, 10.5f, 0.7f, 5, 1.3f, 0.7f, true)
                    .setBold().noDeselect().setBoxCorner(0.35f).setText("Edit Units").setOnClick(() -> {
                        parentContainer.boxes.forEach(b -> {
                            if (b != this)
                                b.editShips.deselect();
                        });
                        editShips.setColourTheme(ALWAYS_GREEN_SELECTED);
                    }).setOnDeselect(() -> {
                        MainPanel.titleScreen.playerShipSettings.updateTeam();
                        editShips.setColourTheme(parentContainer.locked ? GRAYED_OUT : GREEN_SELECTED);
                    })
                    .setColourTheme(GREEN_SELECTED).toggleMode().tooltip(t -> t.add(8, AbstractUITooltip.light(), "Modify the starting units for this player"));
            enableBot = new UIButton(null, internal, RenderOrder.NONE, ButtonOrder.MAIN_BUTTONS, 10.5f, 2.3f, 5, 1f, 0.7f, false);
            enableBot.setBold().noDeselect().setBoxCorner(0.35f).setText("Bot (off)").setOnClick(() -> {
                        isBot = !isBot;
                        enableBot.setColourTheme(isBot ? GREEN : RED);
                        enableBot.setText(isBot ? "Bot (on)" : "Bot (off)");
                    })
                    .setColourTheme(RED).tooltip(t -> t.add(9, AbstractUITooltip.light(), "If enabled, this player will be controlled by a bot. The host cannot be a bot."));
            playerTeamLabel = new UITextLabel(5, 0.7f, false)
                    .updateTextCenter("Player Team").setTextCenterBold();
            deleteButton = new UIShapeButton(null, internal, RenderOrder.NONE, ButtonOrder.MAIN_BUTTONS, 14.75f, BOX_SIZE - 1.5f, 1, 1, false, () -> {
                parentContainer.deletePlayer(getIndex());
            }).setShape(UIShapeButton::x).setBoxCorner(0.2f).setColourTheme(DEEP_RED);
            parent = buttonRegister;
            parent.register(this);
            update(index);
            renderable = g -> {
                GameRenderer.renderOffset(0, getIndex() * BOX_SIZE, g, () -> {
                    GameRenderer.renderOffsetScaled(0, BOX_SIZE, 1, -1, g, () -> {
                        numberBox.render(g);
                        GameRenderer.renderOffset(3, 0.25f, g, () -> {
                            mainBox.render(g);
                        });
                        GameRenderer.renderOffset(4.2f, 2.3f, g, () -> {
                            playerTeamLabel.render(g);
                        });
                        editShips.render(g);
                        enableBot.render(g);
                        playerTeamSelector.render(g);
                        deleteButton.render(g);
                    });
                });
            };
        }

        public int getIndex() {
            return index;
        }

        public void update(int index) {
            this.index = index;
            if (index == 0 && isBot)
                enableBot.runOnCLick();
            if (index == 0) {
                enableBot.setText("Host");
                enableBot.setColourTheme(UIColourTheme.GRAYED_OUT);
            }
            enableBot.setClickEnabled(index != 0);
            numberBox.setText(String.valueOf(index + 1))
                    .setColourTheme(UnitTeam.ORDERED_TEAMS[index].uiColour);
            mainBox.setColourTheme(UnitTeam.ORDERED_TEAMS[index].uiColour);
        }

        public void updateLock(boolean lock) {
            deleteButton.setColourTheme(lock ? GRAYED_OUT : DEEP_RED)
                    .setClickEnabled(!lock);
            if (lock && editShips.isSelected()) {
                editShips.deselect();
            }
            editShips.setColourTheme(lock ? GRAYED_OUT : editShips.isSelected() ? ALWAYS_GREEN_SELECTED : GREEN_SELECTED)
                    .setClickEnabled(!lock);
        }

        @Override
        public void delete() {
            super.delete();
            internal.delete();
            playerTeamSelector.delete();
            numberBox.delete();
            parent.remove(this);
            parent = null;
            deleteButton.delete();
            editShips.delete();
            editShips = null;
            enableBot.delete();
            enableBot = null;
        }

        @Override
        public boolean posInside(ObjPos pos, InputType type) {
            return true;
        }

        private boolean blocking = false;

        @Override
        public boolean blocking(InputType type) {
            return blocking;
        }

        @Override
        public ButtonOrder getButtonOrder() {
            return ButtonOrder.MAIN_BUTTONS;
        }

        @Override
        public void buttonPressed(ObjPos pos, boolean inside, boolean blocked, InputType type) {
            if (!isEnabled())
                return;
            blocking = internal.acceptInput(pos.copy().subtract(0, BOX_SIZE).multiply(1, -1).subtract(x, -index * BOX_SIZE + y), type, true, blocked);
        }

        @Override
        public void buttonReleased(ObjPos pos, boolean inside, boolean blocked, InputType type) {
            if (!isEnabled())
                return;
            blocking = internal.acceptInput(pos.copy().subtract(0, BOX_SIZE).multiply(1, -1).subtract(x, -index * BOX_SIZE + y), type, false, blocked);
        }
    }
}
