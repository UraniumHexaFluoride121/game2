package singleplayer.card;

import render.HorizontalAlign;
import render.UIColourTheme;
import render.VerticalAlign;
import render.types.box.UIDisplayBox;
import render.types.text.StyleElement;
import unit.ShipClass;
import unit.action.Action;
import unit.stats.attribute.UnitAttribute;
import unit.stats.modifiers.groups.CardModifiers;
import unit.stats.modifiers.types.CardModifier;
import unit.type.CorvetteType;
import unit.type.FighterType;

import java.awt.*;

public enum CardAttribute {
    //Income
    INCOME_INCREASE_I(20, CardModifiers.incomeBoost(1)),
    INCOME_INCREASE_II(50, CardModifiers.incomeBoost(2)),
    INCOME_INCREASE_III(80, CardModifiers.incomeBoost(3)),

    //Class action cost
    FIGHTER_ACTION_COST_FIRE_I(20, CardModifiers.classActionCost(ShipClass.FIGHTER, Action.FIRE, 1)),

    //Type attribute
    INTERCEPTOR_DEFENCE_NETWORK(50, CardModifiers.typeAttribute(FighterType.INTERCEPTOR, UnitAttribute.DEFENCE_NETWORK)),
    FRIGATE_DEFENCE_NETWORK(50, CardModifiers.typeAttribute(CorvetteType.FRIGATE, UnitAttribute.DEFENCE_NETWORK)),

    //Type move speed
    BOMBER_MOVE_SPEED_III(30, CardModifiers.typeMoveSpeed(FighterType.BOMBER, 3)),
    SUPPLY_MOVE_SPEED_II(20, CardModifiers.typeMoveSpeed(CorvetteType.SUPPLY, 2)),
    CORVETTE_MOVE_SPEED_I(15, CardModifiers.classMoveSpeed(ShipClass.FIGHTER, 1)),

    //Class damage
    FIGHTER_DAMAGE_INCREASE_I(25, CardModifiers.classDamage(ShipClass.FIGHTER, 1)),
    FIGHTER_DAMAGE_INCREASE_II(50, CardModifiers.classDamage(ShipClass.FIGHTER, 2)),

    //Class HP
    FIGHTER_HP_INCREASE_II(60, CardModifiers.classHP(ShipClass.FIGHTER, 2)),
    //Type HP
    BOMBER_HP_INCREASE_II(25, CardModifiers.classHP(ShipClass.FIGHTER, 2)),

    //Ammo increase
    BOMBER_AMMO_INCREASE(70, CardModifiers.ammoIncrease(FighterType.BOMBER, 1));

    public final int value;
    public final CardModifier modifier;
    public final Color textColour;

    CardAttribute(int value, CardModifier modifier) {
        this.value = value;
        this.modifier = modifier;
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
}
