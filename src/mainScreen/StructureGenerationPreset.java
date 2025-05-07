package mainScreen;

import level.structure.StructureType;

import java.util.ArrayList;
import java.util.HashMap;

public class StructureGenerationPreset {
    public HashMap<StructureType, Integer> neutralMap = new HashMap<>();
    public HashMap<StructureType, Integer> capturedMap = new HashMap<>();

    public StructureGenerationPreset() {
        for (StructureType s : StructureType.SPAWNABLE_TYPES) {
            add(s, 0, 0);
        }
    }

    public StructureGenerationPreset add(StructureType type, int neutral, int captured) {
        neutralMap.put(type, neutral);
        capturedMap.put(type, captured);
        return this;
    }

    public int neutralCount() {
        return neutralMap.values().stream().reduce(Integer::sum).get();
    }

    public int capturedCount() {
        return capturedMap.values().stream().reduce(Integer::sum).get();
    }

    public ArrayList<StructureType> getList(boolean neutral) {
        ArrayList<StructureType> list = new ArrayList<>();
        (neutral ? neutralMap : capturedMap).forEach((t, count) -> {
            for (int i = 0; i < count; i++) {
                list.add(t);
            }
        });
        return list;
    }
}
