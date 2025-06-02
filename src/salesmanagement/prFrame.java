package salesmanagement;

import com.github.lgooddatepicker.components.DatePickerSettings;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.ListModel;
import javax.swing.table.DefaultTableModel;
import salesmanagement.pr.PrItem;
import usermanagement.User;
import usermanagement.SalesManager;
import usermanagement.SalesManagerUser;
import usermanagement.pmpage;
import usermanagement.smpage;

/**
 *
 * @author charlotte
 */
public class prFrame extends javax.swing.JFrame {
    HashMap<String, String> smMap = new HashMap<>();
    HashMap<String, String> supplierMap = new HashMap<>();
    HashMap<String, List<Item>> supplierItemsMap = new HashMap<>();
    
    LocalDate currentDate = LocalDate.now();
    
    PrManager prM = new PrManager();
    PoManager poM = new PoManager();
    private ItemManager iM = new ItemManager();
    private SupplierManager sM = new SupplierManager();
    
    public static final String PR_FILE = "pr.txt";
    public final String USER_LOGIN = "loginUser.txt";
    public static final String[] SM_STATUS = {"SUBMITTED"};
    public static final String[] PM_STATUS = {"SUBMITTED","APPROVED","REJECTED"};
    private String userId = poM.getLoginUserId();
    private String userRole = poM.getUserRoleFromId(userId);

    
    public prFrame() {
        initComponents();
        setLocationRelativeTo(null);
        prM.setPoManager(poM);
        supplierList.setModel(new DefaultListModel<>());
        tCreatedDate.setText(currentDate.toString());
        
        supplierList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateUnitPriceBasedOnSupplier();
            }
        });
        
        loadItemsToComboBox();
        setFormEditable(false);
        tItemUnitPrice.setEditable(false);
        loadTable();
        setupQuantityFieldListener();
        jPrTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = jPrTable.getSelectedRow();
                if (selectedRow >= 0) {
                    displayPr(selectedRow);
                }
            }
        });
        displayPr(0); 
    }
    
    // --- GENERAL METHODS 
    private void clearText() {
        tPrId.setText(prM.generateNewPrId());
        tSalesManager.setSelectedItem(null); 
        supplierList.clearSelection();
        tItems.setSelectedItem(null);
        tItemQuantity.setText("");
        tItemUnitPrice.setText("");
        tTotalCost.setText("");
        tPrStatus.setSelectedItem(null);
        tCreatedDate.setText(currentDate.format(pr.DATE_FORMATTER));
        tRequiredDate.setDate(null);
        reqDateRange(null);
        DefaultListModel<String> model = (DefaultListModel<String>) supplierList.getModel();
        model.clear();
        updateStatusComboBox(userRole);
    }

    
    private void setFormEditable(boolean edit) {
        tCreatedDate.setEnabled(edit);
        tPrId.setEnabled(edit);
    }
    
    //false=disable button
    private void setButton(boolean edit) {
        add.setEnabled(edit);
//        save.setEnabled(edit);
        delete.setEnabled(edit);
    }
    
    private void reqDateRange(LocalDate minDate) {
        DatePickerSettings settings = tRequiredDate.getSettings();
        settings.setFormatForDatesCommonEra(pr.DATE_FORMATTER);

        if (minDate != null) {
            settings.setDateRangeLimits(minDate, null);
        } else {
            settings.setDateRangeLimits(currentDate, null);
        }
    }
    
    // --- PR RELATED METHODS 
    public void displayPr(int index) {
        if (index < 0 || index >= prM.getAllPr().size()) {
            clearText(); 
            return;
        }
        
        prM.loadPr();          
        iM.loadItems();         
        loadAllSuppliers();
        loadSalesManagers();             
        loadSalesManagersToComboBox();

        pr currentPr = prM.getPr(index);
        if (currentPr == null) return;
        
        tPrId.setText(currentPr.getPrId());
        
        if (currentPr.getSmId() != null) {
            updateSalesManagerSelection(currentPr.getSmId());
        }

        updateStatusComboBox(userRole);
        String currentStatus = currentPr.getPrStatus();
        DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) tPrStatus.getModel();
        boolean found = false;
        for (int i = 0; i < model.getSize(); i++) {
            if (model.getElementAt(i).equalsIgnoreCase(currentStatus)) {
                found = true;
                break;
            }
        }
        if (!found && currentStatus != null) {
            model.addElement(currentStatus);
        }
        tPrStatus.setSelectedItem(currentStatus);


        if (currentPr.getCreatedDate() != null) {
            tCreatedDate.setText(currentPr.getCreatedDate().format(pr.DATE_FORMATTER));
            reqDateRange(currentPr.getCreatedDate()); 
        } else {
            tCreatedDate.setText(currentDate.format(pr.DATE_FORMATTER));
            reqDateRange(null); 
        }


        if (currentPr.getRequiredDate() != null) {
            tRequiredDate.setDate(currentPr.getRequiredDate());
        }

        pr.PrItem prItem = currentPr.getItem();
        if (prItem != null) {
            Item item = prItem.getItem();
            if (item != null) {
                tItems.setSelectedItem(item.getItemId() + " - " + item.getItemName());
                tItemQuantity.setText(String.valueOf(prItem.getQuantity()));
                tItemUnitPrice.setText(String.format("%.2f", item.getItemUnitPrice()));
                tTotalCost.setText(String.format("%.2f", prItem.getCost()));
            }
        } else {
            tItems.setSelectedItem(null);
            tItemQuantity.setText("");
            tItemUnitPrice.setText("");
            tTotalCost.setText("");
        }
        if (currentPr.getSupplierId() != null) {
            updateSupplierSelection(currentPr.getSupplierId());
        }
    }

    
    private void loadTable() {
        String[] columnNames = {
            "PR ID", "Sales Manager ID", "Supplier ID",
            "Item ID", "Quantity", "Unit Cost",
            "Status", "Created Date", "Required Date"
        };

        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };
        jPrTable.setModel(model); 
        prM.loadPr();

        for (pr p : prM.getAllPr()) {
            pr.PrItem item = p.getItem();
            if (item != null && item.getItem() != null) {
                model.addRow(new Object[]{
                    p.getPrId(),
                    p.getSmId(),
                    p.getSupplierId(),
                    item.getItem().getItemId(),
                    item.getQuantity(),
                    String.format("%.2f", item.getCost()),
                    p.getPrStatus(),
                    p.getCreatedDate().format(pr.DATE_FORMATTER),
                    p.getRequiredDate().format(pr.DATE_FORMATTER)
                });
            }
        }
    }

    
    private void loadSalesManagers() {
        smMap.clear(); 
        List<User> users = User.getalluser();
        for (User user : users) {
            if (user instanceof SalesManagerUser) {
                smMap.put(user.getUserId(), user.getUsername());
            }
        }
    }
    
    private void loadSalesManagersToComboBox() {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        tSalesManager.removeAllItems();

        for (Map.Entry<String, String> entry : smMap.entrySet()) {
            String displayText = entry.getKey() + " - " + entry.getValue();
            model.addElement(displayText);
        }

        tSalesManager.setModel(model);
    }
    
    private void updateSalesManagerSelection(String smId) {
        String currentSelection = (String)tSalesManager.getSelectedItem();

        if (smId == null || smId.isEmpty()) {
            if (currentSelection != null) {  
                tSalesManager.setSelectedItem(null);
            }
            return;
        }
        String newSelection = null;
        ComboBoxModel<String> model = tSalesManager.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            String element = model.getElementAt(i);
            if (element.startsWith(smId + " -")) {
                newSelection = element;
                break;
            }
        }
        if (newSelection != null && !newSelection.equals(currentSelection)) {
            tSalesManager.setSelectedItem(newSelection);
        } else if (newSelection == null && currentSelection != null) {
            tSalesManager.setSelectedItem(null);
        }
    }
    
    private String getSelectedSalesManagerId() {
        String selected = (String) tSalesManager.getSelectedItem();
        return selected != null ? selected.split(" - ")[0] : null;
    }



    // --- SUPPLIER METHODS
    private void updateSupplierSelection(String selectedSupplierId) {
        ListModel<String> model = supplierList.getModel();

        for (int i = 0; i < model.getSize(); i++) {
            String element = model.getElementAt(i);
            if (element.startsWith(selectedSupplierId + " -")) {
                supplierList.setSelectedIndex(i);
                supplierList.ensureIndexIsVisible(i);
                return;
            }
        }
        supplierList.clearSelection();
    }

    
    private void loadSupplierItems() {
        supplierItemsMap.clear();
        List<SupplierItem> supplierItemLinks = loadSupplierItemLinks();
        for (SupplierItem si : supplierItemLinks) {
            Item item = iM.findItemById(si.getItemId()); 
            if (item != null) {
                supplierItemsMap
                    .computeIfAbsent(si.getSupplierId(), k -> new ArrayList<>())
                    .add(item);
            }
        }
    }
    
    private List<SupplierItem> loadSupplierItemLinks() {
        List<SupplierItem> links = new ArrayList<>();
        File file = new File("supplieritem.txt");

        if (!file.exists()) return links;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                SupplierItem si = SupplierItem.fromFileString(line);
                if (si != null) {
                    links.add(si);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return links;
    }
    
    private void loadSuppliersForSelectedItem() {
        DefaultListModel<String> model = (DefaultListModel<String>) supplierList.getModel();
        model.clear();
        supplierMap.clear();
        String selectedItemText = (String) tItems.getSelectedItem();
        if (selectedItemText == null) return;
        String itemId = selectedItemText.split(" - ")[0].trim();
        if (itemId.isEmpty()) return;

        for (Map.Entry<String, List<Item>> entry : supplierItemsMap.entrySet()) {
            String supplierId = entry.getKey();
            List<Item> items = entry.getValue();

            for (Item item : items) {
                if (item.getItemId().equals(itemId)) {
                    Supplier supplier = sM.findSupplierById(supplierId);
                    if (supplier != null) {
                        String displayText = supplierId + " - " + supplier.getSupplierName();
                        model.addElement(displayText);
                        supplierMap.put(supplierId, supplier.getSupplierName());
                        break; // Add supplier only once per match
                    }
                }
            }
        }
    }
    
    private void loadAllSuppliers() {
        DefaultListModel<String> model = new DefaultListModel<>();
        supplierMap.clear();

        // Assuming sM.getAllSuppliers() returns List<Supplier>
        for (Supplier supplier : sM.getAllSuppliers()) {
            String displayText = supplier.getSupplierId() + " - " + supplier.getSupplierName();
            model.addElement(displayText);
            supplierMap.put(supplier.getSupplierId(), supplier.getSupplierName());
        }

        supplierList.setModel(model);
    }

    
    private void loadItemsToComboBox() {
        tItems.removeAllItems(); 
        List<Item> allItems = iM.getAllItems();
        for (Item item : allItems) {
            String displayText = item.getItemId() + " - " + item.getItemName();
            tItems.addItem(displayText);
        }
    }
    
    // --- QUANTITY, COST, UNIT PRICE METHOD
    private void updateUnitPriceBasedOnSupplier() {
        String selectedSupplierText = supplierList.getSelectedValue();
        String selectedItemText = (String) tItems.getSelectedItem();

        if (selectedSupplierText == null || selectedItemText == null) return;

        String supplierId = selectedSupplierText.split(" - ")[0].trim();
        String itemId = selectedItemText.split(" - ")[0].trim();

        // Check if this supplier supplies this item
        List<Item> items = supplierItemsMap.get(supplierId);
        if (items != null) {
            for (Item item : items) {
                if (item.getItemId().equals(itemId)) {
                    tItemUnitPrice.setText(String.format("%.2f", item.getItemUnitPrice()));
                    updateTotalCost(); // Optional: auto-update total if quantity is already entered
                    break;
                }
            }
        }
    }
    
    private void updateTotalCost() {
        String quantityText = tItemQuantity.getText().trim();
        String unitPriceText = tItemUnitPrice.getText().trim();

        try {
            int quantity = Integer.parseInt(quantityText);
            double unitPrice = Double.parseDouble(unitPriceText);
            double totalCost = quantity * unitPrice;
            tTotalCost.setText(String.format("%.2f", totalCost));
        } catch (NumberFormatException e) {
            tTotalCost.setText(""); // Clear if invalid input
        }
    }
    
    private void setupQuantityFieldListener() {
        tItemQuantity.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                validateAndUpdateTotal();
            }
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                validateAndUpdateTotal();
            }
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                validateAndUpdateTotal();
            }
        });
    }

    private void validateAndUpdateTotal() {
        String quantityText = tItemQuantity.getText().trim();
        if (!quantityText.matches("\\d+")) {
            tTotalCost.setText("");
            return;
        }
        updateTotalCost();
    }

    // --- STATUS
    public void updateStatusComboBox(String userRole) {
        if (userRole.equalsIgnoreCase("SM")) {
            tPrStatus.setModel(new DefaultComboBoxModel<>(SM_STATUS));
            setButton(true);
        } else if (userRole.equalsIgnoreCase("PM")) {
            tPrStatus.setModel(new DefaultComboBoxModel<>(PM_STATUS));
            setButton(false);
        }
    }

    
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
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        save = new javax.swing.JButton();
        delete = new javax.swing.JButton();
        add = new javax.swing.JButton();
        clear = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        tItemQuantity = new javax.swing.JTextField();
        tItemUnitPrice = new javax.swing.JTextField();
        jLabel23 = new javax.swing.JLabel();
        tItems = new javax.swing.JComboBox<>();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        tTotalCost = new javax.swing.JTextField();
        tCreatedDate = new javax.swing.JTextField();
        tSalesManager = new javax.swing.JComboBox<>();
        tRequiredDate = new com.github.lgooddatepicker.components.DatePicker();
        tPrId = new javax.swing.JTextField();
        tPrStatus = new javax.swing.JComboBox<>();
        search = new javax.swing.JTextField();
        btnSearch = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        header = new javax.swing.JLabel();
        btnBack = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        jPrTable = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        supplierList = new javax.swing.JList<>();

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
        save.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveActionPerformed(evt);
            }
        });

        delete.setText("Delete");
        delete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteActionPerformed(evt);
            }
        });

        add.setText("Add");
        add.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addActionPerformed(evt);
            }
        });

        clear.setText("Clear");
        clear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(add)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(save, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(delete, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(clear, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {add, clear, delete, save});

        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(add)
                    .addComponent(save)
                    .addComponent(delete)
                    .addComponent(clear))
                .addContainerGap())
        );

        jPanel2Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {add, clear, delete, save});

        jPanel7.setBackground(new java.awt.Color(217, 232, 239));

        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel12.setText(" ~ PR Detail ~");
        jLabel12.setFont(new java.awt.Font("Segoe UI", 3, 16)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(0, 0, 204));

        jLabel21.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel21.setText("ID :");
        jLabel21.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel6.setText("Item:");
        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel7.setText("Required date:");
        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel13.setText("Created date:");
        jLabel13.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        tItemQuantity.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tItemQuantityActionPerformed(evt);
            }
        });

        jLabel23.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel23.setText("Sales Manager:");
        jLabel23.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        tItems.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        tItems.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tItemsActionPerformed(evt);
            }
        });

        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel8.setText("Quantity:");
        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel9.setText("Unit Price:");
        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

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

        tPrId.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tPrIdActionPerformed(evt);
            }
        });

        tPrStatus.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "SUBMITTED", "APPROVE", "REJECT" }));
        tPrStatus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tPrStatusActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel7Layout.createSequentialGroup()
                        .addGap(0, 21, Short.MAX_VALUE)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel21, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel23, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel9, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel11, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel7Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(18, 18, 18)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tSalesManager, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(tItems, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(tItemQuantity)
                    .addComponent(tItemUnitPrice)
                    .addComponent(tTotalCost)
                    .addComponent(tCreatedDate)
                    .addComponent(tRequiredDate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(tPrStatus, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(tPrId))
                .addGap(48, 48, 48))
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(61, 61, 61)
                .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 213, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel21)
                    .addComponent(tPrId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tSalesManager, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(46, 46, 46)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tCreatedDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(2, 2, 2))
                    .addComponent(tRequiredDate, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(tPrStatus)
                    .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(49, 49, 49)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(tItems, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tItemQuantity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(16, 16, 16)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tItemUnitPrice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(tTotalCost, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(28, 28, 28))
        );

        search.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchActionPerformed(evt);
            }
        });

        btnSearch.setText("Search");
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

        btnBack.setText("Back");
        btnBack.setBackground(new java.awt.Color(102, 102, 102));
        btnBack.setFont(new java.awt.Font("Serif", 1, 12)); // NOI18N
        btnBack.setForeground(new java.awt.Color(255, 255, 255));
        btnBack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBackActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(btnBack)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(header, javax.swing.GroupLayout.PREFERRED_SIZE, 599, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(119, 119, 119))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(header)
                    .addComponent(btnBack))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPrTable.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane3.setViewportView(jPrTable);

        jLabel17.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel17.setText(" ~ Supplier Select~");
        jLabel17.setFont(new java.awt.Font("Segoe UI", 3, 16)); // NOI18N
        jLabel17.setForeground(new java.awt.Color(0, 0, 204));

        supplierList.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(supplierList, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(31, 31, 31))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(55, 55, 55))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(supplierList, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(search, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnSearch)
                .addGap(53, 53, 53))
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 530, Short.MAX_VALUE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(92, 92, 92)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addGap(18, 18, 18)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void addActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addActionPerformed
        try {
            String selectedSupplier = supplierList.getSelectedValue();
            String status = (String) tPrStatus.getSelectedItem();
            String supplierId = selectedSupplier.split(" - ")[0].trim();
            LocalDate reqDate = tRequiredDate.getDate();
            
            String selectedItemText = (String) tItems.getSelectedItem();
            String selectedItemId = selectedItemText.split(" - ")[0].trim();
            int quantity = Integer.parseInt(tItemQuantity.getText().trim());
            Item selectedItem = iM.findItemById(selectedItemId);
            String smId = getSelectedSalesManagerId();
            if (smId == null) {
                JOptionPane.showMessageDialog(this, "Please select a valid Sales Manager.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (selectedItem == null) {
                JOptionPane.showMessageDialog(this, "Selected item not found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            PrItem prItem = new PrItem(selectedItem, quantity);

            pr newPr = new pr(
                tPrId.getText(),
                smId,
                supplierId,
                prItem,
                status,
                currentDate,
                reqDate
            );
            prM.addPr(newPr);
            JOptionPane.showMessageDialog(this, "PR added successfully!");
            clearText();
            loadTable();
            displayPr(0);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error creating PR: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_addActionPerformed

    private void searchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchActionPerformed
        
    }//GEN-LAST:event_searchActionPerformed

    private void tSalesManagerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tSalesManagerActionPerformed
        String selectedId = getSelectedSalesManagerId();
    }//GEN-LAST:event_tSalesManagerActionPerformed

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
                displayPr(i);
                found = true;
                break;
            }
        }

        if (!found) {
            JOptionPane.showMessageDialog(this, 
                search + " not found",
                "Not Found",
                JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_btnSearchActionPerformed

    private void tPrIdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tPrIdActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tPrIdActionPerformed

    private void tItemsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tItemsActionPerformed
        tItemUnitPrice.setText("");
        tTotalCost.setText("");
        tItemQuantity.setText("");
        loadSupplierItems();
        loadSuppliersForSelectedItem();
        
    }//GEN-LAST:event_tItemsActionPerformed

    private void tItemQuantityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tItemQuantityActionPerformed

    }//GEN-LAST:event_tItemQuantityActionPerformed

    private void tPrStatusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tPrStatusActionPerformed
        
    }//GEN-LAST:event_tPrStatusActionPerformed

    private void saveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveActionPerformed
        try {
            String selectedSupplier = supplierList.getSelectedValue();
            String status = (String) tPrStatus.getSelectedItem();
            String supplierId = selectedSupplier.split(" - ")[0].trim();
            String dateText = tCreatedDate.getText();
            LocalDate createDate = LocalDate.parse(dateText);
            LocalDate reqDate = tRequiredDate.getDate();

            String selectedItemText = (String) tItems.getSelectedItem();
            String selectedItemId = selectedItemText.split(" - ")[0].trim();
            
            String quantityText = tItemQuantity.getText().trim();
            int quantity;
            try {
                quantity = Integer.parseInt(quantityText);
                if (quantity <= 0) {
                    JOptionPane.showMessageDialog(this, "Quantity must be greater than 0.", "Invalid Quantity", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid integer for quantity.", "Invalid Quantity", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Item selectedItem = iM.findItemById(selectedItemId);
            
            String smId = getSelectedSalesManagerId();
            if (smId == null) {
                JOptionPane.showMessageDialog(this, "Please select a valid Sales Manager.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (selectedItem == null) {
                JOptionPane.showMessageDialog(this, "Selected item not found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            
            PrItem prItem = new PrItem(selectedItem, quantity);

            pr updatedPr = new pr(
                tPrId.getText(),
                smId,
                supplierId,
                prItem,
                status,
                createDate,
                reqDate
            );

            prM.saveEditedPr(updatedPr); 
            JOptionPane.showMessageDialog(this, "Saved successfully!");
            clearText();
            loadTable();
            displayPr(0);

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
                "Are you sure you want to delete " + prId + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                boolean deleted = prM.deletePr(prId);
                if (deleted) {
                    JOptionPane.showMessageDialog(this, "PR deleted successfully.");
                    clearText();
                    loadTable();
                    displayPr(0);
                } else {
                    JOptionPane.showMessageDialog(this, "PR not found.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error delete PR: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_deleteActionPerformed

    private void btnBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackActionPerformed
        if (userRole.equalsIgnoreCase("SM")) {
            new smpage().setVisible(true);
            this.dispose();
        } else if (userRole.equalsIgnoreCase("PM")) {
            new pmpage().setVisible(true);
            this.dispose();
        }
    }//GEN-LAST:event_btnBackActionPerformed

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
    private javax.swing.JButton btnBack;
    private javax.swing.JButton btnSearch;
    private javax.swing.JButton clear;
    private javax.swing.JButton delete;
    private javax.swing.JLabel header;
    private javax.swing.JFrame jFrame1;
    private javax.swing.JFrame jFrame2;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JTable jPrTable;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JButton save;
    private javax.swing.JTextField search;
    private javax.swing.JList<String> supplierList;
    private javax.swing.JTextField tCreatedDate;
    private javax.swing.JTextField tItemQuantity;
    private javax.swing.JTextField tItemUnitPrice;
    private javax.swing.JComboBox<String> tItems;
    private javax.swing.JTextField tPrId;
    private javax.swing.JComboBox<String> tPrStatus;
    private com.github.lgooddatepicker.components.DatePicker tRequiredDate;
    private javax.swing.JComboBox<String> tSalesManager;
    private javax.swing.JTextField tTotalCost;
    // End of variables declaration//GEN-END:variables
}
