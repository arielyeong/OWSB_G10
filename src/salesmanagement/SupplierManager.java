package salesmanagement;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import usermanagement.SalesManager;

public class SupplierManager extends SalesManager {
    //private List<Supplier> suppliers;
    private static final String SUPPLIER_FILE = "supplier.txt";

    public SupplierManager() {
        //suppliers = new ArrayList<>();
        super();
        loadSuppliersFromFile();
    }

    // --- Core Operations ---

    /*public List<Supplier> getAllSuppliers() {//
        return new ArrayList<>(suppliers);
    }

    public void setSuppliers(List<Supplier> suppliers) {//
        this.suppliers = suppliers;
    }*/
    @Override
    public boolean addSupplier(Supplier supplier) {
        if (findSupplierById(supplier.getSupplierId()) != null) return false;
        suppliers.add(supplier);
        saveSuppliersToFile(); 
        return true;
    }
    @Override
    public boolean deleteSupplier(String supplierId) {
        Supplier supplier = findSupplierById(supplierId);
        if (supplier != null) {
            suppliers.remove(supplier);
            saveSuppliersToFile();
            return true;
        }
        return false;
    }
    @Override
    public boolean updateSupplier(Supplier updatedSupplier) {
        for (int i = 0; i < suppliers.size(); i++) {
            if (suppliers.get(i).getSupplierId().equals(updatedSupplier.getSupplierId())) {
                suppliers.set(i, updatedSupplier);
                saveSuppliersToFile();
                return true;
            }
        }
        return false;
    }

    /*public Supplier findSupplierById(String supplierId) {//
        for (Supplier supplier : suppliers) {
            if (supplier.getSupplierId().equalsIgnoreCase(supplierId)) {
                return supplier;
            }
        }
        return null;
    }

    public Supplier searchSupplier(String supplierId) {//
        return findSupplierById(supplierId);
    }*/

    // --- File Handling ---

    private void loadSuppliersFromFile() {
        suppliers.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(SUPPLIER_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Supplier supplier = Supplier.fromFileString(line);
                if (supplier != null) suppliers.add(supplier);
            }
        } catch (IOException e) {
            System.out.println("No existing supplier file found.");
        }
    }

    /*private void saveSuppliersToFile() {//
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SUPPLIER_FILE))) {
            for (Supplier supplier : suppliers) {
                writer.write(supplier.toFileString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving suppliers: " + e.getMessage());
        }
    }*/
    @Override
    public boolean addItem(Item item){return false;}
    @Override
    public boolean updateItem(Item item){return false;}
    @Override
    public boolean deleteItem(String itemId){return false;}
    @Override
    public boolean addSales(DailySales sales){return false;}
    @Override
    public boolean updateSales(DailySales sales){return false;}
    @Override
    public boolean deleteSales(String salesId){return false;}
    @Override
    public void saveEditedPr(pr editedPr) {}
    @Override
    public boolean deletePr(String prId){return false;}
    @Override
    public boolean addPr(pr pr){return false;}
    @Override
    public boolean updatePr(pr updatedPr){return false;}
}