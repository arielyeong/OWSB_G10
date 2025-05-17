package salesmanagement;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
import javax.swing.table.DefaultTableModel;
import salesmanagement.po.PoItem;

public class FinanceManagerFrame extends JFrame {
    private PoManager poManager;
    private PrManager prManager;
    private String financeManagerId;
    
    private JTable poTable;
    private JTable prTable;
    private JComboBox<String> poStatusFilter;
    
    private static final String[] PO_COLUMNS = {"PO ID", "PR ID", "Supplier ID", "Total Cost", "Status", "Created Date"};
    private static final String[] PR_COLUMNS = {"PR ID", "SM ID", "Supplier ID", "Total Cost", "Status", "Created Date"};
    
    public FinanceManagerFrame(String financeManagerId) {
        this.financeManagerId = financeManagerId;
        this.poManager = new PoManager();
        this.prManager = new PrManager();
        
        initComponents();
        loadData();
    }
    
    private void initComponents() {
        setTitle("Finance Manager Dashboard");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Create tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Purchase Orders tab
        JPanel poPanel = createPurchaseOrdersPanel();
        tabbedPane.addTab("Purchase Orders", poPanel);
        
        // Purchase Requisitions tab
        JPanel prPanel = createPurchaseRequisitionsPanel();
        tabbedPane.addTab("Purchase Requisitions", prPanel);
        
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
        
        JButton approveButton = new JButton("Approve PO");
        approveButton.addActionListener(e -> approvePo());
        
        JButton rejectButton = new JButton("Reject PO");
        rejectButton.addActionListener(e -> rejectPo());
        
        JButton processPaymentButton = new JButton("Process Payment");
        processPaymentButton.addActionListener(e -> processPayment());
        
        buttonsPanel.add(refreshButton);
        buttonsPanel.add(viewDetailsButton);
        buttonsPanel.add(approveButton);
        buttonsPanel.add(rejectButton);
        buttonsPanel.add(processPaymentButton);
        
        panel.add(buttonsPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createPurchaseRequisitionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create table
        DefaultTableModel model = new DefaultTableModel(PR_COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        prTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(prTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Create buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadPurchaseRequisitionsData());
        
        JButton viewDetailsButton = new JButton("View Details");
        viewDetailsButton.addActionListener(e -> viewPrDetails());
        
        buttonsPanel.add(refreshButton);
        buttonsPanel.add(viewDetailsButton);
        
        panel.add(buttonsPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createReportsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("Generate Financial Reports");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setAlignmentX(CENTER_ALIGNMENT);
        panel.add(titleLabel);
        
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        JButton poReportButton = new JButton("Generate PO Financial Report");
        poReportButton.setAlignmentX(CENTER_ALIGNMENT);
        poReportButton.setMaximumSize(new Dimension(250, 30));
        poReportButton.addActionListener(e -> generatePoFinancialReport());
        panel.add(poReportButton);
        
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        JButton pendingPaymentsButton = new JButton("Generate Pending Payments Report");
        pendingPaymentsButton.setAlignmentX(CENTER_ALIGNMENT);
        pendingPaymentsButton.setMaximumSize(new Dimension(250, 30));
        pendingPaymentsButton.addActionListener(e -> generatePendingPaymentsReport());
        panel.add(pendingPaymentsButton);
        
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        JButton completedPaymentsButton = new JButton("Generate Completed Payments Report");
        completedPaymentsButton.setAlignmentX(CENTER_ALIGNMENT);
        completedPaymentsButton.setMaximumSize(new Dimension(250, 30));
        completedPaymentsButton.addActionListener(e -> generateCompletedPaymentsReport());
        panel.add(completedPaymentsButton);
        
        return panel;
    }
    
    private void loadData() {
        loadPurchaseOrdersData();
        loadPurchaseRequisitionsData();
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
    
    private void loadPurchaseRequisitionsData() {
        DefaultTableModel model = (DefaultTableModel) prTable.getModel();
        model.setRowCount(0);
        
        List<pr> prList = prManager.getAllPr();
        
        for (pr pr : prList) {
            model.addRow(new Object[]{
                pr.getPrId(),
                pr.getSmId(),
                pr.getSupplierId(),
                String.format("%.2f", pr.getTotalCost()),
                pr.getPrStatus(),
                pr.getCreatedDate().format(pr.DATE_FORMATTER)
            });
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
        
        if (po.getApprovalDate() != null) {
            details.append("Approval Date: ").append(po.getApprovalDate().format(po.DATE_FORMATTER)).append("\n");
        }
        
        if (po.getDeliveryDate() != null) {
            details.append("Delivery Date: ").append(po.getDeliveryDate().format(po.DATE_FORMATTER)).append("\n");
        }
        
        if (po.getPaymentDate() != null) {
            details.append("Payment Date: ").append(po.getPaymentDate().format(po.DATE_FORMATTER)).append("\n");
        }
        
        details.append("Inventory Updated: ").append(po.isInventoryUpdated() ? "Yes" : "No").append("\n");
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
    
    private void viewPrDetails() {
        int selectedRow = prTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a purchase requisition to view.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String prId = (String) prTable.getValueAt(selectedRow, 0);
        pr pr = prManager.findPrById(prId);
        
        if (pr == null) {
            JOptionPane.showMessageDialog(this, "Purchase requisition not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        StringBuilder details = new StringBuilder();
        details.append("PR ID: ").append(pr.getPrId()).append("\n");
        details.append("Sales Manager ID: ").append(pr.getSmId()).append("\n");
        details.append("Supplier ID: ").append(pr.getSupplierId()).append("\n");
        details.append("Status: ").append(pr.getPrStatus()).append("\n");
        details.append("Created Date: ").append(pr.getCreatedDate().format(pr.DATE_FORMATTER)).append("\n");
        details.append("Required Date: ").append(pr.getRequiredDate().format(pr.DATE_FORMATTER)).append("\n");
        details.append("Total Cost: $").append(String.format("%.2f", pr.getTotalCost())).append("\n\n");
        
        details.append("Items:\n");
        for (pr.PrItem item : pr.getItems()) {
            details.append("- ").append(item.getItem().getItemName())
                  .append(" (").append(item.getItem().getItemId()).append(")")
                  .append(": ").append(item.getQuantity())
                  .append(" x $").append(String.format("%.2f", item.getItem().getItemUnitPrice()))
                  .append(" = $").append(String.format("%.2f", item.getCost()))
                  .append("\n");
        }
        
        JOptionPane.showMessageDialog(this, details.toString(), "Purchase Requisition Details", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void approvePo() {
        int selectedRow = poTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a purchase order to approve.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String poId = (String) poTable.getValueAt(selectedRow, 0);
        po po = poManager.findPoById(poId);
        
        if (po == null) {
            JOptionPane.showMessageDialog(this, "Purchase order not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (!"Pending".equals(po.getPoStatus())) {
            JOptionPane.showMessageDialog(this, "Only pending purchase orders can be approved.", "Invalid Status", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to approve this purchase order?\n" +
                "PO ID: " + po.getPoId() + "\n" +
                "Supplier: " + po.getSupplierId() + "\n" +
                "Total Cost: $" + String.format("%.2f", po.getTotalCost()), 
                "Confirm Approval", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = poManager.approvePo(poId, financeManagerId);
            
            if (success) {
                JOptionPane.showMessageDialog(this, "Purchase order approved successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadPurchaseOrdersData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to approve purchase order.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void rejectPo() {
        int selectedRow = poTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a purchase order to reject.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String poId = (String) poTable.getValueAt(selectedRow, 0);
        po po = poManager.findPoById(poId);
        
        if (po == null) {
            JOptionPane.showMessageDialog(this, "Purchase order not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (!"Pending".equals(po.getPoStatus())) {
            JOptionPane.showMessageDialog(this, "Only pending purchase orders can be rejected.", "Invalid Status", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to reject this purchase order?\n" +
                "PO ID: " + po.getPoId() + "\n" +
                "Supplier: " + po.getSupplierId() + "\n" +
                "Total Cost: $" + String.format("%.2f", po.getTotalCost()), 
                "Confirm Rejection", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = poManager.rejectPo(poId, financeManagerId);
            
            if (success) {
                JOptionPane.showMessageDialog(this, "Purchase order rejected successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadPurchaseOrdersData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to reject purchase order.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void processPayment() {
        int selectedRow = poTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a purchase order to process payment.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String poId = (String) poTable.getValueAt(selectedRow, 0);
        po po = poManager.findPoById(poId);
        
        if (po == null) {
            JOptionPane.showMessageDialog(this, "Purchase order not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (!"Delivered".equals(po.getPoStatus())) {
            JOptionPane.showMessageDialog(this, "Only delivered purchase orders can be processed for payment.", "Invalid Status", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (!po.isInventoryUpdated()) {
            JOptionPane.showMessageDialog(this, "Inventory must be updated by Inventory Manager before processing payment.", "Inventory Not Updated", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to process payment for this purchase order?\n" +
                "PO ID: " + po.getPoId() + "\n" +
                "Supplier: " + po.getSupplierId() + "\n" +
                "Total Payment: $" + String.format("%.2f", po.getTotalCost()), 
                "Confirm Payment", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = poManager.processPayment(poId);
            
            if (success) {
                JOptionPane.showMessageDialog(this, "Payment processed successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadPurchaseOrdersData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to process payment.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void generatePoFinancialReport() {
        try {
            String fileName = "po_financial_report_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".txt";
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
                writer.write("===== PURCHASE ORDER FINANCIAL REPORT =====");
                writer.newLine();
                writer.write("Generated on: " + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                writer.newLine();
                writer.write("Generated by: Finance Manager (ID: " + financeManagerId + ")");
                writer.newLine();
                writer.newLine();
                
                writer.write(String.format("%-10s %-10s %-15s %-15s %-15s %-15s %-15s", 
                        "PO ID", "PR ID", "Supplier", "Status", "Created Date", "Payment Date", "Total Cost"));
                writer.newLine();
                writer.write(String.format("%-10s %-10s %-15s %-15s %-15s %-15s %-15s", 
                        "-----", "-----", "--------", "------", "------------", "------------", "----------"));
                writer.newLine();
                
                List<po> poList = poManager.getAllPo();
                double pendingAmount = 0;
                double paidAmount = 0;
                
                for (po po : poList) {
                    if ("Paid".equals(po.getPoStatus())) {
                        paidAmount += po.getTotalCost();
                    } else if ("Approved".equals(po.getPoStatus()) || "Delivered".equals(po.getPoStatus())) {
                        pendingAmount += po.getTotalCost();
                    }
                    
                    writer.write(String.format("%-10s %-10s %-15s %-15s %-15s %-15s $%-14.2f", 
                            po.getPoId(), 
                            po.getPrId(), 
                            po.getSupplierId(), 
                            po.getPoStatus(),
                            po.getCreatedDate().format(po.DATE_FORMATTER),
                            po.getPaymentDate() != null ? po.getPaymentDate().format(po.DATE_FORMATTER) : "N/A",
                            po.getTotalCost()));
                    writer.newLine();
                }
                
                writer.newLine();
                writer.write(String.format("Total Paid Amount: $%.2f", paidAmount));
                writer.newLine();
                writer.write(String.format("Total Pending Amount: $%.2f", pendingAmount));
                writer.newLine();
                writer.write(String.format("Total Financial Commitment: $%.2f", paidAmount + pendingAmount));
                writer.newLine();
                writer.write("=========================================");
            }
            
            JOptionPane.showMessageDialog(this, "PO Financial report generated successfully: " + fileName, "Report Generated", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error generating report: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void generatePendingPaymentsReport() {
        try {
            String fileName = "pending_payments_report_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".txt";
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
                writer.write("===== PENDING PAYMENTS REPORT =====");
                writer.newLine();
                writer.write("Generated on: " + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                writer.newLine();
                writer.write("Generated by: Finance Manager (ID: " + financeManagerId + ")");
                writer.newLine();
                writer.newLine();
                
                writer.write(String.format("%-10s %-15s %-15s %-15s %-15s", 
                        "PO ID", "Supplier", "Status", "Delivery Date", "Amount Due"));
                writer.newLine();
                writer.write(String.format("%-10s %-15s %-15s %-15s %-15s", 
                        "-----", "--------", "------", "-------------", "----------"));
                writer.newLine();
                
                List<po> poList = poManager.findPoByStatus("Delivered");
                double totalPendingAmount = 0;
                
                for (po po : poList) {
                    if (po.isInventoryUpdated()) {
                        totalPendingAmount += po.getTotalCost();
                        
                        writer.write(String.format("%-10s %-15s %-15s %-15s $%-14.2f", 
                                po.getPoId(), 
                                po.getSupplierId(), 
                                po.getPoStatus(),
                                po.getDeliveryDate().format(po.DATE_FORMATTER),
                                po.getTotalCost()));
                        writer.newLine();
                    }
                }
                
                writer.newLine();
                writer.write(String.format("Total Pending Payments: $%.2f", totalPendingAmount));
                writer.newLine();
                writer.write("================================");
            }
            
            JOptionPane.showMessageDialog(this, "Pending Payments report generated successfully: " + fileName, "Report Generated", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error generating report: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void generateCompletedPaymentsReport() {
        try {
            String fileName = "completed_payments_report_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".txt";
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
                writer.write("===== COMPLETED PAYMENTS REPORT =====");
                writer.newLine();
                writer.write("Generated on: " + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                writer.newLine();
                writer.write("Generated by: Finance Manager (ID: " + financeManagerId + ")");
                writer.newLine();
                writer.newLine();
                
                writer.write(String.format("%-10s %-15s %-15s %-15s %-15s", 
                        "PO ID", "Supplier", "Payment Date", "Delivery Date", "Amount Paid"));
                writer.newLine();
                writer.write(String.format("%-10s %-15s %-15s %-15s %-15s", 
                        "-----", "--------", "------------", "-------------", "----------"));
                writer.newLine();
                
                List<po> poList = poManager.findPoByStatus("Paid");
                double totalPaidAmount = 0;
                
                for (po po : poList) {
                    totalPaidAmount += po.getTotalCost();
                    
                    writer.write(String.format("%-10s %-15s %-15s %-15s $%-14.2f", 
                            po.getPoId(), 
                            po.getSupplierId(), 
                            po.getPaymentDate().format(po.DATE_FORMATTER),
                            po.getDeliveryDate().format(po.DATE_FORMATTER),
                            po.getTotalCost()));
                    writer.newLine();
                }
                
                writer.newLine();
                writer.write(String.format("Total Completed Payments: $%.2f", totalPaidAmount));
                writer.newLine();
                writer.write("==================================");
            }
            
            JOptionPane.showMessageDialog(this, "Completed Payments report generated successfully: " + fileName, "Report Generated", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error generating report: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public static void main(String[] args) {
        // For testing purposes
        new FinanceManagerFrame("FM001").setVisible(true);
    }
}