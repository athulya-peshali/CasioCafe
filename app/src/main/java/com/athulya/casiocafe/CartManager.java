package com.athulya.casiocafe;

import java.util.ArrayList;

public class CartManager {
    private static CartManager instance;
    private ArrayList<CartItem> cartItems;

    private CartManager() {
        cartItems = new ArrayList<>();
    }

    public static CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public ArrayList<CartItem> getCartItems() {
        return cartItems;
    }

    public void addItem(Product product, int quantity) {
        // Check if the item already exists in the cart. If so, just increase the quantity.
        for (CartItem item : cartItems) {
            if (item.getProduct().getId().equals(product.getId())) {
                item.setQuantity(item.getQuantity() + quantity);
                return;
            }
        }
        // Otherwise, add it as a brand new row
        cartItems.add(new CartItem(product, quantity));
    }

    public void removeItem(CartItem item) {
        cartItems.remove(item);
    }

    public double getSubtotal() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getTotalPrice();
        }
        return total;
    }

    public void clearCart() {
        cartItems.clear();
    }
}