package salesmanagement;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author charlotte
 */
public class PrManager {
    private List<pr> prList;
    public static final String FILE_PATH = "pr.txt";
    ItemManager iM = new ItemManager();
    private PoManager poM;
    
    public PrManager() {
        prList = new ArrayList<>();
        loadPr();
    }
    
    public void setPoManager(PoManager poM) {
        this.poM = poM;
    }
    
    public void loadPr() {
        prList.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (!line.isEmpty()) {  
                pr prObject = pr.fromFileString(line, iM.getAllItems());
                prList.add(prObject);
            }
        }
    } catch (IOException e) {
        System.out.println("Error loading PRs: " + e.getMessage());
    }
}
    
    
    public List<pr> getAllPr() {
        return prList;
    }
    
    public pr getPr(int index) {
        if (index >= 0 && index < prList.size()) {
            return prList.get(index);
        }
        return null;
    }

    public pr findPrById(String prId) {
        for (pr pr : prList) {
            if (pr.getPrId().equals(prId)) {
                return pr;
            }
        }
        return null;
    }
    
    public void savePr() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (pr pr : prList) {
                writer.write(pr.toFileString()); 
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving PRs: " + e.getMessage());
        }
    }
    
    public void saveEditedPr(pr editedPr) {
        for (int i = 0; i < prList.size(); i++) {
            if (prList.get(i).getPrId().equals(editedPr.getPrId())) {
                prList.set(i, editedPr);
                break;
            }
        }
        savePr(); // save full list
    }


//    public boolean deletePr(String prId) {
//        pr pr = findPrById(prId);
//        if (pr != null) {
//            prList.remove(pr);
//            savePr();
//            return true;
//        }
//        return false;
//    }
    
    public boolean deletePr(String prId) {
        pr targetPr = findPrById(prId);
        if (targetPr == null) {
            return false;
        }
        poM.deletePoByPrId(prId);  
        // Remove PR
        prList.remove(targetPr);
        savePr();
        return true;
    }


    // Method to add a PR
    public boolean addPr(pr pr) {
        if (findPrById(pr.getPrId()) != null) { // if Duplicate PR ID found
            return false; 
        }
        prList.add(pr);  
        savePr();        
        return true;
    }

    public boolean updatePr(pr updatedPr) {
        for (int i = 0; i < prList.size(); i++) {
            pr existPr = prList.get(i);
            if (existPr.getPrId().equals(updatedPr.getPrId())) {
                existPr.setSmId(updatedPr.getSmId());
                existPr.setSupplierId(updatedPr.getSupplierId());
                existPr.setItem(updatedPr.getItem());
                existPr.setPrStatus(updatedPr.getPrStatus());
                existPr.setCreatedDate(updatedPr.getCreatedDate());
                existPr.setRequiredDate(updatedPr.getRequiredDate());
                savePr(); 
                return true;
            }
        }
        return false; // No matching PR found
    }

    public String generateNewPrId() {
        int maxId = prList.stream()
            .map(p -> p.getPrId().replaceAll("\\D+", ""))
            .filter(s -> !s.isEmpty())
            .mapToInt(Integer::parseInt)
            .max()
            .orElse(0);
        return String.format("PR%03d", maxId + 1);
    }
    
    public List<pr> getApprovedPrs() {
        Set<String> prInPo = poM.getAllPo().stream()
            .map(po::getPrId)
            .collect(Collectors.toSet());

        return prList.stream()
            .filter(p -> "APPROVED".equalsIgnoreCase(p.getPrStatus()))
            .filter(p -> !prInPo.contains(p.getPrId())) 
            .collect(Collectors.toList());
    }

}
