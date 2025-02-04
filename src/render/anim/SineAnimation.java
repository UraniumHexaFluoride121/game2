package render.anim;

import network.PacketWriter;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class SineAnimation implements AnimationTimer {
    private long startTime = System.currentTimeMillis();
    private final float period, initialAngle;

    public SineAnimation(float period, float initialAngle) {
        this.period = period;
        this.initialAngle = (float) Math.toRadians(initialAngle);
    }

    public SineAnimation(DataInputStream reader) throws IOException {
        period = reader.readFloat();
        initialAngle = reader.readFloat();
        startTime = reader.readLong();
    }

    @Override
    public void startTimer() {
        startTime = System.currentTimeMillis();
    }

    @Override
    public boolean finished() {
        return false;
    }

    @Override
    public float normalisedProgress() {
        return (float) Math.sin((System.currentTimeMillis() - startTime) / 1000f * Math.PI * 2 / period + initialAngle);
    }

    @Override
    public void write(DataOutputStream w) throws IOException {
        PacketWriter.writeEnum(AnimationType.SINE, w);
        w.writeFloat(period);
        w.writeFloat(initialAngle);
        w.writeLong(startTime);
    }
}
