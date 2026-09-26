package auction.exceptions;

/**
 * Thrown when a bid is invalid — for example, when the bid amount is
 * below the current highest bid or the auction is already closed.
 */
public class InvalidBidException extends Exception {

    public InvalidBidException(String message) {
        super(message);
    }
}
