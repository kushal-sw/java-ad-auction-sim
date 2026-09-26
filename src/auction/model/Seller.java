package auction.model;

import auction.exceptions.InvalidDataException;

/**
 * Represents a seller who can list products for auction.
 */
public class Seller {

    private final String sellerId;
    private String name;
    private String email;

    /**
     * Creates a new Seller.
     *
     * @throws InvalidDataException if any required field is blank or null
     */
    public Seller(String sellerId, String name, String email) throws InvalidDataException {
        if (sellerId == null || sellerId.isBlank()) {
            throw new InvalidDataException("Seller ID cannot be null or blank.");
        }
        if (name == null || name.isBlank()) {
            throw new InvalidDataException("Seller name cannot be null or blank.");
        }
        if (email == null || email.isBlank()) {
            throw new InvalidDataException("Seller email cannot be null or blank.");
        }
        this.sellerId = sellerId;
        this.name = name;
        this.email = email;
    }

    // ── Getters ──────────────────────────────────────────────

    public String getSellerId() {
        return sellerId;
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
        return "Seller{sellerId='%s', name='%s', email='%s'}".formatted(sellerId, name, email);
    }
}
