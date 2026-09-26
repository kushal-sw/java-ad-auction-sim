package auction.service;

import auction.enums.AuctionStatus;
import auction.exceptions.InvalidBidException;
import auction.exceptions.InvalidDataException;
import auction.model.Auction;
import auction.model.Bid;
import auction.model.Product;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * In-memory service for placing and querying bids.
 *
 * <p>Bid history for each auction is kept in a {@link LinkedList} to
 * preserve insertion order and allow efficient appends.
 */
public class BidService {

    /** Per-auction bid history: auctionId → ordered list of bids. */
    private final Map<String, LinkedList<Bid>> bidHistory = new HashMap<>();

    private final AuctionService  auctionService;
    private final ProductService  productService;

    private final AtomicInteger idCounter = new AtomicInteger(1);

    public BidService(AuctionService auctionService, ProductService productService) {
        this.auctionService = auctionService;
        this.productService = productService;
    }

    // ── Core operations ──────────────────────────────────────

    /**
     * Places a bid on an auction.
     *
     * <p>Validation rules:
     * <ol>
     *   <li>The auction must exist and be {@link AuctionStatus#OPEN}.</li>
     *   <li>If no previous bid exists the amount must exceed the product's
     *       <em>base price</em> (not zero).</li>
     *   <li>If a previous bid exists the amount must exceed the current
     *       highest bid.</li>
     * </ol>
     *
     * @return the accepted {@link Bid}
     * @throws InvalidBidException if any validation rule is violated
     */
    public Bid placeBid(String auctionId, String buyerId, double amount)
            throws InvalidBidException, InvalidDataException {

        // 1 — Auction must exist
        Auction auction = auctionService.findById(auctionId);
        if (auction == null) {
            throw new InvalidBidException(
                    "Auction '%s' not found.".formatted(auctionId));
        }

        // 2 — Auction must be OPEN
        if (auction.getStatus() != AuctionStatus.OPEN) {
            throw new InvalidBidException(
                    "Auction '%s' is %s — bids are only accepted on OPEN auctions."
                            .formatted(auctionId, auction.getStatus()));
        }

        // 3 — Amount must beat the current floor
        Bid currentHighest = auction.getHighestBid();

        if (currentHighest == null) {
            // First bid: compare against the product's base price
            Product product = productService.findById(auction.getProductId());
            double basePrice = (product != null) ? product.getBasePrice() : 0.0;
            if (amount <= basePrice) {
                throw new InvalidBidException(
                        "Bid amount %.2f must exceed the base price %.2f."
                                .formatted(amount, basePrice));
            }
        } else {
            // Subsequent bids: must beat the current highest
            if (amount <= currentHighest.getAmount()) {
                throw new InvalidBidException(
                        "Bid amount %.2f must exceed the current highest bid of %.2f."
                                .formatted(amount, currentHighest.getAmount()));
            }
        }

        // 4 — Create and record the bid
        String bidId = "BID-%03d".formatted(idCounter.getAndIncrement());
        Bid bid = new Bid(bidId, auctionId, buyerId, amount, LocalDateTime.now());

        bidHistory
                .computeIfAbsent(auctionId, k -> new LinkedList<>())
                .add(bid);

        auction.setHighestBid(bid);

        return bid;
    }

    // ── Queries ──────────────────────────────────────────────

    /**
     * Returns the current highest bid for an auction, or {@code null}
     * if no bids have been placed.
     */
    public Bid getHighestBid(String auctionId) {
        Auction auction = auctionService.findById(auctionId);
        return (auction != null) ? auction.getHighestBid() : null;
    }

    /**
     * Returns the full bid history for an auction (earliest first),
     * or an empty list if none exist.
     */
    public List<Bid> getBidHistory(String auctionId) {
        LinkedList<Bid> history = bidHistory.get(auctionId);
        return (history != null)
                ? Collections.unmodifiableList(history)
                : List.of();
    }

    // ── Bid-increment slabs ──────────────────────────────────

    /**
     * Minimum bid increment for each price range.
     * <ul>
     *   <li>Current bid &lt; $100  → increment $10</li>
     *   <li>Current bid &lt; $500  → increment $50</li>
     *   <li>Current bid &lt; $2000 → increment $100</li>
     *   <li>Current bid ≥ $2000  → increment $500</li>
     * </ul>
     */
    private static final double[] SLAB_CEILINGS           = {100,  500,  2000};
    private static final double[] MIN_BID_INCREMENT_SLABS = { 10,   50,   100,  500};

    /**
     * Returns the minimum acceptable next bid amount for an auction,
     * based on the current highest bid (or the product's base price if
     * no bids exist) plus the slab-based increment.
     *
     * @return the minimum next bid, or {@code 0} if the auction is unknown
     */
    public double getMinNextBid(String auctionId) {
        Auction auction = auctionService.findById(auctionId);
        if (auction == null) return 0;

        double floor;
        Bid highest = auction.getHighestBid();
        if (highest != null) {
            floor = highest.getAmount();
        } else {
            Product product = productService.findById(auction.getProductId());
            floor = (product != null) ? product.getBasePrice() : 0;
        }

        // Walk the slabs to find the matching increment
        double increment = MIN_BID_INCREMENT_SLABS[MIN_BID_INCREMENT_SLABS.length - 1];
        for (int i = 0; i < SLAB_CEILINGS.length; i++) {
            if (floor < SLAB_CEILINGS[i]) {
                increment = MIN_BID_INCREMENT_SLABS[i];
                break;
            }
        }

        return floor + increment;
    }
}
