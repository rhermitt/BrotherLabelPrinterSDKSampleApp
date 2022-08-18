/**
 * Adapter used for the recycler view with which the user can view items.
 */

package com.example.foodsampleapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodsampleapp.orders.Item;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class OrderingAdapter extends RecyclerView.Adapter<OrderingAdapter.OrderingHolder> {

    private Context context;
    private ArrayList<Item> items;

    /**
     * Creates a new Adapter with the given context and items.
     * @param context
     * @param items
     */
    public OrderingAdapter(Context context, ArrayList<Item> items){
        this.context = context;
        this.items = items;
    }

    /**
     * Inflates the view for one row of the recycler view.
     * @param parent
     * @param viewType
     * @return
     */
    @NonNull
    @Override
    public OrderingHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_row, parent, false);

        return new OrderingHolder(view);
    }

    /**
     * Sets the information corresponding to the given item.
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(@NonNull OrderingHolder holder, int position) {
        DecimalFormat MONEY = new DecimalFormat("$#,##0.00");
        holder.priceDisplay.setText(MONEY.format(items.get(position).getPrice()));
        holder.itemNameDisplay.setText(items.get(position).toString());
        holder.item = items.get(position);
    }

    /**
     * Gets the number of items available.
     * @return
     */
    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * Implements one row of the recycler view.
     */
    public static class OrderingHolder extends RecyclerView.ViewHolder{

        private TextView priceDisplay;
        private TextView itemNameDisplay;
        private Button addToOrderButton;
        private Item item;

        /**
         * Gets the views from the XML and binds the listener to the button.
         * @param orderingView
         */
        public OrderingHolder(@NonNull View orderingView){
            super(orderingView);
            priceDisplay = orderingView.findViewById(R.id.tv_productPrice);
            itemNameDisplay = orderingView.findViewById(R.id.tv_productName);
            addToOrderButton = orderingView.findViewById(R.id.btn_addToOrder);
            addToOrderButton.setOnClickListener(new addToOrder());
        }

        /**
         * Implements the button's onClickListener, which adds the proper item to the current order.
         */
        private class addToOrder implements View.OnClickListener {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), Activity_AddItemToBasket.class);
                intent.putExtra("ITEM_TO_ADD", OrderingHolder.this.item.getUpc());
                v.getContext().startActivity(intent);
            }
        }


    }
}
