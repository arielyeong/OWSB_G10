package salesmanagement;

import com.github.lgooddatepicker.components.DatePickerSettings;
import java.awt.Dimension;
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
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import salesmanagement.pr.PrItem;
import usermanagement.SalesManagerUser;
import usermanagement.PurchaseManagerUser;
import usermanagement.User;
/**
 *
 * @author charlotte
 */
public class poFrame extends javax.swing.JFrame {
    HashMap<String, String> smMap = new HashMap<>();
    HashMap<String, String> pmMap = new HashMap<>();
    HashMap<String, String> supplierMap = new HashMap<>();
    HashMap<String, List<Item>> supplierItemsMap = new HashMap<>();
    
    LocalDate currentDate = LocalDate.now();
    
    private final ItemManager iM = new ItemManager();
    private final SupplierManager sM = new SupplierManager();
    
    PrManager prM = new PrManager();
    PoManager poM = new PoManager();

    public static final String PR_FILE = "pr.txt";
    
    public poFrame() {
        initComponents();
        setLocationRelativeTo(null);
        prM.setPoManager(poM);
        poM.setPrManager(prM);
        supplierList.setModel(new DefaultListModel<>());
        tCreatedDate.setText(currentDate.toString());
        dateRange(null);
        addDateListeners();
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
        jPoTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = jPoTable.getSelectedRow();
                if (selectedRow >= 0) {
                    displayPo(selectedRow);
                }
            }
        });
        displayPo(0); 
    }
    
     // --- GENERAL METHODS 
    private void clearText() {
        tPoId.setText(poM.generateNewPoId());
        tPrId.setText("");
        tSalesManager.setSelectedItem(null); 
        tPurchaseManager.setSelectedItem(null); 
        supplierList.clearSelection();
        tItem.setSelectedItem(null);
        tItemQuantity.setText("");
        tItemUnitPrice.setText("");
        tTotalCost.setText("");
        tPoStatus.setSelectedItem(null);
        tCreatedDate.setText(currentDate.format(pr.DATE_FORMATTER));
        tOrderDate.setDate(null);
        tDeliveryDate.setDate(null);
        tDeliveryDate.setDate(null);
        dateRange(null);
        DefaultListModel<String> model = (DefaultListModel<String>) supplierList.getModel();
        model.clear();
    }

    
    private void setFormEditable(boolean edit) {
        tPoId.setEnabled(edit);
        tPrId.setEnabled(edit);
        tCreatedDate.setEnabled(edit);
        
        tItem.setEnabled(edit);
        tItemQuantity.setEnabled(edit);
        tItemUnitPrice.setEnabled(edit);
        tTotalCost.setEnabled(edit);
    }
    
    //false=disable button
    private void setButton(boolean edit) {
        add.setEnabled(edit);
        save.setEnabled(edit);
        delete.setEnabled(edit);
    }
    
    private void dateRange(LocalDate minDate) {
        DatePickerSettings oSettings = tOrderDate.getSettings();
        DatePickerSettings dSettings = tDeliveryDate.getSettings();
        DatePickerSettings iSettings = tInvoiceDate.getSettings();
        oSettings.setFormatForDatesCommonEra(pr.DATE_FORMATTER);
        dSettings.setFormatForDatesCommonEra(pr.DATE_FORMATTER);
        iSettings.setFormatForDatesCommonEra(pr.DATE_FORMATTER);

        LocalDate orderDate = tOrderDate.getDate();
        LocalDate deliveryDate = tDeliveryDate.getDate();
        LocalDate orderMin = (minDate != null) ? minDate : currentDate;
        oSettings.setDateRangeLimits(orderMin, null);

        // delivery date min: orderDate or currentDate
        LocalDate deliveryMin = (orderDate != null) ? orderDate : orderMin;
        dSettings.setDateRangeLimits(deliveryMin, null);

        // invoice date min: deliveryDate or orderDate/minDate
        LocalDate invoiceMin = (deliveryDate != null) ? deliveryDate : deliveryMin;
        iSettings.setDateRangeLimits(invoiceMin, null);
    }
    
    private void addDateListeners() {
        tOrderDate.addDateChangeListener(e -> {
            LocalDate newOrderDate = e.getNewDate();
            LocalDate deliveryMin = (newOrderDate != null) ? newOrderDate : currentDate;
            tDeliveryDate.getSettings().setDateRangeLimits(deliveryMin, null);

            LocalDate deliveryDate = tDeliveryDate.getDate();
            LocalDate invoiceMin = (deliveryDate != null) ? deliveryDate : deliveryMin;
            tInvoiceDate.getSettings().setDateRangeLimits(invoiceMin, null);
        });
        tDeliveryDate.addDateChangeListener(e -> {
            LocalDate newDeliveryDate = e.getNewDate();
            LocalDate invoiceMin = (newDeliveryDate != null) ? newDeliveryDate : currentDate;
            tInvoiceDate.getSettings().setDateRangeLimits(invoiceMin, null);
        });
    }

    
    
    // --- PR dialog
    private pr approvedPrDialog() {
        List<pr> approvedPrs = prM.getApprovedPrs();

        if (approvedPrs.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No approved PRs available.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }

        String[] prOptions = approvedPrs.stream()
            .map(p -> p.getPrId() + " - " + p.getItem().getItem().getItemName())
            .toArray(String[]::new);

        // Create JList with PR options
        JList<String> prList = new JList<>(prOptions);
        prList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(prList);
        scrollPane.setPreferredSize(new Dimension(300, 150)); 

        // Show custom dialog with OK/Cancel button
        int result = JOptionPane.showConfirmDialog(
            this,
            scrollPane,
            "Select an Approved PR to Convert to PO",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION && prList.getSelectedValue() != null) {
            String selectedValue = prList.getSelectedValue();
            String selectedPrId = selectedValue.split(" - ")[0].trim();
            return approvedPrs.stream()
                .filter(p -> p.getPrId().equals(selectedPrId))
                .findFirst()
                .orElse(null);
        }

        return null;
    }

    
    // --- PO RELATED METHODS 
    public void displayPo(int index) {
        if (index < 0 || index >= poM.getAllPo().size()) {
            clearText(); 
            return;
        }

        poM.loadPo();
        iM.loadItems();         
        loadAllSuppliers(); 
        loadSalesManagers();      
        loadPurchaseManager();
        loadSalesManagersToComboBox();
        loadPurchaseManagersToComboBox();
        po currentPo = poM.getPo(index);
        if (currentPo == null) return;

        tPrId.setText(currentPo.getPrId());
        tPoId.setText(currentPo.getPoId()); 
        tPoStatus.setSelectedItem(currentPo.getPoStatus());
        if (currentPo.getSmId() != null) {
            updateSalesManagerSelection(currentPo.getSmId());
        }
        if (currentPo.getPmId() != null) {
            updatePurchaseManagerSelection(currentPo.getPmId());
        } else {
            tPurchaseManager.setSelectedItem(null);
        }

        if (currentPo.getCreatedDate() != null) {
            tCreatedDate.setText(currentPo.getCreatedDate().format(po.DATE_FORMATTER));
            dateRange(currentPo.getCreatedDate());
        } else {
            tCreatedDate.setText(currentDate.format(po.DATE_FORMATTER));
            dateRange(null);
        }

        if (currentPo.getOrderDate() != null) {
            tOrderDate.setDate(currentPo.getOrderDate());
        }
        if (currentPo.getOrderDate() != null) {
            tDeliveryDate.setDate(currentPo.getOrderDate());
        }
        if (currentPo.getOrderDate() != null) {
            tDeliveryDate.setDate(currentPo.getOrderDate());
        }

        if (currentPo.getSupplierId() != null) {
            updateSupplierSelection(currentPo.getSupplierId());
        } else {
            supplierList.clearSelection();
        }

        pr.PrItem prItem = currentPo.getItem();
        if (prItem != null) {
            Item item = prItem.getItem();
            if (item != null) {
                tItem.setSelectedItem(item.getItemId() + " - " + item.getItemName());
                loadSuppliersForSelectedItem();
                if (currentPo.getSupplierId() != null) {
                    updateSupplierSelection(currentPo.getSupplierId());
                } else {
                    supplierList.clearSelection();
                }
                tItemQuantity.setText(String.valueOf(prItem.getQuantity()));
                tItemUnitPrice.setText(String.format("%.2f", item.getItemUnitPrice()));
                tTotalCost.setText(String.format("%.2f", prItem.getCost()));
            }
        }  else {
            tItem.setSelectedItem(null);
            tItemQuantity.setText("");
            tItemUnitPrice.setText("");
            tTotalCost.setText("");
            supplierList.clearSelection();
        }
    }
    
    private void loadTable() {
        String[] columnNames = {
            "PO ID", "PR ID", "Purchase Manager", "Sales Manager", "Supplier",
            "Item", "Quantity", "Unit Cost",
            "Status", "Created Date", "Order Date"
        };

        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        jPoTable.setModel(model); 
        poM.loadPo();

        for (po p : poM.getAllPo()) {
            pr.PrItem item = p.getItem();
            if (item != null && item.getItem() != null) {
                model.addRow(new Object[]{
                    p.getPoId(),
                    p.getPrId(),
                    p.getPmId(),
                    p.getSmId(),
                    p.getSupplierId(),
                    item.getItem().getItemId(),
                    item.getQuantity(),
                    String.format("%.2f", item.getCost()),
                    p.getPoStatus(),
                    p.getCreatedDate().format(po.DATE_FORMATTER),
                    (p.getOrderDate() != null ? p.getOrderDate().format(po.DATE_FORMATTER) : "")
                });
            }
        }
    }
    
    
    // --- SALES MANAGER METHOD
    private void loadSalesManagers() {
        smMap.clear(); 
        List<User> users = User.getalluser();
        for (User user : users) {
            if (user instanceof SalesManagerUser) {
                smMap.put(user.getUserId(), user.getUsername());
            } else {
            }
        }
    }
   
    private void loadSalesManagersToComboBox() {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        tSalesManager.removeAllItems();
        List<Map.Entry<String, String>> sortedEntries = new ArrayList<>(smMap.entrySet());
        sortedEntries.sort(Map.Entry.comparingByKey());
        for (Map.Entry<String, String> entry : sortedEntries) {
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

    // --- PURCHASE MANAGER METHOD
    private void loadPurchaseManager() {
        pmMap.clear(); 
        List<User> users = User.getalluser();
        for (User user : users) {
            if (user instanceof PurchaseManagerUser) {
                pmMap.put(user.getUserId(), user.getUsername());
            }
        }
    }
    
    private void loadPurchaseManagersToComboBox() {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        tPurchaseManager.removeAllItems();
        List<Map.Entry<String, String>> sortedEntries = new ArrayList<>(pmMap.entrySet());
        sortedEntries.sort(Map.Entry.comparingByKey());
        for (Map.Entry<String, String> entry : sortedEntries) {
            String displayText = entry.getKey() + " - " + entry.getValue();
            model.addElement(displayText);
        }
        tPurchaseManager.setModel(model);
    }
    
    private void updatePurchaseManagerSelection(String pmId) {
        String currentSelection = (String) tPurchaseManager.getSelectedItem();

        if (pmId == null || pmId.isEmpty()) {
            if (currentSelection != null) {  
                tPurchaseManager.setSelectedItem(null);
            }
            return;
        }

        String newSelection = null;
        ComboBoxModel<String> model = tPurchaseManager.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            String element = model.getElementAt(i);
            if (element.startsWith(pmId + " -")) {
                newSelection = element;
                break;
            }
        }

        if (newSelection != null && !newSelection.equals(currentSelection)) {
            tPurchaseManager.setSelectedItem(newSelection);
        } else if (newSelection == null && currentSelection != null) {
            tPurchaseManager.setSelectedItem(null);
        }
    }

    private String getSelectedPurchaseManagerId() {
        String selected = (String) tPurchaseManager.getSelectedItem();
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
        String selectedItemText = (String) tItem.getSelectedItem();
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
                        break; 
                    }
                }
            }
        }
    }
    
    private void loadAllSuppliers() {
        DefaultListModel<String> model = new DefaultListModel<>();
        supplierMap.clear();
        for (Supplier supplier : sM.getAllSuppliers()) {
            String displayText = supplier.getSupplierId() + " - " + supplier.getSupplierName();
            model.addElement(displayText);
            supplierMap.put(supplier.getSupplierId(), supplier.getSupplierName());
        }

        supplierList.setModel(model);
    }

    private void loadItemsToComboBox() {
        tItem.removeAllItems(); 
        List<Item> allItems = iM.getAllItems();
        for (Item item : allItems) {
            String displayText = item.getItemId() + " - " + item.getItemName();
            tItem.addItem(displayText);
        }
    }
    
    // --- QUANTITY, COST, UNIT PRICE METHOD
    private void updateUnitPriceBasedOnSupplier() {
        String selectedSupplierText = supplierList.getSelectedValue();
        String selectedItemText = (String) tItem.getSelectedItem();

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
        jLabel6 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        tItemQuantity = new javax.swing.JTextField();
        tItemUnitPrice = new javax.swing.JTextField();
        tItem = new javax.swing.JComboBox<>();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        tTotalCost = new javax.swing.JTextField();
        tCreatedDate = new javax.swing.JTextField();
        tPoStatus = new javax.swing.JComboBox<>();
        tPoId = new javax.swing.JTextField();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        tPrId = new javax.swing.JTextField();
        tSalesManager = new javax.swing.JComboBox<>();
        jLabel23 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        tPurchaseManager = new javax.swing.JComboBox<>();
        jLabel14 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        tDeliveryDate = new com.github.lgooddatepicker.components.DatePicker();
        tOrderDate = new com.github.lgooddatepicker.components.DatePicker();
        tInvoiceDate = new com.github.lgooddatepicker.components.DatePicker();
        search = new javax.swing.JTextField();
        btnSearch = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        header = new javax.swing.JLabel();
        btnBack = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        jPoTable = new javax.swing.JTable();
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

        jLabel2.setText("Purchase Order Management");
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

        add.setText("Generate PO");
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
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(add)
                .addGap(18, 18, Short.MAX_VALUE)
                .addComponent(save)
                .addGap(18, 18, 18)
                .addComponent(delete)
                .addGap(18, 18, 18)
                .addComponent(clear)
                .addGap(22, 22, 22))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(add)
                    .addComponent(save)
                    .addComponent(delete)
                    .addComponent(clear))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {add, clear, delete, save});

        jPanel7.setBackground(new java.awt.Color(217, 232, 239));

        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel12.setText(" ~ PO Detail ~");
        jLabel12.setFont(new java.awt.Font("Segoe UI", 3, 16)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(0, 0, 204));

        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel6.setText("Item:");
        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel13.setText("Created date:");
        jLabel13.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        tItemQuantity.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tItemQuantityActionPerformed(evt);
            }
        });

        tItem.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        tItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tItemActionPerformed(evt);
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

        tPoStatus.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "PENDING", "APPROVED", "ORDERED" }));
        tPoStatus.setForeground(new java.awt.Color(255, 255, 255));
        tPoStatus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tPoStatusActionPerformed(evt);
            }
        });

        tPoId.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tPoIdActionPerformed(evt);
            }
        });

        jLabel21.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel21.setText("PO ID:");
        jLabel21.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        jLabel22.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel22.setText(" PR ID:");
        jLabel22.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        tPrId.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tPrIdActionPerformed(evt);
            }
        });

        tSalesManager.setForeground(new java.awt.Color(255, 255, 255));
        tSalesManager.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tSalesManagerActionPerformed(evt);
            }
        });

        jLabel23.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel23.setText("Sales Manager:");
        jLabel23.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        jLabel26.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel26.setText("Purchase Manager:");
        jLabel26.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        tPurchaseManager.setForeground(new java.awt.Color(255, 255, 255));
        tPurchaseManager.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tPurchaseManagerActionPerformed(evt);
            }
        });

        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel14.setText("Order date:");
        jLabel14.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel7.setText("Delivery date:");
        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel15.setText("Invoice date:");
        jLabel15.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel23)
                    .addComponent(jLabel26)
                    .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tPoId, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tPrId, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tPurchaseManager, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tSalesManager, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGap(61, 61, 61)
                        .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 213, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, 123, Short.MAX_VALUE)
                                .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(tCreatedDate, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(tPoStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(tItemQuantity, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(tItem, javax.swing.GroupLayout.Alignment.LEADING, 0, 188, Short.MAX_VALUE)
                                .addComponent(tItemUnitPrice, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(tTotalCost, javax.swing.GroupLayout.Alignment.LEADING))
                            .addComponent(tOrderDate, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(tDeliveryDate, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(tInvoiceDate, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addGap(429, 429, 429))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(11, 11, 11)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel21)
                    .addComponent(tPoId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel22)
                    .addComponent(tPrId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel26, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tPurchaseManager, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tSalesManager, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(27, 27, 27)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(tPoStatus))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(tCreatedDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tOrderDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(tDeliveryDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(7, 7, 7)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(tInvoiceDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(27, 27, 27)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(tItem, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tItemQuantity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tItemUnitPrice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(tTotalCost, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(33, 33, 33))
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
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addComponent(btnBack)
                .addGap(82, 82, 82)
                .addComponent(header, javax.swing.GroupLayout.PREFERRED_SIZE, 599, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(header)
                    .addComponent(btnBack))
                .addContainerGap())
        );

        jPoTable.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane3.setViewportView(jPoTable);

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
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(24, 24, 24)
                        .addComponent(supplierList, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(49, 49, 49)
                        .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(27, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(supplierList, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel2))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(114, 114, 114)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                        .addContainerGap(15, Short.MAX_VALUE)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 516, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(10, 10, 10))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(78, 78, 78)))))
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(112, 112, 112)
                        .addComponent(search, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnSearch))
                    .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, 393, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(20, Short.MAX_VALUE))
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
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
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
            pr selectedPr = approvedPrDialog();
            if (selectedPr == null) return;

            po newPo = poM.createPoFromPr(selectedPr);

            JOptionPane.showMessageDialog(this, "PO created from PR successfully! PO ID: " + newPo.getPoId());
            loadTable(); // Refresh PO table
            clearText();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error creating PO: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
             System.out.println(e);
        }
    }//GEN-LAST:event_addActionPerformed

    private void searchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchActionPerformed
        
    }//GEN-LAST:event_searchActionPerformed

    private void clearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearActionPerformed
        clearText();
    }//GEN-LAST:event_clearActionPerformed

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        String searchId = search.getText().trim().toUpperCase();
        
        if (searchId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a PO/PR ID to search");
            return;
        }
        // Try both PO and PR ID searches
        po foundPo = poM.findPo(searchId); 
        boolean isPrSearch = false;
        if (foundPo == null && searchId.startsWith("PR")) {
            foundPo = poM.findPo(searchId, true);
            isPrSearch = true;
        }

        if (foundPo != null) {
            int index = poM.getAllPo().indexOf(foundPo);
            if (index >= 0) {
                displayPo(index);
                return;
            }
        }
        String searchType = isPrSearch ? "PR" : "PO";
        JOptionPane.showMessageDialog(this, 
            searchId + " not found as " + searchType + " ID",
            "Not Found",
            JOptionPane.WARNING_MESSAGE);
    }//GEN-LAST:event_btnSearchActionPerformed

    private void tItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tItemActionPerformed
        tItemUnitPrice.setText("");
        tTotalCost.setText("");
        tItemQuantity.setText("");
        loadSupplierItems();
        loadSuppliersForSelectedItem();
        
    }//GEN-LAST:event_tItemActionPerformed

    private void tItemQuantityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tItemQuantityActionPerformed

    }//GEN-LAST:event_tItemQuantityActionPerformed

    private void tPoStatusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tPoStatusActionPerformed
        
    }//GEN-LAST:event_tPoStatusActionPerformed

    private void saveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveActionPerformed
        try {
            String poId = tPoId.getText().trim(); 
            String prId = tPrId.getText().trim();
            String smId = getSelectedSalesManagerId();
            String pmId = getSelectedPurchaseManagerId();
            String selectedSupplier = supplierList.getSelectedValue();
            String status = (String) tPoStatus.getSelectedItem();
            
            
            String supplierId = selectedSupplier.split(" - ")[0].trim();
            LocalDate orderDate = tOrderDate.getDate();
            LocalDate deliveryDate = null;
            if (tDeliveryDate.getDate() != null) {
                deliveryDate = tDeliveryDate.getDate();
            }
            LocalDate invoiceDate = null;
            if (tDeliveryDate.getDate() != null) {
                invoiceDate = tDeliveryDate.getDate();
            }


            String selectedItemText = (String) tItem.getSelectedItem();
            String selectedItemId = selectedItemText.split(" - ")[0].trim();
            int quantity = Integer.parseInt(tItemQuantity.getText().trim());

            Item selectedItem = iM.findItemById(selectedItemId);
            if (selectedItem == null) {
                JOptionPane.showMessageDialog(this, "Selected item not found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            PrItem prItem = new PrItem(selectedItem, quantity);

            po updatedPo = new po(
                poId,
                prId,
                pmId,
                smId,
                supplierId,
                prItem,
                status,
                currentDate,
                orderDate,
                deliveryDate, 
                invoiceDate  
            );

            poM.updatePo(updatedPo);
            JOptionPane.showMessageDialog(this, "PO updated successfully!");
            clearText();
            loadTable(); 
            displayPo(0); 
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error saving PO: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_saveActionPerformed

    private void deleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteActionPerformed
        try {
            String poId = tPoId.getText().trim();
            if (poId.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a PO ID to delete.", "Missing ID", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete " + poId + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                boolean deleted = poM.deletePo(poId);
                if (deleted) {
                    JOptionPane.showMessageDialog(this, "PO deleted successfully.");
                    clearText();
                    loadTable();
                    displayPo(0);
                } else {
                    JOptionPane.showMessageDialog(this, "PO not found.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error delete PO: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_deleteActionPerformed

    private void tPoIdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tPoIdActionPerformed
        
    }//GEN-LAST:event_tPoIdActionPerformed

    private void tPrIdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tPrIdActionPerformed
        String selectedId = getSelectedSalesManagerId();
    }//GEN-LAST:event_tPrIdActionPerformed

    private void tSalesManagerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tSalesManagerActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tSalesManagerActionPerformed

    private void tPurchaseManagerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tPurchaseManagerActionPerformed
        String selectedId = getSelectedPurchaseManagerId();
    }//GEN-LAST:event_tPurchaseManagerActionPerformed

    private void btnBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackActionPerformed
        new MainMenu().setVisible(true);
        this.dispose();
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
            java.util.logging.Logger.getLogger(poFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(poFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(poFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(poFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new poFrame().setVisible(true);
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
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel26;
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
    private javax.swing.JTable jPoTable;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JButton save;
    private javax.swing.JTextField search;
    private javax.swing.JList<String> supplierList;
    private javax.swing.JTextField tCreatedDate;
    private com.github.lgooddatepicker.components.DatePicker tDeliveryDate;
    private com.github.lgooddatepicker.components.DatePicker tInvoiceDate;
    private javax.swing.JComboBox<String> tItem;
    private javax.swing.JTextField tItemQuantity;
    private javax.swing.JTextField tItemUnitPrice;
    private com.github.lgooddatepicker.components.DatePicker tOrderDate;
    private javax.swing.JTextField tPoId;
    private javax.swing.JComboBox<String> tPoStatus;
    private javax.swing.JTextField tPrId;
    private javax.swing.JComboBox<String> tPurchaseManager;
    private javax.swing.JComboBox<String> tSalesManager;
    private javax.swing.JTextField tTotalCost;
    // End of variables declaration//GEN-END:variables
}
