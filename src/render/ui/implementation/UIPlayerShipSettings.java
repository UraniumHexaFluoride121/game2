package render.ui.implementation;

import foundation.MainPanel;
import foundation.input.ButtonOrder;
import foundation.input.ButtonRegister;
import render.OrderedRenderable;
import render.RenderOrder;
import render.RenderRegister;
import render.renderables.RenderElement;
import render.texture.ImageRenderer;
import render.ui.UIColourTheme;
import render.ui.types.*;
import unit.UnitPose;
import unit.UnitTeam;
import unit.UnitType;

import java.util.ArrayList;
import java.util.HashMap;

public class UIPlayerShipSettings extends UIContainer {
    public static final HashMap<UnitType, Integer> DEFAULT_PRESET = new HashMap<>();

    static {
        DEFAULT_PRESET.put(UnitType.FIGHTER, 3);
        DEFAULT_PRESET.put(UnitType.BOMBER, 1);
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
        return UnitType.ORDERED_UNIT_TYPES.length * 3.8f + 1.2f;
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
                    new RenderElement(r, RenderOrder.TITLE_SCREEN_BUTTON_BACKGROUND, new UIBox(12, 3f).setColourTheme(UIColourTheme.LIGHT_BLUE_TRANSPARENT_CENTER))
                            .translate(3.5f, i * 3.8f);
                    new RenderElement(r, RenderOrder.TITLE_SCREEN_BUTTON_BACKGROUND, new UIImageBox(3, 3, ImageRenderer.renderImageCentered(UnitType.ORDERED_UNIT_TYPES[i].getImage(team, UnitPose.INFO), false))
                            .setColourTheme(UIColourTheme.LIGHT_BLUE_TRANSPARENT_CENTER))
                            .translate(0, i * 3.8f).setZOrder(1);
                    new UIContainer(r, b, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS, 0, i * 3.8f, (r2, b2) -> {
                        UINumberSelector selector = new UINumberSelector(r2, b2, RenderOrder.TITLE_SCREEN_BUTTONS, ButtonOrder.MAIN_BUTTONS, 6f, 1.15f, 1.3f, 3, 0, 10, 0);
                        selectors.put(UnitType.ORDERED_UNIT_TYPES[finalI], selector);
                        selector.scale(1, -1).translate(0, 3.8f);
                        selector.setOnChanged(() -> MainPanel.titleScreen.playerBoxes.verifyTeams());
                        new RenderElement(r2, RenderOrder.TITLE_SCREEN_BUTTONS, new UITextLabel(9, 0.7f, false, 0, 0.85f)
                                .updateTextCenter(UnitType.ORDERED_UNIT_TYPES[finalI].displayName + " Units").setTextCenterBold())
                                .scale(1, -1).translate(4.7f, 1);
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
        public void delete() {
            super.delete();
            selectors.clear();
        }
    }
}
