package auction.service;

import auction.exceptions.InvalidDataException;
import auction.model.Seller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * In-memory CRUD service for {@link Seller} entities.
 */
public class SellerService {

    private final List<Seller> sellers = new ArrayList<>();

    /**
     * Adds a new seller. Rejects duplicates by ID.
     *
     * @throws InvalidDataException if a seller with the same ID already exists
     */
    public void addSeller(Seller seller) throws InvalidDataException {
        if (findById(seller.getSellerId()) != null) {
            throw new InvalidDataException(
                    "Seller with ID '%s' already exists.".formatted(seller.getSellerId()));
        }
        sellers.add(seller);
    }

    /**
     * Updates an existing seller's mutable fields (name, email).
     *
     * @throws InvalidDataException if the seller is not found
     */
    public void updateSeller(Seller updated) throws InvalidDataException {
        Seller existing = findById(updated.getSellerId());
        if (existing == null) {
            throw new InvalidDataException(
                    "Seller with ID '%s' not found.".formatted(updated.getSellerId()));
        }
        existing.setName(updated.getName());
        existing.setEmail(updated.getEmail());
    }

    /**
     * Deletes a seller by ID.
     *
     * @throws InvalidDataException if the seller is not found
     */
    public void deleteSeller(String sellerId) throws InvalidDataException {
        Seller existing = findById(sellerId);
        if (existing == null) {
            throw new InvalidDataException(
                    "Seller with ID '%s' not found.".formatted(sellerId));
        }
        sellers.remove(existing);
    }

    /**
     * Finds a seller by exact ID, or returns {@code null} if not found.
     */
    public Seller findById(String sellerId) {
        return sellers.stream()
                .filter(s -> s.getSellerId().equals(sellerId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Finds sellers whose name contains the given substring (case-insensitive).
     */
    public List<Seller> findByName(String name) {
        String lower = name.toLowerCase();
        return sellers.stream()
                .filter(s -> s.getName().toLowerCase().contains(lower))
                .collect(Collectors.toList());
    }

    /**
     * Returns an unmodifiable view of all sellers.
     */
    public List<Seller> getAll() {
        return List.copyOf(sellers);
    }
}
