package network;

import java.io.IOException;

@FunctionalInterface
public interface ReaderSupplier<T> {
    T get() throws IOException;
}
