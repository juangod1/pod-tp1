package grupo2.api.model;

public class ElectionStateException extends Exception {
    public ElectionStateException() {
    }

    public ElectionStateException(String s) {
        super(s);
    }

    public ElectionStateException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public ElectionStateException(Throwable throwable) {
        super(throwable);
    }
}
