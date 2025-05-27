package salesmanagement;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 
 * 
 * @WeiKang
 */
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
    
    /**
     * Get a list of all items in the inventory
     * @return List of all items
     */
    public List<Item> viewAllItems() {
        return itemManager.getAllItems();
    }
    
    /**
     * Update stock quantity based on received items from approved Purchase Orders
     * @param poId Purchase Order ID
     * @param receivedQuantity Quantity received
     * @return true if update was successful, false otherwise
     */
    public boolean updateStockFromPO(String poId, int receivedQuantity) {
        po purchaseOrder = poManager.findPo(poId);
        if (purchaseOrder == null || !purchaseOrder.getPoStatus().equals("APPROVED")) {
            return false; // PO not found or not approved
        }
        
        pr.PrItem prItem = purchaseOrder.getItem();
        if (prItem == null) {
            return false; // No item in PO
        }
        
        Item item = itemManager.findItemById(prItem.getItem().getItemId());
        if (item == null) {
            return false; // Item not found in inventory
        }
        
        // Update stock quantity
        int currentStock = item.getStockQuantity();
        item.setStockQuantity(currentStock + receivedQuantity);
        
        // Update PO status to RECEIVED
        purchaseOrder.setPoStatus("RECEIVED");
        purchaseOrder.setDeliveryDate(LocalDate.now());
        
        // Save changes
        itemManager.updateItem(item);
        poManager.updatePo(purchaseOrder);
        
        return true;
    }
    
    /**
     * Get a list of items with stock levels below the threshold
     * @param threshold Stock level threshold
     * @return List of items with low stock
     */
    public List<Item> getLowStockItems(int threshold) {
        return itemManager.getAllItems().stream()
                .filter(item -> item.getStockQuantity() < threshold)
                .collect(Collectors.toList());
    }
    
    /**
     * Get a list of items with stock levels below the default threshold
     * @return List of items with low stock
     */
    public List<Item> getLowStockItems() {
        return getLowStockItems(LOW_STOCK_THRESHOLD);
    }
    
    /**
     * Generate a stock report and save it to a file
     * @return true if report was generated successfully, false otherwise
     */
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
    
    /**
     * Get a list of purchase orders that need to be verified for received items
     * @return List of purchase orders with status APPROVED
     */
    public List<po> getPendingReceiptPOs() {
        return poManager.getAllPo().stream()
                .filter(po -> po.getPoStatus().equals("APPROVED"))
                .collect(Collectors.toList());
    }
}