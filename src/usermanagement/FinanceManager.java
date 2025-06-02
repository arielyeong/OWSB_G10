/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package usermanagement;

/**
 *
 * @author yingx
 */
public class FinanceManager extends User {
     public FinanceManager(String userId, String username, String userPhone, String userEmail, String userAddress, String userPw, String userRole) {
        super(userId, username, userPhone, userEmail, userAddress, userPw, "Finance Manager");//constructor chaining
        
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

}
