package usermanagement;

/**
 *
 * @author charlotte
 */
public class PurchaseManager extends User {
    private int approvalLimit;

    public PurchaseManager(int approvalLimit, String userId, String username, String userPhone, String userEmail, String userAddress, String userPw, String userRole) {
        super(userId, username, userPhone, userEmail, userAddress, userPw, userRole);
        this.approvalLimit = approvalLimit;
    }

    
    public int getApprovalLimit() {
        return approvalLimit;
    }

    public void setApprovalLimit(int approvalLimit) {
        this.approvalLimit = approvalLimit;
    }
    
    
}
