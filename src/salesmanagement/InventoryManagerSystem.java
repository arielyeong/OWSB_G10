package salesmanagement;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class InventoryManagerSystem {
    private ItemManager itemManager;
    private PoManager poManager;
    private static final int LOW_STOCK_THRESHOLD = 10; // Default threshold for low stock alerts
    private static final String STOCK_REPORT_FILE = "stock_report.txt";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    public InventoryManagerSystem(ItemManager itemManager, PoManager poManager) {
        this.itemManager = itemManager;
        this.poManager = poManager;
    }

    public List<Item> viewAllItems() {
        return itemManager.getAllItems();
    }

    public boolean updateStockFromPO(String poId, int receivedQuantity) {
        try {
            if (poId == null || poId.trim().isEmpty() || receivedQuantity <= 0) {
                System.out.println("Invalid input parameters: poId=" + poId + ", receivedQuantity=" + receivedQuantity);
                return false;
            }
            
            po purchaseOrder = poManager.findPo(poId.trim());
            if (purchaseOrder == null) {
                System.out.println("Purchase Order not found: " + poId);
                return false;
            }
            
            String currentStatus = purchaseOrder.getPoStatus();
            if (!"APPROVED".equalsIgnoreCase(currentStatus) && 
                !"SUBMITTED".equalsIgnoreCase(currentStatus) && 
                !"ORDERED".equalsIgnoreCase(currentStatus)) {
                System.out.println("Purchase Order cannot be received. Current status: " + currentStatus);
                return false;
            }
            
            pr.PrItem prItem = purchaseOrder.getItem();
            if (prItem == null) {
                System.out.println("No item found in Purchase Order: " + poId);
                return false;
            }
            
            Item itemFromPr = prItem.getItem();
            if (itemFromPr == null) {
                System.out.println("No item reference found in PrItem for PO: " + poId);
                return false;
            }
            
            Item item = itemManager.findItemById(itemFromPr.getItemId());
            if (item == null) {
                System.out.println("Item not found in inventory: " + itemFromPr.getItemId());
                return false;
            }
            
            int currentStock = item.getStockQuantity();
            int newStock = currentStock + receivedQuantity;
            item.setStockQuantity(newStock);
            
            System.out.println("Updating stock for item " + item.getItemId() + 
                             " from " + currentStock + " to " + newStock);
            
            purchaseOrder.setPoStatus("RECEIVED");
            LocalDate currentDate = LocalDate.now();
            
            if (purchaseOrder.getOrderDate() != null && currentDate.isBefore(purchaseOrder.getOrderDate())) {
                purchaseOrder.setDeliveryDate(purchaseOrder.getOrderDate());
            } else {
                purchaseOrder.setDeliveryDate(currentDate);
            }
            
            boolean itemUpdated = itemManager.updateItem(item);
            boolean poUpdated = poManager.updatePo(purchaseOrder);
            
            if (!itemUpdated) {
                System.out.println("Failed to update item in inventory");
                return false;
            }
            
            if (!poUpdated) {
                System.out.println("Failed to update purchase order");
                return false;
            }
            
            System.out.println("Successfully updated stock from PO: " + poId);
            return true;
            
        } catch (Exception e) {
            System.out.println("Error updating stock from PO: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<Item> getLowStockItems(int threshold) {
        return itemManager.getAllItems().stream()
                .filter(item -> item.getStockQuantity() < threshold)
                .collect(Collectors.toList());
    }

    public List<Item> getLowStockItems() {
        return getLowStockItems(LOW_STOCK_THRESHOLD);
    }

    public boolean generateStockReport() {
        List<Item> items = itemManager.getAllItems();
        LocalDate today = LocalDate.now();
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(STOCK_REPORT_FILE))) {
            writer.write("STOCK REPORT - " + today.format(DATE_FORMATTER));
            writer.newLine();
            writer.write("==================================================");
            writer.newLine();
            writer.write(String.format("%-10s | %-30s | %-15s | %-10s", "Item ID", "Item Name", "Category", "Stock"));
            writer.newLine();
            writer.write("-------------------------------------------------");
            writer.newLine();
            
            for (Item item : items) {
                writer.write(String.format("%-10s | %-30s | %-15s | %-10d", 
                        item.getItemId(), 
                        item.getItemName(), 
                        item.getItemCategory(), 
                        item.getStockQuantity()));
                writer.newLine();
            }
            
            writer.write("==================================================");
            writer.newLine();
            writer.write("Low Stock Items (Below " + LOW_STOCK_THRESHOLD + " units):");
            writer.newLine();
            
            List<Item> lowStockItems = getLowStockItems();
            if (lowStockItems.isEmpty()) {
                writer.write("No items below threshold.");
                writer.newLine();
            } else {
                for (Item item : lowStockItems) {
                    writer.write(String.format("%-10s | %-30s | %-15s | %-10d", 
                            item.getItemId(), 
                            item.getItemName(), 
                            item.getItemCategory(), 
                            item.getStockQuantity()));
                    writer.newLine();
                }
            }
            
            return true;
        } catch (IOException e) {
            System.out.println("Error generating stock report: " + e.getMessage());
            return false;
        }
    }

    public List<po> getPendingReceiptPOs() {
        return poManager.getAllPo().stream()
                .filter(po -> "APPROVED".equals(po.getPoStatus()) || 
                             "SUBMITTED".equals(po.getPoStatus()) || 
                             "ORDERED".equals(po.getPoStatus()))
                .collect(Collectors.toList());
    }
}
