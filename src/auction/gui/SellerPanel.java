package auction.gui;

import auction.exceptions.InvalidDataException;
import auction.model.Seller;
import auction.service.SellerService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * CRUD panel for managing sellers.
 */
public class SellerPanel extends JPanel {

    private final SellerService sellerService;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JTextField idField;
    private final JTextField nameField;
    private final JTextField emailField;
    private final AtomicInteger idCounter = new AtomicInteger(1);

    public SellerPanel(SellerService sellerService) {
        this.sellerService = sellerService;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ── Table ────────────────────────────────────────────
        tableModel = new DefaultTableModel(new String[]{"ID", "Name", "Email"}, 0) {
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
        formPanel.setBorder(BorderFactory.createTitledBorder("Seller Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        formPanel.add(new JLabel("ID:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        idField = new JTextField(15);
        idField.setEditable(false);
        idField.setBackground(new Color(240, 240, 240));
        formPanel.add(idField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        formPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        nameField = new JTextField(15);
        formPanel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        emailField = new JTextField(15);
        formPanel.add(emailField, gbc);

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
            String id = nextId();
            Seller seller = new Seller(id, nameField.getText().trim(),
                                           emailField.getText().trim());
            sellerService.addSeller(seller);
            refreshTable();
            clearForm();
        } catch (InvalidDataException ex) {
            showError(ex);
        }
    }

    private void doUpdate() {
        int row = table.getSelectedRow();
        if (row < 0) { showWarning("Select a seller to update."); return; }
        try {
            String id = (String) tableModel.getValueAt(row, 0);
            Seller updated = new Seller(id, nameField.getText().trim(),
                                            emailField.getText().trim());
            sellerService.updateSeller(updated);
            refreshTable();
        } catch (InvalidDataException ex) {
            showError(ex);
        }
    }

    private void doDelete() {
        int row = table.getSelectedRow();
        if (row < 0) { showWarning("Select a seller to delete."); return; }
        try {
            sellerService.deleteSeller((String) tableModel.getValueAt(row, 0));
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
            id = "S%03d".formatted(idCounter.getAndIncrement());
        } while (sellerService.findById(id) != null);
        return id;
    }

    private void populateForm(int row) {
        idField.setText((String) tableModel.getValueAt(row, 0));
        nameField.setText((String) tableModel.getValueAt(row, 1));
        emailField.setText((String) tableModel.getValueAt(row, 2));
    }

    private void clearForm() {
        idField.setText("");
        nameField.setText("");
        emailField.setText("");
        table.clearSelection();
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        for (Seller s : sellerService.getAll()) {
            tableModel.addRow(new Object[]{s.getSellerId(), s.getName(), s.getEmail()});
        }
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
