package render.anim.sequence;

import network.PacketReceiver;
import render.anim.timer.*;

import java.io.DataInputStream;
import java.io.IOException;

public enum AnimationType {
    LERP, SINE, EXP, POW;

    public static AnimationTimer readTimer(DataInputStream reader) throws IOException {
        AnimationType type = PacketReceiver.readEnum(AnimationType.class, reader);
        return switch (type) {
            case SINE -> new SineAnimation(reader);
            case POW -> new PowAnimation(reader);
            case EXP -> new ExpAnimation(reader);
            case LERP -> new LerpAnimation(reader);
        };
    }

    public static ReversableAnimationTimer readReversableTimer(DataInputStream reader) throws IOException {
        AnimationType type = PacketReceiver.readEnum(AnimationType.class, reader);
        return switch (type) {
            case SINE -> null;
            case POW -> new PowAnimation(reader);
            case EXP -> new ExpAnimation(reader);
            case LERP -> new LerpAnimation(reader);
        };
    }
}
