package com.athulya.casiocafe;

public class Product {
    private String id;
    private String name;
    private double price;
    private String category;
    private String description;
    private String imageUrl;

    // Empty constructor required for Firebase
    public Product() {}

    public Product(String id, String name, double price, String category, String description, String imageUrl) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }

    // NEW Setter: Critical for updating and deleting!
    public void setId(String id) { this.id = id; }
}