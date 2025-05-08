
package usermanagement;

/**
 *
 * @author charlotte
 */
public class User {
    protected String userId;      // Format: "SM001", "PM002", "ADMIN001" etc.
    protected String username;
    protected String userPhone;
    protected String userEmail;
    protected String userAddress;
    protected String userPw;      // In real implementation, store hashed password only
    protected String userRole;

    public User(String userId, String username, String userPhone, String userEmail, String userAddress, String userPw, String userRole) {
        this.userId = userId;
        this.username = username;
        this.userPhone = userPhone;
        this.userEmail = userEmail;
        this.userAddress = userAddress;
        this.userPw = userPw;
        this.userRole = userRole;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }

    public String getUserPw() {
        return userPw;
    }

    public void setUserPw(String userPw) {
        this.userPw = userPw;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }
    
    
}
