package com.disastermanagement.ui;

import com.disastermanagement.dao.DisasterDAO;
import com.disastermanagement.model.Disaster;
import com.disastermanagement.model.Resource;
import com.disastermanagement.model.ResourceDistribution;
import com.disastermanagement.model.User;
import com.disastermanagement.service.ResourceService;
import com.disastermanagement.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel managing relief inventory, depot tracking, stock warnings, and transactional distributions.
 */
public class ResourcePanel extends JPanel {

    private final User currentUser;
    private final ResourceService resourceService = new ResourceService();
    private final DisasterDAO disasterDAO = new DisasterDAO();

    private JTable inventoryTable;
    private DefaultTableModel inventoryModel;
    private JTable distributionTable;
    private DefaultTableModel distributionModel;

    public ResourcePanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(20, 24, 24, 24));

        initComponents();
        loadInventory();
        loadDistributions();
    }

    private void initComponents() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(UITheme.BG_MAIN);

        JLabel titleLabel = new JLabel("Relief Inventory & Supply Logistics Tracking");
        titleLabel.setFont(UITheme.FONT_HEADER_LARGE);
        titleLabel.setForeground(UITheme.PRIMARY_NAVY);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setBackground(UITheme.BG_MAIN);

        JButton addResBtn = UITheme.createPrimaryButton("+ Add Relief Supply Item");
        addResBtn.addActionListener(e -> showAddResourceDialog());
        btnPanel.add(addResBtn);

        JButton distBtn = UITheme.createSuccessButton("🚚 Distribute Supplies to Incident");
        distBtn.addActionListener(e -> showDistributeDialog());
        btnPanel.add(distBtn);

        JButton refreshBtn = UITheme.createSecondaryButton("↻ Refresh");
        refreshBtn.addActionListener(e -> {
            loadInventory();
            loadDistributions();
        });
        btnPanel.add(refreshBtn);

        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(btnPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Center Tabbed Pane: (1) Inventory Warehouse and (2) Distribution History
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.FONT_BODY_BOLD);

        // Tab 1: Inventory Card
        JPanel invCard = UITheme.createCardPanel();
        invCard.setLayout(new BorderLayout(8, 8));

        String[] invCols = {"ID", "Resource Name", "Category", "Available Stock", "Unit", "Storage Depot", "Min Threshold", "Stock Status"};
        inventoryModel = new DefaultTableModel(invCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        inventoryTable = new JTable(inventoryModel);
        UITheme.styleTable(inventoryTable);

        // Custom renderer to highlight low stock in Warning Red / Orange
        inventoryTable.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                String val = (value == null) ? "" : value.toString();
                if ("LOW STOCK ALERT".equalsIgnoreCase(val) || "OUT OF STOCK".equalsIgnoreCase(val)) {
                    c.setForeground(Color.RED);
                    c.setFont(UITheme.FONT_BODY_BOLD);
                } else {
                    c.setForeground(UITheme.ACCENT_GREEN.darker());
                    c.setFont(UITheme.FONT_BODY);
                }
                setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        });

        invCard.add(new JScrollPane(inventoryTable), BorderLayout.CENTER);

        JPanel invBottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        invBottom.setBackground(Color.WHITE);
        JButton updateStockBtn = UITheme.createSecondaryButton("Update Stock Quantity");
        updateStockBtn.addActionListener(e -> updateStockQuantity());
        invBottom.add(updateStockBtn);
        invCard.add(invBottom, BorderLayout.SOUTH);

        tabbedPane.addTab("Warehouse Inventory Levels", invCard);

        // Tab 2: Distribution History Card
        JPanel distCard = UITheme.createCardPanel();
        distCard.setLayout(new BorderLayout(8, 8));

        String[] distCols = {"Dist ID", "Disaster Incident", "Relief Resource", "Quantity Dispatched", "Unit", "Authorized By", "Date / Time", "Dispatch Notes"};
        distributionModel = new DefaultTableModel(distCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        distributionTable = new JTable(distributionModel);
        UITheme.styleTable(distributionTable);
        distCard.add(new JScrollPane(distributionTable), BorderLayout.CENTER);

        tabbedPane.addTab("Relief Distribution Audit Log", distCard);

        add(tabbedPane, BorderLayout.CENTER);
    }

    private void loadInventory() {
        try {
            inventoryModel.setRowCount(0);
            List<Resource> resources = resourceService.getAllResources();
            for (Resource r : resources) {
                String status = (r.getQuantity() == 0) ? "OUT OF STOCK" :
                        (r.isLowStock() ? "LOW STOCK ALERT" : "SUFFICIENT");
                inventoryModel.addRow(new Object[]{
                        r.getResourceId(),
                        r.getResourceName(),
                        r.getCategory(),
                        r.getQuantity(),
                        r.getUnit(),
                        r.getStorageLocation(),
                        r.getMinimumStock(),
                        status
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load resources: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadDistributions() {
        try {
            distributionModel.setRowCount(0);
            List<ResourceDistribution> list = resourceService.getDistributionHistory();
            for (ResourceDistribution rd : list) {
                distributionModel.addRow(new Object[]{
                        rd.getDistributionId(),
                        rd.getDisasterTitle(),
                        rd.getResourceName(),
                        rd.getQuantityDistributed(),
                        rd.getUnit(),
                        rd.getDistributedByName(),
                        rd.getDistributionDate(),
                        rd.getNotes()
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load distribution history: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAddResourceDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Relief Supply Item", true);
        dialog.setSize(460, 480);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(6, 2, 10, 12));
        form.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField nameField = new JTextField();
        JComboBox<String> catBox = new JComboBox<>(new String[]{
                "Food", "Water", "Medical", "Rescue Equipment", "Shelter Supplies", "Clothing", "Other"
        });
        JSpinner qtySpinner = new JSpinner(new SpinnerNumberModel(500, 0, 1000000, 50));
        JTextField unitField = new JTextField("Packets");
        JTextField locField = new JTextField("Central Depot - Bay 1");
        JSpinner minSpinner = new JSpinner(new SpinnerNumberModel(100, 0, 100000, 10));

        form.add(new JLabel("Resource Name:"));
        form.add(nameField);
        form.add(new JLabel("Category:"));
        form.add(catBox);
        form.add(new JLabel("Initial Quantity:"));
        form.add(qtySpinner);
        form.add(new JLabel("Unit (e.g. Kits, Bottles):"));
        form.add(unitField);
        form.add(new JLabel("Storage Depot Location:"));
        form.add(locField);
        form.add(new JLabel("Minimum Stock Alert Threshold:"));
        form.add(minSpinner);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = UITheme.createPrimaryButton("Save Supply Item");
        JButton cancelBtn = UITheme.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        saveBtn.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                String cat = (String) catBox.getSelectedItem();
                int qty = (int) qtySpinner.getValue();
                String unit = unitField.getText().trim();
                String loc = locField.getText().trim();
                int min = (int) minSpinner.getValue();

                Resource res = new Resource(name, cat, qty, unit, loc, min);
                resourceService.addResource(res, currentUser);

                JOptionPane.showMessageDialog(dialog, "Resource supply saved!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadInventory();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);
        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void updateStockQuantity() {
        int row = inventoryTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a resource item from the table.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int resId = (int) inventoryModel.getValueAt(row, 0);
        String name = (String) inventoryModel.getValueAt(row, 1);
        int currentQty = (int) inventoryModel.getValueAt(row, 3);

        String input = JOptionPane.showInputDialog(this,
                "Enter updated stock quantity for '" + name + "':", currentQty);

        if (input != null && !input.trim().isEmpty()) {
            try {
                int newQty = Integer.parseInt(input.trim());
                if (newQty < 0) {
                    JOptionPane.showMessageDialog(this, "Quantity cannot be negative.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                List<Resource> all = resourceService.getAllResources();
                for (Resource r : all) {
                    if (r.getResourceId() == resId) {
                        r.setQuantity(newQty);
                        resourceService.updateResource(r, currentUser);
                        break;
                    }
                }
                JOptionPane.showMessageDialog(this, "Stock quantity updated successfully.", "Updated", JOptionPane.INFORMATION_MESSAGE);
                loadInventory();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid numeric integer.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Update error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showDistributeDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Distribute Relief Supplies (Atomic Transaction)", true);
        dialog.setSize(500, 420);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(5, 2, 10, 14));
        form.setBorder(new EmptyBorder(20, 20, 20, 20));

        JComboBox<Resource> resBox = new JComboBox<>();
        try {
            List<Resource> resources = resourceService.getAllResources();
            for (Resource r : resources) resBox.addItem(r);
        } catch (Exception ignored) {}

        JComboBox<Disaster> disasterBox = new JComboBox<>();
        try {
            List<Disaster> disasters = disasterDAO.findActiveDisasters();
            for (Disaster d : disasters) disasterBox.addItem(d);
        } catch (Exception ignored) {}

        JSpinner qtySpinner = new JSpinner(new SpinnerNumberModel(50, 1, 100000, 10));
        JTextField notesField = new JTextField("Emergency distribution to sector relief staging");

        JLabel availLabel = new JLabel("Select resource to check stock");
        availLabel.setForeground(UITheme.TEXT_MUTED);

        resBox.addActionListener(e -> {
            Resource r = (Resource) resBox.getSelectedItem();
            if (r != null) {
                availLabel.setText(String.format("Available: %d %s (Threshold: %d)", r.getQuantity(), r.getUnit(), r.getMinimumStock()));
                availLabel.setForeground(r.isLowStock() ? UITheme.ACCENT_RED : UITheme.ACCENT_GREEN.darker());
            }
        });

        form.add(new JLabel("Relief Resource:"));
        form.add(resBox);
        form.add(new JLabel("Stock Availability:"));
        form.add(availLabel);
        form.add(new JLabel("Target Incident:"));
        form.add(disasterBox);
        form.add(new JLabel("Quantity to Distribute:"));
        form.add(qtySpinner);
        form.add(new JLabel("Shipment Notes:"));
        form.add(notesField);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton dispatchBtn = UITheme.createSuccessButton("Execute Atomic Distribution");
        JButton cancelBtn = UITheme.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        dispatchBtn.addActionListener(e -> {
            try {
                Resource selectedRes = (Resource) resBox.getSelectedItem();
                Disaster selectedDis = (Disaster) disasterBox.getSelectedItem();
                int qty = (int) qtySpinner.getValue();
                String notes = notesField.getText().trim();

                if (selectedRes == null || selectedDis == null) {
                    JOptionPane.showMessageDialog(dialog, "Please select both a resource and an active disaster.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Execute safe transaction
                boolean success = resourceService.distributeResource(
                        selectedRes.getResourceId(),
                        selectedDis.getDisasterId(),
                        qty,
                        notes,
                        currentUser
                );

                if (success) {
                    JOptionPane.showMessageDialog(dialog,
                            String.format("Distribution of %d %s to '%s' completed successfully!\nInventory stock was atomically updated.",
                                    qty, selectedRes.getUnit(), selectedDis.getTitle()),
                            "Transaction Committed", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadInventory();
                    loadDistributions();
                }

            } catch (Exception ex) {
                // If stock was insufficient, transaction automatically rolled back
                JOptionPane.showMessageDialog(dialog,
                        "Distribution Transaction Blocked / Rolled Back:\n" + ex.getMessage(),
                        "Transaction Aborted", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(dispatchBtn);
        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
}
