package com.athulya.casiocafe;

public class OrderModel {
    private String documentId; // Firestore Document ID (Not saved in DB, just used locally)
    private String orderId;
    private String customerName;
    private String itemsDescription; // e.g., "1x Cappuccino, 2x Espresso"
    private double totalAmount;
    private String status; // "Pending", "Preparing", "Ready for Pickup"
    private long timestamp;

    // Empty constructor required by Firebase
    public OrderModel() { }

    public OrderModel(String orderId, String customerName, String itemsDescription, double totalAmount, String status, long timestamp) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.itemsDescription = itemsDescription;
        this.totalAmount = totalAmount;
        this.status = status;
        this.timestamp = timestamp;
    }

    // Getters
    public String getDocumentId() { return documentId; }
    public String getOrderId() { return orderId; }
    public String getCustomerName() { return customerName; }
    public String getItemsDescription() { return itemsDescription; }
    public double getTotalAmount() { return totalAmount; }
    public String getStatus() { return status; }
    public long getTimestamp() { return timestamp; }

    // Setters
    public void setDocumentId(String documentId) { this.documentId = documentId; }
    public void setStatus(String status) { this.status = status; }
}