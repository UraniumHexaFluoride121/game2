package singleplayer.card;

import java.util.ArrayList;
import java.util.HashMap;

public class AttributeGroup {
    public final float maxValue;
    public final int maxSize;
    public float value = 0;
    public HashMap<CardAttribute, Float> attributeValue = new HashMap<>();
    public ArrayList<CardAttribute> attributes = new ArrayList<>();

    public AttributeGroup(float maxValue, int maxSize) {
        this.maxValue = maxValue;
        this.maxSize = maxSize;
    }

    public void registerAttribute(CardAttribute attribute, float value) {
        attributeValue.put(attribute, value);
    }

    public void addCardAttribute(CardAttribute attribute) {
        if (attributeValue.containsKey(attribute)) {
            value += attributeValue.get(attribute);
            attributes.add(attribute);
        }
    }

    public void removeCardAttribute(CardAttribute attribute) {
        if (attributeValue.containsKey(attribute)) {
            value -= attributeValue.get(attribute);
            attributes.remove(attribute);
        }
    }

    @Override
    public String toString() {
        return "{Attributes, val: " + value + ", registered: " + attributeValue + ", stored: " + attributes + "}";
    }
}
