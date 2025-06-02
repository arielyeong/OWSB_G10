
package usermanagement;
import java .util.*;
import java.io.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;



/**
 *
 * @author charlotte
 */
public abstract class User {
    protected String userId;      // Format: "SM001", "PM002", "ADMIN001" etc.
    protected String username;
    protected String userPhone;
    protected String userEmail;
    protected String userAddress;
    protected String userPw;      // In real implementation, store hashed password only
    protected String userRole;
    protected static final String user_file = "user.txt";

    public User(String userId, String username, String userPhone, String userEmail, String userAddress, String userPw, String userRole) {
        this.userId = userId;
        this.username = username;
        this.userPhone = userPhone;
        this.userEmail = userEmail;
        this.userAddress = userAddress;
        this.userPw = userPw;
        this.userRole = userRole;
    }
    //add all method
    public abstract boolean adduser();
    public abstract boolean deleteuser(String userId);
    public abstract boolean edituser();

    
    protected boolean savetofile(){
        try (BufferedWriter writer=new BufferedWriter(new FileWriter(user_file,true))){
            File file = new File(user_file);
            if(file.exists()&& file.length()>0){
                writer.newLine();
            }
            writer.write(this.toString());
            return true;
        }catch (IOException e){
            e.printStackTrace();
            return false;
        }
    }
    @Override
    public String toString(){//user
        String base = String.join("|", userId, username, userPhone, userEmail, userAddress, userPw, userRole);
        if(this instanceof SalesManagerUser){
            return base + "|"+((SalesManagerUser)this).getSalesRegion();
        }else if (this instanceof PurchaseManagerUser){
            return base + "|"+((PurchaseManagerUser)this).getApprovalLimit();
        }else if (this instanceof InventoryManager){
            return base + "|"+((InventoryManager)this).getApprovalLimit();
        }
        return base;
        //return String.join("|", userId, username, userPhone, userEmail, userAddress, userPw, userRole);
    }
    
    public static User finduserid(String userId){
        try (BufferedReader br = new BufferedReader(new FileReader(user_file))){
            String line;
            while((line =br.readLine())!=null){
                String[]parts = line.split("\\|");
                if(parts.length>=7 && parts[0].equals(userId)){
                    return createuserfromparts(parts);
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }
    //user
    static User createuserfromparts(String[] parts){
        String userRole = parts[6];
        switch (userRole){
            case "Admin":
                return new Administrator(parts[0],parts[1],parts[2],parts[3],parts[4],parts[5],parts[6]);
            case "Sales Manager":
                String region = parts.length>7 ? parts[7]:"";
                return new SalesManagerUser(parts[0],parts[1],parts[2],parts[3],parts[4],parts[5],parts[6],region);
            case "Purchase Manager":
                String approvelimit = parts.length>7 ? parts[7]:"";//Integer.parseInt(parts[7]): "0" ;
                return new PurchaseManagerUser(parts[0],parts[1],parts[2],parts[3],parts[4],parts[5],parts[6],approvelimit);
            case "Inventory Manager":
                String applimit = parts.length>7 ? parts[7]:"";
                return new InventoryManager(parts[0],parts[1],parts[2],parts[3],parts[4],parts[5],parts[6],applimit);
            case "Finance Manager":
                return new FinanceManager(parts[0],parts[1],parts[2],parts[3],parts[4],parts[5],parts[6]);
            default:
                return null;
        }
    }
    
    public static List<User> getalluser(){
        List<User> users = new ArrayList<>();
        try(BufferedReader br = new BufferedReader(new FileReader(user_file))){
            String line;
            while((line=br.readLine()) !=null){
                String[] parts = line.split("\\|");
                if(parts.length >=7){
                    User user = createuserfromparts(parts);
                    if (user !=null){
                        users.add(user);
                       
                    }
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return users;
    }
    
    protected static boolean deleteuserfile(String userId){
        List<User> users = getalluser();
        users.removeIf(user -> user.getUserId().equals(userId));
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(user_file))){
            for(int i=0;i < users.size();i++){
            if(i >0) 
                writer.newLine();
            writer.write(users.get(i).toString());
            }
            return true;
        }catch (IOException e){
            e.printStackTrace();
            return false;
        }
    }
    
    public String getUserId() {//encapulation
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
