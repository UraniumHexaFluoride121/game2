package save;

import java.io.*;
import java.util.TreeMap;
import java.util.function.BiConsumer;

public abstract class SaveManager {
    private static final String SAVE_FILE_NAME = "saves.bin";
    private static TreeMap<String, GameSave> gameSaves = new TreeMap<>();

    public static void addSave(GameSave save, String name) {
        gameSaves.put(name, save);
        save();
    }

    public static void save() {
        try {
            FileOutputStream file = new FileOutputStream(SAVE_FILE_NAME);
            ObjectOutputStream w = new ObjectOutputStream(file);
            w.writeObject(gameSaves);
            file.close();
            w.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void loadSaves() {
        try {
            FileInputStream file = new FileInputStream(SAVE_FILE_NAME);
            ObjectInputStream reader = new ObjectInputStream(file);
            Object o = reader.readObject();
            if (!(o instanceof TreeMap))
                throw new RuntimeException();
            gameSaves = (TreeMap<String, GameSave>) o;
            gameSaves.forEach((name, s) -> s.load());
            file.close();
            reader.close();
        } catch (IOException | ClassNotFoundException | RuntimeException e) {
            gameSaves = new TreeMap<>();
        }
    }

    public static void removeSave(String name) {
        gameSaves.remove(name);
        save();
    }

    public static boolean containsSave(String name) {
        return gameSaves.containsKey(name);
    }

    public static void forEachSave(BiConsumer<String, GameSave> action) {
        gameSaves.forEach(action);
    }
}
