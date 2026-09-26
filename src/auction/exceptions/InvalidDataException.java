package auction.exceptions;

/**
 * Thrown when user-supplied data is invalid — for example, blank names,
 * negative prices, or auction dates in the past.
 */
public class InvalidDataException extends Exception {

    public InvalidDataException(String message) {
        super(message);
    }
}
