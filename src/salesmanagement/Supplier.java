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
public class Supplier {
   // Private variables
    private String supplierId;
    private String supplierName;
    private String supplierPhone;
    private String supplierEmail;
    private String supplierAddress;
    private List<Item> items; // Many-to-many relationship

    // Constructor
    public Supplier(String supplierId, String supplierName, String supplierPhone, String supplierEmail, String supplierAddress, String string) {
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.supplierPhone = supplierPhone;
        this.supplierEmail = supplierEmail;
        this.supplierAddress = supplierAddress;
        this.items = new ArrayList<>();
    }

    // Getters and Setters
    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getSupplierPhone() {
        return supplierPhone;
    }

    public void setSupplierPhone(String supplierPhone) {
        this.supplierPhone = supplierPhone;
    }

    public String getSupplierEmail() {
        return supplierEmail;
    }

    public void setSupplierEmail(String supplierEmail) {
        this.supplierEmail = supplierEmail;
    }

    public String getSupplierAddress() {
        return supplierAddress;
    }

    public void setSupplierAddress(String supplierAddress) {
        this.supplierAddress = supplierAddress;
    }

    // Methods for many-to-many relationship
    public void addItem(Item item) {
        if (!items.contains(item)) {
            items.add(item);
            item.addSupplier(this); // Bidirectional relationship
        }
    }

    public void removeItem(Item item) {
        if (items.remove(item)) {
            item.removeSupplier(this); // Bidirectional relationship
        }
    }

    public List<Item> getItems() {
        return new ArrayList<>(items); // Return copy to preserve encapsulation
    }

    public int getTotalItems() {
        return items.size();
    }

    // File handling method
    public String toFileString() {
        return String.format("%s,%s,%s,%s,%s", 
                supplierId, supplierName, supplierPhone, supplierEmail, supplierAddress);
    }

    // Method to create Supplier from file string
    public static Supplier fromFileString(String fileString) {
        String[] parts = fileString.split(",");
        return new Supplier(parts[0], parts[1], parts[2], parts[3], parts[4], "");
    }


    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
    }
    
}
