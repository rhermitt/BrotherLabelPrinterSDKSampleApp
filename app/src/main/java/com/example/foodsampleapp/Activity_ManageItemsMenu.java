/**
 * Activity which implements the menu in which the vendor can choose to go to the Add Items Menu
 * or the Remove Items Menu.
 */

package com.example.foodsampleapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class Activity_ManageItemsMenu extends AppCompatActivity {

    /**
     * Assigns the On Click Listeners to the buttons and shifts to the proper activity.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_items_menu);
    }

    /**
     * Opens the Remove Items for Sale Activity.
     * @param view
     */
    public void onRemoveItemButtonClick(View view){
        Intent removeItemIntent = new Intent(Activity_ManageItemsMenu.this, Activity_RemoveItemForSale.class);
        startActivity(removeItemIntent);
    }

    /**
     * Opens the Add Item for Sale Activity.
     * @param view
     */
    public void onAddItemButtonClick(View view){
        Intent addItemIntent = new Intent(Activity_ManageItemsMenu.this, Activity_AddItem.class);
        startActivity(addItemIntent);
    }
}