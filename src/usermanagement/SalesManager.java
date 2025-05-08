package usermanagement;

/**
 *
 * @author charlotte
 */
public class SalesManager extends User {
    private String salesRegion;

    public SalesManager(String salesRegion, String userId, String username, String userPhone, String userEmail, String userAddress, String userPw, String userRole) {
        super(userId, username, userPhone, userEmail, userAddress, userPw, userRole);
        this.salesRegion = salesRegion;
    }
    
    @Override
    public String toString() {
        return username;  
    }
}
