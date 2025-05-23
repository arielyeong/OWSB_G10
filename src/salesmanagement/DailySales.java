package salesmanagement;

public class DailySales {
    private String salesId;
    private String itemId;
    private String itemName;
    private String date;
    private int quantitySold;
    private double totalPrice;


    // Constructor
    public DailySales(String salesId, String itemId, String itemName, String date, int quantitySold, double totalPrice) {
        this.salesId = salesId;
        this.itemId = itemId;
        this.itemName = itemName;
        this.date = date;
        this.quantitySold = quantitySold;
        this.totalPrice = totalPrice;
    }

    // Getters and Setters
    public String getSalesId() {
        return salesId;
    }

    public void setSalesId(String salesId) {
        this.salesId = salesId;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getQuantitySold() {
        return quantitySold;
    }

    public void setQuantitySold(int quantitySold) {
        this.quantitySold = quantitySold;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }


    
    

    // Convert to file format
    public String toFileString() {
        return salesId + "|" + itemId + "|" + itemName + "|" + date + "|" + quantitySold + "|" + totalPrice;
    }

    public static DailySales fromFileString(String fileString) {
        String[] parts = fileString.split("\\|");
        return new DailySales(
                parts[0],
                parts[1],
                parts[2],
                parts[3],
                Integer.parseInt(parts[4]),
                Double.parseDouble(parts[5])
        );
    }
}