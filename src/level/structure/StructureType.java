package level.structure;

import render.texture.AssetManager;
import render.texture.ImageRenderer;
import render.texture.ResourceLocation;
import unit.UnitTeam;

import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.util.HashMap;

public enum StructureType {
    BASE("structures/base/base_", "Base", true, true, 3, 60, 1, true);

    public final String displayName;
    private final HashMap<UnitTeam, ImageRenderer> renderers = new HashMap<>();
    public final boolean canBeCapturedByDefault, destroyedOnCapture;
    public final int captureSteps, energyIncome;
    public final float unitRegen;
    public final boolean resupply;

    StructureType(String path, String displayName, boolean canBeCapturedByDefault, boolean destroyedOnCapture, int captureSteps, int energyIncome, float unitRegen, boolean resupply) {
        this.displayName = displayName;
        this.canBeCapturedByDefault = canBeCapturedByDefault;
        this.destroyedOnCapture = destroyedOnCapture;
        this.captureSteps = captureSteps;
        this.energyIncome = energyIncome;
        this.unitRegen = unitRegen;
        this.resupply = resupply;
        RescaleOp op = new RescaleOp(0.7f, 0, null);
        for (UnitTeam team : UnitTeam.ORDERED_TEAMS) {
            BufferedImage image = AssetManager.getImage(new ResourceLocation(path + team.s + ".png"), true);
            op.filter(image, image);
            renderers.put(team, ImageRenderer.renderImageCentered(image, true));
        }
        BufferedImage image = AssetManager.getImage(new ResourceLocation(path + "none.png"), true);
        op.filter(image, image);
        renderers.put(null, ImageRenderer.renderImageCentered(image, true));
    }

    public ImageRenderer getRenderer(UnitTeam team) {
        return renderers.get(team);
    }
}
