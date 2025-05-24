package salesmanagement;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author charlotte
 */

public class po {
    private String poId;
    private String prId;
    private String pmId;
    private String smId;
    private String supplierId;
    private pr.PrItem item;
    private String poStatus;
    private LocalDate createdDate;
    private LocalDate orderDate;
    private LocalDate deliveryDate;
    private LocalDate invoiceDate;
    
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public po(String poId, String prId, String pmId, String smId, String supplierId, pr.PrItem item, String poStatus, LocalDate createdDate, LocalDate orderDate, LocalDate deliveryDate, LocalDate invoiceDate) {
        this.poId = poId;
        this.prId = prId;
        this.pmId = pmId;
        this.smId = smId;
        this.supplierId = supplierId;
        this.item = item;
        this.poStatus = poStatus;
        this.createdDate = createdDate;
        this.orderDate = orderDate;
        this.deliveryDate = deliveryDate;
        this.invoiceDate = invoiceDate;
    }


    

    public String getPoId() {
        return poId;
    }

    public void setPoId(String poId) {
        this.poId = poId;
    }

    public String getPrId() {
        return prId;
    }

    public void setPrId(String prId) {
        this.prId = prId;
    }

    public String getPmId() {
        return pmId;
    }

    public void setPmId(String pmId) {
        this.pmId = pmId;
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

    public pr.PrItem getItem() {
        return item;
    }

    public void setItem(pr.PrItem item) {
        this.item = item;
    }

    public String getPoStatus() {
        return poStatus;
    }

    public void setPoStatus(String poStatus) {
        this.poStatus = poStatus;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(LocalDate deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public LocalDate getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(LocalDate invoiceDate) {
        this.invoiceDate = invoiceDate;
    }
    
    public String toFileString() {
        String itemStr = String.format("[%s,%d]",
            item.getItem().getItemId(),
            item.getQuantity()
        );

        return String.format("%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s",
            poId,
            prId,
            pmId != null ? pmId : "null",
            smId != null ? smId : "null",
            supplierId,
            itemStr,
            poStatus,
            createdDate.format(DATE_FORMATTER),
            orderDate != null ? orderDate.format(DATE_FORMATTER) : "null",
            deliveryDate != null ? deliveryDate.format(DATE_FORMATTER) : "null",
            invoiceDate != null ? invoiceDate.format(DATE_FORMATTER) : "null"
        );
    }

    
    public static po fromFileString(String fileString, List<Item> allItems) {
        try {
            String[] parts = fileString.split("\\|");
            if (parts.length != 11) {
                throw new IllegalArgumentException("Expected 11 parts but got " + parts.length);
            }

            String poId = parts[0];
            String prId = parts[1];
            String pmId = "null".equals(parts[2]) ? null : parts[2];
            String smId = "null".equals(parts[3]) ? null : parts[3];
            String supplierId = parts[4];
            String itemSection = parts[5].trim();
            String poStatus = parts[6];
            LocalDate createdDate = LocalDate.parse(parts[7], DATE_FORMATTER);
            LocalDate orderDate = "null".equals(parts[8]) ? null : LocalDate.parse(parts[8], DATE_FORMATTER);
            LocalDate deliveryDate = "null".equals(parts[9]) ? null : LocalDate.parse(parts[9], DATE_FORMATTER);
            LocalDate invoiceDate = "null".equals(parts[10]) ? null : LocalDate.parse(parts[10], DATE_FORMATTER);

            if (orderDate != null) {
                if (deliveryDate != null && deliveryDate.isBefore(orderDate)) {
                    throw new IllegalArgumentException("Delivery date cannot be before order date.");
                }
                if (invoiceDate != null && invoiceDate.isBefore(orderDate)) {
                    throw new IllegalArgumentException("Invoice date cannot be before order date.");
                }
            }

            // Process the single item
            pr.PrItem prItem = null;
            if (itemSection.startsWith("[") && itemSection.endsWith("]")) {
                itemSection = itemSection.substring(1, itemSection.length() - 1); // Remove brackets
            }

            if (!itemSection.isEmpty()) {
                String[] itemData = itemSection.split(",");
                if (itemData.length == 2) {
                    String itemId = itemData[0].trim();
                    int quantity = Integer.parseInt(itemData[1].trim());

                    Item matchedItem = allItems.stream()
                        .filter(item -> item.getItemId().equals(itemId))
                        .findFirst()
                        .orElse(null);

                    if (matchedItem != null) {
                        prItem = new pr.PrItem(matchedItem, quantity);
                    } else {
                        System.out.println("Item ID not found: " + itemId);
                    }
                }
            }

            return new po(poId, prId, pmId, smId, supplierId, prItem, poStatus,
                          createdDate, orderDate, deliveryDate, invoiceDate);

        } catch (Exception e) {
            throw new IllegalArgumentException(
                "Failed to parse PO: '" + fileString + "'. Error: " + e.getMessage(), e);
        }
    }



    
}
