package unit.stats;

import foundation.NamedEnum;

public interface NameArticle extends NamedEnum {
    Article getArticleEnum();

    default String getArticle() {
        return getArticleEnum().s;
    }

    default String getNameArticle() {
        return getNameArticle(true);
    }

    default String getNameArticle(boolean nameUpperCase) {
        return getArticle() + (nameUpperCase ? getName() : getName().toLowerCase());
    }
}
