/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package salesmanagement;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Yeong Huey Yee
 */
public class Item {
    // Private variables
    private String itemId;
    private String itemName;
    private String itemCategory;
    private String itemUOM;
    private int itemUnitPrice;
    private int stockQuantity;
    private List<Supplier> suppliers; // Many-to-many relationship

    // Constructor
    public Item(String itemId, String itemName, String itemCategory, String itemUOM, int itemUnitPrice, int stockQuantity, String string) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.itemCategory = itemCategory;
        this.itemUOM = itemUOM;
        this.itemUnitPrice = itemUnitPrice;
        this.stockQuantity = stockQuantity;
        this.suppliers = new ArrayList<>();
    }

    // Getters and Setters
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

    public int getItemUnitPrice() {
        return itemUnitPrice;
    }

    public void setItemUnitPrice(int itemUnitPrice) {
        this.itemUnitPrice = itemUnitPrice;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    // Methods for many-to-many relationship
    public void addSupplier(Supplier supplier) {
        if (!suppliers.contains(supplier)) {
            suppliers.add(supplier);
            supplier.addItem(this); // Bidirectional relationship
        }
    }

    public void removeSupplier(Supplier supplier) {
        if (suppliers.remove(supplier)) {
            supplier.removeItem(this); // Bidirectional relationship
        }
    }

    public List<Supplier> getSuppliers() {
        return new ArrayList<>(suppliers); // Return copy to preserve encapsulation
    }

    // File handling method
    public String toFileString() {
        return String.format("%s,%s,%s,%s,%d,%d", 
                itemId, itemName, itemCategory, itemUOM, itemUnitPrice, stockQuantity);
    }

    // Method to create Item from file string
    public static Item fromFileString(String fileString) {
        String[] parts = fileString.split(",");
        return new Item(parts[0], parts[1], parts[2], parts[3], 
                       Integer.parseInt(parts[4]), Integer.parseInt(parts[5]), "");
    }
    
    
    

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
    }
    
}
