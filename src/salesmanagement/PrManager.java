package salesmanagement;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
//import java.io.PrintWriter;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.StandardOpenOption;
//import java.lang.reflect.Field;
//import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel;
import salesmanagement.pr.PrItem;

/**
 *
 * @author charlotte
 */
public class PrManager {
    private List<pr> prList = new ArrayList<>();
    public static final String FILE_PATH = "pr.txt";
    ItemManager iM = new ItemManager();

    public PrManager() {
        prList = new ArrayList<>();
        loadPr();
    }
    
    public void loadPr() {
        prList.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (!line.isEmpty()) {  // Skip empty lines
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
     
//    public static List<pr> loadPrFromFile() {
//        List<pr> prList = new ArrayList<>();
//        ItemManager im = new ItemManager();
//        im.loadItems(); // Load all items once
//        List<Item> allItems = im.getAllItems();
//        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
//            
//            List<PrItem> prItems = new ArrayList<>();
//            int totalCost = 0;
//
//            String line;
//            while ((line = reader.readLine()) != null) {
//                if (line.equals("-----")) {
//                    pr newPr = new pr(prId, smId, supplierId, prItems, prStatus, createdDate, requiredDate);
//                    prList.add(newPr);
//
//                    // Reset for next PR
//                    prId = smId = supplierId = prStatus = "";
//                    createdDate = requiredDate = null;
//                    totalCost = 0;
//                    prItems = new ArrayList<>();
//                } else {
//                    String[] parts = line.split("=", 2);
//                    if (parts.length == 2) {
//                        String key = parts[0].trim();
//                        String value = parts[1].trim();
//
//                        switch (key) {
//                            case "prId" -> prId = value;
//                            case "smId" -> smId = value;
//                            case "supplierId" -> supplierId = value;
//                            case "prStatus" -> prStatus = value;
//                            case "createdDate" -> createdDate = LocalDate.parse(value);
//                            case "requiredDate" -> requiredDate = LocalDate.parse(value);
//                            case "totalCost" -> totalCost = Integer.parseInt(value);
//
//                            case "items" -> {
//                                // Expected format: [I001:3,I002:1]
//                                value = value.replace("[", "").replace("]", "");
//                                String[] itemPairs = value.split(",");
//                                for (String pair : itemPairs) {
//                                    String[] itemData = pair.split(":");
//                                    if (itemData.length == 2) {
//                                        String itemId = itemData[0].trim();
//                                        int qty = Integer.parseInt(itemData[1].trim());
//
//                                        // Find matching item from allItems
//                                        for (Item item : allItems) {
//                                            if (item.getItemId().equals(itemId)) {
//                                                prItems.add(new PrItem(item, qty));
//                                                break;
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return prList;
//    }

//    public static DefaultTableModel PrListToModel(List<pr> prList) {
//        String[] columns = {"PR ID", "Staff ID", "Supplier ID", "Items", "Status", "Created Date", "Required Date"};
//        DefaultTableModel model = new DefaultTableModel(columns, 0);
//
//        for (pr p : prList) {
//            StringBuilder itemString = new StringBuilder();
//            for (PrItem prItem : p.getItems()) {
//                itemString.append("[item=")
//                          .append(prItem.getItem().getItemId())
//                          .append(", quantity=")
//                          .append(prItem.getQuantity())
//                          .append(", totalcost=")
//                          .append(String.format("%.2f", prItem.getItem().getItemUnitPrice() * prItem.getQuantity()))
//                          .append("]");
//            }
//
//            model.addRow(new Object[]{
//                p.getPrId(),
//                p.getSmId(),
//                p.getSupplierId(),
//                itemString.toString(),
//                p.getPrStatus(),
//                p.getCreatedDate().format(pr.DATE_FORMATTER),
//                p.getRequiredDate().format(pr.DATE_FORMATTER)
//            });
//        }
//
//        return model;
//    }


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

    public boolean deletePr(String prId) {
            pr pr = findPrById(prId);
            if (pr != null) {
                prList.remove(pr);
                savePr();
                return true;
            }
            return false;
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
                existPr.setItems(new ArrayList<>(updatedPr.getItems()));
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

    
//    public static void writeFile(Object obj) {
//    Class<?> classObj = obj.getClass();
//    String fileName = classObj.getSimpleName() + ".txt";
//    System.out.println("Filename: " + fileName);
//
//    try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(fileName, true)))) {
//        for (Field field : classObj.getDeclaredFields()) {
//            String fieldName = field.getName();
//            field.setAccessible(true);
//            Object value = field.get(obj);
//
//            // Special handling for List and LocalDate
//            if (value instanceof List) {
//                List<?> list = (List<?>) value;
//                pw.write(fieldName + "=" + list.toString() + "\n");
//            } else {
//                pw.write(fieldName + "=" + value + "\n");
//            }
//        }
//        pw.write("-----\n");
//    } catch (Exception e) {
//        e.printStackTrace();
//    }
//}

//    public static void savePrToFile(pr pr) {
//        try (BufferedWriter w = Files.newBufferedWriter(
//            Path.of(FILE_PATH),
//            StandardOpenOption.CREATE,
//            StandardOpenOption.APPEND)) {
//        
//            w.write(pr.toString());
//            w.newLine();
//            System.out.println("Data saved to: " + FILE_PATH);
//        
//        } catch (IOException e) {
//            System.err.println("Error saving to file: " + e.getMessage());
//        }
//    }
    
    

}
