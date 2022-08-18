/**
 * Allows for storage of orders so they can be retrieved later.
 */

package com.example.foodsampleapp.orders;

import java.util.ArrayList;

public class OrdersDB {

    private ArrayList<Order> orders;

    public OrdersDB(){
        orders = new ArrayList<>();
    }

    public boolean add(Order o){
        return orders.add(o);
    }

    public boolean remove(Order o){
        return orders.remove(o);
    }

    /**
     * Gets a list containing all the order numbers of those orders that have been placed.
     * @return  ArrayList containing all of the order numbers stored in the database.
     */
    public ArrayList<Integer> getOrderNumbers(){
        ArrayList<Integer> numbers = new ArrayList<Integer>(orders.size());
        for(Order order : orders){
            numbers.add(order.getOrderNumber());
        }
        return numbers;
    }

    public ArrayList<Order> getOrders(){
        return this.orders;
    }

}
