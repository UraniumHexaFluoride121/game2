package render.level;

import foundation.MainPanel;
import foundation.WeightedSelector;
import foundation.input.ButtonRegister;
import foundation.input.InputType;
import foundation.math.MathUtil;
import foundation.math.ObjPos;
import level.Level;
import render.*;
import render.anim.timer.LerpAnimation;
import render.anim.timer.PowAnimation;
import render.level.tile.RenderElement;
import render.texture.ResourceLocation;
import render.types.box.UIDisplayBox;
import render.types.container.LevelUIContainer;
import render.types.container.UIContainer;
import render.types.text.TextRenderable;
import render.types.text.UITextDisplayBox;
import singleplayer.card.*;
import unit.ShipClass;
import unit.Unit;
import unit.UnitTeam;
import unit.type.UnitType;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class CardSelectScreen extends LevelUIContainer<Level> {
    private final LerpAnimation overlayTimer = new LerpAnimation(1);
    private boolean renderablesAdded = false;
    private UITextDisplayBox starsDisplay;
    private final ArrayList<CardSelectItem> cards = new ArrayList<>();
    private final WeightedSelector<CardGenerationGroup> cardCategories = new WeightedSelector<>();

    public CardSelectScreen(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, Level level) {
        super(register, buttonRegister, RenderOrder.END_SCREEN, 0, 0, level);
        addRenderables((r, b) -> {
            new RenderElement(r, RenderOrder.TITLE_SCREEN_OVERLAY,
                    g -> {
                        if (!renderablesAdded && overlayTimer.normalisedProgress() > 0.5f) {
                            createCategories();
                            createScreen();
                            renderablesAdded = true;
                        }
                        overlayTimer.renderOverlay(g, LerpAnimation::triangleProgress);
                    }
            );
        });
    }

    private void createCategories() {
        HashMap<UnitType, Integer> unitCounts = new HashMap<>();
        HashMap<ShipClass, Integer> classCounts = new HashMap<>();
        int unitCount = 0;
        for (Unit unit : level.unitSet) {
            if (unit.data.team == UnitTeam.BLUE) {
                unitCounts.putIfAbsent(unit.data.type, 0);
                unitCounts.put(unit.data.type, unitCounts.get(unit.data.type) + 1);
                classCounts.putIfAbsent(unit.data.type.shipClass, 0);
                classCounts.put(unit.data.type.shipClass, classCounts.get(unit.data.type.shipClass) + 1);
                unitCount++;
            }
        }
        for (Map.Entry<UnitType, Integer> entry : unitCounts.entrySet()) {
            cardCategories.add((float) Math.sqrt(entry.getValue()), new CardGenerationGroup(CardType.UPGRADE_CARD)
                    .step(0.5f, 1)
                    .add(1, entry.getKey(), AttributeProperty.UNIQUE)
                    .extend()
                    .step(1, 4)
                    .add(1, entry.getKey())
                    .add(0.5f, entry.getKey().shipClass)
                    .add(0.1f, AttributeProperty.INCOME)
                    .extend()
            );
        }
        for (Map.Entry<ShipClass, Integer> entry : classCounts.entrySet()) {
            cardCategories.add((float) Math.sqrt(entry.getValue()) / 1.5f, new CardGenerationGroup(CardType.UPGRADE_CARD)
                    .step(0.5f, 1)
                    .add(1, entry.getKey(), AttributeProperty.UNIQUE)
                    .extend()
                    .step(1, 4)
                    .add(1, entry.getKey())
                    .add(0.15f, AttributeProperty.DEFENCE)
                    .add(0.15f, AttributeProperty.OFFENCE)
                    .add(0.1f, AttributeProperty.INCOME)
                    .extend()
            );
        }
        cardCategories.add((float) Math.pow(unitCount, 0.3f), new CardGenerationGroup(CardType.UPGRADE_CARD)
                .step(1, 4)
                .add(1, AttributeProperty.OFFENCE)
                .add(0.1f, AttributeProperty.INCOME)
                .extend()
        );
        cardCategories.add((float) Math.pow(unitCount, 0.3f), new CardGenerationGroup(CardType.UPGRADE_CARD)
                .step(1, 4)
                .add(1, AttributeProperty.DEFENCE)
                .add(0.1f, AttributeProperty.INCOME)
                .extend()
        );
        cardCategories.add((float) Math.pow(unitCount, 0.7f), new CardGenerationGroup(CardType.UPGRADE_CARD)
                .step(1, 4)
                .add(1, AttributeProperty.INCOME)
                .extend()
        );
    }

    private void createScreen() {
        Renderable titleScreenImage = Renderable.renderImage(new ResourceLocation("title_screen.png"), false, true, 60, true);
        addRenderables((r, b) -> {
            new RenderElement(r, RenderOrder.TITLE_SCREEN_BACKGROUND,
                    titleScreenImage
            );
            starsDisplay = new UITextDisplayBox(r, RenderOrder.TITLE_SCREEN_BUTTONS,
                    Renderable.right() - 6.5f, Renderable.top() - 2.5f,
                    6, 2, 1.5f).setBold();
            updateStars();
            int cost = 70;
            for (int i = 0; i < 6; i++) {
                cards.add(new CardSelectItem(r, b, RenderOrder.TITLE_SCREEN_BUTTONS,
                        MainPanel.spState.generateCard(UnitTeam.BLUE, cardCategories, cost += 10, 5), i));
            }
        });
    }

    public void updateStars() {
        starsDisplay.setText(MainPanel.spState.stars + TextRenderable.STAR.display);
    }

    @Override
    public void delete() {
        super.delete();
        starsDisplay = null;
    }

    @Override
    public UIContainer setEnabled(boolean enabled) {
        if (enabled)
            overlayTimer.startTimer();
        return super.setEnabled(enabled);
    }

    @Override
    public boolean blocking(InputType type) {
        return isEnabled();
    }

    private static class CardSelectItem extends UIContainer {
        public Card card;
        public int index;
        public PowAnimation timer = new PowAnimation(0.2f, 2);
        public boolean selected = false;
        public UIDisplayBox displayBox;

        public CardSelectItem(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, Card card, int index) {
            super(register, buttonRegister, order,
                    (Card.WIDTH + 2) * (index % 3 + 0.5f) + 1,
                    Renderable.top() / 2 + (Card.HEIGHT / 2 + 1) * (index < 3 ? 1 : -1)
            );
            timer.setReversed(true);
            timer.finish();
            this.card = card;
            this.index = index;
            addRenderables((r, b) -> {
                card.createRenderElement(r, b, RenderOrder.TITLE_SCREEN_BUTTONS, -Card.WIDTH / 2, -Card.HEIGHT / 2, handler -> {
                    handler.add(false).setOnClick(() -> {
                        timer.setReversed(selected);
                        selected = !selected;
                        displayBox.modifyBox(box -> box.setColourTheme(selected ? Card.SELECTED_BOX : Card.BOX));
                    });
                    handler.setIgnoreBlocking(true);
                }, box -> {
                    displayBox = box;
                });
            });
        }

        @Override
        public void render(Graphics2D g) {
            GameRenderer.renderScaledOrigin(getScale(), x, y, g, () -> {
                super.render(g);
            });
        }

        private float getScale() {
            return MathUtil.lerp(1, 1.1f, timer.normalisedProgress());
        }

        @Override
        protected ObjPos getModifiedPos(ObjPos pos) {
            return super.getModifiedPos(pos).scale(1 / getScale(), new ObjPos(x, y));
        }
    }
}
