package salesmanagement;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @DennisKoh
 */
public class FinanceManagerSystem {
    private PoManager poManager;
    private PrManager prManager;
    private SupplierManager supplierManager;
    private ItemManager itemManager;
    private static final String FINANCIAL_REPORT_FILE = "financial_report.txt";
    private static final String PAYMENT_RECORD_FILE = "payment_record.txt";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    // Map to track payments to suppliers
    private Map<String, List<Payment>> supplierPayments;
    
    public FinanceManagerSystem(PoManager poManager, PrManager prManager, 
                              SupplierManager supplierManager, ItemManager itemManager) {
        this.poManager = poManager;
        this.prManager = prManager;
        this.supplierManager = supplierManager;
        this.itemManager = itemManager;
        this.supplierPayments = new HashMap<>();
    }
    
    /**
     * Approve a purchase order
     * @param poId Purchase Order ID
     * @param financeManagerId Finance Manager ID who is approving
     * @return true if approval was successful, false otherwise
     */
    public boolean approvePurchaseOrder(String poId, String financeManagerId) {
        po purchaseOrder = poManager.findPo(poId);
        if (purchaseOrder == null || !purchaseOrder.getPoStatus().equals("PENDING")) {
            return false; // PO not found or not in PENDING status
        }
        
        // Update PO status to APPROVED
        purchaseOrder.setPoStatus("APPROVED");
        purchaseOrder.setOrderDate(LocalDate.now());
        
        // Save changes
        poManager.updatePo(purchaseOrder);
        
        return true;
    }
    
    /**
     * Verify inventory update from a received purchase order
     * @param poId Purchase Order ID
     * @return true if verification was successful, false otherwise
     */
    public boolean verifyInventoryUpdate(String poId) {
        po purchaseOrder = poManager.findPo(poId);
        if (purchaseOrder == null || !purchaseOrder.getPoStatus().equals("RECEIVED")) {
            return false; // PO not found or not in RECEIVED status
        }
        
        // Update PO status to VERIFIED
        purchaseOrder.setPoStatus("VERIFIED");
        
        // Save changes
        poManager.updatePo(purchaseOrder);
        
        return true;
    }
    
    /**
     * Process payment to a supplier for a verified purchase order
     * @param poId Purchase Order ID
     * @param paymentAmount Amount to be paid
     * @param paymentMethod Payment method (e.g., "BANK_TRANSFER", "CHECK")
     * @param financeManagerId Finance Manager ID who is processing the payment
     * @return true if payment was processed successfully, false otherwise
     */
    public boolean processPayment(String poId, double paymentAmount, String paymentMethod, String financeManagerId) {
        po purchaseOrder = poManager.findPo(poId);
        if (purchaseOrder == null || !purchaseOrder.getPoStatus().equals("VERIFIED")) {
            return false; // PO not found or not in VERIFIED status
        }
        
        String supplierId = purchaseOrder.getSupplierId();
        Supplier supplier = supplierManager.findSupplierById(supplierId);
        if (supplier == null) {
            return false; // Supplier not found
        }
        
        // Create payment record
        Payment payment = new Payment(
                poId,
                supplierId,
                paymentAmount,
                paymentMethod,
                LocalDate.now(),
                financeManagerId
        );
        
        // Add payment to supplier's payment records
        if (!supplierPayments.containsKey(supplierId)) {
            supplierPayments.put(supplierId, new ArrayList<>());
        }
        supplierPayments.get(supplierId).add(payment);
        
        // Update PO status to PAID
        purchaseOrder.setPoStatus("PAID");
        purchaseOrder.setInvoiceDate(LocalDate.now());
        
        // Save changes
        poManager.updatePo(purchaseOrder);
        
        // Record payment in file
        recordPayment(payment);
        
        return true;
    }
    
    /**
     * Record payment in the payment record file
     * @param payment Payment to record
     */
    private void recordPayment(Payment payment) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(PAYMENT_RECORD_FILE, true))) {
            writer.write(String.format("%s|%s|%.2f|%s|%s|%s",
                    payment.getPoId(),
                    payment.getSupplierId(),
                    payment.getAmount(),
                    payment.getPaymentMethod(),
                    payment.getPaymentDate().format(DATE_FORMATTER),
                    payment.getFinanceManagerId()));
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Error recording payment: " + e.getMessage());
        }
    }
    
    /**
     * Generate a financial report and save it to a file
     * @return true if report was generated successfully, false otherwise
     */
    public boolean generateFinancialReport() {
        List<po> paidPOs = poManager.getAllPo().stream()
                .filter(po -> po.getPoStatus().equals("PAID"))
                .collect(Collectors.toList());
        
        LocalDate today = LocalDate.now();
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FINANCIAL_REPORT_FILE))) {
            writer.write("FINANCIAL REPORT - " + today.format(DATE_FORMATTER));
            writer.newLine();
            writer.write("==================================================");
            writer.newLine();
            writer.write(String.format("%-10s | %-10s | %-15s | %-15s | %-10s", 
                    "PO ID", "PR ID", "Supplier", "Item", "Amount"));
            writer.newLine();
            writer.write("-------------------------------------------------");
            writer.newLine();
            
            double totalAmount = 0.0;
            
            for (po purchaseOrder : paidPOs) {
                String supplierId = purchaseOrder.getSupplierId();
                Supplier supplier = supplierManager.findSupplierById(supplierId);
                String supplierName = supplier != null ? supplier.getSupplierName() : "Unknown";
                
                pr.PrItem prItem = purchaseOrder.getItem();
                String itemName = prItem != null ? prItem.getItem().getItemName() : "Unknown";
                double amount = prItem != null ? prItem.getCost() : 0.0;
                
                writer.write(String.format("%-10s | %-10s | %-15s | %-15s | $%-9.2f", 
                        purchaseOrder.getPoId(), 
                        purchaseOrder.getPrId(), 
                        supplierName, 
                        itemName, 
                        amount));
                writer.newLine();
                
                totalAmount += amount;
            }
            
            writer.write("-------------------------------------------------");
            writer.newLine();
            writer.write(String.format("%-54s | $%-9.2f", "TOTAL", totalAmount));
            writer.newLine();
            writer.write("==================================================");
            writer.newLine();
            
            return true;
        } catch (IOException e) {
            System.out.println("Error generating financial report: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get a list of purchase orders that need financial approval
     * @return List of purchase orders with status PENDING
     */
    public List<po> getPendingApprovalPOs() {
        return poManager.getAllPo().stream()
                .filter(po -> po.getPoStatus().equals("PENDING"))
                .collect(Collectors.toList());
    }
    
    /**
     * Get a list of purchase orders that need inventory verification
     * @return List of purchase orders with status RECEIVED
     */
    public List<po> getPendingVerificationPOs() {
        return poManager.getAllPo().stream()
                .filter(po -> po.getPoStatus().equals("RECEIVED"))
                .collect(Collectors.toList());
    }
    
    /**
     * Get a list of purchase orders that need payment processing
     * @return List of purchase orders with status VERIFIED
     */
    public List<po> getPendingPaymentPOs() {
        return poManager.getAllPo().stream()
                .filter(po -> po.getPoStatus().equals("VERIFIED"))
                .collect(Collectors.toList());
    }
    
    /**
     * Get a list of purchase requisitions
     * @return List of all purchase requisitions
     */
    public List<pr> getAllPurchaseRequisitions() {
        return prManager.getAllPr();
    }
    
    /**
     * Inner class to represent a payment record
     */
    public static class Payment {
        private String poId;
        private String supplierId;
        private double amount;
        private String paymentMethod;
        private LocalDate paymentDate;
        private String financeManagerId;
        
        public Payment(String poId, String supplierId, double amount, String paymentMethod, 
                      LocalDate paymentDate, String financeManagerId) {
            this.poId = poId;
            this.supplierId = supplierId;
            this.amount = amount;
            this.paymentMethod = paymentMethod;
            this.paymentDate = paymentDate;
            this.financeManagerId = financeManagerId;
        }
        
        public String getPoId() {
            return poId;
        }
        
        public String getSupplierId() {
            return supplierId;
        }
        
        public double getAmount() {
            return amount;
        }
        
        public String getPaymentMethod() {
            return paymentMethod;
        }
        
        public LocalDate getPaymentDate() {
            return paymentDate;
        }
        
        public String getFinanceManagerId() {
            return financeManagerId;
        }
    }
}