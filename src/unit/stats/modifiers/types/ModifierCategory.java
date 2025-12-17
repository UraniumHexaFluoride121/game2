package unit.stats.modifiers.types;

import render.UIColourTheme;
import render.types.text.StyleElement;
import render.types.text.TextRenderable;
import unit.stats.Article;
import unit.stats.ColouredName;
import unit.stats.NameArticle;
import unit.stats.attribute.UnitAttribute;

import java.awt.*;
import java.util.function.Function;

import static render.types.text.StyleElement.*;
import static render.types.text.TextRenderable.*;

public enum ModifierCategory implements ColouredName, NameArticle {
    DAMAGE("Damage", "", Article.A, MODIFIER_DAMAGE, DAMAGE_ICON, 1),
    INCOMING_DAMAGE("Incoming Damage", "", Article.AN, MODIFIER_INCOMING_DAMAGE, DAMAGE_ICON, 1),
    SHIELD_DAMAGE("Shield Damage", "", Article.A, MODIFIER_SHIELD_DAMAGE, null, 1),
    INCOMING_SHIELD_DAMAGE("Incoming Shield Damage", "", Article.AN, MODIFIER_INCOMING_SHIELD_DAMAGE, null, 1),

    //Additive
    REPAIR("Repair", "", Article.A, MODIFIER_REPAIR, REPAIR_ICON, 0),
    HP("Max HP", "", Article.A, MODIFIER_HP, HP_ICON, 0),
    SHIELD_HP("Max Shield HP", "", Article.A, MODIFIER_SHIELD_HP, SHIELD_ICON, 0),
    SHIELD_REGEN("Shield Regeneration", "", Article.A, MODIFIER_SHIELD_REGEN, SHIELD_REGEN_ICON, 0),
    FIRING_RANGE("Firing Range", "", Article.A, MODIFIER_FIRING_RANGE, RANGE_ICON, 0),
    AMMO_CAPACITY("Ammo Capacity", "Ammo Cap.", "", Article.AN, MODIFIER_AMMO_CAPACITY, AMMO_ICON, 0),
    VIEW_RANGE("View Range", "", Article.A, MODIFIER_VIEW_RANGE, VIEW_RANGE_ICON, 0),
    MINING_INCOME("Mining Income", "", Article.A, MODIFIER_MINING, ENERGY_ICON, 0),
    INCOME("Income", "/ Turn", Article.AN, MODIFIER_INCOME, ENERGY_ICON, 0),

    //Action
    MINING("Mining", "", Article.A, MODIFIER_MINING, ENERGY_ICON, 0),
    ACTION_COST("Action Cost", "", Article.AN, MODIFIER_ACTION_COST, ENERGY_ICON, 0),
    ACTION_COST_PER_TURN("Per Turn Action Cost", "", Article.A, MODIFIER_ACTION_COST, ENERGY_ICON, 0),

    //Attribute
    DEFENCE_NETWORK(UnitAttribute.DEFENCE_NETWORK.getName(), "", Article.A, ATTRIBUTE_DEFENCE_NETWORK, DEFENCE_NETWORK_ICON, 0),

    //Display only
    MOVEMENT_SPEED_DISPLAY("Move Distance", "", Article.A, MODIFIER_MOVEMENT_SPEED, MOVE_ICON, 1),
    MOVEMENT_COST_DISPLAY("Move Cost", "", Article.A, MODIFIER_MOVEMENT_SPEED, MOVE_ICON, 1),
    CONCEALMENT("Concealment", "", Article.A, MODIFIER_VIEW_RANGE, VIEW_RANGE_ICON, 1),
    WEAPON_EFFECTIVENESS("Weapon Effectiveness", "", Article.A, MODIFIER_DAMAGE, DAMAGE_ICON, 1),
    WEAPON_EFFECTIVENESS_SHORT("Effectiveness", "", Article.AN, MODIFIER_DAMAGE, DAMAGE_ICON, 1),
    NON_MAX_HP("HP", "", Article.A, MODIFIER_HP, HP_ICON, 0),

    MOVEMENT_COST_ALL("Move Cost (All)", "(All)", "", Article.A, MODIFIER_MOVEMENT_SPEED, MOVE_ICON, 1),
    MOVEMENT_COST_EMPTY("Move Cost (Empty)", "(Empty)", "", Article.A, MODIFIER_MOVEMENT_SPEED, MOVE_ICON, 1),
    MOVEMENT_COST_NEBULA("Move Cost (Nebula)", "(Nebula)", "", Article.A, MODIFIER_MOVEMENT_SPEED, MOVE_ICON, 1),
    MOVEMENT_COST_DENSE_NEBULA("Move Cost (Dense Nebula)", "(Dense Nebula)", "", Article.A, MODIFIER_MOVEMENT_SPEED, MOVE_ICON, 1),
    MOVEMENT_COST_ASTEROIDS("Move Cost (Asteroids)", "(Asteroids)", "", Article.A, MODIFIER_MOVEMENT_SPEED, MOVE_ICON, 1);

    public static final UIColourTheme DAMAGE_BACKGROUND = UIColourTheme.createBoxTheme(new UIColourTheme(
            new Color(213, 99, 99), new Color(193, 90, 90)
    ));
    public static final UIColourTheme HP_GREEN = UIColourTheme.createBoxTheme(UIColourTheme.DEEP_GREEN);
    public static final UIColourTheme HP_YELLOW = UIColourTheme.createBoxTheme(UIColourTheme.DEEP_YELLOW);
    public static final UIColourTheme HP_RED = UIColourTheme.createBoxTheme(UIColourTheme.DEEP_RED);

    private final String name, shortenedName, effectValuePost;
    private final Article article;
    public final StyleElement textColour;
    private final TextRenderable icon;
    public final float identity;
    public final UIColourTheme colour;

    ModifierCategory(String name, String effectValuePost, Article article, StyleElement textColour, TextRenderable icon, float identity) {
        this(name, name, effectValuePost, article, textColour, icon, identity);
    }

    ModifierCategory(String name, String shortenedName, String effectValuePost, Article article, StyleElement textColour, TextRenderable icon, float identity) {
        this.name = name;
        this.shortenedName = shortenedName;
        this.effectValuePost = effectValuePost;
        this.article = article;
        this.textColour = textColour;
        this.icon = icon;
        this.identity = identity;
        colour = UIColourTheme.createBoxTheme(textColour);
    }

    @Override
    public String getName() {
        return name;
    }

    public String getShortenedName() {
        return shortenedName;
    }

    public String icon() {
        return icon == null ? "" : icon.display;
    }

    @Override
    public String colour() {
        return textColour.display;
    }

    public String displayEffectName() {
        return colouredName(null, false);
    }

    public String displayEffectValue(float effect, Function<Float, String> displayMethod) {
        return colour() + displayMethod.apply(effect) + icon() + effectValuePost;
    }

    @Override
    public Article getArticleEnum() {
        return article;
    }
}
