package render.anim;

import network.PacketWriter;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ExpAnimation implements ReversableAnimationTimer {
    private long startTime;
    private final float cutoff;
    private boolean reversed = false;
    private final float decayConstant;

    public ExpAnimation(float cutoff, float decayConstant) {
        this.cutoff = cutoff;
        this.decayConstant = decayConstant;
        startTimer();
    }

    public ExpAnimation(DataInputStream reader) throws IOException {
        startTime = reader.readLong();
        cutoff = reader.readFloat();
        reversed = reader.readBoolean();
        decayConstant = reader.readFloat();
    }

    @Override
    public void startTimer() {
        startTime = System.currentTimeMillis();
    }

    @Override
    public void setReversed(boolean reversed) {
        if (this.reversed != reversed) {
            float progress = normalisedProgress();
            if (reversed) {
                startTime = System.currentTimeMillis() - (long) ((-Math.log(progress) / decayConstant) * 1000f);
            } else
                startTime = System.currentTimeMillis() - (long) ((-Math.log(1 - progress) / decayConstant) * 1000f);
        }
        this.reversed = reversed;
    }

    @Override
    public boolean reversed() {
        return reversed;
    }

    @Override
    public boolean finished() {
        return normalisedProgress() >= cutoff;
    }

    @Override
    public float normalisedProgress() {
        float exp = (float) Math.exp(-decayConstant * (System.currentTimeMillis() - startTime) / 1000f);
        if (reversed)
            return exp;
        return 1 - exp;
    }

    @Override
    public void write(DataOutputStream w) throws IOException {
        PacketWriter.writeEnum(AnimationType.EXP, w);
        w.writeLong(startTime);
        w.writeFloat(cutoff);
        w.writeBoolean(reversed);
        w.writeFloat(decayConstant);
    }
}