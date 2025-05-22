package usermanagement;

/**
 *
 * @author charlotte
 */
public class SalesManager extends User {
    private String salesRegion;

    public SalesManager(String userId, String username, String userPhone, String userEmail, String userAddress, String userPw, String userRole,String salesRegion) {
        super(userId, username, userPhone, userEmail, userAddress, userPw, userRole);
        this.salesRegion = salesRegion;
    }
    
    @Override
    public String toString(){
        return super.toString()+"|"+ salesRegion;
    }
    @Override
    public boolean adduser(){return false;}
    @Override
    public boolean deleteuser(String userId){return false;}
    @Override
    public boolean edituser(){return false;}
    
    public String getSalesRegion() {
        return salesRegion;
    }

    public void setSalesRegion(String salesRegion) {
        this.salesRegion = salesRegion;
    }
}
