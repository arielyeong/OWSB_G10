package salesmanagement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Item {
    private String itemId;
    private String itemName;
    private String itemCategory;
    private String itemUOM;
    private double itemUnitPrice;
    private int stockQuantity;
    private List<String> supplierIds;

    public Item(String itemId, String itemName, String itemCategory, String itemUOM, double itemUnitPrice, int stockQuantity, List<String> supplierIds) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.itemCategory = itemCategory;
        this.itemUOM = itemUOM;
        this.itemUnitPrice = itemUnitPrice;
        this.stockQuantity = stockQuantity;
        this.supplierIds = supplierIds == null ? new ArrayList<>() : supplierIds;
    }

    public Item(String itemId) {
        this.itemId = itemId;
        this.supplierIds = new ArrayList<>();
    }

    // --- Getters and Setters ---
    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemCategory() {
        return itemCategory;
    }

    public void setItemCategory(String itemCategory) {
        this.itemCategory = itemCategory;
    }

    public String getItemUOM() {
        return itemUOM;
    }

    public void setItemUOM(String itemUOM) {
        this.itemUOM = itemUOM;
    }

    public double getItemUnitPrice() {
        return itemUnitPrice;
    }

    public void setItemUnitPrice(double itemUnitPrice) {
        this.itemUnitPrice = itemUnitPrice;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public List<String> getSupplierIds() {
        return new ArrayList<>(supplierIds);
    }

    public void setSupplierIds(List<String> supplierIds) {
        this.supplierIds = supplierIds;
    }

    public void addSupplierId(String supplierId) {
        if (!supplierIds.contains(supplierId)) {
            supplierIds.add(supplierId);
        }
    }

    public void removeSupplierId(String supplierId) {
        supplierIds.remove(supplierId);
    }

    public String toFileString() {
        String suppliersJoined = String.join(";", supplierIds);
        return String.format("%s|%s|%s|%s|%.2f|%d|%s",
                itemId, itemName, itemCategory, itemUOM, itemUnitPrice, stockQuantity, suppliersJoined);
    }

    public static Item fromFileString(String fileString) {
        String[] parts = fileString.split("\\|");
        List<String> supplierList = new ArrayList<>();
        if (parts.length >= 7 && !parts[6].isEmpty()) {
            supplierList = Arrays.asList(parts[6].split(";"));
        }
        return new Item(parts[0], parts[1], parts[2], parts[3],
                Double.parseDouble(parts[4]), Integer.parseInt(parts[5]), supplierList);
    }

    public static void main(String[] args) {
        // Test purpose
    }
}