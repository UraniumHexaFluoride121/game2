package singleplayer.card;

import foundation.WeightedSelector;
import foundation.math.RandomHandler;
import level.TeamData;
import unit.ShipClass;
import unit.type.UnitType;

import java.util.*;

public class AttributeHandler {
    public HashMap<Object[], AttributeGroup> groups = new HashMap<>();
    public HashMap<CardAttribute, HashSet<AttributeGroup>> groupForAttribute = new HashMap<>();

    public AttributeHandler() {
        for (ShipClass value : ShipClass.values()) {
            groups.put(new Object[]{value}, new AttributeGroup(10, 10));
            groups.put(new Object[]{value, AttributeProperty.UNIQUE}, new AttributeGroup(Integer.MAX_VALUE, 8));
        }
        for (UnitType value : UnitType.ORDERED_UNIT_TYPES) {
            groups.put(new Object[]{value}, new AttributeGroup(10, 10));
            groups.put(new Object[]{value, AttributeProperty.UNIQUE}, new AttributeGroup(Integer.MAX_VALUE, 3));
            groups.put(new Object[]{value, AttributeProperty.ADD_UNIT}, new AttributeGroup(5, Integer.MAX_VALUE));
        }
        groups.put(new Object[]{AttributeProperty.ADD_UNIT}, new AttributeGroup(15, Integer.MAX_VALUE));
        groups.put(new Object[]{AttributeProperty.ACTION_COST}, new AttributeGroup(10, 10));
        groups.put(new Object[]{AttributeProperty.DEFENCE}, new AttributeGroup(30, 20));
        groups.put(new Object[]{AttributeProperty.OFFENCE}, new AttributeGroup(30, 20));
        groups.put(new Object[]{AttributeProperty.MOVE_SPEED}, new AttributeGroup(10, 10));
        groups.put(new Object[]{AttributeProperty.INCOME}, new AttributeGroup(10, 10));
        groups.put(new Object[]{AttributeProperty.UNIQUE}, new AttributeGroup(Integer.MAX_VALUE, Integer.MAX_VALUE));
        for (Map.Entry<Object[], AttributeGroup> entry : groups.entrySet()) {
            for (CardAttribute attribute : CardAttribute.values.values()) {
                for (AttributeProperty property : attribute.properties) {
                    if (Arrays.deepEquals(property.obj(), entry.getKey())) {
                        groupForAttribute.putIfAbsent(attribute, new HashSet<>());
                        groupForAttribute.get(attribute).add(entry.getValue());
                        entry.getValue().registerAttribute(attribute, property.value());
                        break;
                    }
                }
            }
        }
    }

    public void addCardAttribute(CardAttribute attribute) {
        for (Map.Entry<Object[], AttributeGroup> entry : groups.entrySet()) {
            for (AttributeProperty property : attribute.properties) {
                if (Arrays.deepEquals(property.obj(), entry.getKey())) {
                    entry.getValue().addCardAttribute(attribute);
                    break;
                }
            }
        }
    }

    public void removeCardAttribute(CardAttribute attribute) {
        for (Map.Entry<Object[], AttributeGroup> entry : groups.entrySet()) {
            for (AttributeProperty property : attribute.properties) {
                if (Arrays.deepEquals(property.obj(), entry.getKey())) {
                    entry.getValue().removeCardAttribute(attribute);
                    break;
                }
            }
        }
    }

    public void addCard(Card card) {
        for (CardAttribute attribute : card.attributes) {
            addCardAttribute(attribute);
        }
    }

    public void removeCard(Card card) {
        for (CardAttribute attribute : card.attributes) {
            removeCardAttribute(attribute);
        }
    }

    public Card generateCard(TeamData data, WeightedSelector<CardGenerationGroup> cards, int targetCost, int allowedError) {
        AttributeGroup group;
        ArrayList<CardAttribute> attributes;
        CardGenerationGroup cg;
        while (true) {
            int currentCost = 0;
            cg = cards.get();
            cg.reset();
            attributes = new ArrayList<>();
            while (true) {
                group = cg.next(this);
                if (group == null) {
                    break;
                }
                ArrayList<CardAttribute> array = new ArrayList<>(group.attributeValue.keySet());
                if (array.isEmpty()) {
                    continue;
                }
                CardAttribute attribute = RandomHandler.randomFromArray(array);
                attributes.add(attribute);
                if (!canAttributesBeAdded(attributes, data)) {
                    attributes.remove(attribute);
                    continue;
                }
                currentCost += attribute.cost;
                if (Math.abs(currentCost - targetCost) <= allowedError) {
                    break;
                }
            }
            if (Math.abs(currentCost - targetCost) <= allowedError) {
                break;
            }
        }
        return new Card(cg.cardType, attributes.toArray(CardAttribute[]::new));
    }

    public boolean canAttributesBeAdded(ArrayList<CardAttribute> attributes, TeamData data) {
        HashMap<AttributeGroup, Float> groups = new HashMap<>();
        HashMap<AttributeGroup, Integer> groupsCount = new HashMap<>();
        HashMap<CardAttribute, Integer> attributeCount = new HashMap<>(), currentCount = data.getAttributeCounts();
        for (CardAttribute attribute : attributes) {
            attributeCount.putIfAbsent(attribute, 0);
            attributeCount.compute(attribute, (k, v) -> v + 1);
        }
        for (Map.Entry<CardAttribute, Integer> entry : attributeCount.entrySet()) {
            if (entry.getValue() + currentCount.getOrDefault(entry.getKey(), 0) > entry.getKey().maxCount) {
                return false;
            }
        }
        for (AttributeGroup group : this.groups.values()) {
            for (CardAttribute attribute : attributes) {
                if (group.attributeValue.containsKey(attribute)) {
                    groupsCount.putIfAbsent(group, 0);
                    groupsCount.compute(group, (k, v) -> v + 1);
                    groups.putIfAbsent(group, 0f);
                    groups.compute(group, (k, v) -> v + group.attributeValue.get(attribute));
                }
            }
        }
        for (AttributeGroup group : this.groups.values()) {
            if (group.maxSize < group.attributes.size() + groupsCount.getOrDefault(group, 0)) {
                return false;
            }
            if (group.maxValue < group.value + groups.getOrDefault(group, 0f)) {
                return false;
            }
        }
        return true;
    }

    public AttributeGroup getGroup(Object... identifiers) {
        for (Map.Entry<Object[], AttributeGroup> entry : groups.entrySet()) {
            if (Arrays.equals(entry.getKey(), identifiers))
                return entry.getValue();
        }
        return null;
    }

    @Override
    public String toString() {
        return groups.toString() + "\n" + groupForAttribute.toString();
    }
}
