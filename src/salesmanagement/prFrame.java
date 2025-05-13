package salesmanagement;

import java.util.ArrayList;
import java.util.HashMap;
import java.time.LocalDate;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import salesmanagement.pr.PrItem;
import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import java.awt.EventQueue;
import java.awt.event.ActionListener;
import java.util.Map;

/**
 *
 * @author charlotte
 */
public class prFrame extends javax.swing.JFrame {
    List<PrItem> itemList = new ArrayList<PrItem>();
    List<PrItem> selectedItems = new ArrayList<PrItem>();
    HashMap<String, String> smMap = new HashMap<>();
    HashMap<String, String> supplierMap = new HashMap<>();
    HashMap<String, List<Item>> supplierItemsMap = new HashMap<>();
    
    LocalDate currentDate = LocalDate.now();
    
    private static final int COL_ITEM_ID = 0;
    private static final int COL_ITEM_NAME = 1;
    private static final int COL_UNIT_PRICE = 2;
    private static final int COL_QUANTITY = 3;
    private static final int COL_TOTAL = 4;

    
    private PrManager prM = new PrManager();
    private ItemManager iM = new ItemManager();
    private SupplierManager sM = new SupplierManager();
    
    public static final String PR_FILE = "pr.txt";
    
    private int count = 0;
    private boolean isUpdatingSupplier = false;
private boolean suppressSupplierAction = false;

    
    
    public prFrame() {
        initComponents();
        initializeItemsTable();
        refreshData();
        
        tCreatedDate.setText(currentDate.toString());
        tCreatedDate.setEditable(false);
        DatePickerSettings settings = tRequiredDate.getSettings();
        settings.setFormatForDatesCommonEra(pr.DATE_FORMATTER);
        setFormEditable(false);
    }
    
    // --- general methods 
    private void clearText() {
        tPrId.setText(prM.generateNewPrId());
        tSalesManager.setSelectedItem(null); 
        tSupplier.setSelectedItem(null);   
        tTotalCost.setText("0.00");
        tPrStatus.setText("");
        tCreatedDate.setText(currentDate.format(pr.DATE_FORMATTER));
        tRequiredDate.setDate(null);
        DatePickerSettings settings = tRequiredDate.getSettings();
        settings.setDateRangeLimits(currentDate, null);
        DefaultTableModel model = (DefaultTableModel) itemsTable.getModel();
        model.setRowCount(0);
        model.setColumnIdentifiers(new Object[]{"ID", "Name", "Unit Price", "Qty", "Total"});
        tSelectedItem.setText("");
        itemList.clear();
        selectedItems.clear();
    }
    
    public void refreshData() {
        prM.loadPr();
        iM.loadItems();
        loadSupplierItems(); 
        String previouslySelectedSupplier = (String)tSupplier.getSelectedItem();
        loadSuppliers();

        if (previouslySelectedSupplier != null) {
            isUpdatingSupplier = true;
            for (int i = 0; i < tSupplier.getItemCount(); i++) {
                if (tSupplier.getItemAt(i).equals(previouslySelectedSupplier)) {
                    tSupplier.setSelectedIndex(i);
                    break;
                }
            }
            isUpdatingSupplier = false;
        }

//        if (!prM.getAllPr().isEmpty()) {
//            displayPr(0);
//            System.out.println("display pr (from refresh)");
//        } else {
//            clearText();
//            updateItemsForSelectedSupplier();
//            System.out.println("update item (from refresh)");
//        }
        EventQueue.invokeLater(() -> {
                if (!prM.getAllPr().isEmpty()) {
                    displayPr(0);
                } else {
                    clearText();
                }
            });
        
    }
    
    private void setFormEditable(boolean edit) {
        tPrStatus.setEnabled(edit);
        tRequiredDate.setEnabled(edit);
        tPrId.setEnabled(edit);
        tSalesManager.setEnabled(edit);
        tSupplier.setEnabled(edit);  
        tTotalCost.setEnabled(edit);
        tPrStatus.setEnabled(edit);
        tCreatedDate.setEnabled(edit);
        tRequiredDate.setEnabled(edit);
        itemsTable.setEnabled(edit);
    }

    
    private void updateNavigationButtons() {
        prev.setEnabled(count > 0);
        next.setEnabled(count < prM.getAllPr().size() - 1);
        first.setEnabled(count > 0);
    }
    
    // --- PR RELATED METHODS 
    public void displayPr(int index) {
        if (index < 0 || index >= prM.getAllPr().size()) {
            clearText();
            return;
        }
        pr currentPr = prM.getPr(index);
        if (currentPr != null) {
            selectedItems.clear(); 
            count = index;
            tPrId.setText(currentPr.getPrId());
            tPrStatus.setText(currentPr.getPrStatus());
            if (currentPr.getCreatedDate() != null) {
                tCreatedDate.setText(currentPr.getCreatedDate().format(pr.DATE_FORMATTER));
            }
            tRequiredDate.setDate(currentPr.getRequiredDate());
            DatePickerSettings settings = tRequiredDate.getSettings();
            settings.setDateRangeLimits(null, null); // Remove limit

            if (currentPr.getSmId() != null) {
                tSalesManager.setSelectedItem(currentPr.getSmId());
            }
            if (currentPr.getSupplierId() != null) {
                updateSupplierSelection(currentPr.getSupplierId());
            }


            // Update items table
            DefaultTableModel model = (DefaultTableModel)itemsTable.getModel();
            model.setRowCount(0); 

            for (pr.PrItem prItem : currentPr.getItems()) {
                Item item = prItem.getItem();
                 System.out.println("Adding item: " + item.getItemId());
                model.addRow(new Object[]{
                    item.getItemId(),
                    item.getItemName(),
                    item.getItemUnitPrice(),
                    prItem.getQuantity(),
                    prItem.getCost()
                });
            }
            tTotalCost.setText(String.format("%.2f", currentPr.getTotalCost()));
            updateNavigationButtons();
            System.out.println("Table row count after filter: " + model.getRowCount());
        }
        System.out.println("display pr");
    }
    
    
    // --- ITEM TABLE RELATED METHODS
    private void initializeItemsTable() {
        DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Name", "Unit Price", "Qty", "Total"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3; // Only quantity column editable
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 2 || columnIndex == 4) return Double.class;
                if (columnIndex == 3) return Integer.class;
                return String.class;
            }
        };

        model.addTableModelListener(e -> {
            int row = e.getFirstRow();
            int column = e.getColumn();
            if (column == COL_QUANTITY) { // Only respond to changes in quantity
                try {
                    int qty = (int) model.getValueAt(row, COL_QUANTITY);
                    double price = (double) model.getValueAt(row, COL_UNIT_PRICE);
                    double cost = qty * price;
                    model.setValueAt(cost, row, COL_TOTAL); // Update total for that item

                    updateTotalCost(model); // Update overall total
                    updateSelectedItemsFromTable(model); // <--- Add this method
                } catch (Exception ex) {
                    System.out.println("Invalid input in quantity field.");
                }
            }
        });

        itemsTable.setModel(model);
        System.out.println("initialise table");
    }
    
    private void updateSupplierSelection(String supplierId) {
        String supplierText = getSupplierDropdownText(supplierId);
        if (supplierText != null && !supplierText.equals(tSupplier.getSelectedItem())) {
            // Remove action listener temporarily
            ActionListener[] listeners = tSupplier.getActionListeners();
            for (ActionListener listener : listeners) {
                tSupplier.removeActionListener(listener);
            }

            tSupplier.setSelectedItem(supplierText);

            // Re-add listeners
            for (ActionListener listener : listeners) {
                tSupplier.addActionListener(listener);
            }
            System.out.println("tSupplier updated: " + supplierText);
        }
    }
    
    private void updateSelectedItemsFromTable(DefaultTableModel model) {
        selectedItems.clear(); 
        for (int i = 0; i < model.getRowCount(); i++) {
            int qty = (int) model.getValueAt(i, COL_QUANTITY);
            if (qty > 0) {
                String itemId = model.getValueAt(i, COL_ITEM_ID).toString();
                String supplierKey = ((String) tSupplier.getSelectedItem()).split(" - ")[0];
                List<Item> supplierItems = supplierItemsMap.getOrDefault(supplierKey, new ArrayList<>());

                for (Item item : supplierItems) {
                    if (item.getItemId().equals(itemId)) {
                        selectedItems.add(new pr.PrItem(item, qty));
                        break;
                    }
                }
            }
        }
        System.out.println("update selected item from table");
        updateSelectedItemsDisplay(); 
    }
    
   
    private void updateTotalCost(DefaultTableModel model) {
        double total = 0.0;
        for (int i = 0; i < model.getRowCount(); i++) {
            Object val = model.getValueAt(i, COL_TOTAL);
            if (val instanceof Number) {
                total += ((Number) val).doubleValue();
            }
        }
        tTotalCost.setText(String.format("%.2f", total));
    }


    private void loadSuppliers() {
        tSupplier.removeAllItems(); 
        List<Supplier> allSuppliers = sM.getAllSuppliers();

        for (Supplier supplier : allSuppliers) {
            tSupplier.addItem(supplier.getSupplierId() + " - " + supplier.getSupplierName());
            supplierMap.put(supplier.getSupplierId(), supplier.getSupplierName());
        }
    }
    
     private void loadSupplierItems() {
        supplierItemsMap.clear();
        List<Item> allItems = iM.getAllItems();

        for (Item item : allItems) {
            String[] parts = item.toFileString().split("\\|");
            if (parts.length >= 7) {
                String supplierIds = parts[6]; 
                String[] suppliers = supplierIds.split(";");

                for (String supplierId : suppliers) {
                    supplierId = supplierId.trim();
                    if (!supplierId.equals("NONE") && sM.findSupplierById(supplierId) != null) {
                        List<Item> supplierList = supplierItemsMap.computeIfAbsent(supplierId, k -> new ArrayList<>());
                        // Check by item ID to avoid duplicates
                        boolean exists = supplierList.stream()
                            .anyMatch(i -> i.getItemId().equals(item.getItemId()));
                        if (!exists) {
                            supplierList.add(item);
                        }
                    }
                }
            }
        }
    }


     
     
    private String getSupplierDropdownText(String supplierId) {
        String name = supplierMap.get(supplierId);
        return name != null ? supplierId + " - " + name : null;
    }

     
     private void updateItemsForSelectedSupplier() {
        DefaultTableModel model = (DefaultTableModel)itemsTable.getModel();
        model.setRowCount(0);
        selectedItems.clear();
        updateSelectedItemsDisplay();
        String selected = (String)tSupplier.getSelectedItem();
        if (selected != null && !selected.isEmpty()) {
            String supplierId = selected.split(" - ")[0];
            List<Item> items = supplierItemsMap.getOrDefault(supplierId, new ArrayList<>());

            for (Item item : items) {
                 System.out.println("Adding item: " + item.getItemId() + " for supplier " + supplierId);
                model.addRow(new Object[]{
                    item.getItemId(),
                    item.getItemName(),
                    item.getItemUOM(),
                    item.getItemUnitPrice(),
                    0, // Default quantity
                    0.00 // Default total
                });
            }
        }
        System.out.println("update item for supplier");
    }
     
     private void filterItemsBySupplier(String supplierId) {
         if (isUpdatingSupplier) {
            return;
        }
        DefaultTableModel model = (DefaultTableModel)itemsTable.getModel();
        model.setRowCount(0);

        List<Item> supplierItems = supplierItemsMap.getOrDefault(supplierId, new ArrayList<>());

        for (Item item : supplierItems) {
            model.addRow(new Object[]{
                item.getItemId(),
                item.getItemName(),
                item.getItemUnitPrice(),
                0, // Default quantity
                0.00 // Default total cost
                
            });
        }
         System.out.println("tSupplier changed to: " + supplierId);
         System.out.println("Table row count after filter: " + model.getRowCount());
    }
     
     private void updateSelectedItemsDisplay() {
        StringBuilder sb = new StringBuilder();
        double total = 0;
        for (PrItem prItem : selectedItems) {
            sb.append(prItem.getItem().getItemName())
              .append(" (x").append(prItem.getQuantity()).append("), ");
            total += prItem.getCost();
        }
        if (!selectedItems.isEmpty()) {
            sb.setLength(sb.length() - 2); // Remove trailing comma
        }
        tSelectedItem.setText("Item selected: " + sb.toString());
        tTotalCost.setText(String.format("%.2f", total));
    }
     
//    private void onAddItemToPrClicked() {
//        int selectedRow = itemsTable.getSelectedRow();
//        if (selectedRow == -1) {
//            JOptionPane.showMessageDialog(this, "Please select an item.");
//            return;
//        }
//        DefaultTableModel model = (DefaultTableModel) itemsTable.getModel();
//        String itemId = model.getValueAt(selectedRow, COL_ITEM_ID).toString();
//        String itemName = model.getValueAt(selectedRow, COL_ITEM_NAME).toString();
//        double unitPrice = Double.parseDouble(model.getValueAt(selectedRow, COL_UNIT_PRICE).toString());
//        int quantity;
//        try {
//            quantity = Integer.parseInt(model.getValueAt(selectedRow, COL_QUANTITY).toString());
//            if (quantity <= 0) throw new NumberFormatException();
//        } catch (NumberFormatException e) {
//            JOptionPane.showMessageDialog(this, "Please enter a valid quantity.");
//            return;
//        }
//
//        String supplierKey = ((String) tSupplier.getSelectedItem()).split(" - ")[0];
//        List<Item> supplierItems = supplierItemsMap.get(supplierKey);
//        Item item = null;
//        for (Item i : supplierItems) {
//            if (i.getItemId().equals(itemId)) {
//                item = i;
//                break;
//            }
//        }
//        if (item == null) {
//            JOptionPane.showMessageDialog(this, "Item not found.");
//            return;
//        }
//
//        pr.PrItem prItem = new pr.PrItem(item, quantity);
//        selectedItems.add(prItem);
//
//        updateSelectedItemsDisplay();
//    }


     




    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        jFrame1 = new javax.swing.JFrame();
        jFrame2 = new javax.swing.JFrame();
        textSearch = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        save = new javax.swing.JButton();
        delete = new javax.swing.JButton();
        add = new javax.swing.JButton();
        clear = new javax.swing.JButton();
        edit = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        tPrStatus = new javax.swing.JTextField();
        tTotalCost = new javax.swing.JTextField();
        tCreatedDate = new javax.swing.JTextField();
        tSalesManager = new javax.swing.JComboBox<>();
        tSupplier = new javax.swing.JComboBox<>();
        tRequiredDate = new com.github.lgooddatepicker.components.DatePicker();
        tPrId = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        itemsTable = new javax.swing.JTable();
        jLabel25 = new javax.swing.JLabel();
        tSelectedItem = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        search = new javax.swing.JTextField();
        btnSearch = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        header = new javax.swing.JLabel();
        jPanel10 = new javax.swing.JPanel();
        prev = new javax.swing.JButton();
        first = new javax.swing.JButton();
        next = new javax.swing.JButton();
        last = new javax.swing.JButton();

        jMenuItem1.setText("jMenuItem1");

        jMenuItem2.setText("jMenuItem2");

        jMenuItem3.setText("jMenuItem3");

        javax.swing.GroupLayout jFrame1Layout = new javax.swing.GroupLayout(jFrame1.getContentPane());
        jFrame1.getContentPane().setLayout(jFrame1Layout);
        jFrame1Layout.setHorizontalGroup(
            jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        jFrame1Layout.setVerticalGroup(
            jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jFrame2Layout = new javax.swing.GroupLayout(jFrame2.getContentPane());
        jFrame2.getContentPane().setLayout(jFrame2Layout);
        jFrame2Layout.setHorizontalGroup(
            jFrame2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        jFrame2Layout.setVerticalGroup(
            jFrame2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        jPanel3.setBackground(new java.awt.Color(200, 224, 235));

        jLabel3.setText("OWSB Purchase Order Management System");
        jLabel3.setFont(new java.awt.Font("Sitka Text", 1, 24)); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel3)
                .addGap(204, 204, 204))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel3))
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel5.setBackground(new java.awt.Color(152, 202, 232));

        jLabel2.setText("Purchase Requisition Management");
        jLabel2.setFont(new java.awt.Font("Sitka Text", 1, 22)); // NOI18N

        jPanel2.setBackground(new java.awt.Color(200, 238, 249));

        save.setText("Save");
        save.setBackground(new java.awt.Color(255, 255, 255));
        save.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveActionPerformed(evt);
            }
        });

        delete.setText("Delete");
        delete.setBackground(new java.awt.Color(255, 255, 255));
        delete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteActionPerformed(evt);
            }
        });

        add.setText("Add");
        add.setBackground(new java.awt.Color(255, 255, 255));
        add.setBorderPainted(false);
        add.setFocusable(false);
        add.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addActionPerformed(evt);
            }
        });

        clear.setText("Clear");
        clear.setBackground(new java.awt.Color(255, 255, 255));
        clear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearActionPerformed(evt);
            }
        });

        edit.setText("Edit");
        edit.setBackground(new java.awt.Color(255, 255, 255));
        edit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(clear, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(add, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(delete, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(edit, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(save, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(add)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(edit)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(save)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(delete)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(clear)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel7.setBackground(new java.awt.Color(217, 232, 239));

        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel12.setText(" ~ PR Detail ~");
        jLabel12.setFont(new java.awt.Font("Segoe UI", 3, 16)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(0, 0, 204));

        jLabel21.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel21.setText("ID :");
        jLabel21.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel7.setText("Required date:");
        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel13.setText("Created date:");
        jLabel13.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        jLabel23.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel23.setText("Sales Manager:");
        jLabel23.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        jLabel24.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel24.setText("Items: ");
        jLabel24.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel10.setText("Status:");
        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel11.setText("Total cost:");
        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        tSalesManager.setForeground(new java.awt.Color(255, 255, 255));
        tSalesManager.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tSalesManagerActionPerformed(evt);
            }
        });

        tSupplier.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tSupplierActionPerformed(evt);
            }
        });

        tPrId.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tPrIdActionPerformed(evt);
            }
        });

        itemsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(itemsTable);

        jLabel25.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel25.setText("Supplier: ");
        jLabel25.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        tSelectedItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tSelectedItemActionPerformed(evt);
            }
        });

        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel14.setText("Selected Items:");
        jLabel14.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGap(13, 13, 13)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel7Layout.createSequentialGroup()
                                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(tPrId, javax.swing.GroupLayout.DEFAULT_SIZE, 181, Short.MAX_VALUE)
                                    .addComponent(tCreatedDate)))
                            .addGroup(jPanel7Layout.createSequentialGroup()
                                .addComponent(jLabel23)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(tSalesManager, 0, 181, Short.MAX_VALUE)))
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel25))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(tPrStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(tSupplier, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(tRequiredDate, javax.swing.GroupLayout.PREFERRED_SIZE, 219, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel7Layout.createSequentialGroup()
                                .addGap(72, 72, 72)
                                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addGroup(jPanel7Layout.createSequentialGroup()
                                        .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(tTotalCost, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel7Layout.createSequentialGroup()
                                        .addComponent(jLabel24)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 545, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGroup(jPanel7Layout.createSequentialGroup()
                                .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(tSelectedItem)))
                        .addContainerGap(35, Short.MAX_VALUE))))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(244, 244, 244))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(tPrStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tRequiredDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(29, 29, 29)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tSupplier, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel25, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tPrId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel21))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tCreatedDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(29, 29, 29)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(tSalesManager, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(19, 19, 19)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel24, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tSelectedItem, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel14))
                .addGap(18, 18, 18)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(tTotalCost, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8, 8, 8))
        );

        search.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchActionPerformed(evt);
            }
        });

        btnSearch.setText("Search");
        btnSearch.setBackground(new java.awt.Color(255, 255, 255));
        btnSearch.setToolTipText("");
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchActionPerformed(evt);
            }
        });

        jPanel4.setBackground(new java.awt.Color(200, 224, 235));

        header.setText("OWSB Purchase Order Management System");
        header.setFont(new java.awt.Font("Sitka Text", 1, 24)); // NOI18N
        header.setToolTipText("");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(header, javax.swing.GroupLayout.PREFERRED_SIZE, 599, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(119, 119, 119))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(header)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel10.setBackground(new java.awt.Color(204, 204, 255));

        prev.setText("Prev");
        prev.setBackground(new java.awt.Color(255, 255, 255));
        prev.setBorder(null);
        prev.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prevActionPerformed(evt);
            }
        });

        first.setText("First");
        first.setBackground(new java.awt.Color(255, 255, 255));
        first.setBorder(null);
        first.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                firstActionPerformed(evt);
            }
        });

        next.setText("Next");
        next.setBackground(new java.awt.Color(255, 255, 255));
        next.setBorder(null);
        next.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextActionPerformed(evt);
            }
        });

        last.setText("Last");
        last.setBackground(new java.awt.Color(255, 255, 255));
        last.setBorder(null);
        last.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lastActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(first, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(prev, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(15, 15, 15)
                .addComponent(next, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(last, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(prev, javax.swing.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE)
                    .addComponent(first, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(last, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(next, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(91, 91, 91)
                        .addComponent(search, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnSearch)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(0, 38, Short.MAX_VALUE)
                        .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(26, 26, 26)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(20, 20, 20))))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(search)
                    .addComponent(btnSearch))
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(86, 86, 86)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(29, 29, 29)
                .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(21, 21, 21))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void addActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addActionPerformed
        try {
            String prId = tPrId.getText();
            String selectedManager = (String) tSalesManager.getSelectedItem();
            String selectedSupplier = (String) tSupplier.getSelectedItem();
            if (selectedSupplier == null || !selectedSupplier.contains(" - ")) {
                JOptionPane.showMessageDialog(this, "Please select a supplier.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String supplierId = selectedSupplier.split(" - ")[0];

            String smId = smMap.get(selectedManager);
            LocalDate reqDate = tRequiredDate.getDate();
            updateSelectedItemsFromTable((DefaultTableModel) itemsTable.getModel());
            // Calculate total cost from itemList
            double totalCost = selectedItems.stream().mapToDouble(PrItem::getCost).sum();

            pr newPr = new pr(
                prId,
                smId,
                supplierId,
                new ArrayList<>(selectedItems),
                tPrStatus.getText().isEmpty() ? "DRAFT" : tPrStatus.getText(),
                currentDate,
                reqDate
            );
            if (prM.addPr(newPr)) {
            JOptionPane.showMessageDialog(this, "PR added successfully!");
            clearText();
            selectedItems.clear();
            updateSelectedItemsDisplay();
        } 
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Error creating PR: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
    }//GEN-LAST:event_addActionPerformed

    private void searchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchActionPerformed
        
    }//GEN-LAST:event_searchActionPerformed

    private void tSalesManagerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tSalesManagerActionPerformed
        for (String smName : smMap.keySet()) {
            tSalesManager.addItem(smName);
        }
    }//GEN-LAST:event_tSalesManagerActionPerformed

    private void tSupplierActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tSupplierActionPerformed
        if (suppressSupplierAction || isUpdatingSupplier) return;

        String selected = (String) tSupplier.getSelectedItem();
        if (selected != null) {
            String supplierId = selected.split(" - ")[0];
            filterItemsBySupplier(supplierId);
        }
        
    }//GEN-LAST:event_tSupplierActionPerformed

    private void clearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearActionPerformed
        clearText();
    }//GEN-LAST:event_clearActionPerformed

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        String searchId = search.getText().toUpperCase();
        if (searchId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a PR ID to search");
            return;
        }
        List<pr> allPrs = prM.getAllPr();
        boolean found = false;

        for (int i = 0; i < allPrs.size(); i++) {
            pr currentPr = allPrs.get(i);
            if (currentPr.getPrId().toUpperCase().equals(searchId)) {
                // Found matching PR - display it
                displayPr(i);
                count = i; // Update current index for navigation
                updateNavigationButtons();
                found = true;
                break;
            }
        }

        if (!found) {
            JOptionPane.showMessageDialog(this, 
                "PR with ID " + search + " not found",
                "Not Found",
                JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_btnSearchActionPerformed

    private void tPrIdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tPrIdActionPerformed
        int selectedRow = itemsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item from the table.");
            return;
        }

        DefaultTableModel model = (DefaultTableModel) itemsTable.getModel();

        String itemId = model.getValueAt(selectedRow, 0).toString();
        int quantity;

        try {
            quantity = Integer.parseInt(model.getValueAt(selectedRow, 3).toString());
            if (quantity <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid quantity.");
            return;
        }

        // Get item from supplierItemsMap
        String supplierKey = ((String) tSupplier.getSelectedItem()).split(" - ")[0];
        List<Item> supplierItems = supplierItemsMap.getOrDefault(supplierKey, new ArrayList<>());

        Item item = null;
        for (Item i : supplierItems) {
            if (i.getItemId().equals(itemId)) {
                item = i;
                break;
            }
        }

        if (item == null) {
            JOptionPane.showMessageDialog(this, "Item not found.");
            return;
        }

        // Check if already added
        for (pr.PrItem existing : selectedItems) {
            if (existing.getItem().getItemId().equals(itemId)) {
                JOptionPane.showMessageDialog(this, "Item already added to PR.");
                return;
            }
        }

        pr.PrItem prItem = new pr.PrItem(item, quantity);
        selectedItems.add(prItem);

        updateSelectedItemsDisplay();
    }//GEN-LAST:event_tPrIdActionPerformed

    private void prevActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prevActionPerformed
        if (count > 0) {
            displayPr(--count);
        }
    }//GEN-LAST:event_prevActionPerformed

    private void firstActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_firstActionPerformed
        count = 0;
        displayPr(count);
    }//GEN-LAST:event_firstActionPerformed

    private void nextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextActionPerformed
        if (count < prM.getAllPr().size() - 1) {
            displayPr(++count);
        }
    }//GEN-LAST:event_nextActionPerformed

    private void lastActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lastActionPerformed
        count = prM.getAllPr().size() - 1;
        displayPr(count);
    }//GEN-LAST:event_lastActionPerformed

    private void tSelectedItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tSelectedItemActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tSelectedItemActionPerformed

    private void saveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveActionPerformed
        try {
            String prId = tPrId.getText();
            
//            String selectedSupplier = (String) tSupplier.getSelectedItem();
//            String supplierId = selectedSupplier.split(" - ")[0];
            
            Object selectedSupplier = tSupplier.getSelectedItem();
            if (selectedSupplier == null) {
                JOptionPane.showMessageDialog(this, "Please select a supplier before editing items.", "No Supplier Selected", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String supplierId = selectedSupplier.toString().split(" - ")[0];

            String selectedManager = (String) tSalesManager.getSelectedItem();
            String smId = smMap.get(selectedManager);
            LocalDate reqDate = tRequiredDate.getDate();

            double totalCost = selectedItems.stream().mapToDouble(PrItem::getCost).sum();
            pr updatedPr = new pr(
                prId,
                smId,
                supplierId,
                new ArrayList<>(selectedItems),
                tPrStatus.getText().isEmpty() ? "DRAFT" : tPrStatus.getText(),
                currentDate,
                reqDate
            );
            prM.updatePr(updatedPr);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error saving PR: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_saveActionPerformed

    private void deleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteActionPerformed
        try {
            String prId = tPrId.getText().trim();
            if (prId.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a PR ID to delete.", "Missing ID", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete PR with ID: " + prId + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                boolean deleted = prM.deletePr(prId);
                if (deleted) {
                    JOptionPane.showMessageDialog(this, "PR deleted successfully.");
                    clearText(); 
                } else {
                    JOptionPane.showMessageDialog(this, "PR not found.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error delete PR: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_deleteActionPerformed

    private void editActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editActionPerformed
        setFormEditable(true);
    }//GEN-LAST:event_editActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(prFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(prFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(prFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(prFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new prFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton add;
    private javax.swing.JButton btnSearch;
    private javax.swing.JButton clear;
    private javax.swing.JButton delete;
    private javax.swing.JButton edit;
    private javax.swing.JButton first;
    private javax.swing.JLabel header;
    private javax.swing.JTable itemsTable;
    private javax.swing.JFrame jFrame1;
    private javax.swing.JFrame jFrame2;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton last;
    private javax.swing.JButton next;
    private javax.swing.JButton prev;
    private javax.swing.JButton save;
    private javax.swing.JTextField search;
    private javax.swing.JTextField tCreatedDate;
    private javax.swing.JTextField tPrId;
    private javax.swing.JTextField tPrStatus;
    private com.github.lgooddatepicker.components.DatePicker tRequiredDate;
    private javax.swing.JComboBox<String> tSalesManager;
    private javax.swing.JTextField tSelectedItem;
    private javax.swing.JComboBox<String> tSupplier;
    private javax.swing.JTextField tTotalCost;
    private javax.swing.JTextField textSearch;
    // End of variables declaration//GEN-END:variables
}
