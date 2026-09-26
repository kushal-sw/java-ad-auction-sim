package auction.gui;

import auction.exceptions.InvalidDataException;
import auction.model.Product;
import auction.model.Seller;
import auction.service.ProductService;
import auction.service.SellerService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * CRUD panel for managing products, with a seller dropdown.
 */
public class ProductPanel extends JPanel {

    private final ProductService productService;
    private final SellerService  sellerService;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JTextField idField;
    private final JTextField nameField;
    private final JTextField descField;
    private final JTextField priceField;
    private final JComboBox<ComboItem> sellerCombo;
    private final AtomicInteger idCounter = new AtomicInteger(1);

    public ProductPanel(ProductService productService, SellerService sellerService) {
        this.productService = productService;
        this.sellerService  = sellerService;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ── Table ────────────────────────────────────────────
        tableModel = new DefaultTableModel(
                new String[]{"ID", "Name", "Description", "Base Price", "Seller", "Status"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                populateForm(table.getSelectedRow());
            }
        });
        add(new JScrollPane(table), BorderLayout.CENTER);

        // ── Form ─────────────────────────────────────────────
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Product Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        int row = 0;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formPanel.add(new JLabel("ID:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        idField = new JTextField(15);
        idField.setEditable(false);
        idField.setBackground(new Color(240, 240, 240));
        formPanel.add(idField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        nameField = new JTextField(15);
        formPanel.add(nameField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        descField = new JTextField(15);
        formPanel.add(descField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formPanel.add(new JLabel("Base Price:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        priceField = new JTextField(15);
        formPanel.add(priceField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        formPanel.add(new JLabel("Seller:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        sellerCombo = new JComboBox<>();
        formPanel.add(sellerCombo, gbc);

        // ── Buttons ──────────────────────────────────────────
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        JButton addBtn    = new JButton("Add");
        JButton updateBtn = new JButton("Update");
        JButton deleteBtn = new JButton("Delete");
        JButton clearBtn  = new JButton("Clear");
        buttonPanel.add(addBtn);
        buttonPanel.add(updateBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(clearBtn);

        addBtn.addActionListener(e -> doAdd());
        updateBtn.addActionListener(e -> doUpdate());
        deleteBtn.addActionListener(e -> doDelete());
        clearBtn.addActionListener(e -> clearForm());

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(formPanel, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);

        refreshTable();
    }

    // ── Actions ──────────────────────────────────────────────

    private void doAdd() {
        try {
            ComboItem seller = (ComboItem) sellerCombo.getSelectedItem();
            if (seller == null) { showWarning("Select a seller."); return; }

            double price = Double.parseDouble(priceField.getText().trim());
            String id = nextId();
            Product product = new Product(id, nameField.getText().trim(),
                    descField.getText().trim(), price, seller.id);
            productService.addProduct(product);
            refreshTable();
            clearForm();
        } catch (NumberFormatException ex) {
            showWarning("Base price must be a valid number.");
        } catch (InvalidDataException ex) {
            showError(ex);
        }
    }

    private void doUpdate() {
        int r = table.getSelectedRow();
        if (r < 0) { showWarning("Select a product to update."); return; }
        try {
            ComboItem seller = (ComboItem) sellerCombo.getSelectedItem();
            if (seller == null) { showWarning("Select a seller."); return; }

            String id = (String) tableModel.getValueAt(r, 0);
            double price = Double.parseDouble(priceField.getText().trim());
            Product updated = new Product(id, nameField.getText().trim(),
                    descField.getText().trim(), price, seller.id);
            // Carry over the existing status
            Product existing = productService.findById(id);
            if (existing != null) updated.setStatus(existing.getStatus());
            productService.updateProduct(updated);
            refreshTable();
        } catch (NumberFormatException ex) {
            showWarning("Base price must be a valid number.");
        } catch (InvalidDataException ex) {
            showError(ex);
        }
    }

    private void doDelete() {
        int r = table.getSelectedRow();
        if (r < 0) { showWarning("Select a product to delete."); return; }
        try {
            productService.deleteProduct((String) tableModel.getValueAt(r, 0));
            refreshTable();
            clearForm();
        } catch (InvalidDataException ex) {
            showError(ex);
        }
    }

    // ── Helpers ──────────────────────────────────────────────

    private String nextId() {
        String id;
        do {
            id = "P%03d".formatted(idCounter.getAndIncrement());
        } while (productService.findById(id) != null);
        return id;
    }

    private void populateForm(int row) {
        idField.setText((String) tableModel.getValueAt(row, 0));
        nameField.setText((String) tableModel.getValueAt(row, 1));
        descField.setText((String) tableModel.getValueAt(row, 2));
        priceField.setText(tableModel.getValueAt(row, 3).toString());
        // Select matching seller in combo
        String sellerId = (String) tableModel.getValueAt(row, 4);
        for (int i = 0; i < sellerCombo.getItemCount(); i++) {
            if (sellerCombo.getItemAt(i).id.equals(sellerId)) {
                sellerCombo.setSelectedIndex(i);
                break;
            }
        }
    }

    private void clearForm() {
        idField.setText("");
        nameField.setText("");
        descField.setText("");
        priceField.setText("");
        if (sellerCombo.getItemCount() > 0) sellerCombo.setSelectedIndex(0);
        table.clearSelection();
    }

    public void refreshTable() {
        // Refresh seller dropdown
        ComboItem prev = (ComboItem) sellerCombo.getSelectedItem();
        sellerCombo.removeAllItems();
        for (Seller s : sellerService.getAll()) {
            sellerCombo.addItem(new ComboItem(s.getSellerId(),
                    s.getSellerId() + " — " + s.getName()));
        }
        // Restore previous selection if still present
        if (prev != null) {
            for (int i = 0; i < sellerCombo.getItemCount(); i++) {
                if (sellerCombo.getItemAt(i).id.equals(prev.id)) {
                    sellerCombo.setSelectedIndex(i);
                    break;
                }
            }
        }

        // Refresh table
        tableModel.setRowCount(0);
        for (Product p : productService.getAll()) {
            tableModel.addRow(new Object[]{
                    p.getProductId(), p.getName(), p.getDescription(),
                    "%.2f".formatted(p.getBasePrice()),
                    p.getSellerId(), p.getStatus()
            });
        }
    }

    /** Lightweight wrapper for JComboBox items. */
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
