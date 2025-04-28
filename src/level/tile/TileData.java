package level.tile;

import network.PacketWriter;
import network.Writable;

import java.io.DataOutputStream;
import java.io.IOException;

public record TileData(TileType type, double randomValue, boolean hasStructure, int miningBarFill) implements Writable {
    @Override
    public void write(DataOutputStream w) throws IOException {
        PacketWriter.writeEnum(type, w);
        w.writeDouble(randomValue);
        w.writeBoolean(hasStructure);
        w.writeInt(miningBarFill);
    }
}
