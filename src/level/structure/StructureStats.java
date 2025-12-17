package level.structure;

import level.Level;

public class StructureStats {
    public final StructureType type;

    public StructureStats(StructureType type) {
        this.type = type;
    }

    public float unitRegen(Level level) {
        return type.unitRegen;
    }

    public int energyIncome(Level level) {
        return type.energyIncome;
    }

    public boolean resupply(Level level) {
        return type.resupply;
    }
}
