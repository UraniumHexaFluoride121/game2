package foundation.input;

public interface InputReceiver {
    void acceptPressed(InputType type);
    void acceptReleased(InputType type);
}
