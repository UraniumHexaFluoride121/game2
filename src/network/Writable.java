package network;

import java.io.DataOutputStream;
import java.io.IOException;

public interface Writable {
    void write(DataOutputStream w) throws IOException;
}
