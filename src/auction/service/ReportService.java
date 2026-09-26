package auction.service;

import auction.enums.AuctionStatus;
import auction.enums.PaymentStatus;
import auction.model.Auction;
import auction.model.Bid;
import auction.model.Product;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Reporting service — read-only queries across auctions, bids, and products.
 */
public class ReportService {

    private final AuctionService  auctionService;
    private final ProductService  productService;
    private final BidService      bidService;

    public ReportService(AuctionService auctionService,
                         ProductService productService,
                         BidService bidService) {
        this.auctionService  = auctionService;
        this.productService  = productService;
        this.bidService      = bidService;
    }

    // ── Reports ──────────────────────────────────────────────

    /**
     * Returns the top-N highest winning bids across all <em>closed</em>
     * auctions, sorted descending by amount.
     */
    public List<Bid> getTopHighestBids(int n) {
        return auctionService.getAuctionsSortedByHighestBid().stream()
                .filter(a -> a.getStatus() == AuctionStatus.CLOSED)
                .filter(a -> a.getHighestBid() != null)
                .map(Auction::getHighestBid)
                .sorted(Comparator.comparingDouble(Bid::getAmount).reversed())
                .limit(n)
                .collect(Collectors.toList());
    }

    /**
     * Returns closed auctions whose payment is still
     * {@link PaymentStatus#PENDING}.
     */
    public List<Auction> getUnpaidWinners() {
        return auctionService.getAuctionsSortedByEndTime().stream()
                .filter(a -> a.getStatus() == AuctionStatus.CLOSED)
                .filter(a -> a.getPaymentStatus() == PaymentStatus.PENDING)
                .collect(Collectors.toList());
    }

    /**
     * Computes a sales summary for a seller: total revenue and number of
     * auctions won on that seller's products.
     *
     * <p>Traces the path <strong>Auction → Product → Seller</strong>
     * using {@link ProductService#findById} because {@link Auction} only
     * stores {@code productId}, not {@code sellerId}.
     */
    public SalesSummary getSellerSalesSummary(String sellerId) {
        double totalAmount = 0;
        int    auctionCount = 0;

        for (Auction auction : auctionService.getAuctionsSortedByEndTime()) {
            // Only count closed auctions that have a winner
            if (auction.getStatus() != AuctionStatus.CLOSED
                    || auction.getWinnerId() == null) {
                continue;
            }

            // Trace: Auction.productId → Product → Product.sellerId
            Product product = productService.findById(auction.getProductId());
            if (product == null) {
                continue; // orphaned auction — skip
            }
            if (!product.getSellerId().equals(sellerId)) {
                continue; // belongs to a different seller
            }

            totalAmount += auction.getHighestBid().getAmount();
            auctionCount++;
        }

        return new SalesSummary(sellerId, auctionCount, totalAmount);
    }

    /**
     * Returns <em>OPEN</em> auctions whose {@code endTime} falls within
     * the next {@code hours} hours from <strong>now</strong>.
     *
     * <p>Uses {@link AuctionService#getAuctionsEndingBetween} which
     * leverages the TreeMap's {@code subMap} for an efficient range scan,
     * then filters to OPEN status only.
     */
    public List<Auction> getAuctionsClosingSoon(int hours) {
        LocalDateTime now     = LocalDateTime.now();
        LocalDateTime horizon = now.plusHours(hours);

        return auctionService.getAuctionsEndingBetween(now, horizon).stream()
                .filter(a -> a.getStatus() == AuctionStatus.OPEN)
                .collect(Collectors.toList());
    }

    // ── Summary record ───────────────────────────────────────

    /**
     * Simple value object returned by {@link #getSellerSalesSummary}.
     */
    public static class SalesSummary {

        private final String sellerId;
        private final int    auctionCount;
        private final double totalAmount;

        public SalesSummary(String sellerId, int auctionCount, double totalAmount) {
            this.sellerId     = sellerId;
            this.auctionCount = auctionCount;
            this.totalAmount  = totalAmount;
        }

        public String getSellerId()    { return sellerId; }
        public int    getAuctionCount() { return auctionCount; }
        public double getTotalAmount()  { return totalAmount; }

        @Override
        public String toString() {
            return "SalesSummary{sellerId='%s', auctionCount=%d, totalAmount=%.2f}"
                    .formatted(sellerId, auctionCount, totalAmount);
        }
    }
}
