/**
 * Activity that allows the vendor to remove an item from the list of available items.
 */

package com.example.foodsampleapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.foodsampleapp.orders.Item;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Activity_RemoveItemForSale extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private ListView itemsAvailable;
    private ArrayAdapter<String> itemAdapter;
    private ArrayList<String> names;

    /**
     * Sets up the List View to display the items for sale.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remove_item_for_sale);

        itemsAvailable = findViewById(R.id.lv_itemsInOrder);

        setUpListView();
        itemAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, names);

    }

    /**
     * Creates and assigns the adapter to the List View and sets the Listener.
     */
    private void setUpListView(){
        names = getAllItemNames();
        itemAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, names);
        itemsAvailable.setOnItemClickListener(this);
        itemsAvailable.setAdapter(itemAdapter);
    }

    /**
     * Since the toString Method of Item returns more than just the item names, this method is used
     * to get a list of just the names.
     * @return  List of all available item names.
     */
    private ArrayList<String> getAllItemNames(){
        ArrayList<String> itemNames = new ArrayList<String>(MainActivity.availableItems.size());
        for(Item item : MainActivity.availableItems){
            itemNames.add(item.getName());
        }
        return itemNames;
    }

    /**
     * When an item in the lsitview is clicked, it is removed from the list of available items.
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String name = (String)parent.getItemAtPosition(position);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.are_you_sure);
        alert.setMessage("Are you sure you want to remove " + name);
        alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                itemAdapter.remove(name);
                itemAdapter.notifyDataSetChanged();

                MainActivity.availableItems.remove(position);
                removeFromPersistentStorage(name);
                setUpListView();
            }
        });
        alert.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog dialogue = alert.create();
        dialogue.show();
    }

    /**
     * Removes the item from the persistent internal storage.
     * @param name
     */
    private void removeFromPersistentStorage(String name){
        String path = getFilesDir() + "" + File.separatorChar + "itemsforsale.csv";
        File originalFile = new File(path);
        File newFile = new File(getFilesDir() + "" + File.separatorChar + "itemsforsale2.csv");
        BufferedReader reader = null;
        BufferedWriter writer = null;
        try {
            FileReader fileReader = new FileReader(originalFile);
            reader = new BufferedReader(fileReader);
            FileWriter fileWriter = new FileWriter(newFile);
            writer = new BufferedWriter(fileWriter);
        }
        catch(Exception e){
            e.printStackTrace();
            return;
        }

        String line = "";
        boolean first = true;

        try{
            while((line = reader.readLine()) != null){
                String[] items = line.split(",");

                String existingName = items[0];
                if(first){
                    writer.write("Name,Price,Instructions");
                    first = false;
                    continue;
                }
                if(existingName.equals(name)){
                    continue;
                }

                writer.write("\n");
                writer.write(line);
            }
        }
        catch( IOException e){
            Log.e("", "Exception in delete when reading");
        }

        try {
            reader.close();
            writer.close();
        }
        catch (IOException e){
            Log.e("", "Exception in delete when closing");
        }

        boolean deleted = originalFile.delete();
        if(!deleted){
            Log.e("", "Problem deleting original");
            return;
        }

        boolean renamed = newFile.renameTo(originalFile);
        if(!renamed){
            Log.e("", "Problem Renaming");
            return;
        }
    }

}