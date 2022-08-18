/**
 * Activity containing the navigation choices for customers.
 */

package com.example.foodsampleapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Activity_CustomerMenu extends AppCompatActivity {

    /**
     * Assigns the On Click Listeners to the buttons and shifts to the proper activity.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_menu);

        Button orderFood = findViewById(R.id.btn_orderFood);
        orderFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent orderItemIntent = new Intent(Activity_CustomerMenu.this, Activity_OrderItems.class);
                startActivity(orderItemIntent);
            }
        });

        Button viewBasket = findViewById(R.id.btn_viewBasket);
        viewBasket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent viewBasketIntent = new Intent(Activity_CustomerMenu.this, Activity_ViewBasket.class);
                startActivity(viewBasketIntent);
            }
        });
    }
}