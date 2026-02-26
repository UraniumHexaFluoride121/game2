package save;

import render.texture.AssetManager;
import render.texture.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.TreeMap;
import java.util.function.BiConsumer;

public class SaveManager<T extends LoadedFromSave> {
    private final String saveFileName;
    public TreeMap<String, T> gameSaves = new TreeMap<>();

    public SaveManager(String saveFileName) {
        this.saveFileName = saveFileName;
    }

    public void addSave(T save, String name) {
        gameSaves.put(name, save);
        save();
    }

    public void save() {
        try {
            FileOutputStream file = new FileOutputStream(saveFileName);
            ObjectOutputStream w = new ObjectOutputStream(file);
            w.writeObject(gameSaves);
            file.close();
            w.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void loadSavesExternal() {
        try {
            FileInputStream file = new FileInputStream(saveFileName);
            ObjectInputStream reader = new ObjectInputStream(file);
            Object o = reader.readObject();
            if (!(o instanceof TreeMap))
                throw new RuntimeException();
            gameSaves = (TreeMap<String, T>) o;
            gameSaves.forEach((name, s) -> s.load());
            reader.close();
        } catch (IOException | ClassNotFoundException | RuntimeException e) {
            gameSaves = new TreeMap<>();
        }
    }

    public void loadSavesInternal() {
        ResourceLocation resource = new ResourceLocation(saveFileName);
        try {

            InputStream inputStream = AssetManager.getResource(resource);
            ObjectInputStream reader = new ObjectInputStream(inputStream);
            Object o = reader.readObject();
            if (!(o instanceof TreeMap))
                throw new RuntimeException();
            gameSaves = (TreeMap<String, T>) o;
            gameSaves.forEach((name, s) -> s.load());
            reader.close();
        } catch (IOException | ClassNotFoundException | RuntimeException e) {
            gameSaves = new TreeMap<>();
        }
    }

    public void removeSave(String name) {
        gameSaves.remove(name);
        save();
    }

    public boolean containsSave(String name) {
        return gameSaves.containsKey(name);
    }

    public void forEachSave(BiConsumer<String, T> action) {
        gameSaves.forEach(action);
    }
}
