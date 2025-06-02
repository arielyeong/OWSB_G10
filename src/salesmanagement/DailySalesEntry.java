/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package salesmanagement;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.JOptionPane;
import usermanagement.smpage;


/**
 *
 * @author Yeong Huey Yee
 */
public class DailySalesEntry extends javax.swing.JFrame {
    
    private DailySalesManager salesManager;
    private javax.swing.JTextField txtDate;
    private final String ITEM_FILE = "item.txt";
    private List<Item> items = new ArrayList<>();
    private List<DailySales> salesList;
    private int currentSalesIndex = -1;
    private List<DailySales> allSales;
    
    /**
     * Creates new form DailySalesEntry
     */
    public DailySalesEntry() {
    initComponents();
    setLocationRelativeTo(null);
    txtItemName.setEditable(false);
    txtSaleId1.setEditable(false);
    salesManager = new DailySalesManager();
    salesList = salesManager.getAllSales();
    loadItemsFromFile();
    refreshTable();
    refreshItemTable();
    clearFields();
}
    
    private void loadItemsFromFile() {
    try (BufferedReader reader = new BufferedReader(new FileReader(ITEM_FILE))) {
        String line;
        while ((line = reader.readLine()) != null) {
            Item item = Item.fromFileString(line);
            items.add(item);
        }
    } catch (IOException e) {
        System.out.println("No existing item file found, starting fresh.");
    }
}
    
    private void refreshTable() {
        String[] columnNames = {"Sales ID", "Item ID", "Item Name", "Date", "Quantity Sold", "Total Price"};
        List<DailySales> salesList = salesManager.getAllSales();
        String[][] data = new String[salesList.size()][6];

        for (int i = 0; i < salesList.size(); i++) {
            DailySales sale = salesList.get(i);
            data[i][0] = sale.getSalesId();
            data[i][1] = sale.getItemId();
            data[i][2] = sale.getItemName();
            data[i][3] = sale.getDate();
            data[i][4] = String.valueOf(sale.getQuantitySold());
            data[i][5] = String.valueOf(sale.getTotalPrice());
        }

        jTable3.setModel(new javax.swing.table.DefaultTableModel(data, columnNames));
    }
    
    private void refreshItemTable() {
        String[] columnNames = {"Item ID", "Item Name", "Category", "UOM", "Price", "Quantity", "Suppliers"};
        String[][] data = new String[items.size()][7];

        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            data[i][0] = item.getItemId();
            data[i][1] = item.getItemName();
            data[i][2] = item.getItemCategory();
            data[i][3] = item.getItemUOM();
            data[i][4] = String.valueOf(item.getItemUnitPrice());
            data[i][5] = String.valueOf(item.getStockQuantity());
            data[i][6] = getSuppliersForItem(item.getItemId()); // 🔄
        }

        jTable1.setModel(new javax.swing.table.DefaultTableModel(data, columnNames));
    }
    
    private String getSuppliersForItem(String itemId) {
        List<String> supplierIds = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("supplieritem.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2 && parts[1].equals(itemId)) {
                    supplierIds.add(parts[0]);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading supplieritem.txt: " + e.getMessage());
        }
        return String.join(", ", supplierIds);
    }
    
    private void saveItemsToFile() {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(ITEM_FILE))) {
        for (Item item : items) {
            writer.write(item.toFileString());
            writer.newLine();
        }
    } catch (IOException e) {
        System.out.println("Error saving items to file: " + e.getMessage());
    }
}
    
    private void clearFields() {
         txtSaleId1.setText(generateNextSalesId(salesManager.getAllSales()));
        txtItemId.setText("");
        txtItemName.setText("");
        txtQtySold.setText("");
        txtTotalPrice.setText("");
        textSearch.setText("");
        datePicker1.setDate(LocalDate.now()); // Set default to today’s date
    }
    
    private void displaySale(DailySales sale) {
    if (sale != null) {
        txtSaleId1.setText(sale.getSalesId());
        txtItemId.setText(sale.getItemId());
        txtItemName.setText(sale.getItemName());
        txtQtySold.setText(String.valueOf(sale.getQuantitySold()));
        txtTotalPrice.setText(String.valueOf(sale.getTotalPrice()));

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date parsedDate = sdf.parse(sale.getDate());

            Instant instant = parsedDate.toInstant();
            ZoneId zoneId = ZoneId.systemDefault();
            LocalDate localDate = instant.atZone(zoneId).toLocalDate();

            datePicker1.setDate(localDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        }
    }

    
    private String generateNextSalesId(List<DailySales> currentSales) {
        int maxId = 0;
        for (DailySales sale : currentSales) {
            try {
                int idNum = Integer.parseInt(sale.getSalesId().replaceAll("\\D", ""));
                if (idNum > maxId) {
                    maxId = idNum;
                }
            } catch (NumberFormatException e) {
               
            }
        }
        return String.format("SD%03d", maxId + 1); 
    }

    
    private void updateTotalPrice() {
        try {
            String selectedItemId = txtItemId.getText().trim();
            String qtyText = txtQtySold.getText().trim();

            if (!selectedItemId.isEmpty() && !qtyText.isEmpty()) {
                int quantity = Integer.parseInt(qtyText);
                for (Item item : items) {
                    if (item.getItemId().equals(selectedItemId)) {
                        double totalPrice = quantity * item.getItemUnitPrice();
                        txtTotalPrice.setText(String.format("%.2f", totalPrice));
                        return;
                    }
                }
            } else {
                txtTotalPrice.setText("");
            }
        } catch (NumberFormatException e) {
            txtTotalPrice.setText("");
        }
    }
    
    private Item findItemById(String itemId) {
        for (Item item : items) {  
            if (item.getItemId().equals(itemId)) {
                return item;
            }
        }
        return null;
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        btnBack = new javax.swing.JButton();
        header = new javax.swing.JLabel();
        textSearch = new javax.swing.JTextField();
        btnSearch = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        txtItemId = new javax.swing.JTextField();
        txtSaleId1 = new javax.swing.JTextField();
        txtItemName = new javax.swing.JTextField();
        txtQtySold = new javax.swing.JTextField();
        txtTotalPrice = new javax.swing.JTextField();
        datePicker1 = new com.github.lgooddatepicker.components.DatePicker();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable3 = new javax.swing.JTable();
        jPanel10 = new javax.swing.JPanel();
        prev = new javax.swing.JButton();
        first = new javax.swing.JButton();
        next = new javax.swing.JButton();
        last = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        btnAdd = new javax.swing.JButton();
        btnUpdate = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        btnClear = new javax.swing.JButton();
        jLabel13 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(152, 202, 232));

        jPanel2.setBackground(new java.awt.Color(200, 224, 235));
        jPanel2.setPreferredSize(new java.awt.Dimension(940, 50));

        btnBack.setText("Back");
        btnBack.setBackground(new java.awt.Color(102, 102, 102));
        btnBack.setFont(new java.awt.Font("Serif", 1, 12)); // NOI18N
        btnBack.setForeground(new java.awt.Color(255, 255, 255));
        btnBack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBackActionPerformed(evt);
            }
        });

        header.setText("OWSB Purchase Order Management System");
        header.setFont(new java.awt.Font("Sitka Text", 1, 24)); // NOI18N
        header.setToolTipText("");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(50, 50, 50)
                .addComponent(btnBack)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(header, javax.swing.GroupLayout.PREFERRED_SIZE, 599, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(103, 103, 103))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(10, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnBack)
                    .addComponent(header))
                .addGap(9, 9, 9))
        );

        btnSearch.setText("Search");
        btnSearch.setToolTipText("");
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchActionPerformed(evt);
            }
        });

        jPanel3.setBackground(new java.awt.Color(200, 224, 235));

        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel12.setText(" ~ Daily Sales Entry ~");
        jLabel12.setFont(new java.awt.Font("Segoe UI", 3, 16)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(0, 0, 204));

        jLabel21.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel21.setText("Sales ID :");
        jLabel21.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        jLabel22.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel22.setText("Quantity Sold :");
        jLabel22.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        jLabel23.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel23.setText("Item ID :");
        jLabel23.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        jLabel24.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel24.setText(" Date :");
        jLabel24.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        jLabel25.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel25.setText("Total Price (RM) :");
        jLabel25.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        jLabel26.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel26.setText("Item Name :");
        jLabel26.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        txtItemId.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtItemIdActionPerformed(evt);
            }
        });
        txtItemId.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtItemIdKeyReleased(evt);
            }
        });

        txtSaleId1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSaleId1ActionPerformed(evt);
            }
        });

        txtQtySold.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtQtySoldKeyReleased(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel24, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(jPanel3Layout.createSequentialGroup()
                                    .addGap(28, 28, 28)
                                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel26, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addComponent(jLabel25, javax.swing.GroupLayout.Alignment.TRAILING)))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtSaleId1)
                            .addComponent(txtItemId)
                            .addComponent(txtItemName)
                            .addComponent(txtQtySold)
                            .addComponent(txtTotalPrice)
                            .addComponent(datePicker1, javax.swing.GroupLayout.PREFERRED_SIZE, 244, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(107, 107, 107)
                        .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(34, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtSaleId1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtItemId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(19, 19, 19)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel26, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtItemName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel24, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(datePicker1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtQtySold, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel25, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtTotalPrice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Daily Item-wise Sales Management:");
        jLabel1.setFont(new java.awt.Font("Sitka Text", 3, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(0, 51, 255));

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {},
                {},
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(jTable1);

        jTable3.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {},
                {},
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        jScrollPane2.setViewportView(jTable3);

        jPanel10.setBackground(new java.awt.Color(204, 204, 255));

        prev.setText("Prev");
        prev.setBorder(null);
        prev.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prevActionPerformed(evt);
            }
        });

        first.setText("First");
        first.setBorder(null);
        first.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                firstActionPerformed(evt);
            }
        });

        next.setText("Next");
        next.setBorder(null);
        next.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextActionPerformed(evt);
            }
        });

        last.setText("Last");
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
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel10Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(last, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(next, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(prev, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(first, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jPanel4.setBackground(new java.awt.Color(255, 204, 204));

        btnAdd.setText("Add Sale");
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        btnUpdate.setText("Update Sale");
        btnUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateActionPerformed(evt);
            }
        });

        btnDelete.setText("Delete Sale");
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });

        btnClear.setText("Clear");
        btnClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(btnAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnUpdate)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnClear)
                .addContainerGap(12, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnUpdate, javax.swing.GroupLayout.DEFAULT_SIZE, 35, Short.MAX_VALUE)
                    .addComponent(btnAdd, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnDelete, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnClear, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel13.setText(" Item List :");
        jLabel13.setFont(new java.awt.Font("Segoe UI", 3, 14)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(0, 0, 204));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 956, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(42, 42, 42)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 419, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 419, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(29, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 447, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(textSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 311, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnSearch)
                        .addGap(61, 61, 61))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(50, 50, 50)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(59, 59, 59))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSearch)
                    .addComponent(textSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(22, 22, 22)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 137, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(3, 3, 3)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackActionPerformed
        new smpage().setVisible(true);
        this.dispose();
    }//GEN-LAST:event_btnBackActionPerformed

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        String salesId = textSearch.getText();
        DailySales sale = salesManager.findSalesById(salesId);

        if (sale != null) {
            txtSaleId1.setText(sale.getSalesId());
            txtItemId.setText(sale.getItemId());
            txtItemName.setText(sale.getItemName());
            txtQtySold.setText(String.valueOf(sale.getQuantitySold()));
            txtTotalPrice.setText(String.valueOf(sale.getTotalPrice()));

            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date parsedDate = sdf.parse(sale.getDate());

                // Convert java.util.Date → java.time.LocalDate
                Instant instant = parsedDate.toInstant();
                ZoneId zoneId = ZoneId.systemDefault();
                LocalDate localDate = instant.atZone(zoneId).toLocalDate();

                datePicker1.setDate(localDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Sales record not found!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnSearchActionPerformed

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        
        try {
        String saleId = txtSaleId1.getText().trim();
        String itemId = txtItemId.getText().trim();
        String qtyText = txtQtySold.getText().trim();
        String priceText = txtTotalPrice.getText().trim();
        LocalDate localDate = datePicker1.getDate();

        // Validation
        if (saleId.isEmpty() || itemId.isEmpty() || qtyText.isEmpty() || priceText.isEmpty() || localDate == null) {
            JOptionPane.showMessageDialog(this, "All fields must be filled, including the date!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Date dateObj = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        if (salesManager.findSalesById(saleId) != null) {
            JOptionPane.showMessageDialog(this, "Sales ID already exists!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int qtyToSell = Integer.parseInt(qtyText);
        double totalPrice = Double.parseDouble(priceText);

        if (qtyToSell <= 0) {
            JOptionPane.showMessageDialog(this, "Quantity must be greater than 0!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (totalPrice <= 0) {
            JOptionPane.showMessageDialog(this, "Total price must be greater than 0!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Item selectedItem = findItemById(itemId);
        if (selectedItem == null) {
            JOptionPane.showMessageDialog(this, "Invalid Item ID!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (qtyToSell > selectedItem.getStockQuantity()) {
            JOptionPane.showMessageDialog(this, "Not enough stock!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create new record
        String date = new SimpleDateFormat("yyyy-MM-dd").format(dateObj);

        DailySales sale = new DailySales(
            saleId,
            selectedItem.getItemId(),
            selectedItem.getItemName(),
            date,
            qtyToSell,
            totalPrice
        );

        if (salesManager.addSales(sale)) {
            selectedItem.setStockQuantity(selectedItem.getStockQuantity() - qtyToSell);
            saveItemsToFile();
            JOptionPane.showMessageDialog(this, "Sale added successfully!");
            clearFields();
            refreshTable();
            refreshItemTable();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to add sale!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, "Quantity and Price must be valid numbers!", "Format Error", JOptionPane.ERROR_MESSAGE);
    }
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
        try {
        String saleId = txtSaleId1.getText().trim();
        String itemId = txtItemId.getText().trim();
        String qtyText = txtQtySold.getText().trim();
        String priceText = txtTotalPrice.getText().trim();
        LocalDate localDate = datePicker1.getDate();

        // Validation
        if (saleId.isEmpty() || itemId.isEmpty() || qtyText.isEmpty() || priceText.isEmpty() || localDate == null) {
            JOptionPane.showMessageDialog(this, "All fields must be filled, including the date!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Date dateObj = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        DailySales oldSale = salesManager.findSalesById(saleId);
        if (oldSale == null) {
            JOptionPane.showMessageDialog(this, "Sales record not found!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int oldQtySold = oldSale.getQuantitySold();
        int newQtySold = Integer.parseInt(qtyText);
        double totalPrice = Double.parseDouble(priceText);

        if (newQtySold <= 0) {
            JOptionPane.showMessageDialog(this, "Quantity must be greater than 0!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (totalPrice <= 0) {
            JOptionPane.showMessageDialog(this, "Total price must be greater than 0!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Item item = findItemById(itemId);
        if (item == null) {
            JOptionPane.showMessageDialog(this, "Invalid Item ID!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Check stock movement/changes
        int newStock = item.getStockQuantity() + oldQtySold - newQtySold;
        if (newStock < 0) {
            JOptionPane.showMessageDialog(this, "Not enough stock after update!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Update
        String date = new SimpleDateFormat("yyyy-MM-dd").format(dateObj);

        DailySales newSale = new DailySales(
            saleId,
            itemId,
            txtItemName.getText(),
            date,
            newQtySold,
            totalPrice
        );

        if (salesManager.updateSales(newSale)) {
            item.setStockQuantity(newStock);
            saveItemsToFile();
            JOptionPane.showMessageDialog(this, "Sale updated successfully!");
            clearFields();
            refreshTable();
            refreshItemTable();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update sale!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, "Quantity and Price must be valid numbers!", "Format Error", JOptionPane.ERROR_MESSAGE);
    }
    }//GEN-LAST:event_btnUpdateActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        String salesId = textSearch.getText().trim();

        if (salesId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a Sales ID to delete.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        DailySales sale = salesManager.findSalesById(salesId);
        if (sale == null) {
            JOptionPane.showMessageDialog(this, "Sales record not found!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this sale?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            
            Item item = findItemById(sale.getItemId());
            if (item != null) {
                item.setStockQuantity(item.getStockQuantity() + sale.getQuantitySold());
                saveItemsToFile(); 
            }

            if (salesManager.deleteSales(salesId)) {
                JOptionPane.showMessageDialog(this, "Sale deleted successfully!");
                clearFields();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete sale!", "Error", JOptionPane.ERROR_MESSAGE);
            }
            refreshTable();
            refreshItemTable();
        }
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearActionPerformed
        clearFields();
    }//GEN-LAST:event_btnClearActionPerformed

    private void txtSaleId1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSaleId1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtSaleId1ActionPerformed

    private void txtItemIdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtItemIdActionPerformed
       
    }//GEN-LAST:event_txtItemIdActionPerformed

    private void txtItemIdKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtItemIdKeyReleased
        String selectedItemId = txtItemId.getText().trim();
    if (!selectedItemId.isEmpty()) {
        for (Item item : items) {
            if (item.getItemId().equals(selectedItemId)) {
                txtItemName.setText(item.getItemName());
                return;
            }
        }
    }
    txtItemName.setText("");
    updateTotalPrice();
    }//GEN-LAST:event_txtItemIdKeyReleased

    private void txtQtySoldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtQtySoldKeyReleased
       updateTotalPrice();
    }//GEN-LAST:event_txtQtySoldKeyReleased

    private void prevActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prevActionPerformed
         if (!salesList.isEmpty() && currentSalesIndex > 0) {
        currentSalesIndex--;
        displaySale(salesList.get(currentSalesIndex));
    }
    }//GEN-LAST:event_prevActionPerformed

    private void firstActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_firstActionPerformed
        if (!salesList.isEmpty()) {
        currentSalesIndex = 0;
        displaySale(salesList.get(currentSalesIndex));
    }
    }//GEN-LAST:event_firstActionPerformed

    private void nextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextActionPerformed
        if (currentSalesIndex < salesList.size() - 1) {
        currentSalesIndex++;
        displaySale(salesList.get(currentSalesIndex));
    }
    }//GEN-LAST:event_nextActionPerformed

    private void lastActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lastActionPerformed
        if (!salesList.isEmpty()) {
        currentSalesIndex = salesList.size() - 1;
        displaySale(salesList.get(currentSalesIndex));
    }
    }//GEN-LAST:event_lastActionPerformed

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
            java.util.logging.Logger.getLogger(DailySalesEntry.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(DailySalesEntry.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(DailySalesEntry.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(DailySalesEntry.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new DailySalesEntry().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnBack;
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnSearch;
    private javax.swing.JButton btnUpdate;
    private com.github.lgooddatepicker.components.DatePicker datePicker1;
    private javax.swing.JButton first;
    private javax.swing.JLabel header;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable3;
    private javax.swing.JButton last;
    private javax.swing.JButton next;
    private javax.swing.JButton prev;
    private javax.swing.JTextField textSearch;
    private javax.swing.JTextField txtItemId;
    private javax.swing.JTextField txtItemName;
    private javax.swing.JTextField txtQtySold;
    private javax.swing.JTextField txtSaleId1;
    private javax.swing.JTextField txtTotalPrice;
    // End of variables declaration//GEN-END:variables


     
}
