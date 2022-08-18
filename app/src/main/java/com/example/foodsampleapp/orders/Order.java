/**
 * Encapsulates data needed for an order, including the order number and the items in the order.
 */

package com.example.foodsampleapp.orders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;

public class Order {

    private HashMap<OrderedItem, Integer> items;
    private int orderNumber;
    private String customerName;
    private LocalDateTime pickupTime;
    private LocalDateTime placedTime;
    private boolean asap;

    private static int nextNumber = 1;

    /**
     * Creates an order with the current order number then increments the global order number.
     */
    public Order(){
        items = new HashMap<>();
        this.orderNumber = nextNumber++;
        this.pickupTime = null;
        this.placedTime = null;
        this.asap = true;
    }

    /**
     * Creates an order given a customer name.
     * @param name  The customer name given to the order.
     */
    public Order(String name){
        items = new HashMap<>();
        this.orderNumber = nextNumber++;
        this.customerName = name;
        this.pickupTime = null;
        this.placedTime = null;
        this.asap = true;
    }

    /**
     * Adds a new item to the order in the given quantity.
     * @param newItem
     * @param quantity
     * @return
     */
    public boolean add(OrderedItem newItem, int quantity){
        if(quantity <= 0){
            return false;
        }
        if(newItem == null){
            return false;
        }
        if(items.containsKey(newItem)){
            items.put(newItem, quantity + items.get(newItem));
        }
        else{
            items.put(newItem, quantity);
        }
        return true;
    }

    /**
     * Removes the given item in the given quantity.
     * @param item
     * @param quantityToRemove
     * @return  Returns false if quantityToRemove <= 0, quantityToRemove is larger than the amount currently in the order,
     *          or the item is not in the order at all.
     */
    public boolean remove(OrderedItem item, int quantityToRemove){
        if(quantityToRemove <= 0){
            return false;
        }
        if(this.items.containsKey(item)){
            int currentQuantity = this.items.get(item);
            if(quantityToRemove > currentQuantity){
                return false;
            }
            if(quantityToRemove == currentQuantity){
                this.items.remove(item);
                return true;
            }
            this.items.put(item, currentQuantity - quantityToRemove);
            return true;
        }
        return false;
    }

    /**
     * Gets a string representation.
     * @return  String of the form "[order number]: [list of items]"
     */
    public String toString(){
        return this.orderNumber + ": " + this.items;
    }


    public HashMap<OrderedItem, Integer> getItems(){
        return this.items;
    }

    /**
     * Computes the total price of all items in the order.
     * @return
     */
    public double getTotalPrice(){
        double price = 0.0;
        for(OrderedItem item : this.items.keySet()){
            price += item.getPrice() * this.items.get(item);
        }
        return price;
    }

    /**
     * Compares two orders based on their order number.
     * @param o
     * @return  True if the two orders have the same order number. False otherwise.
     */
    @Override
    public boolean equals(Object o){
        if(o instanceof Order){
            return this.orderNumber == ((Order) o).orderNumber;
        }
        return false;
    }

    public int getOrderNumber(){
        return this.orderNumber;
    }

    public String getCustomerName(){
        return this.customerName;
    }

    public void setCustomerName(String name){
        this.customerName = name;
    }

    public void setAsap(boolean a){
        this.asap = a;
    }

    public void setPickupTime(LocalDateTime ldt){
        this.pickupTime = ldt;
    }

    public void setPlacedTime(LocalDateTime ldt){
        this.placedTime = ldt;
    }

    public boolean getAsap(){
        return this.asap;
    }

    public LocalDateTime getPickupTime(){
        return this.pickupTime;
    }

    public LocalDateTime getPlacedTime(){
        return this.placedTime;
    }

    public static void main(String[] args) {
        Item i = new Item("Hamburger", "Chop Onions; Place Burger on Bun; Add Ketchup; Place Onions; Top Bun", 4.50);
        Item i2 = new Item("Hamburger", "Chop Onions; Place Burger on Bun; Add Ketchup; Place Onions; Top Bun", 4.50);
        Order o = new Order();
        String[] inst = {"No Ketchup"};
        OrderedItem oi1 = new OrderedItem(i, inst);
        o.add(oi1, 2);

        String[] inst2 = {"No Ketchup"};
        OrderedItem oi2 = new OrderedItem(i, inst2);
        o.add(oi2, 4);

        System.out.println(oi1.hashCode());
        System.out.println(oi2.hashCode());

        String[] inst3 = {"No Ketchup"};
        OrderedItem oi3 = new OrderedItem(i, inst3);
        o.remove(oi3, 3);

        for(OrderedItem key : o.getItems().keySet()) {
            System.out.println(key + " : " + o.getItems().get(key));
        }
    }
}
