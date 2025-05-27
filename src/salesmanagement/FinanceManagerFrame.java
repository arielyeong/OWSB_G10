package salesmanagement;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * UI Frame for Finance Manager 
 * @DennisKoh
 */
public class FinanceManagerFrame extends JFrame {
    private FinanceManagerSystem financeSystem;
    private PoManager poManager;
    private PrManager prManager;
    private SupplierManager supplierManager;
    private ItemManager itemManager;
    
    private JTable poTable;
    private JTable prTable;
    private DefaultTableModel poTableModel;
    private DefaultTableModel prTableModel;
    private JTextField searchField;
    private JTextField poIdField;
    private JTextField paymentAmountField;
    private JComboBox<String> paymentMethodCombo;
    private JTextField financeManagerIdField;
    private JButton approveButton;
    private JButton verifyButton;
    private JButton processPaymentButton;
    private JButton generateReportButton;
    private JButton refreshButton;
    private JButton backButton;
    
    public FinanceManagerFrame() {
        // Initialize managers and systems
        itemManager = new ItemManager();
        supplierManager = new SupplierManager();
        poManager = new PoManager();
        prManager = new PrManager();
        poManager.setPrManager(prManager);
        prManager.setPoManager(poManager);
        financeSystem = new FinanceManagerSystem(poManager, prManager, supplierManager, itemManager);
        
        // Set up the frame
        setTitle("Finance Manager System");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Create the main panel with a border layout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(152, 202, 232));
        
        // Create the header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(200, 224, 235));
        headerPanel.setPreferredSize(new Dimension(900, 50));
        
        // Add back button to header
        backButton = new JButton("Back");
        backButton.setBackground(new Color(102, 102, 102));
        backButton.setForeground(Color.WHITE);
        backButton.setFont(new Font("Serif", Font.BOLD, 12));
        backButton.addActionListener(e -> {
            new MainMenu().setVisible(true);
            dispose();
        });
        headerPanel.add(backButton, BorderLayout.WEST);
        
        // Add header label
        JLabel headerLabel = new JLabel("OWSB Purchase Order Management System");
        headerLabel.setFont(new Font("Sitka Text", Font.BOLD, 24));
        headerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(headerLabel, BorderLayout.CENTER);
        
        // Add header to main panel
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Create the title panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(152, 202, 232));
        
        // Add title label
        JLabel titleLabel = new JLabel("FINANCE MANAGEMENT:");
        titleLabel.setFont(new Font("Sitka Text", Font.BOLD | Font.ITALIC, 24));
        titleLabel.setForeground(new Color(0, 51, 255));
        titleLabel.setHorizontalAlignment(SwingConstants.LEFT);
        titlePanel.add(titleLabel, BorderLayout.WEST);
        
        // Add search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setBackground(new Color(152, 202, 232));
        searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> searchPOs());
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        titlePanel.add(searchPanel, BorderLayout.EAST);
        
        // Add title panel to main panel
        mainPanel.add(titlePanel, BorderLayout.CENTER);
        
        // Create the content panel
        JPanel contentPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        contentPanel.setBackground(new Color(152, 202, 232));
        
        // Create the PO table panel
        JPanel poTablePanel = new JPanel(new BorderLayout());
        poTablePanel.setBackground(new Color(217, 232, 239));
        poTablePanel.setBorder(BorderFactory.createTitledBorder("Purchase Orders"));
        
        // Create the PO table
        String[] poColumns = {"PO ID", "PR ID", "Supplier ID", "Item ID", "Quantity", "Status", "Created Date"};
        poTableModel = new DefaultTableModel(poColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };
        poTable = new JTable(poTableModel);
        JScrollPane poScrollPane = new JScrollPane(poTable);
        poTablePanel.add(poScrollPane, BorderLayout.CENTER);
        
        // Create the PR table panel
        JPanel prTablePanel = new JPanel(new BorderLayout());
        prTablePanel.setBackground(new Color(217, 232, 239));
        prTablePanel.setBorder(BorderFactory.createTitledBorder("Purchase Requisitions"));
        
        // Create the PR table
        String[] prColumns = {"PR ID", "SM ID", "Supplier ID", "Item ID", "Quantity", "Status", "Created Date"};
        prTableModel = new DefaultTableModel(prColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };
        prTable = new JTable(prTableModel);
        JScrollPane prScrollPane = new JScrollPane(prTable);
        prTablePanel.add(prScrollPane, BorderLayout.CENTER);
        
        // Add tables to content panel
        contentPanel.add(poTablePanel);
        contentPanel.add(prTablePanel);
        
        // Create the action panel
        JPanel actionPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        actionPanel.setBackground(new Color(152, 202, 232));
        
        // Create the approval panel
        JPanel approvalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        approvalPanel.setBackground(new Color(217, 232, 239));
        approvalPanel.setBorder(BorderFactory.createTitledBorder("PO Approval and Verification"));
        
        // Add components to approval panel
        JLabel poIdLabel = new JLabel("PO ID:");
        poIdField = new JTextField(10);
        JLabel financeManagerIdLabel = new JLabel("FM ID:");
        financeManagerIdField = new JTextField(10);
        approveButton = new JButton("Approve PO");
        approveButton.addActionListener(e -> approvePO());
        verifyButton = new JButton("Verify Inventory");
        verifyButton.addActionListener(e -> verifyInventory());
        
        approvalPanel.add(poIdLabel);
        approvalPanel.add(poIdField);
        approvalPanel.add(financeManagerIdLabel);
        approvalPanel.add(financeManagerIdField);
        approvalPanel.add(approveButton);
        approvalPanel.add(verifyButton);
        
        // Create the payment panel
        JPanel paymentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        paymentPanel.setBackground(new Color(217, 232, 239));
        paymentPanel.setBorder(BorderFactory.createTitledBorder("Payment Processing"));
        
        // Add components to payment panel
        JLabel paymentAmountLabel = new JLabel("Amount:");
        paymentAmountField = new JTextField(10);
        JLabel paymentMethodLabel = new JLabel("Method:");
        paymentMethodCombo = new JComboBox<>(new String[]{"BANK_TRANSFER", "CHECK", "CASH"});
        processPaymentButton = new JButton("Process Payment");
        processPaymentButton.addActionListener(e -> processPayment());
        generateReportButton = new JButton("Generate Financial Report");
        generateReportButton.addActionListener(e -> generateFinancialReport());
        refreshButton = new JButton("Refresh Data");
        refreshButton.addActionListener(e -> refreshData());
        
        paymentPanel.add(paymentAmountLabel);
        paymentPanel.add(paymentAmountField);
        paymentPanel.add(paymentMethodLabel);
        paymentPanel.add(paymentMethodCombo);
        paymentPanel.add(processPaymentButton);
        paymentPanel.add(generateReportButton);
        paymentPanel.add(refreshButton);
        
        // Add panels to action panel
        actionPanel.add(approvalPanel);
        actionPanel.add(paymentPanel);
        
        // Add content and action panels to a container panel
        JPanel containerPanel = new JPanel(new BorderLayout());
        containerPanel.setBackground(new Color(152, 202, 232));
        containerPanel.add(contentPanel, BorderLayout.CENTER);
        containerPanel.add(actionPanel, BorderLayout.SOUTH);
        
        // Add container panel to main panel
        mainPanel.add(containerPanel, BorderLayout.SOUTH);
        
        // Add main panel to frame
        add(mainPanel);
        
        // Load initial data
        refreshData();
    }
    
    /**
     * Search for POs by ID
     */
    private void searchPOs() {
        String searchTerm = searchField.getText().trim().toUpperCase();
        if (searchTerm.isEmpty()) {
            refreshData(); // If search is empty, show all POs
            return;
        }
        
        // Clear the table
        poTableModel.setRowCount(0);
        
        // Get all POs and filter
        List<po> allPOs = poManager.getAllPo();
        for (po purchaseOrder : allPOs) {
            if (purchaseOrder.getPoId().toUpperCase().contains(searchTerm) || 
                purchaseOrder.getPrId().toUpperCase().contains(searchTerm)) {
                pr.PrItem prItem = purchaseOrder.getItem();
                Object[] row = {
                    purchaseOrder.getPoId(),
                    purchaseOrder.getPrId(),
                    purchaseOrder.getSupplierId(),
                    prItem != null ? prItem.getItem().getItemId() : "N/A",
                    prItem != null ? prItem.getQuantity() : 0,
                    purchaseOrder.getPoStatus(),
                    purchaseOrder.getCreatedDate()
                };
                poTableModel.addRow(row);
            }
        }
    }
    
    /**
     * Approve a purchase order
     */
    private void approvePO() {
        String poId = poIdField.getText().trim();
        String financeManagerId = financeManagerIdField.getText().trim();
        
        if (poId.isEmpty() || financeManagerId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter PO ID and Finance Manager ID", 
                    "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        boolean success = financeSystem.approvePurchaseOrder(poId, financeManagerId);
        if (success) {
            JOptionPane.showMessageDialog(this, "Purchase Order approved successfully", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshData();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to approve Purchase Order. Check if PO exists and is in PENDING status.", 
                    "Approval Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Verify inventory update
     */
    private void verifyInventory() {
        String poId = poIdField.getText().trim();
        
        if (poId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter PO ID", 
                    "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        boolean success = financeSystem.verifyInventoryUpdate(poId);
        if (success) {
            JOptionPane.showMessageDialog(this, "Inventory update verified successfully", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshData();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to verify inventory update. Check if PO exists and is in RECEIVED status.", 
                    "Verification Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Process payment for a purchase order
     */
    private void processPayment() {
        String poId = poIdField.getText().trim();
        String financeManagerId = financeManagerIdField.getText().trim();
        String paymentAmountStr = paymentAmountField.getText().trim();
        String paymentMethod = (String) paymentMethodCombo.getSelectedItem();
        
        if (poId.isEmpty() || financeManagerId.isEmpty() || paymentAmountStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter PO ID, Finance Manager ID, and Payment Amount", 
                    "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            double paymentAmount = Double.parseDouble(paymentAmountStr);
            if (paymentAmount <= 0) {
                JOptionPane.showMessageDialog(this, "Payment amount must be greater than zero", 
                        "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Get the PO to find supplier details
            po purchaseOrder = poManager.findPo(poId);
            if (purchaseOrder == null) {
                JOptionPane.showMessageDialog(this, "Purchase Order not found", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // If payment method is bank transfer, show bank details
            if (paymentMethod.equals("BANK_TRANSFER")) {
                String supplierId = purchaseOrder.getSupplierId();
                Supplier supplier = supplierManager.findSupplierById(supplierId);
                if (supplier == null) {
                    JOptionPane.showMessageDialog(this, "Supplier not found", 
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Show bank details dialog
                JOptionPane.showMessageDialog(this,
                    "Bank Transfer Details:\n" +
                    "Bank Name: " + supplier.getSupplierBankName() + "\n" +
                    "Account Number: " + supplier.getSupplierAccNo(),
                    "Bank Transfer Information",
                    JOptionPane.INFORMATION_MESSAGE);
            }
            
            boolean success = financeSystem.processPayment(poId, paymentAmount, paymentMethod, financeManagerId);
            if (success) {
                JOptionPane.showMessageDialog(this, "Payment processed successfully", 
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                poIdField.setText("");
                paymentAmountField.setText("");
                refreshData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to process payment. Check if PO exists and is in VERIFIED status.", 
                        "Payment Failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number for payment amount", 
                    "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Generate a financial report
     */
    private void generateFinancialReport() {
        boolean success = financeSystem.generateFinancialReport();
        if (success) {
            JOptionPane.showMessageDialog(this, "Financial report generated successfully", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Failed to generate financial report", 
                    "Report Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Refresh all data in the tables
     */
    private void refreshData() {
        // Clear the tables
        poTableModel.setRowCount(0);
        prTableModel.setRowCount(0);
        
        // Load POs
        List<po> allPOs = poManager.getAllPo();
        for (po purchaseOrder : allPOs) {
            pr.PrItem prItem = purchaseOrder.getItem();
            Object[] row = {
                purchaseOrder.getPoId(),
                purchaseOrder.getPrId(),
                purchaseOrder.getSupplierId(),
                prItem != null ? prItem.getItem().getItemId() : "N/A",
                prItem != null ? prItem.getQuantity() : 0,
                purchaseOrder.getPoStatus(),
                purchaseOrder.getCreatedDate()
            };
            poTableModel.addRow(row);
        }
        
        // Load PRs
        List<pr> allPRs = prManager.getAllPr();
        for (pr purchaseRequisition : allPRs) {
            pr.PrItem prItem = purchaseRequisition.getItem();
            Object[] row = {
                purchaseRequisition.getPrId(),
                purchaseRequisition.getSmId(),
                purchaseRequisition.getSupplierId(),
                prItem != null ? prItem.getItem().getItemId() : "N/A",
                prItem != null ? prItem.getQuantity() : 0,
                purchaseRequisition.getPrStatus(),
                purchaseRequisition.getCreatedDate()
            };
            prTableModel.addRow(row);
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new FinanceManagerFrame().setVisible(true);
        });
    }
}