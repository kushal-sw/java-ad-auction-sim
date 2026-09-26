package auction.model;

import auction.exceptions.InvalidDataException;

import java.time.LocalDateTime;

/**
 * Represents a single bid placed by a buyer on an auction.
 */
public class Bid {

    private final String bidId;
    private final String auctionId;
    private final String buyerId;
    private double amount;
    private final LocalDateTime timestamp;

    /**
     * Creates a new Bid.
     *
     * @throws InvalidDataException if any required field is invalid
     */
    public Bid(String bidId, String auctionId, String buyerId,
               double amount, LocalDateTime timestamp) throws InvalidDataException {
        if (bidId == null || bidId.isBlank()) {
            throw new InvalidDataException("Bid ID cannot be null or blank.");
        }
        if (auctionId == null || auctionId.isBlank()) {
            throw new InvalidDataException("Auction ID cannot be null or blank.");
        }
        if (buyerId == null || buyerId.isBlank()) {
            throw new InvalidDataException("Buyer ID cannot be null or blank.");
        }
        if (amount <= 0) {
            throw new InvalidDataException("Bid amount must be greater than zero.");
        }
        if (timestamp == null) {
            throw new InvalidDataException("Bid timestamp cannot be null.");
        }
        this.bidId = bidId;
        this.auctionId = auctionId;
        this.buyerId = buyerId;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    // ── Getters ──────────────────────────────────────────────

    public String getBidId() {
        return bidId;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    // ── Setters (mutable fields only) ────────────────────────

    public void setAmount(double amount) {
        this.amount = amount;
    }

    // ── Object overrides ─────────────────────────────────────

    @Override
    public String toString() {
        return "Bid{bidId='%s', auctionId='%s', buyerId='%s', amount=%.2f, timestamp=%s}"
                .formatted(bidId, auctionId, buyerId, amount, timestamp);
    }
}
