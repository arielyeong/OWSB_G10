/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package usermanagement;

/**
 *
 * @author yingx
 */
public class InventoryManager extends User {
    private String approvalLimit;

    public InventoryManager(String userId, String username, String userPhone, String userEmail, String userAddress, String userPw, String userRole, String approvalLimit) {
        super(userId, username, userPhone, userEmail, userAddress, userPw, "Inventory Manager");
        this.approvalLimit = approvalLimit;
    }
    @Override
    public String toString(){
        return super.toString()+"|"+ approvalLimit;
    }
    @Override
    public boolean adduser(){
        return savetofile();
    }
    
    @Override
    public boolean deleteuser(String userId){
        return deleteuserfile(userId);
    }
    
    @Override
    public boolean edituser(){
        if(!deleteuserfile(this.userId)){
            return false;
        }
        return savetofile();
    }

    public String getApprovalLimit() {
        return approvalLimit;
    }

    public void setApprovalLimit(String approvalLimit) {
        this.approvalLimit = approvalLimit;
    }
}
