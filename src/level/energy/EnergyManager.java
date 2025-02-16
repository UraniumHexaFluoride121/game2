package level.energy;

import foundation.input.ButtonOrder;
import foundation.input.ButtonRegister;
import level.Level;
import network.PacketReceiver;
import network.PacketWriter;
import network.Writable;
import render.*;
import render.anim.PowAnimation;
import render.renderables.RenderElement;
import render.renderables.text.FixedTextRenderer;
import render.renderables.text.TextAlign;
import render.texture.ImageRenderer;
import render.texture.ResourceLocation;
import render.ui.UIColourTheme;
import render.ui.types.LevelUIContainer;
import render.ui.types.UIBox;
import render.ui.types.UIClickBlockingBox;
import render.ui.types.UITextLabel;
import unit.Unit;
import unit.UnitTeam;
import unit.action.Action;

import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;

import static render.ui.types.UITextLabel.*;

public class EnergyManager extends LevelUIContainer implements Writable {
    public static final ImageRenderer ENERGY_IMAGE = ImageRenderer.renderImageCentered(new ResourceLocation("icons/energy.png"), true, true);

    private final FixedTextRenderer availableText, incomeText, availableChangeText;
    public HashMap<UnitTeam, Integer> availableMap = new HashMap<>();
    public HashMap<UnitTeam, Integer> incomeMap = new HashMap<>();
    private final ArrayList<NumberChangeText> changeTexts = new ArrayList<>();

    public EnergyManager(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, ButtonOrder buttonOrder, float x, float y, Level level) {
        super(register, buttonRegister, order, buttonOrder, x, y, level);
        for (UnitTeam team : UnitTeam.ORDERED_TEAMS) {
            availableMap.putIfAbsent(team, 0);
            incomeMap.put(team, 0);
        }
        availableText = new FixedTextRenderer(null, .7f, UITextLabel.TEXT_COLOUR)
                .setBold(true).setTextAlign(TextAlign.LEFT);
        incomeText = new FixedTextRenderer(null, .7f, UITextLabel.GREEN_TEXT_COLOUR)
                .setBold(true).setTextAlign(TextAlign.LEFT);
        availableChangeText = new FixedTextRenderer(null, .7f, UITextLabel.GREEN_TEXT_COLOUR)
                .setBold(true).setTextAlign(TextAlign.RIGHT);
        Renderable availableChangeTranslated = availableChangeText.translate(8.4f, 1.75f);
        addRenderables((r, b) -> {
            new UIClickBlockingBox(r, b, RenderOrder.LEVEL_UI, ButtonOrder.LEVEL_UI, 0, -.6f, 10, 3.6f, box -> box.setColourTheme(UIColourTheme.LIGHT_BLUE_TRANSPARENT_CENTER)).setZOrder(-1);
            new RenderElement(r, RenderOrder.LEVEL_UI,
                    new FixedTextRenderer("Available:", .7f, UITextLabel.TEXT_COLOUR)
                            .setBold(true).setTextAlign(TextAlign.LEFT).translate(.5f, 1.75f),
                    new FixedTextRenderer("Income:", .7f, UITextLabel.TEXT_COLOUR)
                            .setBold(true).setTextAlign(TextAlign.LEFT).translate(.5f, .15f),
                    new UIBox(5, 1.2f).setCorner(.25f).setColourTheme(UIColourTheme.DARK_GRAY).translate(4.6f, 1.4f),
                    new UIBox(5, 1.2f).setCorner(.25f).setColourTheme(UIColourTheme.DARK_GRAY).translate(4.6f, -.2f),
                    availableText.translate(5, 1.75f), incomeText.translate(5, .15f),
                    g -> {
                        if (level.hasActiveAction()) {
                            level.selectedUnit.type.getActionCost(level.getActiveAction()).ifPresent(cost -> {
                                updateAvailableChange(-cost);
                            });
                        }
                        if (renderAvailableChange) {
                            renderAvailableChange = false;
                            availableChangeTranslated.render(g);
                        }
                        GameRenderer.renderOffset(8.4f, 1.75f, g, () -> {
                            changeTexts.removeIf(t -> t.anim.finished());
                            changeTexts.forEach(t -> t.render(g));
                        });
                        g.translate(8.9f, 2f);
                        ENERGY_IMAGE.render(g, 1.3f);
                        g.translate(0, -1.6f);
                        ENERGY_IMAGE.render(g, 1.3f);
                    }
            );
        });
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled() && level.isThisPlayerAlive();
    }

    private boolean renderAvailableChange = false;

    public void updateAvailableChange(int amount) {
        renderAvailableChange = true;
        availableChangeText.setTextColour(amount < 0 ? RED_TEXT_COLOUR : GREEN_TEXT_COLOUR);
        availableChangeText.updateText(String.valueOf(amount));
    }

    public void updateDisplay(UnitTeam team) {
        incomeText.updateText("+" + incomeMap.get(team));
        availableText.updateText(String.valueOf(availableMap.get(team)));
    }

    public void incrementTurn(UnitTeam team) {
        addAvailable(team, incomeMap.get(team));
    }

    private void addAvailable(UnitTeam team, int amount) {
        availableMap.compute(team, (t, i) -> Math.clamp(i + amount, 0, Math.round(incomeMap.get(team) * 2.5f)));
        if (level.getThisTeam() == team) {
            updateDisplay(team);
            changeTexts.add(new NumberChangeText(amount));
        }
    }

    public void recalculateIncome() {
        for (UnitTeam team : UnitTeam.ORDERED_TEAMS) {
            incomeMap.put(team, 0);
        }
        level.tileSelector.tileSet.forEach(t -> {
            if (t.hasStructure() && t.structure.team != null) {
                incomeMap.compute(t.structure.team, (team, i) -> i + t.structure.type.energyIncome);
            }
        });
    }

    public boolean canAfford(UnitTeam team, int cost, boolean consume) {
        if (availableMap.get(team) - cost >= 0) {
            if (consume) {
                addAvailable(team, -cost);
            }
            return true;
        }
        return false;
    }

    public boolean canAfford(Unit unit, Action action, boolean consume) {
        Optional<Integer> actionCost = unit.type.getActionCost(action);
        return actionCost.map(cost -> canAfford(unit.team, cost, consume)).orElse(true);
    }

    @Override
    public void write(DataOutputStream w) throws IOException {
        PacketWriter.writeMap(availableMap, k -> PacketWriter.writeEnum(k, w), w::writeInt, w);
        PacketWriter.writeMap(incomeMap, k -> PacketWriter.writeEnum(k, w), w::writeInt, w);
    }

    public void updateFromRead(HashMap<UnitTeam, Integer> availableMap, HashMap<UnitTeam, Integer> incomeMap) {
        UnitTeam team = level.getThisTeam();
        if (!Objects.equals(availableMap.get(team), this.availableMap.get(team))) {
            changeTexts.add(new NumberChangeText(availableMap.get(team) - this.availableMap.get(team)));
        }
        this.availableMap = availableMap;
        this.incomeMap = incomeMap;
        recalculateIncome();
        updateDisplay(team);
    }

    private static class NumberChangeText implements Renderable {
        public final FixedTextRenderer text;
        public final PowAnimation anim = new PowAnimation(1, .7f);

        public NumberChangeText(int change) {
            text = new FixedTextRenderer(null, .7f, change < 0 ? UITextLabel.RED_TEXT_COLOUR : UITextLabel.GREEN_TEXT_COLOUR)
                    .setBold(true).setTextAlign(TextAlign.RIGHT);
            text.updateText((change < 0 ? "" : "+") + change);
        }

        @Override
        public void render(Graphics2D g) {
            GameRenderer.renderOffset(0, -anim.normalisedProgress() * 0.5f, g, () -> {
                text.render(g);
            });
        }
    }
}