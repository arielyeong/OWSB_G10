package salesmanagement;

import java.util.ArrayList;
import java.util.List;
import java.io.*;
import usermanagement.SalesManager;

public class ItemManager extends SalesManager {
    //private List<Item> items;
    private static final String ITEM_FILE = "item.txt";

    public ItemManager() {
        //items = new ArrayList<>();
        super();
        loadItems();
    }

    public void loadItems() {
        try (BufferedReader reader = new BufferedReader(new FileReader(ITEM_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Item item = Item.fromFileString(line);
                items.add(item);
            }
        } catch (IOException e) {
            System.out.println("No existing item file found. Starting fresh.");
        }
    }

    /*private void saveItems() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ITEM_FILE))) {
            for (Item item : items) {
                writer.write(item.toFileString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving items: " + e.getMessage());
        }
    }*/
    @Override
    public boolean addItem(Item item) {
        if (findItemById(item.getItemId()) != null) {
            return false; // Duplicate
        }
        items.add(item);
        saveItems();
        return true;
    }
    @Override
    public boolean updateItem(Item updatedItem) {
        for (int i = 0; i < items.size(); i++) {
            Item existingItem = items.get(i);
            if (existingItem.getItemId().equals(updatedItem.getItemId())) {
                // Update all fields
                existingItem.setItemName(updatedItem.getItemName());
                existingItem.setItemCategory(updatedItem.getItemCategory());
                existingItem.setItemUOM(updatedItem.getItemUOM());
                existingItem.setItemUnitPrice(updatedItem.getItemUnitPrice());
                existingItem.setStockQuantity(updatedItem.getStockQuantity());
                existingItem.setSupplierIds(updatedItem.getSupplierIds());
                saveItems();
                return true;
            }
        }
        return false;
    }
    @Override
    public boolean deleteItem(String itemId) {
        Item item = findItemById(itemId);
        if (item != null) {
            items.remove(item);
            saveItems();
            return true;
        }
        return false;
    }

    /*public Item findItemById(String itemId) {
        for (Item item : items) {
            if (item.getItemId().equals(itemId)) {
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
    }*/
    
    @Override
    public boolean addSupplier(salesmanagement.Supplier supplier){return false;}
    @Override
    public boolean deleteSupplier(String supplierId){return false;}
    @Override
    public boolean updateSupplier(Supplier updatedSupplier){return false;}
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