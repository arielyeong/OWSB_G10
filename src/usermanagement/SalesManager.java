package usermanagement;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import salesmanagement.DailySales;
import salesmanagement.Item;
import salesmanagement.PoManager;
import static salesmanagement.PrManager.FILE_PATH;
import salesmanagement.Supplier;
import salesmanagement.pr;

/**
 *
 * @author charlotte
 */
public abstract class SalesManager /*extends User*/ {
    /*private String salesRegion;*/
    protected List<Item> items;
    protected List<Supplier> suppliers;
    protected List<DailySales> salesList = new ArrayList<>();
    protected List<pr> prList;



    private static final String ITEM_FILE = "item.txt";
    private static final String SUPPLIER_FILE = "supplier.txt";
    private final String SALES_FILE = "dailysales.txt";
    public static final String FILE_PATH = "pr.txt";

    

    public abstract boolean addItem(Item item);
    public abstract boolean updateItem(Item item);
    public abstract boolean deleteItem(String itemId);
    public abstract boolean addSupplier(Supplier supplier) ;
    public abstract boolean deleteSupplier(String supplierId);
    public abstract boolean updateSupplier(Supplier updatedSupplier);
    public abstract boolean addSales(DailySales sales);
    public abstract boolean updateSales(DailySales sales);
    public abstract boolean deleteSales(String salesId);
    public abstract void saveEditedPr(pr editedPr);
    public abstract boolean deletePr(String prId) ;
    public abstract boolean addPr(pr pr);
    public abstract boolean updatePr(pr updatedPr);

    public SalesManager(/*String userId, String username, String userPhone, String userEmail, String userAddress, String userPw, String userRole,String salesRegion*/) {
        //super(userId, username, userPhone, userEmail, userAddress, userPw, userRole);
        //this.salesRegion = salesRegion;
        items = new ArrayList<>();
        suppliers = new ArrayList<>();
        salesList = new ArrayList<>();
        prList = new ArrayList<>();


        
    }
    //ItemManager
    protected void saveItems() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ITEM_FILE))) {
            for (Item item : items) {
                writer.write(item.toFileString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving items: " + e.getMessage());
        }
    }
    
    public Item findItemById(String itemId) {
        for (Item item : items) {
            if (item.getItemId().equals(itemId)) {
                return item;
            }
        }
        return null;
    }
    
    public Item findItemByName(String name){
        for (Item item : items){
            if (item.getItemName().equalsIgnoreCase(name)){
                return item;
            }
        }
        return null;
    }
    
    public List<Item> getAllItems() {
        return new ArrayList<>(items);
    }

    public List<Item> findItemsBySupplierId(String supplierId) {
        List<Item> result = new ArrayList<>();
        for (Item item : items) {
            if (item.getSupplierIds().contains(supplierId)) {
                result.add(item);
            }
        }
        return result;
    }
    
    //SupplierManager
    public List<Supplier> getAllSuppliers() {//
        return new ArrayList<>(suppliers);
    }

    public void setSuppliers(List<Supplier> suppliers) {//
        this.suppliers = suppliers;
    }
    
    public Supplier findSupplierById(String supplierId) {//
        for (Supplier supplier : suppliers) {
            if (supplier.getSupplierId().equalsIgnoreCase(supplierId)) {
                return supplier;
            }
        }
        return null;
    }

    public Supplier searchSupplier(String supplierId) {//
        return findSupplierById(supplierId);
    }
    
    protected void saveSuppliersToFile() {//
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SUPPLIER_FILE))) {
            for (Supplier supplier : suppliers) {
                writer.write(supplier.toFileString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving suppliers: " + e.getMessage());
        }
    }
    
    //DailySalesManager
    public List<DailySales> getAllSales() {//
        return salesList;
    }
    
    protected void saveSalesToFile() {//
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SALES_FILE))) {
            for (DailySales sale : salesList) {
                writer.write(sale.toFileString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving sales to file: " + e.getMessage());
        }
    }
    
    public DailySales findSalesById(String salesId) {//
        for (DailySales sale : salesList) {
            if (sale.getSalesId().equals(salesId)) {
                return sale;
            }
        }
        return null;
    }
    
    //pr
    public List<pr> getAllPr() {//
        return prList;
    }
    
    public pr getPr(int index) {//
        if (index >= 0 && index < prList.size()) {
            return prList.get(index);
        }
        return null;
    }

    public pr findPrById(String prId) {//
        for (pr pr : prList) {
            if (pr.getPrId().equals(prId)) {
                return pr;
            }
        }
        return null;
    }

    protected void savePr() {//
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (pr pr : prList) {
                writer.write(pr.toFileString()); 
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving PRs: " + e.getMessage());
        }
    }
    
    public String generateNewPrId() {//
        int maxId = prList.stream()
            .map(p -> p.getPrId().replaceAll("\\D+", ""))
            .filter(s -> !s.isEmpty())
            .mapToInt(Integer::parseInt)
            .max()
            .orElse(0);
        return String.format("PR%03d", maxId + 1);
    }
    
    
    /*@Override
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
    }*/
}
