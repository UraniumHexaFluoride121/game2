package unit;

import foundation.IInternalName;
import render.types.text.TextRenderable;
import unit.stats.Article;
import unit.stats.NameArticle;
import unit.weapon.DamageClass;

public enum ShipClass implements NameArticle, IInternalName {
    FIGHTER("FIGHTER", "Fighter", "Fighters", "Fighter-Class", "Fighter-Class", Article.A, TextRenderable.FIGHTER_ICON),
    CORVETTE("CORVETTE", "Corvette", "Corvettes", "Corvette-Class", "Corvette-Class", Article.A, TextRenderable.CORVETTE_ICON),
    CRUISER("CRUISER", "Cruiser", "Cruisers", "Cruiser-Class", "Cruiser-Class", Article.A, TextRenderable.CRUISER_ICON);

    private final String internalName, name, pluralName, className, classNamePlural;
    private final Article article;
    public final TextRenderable icon;

    ShipClass(String internalName, String name, String pluralName, String className, String classNamePlural, Article article, TextRenderable icon) {
        this.internalName = internalName;
        this.name = name;
        this.pluralName = pluralName;
        this.className = className;
        this.classNamePlural = classNamePlural;
        this.article = article;
        this.icon = icon;
    }

    public DamageClass getDamageType() {
        return switch (this) {
            case FIGHTER -> DamageClass.FIGHTER;
            case CORVETTE -> DamageClass.CORVETTE;
            case CRUISER -> DamageClass.CRUISER;
        };
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getInternalName() {
        return internalName;
    }

    public String getPluralName() {
        return pluralName;
    }

    public String getClassName() {
        return className;
    }

    public String getClassNamePlural() {
        return classNamePlural;
    }

    @Override
    public Article getArticleEnum() {
        return article;
    }
}
