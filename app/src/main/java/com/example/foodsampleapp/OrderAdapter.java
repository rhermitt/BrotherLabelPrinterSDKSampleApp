/**
 * Adapter that allows for an order, which is made up of a HashMap of items, to be displayed in a List View.
 */

package com.example.foodsampleapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.foodsampleapp.orders.Item;
import com.example.foodsampleapp.orders.OrderedItem;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OrderAdapter extends BaseAdapter {

    private ArrayList<Map.Entry<OrderedItem, Integer>> data;

    /**
     * Creates a new OrderAdapter of the given HashMap.
     * @param items The items in the order with their quantities.
     */
    public OrderAdapter(HashMap<OrderedItem, Integer> items){
        data = new ArrayList<Map.Entry<OrderedItem, Integer>>();
        data.addAll(items.entrySet());
    }

    /**
     * Gets the number of items in the order.
     * @return
     */
    @Override
    public int getCount(){
        return data.size();
    }

    /**
     * Gets the item at the given position.
     * @param position
     * @return
     */
    @Override
    public Map.Entry<OrderedItem, Integer> getItem(int position){
        return (Map.Entry<OrderedItem, Integer>) data.get(position);
    }

    /**
     * Gets the UPC of the item at the given position.
     * @param position
     * @return
     */
    @Override
    public long getItemId(int position){
        return data.get(position).getKey().getItem().getUpc();
    }

    /**
     * Gets the view for one row of the table.
     * @param position
     * @param convertView
     * @param parent
     * @return  A view displaying the item quantity, name, UPC, and price.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View result;

        if(convertView == null){
            result = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_adapter, parent, false);
        }
        else{
            result = convertView;
        }

        Map.Entry<OrderedItem, Integer> item = getItem(position);

        String quantity = item.getValue() + " X ";
        ((TextView) result.findViewById(R.id.tv_orderAdapter1)).setText(quantity);
        DecimalFormat MONEY = new DecimalFormat("$#,##0.00");
        String itemAndPrice = item.getKey().toString() + " - " + MONEY.format(item.getKey().getPrice()) + " each";
        ((TextView) result.findViewById(R.id.tv_orderAdapter2)).setText(itemAndPrice);
        String specialInstructions = "";
        if(item.getKey().hasSpecialInstructions()){
            specialInstructions += "Special Instructions:";
            for(String instruction : item.getKey().getSpecialInstructions()){
                specialInstructions += "\n\t" + instruction;
            }
        }
        ((TextView) result.findViewById(R.id.tv_orderAdapter3)).setText(specialInstructions);

        return result;
    }

}
