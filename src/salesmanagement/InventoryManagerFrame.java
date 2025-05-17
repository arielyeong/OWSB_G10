package salesmanagement;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import salesmanagement.po.PoItem;

public class InventoryManagerFrame extends JFrame {
    private ItemManager itemManager;
    private PoManager poManager;
    private String inventoryManagerId;
    
    private JTable itemsTable;
    private JTable poTable;
    private JTable lowStockTable;
    private JComboBox<String> poStatusFilter;
    
    private static final String[] ITEM_COLUMNS = {"Item ID", "Name", "Category", "UOM", "Unit Price", "Stock Quantity"};
    private static final String[] PO_COLUMNS = {"PO ID", "PR ID", "Supplier ID", "Total Cost", "Status", "Created Date"};
    private static final String[] LOW_STOCK_COLUMNS = {"Item ID", "Name", "Category", "Current Stock", "Status"};
    
    // Threshold for low stock alert
    private static final int LOW_STOCK_THRESHOLD = 10;
    
    public InventoryManagerFrame(String inventoryManagerId) {
        this.inventoryManagerId = inventoryManagerId;
        this.itemManager = new ItemManager();
        this.poManager = new PoManager();
        
        initComponents();
        loadData();
    }
    
    private void initComponents() {
        setTitle("Inventory Manager Dashboard");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Create tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Items tab
        JPanel itemsPanel = createItemsPanel();
        tabbedPane.addTab("Inventory Items", itemsPanel);
        
        // Purchase Orders tab
        JPanel poPanel = createPurchaseOrdersPanel();
        tabbedPane.addTab("Purchase Orders", poPanel);
        
        // Low Stock Alerts tab
        JPanel lowStockPanel = createLowStockPanel();
        tabbedPane.addTab("Low Stock Alerts", lowStockPanel);
        
        // Reports tab
        JPanel reportsPanel = createReportsPanel();
        tabbedPane.addTab("Reports", reportsPanel);
        
        // Add tabbed pane to frame
        getContentPane().add(tabbedPane);
        
        // Add back to main menu button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton backButton = new JButton("Back to Main Menu");
        backButton.addActionListener(e -> {
            new MainMenu().setVisible(true);
            dispose();
        });
        bottomPanel.add(backButton);
        getContentPane().add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createItemsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create table
        DefaultTableModel model = new DefaultTableModel(ITEM_COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        itemsTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(itemsTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Create buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadItemsData());
        
        JButton updateStockButton = new JButton("Update Stock");
        updateStockButton.addActionListener(e -> showUpdateStockDialog());
        
        buttonsPanel.add(refreshButton);
        buttonsPanel.add(updateStockButton);
        
        panel.add(buttonsPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createPurchaseOrdersPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Filter by Status:"));
        
        String[] statusOptions = {"All", "Pending", "Approved", "Delivered", "Paid", "Rejected"};
        poStatusFilter = new JComboBox<>(statusOptions);
        poStatusFilter.addActionListener(e -> loadPurchaseOrdersData());
        filterPanel.add(poStatusFilter);
        
        panel.add(filterPanel, BorderLayout.NORTH);
        
        // Create table
        DefaultTableModel model = new DefaultTableModel(PO_COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        poTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(poTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Create buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadPurchaseOrdersData());
        
        JButton viewDetailsButton = new JButton("View Details");
        viewDetailsButton.addActionListener(e -> viewPoDetails());
        
        JButton markDeliveredButton = new JButton("Mark as Delivered");
        markDeliveredButton.addActionListener(e -> markPoAsDelivered());
        
        JButton updateInventoryButton = new JButton("Update Inventory");
        updateInventoryButton.addActionListener(e -> updateInventoryFromPo());
        
        buttonsPanel.add(refreshButton);
        buttonsPanel.add(viewDetailsButton);
        buttonsPanel.add(markDeliveredButton);
        buttonsPanel.add(updateInventoryButton);
        
        panel.add(buttonsPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createLowStockPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create table
        DefaultTableModel model = new DefaultTableModel(LOW_STOCK_COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        lowStockTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(lowStockTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Create buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadLowStockData());
        
        buttonsPanel.add(refreshButton);
        
        panel.add(buttonsPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createReportsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("Generate Reports");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setAlignmentX(CENTER_ALIGNMENT);
        panel.add(titleLabel);
        
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        JButton stockReportButton = new JButton("Generate Stock Report");
        stockReportButton.setAlignmentX(CENTER_ALIGNMENT);
        stockReportButton.setMaximumSize(new Dimension(200, 30));
        stockReportButton.addActionListener(e -> generateStockReport());
        panel.add(stockReportButton);
        
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        JButton poReportButton = new JButton("Generate PO Report");
        poReportButton.setAlignmentX(CENTER_ALIGNMENT);
        poReportButton.setMaximumSize(new Dimension(200, 30));
        poReportButton.addActionListener(e -> generatePoReport());
        panel.add(poReportButton);
        
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        JButton lowStockReportButton = new JButton("Generate Low Stock Report");
        lowStockReportButton.setAlignmentX(CENTER_ALIGNMENT);
        lowStockReportButton.setMaximumSize(new Dimension(200, 30));
        lowStockReportButton.addActionListener(e -> generateLowStockReport());
        panel.add(lowStockReportButton);
        
        return panel;
    }
    
    private void loadData() {
        loadItemsData();
        loadPurchaseOrdersData();
        loadLowStockData();
    }
    
    private void loadItemsData() {
        DefaultTableModel model = (DefaultTableModel) itemsTable.getModel();
        model.setRowCount(0);
        
        List<Item> items = itemManager.getAllItems();
        for (Item item : items) {
            model.addRow(new Object[]{
                item.getItemId(),
                item.getItemName(),
                item.getItemCategory(),
                item.getItemUOM(),
                String.format("%.2f", item.getItemUnitPrice()),
                item.getStockQuantity()
            });
        }
    }
    
    private void loadPurchaseOrdersData() {
        DefaultTableModel model = (DefaultTableModel) poTable.getModel();
        model.setRowCount(0);
        
        String selectedStatus = (String) poStatusFilter.getSelectedItem();
        List<po> poList;
        
        if ("All".equals(selectedStatus)) {
            poList = poManager.getAllPo();
        } else {
            poList = poManager.findPoByStatus(selectedStatus);
        }
        
        for (po po : poList) {
            model.addRow(new Object[]{
                po.getPoId(),
                po.getPrId(),
                po.getSupplierId(),
                String.format("%.2f", po.getTotalCost()),
                po.getPoStatus(),
                po.getCreatedDate().format(po.DATE_FORMATTER)
            });
        }
    }
    
    private void loadLowStockData() {
        DefaultTableModel model = (DefaultTableModel) lowStockTable.getModel();
        model.setRowCount(0);
        
        List<Item> items = itemManager.getAllItems();
        for (Item item : items) {
            if (item.getStockQuantity() <= LOW_STOCK_THRESHOLD) {
                String status = item.getStockQuantity() == 0 ? "OUT OF STOCK" : "LOW STOCK";
                model.addRow(new Object[]{
                    item.getItemId(),
                    item.getItemName(),
                    item.getItemCategory(),
                    item.getStockQuantity(),
                    status
                });
            }
        }
    }
    
    private void showUpdateStockDialog() {
        int selectedRow = itemsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item to update.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String itemId = (String) itemsTable.getValueAt(selectedRow, 0);
        Item item = itemManager.findItemById(itemId);
        
        if (item == null) {
            JOptionPane.showMessageDialog(this, "Item not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        JPanel itemInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        itemInfoPanel.add(new JLabel("Item: " + item.getItemName()));
        panel.add(itemInfoPanel);
        
        JPanel currentStockPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        currentStockPanel.add(new JLabel("Current Stock: " + item.getStockQuantity()));
        panel.add(currentStockPanel);
        
        JPanel newStockPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        newStockPanel.add(new JLabel("New Stock Quantity:"));
        JTextField newStockField = new JTextField(10);
        newStockField.setText(String.valueOf(item.getStockQuantity()));
        newStockPanel.add(newStockField);
        panel.add(newStockPanel);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "Update Stock", JOptionPane.OK_CANCEL_OPTION);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                int newStock = Integer.parseInt(newStockField.getText());
                if (newStock < 0) {
                    JOptionPane.showMessageDialog(this, "Stock quantity cannot be negative.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                item.setStockQuantity(newStock);
                boolean success = itemManager.updateItem(item);
                
                if (success) {
                    JOptionPane.showMessageDialog(this, "Stock updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadItemsData();
                    loadLowStockData();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update stock.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Please enter a valid number.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void viewPoDetails() {
        int selectedRow = poTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a purchase order to view.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String poId = (String) poTable.getValueAt(selectedRow, 0);
        po po = poManager.findPoById(poId);
        
        if (po == null) {
            JOptionPane.showMessageDialog(this, "Purchase order not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        StringBuilder details = new StringBuilder();
        details.append("PO ID: ").append(po.getPoId()).append("\n");
        details.append("PR ID: ").append(po.getPrId()).append("\n");
        details.append("Supplier ID: ").append(po.getSupplierId()).append("\n");
        details.append("Status: ").append(po.getPoStatus()).append("\n");
        details.append("Created Date: ").append(po.getCreatedDate().format(po.DATE_FORMATTER)).append("\n");
        details.append("Total Cost: $").append(String.format("%.2f", po.getTotalCost())).append("\n\n");
        
        details.append("Items:\n");
        for (PoItem item : po.getItems()) {
            details.append("- ").append(item.getItem().getItemName())
                  .append(" (").append(item.getItem().getItemId()).append(")")
                  .append(": ").append(item.getQuantity())
                  .append(" x $").append(String.format("%.2f", item.getItem().getItemUnitPrice()))
                  .append(" = $").append(String.format("%.2f", item.getCost()))
                  .append("\n");
        }
        
        JOptionPane.showMessageDialog(this, details.toString(), "Purchase Order Details", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void markPoAsDelivered() {
        int selectedRow = poTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a purchase order to mark as delivered.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String poId = (String) poTable.getValueAt(selectedRow, 0);
        po po = poManager.findPoById(poId);
        
        if (po == null) {
            JOptionPane.showMessageDialog(this, "Purchase order not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (!"Approved".equals(po.getPoStatus())) {
            JOptionPane.showMessageDialog(this, "Only approved purchase orders can be marked as delivered.", "Invalid Status", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to mark this purchase order as delivered?\n" +
                "This will record that all items have been received.", 
                "Confirm Delivery", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = poManager.markPoAsDelivered(poId, inventoryManagerId, LocalDate.now());
            
            if (success) {
                JOptionPane.showMessageDialog(this, "Purchase order marked as delivered.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadPurchaseOrdersData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update purchase order.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void updateInventoryFromPo() {
        int selectedRow = poTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a purchase order to update inventory.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String poId = (String) poTable.getValueAt(selectedRow, 0);
        po po = poManager.findPoById(poId);
        
        if (po == null) {
            JOptionPane.showMessageDialog(this, "Purchase order not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (!"Delivered".equals(po.getPoStatus())) {
            JOptionPane.showMessageDialog(this, "Only delivered purchase orders can update inventory.", "Invalid Status", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (po.isInventoryUpdated()) {
            JOptionPane.showMessageDialog(this, "Inventory has already been updated for this purchase order.", "Already Updated", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to update inventory with items from this purchase order?\n" +
                "This will increase stock levels for all items in the PO.", 
                "Confirm Inventory Update", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = poManager.updateInventory(poId, itemManager);
            
            if (success) {
                JOptionPane.showMessageDialog(this, "Inventory updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadPurchaseOrdersData();
                loadItemsData();
                loadLowStockData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update inventory.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void generateStockReport() {
        try {
            String fileName = "stock_report_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".txt";
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
                writer.write("===== STOCK REPORT =====");
                writer.newLine();
                writer.write("Generated on: " + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                writer.newLine();
                writer.write("Generated by: Inventory Manager (ID: " + inventoryManagerId + ")");
                writer.newLine();
                writer.newLine();
                
                writer.write(String.format("%-10s %-30s %-15s %-10s %-15s", "Item ID", "Name", "Category", "Stock", "Value"));
                writer.newLine();
                writer.write(String.format("%-10s %-30s %-15s %-10s %-15s", "-------", "----", "--------", "-----", "-----"));
                writer.newLine();
                
                List<Item> items = itemManager.getAllItems();
                double totalValue = 0;
                
                for (Item item : items) {
                    double itemValue = item.getItemUnitPrice() * item.getStockQuantity();
                    totalValue += itemValue;
                    
                    writer.write(String.format("%-10s %-30s %-15s %-10d $%-14.2f", 
                            item.getItemId(), 
                            item.getItemName(), 
                            item.getItemCategory(), 
                            item.getStockQuantity(),
                            itemValue));
                    writer.newLine();
                }
                
                writer.newLine();
                writer.write(String.format("Total Inventory Value: $%.2f", totalValue));
                writer.newLine();
                writer.write("=======================");
            }
            
            JOptionPane.showMessageDialog(this, "Stock report generated successfully: " + fileName, "Report Generated", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error generating report: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void generatePoReport() {
        try {
            String fileName = "po_report_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".txt";
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
                writer.write("===== PURCHASE ORDER REPORT =====");
                writer.newLine();
                writer.write("Generated on: " + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                writer.newLine();
                writer.write("Generated by: Inventory Manager (ID: " + inventoryManagerId + ")");
                writer.newLine();
                writer.newLine();
                
                writer.write(String.format("%-10s %-10s %-15s %-15s %-15s %-15s", 
                        "PO ID", "PR ID", "Supplier", "Status", "Created Date", "Total Cost"));
                writer.newLine();
                writer.write(String.format("%-10s %-10s %-15s %-15s %-15s %-15s", 
                        "-----", "-----", "--------", "------", "------------", "----------"));
                writer.newLine();
                
                List<po> poList = poManager.getAllPo();
                double totalValue = 0;
                
                for (po po : poList) {
                    totalValue += po.getTotalCost();
                    
                    writer.write(String.format("%-10s %-10s %-15s %-15s %-15s $%-14.2f", 
                            po.getPoId(), 
                            po.getPrId(), 
                            po.getSupplierId(), 
                            po.getPoStatus(),
                            po.getCreatedDate().format(po.DATE_FORMATTER),
                            po.getTotalCost()));
                    writer.newLine();
                }
                
                writer.newLine();
                writer.write(String.format("Total PO Value: $%.2f", totalValue));
                writer.newLine();
                writer.write("================================");
            }
            
            JOptionPane.showMessageDialog(this, "PO report generated successfully: " + fileName, "Report Generated", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error generating report: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void generateLowStockReport() {
        try {
            String fileName = "low_stock_report_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".txt";
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
                writer.write("===== LOW STOCK REPORT =====");
                writer.newLine();
                writer.write("Generated on: " + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                writer.newLine();
                writer.write("Generated by: Inventory Manager (ID: " + inventoryManagerId + ")");
                writer.newLine();
                writer.write("Low Stock Threshold: " + LOW_STOCK_THRESHOLD);
                writer.newLine();
                writer.newLine();
                
                writer.write(String.format("%-10s %-30s %-15s %-10s %-15s", 
                        "Item ID", "Name", "Category", "Stock", "Status"));
                writer.newLine();
                writer.write(String.format("%-10s %-30s %-15s %-10s %-15s", 
                        "-------", "----", "--------", "-----", "------"));
                writer.newLine();
                
                List<Item> items = itemManager.getAllItems();
                int lowStockCount = 0;
                int outOfStockCount = 0;
                
                for (Item item : items) {
                    if (item.getStockQuantity() <= LOW_STOCK_THRESHOLD) {
                        String status = item.getStockQuantity() == 0 ? "OUT OF STOCK" : "LOW STOCK";
                        
                        if (item.getStockQuantity() == 0) {
                            outOfStockCount++;
                        } else {
                            lowStockCount++;
                        }
                        
                        writer.write(String.format("%-10s %-30s %-15s %-10d %-15s", 
                                item.getItemId(), 
                                item.getItemName(), 
                                item.getItemCategory(), 
                                item.getStockQuantity(),
                                status));
                        writer.newLine();
                    }
                }
                
                writer.newLine();
                writer.write("Summary:");
                writer.newLine();
                writer.write("Low Stock Items: " + lowStockCount);
                writer.newLine();
                writer.write("Out of Stock Items: " + outOfStockCount);
                writer.newLine();
                writer.write("Total Items Requiring Attention: " + (lowStockCount + outOfStockCount));
                writer.newLine();
                writer.write("============================");
            }
            
            JOptionPane.showMessageDialog(this, "Low stock report generated successfully: " + fileName, "Report Generated", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error generating report: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public static void main(String[] args) {
        // For testing purposes
        new InventoryManagerFrame("IM001").setVisible(true);
    }
}