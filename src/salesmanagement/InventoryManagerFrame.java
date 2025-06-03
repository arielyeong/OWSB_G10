package salesmanagement;


import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import usermanagement.impage;

public class InventoryManagerFrame extends JFrame {
    private InventoryManagerSystem inventorySystem;
    private ItemManager itemManager;
    private PoManager poManager;
    
    private JTable itemTable;
    private JTable poTable;
    private DefaultTableModel itemTableModel;
    private DefaultTableModel poTableModel;
    private JTextField searchField;
    private JTextField poIdField;
    private JTextField receivedQuantityField;
    private JButton updateStockButton;
    private JButton generateReportButton;
    private JButton viewLowStockButton;
    private JButton refreshButton;
    private JButton backButton;
    
    public InventoryManagerFrame() {
        itemManager = new ItemManager();
        poManager = new PoManager();
        inventorySystem = new InventoryManagerSystem(itemManager, poManager);
        
        setTitle("Inventory Manager System");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(152, 202, 232));
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(200, 224, 235));
        headerPanel.setPreferredSize(new Dimension(900, 50));
        
        backButton = new JButton("Back");
        backButton.setBackground(new Color(102, 102, 102));
        backButton.setForeground(Color.WHITE);
        backButton.setFont(new Font("Serif", Font.BOLD, 12));
        backButton.addActionListener(e -> {
            new impage().setVisible(true);
            dispose();
        });
        headerPanel.add(backButton, BorderLayout.WEST);
        
        JLabel headerLabel = new JLabel("OWSB Purchase Order Management System");
        headerLabel.setFont(new Font("Sitka Text", Font.BOLD, 24));
        headerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(headerLabel, BorderLayout.CENTER);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(152, 202, 232));
        
        JLabel titleLabel = new JLabel("INVENTORY MANAGEMENT:");
        titleLabel.setFont(new Font("Sitka Text", Font.BOLD | Font.ITALIC, 24));
        titleLabel.setForeground(new Color(0, 51, 255));
        titleLabel.setHorizontalAlignment(SwingConstants.LEFT);
        titlePanel.add(titleLabel, BorderLayout.WEST);
        
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setBackground(new Color(152, 202, 232));
        searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> searchItems());
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        titlePanel.add(searchPanel, BorderLayout.EAST);
        
        mainPanel.add(titlePanel, BorderLayout.CENTER);
        
        JPanel contentPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        contentPanel.setBackground(new Color(152, 202, 232));
        
        JPanel itemTablePanel = new JPanel(new BorderLayout());
        itemTablePanel.setBackground(new Color(217, 232, 239));
        itemTablePanel.setBorder(BorderFactory.createTitledBorder("Inventory Items"));
        
        String[] itemColumns = {"Item ID", "Name", "Category", "UOM", "Price", "Stock"};
        itemTableModel = new DefaultTableModel(itemColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };
        itemTable = new JTable(itemTableModel);
        JScrollPane itemScrollPane = new JScrollPane(itemTable);
        itemTablePanel.add(itemScrollPane, BorderLayout.CENTER);
        
        JPanel poTablePanel = new JPanel(new BorderLayout());
        poTablePanel.setBackground(new Color(217, 232, 239));
        poTablePanel.setBorder(BorderFactory.createTitledBorder("Pending Receipt Purchase Orders"));
        
        String[] poColumns = {"PO ID", "PR ID", "Supplier ID", "Item ID", "Quantity", "Status"};
        poTableModel = new DefaultTableModel(poColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        poTable = new JTable(poTableModel);
        JScrollPane poScrollPane = new JScrollPane(poTable);
        poTablePanel.add(poScrollPane, BorderLayout.CENTER);
        
        contentPanel.add(itemTablePanel);
        contentPanel.add(poTablePanel);
        
        JPanel actionPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        actionPanel.setBackground(new Color(152, 202, 232));
        
        JPanel updateStockPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        updateStockPanel.setBackground(new Color(217, 232, 239));
        updateStockPanel.setBorder(BorderFactory.createTitledBorder("Update Stock from PO"));
        
        JLabel poIdLabel = new JLabel("PO ID:");
        poIdField = new JTextField(10);
        JLabel quantityLabel = new JLabel("Received Quantity:");
        receivedQuantityField = new JTextField(5);
        updateStockButton = new JButton("Update Stock");
        updateStockButton.addActionListener(e -> updateStockFromPO());
        
        updateStockPanel.add(poIdLabel);
        updateStockPanel.add(poIdField);
        updateStockPanel.add(quantityLabel);
        updateStockPanel.add(receivedQuantityField);
        updateStockPanel.add(updateStockButton);
        
        JPanel reportPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        reportPanel.setBackground(new Color(217, 232, 239));
        reportPanel.setBorder(BorderFactory.createTitledBorder("Reports and Actions"));
        
        generateReportButton = new JButton("Generate Stock Report");
        generateReportButton.addActionListener(e -> generateStockReport());
        viewLowStockButton = new JButton("View Low Stock Items");
        viewLowStockButton.addActionListener(e -> viewLowStockItems());
        refreshButton = new JButton("Refresh Data");
        refreshButton.addActionListener(e -> refreshData());
        
        reportPanel.add(generateReportButton);
        reportPanel.add(viewLowStockButton);
        reportPanel.add(refreshButton);
        
        actionPanel.add(updateStockPanel);
        actionPanel.add(reportPanel);
        
        JPanel containerPanel = new JPanel(new BorderLayout());
        containerPanel.setBackground(new Color(152, 202, 232));
        containerPanel.add(contentPanel, BorderLayout.CENTER);
        containerPanel.add(actionPanel, BorderLayout.SOUTH);
        
        mainPanel.add(containerPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        refreshData();
    }

    private void searchItems() {
        String searchTerm = searchField.getText().trim().toLowerCase();
        if (searchTerm.isEmpty()) {
            refreshData(); 
            return;
        }
        
        itemTableModel.setRowCount(0);
        
        List<Item> items = itemManager.getAllItems();
        for (Item item : items) {
            if (item.getItemId().toLowerCase().contains(searchTerm) || 
                item.getItemName().toLowerCase().contains(searchTerm)) {
                Object[] row = {
                    item.getItemId(),
                    item.getItemName(),
                    item.getItemCategory(),
                    item.getItemUOM(),
                    item.getItemUnitPrice(),
                    item.getStockQuantity()
                };
                itemTableModel.addRow(row);
            }
        }
    }

    private void updateStockFromPO() {
        String poId = poIdField.getText().trim();
        String quantityStr = receivedQuantityField.getText().trim();
        
        if (poId.isEmpty() || quantityStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter PO ID and received quantity", 
                    "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            int receivedQuantity = Integer.parseInt(quantityStr);
            if (receivedQuantity <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be greater than zero", 
                        "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            boolean success = inventorySystem.updateStockFromPO(poId, receivedQuantity);
            if (success) {
                JOptionPane.showMessageDialog(this, "Stock updated successfully", 
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                poIdField.setText("");
                receivedQuantityField.setText("");
                refreshData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update stock. Check if PO exists and has valid status (SUBMITTED/APPROVED/ORDERED).", 
                        "Update Failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number for quantity", 
                    "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void generateStockReport() {
        boolean success = inventorySystem.generateStockReport();
        if (success) {
            JOptionPane.showMessageDialog(this, "Stock report generated successfully", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Failed to generate stock report", 
                    "Report Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewLowStockItems() {
        itemTableModel.setRowCount(0);
        
        List<Item> lowStockItems = inventorySystem.getLowStockItems();
        if (lowStockItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No items with low stock found", 
                    "Low Stock Items", JOptionPane.INFORMATION_MESSAGE);
            refreshData(); // Show all items again
            return;
        }
        
        for (Item item : lowStockItems) {
            Object[] row = {
                item.getItemId(),
                item.getItemName(),
                item.getItemCategory(),
                item.getItemUOM(),
                item.getItemUnitPrice(),
                item.getStockQuantity()
            };
            itemTableModel.addRow(row);
        }
    }

    private void refreshData() {
        itemTableModel.setRowCount(0);
        poTableModel.setRowCount(0);
        
        List<Item> items = inventorySystem.viewAllItems();
        for (Item item : items) {
            Object[] row = {
                item.getItemId(),
                item.getItemName(),
                item.getItemCategory(),
                item.getItemUOM(),
                item.getItemUnitPrice(),
                item.getStockQuantity()
            };
            itemTableModel.addRow(row);
        }
        
        List<po> pendingPOs = inventorySystem.getPendingReceiptPOs();
        for (po purchaseOrder : pendingPOs) {
            pr.PrItem prItem = purchaseOrder.getItem();
            Object[] row = {
                purchaseOrder.getPoId(),
                purchaseOrder.getPrId(),
                purchaseOrder.getSupplierId(),
                prItem != null ? prItem.getItem().getItemId() : "N/A",
                prItem != null ? prItem.getQuantity() : 0,
                purchaseOrder.getPoStatus()
            };
            poTableModel.addRow(row);
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new InventoryManagerFrame().setVisible(true);
        });
    }
}
