/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package usermanagement;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.awt.*;
import java.util.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author yingx
 */
public class adminpage extends javax.swing.JFrame {
    private final String user_file = "user.txt";
    private List<User> allUsers = new ArrayList<>();
    private int currentUserIndex = 0;
    //private Administrator admin;
    //private int countuser = 1;
    //private List<User> userlist = new ArrayList<>();
    
    private void clearfileds(){
        useridtxt.setText("");
        usernametxt.setText("");
        passwdtxt.setText("");
        userphntxt.setText("");
        useremailtxt.setText("");
        useraddresstxt.setText("");
        rolebox.setSelectedIndex(0);
        saleregionbox.setSelectedIndex(0);
        approvelimitbox.setSelectedIndex(0);
    }
    private String generateUserid(String role){
        //return String.format("U%03d", countuser++);//admin.getUserId().size()+1);
        String prefix = "";
        switch (role){
            case "Admin":
                prefix= "ADMIN";
                break;
            case "Sales Manager":
                prefix= "SM";
                break;
            case "Purchase Manager":
                prefix= "PM";
                break;
            case "Inventory Manager":
                prefix= "IM";
                break;
            case "Finance Manager":
                prefix= "FM";
                break;
            default:
                JOptionPane.showMessageDialog(this, "Error role"); 
        }
        int maxid = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(user_file))) {
            String line;
            while((line = reader.readLine())!=null){
                String[] parts = line.split("\\|");
                if (parts.length > 0){
                    String id =parts[0];
                    if (id.startsWith(prefix)){
                        try {
                            //int num = Integer.parseInt(id.substring(1));
                            //if (num > maxid){maxid = num;
                            String numStr = id.substring(prefix.length());
                            int num= Integer.parseInt(numStr);
                            if(num>maxid){
                                maxid = num;
                            }
                        }catch (NumberFormatException e){
                            //skip if number format invalid
                        }
                    }
                }
            }
        }catch(IOException e){
            JOptionPane.showMessageDialog(this, "Error id");
        }
        return String.format("%s%03d", prefix, maxid + 1);
    }
    private void extradetails(String role){
        boolean issm = role.equals("Sales Manager");
        boolean ispmorim = role.equals("Purchase Manager")||role.equals("Inventory Manager");
        
        saleregionbox.setVisible(issm);
        approvelimitbox.setVisible(ispmorim);
    }
    
    /**
     * Creates new form adminpage
     */
    public adminpage() {
        initComponents();
        //admin = new Administrator();
        //this.admin = admin;
        useridtxt.setText(generateUserid(rolebox.getSelectedItem().toString()));
        //adduser();
        extradetails(rolebox.getSelectedItem().toString());
        usertxtarea.setFont(new Font(Font.MONOSPACED,Font.PLAIN,12));
        loadAllUsers();
    }
    
    private void loadAllUsers(){
        allUsers = User.getalluser();
        if(!allUsers.isEmpty()){
            currentUserIndex = 0;
            displayCurrentUser();
        }else{
            usertxtarea.setText("No users found");
        }
    }
    
    private void displayCurrentUser(){
        if(allUsers.isEmpty() || currentUserIndex < 0 || currentUserIndex >=allUsers.size()){
            return;
        }
        User user = allUsers.get(currentUserIndex);
        String format = "%-30s: %s\n";
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(format,"User ID",user.getUserId()));
        sb.append(String.format(format,"Username",user.getUsername()));
        sb.append(String.format(format,"User Phone Number",user.getUserPhone()));
        sb.append(String.format(format,"User Email",user.getUserEmail()));
        sb.append(String.format(format,"User Address",user.getUserAddress()));
        sb.append(String.format(format,"User Role",user.getUserRole()));

        if (user instanceof SalesManagerUser){
            sb.append(String.format(format, "Sales Region",((SalesManagerUser)user).getSalesRegion()));
        }else if(user instanceof PurchaseManagerUser || user instanceof InventoryManager){
            sb.append(String.format(format, "Approve Limit",((PurchaseManagerUser)user).getApprovalLimit()));
        }
        
        usertxtarea.setText(sb.toString());
        
        btnfirst.setEnabled(currentUserIndex >0);
        btnprevious.setEnabled(currentUserIndex >0);
        btnnext.setEnabled(currentUserIndex <allUsers.size()-1);
        btnlast.setEnabled(currentUserIndex <allUsers.size()-1);

    }
    
    //private void adduser(){
      //  useridtxt.setText(generateUserid());
    //}
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
        header = new javax.swing.JLabel();
        btnBack = new javax.swing.JButton();
        btnExit = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        useridtxt = new javax.swing.JTextField();
        rolebox = new javax.swing.JComboBox<>();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        usernametxt = new javax.swing.JTextField();
        jLabel27 = new javax.swing.JLabel();
        userphntxt = new javax.swing.JTextField();
        useremailtxt = new javax.swing.JTextField();
        useraddresstxt = new javax.swing.JTextField();
        btnsave = new javax.swing.JButton();
        passwdtxt = new javax.swing.JPasswordField();
        btnedit = new javax.swing.JButton();
        btndelete = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        saleregionbox = new javax.swing.JComboBox<>();
        jLabel3 = new javax.swing.JLabel();
        approvelimitbox = new javax.swing.JComboBox<>();
        jScrollPane1 = new javax.swing.JScrollPane();
        usertxtarea = new javax.swing.JTextArea();
        btnfirst = new javax.swing.JButton();
        btnprevious = new javax.swing.JButton();
        btnnext = new javax.swing.JButton();
        btnlast = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(152, 202, 232));
        jPanel1.setPreferredSize(new java.awt.Dimension(956, 525));

        jPanel2.setBackground(new java.awt.Color(200, 224, 235));
        jPanel2.setPreferredSize(new java.awt.Dimension(940, 50));

        header.setFont(new java.awt.Font("Sitka Text", 1, 24)); // NOI18N
        header.setText("OWSB Purchase Order Management System");
        header.setToolTipText("");

        btnBack.setBackground(new java.awt.Color(102, 102, 102));
        btnBack.setFont(new java.awt.Font("Serif", 1, 12)); // NOI18N
        btnBack.setForeground(new java.awt.Color(255, 255, 255));
        btnBack.setText("Back");
        btnBack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBackActionPerformed(evt);
            }
        });

        btnExit.setBackground(new java.awt.Color(102, 102, 102));
        btnExit.setFont(new java.awt.Font("Serif", 1, 12)); // NOI18N
        btnExit.setForeground(new java.awt.Color(255, 255, 255));
        btnExit.setText("Exit");
        btnExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExitActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(49, 49, 49)
                .addComponent(btnBack)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 163, Short.MAX_VALUE)
                .addComponent(header, javax.swing.GroupLayout.PREFERRED_SIZE, 599, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnExit)
                .addGap(25, 25, 25))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(10, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(header)
                    .addComponent(btnBack)
                    .addComponent(btnExit))
                .addGap(9, 9, 9))
        );

        jPanel3.setBackground(new java.awt.Color(200, 224, 235));

        jLabel12.setFont(new java.awt.Font("Segoe UI", 3, 16)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(0, 0, 204));
        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel12.setText("Register");

        jLabel21.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel21.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel21.setText("User ID: ");

        jLabel22.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel22.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel22.setText("Password: ");

        jLabel23.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel23.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel23.setText("Username: ");

        useridtxt.setEditable(false);
        useridtxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useridtxtActionPerformed(evt);
            }
        });

        rolebox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Admin", "Sales Manager", "Purchase Manager", "Inventory Manager", "Finance Manager" }));
        rolebox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                roleboxActionPerformed(evt);
            }
        });

        jLabel24.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel24.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel24.setText("User Address:");

        jLabel25.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel25.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel25.setText("User Phone Number: ");

        jLabel26.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel26.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel26.setText("User Email: ");

        usernametxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                usernametxtActionPerformed(evt);
            }
        });

        jLabel27.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel27.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel27.setText("Role: ");

        userphntxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                userphntxtActionPerformed(evt);
            }
        });

        useremailtxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useremailtxtActionPerformed(evt);
            }
        });

        useraddresstxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useraddresstxtActionPerformed(evt);
            }
        });

        btnsave.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnsave.setText("Save");
        btnsave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnsaveActionPerformed(evt);
            }
        });

        passwdtxt.setText("passwd");
        passwdtxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                passwdtxtActionPerformed(evt);
            }
        });

        btnedit.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnedit.setText("Edit");
        btnedit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btneditActionPerformed(evt);
            }
        });

        btndelete.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btndelete.setText("Delete");
        btndelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btndeleteActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(137, 137, 137)
                        .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                            .addGap(57, 57, 57)
                            .addComponent(btndelete)
                            .addGap(18, 18, 18)
                            .addComponent(btnsave)
                            .addGap(18, 18, 18)
                            .addComponent(btnedit))
                        .addGroup(jPanel3Layout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(jLabel25, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel26, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel24, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel27, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGap(18, 18, 18)
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(useraddresstxt)
                                .addComponent(rolebox, 0, 217, Short.MAX_VALUE)
                                .addComponent(useremailtxt)
                                .addComponent(userphntxt)
                                .addComponent(usernametxt)
                                .addComponent(useridtxt)
                                .addComponent(passwdtxt)))))
                .addContainerGap(77, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(39, 39, 39)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(useridtxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(22, 22, 22)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(passwdtxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(usernametxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel25, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(userphntxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel26, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(useremailtxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel24, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(useraddresstxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel27, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rolebox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 40, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnsave)
                    .addComponent(btnedit)
                    .addComponent(btndelete))
                .addGap(26, 26, 26))
        );

        jPanel4.setBackground(new java.awt.Color(200, 224, 235));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel2.setText("Sales Region: ");

        saleregionbox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Kuala Lumpur", "Shah Alam", "Klang Valley", "Putrajaya", "Damansara" }));
        saleregionbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saleregionboxActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel3.setText("Approve Limit: ");

        approvelimitbox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" }));
        approvelimitbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                approvelimitboxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(56, 56, 56)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel3)
                    .addComponent(jLabel2))
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(saleregionbox, javax.swing.GroupLayout.PREFERRED_SIZE, 191, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(approvelimitbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(43, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(saleregionbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(approvelimitbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addContainerGap(22, Short.MAX_VALUE))
        );

        usertxtarea.setColumns(20);
        usertxtarea.setRows(5);
        jScrollPane1.setViewportView(usertxtarea);

        btnfirst.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnfirst.setText("First");
        btnfirst.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnfirstActionPerformed(evt);
            }
        });

        btnprevious.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnprevious.setText("Previous");
        btnprevious.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnpreviousActionPerformed(evt);
            }
        });

        btnnext.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnnext.setText("Next");
        btnnext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnnextActionPerformed(evt);
            }
        });

        btnlast.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnlast.setText("Last");
        btnlast.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnlastActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 986, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnfirst)
                        .addGap(18, 18, 18)
                        .addComponent(btnprevious)
                        .addGap(18, 18, 18)
                        .addComponent(btnnext)
                        .addGap(18, 18, 18)
                        .addComponent(btnlast)
                        .addGap(46, 46, 46))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 397, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(38, Short.MAX_VALUE))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 273, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnfirst)
                            .addComponent(btnprevious)
                            .addComponent(btnnext)
                            .addComponent(btnlast))))
                .addGap(0, 52, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 986, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 569, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackActionPerformed
        new mainlogin().setVisible(true);
        this.dispose();
    }//GEN-LAST:event_btnBackActionPerformed

    private void btnsaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnsaveActionPerformed
        // TODO add your handling code here:
        String userid = useridtxt.getText();
        String username = usernametxt.getText();
        String passwd = new String (passwdtxt.getPassword());
        String phn = userphntxt.getText();
        String email = useremailtxt.getText();
        String address = useraddresstxt.getText();
        String role = rolebox.getSelectedItem().toString();
        //User newuser = new User(userid, username, passwd, phn, email, address, role);
        //String userid = generateUserid(role);
        
        User newuser;
        switch(role){//user
            case "Admin":
                newuser = new Administrator(userid, username,passwd, phn, email, address,role);
                break;
            case "Sales Manager":
                String region = saleregionbox.getSelectedItem().toString();
                newuser = new SalesManagerUser(userid, username,passwd, phn, email, address,role,region);
                break;
            case "Purchase Manager":
                String approvelimit = approvelimitbox.getSelectedItem().toString();
                newuser = new PurchaseManagerUser(userid, username,passwd, phn, email, address,role,approvelimit);
                break;
            case "Inventory Manager":
                String applimit = approvelimitbox.getSelectedItem().toString();
                newuser = new InventoryManager(userid, username,passwd, phn, email, address,role,applimit);
                break;
            case "Finance Manager":
                newuser = new FinanceManager(userid, username,passwd, phn, email, address,role);
                break;
            default:
                JOptionPane.showMessageDialog(this, "error save");
                return;

        }
        if(newuser instanceof Administrator){
            if(((Administrator)newuser).adduser()){
                JOptionPane.showMessageDialog(this, "add user");
                clearfileds();
                useridtxt.setText(generateUserid(role));
            }else{
                JOptionPane.showMessageDialog(this, "error add user");
            }
        }
        loadAllUsers();
    }//GEN-LAST:event_btnsaveActionPerformed

    private void useraddresstxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useraddresstxtActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_useraddresstxtActionPerformed

    private void passwdtxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_passwdtxtActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_passwdtxtActionPerformed

    private void usernametxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_usernametxtActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_usernametxtActionPerformed

    private void btndeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btndeleteActionPerformed
        // TODO add your handling code here:
        String userid = useridtxt.getText();
        User user = User.finduserid(userid);
        
        if (user instanceof Administrator){
            if (((Administrator)user).deleteuser(userid)){
                JOptionPane.showMessageDialog(this, "deleted");
                clearfileds();
            }else{
                JOptionPane.showMessageDialog(this, "error delete");
            }
        }
        loadAllUsers();
    }//GEN-LAST:event_btndeleteActionPerformed

    private void btnlastActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnlastActionPerformed
        // TODO add your handling code here:
        currentUserIndex = allUsers.size()-1;
        displayCurrentUser();
    }//GEN-LAST:event_btnlastActionPerformed

    private void btnpreviousActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnpreviousActionPerformed
        // TODO add your handling code here:
        if(currentUserIndex > 0){
            currentUserIndex--;
            displayCurrentUser();
        }
    }//GEN-LAST:event_btnpreviousActionPerformed

    private void btnfirstActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnfirstActionPerformed
        // TODO add your handling code here:
        currentUserIndex = 0;
        displayCurrentUser();
    }//GEN-LAST:event_btnfirstActionPerformed

    private void btnnextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnnextActionPerformed
        // TODO add your handling code here:
        if(currentUserIndex < allUsers.size()-1){
            currentUserIndex++;
            displayCurrentUser();
        }
    }//GEN-LAST:event_btnnextActionPerformed

    private void useridtxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useridtxtActionPerformed
        // TODO add your handling code here:
        //useridtxt.setText(generateUserid());
    }//GEN-LAST:event_useridtxtActionPerformed

    private void userphntxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_userphntxtActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_userphntxtActionPerformed

    private void useremailtxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useremailtxtActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_useremailtxtActionPerformed

    private void roleboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_roleboxActionPerformed
        // TODO add your handling code here:
        String selectedrole = rolebox.getSelectedItem().toString();
        extradetails(selectedrole);
        useridtxt.setText(generateUserid(selectedrole));
    }//GEN-LAST:event_roleboxActionPerformed

    private void btneditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btneditActionPerformed
        // TODO add your handling code here:
        String userid = useridtxt.getText();
        User user = User.finduserid(userid);
        
        if (user !=null && user instanceof Administrator){
            user.username=usernametxt.getText();//username,passwd, phn, email, address,role
            user.userPw = new String(passwdtxt.getPassword());
            user.userPhone=userphntxt.getText();
            user.userEmail=useremailtxt.getText();
            user.userAddress=useraddresstxt.getText();
            user.userRole=rolebox.getSelectedItem().toString();
            if (((Administrator)user).edituser()){
                JOptionPane.showMessageDialog(this, "edited");
            }else{
                JOptionPane.showMessageDialog(this, "error edit");
            }
        }
        loadAllUsers();
    }//GEN-LAST:event_btneditActionPerformed

    private void saleregionboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saleregionboxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_saleregionboxActionPerformed

    private void approvelimitboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_approvelimitboxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_approvelimitboxActionPerformed

    private void btnExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExitActionPerformed
        // TODO add your handling code here:
        System.exit(0);
    }//GEN-LAST:event_btnExitActionPerformed

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
            java.util.logging.Logger.getLogger(adminpage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(adminpage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(adminpage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(adminpage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new adminpage().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> approvelimitbox;
    private javax.swing.JButton btnBack;
    private javax.swing.JButton btnExit;
    private javax.swing.JButton btndelete;
    private javax.swing.JButton btnedit;
    private javax.swing.JButton btnfirst;
    private javax.swing.JButton btnlast;
    private javax.swing.JButton btnnext;
    private javax.swing.JButton btnprevious;
    private javax.swing.JButton btnsave;
    private javax.swing.JLabel header;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPasswordField passwdtxt;
    private javax.swing.JComboBox<String> rolebox;
    private javax.swing.JComboBox<String> saleregionbox;
    private javax.swing.JTextField useraddresstxt;
    private javax.swing.JTextField useremailtxt;
    private javax.swing.JTextField useridtxt;
    private javax.swing.JTextField usernametxt;
    private javax.swing.JTextField userphntxt;
    private javax.swing.JTextArea usertxtarea;
    // End of variables declaration//GEN-END:variables
}
