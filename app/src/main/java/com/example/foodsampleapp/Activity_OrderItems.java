/**
 * Activity containing the interface that allows customers to order items.
 */

package com.example.foodsampleapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class Activity_OrderItems extends AppCompatActivity {

    /**
     * Sets up the Recycler View.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_items);

        RecyclerView rcView = findViewById(R.id.rv_itemsRecyclerView);
        OrderingAdapter adapter = new OrderingAdapter(this, MainActivity.availableItems);
        rcView.setAdapter(adapter);
        rcView.setLayoutManager(new LinearLayoutManager(this));
    }

    /**
     * Sends the user to their current basket when the View Current Basket Button is clicked.
     * @param view
     */
    public void onViewBasketButtonClick(View view){
        finish();
        Intent basketIntent = new Intent(Activity_OrderItems.this, Activity_ViewBasket.class);
        startActivity(basketIntent);
    }
}