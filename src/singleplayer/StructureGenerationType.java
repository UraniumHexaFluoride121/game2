package singleplayer;

import foundation.math.MathUtil;
import foundation.math.RandomHandler;
import level.structure.StructureType;
import mainScreen.StructureGenerationPreset;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public enum StructureGenerationType {
    DEFAULT(
            new StructureGenerationPreset()
                    .add(StructureType.REFINERY, 1, 0),
            new StructureGenerationPreset()
                    .add(StructureType.REFINERY, 3, 1),
            3, 6, 0, 2);

    public final StructureGenerationPreset from, to;
    public final int nMin, nMax, cMin, cMax;

    StructureGenerationType(StructureGenerationPreset from, StructureGenerationPreset structureGenerationPreset, int nMin, int nMax, int cMin, int cMax) {
        this.from = from;
        to = structureGenerationPreset;
        this.nMin = nMin;
        this.nMax = nMax;
        this.cMin = cMin;
        this.cMax = cMax;
    }

    public StructureGenerationPreset getPreset(Supplier<Double> random) {
        ArrayList<StructureType> neutralStructs = new ArrayList<>();
        from.neutralMap.forEach((type, count) -> {
            int c = MathUtil.randIntBetween(count, to.neutralMap.get(type), random);
            for (int i = 0; i < c; i++) {
                neutralStructs.add(type);
            }
        });
        RandomHandler.randomise(neutralStructs, random);
        List<StructureType> finalNeutralStructs = neutralStructs.subList(0, Math.min(neutralStructs.size(), MathUtil.randIntBetween(nMin, nMax, random)));
        ArrayList<StructureType> capturedStructs = new ArrayList<>();
        from.capturedMap.forEach((type, count) -> {
            int c = MathUtil.randIntBetween(count, to.capturedMap.get(type), random);
            for (int i = 0; i < c; i++) {
                capturedStructs.add(type);
            }
        });
        RandomHandler.randomise(capturedStructs, random);
        List<StructureType> finalCapturedStructs = capturedStructs.subList(0, Math.min(capturedStructs.size(), MathUtil.randIntBetween(cMin, cMax, random)));
        StructureGenerationPreset preset = new StructureGenerationPreset();
        for (StructureType type : finalNeutralStructs) {
            preset.incrementNeutral(type, 1);
        }
        for (StructureType type : finalCapturedStructs) {
            preset.incrementCaptured(type, 1);
        }
        return preset;
    }
}
