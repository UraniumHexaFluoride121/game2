package level.structure;

import foundation.MainPanel;
import render.texture.AssetManager;
import render.texture.ImageRenderer;
import render.texture.ResourceLocation;
import unit.UnitTeam;

import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.util.HashMap;

public enum StructureType {
    BASE("structures/base/base_", "Base", true, true, 3, 60, 2, true);

    public final String displayName;
    private final HashMap<UnitTeam, ImageRenderer> renderers = new HashMap<>();
    public final boolean canBeCapturedByDefault, destroyedOnCapture;
    public final int captureSteps, energyIncome;
    public final float unitRegen;
    public final boolean resupply;
    public final String path;

    StructureType(String path, String displayName, boolean canBeCapturedByDefault, boolean destroyedOnCapture, int captureSteps, int energyIncome, float unitRegen, boolean resupply) {
        this.path = path;
        this.displayName = displayName;
        this.canBeCapturedByDefault = canBeCapturedByDefault;
        this.destroyedOnCapture = destroyedOnCapture;
        this.captureSteps = captureSteps;
        this.energyIncome = energyIncome;
        this.unitRegen = unitRegen;
        this.resupply = resupply;
    }

    public ImageRenderer getRenderer(UnitTeam team) {
        return renderers.get(team);
    }

    public static void init() {
        MainPanel.setLoadBarEnabled(true);
        StructureType[] values = values();
        for (int i = 0; i < values.length; i++) {
            StructureType type = values[i];
            RescaleOp op = new RescaleOp(0.7f, 0, null);
            for (UnitTeam team : UnitTeam.ORDERED_TEAMS) {
                BufferedImage image = AssetManager.getImage(new ResourceLocation(type.path + team.s + ".png"), true);
                op.filter(image, image);
                type.renderers.put(team, ImageRenderer.renderImageCentered(image, true));
            }
            BufferedImage image = AssetManager.getImage(new ResourceLocation(type.path + "none.png"), true);
            op.filter(image, image);
            type.renderers.put(null, ImageRenderer.renderImageCentered(image, true));
            MainPanel.setLoadBarProgress((i + 1f) / values.length);
        }
        MainPanel.setLoadBarEnabled(false);
    }
}
