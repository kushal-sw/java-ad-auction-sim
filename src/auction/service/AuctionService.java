package auction.service;

import auction.enums.AuctionStatus;
import auction.enums.PaymentStatus;
import auction.enums.ProductStatus;
import auction.exceptions.InvalidDataException;
import auction.model.Auction;
import auction.model.Bid;
import auction.model.Product;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * In-memory service for managing auctions.
 *
 * <p>Internally uses:
 * <ul>
 *   <li>{@code HashMap<String, Auction>} — O(1) lookup by auction ID</li>
 *   <li>{@code TreeMap<LocalDateTime, List<Auction>>} — auctions ordered by
 *       end time, with a {@code List} at each key to handle collisions when
 *       two auctions share the exact same end time</li>
 * </ul>
 */
public class AuctionService {

    private final Map<String, Auction> auctionsById = new HashMap<>();

    /**
     * Values are lists so that two auctions with the <em>same</em> endTime
     * do not silently overwrite each other (a plain
     * {@code TreeMap<LocalDateTime, Auction>} would).
     */
    private final TreeMap<LocalDateTime, List<Auction>> auctionsByEndTime = new TreeMap<>();

    private final AtomicInteger idCounter = new AtomicInteger(1);

    // ── CRUD ─────────────────────────────────────────────────

    /**
     * Creates a new auction for the given product.
     * <p>The auction starts in {@link AuctionStatus#SCHEDULED} state.
     * Call {@link #openAuction} to move it to {@code OPEN} before bids
     * can be placed. The product's status is set to
     * {@link ProductStatus#IN_AUCTION}.
     *
     * @return the newly created {@link Auction}
     * @throws InvalidDataException if the product is null, or dates are invalid
     */
    public Auction createAuction(Product product, LocalDateTime start,
                                 LocalDateTime end) throws InvalidDataException {
        if (product == null) {
            throw new InvalidDataException("Product cannot be null.");
        }

        String auctionId = "AUC-%03d".formatted(idCounter.getAndIncrement());
        Auction auction = new Auction(auctionId, product.getProductId(), start, end);
        // Auction constructor already validates start < end and sets SCHEDULED

        product.setStatus(ProductStatus.IN_AUCTION);

        auctionsById.put(auctionId, auction);
        auctionsByEndTime
                .computeIfAbsent(end, k -> new ArrayList<>())
                .add(auction);

        return auction;
    }

    /**
     * Transitions an auction from {@code SCHEDULED} → {@code OPEN}.
     *
     * @throws InvalidDataException if not found or not in SCHEDULED state
     */
    public void openAuction(String auctionId) throws InvalidDataException {
        Auction auction = getOrThrow(auctionId);
        if (auction.getStatus() != AuctionStatus.SCHEDULED) {
            throw new InvalidDataException(
                    "Auction '%s' is %s — can only open a SCHEDULED auction."
                            .formatted(auctionId, auction.getStatus()));
        }
        auction.setStatus(AuctionStatus.OPEN);
    }

    /**
     * Closes an auction and determines the winner.
     *
     * <p>If the auction has a highest bid, the bid's buyer becomes the
     * winner and {@link PaymentStatus#PENDING} is recorded on the auction.
     * If there are no bids the auction closes cleanly with no winner.
     *
     * <p>The winner is read from {@code auction.getHighestBid()} which is
     * kept up-to-date by {@link BidService#placeBid}.
     *
     * @throws InvalidDataException if not found or already closed
     */
    public void closeAuction(String auctionId) throws InvalidDataException {
        Auction auction = getOrThrow(auctionId);
        if (auction.getStatus() == AuctionStatus.CLOSED) {
            throw new InvalidDataException(
                    "Auction '%s' is already CLOSED.".formatted(auctionId));
        }

        auction.setStatus(AuctionStatus.CLOSED);

        // Determine winner from the highest bid (maintained by BidService)
        Bid highest = auction.getHighestBid();
        if (highest != null) {
            auction.setWinnerId(highest.getBuyerId());
            auction.setPaymentStatus(PaymentStatus.PENDING);
        }
        // No bids → no winner, paymentStatus stays null — this is fine
    }

    /**
     * Looks up an auction by ID, or returns {@code null}.
     */
    public Auction findById(String auctionId) {
        return auctionsById.get(auctionId);
    }

    // ── Sorted views ─────────────────────────────────────────

    /**
     * Returns all auctions ordered by end time (earliest first), using the
     * TreeMap's natural ordering. Auctions that share an end time appear
     * together in insertion order.
     */
    public List<Auction> getAuctionsSortedByEndTime() {
        return auctionsByEndTime.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    /**
     * Returns all auctions sorted by highest bid amount (descending).
     * Auctions with no bids appear at the end.
     */
    public List<Auction> getAuctionsSortedByHighestBid() {
        return auctionsById.values().stream()
                .sorted(Comparator.comparingDouble(
                        (Auction a) -> a.getHighestBid() != null
                                ? a.getHighestBid().getAmount()
                                : -1.0)
                        .reversed())
                .collect(Collectors.toList());
    }

    /**
     * Returns auctions whose end time falls in the range
     * [{@code from}, {@code to}] (both inclusive), leveraging the
     * TreeMap's {@code subMap} for an efficient range scan.
     */
    public List<Auction> getAuctionsEndingBetween(LocalDateTime from, LocalDateTime to) {
        return auctionsByEndTime
                .subMap(from, true, to, true)
                .values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    // ── Helpers ──────────────────────────────────────────────

    private Auction getOrThrow(String auctionId) throws InvalidDataException {
        Auction auction = auctionsById.get(auctionId);
        if (auction == null) {
            throw new InvalidDataException(
                    "Auction with ID '%s' not found.".formatted(auctionId));
        }
        return auction;
    }
}
