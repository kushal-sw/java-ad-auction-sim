package auction.gui;

import auction.model.Auction;
import auction.model.Bid;
import auction.model.Seller;
import auction.service.AuctionService;
import auction.service.BidService;
import auction.service.ProductService;
import auction.service.ReportService;
import auction.service.SellerService;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Panel for running the four report types and displaying results.
 */
public class ReportPanel extends JPanel {

    private final ReportService reportService;
    private final SellerService sellerService;

    private final JComboBox<String> reportTypeCombo;
    private final JSpinner paramSpinner;
    private final JComboBox<ComboItem> sellerCombo;
    private final JLabel paramLabel;
    private final JLabel sellerLabel;
    private final JTextArea resultArea;

    public ReportPanel(ReportService reportService, SellerService sellerService) {
        this.reportService = reportService;
        this.sellerService = sellerService;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ── Controls ─────────────────────────────────────────
        JPanel controlPanel = new JPanel(new GridBagLayout());
        controlPanel.setBorder(BorderFactory.createTitledBorder("Report Controls"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        controlPanel.add(new JLabel("Report:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        reportTypeCombo = new JComboBox<>(new String[]{
                "Top Highest Bids",
                "Unpaid Winners",
                "Seller Sales Summary",
                "Auctions Closing Soon"
        });
        reportTypeCombo.addActionListener(e -> onReportTypeChanged());
        controlPanel.add(reportTypeCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        paramLabel = new JLabel("Top N:");
        controlPanel.add(paramLabel, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        paramSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 100, 1));
        controlPanel.add(paramSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        sellerLabel = new JLabel("Seller:");
        controlPanel.add(sellerLabel, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        sellerCombo = new JComboBox<>();
        controlPanel.add(sellerCombo, gbc);

        JButton runBtn = new JButton("Run Report");
        runBtn.addActionListener(e -> runReport());
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        controlPanel.add(runBtn, gbc);

        add(controlPanel, BorderLayout.NORTH);

        // ── Results ──────────────────────────────────────────
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Results"));
        add(scrollPane, BorderLayout.CENTER);

        onReportTypeChanged(); // set initial visibility
    }

    // ── UI logic ─────────────────────────────────────────────

    private void onReportTypeChanged() {
        String type = (String) reportTypeCombo.getSelectedItem();
        boolean showParam  = "Top Highest Bids".equals(type)
                          || "Auctions Closing Soon".equals(type);
        boolean showSeller = "Seller Sales Summary".equals(type);

        paramLabel.setVisible(showParam);
        paramSpinner.setVisible(showParam);
        sellerLabel.setVisible(showSeller);
        sellerCombo.setVisible(showSeller);

        if ("Top Highest Bids".equals(type)) {
            paramLabel.setText("Top N:");
        } else if ("Auctions Closing Soon".equals(type)) {
            paramLabel.setText("Within hours:");
        }
    }

    private void runReport() {
        String type = (String) reportTypeCombo.getSelectedItem();
        StringBuilder sb = new StringBuilder();

        switch (type) {
            case "Top Highest Bids" -> {
                int n = (int) paramSpinner.getValue();
                sb.append("═══  Top %d Highest Bids (closed auctions)  ═══\n\n".formatted(n));
                List<Bid> bids = reportService.getTopHighestBids(n);
                if (bids.isEmpty()) {
                    sb.append("  (no closed auctions with bids)\n");
                } else {
                    sb.append("  %-10s  %-10s  %-8s  %s\n"
                            .formatted("Bid ID", "Auction", "Buyer", "Amount"));
                    sb.append("  ─".repeat(22)).append("\n");
                    for (Bid b : bids) {
                        sb.append("  %-10s  %-10s  %-8s  $%.2f\n".formatted(
                                b.getBidId(), b.getAuctionId(),
                                b.getBuyerId(), b.getAmount()));
                    }
                }
            }
            case "Unpaid Winners" -> {
                sb.append("═══  Unpaid Winners (CLOSED + PENDING)  ═══\n\n");
                List<Auction> auctions = reportService.getUnpaidWinners();
                if (auctions.isEmpty()) {
                    sb.append("  (all winners have paid)\n");
                } else {
                    sb.append("  %-10s  %-8s  %-10s  %s\n"
                            .formatted("Auction", "Winner", "Amount", "Payment"));
                    sb.append("  ─".repeat(22)).append("\n");
                    for (Auction a : auctions) {
                        sb.append("  %-10s  %-8s  $%-9.2f  %s\n".formatted(
                                a.getAuctionId(), a.getWinnerId(),
                                a.getHighestBid().getAmount(), a.getPaymentStatus()));
                    }
                }
            }
            case "Seller Sales Summary" -> {
                ComboItem seller = (ComboItem) sellerCombo.getSelectedItem();
                if (seller == null) {
                    sb.append("  (select a seller first)\n");
                } else {
                    ReportService.SalesSummary summary =
                            reportService.getSellerSalesSummary(seller.id);
                    sb.append("═══  Sales Summary for %s  ═══\n\n".formatted(seller));
                    sb.append("  Auctions won:  %d\n".formatted(summary.getAuctionCount()));
                    sb.append("  Total revenue: $%.2f\n".formatted(summary.getTotalAmount()));
                }
            }
            case "Auctions Closing Soon" -> {
                int hours = (int) paramSpinner.getValue();
                sb.append("═══  OPEN Auctions Closing Within %d Hour(s)  ═══\n\n"
                        .formatted(hours));
                List<Auction> auctions = reportService.getAuctionsClosingSoon(hours);
                if (auctions.isEmpty()) {
                    sb.append("  (none)\n");
                } else {
                    sb.append("  %-10s  %-8s  %s\n"
                            .formatted("Auction", "Product", "End Time"));
                    sb.append("  ─".repeat(22)).append("\n");
                    for (Auction a : auctions) {
                        sb.append("  %-10s  %-8s  %s\n".formatted(
                                a.getAuctionId(), a.getProductId(),
                                a.getEndTime()));
                    }
                }
            }
        }

        resultArea.setText(sb.toString());
        resultArea.setCaretPosition(0);
    }

    public void refreshTable() {
        // Refresh seller dropdown
        ComboItem prev = (ComboItem) sellerCombo.getSelectedItem();
        sellerCombo.removeAllItems();
        for (Seller s : sellerService.getAll()) {
            sellerCombo.addItem(new ComboItem(s.getSellerId(),
                    s.getSellerId() + " — " + s.getName()));
        }
        if (prev != null) {
            for (int i = 0; i < sellerCombo.getItemCount(); i++) {
                if (sellerCombo.getItemAt(i).id.equals(prev.id)) {
                    sellerCombo.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    // ── Helpers ──────────────────────────────────────────────

    static class ComboItem {
        final String id;
        private final String display;
        ComboItem(String id, String display) { this.id = id; this.display = display; }
        @Override public String toString() { return display; }
    }
}
