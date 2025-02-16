package mainScreen;

import foundation.MainPanel;
import foundation.input.ButtonOrder;
import foundation.input.ButtonRegister;
import foundation.input.InputType;
import foundation.input.RegisteredButtonInputReceiver;
import foundation.math.ObjPos;
import level.PlayerTeam;
import render.*;
import render.ui.UIColourTheme;
import render.ui.types.*;
import unit.UnitTeam;

import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class UIPlayerBoxes extends AbstractRenderElement implements RegisteredButtonInputReceiver {
    private static final int MAX_PLAYERS = UnitTeam.ORDERED_TEAMS.length;
    private ButtonOrder buttonOrder;
    private ButtonRegister buttonRegister, internal = new ButtonRegister();
    private GameRenderer renderer = new GameRenderer(new AffineTransform(), null);
    private final float x, y;
    private final HashSet<PlayerBox> removed = new HashSet<>();

    private final ArrayList<PlayerBox> boxes = new ArrayList<>();
    private UIShapeButton plus;

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
            removed.forEach(PlayerBox::delete);
            removed.clear();
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
        boolean unitsVerified = MainPanel.titleScreen.playerShipSettings == null || MainPanel.titleScreen.playerShipSettings.verifyTeams();
        if (!unitsVerified)
            verified = false;
        if (MainPanel.titleScreen.newGameTabs != null && MainPanel.titleScreen.newGameTabs.isEnabled()) {
            MainPanel.titleScreen.startLanGame.setEnabled(verified);
            MainPanel.titleScreen.startLocalGame.setEnabled(verified);
            UITextDisplayBox box = MainPanel.titleScreen.gameCannotBeStarted;
            box.setEnabled(!verified);
            if (boxes.size() <= 1)
                box.setText("At least 2 players are needed to start game");
            else if (!playerTeamsVerified)
                box.setText("At least 2 teams are needed to start game");
            else if (!unitsVerified)
                box.setText("Every player must start with at least one unit");
        }
    }

    public boolean getVerifyTeams() {
        PlayerTeam team = null;
        boolean verified = false;
        for (PlayerBox box : boxes) {
            if (team == null)
                team = box.playerTeamSelector.getValue();
            else if (box.playerTeamSelector.getValue() != team) {
                verified = true;
                break;
            }
        }
        return verified;
    }

    public int getTeamCount() {
        return boxes.size();
    }

    private void addBox() {
        boxes.add(new PlayerBox(renderer, internal, RenderOrder.TITLE_SCREEN_BUTTONS, boxes.size(), x, y, this));
        createPlus();
    }

    private void createPlus() {
        verifyTeams();
        if (plus != null)
            plus.delete();
        if (boxes.size() != MAX_PLAYERS)
            plus = new UIShapeButton(register, internal, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS, 5, boxes.size() * 4 + 0.5f, 7, 3, false, this::addBox)
                    .setShape(UIShapeButton::plus);
    }

    public void deletePlayer(int index) {
        removed.add(boxes.remove(index));
        for (int i = 0; i < boxes.size(); i++) {
            boxes.get(i).update(i);
        }
        createPlus();
    }

    public float getScrollDistance() {
        return (boxes.size() == MAX_PLAYERS ? boxes.size() * 4 : (boxes.size() + 1) * 4) - 14;
    }

    public UnitTeam getEditShipsTeam() {
        for (PlayerBox b : boxes) {
            if (b.editShips.isSelected())
                return UnitTeam.ORDERED_TEAMS[b.index];
        }
        return null;
    }

    @Override
    public boolean posInside(ObjPos pos) {
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
        private final UIButton editShips;
        public final UIEnumSelector<PlayerTeam> playerTeamSelector;
        private ButtonRegister parent, internal = new ButtonRegister();
        private int index;
        private final float x, y;
        private final UITextLabel playerTeamLabel;

        public PlayerBox(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, int index, float x, float y, UIPlayerBoxes parentContainer) {
            super(register, order);
            this.x = x;
            this.y = y;
            numberBox = new UITextDisplayBox(null, RenderOrder.NONE, 0.1f, 1, 2, 2, 1.7f)
                    .setBold();
            mainBox = new UIBox(13, 3.5f, 0, UIBox.BoxShape.RECTANGLE);
            playerTeamSelector = new UIEnumSelector<>(null, internal, RenderOrder.NONE, ButtonOrder.MAIN_BUTTONS, 4, 0.7f, 1.3f, 2, PlayerTeam.class, PlayerTeam.values()[index])
                    .setOnChanged(parentContainer::verifyTeams);
            editShips = new UIButton(null, internal, RenderOrder.NONE, ButtonOrder.MAIN_BUTTONS, 10.5f, 0.7f, 5, 1.3f, 0.7f, true)
                    .setBold().noDeselect().setBoxCorner(0.35f).setText("Edit Units").setOnClick(() -> {
                        parentContainer.boxes.forEach(b -> {
                            if (b != this)
                                b.editShips.deselect();
                        });
                    }).setOnDeselect(() -> MainPanel.titleScreen.playerShipSettings.updateTeam());
            playerTeamLabel = new UITextLabel(5, 0.7f, false)
                    .updateTextCenter("Player Team").setTextCenterBold();
            deleteButton = new UIShapeButton(null, internal, RenderOrder.NONE, ButtonOrder.MAIN_BUTTONS, 14.75f, 2.5f, 1, 1, false, () -> {
                parentContainer.deletePlayer(getIndex());
            }).setShape(UIShapeButton::x).setBoxCorner(0.2f).setColourTheme(UIColourTheme.DEEP_RED);
            parent = buttonRegister;
            parent.register(this);
            update(index);
            renderable = g -> {
                GameRenderer.renderOffset(0, getIndex() * 4, g, () -> {
                    GameRenderer.renderOffsetScaled(0, 4, 1, -1, g, () -> {
                        numberBox.render(g);
                        GameRenderer.renderOffset(3, 0.25f, g, () -> {
                            mainBox.render(g);
                        });
                        GameRenderer.renderOffset(4.2f, 2.3f, g, () -> {
                            playerTeamLabel.render(g);
                        });
                        editShips.render(g);
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
            numberBox.setText(String.valueOf(index + 1))
                    .setColourTheme(UnitTeam.ORDERED_TEAMS[index].uiColour);
            mainBox.setColourTheme(UnitTeam.ORDERED_TEAMS[index].uiColour);
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
        }

        @Override
        public boolean posInside(ObjPos pos) {
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
            blocking = internal.acceptInput(pos.copy().subtract(0, 4).multiply(1, -1).subtract(x, -index * 4 + y), type, true, blocked);
        }

        @Override
        public void buttonReleased(ObjPos pos, boolean inside, boolean blocked, InputType type) {
            if (!isEnabled())
                return;
            blocking = internal.acceptInput(pos.copy().subtract(0, 4).multiply(1, -1).subtract(x, -index * 4 + y), type, false, blocked);
        }
    }
}
