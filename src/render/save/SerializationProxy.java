package render.save;

import java.io.ObjectStreamException;
import java.io.Serial;
import java.io.Serializable;

public class SerializationProxy<T extends SerializedByProxy> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final Class<T> clazz;
    private final String name;

    public <E extends T> SerializationProxy(Class<T> clazz, E v) {
        this.clazz = clazz;
        name = v.getInternalName();
    }

    @Serial
    private Object readResolve() throws ObjectStreamException {
        try {
            return clazz.getMethod("valueOf", String.class)
                    .invoke(null, name);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
