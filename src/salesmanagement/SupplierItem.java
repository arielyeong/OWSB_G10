package salesmanagement;

import java.util.Objects;

public class SupplierItem {
    private String supplierId;
    private String itemId;

    // --- Constructor ---
    public SupplierItem(String supplierId, String itemId) {
        this.supplierId = supplierId;
        this.itemId = itemId;
    }

    // --- Getters ---
    public String getSupplierId() {
        return supplierId;
    }

    public String getItemId() {
        return itemId;
    }

    // --- File saving ---
    public String toFileString() {
        return supplierId + "," + itemId;
    }

    // --- File loading ---
    public static SupplierItem fromFileString(String line) {
        String[] parts = line.split(",");
        if (parts.length != 2) {
            return null; // Invalid line
        }
        return new SupplierItem(parts[0].trim(), parts[1].trim());
    }

    // --- Equals and hashCode to support use in Sets or Lists ---
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof SupplierItem)) return false;
        SupplierItem other = (SupplierItem) obj;
        return Objects.equals(supplierId, other.supplierId) && Objects.equals(itemId, other.itemId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(supplierId, itemId);
    }
}