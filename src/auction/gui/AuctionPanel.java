package auction.gui;

import auction.exceptions.InvalidDataException;
import auction.model.Auction;
import auction.model.Product;
import auction.service.AuctionService;
import auction.service.ProductService;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * Panel for creating, opening, and closing auctions with sortable table.
 */
public class AuctionPanel extends JPanel {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final AuctionService auctionService;
    private final ProductService productService;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JComboBox<ComboItem> productCombo;
    private final JTextField startField;
    private final JTextField endField;
    private final JComboBox<String> sortCombo;

    public AuctionPanel(AuctionService auctionService, ProductService productService) {
        this.auctionService = auctionService;
        this.productService = productService;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ── Sort bar ─────────────────────────────────────────
        JPanel sortPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sortPanel.add(new JLabel("Sort by:"));
        sortCombo = new JComboBox<>(new String[]{"End Time", "Highest Bid"});
        sortCombo.addActionListener(e -> refreshTable());
        sortPanel.add(sortCombo);
        add(sortPanel, BorderLayout.NORTH);

        // ── Table ────────────────────────────────────────────
        tableModel = new DefaultTableModel(
                new String[]{"ID", "Product", "Status", "Start", "End",
                             "Highest Bid", "Winner", "Payment"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // ── Form ─────────────────────────────────────────────
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Create / Manage Auction"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        int row = 0;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formPanel.add(new JLabel("Product:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        productCombo = new JComboBox<>();
        formPanel.add(productCombo, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formPanel.add(new JLabel("Start (yyyy-MM-dd HH:mm):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        startField = new JTextField(LocalDateTime.now().format(FMT), 15);
        formPanel.add(startField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formPanel.add(new JLabel("End (yyyy-MM-dd HH:mm):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        endField = new JTextField(LocalDateTime.now().plusHours(2).format(FMT), 15);
        formPanel.add(endField, gbc);

        // ── Buttons ──────────────────────────────────────────
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        JButton createBtn = new JButton("Create Auction");
        JButton openBtn   = new JButton("Open");
        JButton closeBtn  = new JButton("Close");
        buttonPanel.add(createBtn);
        buttonPanel.add(openBtn);
        buttonPanel.add(closeBtn);

        createBtn.addActionListener(e -> doCreate());
        openBtn.addActionListener(e -> doOpen());
        closeBtn.addActionListener(e -> doClose());

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(formPanel, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);

        refreshTable();
    }

    // ── Actions ──────────────────────────────────────────────

    private void doCreate() {
        try {
            ComboItem pi = (ComboItem) productCombo.getSelectedItem();
            if (pi == null) { showWarning("Select a product."); return; }

            LocalDateTime start = LocalDateTime.parse(startField.getText().trim(), FMT);
            LocalDateTime end   = LocalDateTime.parse(endField.getText().trim(), FMT);

            Product product = productService.findById(pi.id);
            if (product == null) { showWarning("Product not found."); return; }

            Auction auction = auctionService.createAuction(product, start, end);
            refreshTable();
            JOptionPane.showMessageDialog(this,
                    "Auction created: " + auction.getAuctionId(),
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (DateTimeParseException ex) {
            showWarning("Invalid date format. Use yyyy-MM-dd HH:mm");
        } catch (InvalidDataException ex) {
            showError(ex);
        }
    }

    private void doOpen() {
        String auctionId = getSelectedAuctionId();
        if (auctionId == null) return;
        try {
            auctionService.openAuction(auctionId);
            refreshTable();
        } catch (InvalidDataException ex) {
            showError(ex);
        }
    }

    private void doClose() {
        String auctionId = getSelectedAuctionId();
        if (auctionId == null) return;
        try {
            auctionService.closeAuction(auctionId);
            refreshTable();
        } catch (InvalidDataException ex) {
            showError(ex);
        }
    }

    // ── Helpers ──────────────────────────────────────────────

    private String getSelectedAuctionId() {
        int r = table.getSelectedRow();
        if (r < 0) {
            showWarning("Select an auction in the table.");
            return null;
        }
        return (String) tableModel.getValueAt(r, 0);
    }

    public void refreshTable() {
        // Refresh product dropdown
        ComboItem prev = (ComboItem) productCombo.getSelectedItem();
        productCombo.removeAllItems();
        for (Product p : productService.getAll()) {
            productCombo.addItem(new ComboItem(p.getProductId(),
                    p.getProductId() + " — " + p.getName()
                            + " ($" + "%.2f".formatted(p.getBasePrice()) + ")"));
        }
        if (prev != null) {
            for (int i = 0; i < productCombo.getItemCount(); i++) {
                if (productCombo.getItemAt(i).id.equals(prev.id)) {
                    productCombo.setSelectedIndex(i);
                    break;
                }
            }
        }

        // Refresh table with selected sort
        List<Auction> auctions = "Highest Bid".equals(sortCombo.getSelectedItem())
                ? auctionService.getAuctionsSortedByHighestBid()
                : auctionService.getAuctionsSortedByEndTime();

        tableModel.setRowCount(0);
        for (Auction a : auctions) {
            String highBid = a.getHighestBid() != null
                    ? "%.2f".formatted(a.getHighestBid().getAmount()) : "—";
            String winner  = a.getWinnerId() != null ? a.getWinnerId() : "—";
            String payment = a.getPaymentStatus() != null
                    ? a.getPaymentStatus().name() : "—";

            tableModel.addRow(new Object[]{
                    a.getAuctionId(),
                    a.getProductId(),
                    a.getStatus(),
                    a.getStartTime().format(FMT),
                    a.getEndTime().format(FMT),
                    highBid, winner, payment
            });
        }
    }

    static class ComboItem {
        final String id;
        private final String display;
        ComboItem(String id, String display) { this.id = id; this.display = display; }
        @Override public String toString() { return display; }
    }

    private void showError(Exception ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    private void showWarning(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Warning",
                JOptionPane.WARNING_MESSAGE);
    }
}
