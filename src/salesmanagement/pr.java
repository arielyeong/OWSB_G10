package salesmanagement;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 *
 * @author charlotte
 */
public class pr {
    private String prId;
    private String smId;
    private String supplierId;
    private List<PrItem> items;
    private String prStatus;
    private LocalDate createdDate;
    private LocalDate requiredDate;
    
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public pr(String prId, String smId, String supplierId, List<PrItem> items, String prStatus, LocalDate createdDate, LocalDate requiredDate) {
        this.prId = prId;
        this.smId = smId;
        this.supplierId = supplierId;
        this.items = items;
        this.prStatus = prStatus;
        this.createdDate = createdDate;
        this.requiredDate = requiredDate;
    }


    
    

    public String getPrId() {
        return prId;
    }

    public void setPrId(String prId) {
        this.prId = prId;
    }

    public String getSmId() {
        return smId;
    }

    public void setSmId(String smId) {
        this.smId = smId;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId;
    }

    public List<PrItem> getItems() {
        return items;
    }

    public void setItems(ArrayList<PrItem> items) {
        this.items = items;
    }

    public double getTotalCost() {
        int totalCost = 0;
        for (PrItem prItem : items) {
            totalCost += prItem.getItem().getItemUnitPrice() * prItem.getQuantity();
        }
        return totalCost;
    }

    public String getPrStatus() {
        return prStatus;
    }

    public void setPrStatus(String prStatus) {
        this.prStatus = prStatus;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDate getRequiredDate() {
        return requiredDate;
    }

    public void setRequiredDate(LocalDate requiredDate) {
        this.requiredDate = requiredDate;
    }
    
    public static class PrItem {
        private Item item;
        private int quantity; //buying quantity

        public PrItem(Item item, int quantity) {
            this.item = item;
            this.quantity = quantity;
        }

        public void setItem(Item item) {
            this.item = item;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public Item getItem() {
            return item;
        }

        public int getQuantity() {
            return quantity;
        }
//
//        public int getCost() {
//            return item.getItemUnitPrice() * quantity;
//        }
//
//        @Override
//        public String toString() {
//            return item.getItemId() + ":" + quantity;
//        }
    }
    
    private String itemsToFileString() {
        StringBuilder sb = new StringBuilder();
        for (PrItem prItem : items) {
            sb.append(String.format("[item=%s, quantity=%d, totalcost=%.2f]",
                prItem.getItem().getItemId(),
                prItem.getQuantity(),
                prItem.getItem().getItemUnitPrice() * prItem.getQuantity()
            ));
        }
        return sb.toString();
    }

    
    public String toFileString() {
        return String.format("%s|%s|%s|%s|%s|%s",
            prId,
            smId,
            supplierId,
            itemsToFileString(),
            prStatus,
            createdDate.format(DATE_FORMATTER),
            requiredDate.format(DATE_FORMATTER));
    }
    
    public static pr fromFileString(String fileString, List<Item> allItems) {
        String[] parts = fileString.split("\\|");
        if (parts.length < 7) {
            throw new IllegalArgumentException("Invalid PR format: " + fileString);
        }

        String prId = parts[0];
        String smId = parts[1];
        String supplierId = parts[2];
        String itemSection = parts[3];
        String prStatus = parts[4];
        LocalDate createdDate = LocalDate.parse(parts[5], DATE_FORMATTER);
        LocalDate requiredDate = LocalDate.parse(parts[6], DATE_FORMATTER);

        List<PrItem> prItems = new ArrayList<>();

        // Split the item section into blocks like: [item=..., quantity=..., ...]
        Pattern itemPattern = Pattern.compile("\\[item=([^,]+),\\s*quantity=(\\d+),\\s*totalcost=([\\d.]+)]");
        Matcher matcher = itemPattern.matcher(itemSection);

        while (matcher.find()) {
            String itemId = matcher.group(1);
            int quantity = Integer.parseInt(matcher.group(2));

            Item matchedItem = allItems.stream()
                .filter(item -> item.getItemId().equals(itemId))
                .findFirst()
                .orElse(null);

            if (matchedItem != null) {
                prItems.add(new PrItem(matchedItem, quantity));
            }
        }

        return new pr(prId, smId, supplierId, prItems, prStatus, createdDate, requiredDate);
    }



    public static void main(String[] args) {
        
    }
    
}
