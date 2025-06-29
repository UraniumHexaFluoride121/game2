package level;

import mainScreen.TitleScreen;
import network.Writable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;

public class GameplaySettings implements Serializable, Writable {
    public final boolean isFoWEnabled, showFiringAnim;

    public GameplaySettings(TitleScreen titleScreen) {
        isFoWEnabled = titleScreen.toggleFoW.getText().equals("Enabled");
        showFiringAnim = titleScreen.showFiringAnim.getText().equals("Enabled");
    }

    public GameplaySettings(boolean isFoWEnabled, boolean showFiringAnim) {
        this.isFoWEnabled = isFoWEnabled;
        this.showFiringAnim = showFiringAnim;
    }

    public GameplaySettings(DataInputStream reader) throws IOException {
        isFoWEnabled = reader.readBoolean();
        showFiringAnim = reader.readBoolean();
    }

    @Override
    public void write(DataOutputStream w) throws IOException {
        w.writeBoolean(isFoWEnabled);
        w.writeBoolean(showFiringAnim);
    }
}
