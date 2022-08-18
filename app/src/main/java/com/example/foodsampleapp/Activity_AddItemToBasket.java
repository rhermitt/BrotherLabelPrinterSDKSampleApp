/**
 * Activity to add an OrderedItem to the basket.
 */

package com.example.foodsampleapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.foodsampleapp.orders.Item;
import com.example.foodsampleapp.orders.OrderedItem;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class Activity_AddItemToBasket extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private TableLayout specialInstructions;
    private ArrayList<EditText> instructionInputs;
    private TextView itemName;
    private TextView price;
    private Spinner quantitySpinner;
    private ArrayAdapter<Integer> spinnerAdapter;

    private int currentQuantity;

    private Item toAdd;

    /**
     * Gets the item to add from the intent and initializes the fields.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item_to_basket);

        Intent intent = getIntent();
        int upc = intent.getIntExtra("ITEM_TO_ADD", 0);
        toAdd = findItem(upc);

        itemName = findViewById(R.id.tv_itemToAdd);
        price = findViewById(R.id.tv_newItemPrice);

        DecimalFormat MONEY = new DecimalFormat("$#,##0.00");
        itemName.setText(toAdd.getName());
        price.setText(MONEY.format(toAdd.getPrice()));

        quantitySpinner = findViewById(R.id.spnr_quantityToAdd);
        setUpSpinner();

        setUpFirstInstruction();
    }

    /**
     * Sets up the quantity spinner using an array containing values from 1 to 10.
     */
    private void setUpSpinner(){
        ArrayList<Integer> quantities = new ArrayList<Integer>();
        for (int i = 1; i <= 10 ; i++) {
            quantities.add(i);
        }

        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, quantities);
        quantitySpinner.setAdapter(spinnerAdapter);
        quantitySpinner.setOnItemSelectedListener(this);
    }

    /**
     * Puts the initial instruction input box inside the TableLayout.
     */
    private void setUpFirstInstruction(){
        specialInstructions = findViewById(R.id.tl_placedOrders);

        instructionInputs = new ArrayList<>();

        TableRow firstRow = new TableRow(this);
        firstRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

        TextInputLayout firstInput = new TextInputLayout(this);
        TextInputEditText firstEditText = new TextInputEditText(firstInput.getContext());
        firstEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        firstInput.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, (float)1.0));

        firstInput.setHint(R.string.special_instruction_one);
        firstInput.addView(firstEditText, new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

        firstRow.addView(firstInput);
        specialInstructions.addView(firstRow, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));

        instructionInputs.add(firstEditText);
    }

    /**
     * Gets the Item object with the given UPC.
     * @param upc   The upc code of the item to find.
     * @return  The item with the given upc code.
     */
    private Item findItem(int upc){
        for(Item item : MainActivity.availableItems){
            if (item.getUpc() == upc){
                return item;
            }
        }
        return null;
    }

    /**
     * Creates a new Special Instructions Text Input and adds it to the TableView.
     * @param view
     */
    public void onAddSpecialInstructionButtonClick(View view){
        TableRow newRow = new TableRow(this);
        newRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

        TextInputLayout newInput = new TextInputLayout(this);
        TextInputEditText newEditText = new TextInputEditText(newInput.getContext());
        newEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        newInput.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, (float)1.0));

        newInput.setHint("Special Instruction " + (instructionInputs.size() + 1));
        newInput.addView(newEditText, new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

        newRow.addView(newInput);
        specialInstructions.addView(newRow, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
        instructionInputs.add(newEditText);
    }

    /**
     * Adds the item to the basket in the given quantity.
     * @param view
     */
    public void onAddItemToBasketButtonClick(View view){
        String[] specialInstructions = new String[instructionInputs.size()];
        for (int i = 0; i < instructionInputs.size(); i++) {
            specialInstructions[i] = instructionInputs.get(i).getText().toString();
        }
        OrderedItem newItem = new OrderedItem(toAdd, specialInstructions);
        MainActivity.currentOrder.add(newItem, currentQuantity);
        Log.d("", MainActivity.currentOrder.toString());
        finish();
    }

    /**
     * When a new item is selected in the spinner, the current quantity is updated to that value.
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        int selectedItem = (int)parent.getSelectedItem();
        currentQuantity = selectedItem;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}