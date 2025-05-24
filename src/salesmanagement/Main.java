/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package salesmanagement;

/**
 *
 * @author Yeong Huey Yee
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
         itementry item = new itementry();
         item.setLocationRelativeTo(null);
         item.setVisible(true);
         
         supplierentry supplier = new supplierentry();
         supplier.setLocationRelativeTo(null);
         supplier.setVisible(true);
    }
    
}
