package render;

public interface RenderRegister<T extends Renderable> extends Renderable {
    void register(T r);
    void remove(T r);
}
