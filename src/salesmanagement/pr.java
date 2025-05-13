package salesmanagement;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
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
    
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

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
            totalCost += prItem.getCost();
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

        public double getCost() {
            return item.getItemUnitPrice() * quantity;
        }

        @Override
        public String toString() {
            return item.getItemId() + "," + quantity;
        }
    }

    public String toFileString() {
        StringBuilder itemStr = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            PrItem pi = items.get(i);
            itemStr.append(pi.getItem().getItemId())
                   .append(",")
                   .append(pi.getQuantity());
            if (i < items.size() - 1) {
                itemStr.append(";");
            }
        }
        itemStr.append("]");

        return String.format("%s|%s|%s|%s|%s|%s|%s",
            prId,
            smId != null ? smId : "null",
            supplierId != null ? supplierId : "null",
            itemStr.toString(),
            prStatus,
            createdDate.format(DATE_FORMATTER),
            requiredDate.format(DATE_FORMATTER));
    }

    public static pr fromFileString(String fileString, List<Item> allItems) {
        try {
            String[] parts = fileString.split("\\|");
            if (parts.length != 7) {
                throw new IllegalArgumentException("Expected 7 parts but got " + parts.length);
            }

            String prId = parts[0];
            String smId = parts[1];
            String supplierId = "null".equals(parts[2]) ? null : parts[2];
            String itemSection = parts[3].trim();  // [I001,2;I002,3]
            String prStatus = parts[4];
            LocalDate createdDate = LocalDate.parse(parts[5], DATE_FORMATTER);
            LocalDate requiredDate = LocalDate.parse(parts[6], DATE_FORMATTER);

            List<PrItem> prItems = new ArrayList<>();

            // ✅ Remove brackets and only then split
            if (itemSection.startsWith("[") && itemSection.endsWith("]")) {
                itemSection = itemSection.substring(1, itemSection.length() - 1);
            }

            if (!itemSection.isEmpty()) {
                String[] itemPairs = itemSection.split(";");
                for (String pair : itemPairs) {
                    String[] itemData = pair.trim().split(",");
                    if (itemData.length == 2) {
                        String itemId = itemData[0].trim();
                        int quantity = Integer.parseInt(itemData[1].trim());

                        // Find item by ID from the item list
                        Item matchedItem = allItems.stream()
                            .filter(item -> item.getItemId().equals(itemId))
                            .findFirst()
                            .orElse(null);

                        if (matchedItem != null) {
                            prItems.add(new PrItem(matchedItem, quantity));
                        } else {
                            System.out.println("Item ID not found: " + itemId);
                        }
                    }
                }
            }

            return new pr(prId, smId, supplierId, prItems, prStatus, createdDate, requiredDate);

        } catch (Exception e) {
            throw new IllegalArgumentException(
                "Failed to parse PR: '" + fileString + "'. Error: " + e.getMessage(), e);
        }
    }

    
    public static void main(String[] args) {
        
    }
    
}
