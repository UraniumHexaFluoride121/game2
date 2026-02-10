package render.save;

import foundation.IInternalName;

/**
 * An interface that allows for enum-like serialization of static objects. When implemented on
 * a {@code Serializable} class {@code T}, all subclasses of the {@code T} should have the following code:
 * <pre>
 * {@code
 * @Serial
 * private Object writeReplace() throws ObjectStreamException {
 *    return new SerializationProxy<>(T.class, this);
 * }}
 * </pre>
 * Not only that, but the superclass {@code T} should have a method {@code valueOf(String name)}
 * that returns the correct static instance:
 * <pre>{@code
 * public static T valueOf(String name) {
 *     //return the static instance with the specified name
 * }
 * }
 * </pre>
 */
public interface SerializedByProxy extends IInternalName {
}
