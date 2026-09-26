package auction;

import auction.enums.PaymentStatus;
import auction.exceptions.InvalidBidException;
import auction.exceptions.InvalidDataException;
import auction.gui.MainFrame;
import auction.model.Auction;
import auction.model.Bid;
import auction.model.Buyer;
import auction.model.Product;
import auction.model.Seller;
import auction.service.AuctionService;
import auction.service.BidService;
import auction.service.BuyerService;
import auction.service.PaymentService;
import auction.service.ProductService;
import auction.service.ReportService;
import auction.service.SellerService;

import javax.swing.*;
import java.time.LocalDateTime;

/**
 * Application entry point — launches the Swing GUI.
 *
 * <p>The previous console-based tests are preserved in
 * {@link #runConsoleTests} and can be invoked by passing
 * {@code --console} as a command-line argument.
 */
public class Main {

    public static void main(String[] args) {

        // ── Shared service instances ─────────────────────────
        SellerService  sellerService  = new SellerService();
        BuyerService   buyerService   = new BuyerService();
        ProductService productService = new ProductService();
        AuctionService auctionService = new AuctionService();
        BidService     bidService     = new BidService(auctionService, productService);
        PaymentService paymentService = new PaymentService(auctionService);
        ReportService  reportService  = new ReportService(auctionService, productService, bidService);

        // ── Check for --console flag ─────────────────────────
        if (args.length > 0 && "--console".equals(args[0])) {
            runConsoleTests(sellerService, buyerService, productService,
                    auctionService, bidService, paymentService, reportService);
            return;
        }

        // ── Launch Swing GUI ─────────────────────────────────
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(
                        UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) { }

            MainFrame frame = new MainFrame(
                    sellerService, buyerService, productService,
                    auctionService, bidService, paymentService, reportService);
            frame.setVisible(true);
        });
    }

    // ═════════════════════════════════════════════════════════
    // Previous console tests — run with: java -cp out auction.Main --console
    // ═════════════════════════════════════════════════════════

    static void runConsoleTests(SellerService sellerService,
                                BuyerService buyerService,
                                ProductService productService,
                                AuctionService auctionService,
                                BidService bidService,
                                PaymentService paymentService,
                                ReportService reportService) {
        try {
            // ── Setup ────────────────────────────────────────
            Seller s1 = new Seller("S001", "Alice Johnson", "alice@example.com");
            Seller s2 = new Seller("S002", "Bob Smith",     "bob@example.com");
            sellerService.addSeller(s1);
            sellerService.addSeller(s2);

            Buyer b1 = new Buyer("B001", "Charlie Brown", "charlie@example.com");
            Buyer b2 = new Buyer("B002", "Diana Prince",  "diana@example.com");
            buyerService.addBuyer(b1);
            buyerService.addBuyer(b2);

            Product p1 = new Product("P001", "Vintage Watch",
                    "1960s Omega Seamaster", 500.00, "S001");
            Product p2 = new Product("P002", "Oil Painting",
                    "Landscape by local artist", 150.00, "S001");
            Product p3 = new Product("P003", "Antique Vase",
                    "Ming dynasty replica", 300.00, "S002");
            Product p4 = new Product("P004", "Silver Ring",
                    "Handcrafted sterling silver", 80.00, "S001");
            productService.addProduct(p1);
            productService.addProduct(p2);
            productService.addProduct(p3);
            productService.addProduct(p4);

            LocalDateTime now = LocalDateTime.now();

            // AUC-001: closed, winner B001 @ 750, PAID
            Auction auc1 = auctionService.createAuction(p1, now, now.plusHours(2));
            auctionService.openAuction(auc1.getAuctionId());
            bidService.placeBid(auc1.getAuctionId(), "B001", 550.00);
            bidService.placeBid(auc1.getAuctionId(), "B002", 600.00);
            bidService.placeBid(auc1.getAuctionId(), "B001", 750.00);
            auctionService.closeAuction(auc1.getAuctionId());
            paymentService.markAsPaid(auc1.getAuctionId());

            // AUC-002: closed, winner B002 @ 200, PENDING
            Auction auc2 = auctionService.createAuction(p2, now, now.plusHours(3));
            auctionService.openAuction(auc2.getAuctionId());
            bidService.placeBid(auc2.getAuctionId(), "B002", 200.00);
            auctionService.closeAuction(auc2.getAuctionId());

            // AUC-003: closed, winner B001 @ 500, PENDING
            Auction auc3 = auctionService.createAuction(p3, now, now.plusHours(4));
            auctionService.openAuction(auc3.getAuctionId());
            bidService.placeBid(auc3.getAuctionId(), "B001", 350.00);
            bidService.placeBid(auc3.getAuctionId(), "B001", 500.00);
            auctionService.closeAuction(auc3.getAuctionId());

            // AUC-004: OPEN, closing soon
            Auction auc4 = auctionService.createAuction(p4, now, now.plusHours(1));
            auctionService.openAuction(auc4.getAuctionId());
            bidService.placeBid(auc4.getAuctionId(), "B002", 100.00);

            // ── Reports ──────────────────────────────────────
            System.out.println("╔══════════════════════════════════════════════╗");
            System.out.println("║           REPORT SERVICE OUTPUT             ║");
            System.out.println("╚══════════════════════════════════════════════╝");

            System.out.println("\n── Top 3 Highest Bids (closed auctions) ──");
            reportService.getTopHighestBids(3).forEach(bid ->
                    System.out.printf("  %-8s  auction=%-8s  buyer=%-5s  amount=%.2f%n",
                            bid.getBidId(), bid.getAuctionId(),
                            bid.getBuyerId(), bid.getAmount()));

            System.out.println("\n── Unpaid Winners (CLOSED + PENDING) ──");
            reportService.getUnpaidWinners().forEach(a ->
                    System.out.printf("  %-8s  winner=%-5s  amount=%.2f  payment=%s%n",
                            a.getAuctionId(), a.getWinnerId(),
                            a.getHighestBid().getAmount(), a.getPaymentStatus()));

            System.out.println("\n── Seller Sales Summary ──");
            System.out.println("  Alice (S001): "
                    + reportService.getSellerSalesSummary("S001"));
            System.out.println("  Bob   (S002): "
                    + reportService.getSellerSalesSummary("S002"));

            System.out.println("\n── Auctions Closing Soon (within 2 hours) ──");
            reportService.getAuctionsClosingSoon(2).forEach(a ->
                    System.out.printf("  %-8s  product=%-5s  status=%-6s  ends=%s%n",
                            a.getAuctionId(), a.getProductId(),
                            a.getStatus(), a.getEndTime()));

            System.out.println("\n✔ All console tests passed.");

        } catch (InvalidDataException | InvalidBidException e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
