package usermanagement;

/**
 * FinanceManager 
 */
public class FinanceManager extends User {
    private double approvalLimit;
    private String paymentAuthority;

    public FinanceManager(double approvalLimit, String paymentAuthority, String userId, 
                        String username, String userPhone, String userEmail, 
                        String userAddress, String userPw, String userRole) {
        super(userId, username, userPhone, userEmail, userAddress, userPw, userRole);
        this.approvalLimit = approvalLimit;
        this.paymentAuthority = paymentAuthority;
    }

    public double getApprovalLimit() {
        return approvalLimit;
    }

    public void setApprovalLimit(double approvalLimit) {
        this.approvalLimit = approvalLimit;
    }

    public String getPaymentAuthority() {
        return paymentAuthority;
    }

    public void setPaymentAuthority(String paymentAuthority) {
        this.paymentAuthority = paymentAuthority;
    }
    
    @Override
    public String toString() {
        return username;
    }
} 