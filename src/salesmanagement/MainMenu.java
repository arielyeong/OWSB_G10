package salesmanagement;

import javax.swing.*;

public class MainMenu extends JFrame {

    public MainMenu() {
        setTitle("OWSB Purchase Order Management System");
        setSize(500, 400);
        setLayout(null);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        JLabel title = new JLabel("Main Menu", SwingConstants.CENTER);
        title.setBounds(100, 30, 300, 40);
        title.setFont(new java.awt.Font("Segoe UI", 1, 24));
        add(title);

        JButton btnItem = new JButton("Item Management");
        btnItem.setBounds(150, 100, 200, 40);
        add(btnItem);

        JButton btnSupplier = new JButton("Supplier Management");
        btnSupplier.setBounds(150, 150, 200, 40);
        add(btnSupplier);

        JButton btnSales = new JButton("Daily Sales Entry");
        btnSales.setBounds(150, 200, 200, 40);
        add(btnSales);
        
        JButton btnPr = new JButton("PR Entry");
        btnPr.setBounds(150, 250, 200, 40);
        add(btnPr);

        JButton btnExit = new JButton("Exit");
        btnExit.setBounds(150, 300, 200, 40);
        add(btnExit);

        // Action Listeners
        btnItem.addActionListener(e -> {
            new itementry().setVisible(true);
            dispose();
        });

        btnSupplier.addActionListener(e -> {
            new supplierentry().setVisible(true);
            dispose();
        });

        btnSales.addActionListener(e -> {
            new DailySalesEntry().setVisible(true);
            dispose();
        });
        
        btnPr.addActionListener(e -> {
            new prFrame().setVisible(true);
            dispose();
        });

        btnExit.addActionListener(e -> System.exit(0));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainMenu().setVisible(true);
        });
    }
}