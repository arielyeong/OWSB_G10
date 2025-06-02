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


//DennisKoh

public class FinanceManagerSystem {
    private PoManager poManager;
    private PrManager prManager;
    private SupplierManager supplierManager;
    private ItemManager itemManager;
    private static final String FINANCIAL_REPORT_FILE = "financial_report.txt";
    private static final String PAYMENT_RECORD_FILE = "payment_record.txt";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    private Map<String, List<Payment>> supplierPayments;
    
    public FinanceManagerSystem(PoManager poManager, PrManager prManager, 
                              SupplierManager supplierManager, ItemManager itemManager) {
        this.poManager = poManager;
        this.prManager = prManager;
        this.supplierManager = supplierManager;
        this.itemManager = itemManager;
        this.supplierPayments = new HashMap<>();
    }

    public boolean approvePurchaseOrder(String poId, String financeManagerId) {
        po purchaseOrder = poManager.findPo(poId);
        if (purchaseOrder == null) {
            return false;
        }
        
        String currentStatus = purchaseOrder.getPoStatus();
        if (!"SUBMITTED".equals(currentStatus) && !"ORDERED".equals(currentStatus)) {
            return false; 
        }
        
        purchaseOrder.setPoStatus("APPROVED");
        LocalDate currentDate = LocalDate.now();
        purchaseOrder.setOrderDate(currentDate);
        
        if (purchaseOrder.getDeliveryDate() == null || 
            purchaseOrder.getDeliveryDate().isBefore(currentDate)) {
            purchaseOrder.setDeliveryDate(currentDate.plusDays(7)); // Set delivery 7 days from now
        }
        
        if (purchaseOrder.getInvoiceDate() != null && 
            purchaseOrder.getInvoiceDate().isBefore(currentDate)) {
            purchaseOrder.setInvoiceDate(null);
        }
        
        poManager.updatePo(purchaseOrder);
        
        return true;
    }

    public boolean receiveInventory(String poId) {
        po purchaseOrder = poManager.findPo(poId);
        if (purchaseOrder == null) {
            return false; 
        }
        
        String currentStatus = purchaseOrder.getPoStatus();
        if (!"APPROVED".equals(currentStatus) && !"ORDERED".equals(currentStatus)) {
            return false; 
        }
        
        purchaseOrder.setPoStatus("RECEIVED");
        LocalDate currentDate = LocalDate.now();
        
        if (purchaseOrder.getOrderDate() != null && currentDate.isBefore(purchaseOrder.getOrderDate())) {
            purchaseOrder.setDeliveryDate(purchaseOrder.getOrderDate());
        } else {
            purchaseOrder.setDeliveryDate(currentDate);
        }
        
        poManager.updatePo(purchaseOrder);
        
        return true;
    }
    
    public boolean verifyInventoryUpdate(String poId) {
        po purchaseOrder = poManager.findPo(poId);
        if (purchaseOrder == null) {
            return false; 
        }
        
        String currentStatus = purchaseOrder.getPoStatus();
        if ("APPROVED".equals(currentStatus) || "ORDERED".equals(currentStatus)) {
            if (!receiveInventory(poId)) {
                return false;
            }
            purchaseOrder = poManager.findPo(poId);
        }
        
        if (!purchaseOrder.getPoStatus().equals("RECEIVED")) {
            return false; 
        }
        
        purchaseOrder.setPoStatus("VERIFIED");
        
        poManager.updatePo(purchaseOrder);
        
        return true;
    }
    
    public boolean processPayment(String poId, double paymentAmount, String paymentMethod, String financeManagerId) {
        po purchaseOrder = poManager.findPo(poId);
        if (purchaseOrder == null || !purchaseOrder.getPoStatus().equals("VERIFIED")) {
            return false; 
        }
        
        String supplierId = purchaseOrder.getSupplierId();
        Supplier supplier = supplierManager.findSupplierById(supplierId);
        if (supplier == null) {
            return false; 
        }
        
        Payment payment = new Payment(
                poId,
                supplierId,
                paymentAmount,
                paymentMethod,
                LocalDate.now(),
                financeManagerId
        );
        
        if (!supplierPayments.containsKey(supplierId)) {
            supplierPayments.put(supplierId, new ArrayList<>());
        }
        supplierPayments.get(supplierId).add(payment);
        
        purchaseOrder.setPoStatus("PAID");
        purchaseOrder.setInvoiceDate(LocalDate.now());
        
        poManager.updatePo(purchaseOrder);
        
        recordPayment(payment);
        
        return true;
    }
    
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

    public List<po> getPendingApprovalPOs() {
        return poManager.getAllPo().stream()
                .filter(po -> po.getPoStatus().equals("SUBMITTED"))
                .collect(Collectors.toList());
    }
    
    public List<po> getPendingVerificationPOs() {
        return poManager.getAllPo().stream()
                .filter(po -> po.getPoStatus().equals("RECEIVED"))
                .collect(Collectors.toList());
    }

    public List<po> getPendingPaymentPOs() {
        return poManager.getAllPo().stream()
                .filter(po -> po.getPoStatus().equals("VERIFIED"))
                .collect(Collectors.toList());
    }

    public List<pr> getAllPurchaseRequisitions() {
        return prManager.getAllPr();
    }

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
