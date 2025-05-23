package usermanagement;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import salesmanagement.PoManager;
import salesmanagement.PrManager;
import salesmanagement.po;
import salesmanagement.pr;
import java.util.ArrayList;
import static salesmanagement.PoManager.FILE_PATH;


/**
 *
 * @author charlotte
 */
public abstract class PurchaseManager /*extends User*/ {//inheritance
    //private String approvalLimit;
    
    protected PrManager prM;
    protected List<po> poList;
    protected List<pr> prList;

    
    public static final String FILE_PATH = "po.txt";
    private final LocalDate currentDate = LocalDate.now();


    
    public abstract boolean deletePo(String poId);
    public abstract boolean addPo(po po);
    public abstract boolean updatePo(po updatedPo);


    public PurchaseManager(/*String userId, String username, String userPhone, String userEmail, String userAddress, String userPw, String userRole, String approvalLimit*/) {
        //super(userId, username, userPhone, userEmail, userAddress, userPw, "Purchase Manager");
        //this.approvalLimit = approvalLimit;
        prList = new ArrayList<>();
        poList = new ArrayList<>();
    }
    
    
    public void setPrManager(PrManager prM) {
        this.prM = prM;
    }
    
    public List<po> getAllPo() {//
        return poList;
    }
    
    public po getPo(int index) {//
        if (index >= 0 && index < poList.size()) {
            return poList.get(index);
        }
        return null;
    }
    
    public po findPoById(String poId) {//
        for (po po : poList) {
            if (po.getPoId().equals(poId)) {
                return po;
            }
        }
        return null;
    }
    
    protected void savePo() {//
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (po po: poList) {
                writer.write(po.toFileString()); 
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving POs: " + e.getMessage());
        }
    }
    
    public String generateNewPoId() {
        int maxId = poList.stream()
            .map(p -> p.getPoId().replaceAll("\\D+", ""))
            .filter(s -> !s.isEmpty())
            .mapToInt(Integer::parseInt)
            .max()
            .orElse(0);
        return String.format("PO%03d", maxId + 1);
    }
    
    public po createPoFromPr(pr approvedPr, String pmId) {
        List<pr> approvedPrs = prM.getApprovedPrs();
        //List<pr> approvedPrs = getApprovedPrs();
        boolean isValid = approvedPrs.stream()
            .anyMatch(p -> p.getPrId().equals(approvedPr.getPrId()));

        if (!isValid) {
            throw new IllegalArgumentException("This PR is either not approved or already used in a PO.");
        }

        String newPoId = generateNewPoId();
        pr.PrItem prItem = approvedPr.getItem();  
        po newPo = new po(
            newPoId,
            approvedPr.getPrId(),
            pmId,
            approvedPr.getSmId(),
            approvedPr.getSupplierId(),
            prItem, 
            "PENDING",
            currentDate,
            null,
            null,
            null
        );
        addPo(newPo);
        return newPo;
    }
    /*@Override
    public String toString(){
        return super.toString()+"|"+ approvalLimit;
    }
    @Override
    public boolean adduser(){return false;}
    @Override
    public boolean deleteuser(String userId){return false;}
    @Override
    public boolean edituser(){return false;}

    public String getApprovalLimit() {
        return approvalLimit;
    }

    public void setApprovalLimit(String approvalLimit) {
        this.approvalLimit = approvalLimit;
    }*/
    
}
