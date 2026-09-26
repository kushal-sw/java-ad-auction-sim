package auction.gui;

import auction.service.AuctionService;
import auction.service.BidService;
import auction.service.BuyerService;
import auction.service.PaymentService;
import auction.service.ProductService;
import auction.service.ReportService;
import auction.service.SellerService;

import javax.swing.*;

/**
 * Main application window — a {@link JTabbedPane} with six tabs.
 *
 * <p>Each tab's panel is refreshed whenever its tab is selected, so
 * cross-panel data changes (e.g. adding a seller) are reflected in
 * dependent dropdowns (e.g. the Products tab's seller combo).
 */
public class MainFrame extends JFrame {

    public MainFrame(SellerService sellerService,
                     BuyerService buyerService,
                     ProductService productService,
                     AuctionService auctionService,
                     BidService bidService,
                     PaymentService paymentService,
                     ReportService reportService) {

        super("Online Auction System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 650);
        setLocationRelativeTo(null);

        // ── Panels ───────────────────────────────────────────
        SellerPanel  sellerPanel  = new SellerPanel(sellerService);
        BuyerPanel   buyerPanel   = new BuyerPanel(buyerService);
        ProductPanel productPanel = new ProductPanel(productService, sellerService);
        AuctionPanel auctionPanel = new AuctionPanel(auctionService, productService);
        BidPanel     bidPanel     = new BidPanel(auctionService, buyerService, bidService);
        ReportPanel  reportPanel  = new ReportPanel(reportService, sellerService);

        // ── Tabbed pane ──────────────────────────────────────
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Sellers",  sellerPanel);
        tabs.addTab("Buyers",   buyerPanel);
        tabs.addTab("Products", productPanel);
        tabs.addTab("Auctions", auctionPanel);
        tabs.addTab("Bids",     bidPanel);
        tabs.addTab("Reports",  reportPanel);

        // Refresh the selected tab whenever it's activated
        tabs.addChangeListener(e -> {
            switch (tabs.getSelectedIndex()) {
                case 0 -> sellerPanel.refreshTable();
                case 1 -> buyerPanel.refreshTable();
                case 2 -> productPanel.refreshTable();
                case 3 -> auctionPanel.refreshTable();
                case 4 -> bidPanel.refreshTable();
                case 5 -> reportPanel.refreshTable();
            }
        });

        add(tabs);
    }
}
