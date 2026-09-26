package auction.gui;

import auction.exceptions.InvalidBidException;
import auction.exceptions.InvalidDataException;
import auction.model.Auction;
import auction.model.Bid;
import auction.model.Buyer;
import auction.service.AuctionService;
import auction.service.BidService;
import auction.service.BuyerService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Panel for placing bids and viewing bid history per auction.
 */
public class BidPanel extends JPanel {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AuctionService auctionService;
    private final BuyerService   buyerService;
    private final BidService     bidService;

    private final JComboBox<ComboItem> auctionCombo;
    private final JComboBox<ComboItem> buyerCombo;
    private final JTextField amountField;
    private final DefaultTableModel historyModel;
    private final JTable historyTable;
    private final JLabel highestBidLabel;
    private final JLabel hintLabel;

    public BidPanel(AuctionService auctionService, BuyerService buyerService,
                    BidService bidService) {
        this.auctionService = auctionService;
        this.buyerService   = buyerService;
        this.bidService     = bidService;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ── Top: bid form ────────────────────────────────────
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Place a Bid"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        int row = 0;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formPanel.add(new JLabel("Auction:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        auctionCombo = new JComboBox<>();
        auctionCombo.addActionListener(e -> onAuctionSelected());
        formPanel.add(auctionCombo, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formPanel.add(new JLabel("Buyer:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        buyerCombo = new JComboBox<>();
        formPanel.add(buyerCombo, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formPanel.add(new JLabel("Amount:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        amountField = new JTextField(15);
        formPanel.add(amountField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formPanel.add(new JLabel("Current Highest:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        highestBidLabel = new JLabel("—");
        highestBidLabel.setFont(highestBidLabel.getFont().deriveFont(Font.BOLD));
        formPanel.add(highestBidLabel, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formPanel.add(new JLabel("Min Next Bid:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        hintLabel = new JLabel("—");
        hintLabel.setForeground(new Color(0, 120, 60));
        formPanel.add(hintLabel, gbc);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(formPanel, BorderLayout.CENTER);

        JButton bidBtn = new JButton("Place Bid");
        bidBtn.addActionListener(e -> doPlaceBid());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPanel.add(bidBtn);
        topPanel.add(btnPanel, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        // ── Bottom: bid history table ────────────────────────
        historyModel = new DefaultTableModel(
                new String[]{"Bid ID", "Buyer", "Amount", "Timestamp"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        historyTable = new JTable(historyModel);
        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Bid History"));
        add(scrollPane, BorderLayout.CENTER);

        refreshTable();
    }

    // ── Actions ──────────────────────────────────────────────

    private void doPlaceBid() {
        ComboItem auc = (ComboItem) auctionCombo.getSelectedItem();
        ComboItem buy = (ComboItem) buyerCombo.getSelectedItem();
        if (auc == null) { showWarning("Select an auction."); return; }
        if (buy == null) { showWarning("Select a buyer."); return; }

        try {
            double amount = Double.parseDouble(amountField.getText().trim());
            bidService.placeBid(auc.id, buy.id, amount);
            amountField.setText("");
            refreshBidHistory(auc.id);
            JOptionPane.showMessageDialog(this, "Bid placed successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) {
            showWarning("Amount must be a valid number.");
        } catch (InvalidBidException ex) {
            // Requirement: show error dialog for InvalidBidException
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Invalid Bid", JOptionPane.ERROR_MESSAGE);
        } catch (InvalidDataException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onAuctionSelected() {
        ComboItem auc = (ComboItem) auctionCombo.getSelectedItem();
        if (auc != null) {
            refreshBidHistory(auc.id);
        }
    }

    // ── Refresh ──────────────────────────────────────────────

    private void refreshBidHistory(String auctionId) {
        historyModel.setRowCount(0);
        List<Bid> bids = bidService.getBidHistory(auctionId);
        for (Bid b : bids) {
            historyModel.addRow(new Object[]{
                    b.getBidId(), b.getBuyerId(),
                    "%.2f".formatted(b.getAmount()),
                    b.getTimestamp().format(FMT)
            });
        }

        Bid highest = bidService.getHighestBid(auctionId);
        if (highest != null) {
            highestBidLabel.setText("$%.2f  (by %s)".formatted(
                    highest.getAmount(), highest.getBuyerId()));
        } else {
            Auction a = auctionService.findById(auctionId);
            if (a != null) {
                highestBidLabel.setText("No bids yet (product: %s)"
                        .formatted(a.getProductId()));
            } else {
                highestBidLabel.setText("—");
            }
        }

        // Show slab-based minimum next bid hint
        double minNext = bidService.getMinNextBid(auctionId);
        if (minNext > 0) {
            hintLabel.setText("$%.2f  (bid must exceed this)".formatted(minNext));
        } else {
            hintLabel.setText("—");
        }
    }

    public void refreshTable() {
        // Refresh auction dropdown
        ComboItem prevAuc = (ComboItem) auctionCombo.getSelectedItem();
        auctionCombo.removeAllItems();
        for (Auction a : auctionService.getAuctionsSortedByEndTime()) {
            String label = "%s — %s [%s]".formatted(
                    a.getAuctionId(), a.getProductId(), a.getStatus());
            auctionCombo.addItem(new ComboItem(a.getAuctionId(), label));
        }
        if (prevAuc != null) {
            for (int i = 0; i < auctionCombo.getItemCount(); i++) {
                if (auctionCombo.getItemAt(i).id.equals(prevAuc.id)) {
                    auctionCombo.setSelectedIndex(i);
                    break;
                }
            }
        }

        // Refresh buyer dropdown
        ComboItem prevBuy = (ComboItem) buyerCombo.getSelectedItem();
        buyerCombo.removeAllItems();
        for (Buyer b : buyerService.getAll()) {
            buyerCombo.addItem(new ComboItem(b.getBuyerId(),
                    b.getBuyerId() + " — " + b.getName()));
        }
        if (prevBuy != null) {
            for (int i = 0; i < buyerCombo.getItemCount(); i++) {
                if (buyerCombo.getItemAt(i).id.equals(prevBuy.id)) {
                    buyerCombo.setSelectedIndex(i);
                    break;
                }
            }
        }

        // Refresh history for currently selected auction
        ComboItem auc = (ComboItem) auctionCombo.getSelectedItem();
        if (auc != null) {
            refreshBidHistory(auc.id);
        } else {
            historyModel.setRowCount(0);
            highestBidLabel.setText("—");
        }
    }

    // ── Helpers ──────────────────────────────────────────────

    static class ComboItem {
        final String id;
        private final String display;
        ComboItem(String id, String display) { this.id = id; this.display = display; }
        @Override public String toString() { return display; }
    }

    private void showWarning(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Warning",
                JOptionPane.WARNING_MESSAGE);
    }
}
