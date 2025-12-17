package level.energy;

import foundation.input.ButtonRegister;
import foundation.math.MathUtil;
import level.Level;
import level.structure.Structure;
import level.tutorial.TutorialElement;
import level.tutorial.TutorialManager;
import level.tutorial.sequence.event.EnergyBoxListener;
import network.NetworkState;
import network.PacketWriter;
import network.Writable;
import render.*;
import render.anim.timer.PowAnimation;
import render.level.tile.RenderElement;
import render.types.text.*;
import render.HorizontalAlign;
import render.texture.ImageRenderer;
import render.texture.ResourceLocation;
import render.UIColourTheme;
import render.types.box.UIBox;
import render.types.input.button.UIButton;
import render.types.container.LevelUIContainer;
import render.types.container.UIContainer;
import render.types.container.UIScrollSurface;
import unit.Unit;
import unit.UnitTeam;
import unit.action.Action;
import unit.stats.modifiers.types.ModifierCategory;

import java.awt.*;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static render.types.text.UITextLabel.*;

public class EnergyManager extends LevelUIContainer<Level> implements Writable {
    public static final ImageRenderer ENERGY_IMAGE = ImageRenderer.renderImageCentered(new ResourceLocation("icons/energy.png"), true, true);
    public static final int TUTORIAL_INCOME = 100;
    public static final String displayName = "Antimatter";

    private final TextRenderer availableText, incomeText, availableChangeText, incomeChangeText;
    public HashMap<UnitTeam, Integer> availableMap = new HashMap<>();
    public HashMap<UnitTeam, Integer> incomeMap = new HashMap<>();
    public HashMap<UnitTeam, Integer> costsMap = new HashMap<>();
    private final ArrayList<NumberChangeText> changeTexts = new ArrayList<>();
    private UIContainer incomeBox;
    private UIScrollSurface incomeLineItemsScroll, costLineItemsScroll;
    private final ArrayList<UIContainer> incomeLineItems = new ArrayList<>(), costLineItems = new ArrayList<>();
    private final TextRenderer maxCapacityText = new TextRenderer(null, 0.7f, TEXT_COLOUR_DARK),
            noCostsText = new TextRenderer(null, 0.6f, TEXT_COLOUR_DARK);

    public EnergyManager(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, float x, float y, Level level) {
        super(register, buttonRegister, order, x, y, level);
        for (UnitTeam team : UnitTeam.ORDERED_TEAMS) {
            availableMap.putIfAbsent(team, 0);
            incomeMap.put(team, 0);
            costsMap.put(team, 0);
        }
        availableText = new TextRenderer(null, .7f, UITextLabel.TEXT_COLOUR)
                .setBold(true).setTextAlign(HorizontalAlign.LEFT);
        incomeText = new TextRenderer(null, .7f, StyleElement.ENERGY_COST_GREEN.colour)
                .setBold(true).setTextAlign(HorizontalAlign.LEFT);
        availableChangeText = new TextRenderer(null, .7f, StyleElement.ENERGY_COST_GREEN.colour)
                .setBold(true).setTextAlign(HorizontalAlign.RIGHT);
        incomeChangeText = new TextRenderer(null, .7f, StyleElement.ENERGY_COST_GREEN.colour)
                .setBold(true).setTextAlign(HorizontalAlign.RIGHT);
        Renderable availableChangeTranslated = availableChangeText.translate(8.4f, 1.75f);
        Renderable incomeChangeTranslated = incomeChangeText.translate(8.4f, .15f);
        addRenderables((r, b) -> {
            new UIButton(r, b, RenderOrder.LEVEL_UI, 0, -.6f, 10, 3.6f, 0, true)
                    .setColourTheme(UIColourTheme.LIGHT_BLUE_BOX).setOnClick(() -> {
                        incomeBox.setEnabled(true);
                        TutorialManager.acceptEvent(new EnergyBoxListener.IncomeBoxEvent(level));
                    }).setOnDeselect(() -> {
                        incomeBox.setEnabled(false);
                    }).toggleMode().tooltip(t -> t.add(13, AbstractUITooltip.dark(), displayName + " is used by your units to perform actions. Income is credited to you at the start of each turn. Click this window to see more details.")).setZOrder(-1);
            new RenderElement(r, RenderOrder.LEVEL_UI,
                    new TextRenderer("Available:", .7f, UITextLabel.TEXT_COLOUR)
                            .setBold(true).setTextAlign(HorizontalAlign.LEFT).translate(.5f, 1.75f),
                    new TextRenderer("Income:", .7f, UITextLabel.TEXT_COLOUR)
                            .setBold(true).setTextAlign(HorizontalAlign.LEFT).translate(.5f, .15f),
                    new UIBox(5, 1.2f).setCorner(.5f).setColourTheme(UIColourTheme.DARK_GRAY).translate(4.6f, 1.4f),
                    new UIBox(5, 1.2f).setCorner(.5f).setColourTheme(UIColourTheme.DARK_GRAY).translate(4.6f, -.2f),
                    availableText.translate(5, 1.75f), incomeText.translate(5, .15f),
                    g -> {
                        GameRenderer.renderTransformed(g, () -> {
                            if (level.hasActiveAction()) {
                                level.selectedUnit.stats.getActionCost(level.getActiveAction()).ifPresent(cost -> {
                                    updateAvailableChange(-cost);
                                });
                            }
                            if (renderAvailableChange) {
                                renderAvailableChange = false;
                                availableChangeTranslated.render(g);
                            }
                            if (renderIncomeChange) {
                                renderIncomeChange = false;
                                incomeChangeTranslated.render(g);
                            }
                            changeTexts.removeIf(t -> t.anim.finished());
                            changeTexts.forEach(t -> {
                                GameRenderer.renderOffset(8.4f, t.incomeChange ? .15f : 1.75f, g, () -> {
                                    t.render(g);
                                });
                            });
                            g.translate(8.9f, 2f);
                            ENERGY_IMAGE.render(g, 1.3f);
                            g.translate(0, -1.6f);
                            ENERGY_IMAGE.render(g, 1.3f);
                        });
                    });
            incomeBox = new UIContainer(r, b, RenderOrder.LEVEL_UI, 0, -14.5f).addRenderables((r2, b2) -> {
                new RenderElement(r2, RenderOrder.LEVEL_UI,
                        new UIBox(10, 13).setColourTheme(UIColourTheme.LIGHT_BLUE_FULLY_OPAQUE_CENTER),
                        new UITextLabel(8, 1, false).setTextCenterBold().updateTextCenter(displayName).translate(.7f, 11.5f),
                        new TextRenderer("Income:", 0.7f, TEXT_COLOUR_DARK).setBold(true).setTextAlign(HorizontalAlign.LEFT).translate(0.5f, 10.7f),
                        new TextRenderer("Expenses:", 0.7f, TEXT_COLOUR_DARK).setBold(true).setTextAlign(HorizontalAlign.LEFT).translate(0.5f, 5.7f),
                        noCostsText.setItalic(true).setTextAlign(HorizontalAlign.CENTER).translate(5, 4.7f),
                        maxCapacityText.setBold(true).setTextAlign(HorizontalAlign.LEFT).translate(0.5f, 0.6f)
                ).setZOrder(-1);
                incomeLineItemsScroll = new UIScrollSurface(r2, b2, RenderOrder.LEVEL_UI, 0, 6.5f, 10, 4, false, (r3, b3) -> {
                }).addScrollBar(0.25f, 0.2f, -0.2f).setScrollSpeed(0.2f);
                costLineItemsScroll = new UIScrollSurface(r2, b2, RenderOrder.LEVEL_UI, 0, 1.5f, 10, 4, false, (r3, b3) -> {
                }).addScrollBar(0.25f, 0.2f, -0.2f).setScrollSpeed(0.2f);
            }).setEnabled(false);
        });
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled() && level.isThisPlayerAlive();
    }

    private boolean renderAvailableChange = false, renderIncomeChange;

    public void updateAvailableChange(int amount) {
        renderAvailableChange = true;
        availableChangeText.setTextColour(numberColour(amount));
        availableChangeText.updateText(numberText(amount));
    }

    public void updateIncomeChange(int amount) {
        renderIncomeChange = true;
        incomeChangeText.setTextColour(numberColour(amount));
        incomeChangeText.updateText(numberText(amount));
    }

    public void updateDisplay(UnitTeam team) {
        incomeText.updateText(numberText(incomeMap.get(team) + costsMap.get(team)));
        availableText.updateText(String.valueOf(availableMap.get(team)));
        incomeText.setTextColour(numberColour(incomeMap.get(team) + costsMap.get(team)));
    }

    public void incrementTurn(UnitTeam team) {
        recalculateIncome();
        int added = incomeMap.get(team) + costsMap.get(team);
        if (availableMap.get(team) + added < 0) {
            level.unitSet.forEach(u -> {
                if (u.data.team == team && u.data.stealthMode) {
                    u.setStealthMode(false);
                    if (level.networkState == NetworkState.SERVER) {
                        level.server.sendUnitStealthPacket(u);
                    }
                }
            });
        }
        addAvailable(team, incomeMap.get(team) + costsMap.get(team));
    }

    public void addAvailable(UnitTeam team, int amount) {
        availableMap.compute(team, (t, i) -> Math.clamp(i + amount, 0, Math.max(i, getEnergyCapacity(team))));
        if (level.getThisTeam() == team) {
            updateDisplay(team);
            changeTexts.add(new NumberChangeText(amount, false));
        }
    }

    private int getEnergyCapacity(UnitTeam team) {
        return Math.round(incomeMap.get(team) * 1.75f);
    }

    public void recalculateIncome() {
        UnitTeam thisTeam = level.getThisTeam();
        int currentIncome = incomeMap.get(thisTeam) + costsMap.get(thisTeam);
        for (UnitTeam team : UnitTeam.ORDERED_TEAMS) {
            incomeMap.put(team, 0);
            costsMap.put(team, 0);
        }
        incomeLineItems.forEach(UIContainer::delete);
        incomeLineItems.clear();
        costLineItems.forEach(UIContainer::delete);
        costLineItems.clear();
        ArrayList<LineItemData> incomeList = new ArrayList<>(), expenseList = new ArrayList<>();
        if (TutorialManager.isDisabled(TutorialElement.TUTORIAL_INCOME_DISABLED)) {
            incomeList.add(new LineItemData("Tutorial", TUTORIAL_INCOME, -1));
            for (UnitTeam team : UnitTeam.ORDERED_TEAMS) {
                incomeMap.compute(team, (t, i) -> i + TUTORIAL_INCOME);
            }
        }
        for (UnitTeam team : UnitTeam.ORDERED_TEAMS) {
            if (!level.teamData.containsKey(team))
                continue;
            int income = Math.round(level.teamData.get(team).getCardModifierValue(ModifierCategory.INCOME, Float::sum));
            if (income != 0) {
                incomeList.add(new LineItemData("Cards", income, -1));
                incomeMap.compute(team, (t, i) -> i + income);
            }
        }
        level.tileSelector.tileSet.forEach(t -> {
            Structure s = t.structure;
            if (t.hasStructure() && s.team != null) {
                incomeMap.compute(s.team, (team, i) -> {
                    if (team == thisTeam)
                        LineItemData.addToList(s.type.getName(), s.stats.energyIncome(level), incomeList);
                    return i + s.stats.energyIncome(level);
                });
            }
        });
        level.unitSet.forEach(u -> {
            if (u.data.stealthMode)
                u.stats.getPerTurnActionCost(Action.STEALTH).ifPresent(cost -> {
                    costsMap.compute(u.data.team, (team, i) -> {
                        if (team == thisTeam)
                            LineItemData.addToList(u.data.type.getName() + " (" + Action.STEALTH.getName() + ")", -cost, expenseList);
                        return i - cost;
                    });
                });
            if (u.data.mining)
                u.stats.getPerTurnActionCost(Action.MINE).ifPresent(cost -> {
                    incomeMap.compute(u.data.team, (team, i) -> {
                        if (team == thisTeam)
                            LineItemData.addToList(u.data.type.getName() + " (" + Action.MINE.getName() + ")", -cost, incomeList);
                        return i - cost;
                    });
                });
        });
        int newIncome = incomeMap.get(thisTeam) + costsMap.get(thisTeam);
        if (newIncome != currentIncome) {
            changeTexts.add(new NumberChangeText(newIncome - currentIncome, true));
            updateDisplay(thisTeam);
        }
        AtomicInteger i = new AtomicInteger();
        incomeList.sort(Comparator.naturalOrder());
        incomeList.forEach(s -> {
            i.getAndIncrement();
            if (s.income == 0)
                return;
            incomeLineItemsScroll.addRenderables((r, b) -> {
                incomeLineItems.add(new UIContainer(r, b, RenderOrder.LEVEL_UI, 0.5f, i.get() * -1.3f).addRenderables((r2, b2) -> {
                    new RenderElement(r2, RenderOrder.LEVEL_UI,
                            new UIBox(9, 1f).setColourTheme(UIColourTheme.DARK_GRAY).setCorner(0.5f),
                            new TextRenderer((s.count == -1 ? "" : s.count + "x ") + s.name, 0.6f, TEXT_COLOUR)
                                    .setBold(true).setTextAlign(HorizontalAlign.LEFT).translate(0.3f, 0.3f),
                            new TextRenderer(numberText(s.income) + TextRenderable.ENERGY.display, 0.7f, numberColour(s.income))
                                    .setBold(true).setTextAlign(HorizontalAlign.RIGHT).translate(9 - 0.1f, 0.25f)
                    );
                }));
            });
        });
        i.set(0);
        expenseList.sort(Comparator.<LineItemData>naturalOrder().reversed());
        expenseList.forEach(s -> {
            i.getAndIncrement();
            if (s.income == 0)
                return;
            costLineItemsScroll.addRenderables((r, b) -> {
                costLineItems.add(new UIContainer(r, b, RenderOrder.LEVEL_UI, 0.5f, i.get() * -1.3f).addRenderables((r2, b2) -> {
                    new RenderElement(r2, RenderOrder.LEVEL_UI,
                            new UIBox(9, 1f).setColourTheme(UIColourTheme.DARK_GRAY).setCorner(0.5f),
                            new TextRenderer(s.count + "x " + s.name, 0.6f, TEXT_COLOUR)
                                    .setBold(true).setTextAlign(HorizontalAlign.LEFT).translate(0.3f, 0.3f),
                            new TextRenderer(numberText(s.income) + TextRenderable.ENERGY.display, 0.7f, numberColour(s.income))
                                    .setBold(true).setTextAlign(HorizontalAlign.RIGHT).translate(9 - 0.1f, 0.25f)
                    );
                }));
            });
        });
        noCostsText.updateText(costLineItems.isEmpty() ? "Nothing to see here yet" : null);
        maxCapacityText.updateText("Storage capacity: " + getEnergyCapacity(thisTeam) + TextRenderable.ENERGY.display);
        incomeLineItemsScroll.setScrollMax(incomeLineItems.size() * 1.3f - incomeLineItemsScroll.height + 0.2f);
        costLineItemsScroll.setScrollMax(costLineItems.size() * 1.3f - costLineItemsScroll.height + 0.2f);
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
        Optional<Integer> actionCost = unit.stats.getActionCost(action);
        return actionCost.map(cost -> canAfford(unit.data.team, cost, consume)).orElse(true);
    }

    @Override
    public void write(DataOutputStream w) throws IOException {
        PacketWriter.writeMap(availableMap, k -> PacketWriter.writeEnum(k, w), w::writeInt, w);
        PacketWriter.writeMap(incomeMap, k -> PacketWriter.writeEnum(k, w), w::writeInt, w);
    }

    public void updateFromRead(HashMap<UnitTeam, Integer> availableMap, HashMap<UnitTeam, Integer> incomeMap) {
        UnitTeam team = level.getThisTeam();
        if (!Objects.equals(availableMap.get(team), this.availableMap.get(team))) {
            changeTexts.add(new NumberChangeText(availableMap.get(team) - this.availableMap.get(team), false));
        }
        this.availableMap = availableMap;
        this.incomeMap = incomeMap;
        recalculateIncome();
        updateDisplay(team);
    }

    public static String numberText(int amount) {
        return (amount <= 0 ? "" : "+") + amount;
    }

    public static String numberText(float amount, int decimals) {
        return (amount <= 0 ? "" : "+") + MathUtil.floatToString(amount, decimals);
    }

    public static Color numberColour(int amount) {
        return amount == 0 ? TEXT_COLOUR : (amount < 0 ? StyleElement.ENERGY_COST_RED : StyleElement.ENERGY_COST_GREEN).colour;
    }

    public static Color numberColour(float amount) {
        return amount == 0 ? TEXT_COLOUR : (amount < 0 ? StyleElement.ENERGY_COST_RED : StyleElement.ENERGY_COST_GREEN).colour;
    }

    public static String colouredDisplay(StyleElement end, boolean lowercase) {
        return StyleElement.ENERGY_DISPLAY.display + (lowercase ? displayName.toLowerCase() : displayName) + TextRenderable.ENERGY_ICON.display + (end == null ? "" : end.display);
    }

    public static String colouredAmount(StyleElement end, int amount) {
        return StyleElement.ENERGY_DISPLAY.display + amount + TextRenderable.ENERGY_ICON.display + (end == null ? "" : end.display);
    }

    @Override
    public void delete() {
        super.delete();
        incomeBox = null;
        incomeLineItemsScroll = null;
        costLineItemsScroll = null;
        incomeLineItems.forEach(UIContainer::delete);
        costLineItems.forEach(UIContainer::delete);
    }

    private static class LineItemData implements Comparable<LineItemData> {
        public final String name;
        public int income;
        public int count;

        public LineItemData(String name, int income, int count) {
            this.name = name;
            this.income = income;
            this.count = count;
        }

        public static void addToList(String name, int income, ArrayList<LineItemData> list) {
            for (LineItemData data : list) {
                if (data.name.equals(name)) {
                    data.income += income;
                    data.count++;
                    return;
                }
            }
            list.add(new LineItemData(name, income, 1));
        }

        @Override
        public int compareTo(LineItemData o) {
            if (income == o.income)
                return name.compareTo(o.name);
            else
                return Integer.compare(o.income, income);
        }
    }

    private static class NumberChangeText implements Renderable {
        public final TextRenderer text;
        public final PowAnimation anim = new PowAnimation(1, .7f);
        public final boolean incomeChange;

        public NumberChangeText(int change, boolean incomeChange) {
            text = new TextRenderer(null, .7f, numberColour(change))
                    .setBold(true).setTextAlign(HorizontalAlign.RIGHT);
            this.incomeChange = incomeChange;
            text.updateText(numberText(change));
        }

        @Override
        public void render(Graphics2D g) {
            GameRenderer.renderOffset(0, -anim.normalisedProgress() * 0.5f, g, () -> {
                text.render(g);
            });
        }
    }
}