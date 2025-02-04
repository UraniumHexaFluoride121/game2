package network;

import java.awt.*;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public record PacketReceiver(PacketType type, Consumer<DataInputStream> reader) {
    public static <T extends Enum<T>> T readEnum(Class<T> clazz, DataInputStream reader) throws IOException {
        return clazz.getEnumConstants()[reader.readInt()];
    }

    public static Point readPoint(DataInputStream reader) throws IOException {
        return new Point(reader.readInt(), reader.readInt());
    }

    public static <T extends Map<K, V>, K, V> T readMap(T map, ReaderSupplier<K> keyReader, ReaderSupplier<V> valueReader, DataInputStream reader) throws IOException {
        int size = reader.readInt();
        for (int i = 0; i < size; i++) {
            map.put(keyReader.get(), valueReader.get());
        }
        return map;
    }

    public static <T extends Collection<E>, E> T readCollection(T collection, ReaderSupplier<E> valueReader, DataInputStream reader) throws IOException {
        int size = reader.readInt();
        for (int i = 0; i < size; i++) {
            collection.add(valueReader.get());
        }
        return collection;
    }
}
