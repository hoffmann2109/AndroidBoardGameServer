package at.aau.serg.monopoly.websoket.exception;

public class PropertyDataLoadException extends RuntimeException {
    public PropertyDataLoadException(String message) {
        super(message);
    }

    public PropertyDataLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
