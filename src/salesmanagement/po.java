package salesmanagement;

/**
 *
 * @author charlotte
 */
import java.util.Date;
import java.util.ArrayList;

public class po {
    private String poId;
    private String prId;
    private String pmId;
    private String smId;
    private String supplierId;
    private ArrayList<Item> items; 
    private int itemQuantity;
    private int totalCost;
    private String poStatus;
    private Date createdDate;
    private Date orderDate;
    private Date deliveryDate;
    private Date invoiceDate;
    
}
