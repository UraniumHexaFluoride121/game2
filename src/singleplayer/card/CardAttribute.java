package singleplayer.card;

import foundation.IInternalName;
import network.Writable;
import render.HorizontalAlign;
import render.UIColourTheme;
import render.VerticalAlign;
import render.save.SerializationProxy;
import render.save.SerializedByProxy;
import render.types.box.UIDisplayBox;
import render.types.text.StyleElement;
import unit.ShipClass;
import unit.action.Action;
import unit.stats.attribute.UnitAttribute;
import unit.stats.modifiers.groups.CardModifiers;
import unit.stats.modifiers.types.CardModifier;
import unit.type.CorvetteType;
import unit.type.FighterType;
import unit.type.UnitType;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class CardAttribute implements SerializedByProxy, Serializable, Writable {
    public static final HashMap<String, CardAttribute> values = new HashMap<>();

    static {
        //Income
        add(CardModifiers.incomeBoost(), "INCOME")
                .level(1, 20, 3)
                .level(2, 50, 3)
                .level(3, 50, 3)
                .scaledProperty(1, 0.5f, AttributeProperty.INCOME)
                .generate();
        //Class attributes
        for (ShipClass shipClass : ShipClass.values()) {
            //Actions
            for (Action action : new Action[]{Action.FIRE}) {
                //Action cost reduction
                add(CardModifiers.classActionCost(shipClass, action),
                        shipClass, "ACTION_COST", action)
                        .level(1, 20, 2)
                        .level(2, 40, 2)
                        .level(3, 60, 2)
                        .proportionalProperty(1, AttributeProperty.ACTION_COST)
                        .scaledProperty(1, 0.5f, shipClass)
                        .generate();
            }
            //Class HP
            add(CardModifiers.classHP(shipClass),
                    shipClass, "HP")
                    .level(1, 50, 2)
                    .level(2, 80, 1)
                    .constantProperty(3, AttributeProperty.DEFENCE)
                    .constantProperty(2, shipClass)
                    .generate();
            //Class damage
            add(CardModifiers.classDamage(shipClass),
                    shipClass, "DAMAGE")
                    .level(1, 30, 2)
                    .level(2, 50, 2)
                    .level(3, 70, 2)
                    .proportionalProperty(2, AttributeProperty.OFFENCE)
                    .proportionalProperty(1.5f, shipClass)
                    .generate();
            //Class move speed
            add(CardModifiers.classMoveSpeed(shipClass),
                    shipClass, "MOVE_SPEED")
                    .level(1, 25, 1)
                    .level(2, 40, 1)
                    .level(3, 55, 1)
                    .proportionalProperty(1, AttributeProperty.MOVE_SPEED)
                    .proportionalProperty(1, shipClass)
                    .generate();
        }
        //Type attributes
        for (UnitType type : UnitType.ORDERED_UNIT_TYPES) {
            if (type.ammoCapacity != 0) {
                //Ammo capacity
                add(CardModifiers.ammoIncrease(type, 1),
                        type, "AMMO")
                        .level(1, 70, 1)
                        .constantProperty(3, AttributeProperty.UNIQUE)
                        .constantProperty(3, type, AttributeProperty.UNIQUE)
                        .generate();
            }
            //Type HP
            add(CardModifiers.typeHP(type),
                    type, "HP")
                    .level(1, 20, 1)
                    .level(2, 35, 1)
                    .level(3, 50, 1)
                    .proportionalProperty(1, AttributeProperty.DEFENCE)
                    .proportionalProperty(1.5f, type)
                    .generate();
            //Type move speed
            add(CardModifiers.typeMoveSpeed(type),
                    type, "MOVE_SPEED")
                    .level(1, 15, 1)
                    .level(2, 30, 1)
                    .level(3, 45, 1)
                    .proportionalProperty(1, AttributeProperty.MOVE_SPEED)
                    .proportionalProperty(1, type)
                    .generate();
        }
        //Defence network
        for (UnitType type : new UnitType[]{CorvetteType.FRIGATE, FighterType.INTERCEPTOR}) {
            for (UnitAttribute attribute : type.attributes) {
                if (attribute == UnitAttribute.DEFENCE_NETWORK)
                    throw new RuntimeException();
            }
            add(CardModifiers.typeAttribute(type, UnitAttribute.DEFENCE_NETWORK),
                    type, UnitAttribute.DEFENCE_NETWORK)
                    .level(1, 50, 1)
                    .constantProperty(3, type)
                    .constantProperty(3, type, AttributeProperty.UNIQUE)
                    .constantProperty(1, AttributeProperty.UNIQUE)
                    .generate();
        }
    }

    public final int cost, maxCount;
    public final CardModifier modifier;
    public final Color textColour;
    public final AttributeProperty[] properties;
    private final String internalName;

    private CardAttribute(int cost, int maxCount, CardModifier modifier, String internalName, AttributeProperty... properties) {
        this.cost = cost;
        this.maxCount = maxCount;
        this.modifier = modifier;
        this.properties = properties;
        this.internalName = internalName;
        textColour = UIColourTheme.darken(UIColourTheme.pow(modifier.colour().borderColour, 0.15f), 0.8f);
    }

    public void addRenderer(UIDisplayBox cardBox) {
        float height = 1.3f + modifier.categories().size() * 0.6f;
        UIDisplayBox box = new UIDisplayBox(0, 0, Card.WIDTH - 1, height, b -> b.setColourTheme(modifier.listColour()), false);
        box.addText(0.6f, HorizontalAlign.LEFT, modifier.name());
        box.addSpace(0.3f, 0);
        box.setColumnVerticalAlign(0, VerticalAlign.TOP);
        box.setColumnVerticalAlign(1, VerticalAlign.TOP);
        int lastIndex = box.getLastIndex(0);
        modifier.forEachCategory((cat, effect) -> {
            String value = modifier.effectDescriptionValue(cat);
            String condition = modifier.effectCondition(cat);
            box.addText(0.6f, HorizontalAlign.LEFT, 0, value);
            box.addSpace(0.2f, 0);
            box.addText(0.6f, HorizontalAlign.RIGHT, 1, condition == null ? null : StyleElement.getStyles(value)[0] + condition);
            box.addSpace(0.2f, 1);
        });
        box.setColumnTopMarginToElement(1, 0, lastIndex, VerticalAlign.BOTTOM);
        cardBox.addBox(box, HorizontalAlign.CENTER, 0, false);
    }

    public String getToolTip() {
        return modifier.description();
    }

    @Override
    public String getInternalName() {
        return internalName;
    }

    @Serial
    private Object writeReplace() throws ObjectStreamException {
        return new SerializationProxy<>(CardAttribute.class, this);
    }

    public static CardAttribute valueOf(String internalName) {
        CardAttribute attribute = values.get(internalName);
        if (attribute == null) {
            throw new IllegalArgumentException("Card Attribute not found: " + internalName);
        }
        return attribute;
    }

    public static CardAttribute valueOf(int level, Object... base) {
        String name = getName(base) + "_" + level;
        CardAttribute attribute = values.get(name);
        if (attribute == null) {
            throw new IllegalArgumentException("Card Attribute not found: " + name);
        }
        return attribute;
    }

    private static CardAttributeFactory add(Function<Integer, ? extends CardModifier> modifier, Object... base) {
        return new CardAttributeFactory(modifier, getName(base));
    }

    private static String getName(Object... base) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < base.length; i++) {
            if (base[i] instanceof IInternalName n)
                s.append(n.getInternalName().toUpperCase());
            else if (base[i] instanceof String str)
                s.append(str.toUpperCase());
            else
                throw new IllegalArgumentException("Illegal argument: " + base[i]);
            if (i < base.length - 1) {
                s.append("_");
            }
        }
        return s.toString();
    }

    private static UnaryOperator<Integer> constInt(int v) {
        return i -> v;
    }

    private static UnaryOperator<Integer> scaledInt(int... v) {
        return i -> v[i];
    }

    private static Function<Integer, Float> constFloat(float v) {
        return i -> v;
    }

    private static Function<Integer, Float> scaledFloat(float... v) {
        return i -> v[i];
    }

    @Override
    public void write(DataOutputStream w) throws IOException {
        w.writeUTF(getInternalName());
    }

    public static CardAttribute read(DataInputStream w) throws IOException {
        return valueOf(w.readUTF());
    }

    private static class CardAttributeFactory {
        private final Function<Integer, ? extends CardModifier> modifier;
        private final ArrayList<LevelData> levels = new ArrayList<>();
        private final ArrayList<Function<Integer, AttributeProperty>> properties = new ArrayList<>();
        private final String base;

        private CardAttributeFactory(Function<Integer, ? extends CardModifier> modifier, String base) {
            this.modifier = modifier;
            this.base = base;
        }

        private void generate() {
            for (LevelData level : levels) {
                String name = base + "_" + level.level;
                AttributeProperty[] p = new AttributeProperty[properties.size()];
                for (int i = 0; i < p.length; i++) {
                    p[i] = properties.get(i).apply(level.level);
                }
                values.put(name, new CardAttribute(
                        level.cost, level.maxCount, modifier.apply(level.level), name, p
                ));
            }
        }

        private CardAttributeFactory level(int level, int cost, int maxCount) {
            levels.add(new LevelData(level, cost, maxCount));
            return this;
        }

        /**
         * Adds a property that always has a constant value.
         *
         * @param value       The value the property should have
         * @param identifiers The property's identifiers
         */
        private CardAttributeFactory constantProperty(float value, Object... identifiers) {
            properties.add(level -> new AttributeProperty(value, identifiers));
            return this;
        }

        /**
         * Adds a property with linear value scaling based on level. The final value
         * is equal to {@code base} for level 1, and each level after that adds a multiple of {@code scale}.
         *
         * @param base        The base value at level 1
         * @param scale       The additional value to add for each level after 1
         * @param identifiers The property's identifiers
         */
        private CardAttributeFactory scaledProperty(float base, float scale, Object... identifiers) {
            properties.add(level -> new AttributeProperty(base + (level - 1) * scale, identifiers));
            return this;
        }

        /**
         * Adds a property with linear value scaling based on level. The final value
         * is equal to {@code scale * level}.
         *
         * @param scale       The value scale factor
         * @param identifiers The property's identifiers
         */
        private CardAttributeFactory proportionalProperty(float scale, Object... identifiers) {
            properties.add(level -> new AttributeProperty(level * scale, identifiers));
            return this;
        }

        /**
         * Adds a property with a value specified by the provided value function.
         *
         * @param value       The function that decides the value as a function of level
         * @param identifiers The property's identifiers
         */
        private CardAttributeFactory functionalProperty(Function<Integer, Float> value, Object... identifiers) {
            properties.add(level -> new AttributeProperty(value.apply(level), identifiers));
            return this;
        }
    }

    private record LevelData(int level, int cost, int maxCount) {

    }

    ;
}
