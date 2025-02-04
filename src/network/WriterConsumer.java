package network;

import java.io.IOException;

@FunctionalInterface
public interface WriterConsumer<T> {
    void accept(T t) throws IOException;
}
