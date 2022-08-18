/**
 * Activity in which the customer can view the items currently in their basket and place the order.
 */

package com.example.foodsampleapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.foodsampleapp.common.MsgDialog;
import com.example.foodsampleapp.common.MsgHandle;
import com.example.foodsampleapp.orders.Order;
import com.example.foodsampleapp.orders.OrderedItem;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;

public class Activity_ViewBasket extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private OrderAdapter adapter;
    private ListView basket;
    private EditText priceDisplay;
    private EditText orderName;

    MsgHandle mHandle;
    MsgDialog mDialog;

    /**
     * Sets up the initial price in the display and the List View.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_basket);

        priceDisplay = findViewById(R.id.tv_basketPrice);
        DecimalFormat MONEY = new DecimalFormat("$#,##0.00");
        priceDisplay.setText(MONEY.format(MainActivity.currentOrder.getTotalPrice()));

        orderName = findViewById(R.id.tv_orderName);

        basket = findViewById(R.id.lv_currentBasketList);
        setUpListViewAdapter();

        mDialog = new MsgDialog(this);
        mHandle = new MsgHandle(this, mDialog);
    }

    /**
     * Assigns the adapter to the ListView and assigns its listener.
     */
    private void setUpListViewAdapter(){
        adapter = new OrderAdapter(MainActivity.currentOrder.getItems());
        basket.setOnItemClickListener(this);
        basket.setAdapter(adapter);
    }

    /**
     * When an item is selected in the ListView, the user has a choice of how many they want to remove.
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Map.Entry<OrderedItem, Integer> selected = (Map.Entry<OrderedItem, Integer>) parent.getItemAtPosition(position);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.remove);
        ArrayList<Integer> quantities = getQuantitiesArray(selected.getValue());
        ArrayAdapter<Integer> quantitiesAdapter = new ArrayAdapter<Integer>(Activity_ViewBasket.this,
                                android.R.layout.simple_spinner_item, quantities);

        LayoutInflater inflater = this.getLayoutInflater();
        View alertView = inflater.inflate(R.layout.remove_item_dialogue, null);
        alert.setView(alertView);
        TextView itemSelected = alertView.findViewById(R.id.tv_itemToRemove);
        itemSelected.setText(selected.getKey().toString());
        Spinner quantity = alertView.findViewById(R.id.spnr_quantityToRemove);
        quantity.setAdapter(quantitiesAdapter);

        alert.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int quantitySelected = (Integer)quantity.getSelectedItem();
                MainActivity.currentOrder.remove(selected.getKey(), quantitySelected);
                setUpListViewAdapter();
                DecimalFormat MONEY = new DecimalFormat("$#,##0.00");
                priceDisplay.setText(MONEY.format(MainActivity.currentOrder.getTotalPrice()));
                Toast.makeText(getApplicationContext(), quantitySelected + " " + selected.getKey().toString() +
                                    " Removed", Toast.LENGTH_SHORT).show();
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog dialog = alert.create();
        dialog.show();
    }

    /**
     * Adds the newly placed order to the list of placed orders and creates a new order to be the
     * current order.
     * @param view
     */
    public void onPrintPlaceOrderButtonClick(View view){
        if(MainActivity.currentOrder.getItems().size() < 1){
            Toast.makeText(getApplicationContext(), "Order is Empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if(orderName.getText().toString().equalsIgnoreCase("")){
            Toast.makeText(getApplicationContext(), "Please Enter a Name", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = View.inflate(this, R.layout.date_time_picker, null);
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();

        dialogView.findViewById(R.id.btn_orderTimePickup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DatePicker datePicker = (DatePicker) dialogView.findViewById(R.id.date_picker);
                TimePicker timePicker = (TimePicker) dialogView.findViewById(R.id.time_picker);

                MainActivity.currentOrder.setCustomerName(orderName.getText().toString());
                LocalDateTime pickupTime  = LocalDateTime.of(datePicker.getYear(), datePicker.getMonth() + 1,
                        datePicker.getDayOfMonth(), timePicker.getHour(), timePicker.getMinute());

                if(pickupTime.isBefore(LocalDateTime.now())){
                    Toast.makeText(getApplicationContext(), "Pickup Time Must Be In The Future", Toast.LENGTH_SHORT).show();
                    alertDialog.dismiss();
                    return;
                }

                MainActivity.currentOrder.setPickupTime(pickupTime);
                MainActivity.currentOrder.setAsap(false);

                MainActivity.currentOrder.setPlacedTime(LocalDateTime.now());

                MainActivity.placedOrders.add(MainActivity.currentOrder);
                MainActivity.currentOrder = new Order();
                setUpListViewAdapter();
                DecimalFormat MONEY = new DecimalFormat("$#,##0.00");
                priceDisplay.setText(MONEY.format(0.0));
                orderName.getText().clear();

                alertDialog.dismiss();
                Activity_ViewBasket.this.finish();
            }});

        dialogView.findViewById(R.id.btn_asap).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                MainActivity.currentOrder.setCustomerName(orderName.getText().toString());
                MainActivity.currentOrder.setAsap(true);
                MainActivity.currentOrder.setPlacedTime(LocalDateTime.now());

                MainActivity.placedOrders.add(MainActivity.currentOrder);
                MainActivity.currentOrder = new Order();
                setUpListViewAdapter();
                DecimalFormat MONEY = new DecimalFormat("$#,##0.00");
                priceDisplay.setText(MONEY.format(0.0));
                orderName.getText().clear();

                alertDialog.dismiss();
                Activity_ViewBasket.this.finish();
            }
        });
        alertDialog.setView(dialogView);
        alertDialog.setTitle(R.string.set_pickup_time);
        alertDialog.show();

    }

    /**
     * Gets an array that will be used in the the alert for removing items.
     * @param i The number of items in the basket.
     * @return  An array containing all integer values in the range [1,i].
     */
    private ArrayList<Integer> getQuantitiesArray(int i){
        ArrayList<Integer> quantities = new ArrayList<>();
        for (int j = 1; j <= i; j++) {
            quantities.add(j-1,j);
        }
        return quantities;
    }
}