package salesmanagement;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import usermanagement.PurchaseManager;

/**
 *
 * @author charlotte
 */
public class PoManager extends PurchaseManager {
    
    //private List<po> poList;
    //private List<pr> prList;
    public static final String FILE_PATH = "po.txt";
    ItemManager iM = new ItemManager();
    //private PrManager prM;
    
    private final LocalDate currentDate = LocalDate.now();

    public PoManager() {
        //prList = new ArrayList<>();
        //poList = new ArrayList<>();
        super();
        loadPo();
    }
    
    public void setPrManager(PrManager prM) {
        this.prM = prM;
    }
    
    public void loadPo() {
        poList.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) { // Skip empty lines
                    po poObject = po.fromFileString(line, iM.getAllItems());
                    poList.add(poObject);
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading POs: " + e.getMessage());
        }
    }
    
    /*public List<po> getAllPo() {//
        return poList;
    }
    
    public po getPo(int index) {//
        if (index >= 0 && index < poList.size()) {
            return poList.get(index);
        }
        return null;
    }
    
    public po findPoById(String poId) {//
        for (po p : poList) {
            if (po.getPoId().equals(poId)) {
                return po;
            }
        }
        return null;
    }
    
    public po findPoByPrId(String prId){
        for(po p : poList){
            if (p.getPrId().trim().equals(prId.trim())){
                return p;
            }
        }
        return null;
    }
    
    public void savePo() {//
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (po po: poList) {
                writer.write(po.toFileString()); 
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving POs: " + e.getMessage());
        }
    }*/
    @Override
    public boolean deletePo(String poId) {
        po po = findPoById(poId);
        if (po != null) {
            poList.remove(po);
            savePo();
            return true;
        }
        return false;
    }
    @Override
    public boolean deletePoByPrId(String prId){
        po po = findPoByPrId(prId);
        if(po != null){
            poList.remove(po);
            savePo();
            return true;
        }
        return false;
    }
    
    @Override
    public boolean addPo(po po) {
        if (findPoById(po.getPoId()) != null) { // if duplicate PO ID found
            return false; 
        }
        poList.add(po);  
        savePo();        
        return true;
    }
    @Override
    public boolean updatePo(po updatedPo) {
        for (int i = 0; i < poList.size(); i++) {
            po existPo = poList.get(i);
            if (existPo.getPoId().equals(updatedPo.getPoId())) {
                existPo.setPrId(updatedPo.getPrId());
                existPo.setPmId(updatedPo.getPmId());
                existPo.setSmId(updatedPo.getSmId());
                existPo.setSupplierId(updatedPo.getSupplierId());
                existPo.setItem(updatedPo.getItem());
                existPo.setPoStatus(updatedPo.getPoStatus());
                existPo.setCreatedDate(updatedPo.getCreatedDate());
                existPo.setOrderDate(updatedPo.getOrderDate());
                existPo.setDeliveryDate(updatedPo.getDeliveryDate());
                existPo.setInvoiceDate(updatedPo.getInvoiceDate());
                savePo(); 
                return true;
            }
        }
        return false;
    }
    
    /*public String generateNewPoId() {
        int maxId = poList.stream()
            .map(p -> p.getPoId().replaceAll("\\D+", ""))
            .filter(s -> !s.isEmpty())
            .mapToInt(Integer::parseInt)
            .max()
            .orElse(0);
        return String.format("PO%03d", maxId + 1);
    }*
    
    public po createPoFromPr(pr approvedPr) {//
        List<pr> approvedPrs = prM.getApprovedPrs();
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
            null,
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
    }*/

}
