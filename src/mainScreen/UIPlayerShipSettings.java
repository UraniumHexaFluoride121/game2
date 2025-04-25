package mainScreen;

import foundation.MainPanel;
import foundation.input.ButtonOrder;
import foundation.input.ButtonRegister;
import foundation.input.InputType;
import foundation.math.ObjPos;
import render.OrderedRenderable;
import render.RenderOrder;
import render.RenderRegister;
import render.level.tile.RenderElement;
import render.texture.ImageRenderer;
import render.UIColourTheme;
import render.types.box.UIBox;
import render.types.box.UIImageBox;
import render.types.text.UITextLabel;
import render.types.input.UINumberSelector;
import render.types.container.UIContainer;
import unit.UnitPose;
import unit.UnitTeam;
import unit.type.CorvetteType;
import unit.type.CruiserType;
import unit.type.FighterType;
import unit.type.UnitType;

import java.util.ArrayList;
import java.util.HashMap;

public class UIPlayerShipSettings extends UIContainer {
    public static final HashMap<UnitType, Integer> DEFAULT_PRESET = new HashMap<>();
    public static final float TOP_MARGIN = 11;
    private static final float SIZE = 4;

    static {
        DEFAULT_PRESET.put(FighterType.FIGHTER, 2);
        DEFAULT_PRESET.put(FighterType.BOMBER, 2);
        DEFAULT_PRESET.put(FighterType.SCOUT, 2);
        DEFAULT_PRESET.put(CorvetteType.CORVETTE, 2);
        DEFAULT_PRESET.put(CorvetteType.DEFENDER, 0);
        DEFAULT_PRESET.put(CorvetteType.ARTILLERY, 1);
        DEFAULT_PRESET.put(CorvetteType.SUPPLY, 1);
        DEFAULT_PRESET.put(CruiserType.CRUISER, 0);
    }

    private final HashMap<UnitTeam, TeamSettings> teamSettingsMap = new HashMap<>();
    public static HashMap<UnitType, Integer> clipboardPreset = null;
    public UnitTeam currentTeam = null;

    public UIPlayerShipSettings(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, ButtonOrder buttonOrder) {
        super(register, buttonRegister, order, buttonOrder, 1, 1);
        addRenderables((r, b) -> {
            for (UnitTeam team : UnitTeam.ORDERED_TEAMS) {
                TeamSettings teamSettings = new TeamSettings(r, b, team);
                teamSettingsMap.put(team, teamSettings);
            }
        });
        loadPreset(DEFAULT_PRESET);
    }

    public void updateTeam() {
        UnitTeam team = MainPanel.titleScreen.playerBoxes.getEditShipsTeam();
        teamSettingsMap.forEach((t, s) -> s.setEnabled(team == t));
        MainPanel.titleScreen.playerShipSettingsContainer.setEnabled(team != null);
        currentTeam = team;
    }

    public float scrollDistance() {
        return UnitType.ORDERED_UNIT_TYPES.length * (SIZE + .8f) + 1.2f;
    }

    public boolean verifyTeams() {
        for (int i = 0; i < MainPanel.titleScreen.playerBoxes.getTeamCount(); i++) {
            if (teamSettingsMap.get(UnitTeam.ORDERED_TEAMS[i]).getTypes().isEmpty())
                return false;
        }
        return true;
    }

    public HashMap<UnitTeam, ArrayList<UnitType>> getUnits() {
        HashMap<UnitTeam, ArrayList<UnitType>> units = new HashMap<>();
        for (int i = 0; i < MainPanel.titleScreen.playerBoxes.getTeamCount(); i++) {
            UnitTeam team = UnitTeam.ORDERED_TEAMS[i];
            units.put(team, teamSettingsMap.get(team).getTypes());
        }
        return units;
    }

    public int unitCount() {
        int count = 0;
        for (int i = 0; i < MainPanel.titleScreen.playerBoxes.getTeamCount(); i++) {
            count += teamSettingsMap.get(UnitTeam.ORDERED_TEAMS[i]).getTypes().size();
        }
        return count;
    }

    public void loadPreset(HashMap<UnitType, Integer> preset) {
        teamSettingsMap.forEach((team, teamSettings) -> {
            teamSettings.selectors.forEach((type, selector) -> {
                selector.setValue(preset.getOrDefault(type, 0), false);
            });
        });
    }

    public void loadPresetForCurrentTeam(HashMap<UnitType, Integer> preset) {
        if (currentTeam == null)
            return;
        teamSettingsMap.get(currentTeam).selectors.forEach((type, selector) -> {
            selector.setValue(preset.getOrDefault(type, 0), false);
        });
    }

    public HashMap<UnitType, Integer> getCurrentPreset() {
        if (currentTeam == null)
            return null;
        return teamSettingsMap.get(currentTeam).getPreset();
    }

    private static class TeamSettings extends UIContainer {
        public final UnitTeam team;
        public final HashMap<UnitType, UINumberSelector> selectors = new HashMap<>();

        public TeamSettings(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, UnitTeam team) {
            super(register, buttonRegister, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS, 0, 0);
            addRenderables((r, b) -> {
                for (int i = 0; i < UnitType.ORDERED_UNIT_TYPES.length; i++) {
                    int finalI = i;
                    new RenderElement(r, RenderOrder.TITLE_SCREEN_BUTTON_BACKGROUND, new UIBox(15 - SIZE, SIZE).setColourTheme(UIColourTheme.LIGHT_BLUE_TRANSPARENT_CENTER))
                            .translate(4.5f, i * (SIZE + .8f));
                    new RenderElement(r, RenderOrder.TITLE_SCREEN_BUTTON_BACKGROUND, new UIImageBox(SIZE, SIZE, ImageRenderer.renderImageCentered(UnitType.ORDERED_UNIT_TYPES[i].getImage(team, UnitPose.INFO), false))
                            .setColourTheme(UIColourTheme.LIGHT_BLUE_TRANSPARENT_CENTER))
                            .translate(0, i * (SIZE + .8f)).setZOrder(1);
                    new UIContainer(r, b, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS, 0, i * (SIZE + .8f), (r2, b2) -> {
                        UINumberSelector selector = new UINumberSelector(r2, b2, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS, 4.5f + SIZE / 2, 1.25f, 1.5f, 3, 0, 10, 0);
                        selectors.put(UnitType.ORDERED_UNIT_TYPES[finalI], selector);
                        selector.scale(1, -1).translate(0, (SIZE + .8f));
                        selector.setOnChanged(() -> MainPanel.titleScreen.playerBoxes.verifyTeams());
                        new RenderElement(r2, RenderOrder.TITLE_SCREEN_BUTTONS, new UITextLabel(9, 1.f, false, 0, 0.85f)
                                .updateTextCenter(UnitType.ORDERED_UNIT_TYPES[finalI].getName() + " Units").setTextCenterBold())
                                .scale(1, -1).translate(3.2f + SIZE / 2, 1.2f);
                    });
                }
            });
            this.team = team;
        }

        public ArrayList<UnitType> getTypes() {
            ArrayList<UnitType> list = new ArrayList<>();
            for (UnitType type : UnitType.ORDERED_UNIT_TYPES) {
                for (int i = 0; i < selectors.get(type).getValue(); i++) {
                    list.add(type);
                }
            }
            return list;
        }

        public HashMap<UnitType, Integer> getPreset() {
            HashMap<UnitType, Integer> preset = new HashMap<>();
            for (UnitType type : UnitType.ORDERED_UNIT_TYPES) {
                preset.put(type, selectors.get(type).getValue());
            }
            return preset;
        }

        @Override
        public void buttonPressed(ObjPos pos, boolean inside, boolean blocked, InputType type) {
            super.buttonPressed(pos.copy().addY(-0.8f), inside, blocked, type);
        }

        @Override
        public void buttonReleased(ObjPos pos, boolean inside, boolean blocked, InputType type) {
            super.buttonReleased(pos.copy().addY(-0.8f), inside, blocked, type);
        }

        @Override
        public void delete() {
            super.delete();
            selectors.clear();
        }
    }
}
