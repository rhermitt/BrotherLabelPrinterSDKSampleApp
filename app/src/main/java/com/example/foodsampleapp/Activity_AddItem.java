/**
 * Activity that allows for adding an item to the list of those available.
 */

package com.example.foodsampleapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import android.view.View;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import com.example.foodsampleapp.orders.Item;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Activity_AddItem extends AppCompatActivity {

    private TableLayout instructions;
    private EditText itemName;
    private EditText priceInput;

    private ArrayList<EditText> instructionInputs;

    private static final int INVALID_NAME = 1;
    private static final int INVALID_PRICE = 2;

    /**
     * Initializes the views and sets up the instructions input.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        itemName = findViewById(R.id.tv_itemName);
        priceInput = findViewById(R.id.tv_price);

        setUpFirstInstruction();

    }

    /**
     * Adds a new instruction input box to the TableLayout.
     * @param view
     */
    public void onAddNewInstructionButtonClick(View view){
        TableRow newRow = new TableRow(this);
        newRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

        TextInputLayout newInput = new TextInputLayout(this);
        TextInputEditText newEditText = new TextInputEditText(newInput.getContext());
        newInput.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, (float)1.0));

        newInput.setHint("Instruction " + (instructionInputs.size() + 1));
        newInput.addView(newEditText, new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

        newRow.addView(newInput);
        instructions.addView(newRow, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
        instructionInputs.add(newEditText);
    }

    /**
     * Adds the item to the list of items.
     * @param view
     */
    public void onAddItemButtonClick(View view){
        String name = itemName.getText().toString();
        if(name.equals("")){
            invalidInputAlert(INVALID_NAME);
            return;
        }
        double price = 0;
        try {
            price = Double.parseDouble(priceInput.getText().toString());
        }
        catch (Exception e){
            invalidInputAlert(INVALID_PRICE);
            return;
        }

        if(price < 0){
            invalidInputAlert(INVALID_PRICE);
            return;
        }

        String[] newInstructions = new String[instructionInputs.size()];

        for (int i = 0; i < instructionInputs.size(); i++) {
            newInstructions[i] = instructionInputs.get(i).getText().toString();
        }

        Item newItem = new Item(name, newInstructions, price);
        MainActivity.availableItems.add(newItem);
        writeToStorageFile(newItem);
        Toast.makeText(getApplicationContext(), name + " Created", Toast.LENGTH_SHORT).show();

        itemName.getText().clear();
        priceInput.getText().clear();

        instructions.removeAllViews();
        setUpFirstInstruction();

    }

    /**
     * Writes the new item to the persistent internal storage.
     * @param item  The new item to write.
     */
    private void writeToStorageFile(Item item){
        String path = getFilesDir() + "" + File.separatorChar + "itemsforsale.csv";

        BufferedWriter writer = null;
        try {
            FileWriter fileWriter = new FileWriter(new File(path), true);
            writer = new BufferedWriter(fileWriter);
        }
        catch (IOException e){
            e.printStackTrace();
            return;
        }

        String info = item.getName() + "," + item.getPrice() + ',';
        for (int i = 0; i < item.getInstructions().length; i++) {
            info += item.getInstructions()[i];
            if(i != item.getInstructions().length - 1){
                info += ";";
            }
        }

        try {
            writer.write("\n");
            writer.write(info);
            writer.close();
        }
        catch(IOException e){
            e.printStackTrace();
            return;
        }
    }

    /**
     * Creates and shows an alert dialogue of the proper type based on the information that is invalid.
     * @param type  The reason for invalidity, can be either INVALID_NAME or INVALID_PRICE.
     */
    private void invalidInputAlert(int type){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.error);
        if(type == INVALID_NAME){
            alert.setMessage(R.string.invalid_name);
        }
        else if(type == INVALID_PRICE){
            alert.setMessage(R.string.invalid_price);
        }
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        AlertDialog dialog = alert.create();
        dialog.show();
    }

    /**
     * Puts the initial instruction input box inside the TableLayout.
     */
    private void setUpFirstInstruction(){
        instructions = findViewById(R.id.tl_placedOrders);

        instructionInputs = new ArrayList<>();

        TableRow firstRow = new TableRow(this);
        firstRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

        TextInputLayout firstInput = new TextInputLayout(this);
        TextInputEditText firstEditText = new TextInputEditText(firstInput.getContext());
        firstInput.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, (float)1.0));

        firstInput.setHint(R.string.instruction_one);
        firstInput.addView(firstEditText, new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

        firstRow.addView(firstInput);
        instructions.addView(firstRow, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));

        instructionInputs.add(firstEditText);
    }
}