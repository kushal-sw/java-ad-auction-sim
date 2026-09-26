package auction.service;

import auction.exceptions.InvalidDataException;
import auction.model.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * In-memory CRUD service for {@link Product} entities.
 */
public class ProductService {

    private final List<Product> products = new ArrayList<>();

    /**
     * Adds a new product. Rejects duplicates by ID.
     *
     * @throws InvalidDataException if a product with the same ID already exists
     */
    public void addProduct(Product product) throws InvalidDataException {
        if (findById(product.getProductId()) != null) {
            throw new InvalidDataException(
                    "Product with ID '%s' already exists.".formatted(product.getProductId()));
        }
        products.add(product);
    }

    /**
     * Updates an existing product's mutable fields (name, description, basePrice, status).
     *
     * @throws InvalidDataException if the product is not found
     */
    public void updateProduct(Product updated) throws InvalidDataException {
        Product existing = findById(updated.getProductId());
        if (existing == null) {
            throw new InvalidDataException(
                    "Product with ID '%s' not found.".formatted(updated.getProductId()));
        }
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setBasePrice(updated.getBasePrice());
        existing.setStatus(updated.getStatus());
    }

    /**
     * Deletes a product by ID.
     *
     * @throws InvalidDataException if the product is not found
     */
    public void deleteProduct(String productId) throws InvalidDataException {
        Product existing = findById(productId);
        if (existing == null) {
            throw new InvalidDataException(
                    "Product with ID '%s' not found.".formatted(productId));
        }
        products.remove(existing);
    }

    /**
     * Finds a product by exact ID, or returns {@code null} if not found.
     */
    public Product findById(String productId) {
        return products.stream()
                .filter(p -> p.getProductId().equals(productId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Finds products whose name contains the given substring (case-insensitive).
     */
    public List<Product> findByName(String name) {
        String lower = name.toLowerCase();
        return products.stream()
                .filter(p -> p.getName().toLowerCase().contains(lower))
                .collect(Collectors.toList());
    }

    /**
     * Finds all products listed by a specific seller.
     */
    public List<Product> findBySeller(String sellerId) {
        return products.stream()
                .filter(p -> p.getSellerId().equals(sellerId))
                .collect(Collectors.toList());
    }

    /**
     * Returns an unmodifiable view of all products.
     */
    public List<Product> getAll() {
        return List.copyOf(products);
    }
}
