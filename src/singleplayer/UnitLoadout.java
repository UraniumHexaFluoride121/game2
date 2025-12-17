package singleplayer;

import unit.type.UnitType;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;

public class UnitLoadout implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public final ArrayList<UnitElement> unitCounts = new ArrayList<>();

    public UnitLoadout() {
    }

    public UnitLoadout addUnit(UnitType type, int count) {
        return addUnit(new UnitElement(type, count));
    }

    private UnitLoadout addUnit(UnitElement element) {
        if (containsType(element.type)) {
            getType(element.type).count += element.count;
        } else
            unitCounts.add(element);
        return this;
    }

    public boolean containsType(UnitType type) {
        for (UnitElement unitCount : unitCounts) {
            if (unitCount.type == type)
                return true;
        }
        return false;
    }

    public UnitElement getType(UnitType type) {
        for (UnitElement unitCount : unitCounts) {
            if (unitCount.type == type)
                return unitCount;
        }
        return null;
    }

    public UnitLoadout copy() {
        UnitLoadout l = new UnitLoadout();
        unitCounts.forEach(c -> l.addUnit(c.type, c.count));
        return l;
    }

    public static class UnitElement implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        public final UnitType type;
        public int count;

        public UnitElement(UnitType type, int count) {
            this.type = type;
            this.count = count;
        }
    }
}
