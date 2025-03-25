package level;

import mainScreen.TitleScreen;
import network.Writable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;

public class GameplaySettings implements Serializable, Writable {
    public final boolean isFoWEnabled;

    public GameplaySettings(TitleScreen titleScreen) {
        isFoWEnabled = titleScreen.toggleFoW.isSelected();
    }

    public GameplaySettings(DataInputStream reader) throws IOException {
        isFoWEnabled = reader.readBoolean();
    }

    @Override
    public void write(DataOutputStream w) throws IOException {
        w.writeBoolean(isFoWEnabled);
    }
}
