package network;

import java.awt.*;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public record PacketWriter(PacketType type, WriterConsumer<DataOutputStream> writer) {
    public static <T extends Enum<T>> void writeEnum(T value, DataOutputStream writer) throws IOException {
        writer.writeInt(value.ordinal());
    }

    public static void writePoint(Point p, DataOutputStream writer) throws IOException {
        writer.writeInt(p.x);
        writer.writeInt(p.y);
    }

    public static <K, V> void writeMap(Map<K, V> map, WriterConsumer<K> keyWriter, WriterConsumer<V> valueWriter, DataOutputStream writer) throws IOException {
        writer.writeInt(map.size());
        Set<Map.Entry<K, V>> entries = map.entrySet();
        for (Map.Entry<K, V> entry : entries) {
            keyWriter.accept(entry.getKey());
            valueWriter.accept(entry.getValue());
        }
    }

    public static <E> void writeCollection(Collection<E> collection, WriterConsumer<E> valueWriter, DataOutputStream writer) throws IOException {
        writer.writeInt(collection.size());
        for (E e : collection) {
            valueWriter.accept(e);
        }
    }
}
