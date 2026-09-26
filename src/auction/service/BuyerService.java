package auction.service;

import auction.exceptions.InvalidDataException;
import auction.model.Buyer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * In-memory CRUD service for {@link Buyer} entities.
 */
public class BuyerService {

    private final List<Buyer> buyers = new ArrayList<>();

    /**
     * Adds a new buyer. Rejects duplicates by ID.
     *
     * @throws InvalidDataException if a buyer with the same ID already exists
     */
    public void addBuyer(Buyer buyer) throws InvalidDataException {
        if (findById(buyer.getBuyerId()) != null) {
            throw new InvalidDataException(
                    "Buyer with ID '%s' already exists.".formatted(buyer.getBuyerId()));
        }
        buyers.add(buyer);
    }

    /**
     * Updates an existing buyer's mutable fields (name, email).
     *
     * @throws InvalidDataException if the buyer is not found
     */
    public void updateBuyer(Buyer updated) throws InvalidDataException {
        Buyer existing = findById(updated.getBuyerId());
        if (existing == null) {
            throw new InvalidDataException(
                    "Buyer with ID '%s' not found.".formatted(updated.getBuyerId()));
        }
        existing.setName(updated.getName());
        existing.setEmail(updated.getEmail());
    }

    /**
     * Deletes a buyer by ID.
     *
     * @throws InvalidDataException if the buyer is not found
     */
    public void deleteBuyer(String buyerId) throws InvalidDataException {
        Buyer existing = findById(buyerId);
        if (existing == null) {
            throw new InvalidDataException(
                    "Buyer with ID '%s' not found.".formatted(buyerId));
        }
        buyers.remove(existing);
    }

    /**
     * Finds a buyer by exact ID, or returns {@code null} if not found.
     */
    public Buyer findById(String buyerId) {
        return buyers.stream()
                .filter(b -> b.getBuyerId().equals(buyerId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Finds buyers whose name contains the given substring (case-insensitive).
     */
    public List<Buyer> findByName(String name) {
        String lower = name.toLowerCase();
        return buyers.stream()
                .filter(b -> b.getName().toLowerCase().contains(lower))
                .collect(Collectors.toList());
    }

    /**
     * Returns an unmodifiable view of all buyers.
     */
    public List<Buyer> getAll() {
        return List.copyOf(buyers);
    }
}
