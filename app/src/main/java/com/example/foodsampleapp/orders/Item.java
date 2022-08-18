/**
 * Encapsulates data relating to an item, including Name, Instructions for Preparing, Price, and UPC.
 */

package com.example.foodsampleapp.orders;

public class Item {

    private String name;
    private String[] instructions;
    private double price;
    private int upc;

    private static int nextUPC = 1;

    public Item(String name, String[] instructions, double price){
        this.name = name;
        this.instructions = instructions;
        this.price = price;
        this.upc = nextUPC++;
    }

    /**
     * Creates an item where instructions is a semicolon separated list of instructions
     * @param name
     * @param instructions
     * @param price
     */
    public Item(String name, String instructions, double price){
        this.name = name;
        this.instructions = stripAll(instructions.split(";"));
        this.price = price;
        this.upc = nextUPC++;
    }


    public String getName(){
        return this.name;
    }

    public double getPrice(){
        return this.price;
    }

    public String[] getInstructions(){
        return this.instructions;
    }

    public int getUpc(){
        return this.upc;
    }

    /**
     * Compares two items by their UPC code.
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj){
        if(obj instanceof Item){
            return this.upc == ((Item)obj).upc;
        }
        return false;
    }

    /**
     * Overrides the hash code method so that each item in an order has its upc as the key.
     * @return
     */
    @Override
    public int hashCode(){
        return this.upc;
    }

    /**
     * Strips leading and trailing whitespace from every entry in a String array. Used on the instructions.
     * @param arr
     * @return
     */
    private static String[] stripAll(String[] arr){
        for (int i = 0; i < arr.length; i++) {
            arr[i] = arr[i].trim();
        }
        return arr;
    }

    /**
     * Provides a String representations of the Item.
     * @return  String of the form "[name] (UPC: [upc])".
     */
    @Override
    public String toString(){
        return this.name + " (UPC: " + this.upc + ")";
    }

    public static void main(String[] args) {
        Item i = new Item("Hamburger", "Chop Onions; Place Burger on Bun; Add Ketchup; Place Onions; Top Bun", 4.50);
        for(int j=0; j<i.getInstructions().length; j++){
            System.out.println(i.getInstructions()[j]);
        }
    }
}
