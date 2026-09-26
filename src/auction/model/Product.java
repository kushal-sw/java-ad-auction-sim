package auction.model;

import auction.enums.ProductStatus;
import auction.exceptions.InvalidDataException;

/**
 * Represents a product listed by a seller that can be put up for auction.
 */
public class Product {

    private final String productId;
    private String name;
    private String description;
    private double basePrice;
    private final String sellerId;
    private ProductStatus status;

    /**
     * Creates a new Product.
     *
     * @throws InvalidDataException if any required field is invalid
     */
    public Product(String productId, String name, String description,
                   double basePrice, String sellerId) throws InvalidDataException {
        if (productId == null || productId.isBlank()) {
            throw new InvalidDataException("Product ID cannot be null or blank.");
        }
        if (name == null || name.isBlank()) {
            throw new InvalidDataException("Product name cannot be null or blank.");
        }
        if (basePrice < 0) {
            throw new InvalidDataException("Base price cannot be negative.");
        }
        if (sellerId == null || sellerId.isBlank()) {
            throw new InvalidDataException("Seller ID cannot be null or blank.");
        }
        this.productId = productId;
        this.name = name;
        this.description = (description != null) ? description : "";
        this.basePrice = basePrice;
        this.sellerId = sellerId;
        this.status = ProductStatus.LISTED;
    }

    // ── Getters ──────────────────────────────────────────────

    public String getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public String getSellerId() {
        return sellerId;
    }

    public ProductStatus getStatus() {
        return status;
    }

    // ── Setters (mutable fields only) ────────────────────────

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setBasePrice(double basePrice) {
        this.basePrice = basePrice;
    }

    public void setStatus(ProductStatus status) {
        this.status = status;
    }

    // ── Object overrides ─────────────────────────────────────

    @Override
    public String toString() {
        return "Product{productId='%s', name='%s', basePrice=%.2f, sellerId='%s', status=%s}"
                .formatted(productId, name, basePrice, sellerId, status);
    }
}
