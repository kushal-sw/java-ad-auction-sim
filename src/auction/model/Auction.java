package auction.model;

import auction.enums.AuctionStatus;
import auction.enums.PaymentStatus;
import auction.exceptions.InvalidDataException;

import java.time.LocalDateTime;

/**
 * Represents an auction for a specific product, with a time window,
 * a reference to the current highest bid, and winner/payment tracking.
 */
public class Auction {

    private final String auctionId;
    private final String productId;
    private AuctionStatus status;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private Bid highestBid;
    private String winnerId;
    private PaymentStatus paymentStatus;

    /**
     * Creates a new Auction.
     *
     * @throws InvalidDataException if any required field is invalid or
     *                              endTime is not after startTime
     */
    public Auction(String auctionId, String productId,
                   LocalDateTime startTime, LocalDateTime endTime) throws InvalidDataException {
        if (auctionId == null || auctionId.isBlank()) {
            throw new InvalidDataException("Auction ID cannot be null or blank.");
        }
        if (productId == null || productId.isBlank()) {
            throw new InvalidDataException("Product ID cannot be null or blank.");
        }
        if (startTime == null) {
            throw new InvalidDataException("Auction start time cannot be null.");
        }
        if (endTime == null) {
            throw new InvalidDataException("Auction end time cannot be null.");
        }
        if (!endTime.isAfter(startTime)) {
            throw new InvalidDataException("Auction end time must be after start time.");
        }
        this.auctionId = auctionId;
        this.productId = productId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = AuctionStatus.SCHEDULED;
        this.highestBid = null;
        this.winnerId = null;
        this.paymentStatus = null;
    }

    // ── Getters ──────────────────────────────────────────────

    public String getAuctionId() {
        return auctionId;
    }

    public String getProductId() {
        return productId;
    }

    public AuctionStatus getStatus() {
        return status;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public Bid getHighestBid() {
        return highestBid;
    }

    public String getWinnerId() {
        return winnerId;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    // ── Setters (mutable fields only) ────────────────────────

    public void setStatus(AuctionStatus status) {
        this.status = status;
    }

    public void setHighestBid(Bid highestBid) {
        this.highestBid = highestBid;
    }

    public void setWinnerId(String winnerId) {
        this.winnerId = winnerId;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    // ── Object overrides ─────────────────────────────────────

    @Override
    public String toString() {
        String highestBidStr = (highestBid != null)
                ? "%.2f".formatted(highestBid.getAmount())
                : "none";
        String winnerStr = (winnerId != null) ? winnerId : "none";
        String paymentStr = (paymentStatus != null) ? paymentStatus.name() : "N/A";
        return ("Auction{auctionId='%s', productId='%s', status=%s, start=%s, end=%s, "
                + "highestBid=%s, winner=%s, payment=%s}")
                .formatted(auctionId, productId, status, startTime, endTime,
                           highestBidStr, winnerStr, paymentStr);
    }
}
