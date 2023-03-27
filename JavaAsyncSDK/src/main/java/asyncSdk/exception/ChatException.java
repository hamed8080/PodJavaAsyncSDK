package asyncSdk.exception;

public abstract class ChatException extends Exception{
    private String message;
    private int code;

    public ChatException(String message, int code) {
        this.message = message;
        this.code = code;
    }
}
