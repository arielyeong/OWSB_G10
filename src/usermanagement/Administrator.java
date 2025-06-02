/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package usermanagement;

import java .util.ArrayList;
import java.io.*;
import static usermanagement.User.deleteuserfile;

/**
 *
 * @author yingx
 */
public class Administrator extends User {//inheritance

    public Administrator(String userId, String username, String userPhone, String userEmail, String userAddress, String userPw, String userRole) {
        super(userId, username, userPhone, userEmail, userAddress, userPw, "Admin");//constructor chaining
        
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
