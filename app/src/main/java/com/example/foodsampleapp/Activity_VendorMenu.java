/**
 * Activity containing the options for the Vendor.
 */

package com.example.foodsampleapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.brother.sdk.lmprinter.Channel;
import com.brother.sdk.lmprinter.GetStatusError;
import com.brother.sdk.lmprinter.GetStatusResult;
import com.brother.sdk.lmprinter.OpenChannelError;
import com.brother.sdk.lmprinter.PrinterDriver;
import com.brother.sdk.lmprinter.PrinterDriverGenerateResult;
import com.brother.sdk.lmprinter.PrinterDriverGenerator;

public class Activity_VendorMenu extends AppCompatActivity {

    /**
     * Assigns the On Click Listeners to the buttons and shifts to the proper activity.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendor_menu);

        Button viewPlacedOrders = findViewById(R.id.btn_viewPlacedOrders);
        viewPlacedOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent placedOrdersIntent = new Intent(Activity_VendorMenu.this, Activity_PlacedOrders.class);
                startActivity(placedOrdersIntent);
            }
        });

        Button viewFulfilledOrders = findViewById(R.id.btn_viewFulfilledOrders);
        viewFulfilledOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent fulfilledOrdersIntent = new Intent(Activity_VendorMenu.this, Activity_FulfilledOrders.class);
                startActivity(fulfilledOrdersIntent);
            }
        });

        Button addItem = findViewById(R.id.btn_addItem);
        addItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newItemIntent = new Intent(Activity_VendorMenu.this, Activity_ManageItemsMenu.class);
                startActivity(newItemIntent);
            }
        });

        Button printerSettings = findViewById(R.id.btn_printerSettings);
        printerSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent printerSettingsIntent = new Intent(Activity_VendorMenu.this, Activity_PrinterSettings.class);
                startActivity(printerSettingsIntent);
            }
        });
    }
}