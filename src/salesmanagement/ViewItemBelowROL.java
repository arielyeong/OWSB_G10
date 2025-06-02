/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package salesmanagement;

import java.awt.Color;
import java.awt.Component;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import usermanagement.smpage;
/**
 *
 * @author Yeong Huey Yee
 */
public class ViewItemBelowROL extends javax.swing.JFrame {
    
    private final String ITEM_FILE = "item.txt";
    private List<Item> items = new ArrayList<>();
    /**
     * Creates new form ViewItemBelowROL
     */
    public ViewItemBelowROL() {
        initComponents();
        setLocationRelativeTo(null);
        loadDataFromFiles(); // Load items from file
        refreshItemTable();  // Display all items initially
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
                    data[i][6] = getSuppliersForItem(item.getItemId());
                }

                itemTable.setModel(new javax.swing.table.DefaultTableModel(data, columnNames));

                // Apply the custom renderer to all columns to highlight low stock rows
                for (int i = 0; i < itemTable.getColumnCount(); i++) {
                    itemTable.getColumnModel().getColumn(i).setCellRenderer(new LowStockRenderer());
                }
            }
        
        private void loadDataFromFiles() {
            items.clear();
            try (BufferedReader reader = new BufferedReader(new FileReader(ITEM_FILE))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    Item item = Item.fromFileString(line);
                    if (item != null) {
                        items.add(item);
                    }
                }
            } catch (IOException e) {
                System.out.println("No existing item file found, starting fresh: " + e.getMessage());
            }
            }
        
        class LowStockRenderer extends DefaultTableCellRenderer {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, 
                    boolean hasFocus, int row, int column) {
                Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                // Get the quantity from the "Quantity" column (index 5)
                String quantityStr = table.getModel().getValueAt(row, 5).toString();
                try {
                    int quantity = Integer.parseInt(quantityStr);
                    if (quantity < 10) {
                        cell.setBackground(Color.RED); // Highlight the entire row in red
                    } else {
                        cell.setBackground(Color.WHITE); // Default background for other rows
                    }
                } catch (NumberFormatException e) {
                    cell.setBackground(Color.WHITE); // Fallback in case of parsing error
                }

                // If the row is selected, override the background to show selection
                if (isSelected) {
                    cell.setBackground(table.getSelectionBackground());
                }

                return cell;
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

        jPanel1 = new javax.swing.JPanel();
        jPanel13 = new javax.swing.JPanel();
        btnBack8 = new javax.swing.JButton();
        header8 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        itemTable = new javax.swing.JTable();
        btnViewItem = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        btnRefresh = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(152, 202, 232));

        jPanel13.setBackground(new java.awt.Color(200, 224, 235));
        jPanel13.setPreferredSize(new java.awt.Dimension(940, 50));

        btnBack8.setBackground(new java.awt.Color(102, 102, 102));
        btnBack8.setFont(new java.awt.Font("Serif", 1, 12)); // NOI18N
        btnBack8.setForeground(new java.awt.Color(255, 255, 255));
        btnBack8.setText("Back");
        btnBack8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBack8btnBack1ActionPerformed(evt);
            }
        });

        header8.setFont(new java.awt.Font("Sitka Text", 1, 24)); // NOI18N
        header8.setText("OWSB Purchase Order Management System");
        header8.setToolTipText("");

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addGap(50, 50, 50)
                .addComponent(btnBack8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 64, Short.MAX_VALUE)
                .addComponent(header8, javax.swing.GroupLayout.PREFERRED_SIZE, 599, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(40, 40, 40))
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel13Layout.createSequentialGroup()
                .addContainerGap(10, Short.MAX_VALUE)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnBack8)
                    .addComponent(header8))
                .addGap(9, 9, 9))
        );

        itemTable.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(itemTable);

        btnViewItem.setText("View Low Stock Item");
        btnViewItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnViewItemActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Sitka Text", 3, 24)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(0, 51, 255));
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel3.setText("Item List:");

        btnRefresh.setText("Refresh");
        btnRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, 825, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(78, 78, 78)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 663, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 338, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnRefresh)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnViewItem)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnViewItem, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRefresh, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 398, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(23, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnBack8btnBack1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBack8btnBack1ActionPerformed
        new smpage().setVisible(true);
        this.dispose();
    }//GEN-LAST:event_btnBack8btnBack1ActionPerformed

    private void btnViewItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnViewItemActionPerformed
        // Filter items with stock less than 10
        List<Item> lowStockItems = items.stream()
                .filter(item -> item.getStockQuantity() < 10)
                .collect(Collectors.toList());

        // Prepare data for the table
        String[] columnNames = {"Item ID", "Item Name", "Category", "UOM", "Price", "Quantity", "Suppliers"};
        String[][] data = new String[lowStockItems.size()][7];

        for (int i = 0; i < lowStockItems.size(); i++) {
            Item item = lowStockItems.get(i);
            data[i][0] = item.getItemId();
            data[i][1] = item.getItemName();
            data[i][2] = item.getItemCategory();
            data[i][3] = item.getItemUOM();
            data[i][4] = String.valueOf(item.getItemUnitPrice());
            data[i][5] = String.valueOf(item.getStockQuantity());
            data[i][6] = getSuppliersForItem(item.getItemId());
        }

        // Update the table model
        itemTable.setModel(new javax.swing.table.DefaultTableModel(data, columnNames));
    }//GEN-LAST:event_btnViewItemActionPerformed

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        refreshItemTable();
    }//GEN-LAST:event_btnRefreshActionPerformed

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
            java.util.logging.Logger.getLogger(ViewItemBelowROL.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ViewItemBelowROL.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ViewItemBelowROL.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ViewItemBelowROL.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ViewItemBelowROL().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBack6;
    private javax.swing.JButton btnBack7;
    private javax.swing.JButton btnBack8;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnViewItem;
    private javax.swing.JLabel header6;
    private javax.swing.JLabel header7;
    private javax.swing.JLabel header8;
    private javax.swing.JTable itemTable;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
