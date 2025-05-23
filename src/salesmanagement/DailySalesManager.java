package salesmanagement;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import usermanagement.SalesManager;


public class DailySalesManager extends SalesManager {
    //private List<DailySales> salesList = new ArrayList<>();
    private final String SALES_FILE = "dailysales.txt";

    public DailySalesManager() {
        //salesList = new ArrayList<>();
        super();
        loadSalesFromFile();
    }
    
    /**
     *
     * @return
     */
    /*public List<DailySales> getAllSales() {//
        return salesList;
    }*/

    private void loadSalesFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(SALES_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                salesList.add(DailySales.fromFileString(line));
            }
        } catch (IOException e) {
            System.out.println("No existing daily sales file found, starting fresh.");
        }
    }

    /*private void saveSalesToFile() {//
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SALES_FILE))) {
            for (DailySales sale : salesList) {
                writer.write(sale.toFileString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving sales to file: " + e.getMessage());
        }
    }*/
    @Override
    public boolean addSales(DailySales sales) {
        for (DailySales existing : salesList) {
            //check duplicate sales id
            if (existing.getSalesId().equals(sales.getSalesId())) {
                return false;
            }
        }
        salesList.add(sales);
        saveSalesToFile();
        return true;
    }
    @Override
    public boolean updateSales(DailySales sales) {
        for (int i = 0; i < salesList.size(); i++) {
            if (salesList.get(i).getSalesId().equals(sales.getSalesId())) {
                salesList.set(i, sales);
                saveSalesToFile();
                return true;
            }
        }
        return false;
    }
    @Override
    public boolean deleteSales(String salesId) {
        boolean removed = salesList.removeIf(s -> s.getSalesId().equals(salesId));
        if (removed) {
            saveSalesToFile();
        }
        return removed;
    }

    /*public DailySales findSalesById(String salesId) {//
        for (DailySales sale : salesList) {
            if (sale.getSalesId().equals(salesId)) {
                return sale;
            }
        }
        return null;
    }*/

    @Override
    public boolean addItem(Item item){return false;}
    @Override
    public boolean updateItem(Item item){return false;}
    @Override
    public boolean deleteItem(String itemId){return false;}
    @Override
    public boolean addSupplier(salesmanagement.Supplier supplier){return false;}
    @Override
    public boolean deleteSupplier(String supplierId){return false;}
    @Override
    public boolean updateSupplier(Supplier updatedSupplier){return false;}
    @Override
    public void saveEditedPr(pr editedPr) {}
    @Override
    public boolean deletePr(String prId){return false;}
    @Override
    public boolean addPr(pr pr){return false;}
    @Override
    public boolean updatePr(pr updatedPr){return false;}
    
}