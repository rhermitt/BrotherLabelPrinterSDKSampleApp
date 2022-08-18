/**
 * Represents an individual item that has been ordered. Corresponds to one Item and can then be
 * given special instructions.
 */

package com.example.foodsampleapp.orders;

import java.util.Arrays;

public class OrderedItem {

    private Item item;
    private String[] specialInstructions;

    /**
     * Constructor, creates the OrderedItem by associating it with the given item and assigning its
     * special instructions.
     * @param item
     * @param specialInstructions
     */
    public OrderedItem(Item item, String[] specialInstructions){
        this.item = item;
        this.specialInstructions = specialInstructions;
    }

    public Item getItem(){
        return this.item;
    }

    public String getName(){
        return this.item.getName();
    }

    public String[] getSpecialInstructions(){
        return this.specialInstructions;
    }

    /**
     * Converts the special instructions array to a semicolon separated string.
     * @return  String containing the special instructions, semicolon separated.
     */
    public String getSpecialInstructionsString(){
        String instructions = this.specialInstructions[0];
        for(int i=1; i<this.specialInstructions.length; i++){
            instructions += ("; " + this.specialInstructions[i]);
        }
        return instructions;
    }

    public double getPrice(){
        return this.item.getPrice();
    }

    public int getUpc(){
        return this.item.getUpc();
    }

    /**
     * Gets the same string representation as the associated item.
     * @return  String form of the OrderedItem.
     */
    @Override
    public String toString(){
        return this.item.toString();
    }

    /**
     * Standardizes the hash code of the OrderedItem so that two instances corresponding to the
     * same item with the same special instructions have the same hash code.
     * @return  The hash code.
     */
    @Override
    public int hashCode(){
        return 11 * this.item.hashCode() * Arrays.hashCode(this.specialInstructions);
    }

    /**
     * Determines if there are special instructions, since the array may be initialized but have no
     * actual information in it.
     * @return  True if any entry in the specialInstructions is not the empty String.
     */
    public boolean hasSpecialInstructions(){
        for(String instruction : this.specialInstructions){
            if(!instruction.equals("")){
                return true;
            }
        }
        return false;
    }

    /**
     * Compares for equality.
     * @param obj   Object to compare against.
     * @return  True if the two instances correspond to the same item and have the same special instructions in the same order.
     *          False otherwise.
     */
    @Override
    public boolean equals(Object obj){
        if(obj instanceof OrderedItem){
            OrderedItem comp = (OrderedItem) obj;
            boolean sameInstructions = true;
            if(this.specialInstructions.length != comp.specialInstructions.length){
                return false;
            }
            for (int i = 0; i < this.specialInstructions.length; i++) {
                if(!this.specialInstructions[i].equals(comp.specialInstructions[i])){
                    sameInstructions = false;
                    break;
                }
            }
            return (this.item.getUpc() == comp.item.getUpc()) && sameInstructions;
        }
        return false;
    }
}
