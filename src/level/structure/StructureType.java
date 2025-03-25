package level.structure;

import foundation.MainPanel;
import foundation.NamedEnum;
import render.texture.AssetManager;
import render.texture.ImageRenderer;
import render.texture.ResourceLocation;
import unit.UnitTeam;

import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.util.HashMap;

public enum StructureType implements NamedEnum {
    BASE("structures/base/base_", "Base", true, true, 3, 60, 2, true, false);

    public static final RescaleOp op = new RescaleOp(0.7f, 0, null);
    private final String displayName;
    private final HashMap<UnitTeam, ImageRenderer> renderers = new HashMap<>();
    private final HashMap<UnitTeam, ImageRenderer> lightRenderers = new HashMap<>();
    public final boolean canBeCapturedByDefault, destroyedOnCapture;
    public final int captureSteps, energyIncome;
    public final float unitRegen;
    public final boolean resupply;
    public final String path;
    public final boolean hasNeutral;

    StructureType(String path, String displayName, boolean canBeCapturedByDefault, boolean destroyedOnCapture, int captureSteps, int energyIncome, float unitRegen, boolean resupply, boolean hasNeutral) {
        this.path = path;
        this.displayName = displayName;
        this.canBeCapturedByDefault = canBeCapturedByDefault;
        this.destroyedOnCapture = destroyedOnCapture;
        this.captureSteps = captureSteps;
        this.energyIncome = energyIncome;
        this.unitRegen = unitRegen;
        this.resupply = resupply;
        this.hasNeutral = hasNeutral;
    }

    public ImageRenderer getImage(UnitTeam team) {
        return renderers.get(team);
    }

    public ImageRenderer getLightImage(UnitTeam team) {
        return lightRenderers.get(team);
    }

    public static void init() {
        MainPanel.setLoadBarEnabled(true);
        StructureType[] values = values();
        for (int i = 0; i < values.length; i++) {
            StructureType type = values[i];
            for (UnitTeam team : UnitTeam.ORDERED_TEAMS) {
                loadImage(new ResourceLocation(type.path + team.s + ".png"), team, type);
            }
            loadImage(new ResourceLocation(type.path + "none.png"), null, type);
            MainPanel.setLoadBarProgress((i + 1f) / values.length);
        }
        MainPanel.setLoadBarEnabled(false);
    }

    private static void loadImage(ResourceLocation location, UnitTeam team, StructureType type) {
        BufferedImage image = AssetManager.getImage(location, true), darkImage = op.createCompatibleDestImage(image, null);
        op.filter(image, darkImage);
        type.renderers.put(team, ImageRenderer.renderImageCentered(darkImage, true));
        type.lightRenderers.put(team, ImageRenderer.renderImageCentered(image, true));
    }

    @Override
    public String getName() {
        return displayName;
    }
}
