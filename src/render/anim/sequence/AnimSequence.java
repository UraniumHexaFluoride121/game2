package render.anim.sequence;

import foundation.math.MathUtil;

import java.util.Map;
import java.util.TreeMap;

public class AnimSequence implements AnimValue {
    private final TreeMap<Float, Keyframe> keyframes = new TreeMap<>();

    public AnimSequence() {
    }

    public AnimSequence addKeyframe(float time, float value, KeyframeFunction type) {
        keyframes.put(time, new Keyframe(time, value, type));
        return this;
    }

    public AnimSequence endSequence(float time, float value) {
        keyframes.put(time, new Keyframe(time, value, null));
        return this;
    }

    @Override
    public float getValue(float time) {
        Map.Entry<Float, Keyframe> entryA = keyframes.floorEntry(time);
        Map.Entry<Float, Keyframe> entryB = keyframes.ceilingEntry(time);
        if (entryA == null)
            return entryB.getValue().value;
        if (entryB == null)
            return entryA.getValue().value;
        Keyframe a = entryA.getValue();
        Keyframe b = entryB.getValue();
        if (a == b)
            return a.value;
        return MathUtil.lerp(a.value, b.value, a.type.function().apply(MathUtil.normalise(a.time, b.time, time)));
    }

    private record Keyframe(float time, float value, KeyframeFunction type) {
    }
}
