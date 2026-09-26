package auction.model;

import auction.exceptions.InvalidDataException;

/**
 * Represents a buyer who can place bids on auctions.
 */
public class Buyer {

    private final String buyerId;
    private String name;
    private String email;

    /**
     * Creates a new Buyer.
     *
     * @throws InvalidDataException if any required field is blank or null
     */
    public Buyer(String buyerId, String name, String email) throws InvalidDataException {
        if (buyerId == null || buyerId.isBlank()) {
            throw new InvalidDataException("Buyer ID cannot be null or blank.");
        }
        if (name == null || name.isBlank()) {
            throw new InvalidDataException("Buyer name cannot be null or blank.");
        }
        if (email == null || email.isBlank()) {
            throw new InvalidDataException("Buyer email cannot be null or blank.");
        }
        this.buyerId = buyerId;
        this.name = name;
        this.email = email;
    }

    // ── Getters ──────────────────────────────────────────────

    public String getBuyerId() {
        return buyerId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    // ── Setters (mutable fields only) ────────────────────────

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // ── Object overrides ─────────────────────────────────────

    @Override
    public String toString() {
        return "Buyer{buyerId='%s', name='%s', email='%s'}".formatted(buyerId, name, email);
    }
}
