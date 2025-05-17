package usermanagement;

/**
 * InventoryManager class represents users with inventory management responsibilities.
 * This includes managing stock levels, updating inventory based on received items,
 * and generating inventory reports.
 */
public class InventoryManager extends User {
    private int lowStockThreshold;

    public InventoryManager(int lowStockThreshold, String userId, String username, String userPhone, 
                          String userEmail, String userAddress, String userPw, String userRole) {
        super(userId, username, userPhone, userEmail, userAddress, userPw, userRole);
        this.lowStockThreshold = lowStockThreshold;
    }

    public int getLowStockThreshold() {
        return lowStockThreshold;
    }

    public void setLowStockThreshold(int lowStockThreshold) {
        this.lowStockThreshold = lowStockThreshold;
    }
    
    @Override
    public String toString() {
        return username;
    }
} 