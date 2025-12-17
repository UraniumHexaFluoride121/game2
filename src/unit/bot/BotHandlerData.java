package unit.bot;

import network.Writable;

import java.io.*;

public class BotHandlerData implements Serializable, Writable {
    @Serial
    private static final long serialVersionUID = 1L;

    public int enemiesDestroyed = 0;

    public BotHandlerData() {
    }

    public BotHandlerData(DataInputStream reader) throws IOException {
        enemiesDestroyed = reader.readInt();
    }

    public BotHandlerData copy() {
        BotHandlerData data = new BotHandlerData();
        data.enemiesDestroyed = enemiesDestroyed;
        return data;
    }

    @Override
    public void write(DataOutputStream w) throws IOException {
        w.writeInt(enemiesDestroyed);
    }
}
