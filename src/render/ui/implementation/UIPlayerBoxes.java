package render.ui.implementation;

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
        for (PlayerBox box : boxes) {
            if (team == null)
                team = box.playerTeamSelector.getValue();
            else if (box.playerTeamSelector.getValue() != team) {
                verified = true;
                break;
            }
        }
        if (boxes.size() <= 1)
            verified = false;
        if (MainPanel.titleScreen.newGameTabs != null && MainPanel.titleScreen.newGameTabs.enabled) {
            MainPanel.titleScreen.startLanGame.setEnabled(verified);
            MainPanel.titleScreen.startLocalGame.setEnabled(verified);
            UITextDisplayBox box = MainPanel.titleScreen.gameCannotBeStarted;
            box.setEnabled(!verified);
            if (boxes.size() <= 1)
                box.setText("At least 2 players are needed to start game");
            else
                box.setText("At least 2 teams are needed to start game");
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
        if (!enabled)
            return;
        blocking = !blocked && internal.acceptInput(pos.copy(), type, true);
    }

    @Override
    public void buttonReleased(ObjPos pos, boolean inside, boolean blocked, InputType type) {
        if (!enabled)
            return;
        blocking = !blocked && internal.acceptInput(pos.copy(), type, false);
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
        public final UIEnumSelector<PlayerTeam> playerTeamSelector;
        private ButtonRegister parent, internal = new ButtonRegister();
        private int index;
        private final float x, y;
        private final UITextLabel playerTeamLabel;

        public PlayerBox(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, int index, float x, float y, UIPlayerBoxes parentContainer) {
            super(register, order);
            this.x = x;
            this.y = y;
            numberBox = new UITextDisplayBox(null, RenderOrder.NONE, 0.1f, 1, 2, 2, 1.7f);
            mainBox = new UIBox(13, 3.5f, 0, UIBox.BoxShape.RECTANGLE);
            playerTeamSelector = new UIEnumSelector<>(null, internal, RenderOrder.NONE, ButtonOrder.MAIN_BUTTONS, 5, 0.7f, 1.3f, 2, PlayerTeam.class, PlayerTeam.values()[index])
                    .setOnChanged(parentContainer::verifyTeams);
            playerTeamLabel = new UITextLabel(5, 0.7f, false)
                    .updateTextCenter("Player Team").setTextCenterBold();
            deleteButton = new UIShapeButton(null, internal, RenderOrder.NONE, ButtonOrder.MAIN_BUTTONS, 14.5f, 2.25f, 1, 1, false, () -> {
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
                        GameRenderer.renderOffset(5.2f, 2.3f, g, () -> {
                            playerTeamLabel.render(g);
                        });
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
            if (!enabled)
                return;
            blocking = !blocked && internal.acceptInput(pos.copy().subtract(0, 4).multiply(1, -1).subtract(x, -index * 4 + y), type, true);
        }

        @Override
        public void buttonReleased(ObjPos pos, boolean inside, boolean blocked, InputType type) {
            if (!enabled)
                return;
            blocking = !blocked && internal.acceptInput(pos.copy().subtract(0, 4).multiply(1, -1).subtract(x, -index * 4 + y), type, false);
        }
    }
}
