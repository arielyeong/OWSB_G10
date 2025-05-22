package salesmanagement;

import java.util.ArrayList;
import java.util.List;

public class Supplier {
    private String supplierId;
    private String supplierName;
    private String supplierPhone;
    private String supplierEmail;
    private String supplierAddress;
    private String supplierBankName;
    private int supplierAccNo;
    private List<Item> items; // A supplier can supply many items

    // --- Constructor ---
    public Supplier(String supplierId, String supplierName, String supplierPhone, String supplierEmail, String supplierAddress, String supplierBankName, int supplierAccNo) {
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.supplierPhone = supplierPhone;
        this.supplierEmail = supplierEmail;
        this.supplierAddress = supplierAddress;
        this.supplierBankName = supplierBankName;
        this.supplierAccNo = supplierAccNo;
        this.items = new ArrayList<>();
    }
    
    

    // --- Getters and Setters ---
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

    public String getSupplierBankName() {
        return supplierBankName;
    }

    public void setSupplierBankName(String supplierBankName) {
        this.supplierBankName = supplierBankName;
    }

    public int getSupplierAccNo() {
        return supplierAccNo;
    }

    public void setSupplierAccNo(int supplierAccNo) {
        this.supplierAccNo = supplierAccNo;
    }
    
    

    public List<Item> getItems() {
        return items; // Allow direct manipulation for many-to-many mapping
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    // --- Methods to manage item relationship ---
    public void addItem(Item item) {
        if (!items.contains(item)) {
            items.add(item);
        }
    }

    public void removeItem(Item item) {
        items.remove(item);
    }

    public int getTotalItems() {
        return items.size();
    }

    // --- File saving (excluding item list for many-to-many) ---
    public String toFileString() {
        return supplierId + "|" + supplierName + "|" + supplierPhone + "|" + supplierEmail + "|" + supplierAddress + "|" + supplierBankName + "|" + supplierAccNo;
    }

    // --- File loading ---
    public static Supplier fromFileString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length < 7) {
            return null; 
        }
        return new Supplier(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], Integer.parseInt(parts[6]));
    }
}