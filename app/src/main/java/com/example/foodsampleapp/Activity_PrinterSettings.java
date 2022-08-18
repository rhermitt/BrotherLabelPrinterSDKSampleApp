/**
 * Displays the list for the general Printer Settings
 */

package com.example.foodsampleapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.brother.sdk.lmprinter.PrinterModel;
import com.brother.sdk.lmprinter.setting.PrintImageSettings;
import com.brother.sdk.lmprinter.setting.QLPrintSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Activity_PrinterSettings extends AppCompatActivity implements AdapterView.OnItemClickListener{

    private SettingsAdapter settingsAdapter;
    private ListView settingsList;
    private Spinner connectionTypes;

    private ArrayAdapter<String> connectionTypeAdapter;

    private HashMap<String, Object> intents;
    private HashMap<String, ArrayList<String>> settingToOptions;
    public static ArrayList<String> checkedModelStrings;

    private ArrayList<PrinterModel> checkedModels;

    /**
     * Initializes the lists and maps and sets up the list view.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        intents = new HashMap<>();
        settingToOptions = new HashMap<>();
        checkedModelStrings = new ArrayList<>();
        checkedModels = new ArrayList<>();
        checkedModels.add(PrinterModel.QL_820NWB);
        checkedModelStrings.add(PrinterModel.QL_820NWB.name());

        setContentView(R.layout.activity_printer_settings);

        connectionTypes = findViewById(R.id.spnr_connectionTypes);
        connectionTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.connection_types));
        connectionTypes.setAdapter(connectionTypeAdapter);
        connectionTypes.setOnItemSelectedListener(new ConnectionSelect());
        setUpInitialSpinnerSelection();

        settingsList = findViewById(R.id.lv_printerSettings);
        setUpIntentsMap();
        setUpOptions();
        setUpListView(((String)connectionTypes.getSelectedItem()).equalsIgnoreCase("Wifi"));
    }

    private void setUpInitialSpinnerSelection(){
        if(MainActivity.activePrinterInfo.get("ipAddress") == null && MainActivity.activePrinterInfo.get("MacAddress") == null){
            connectionTypes.setSelection(0);
        }
        else if(MainActivity.activePrinterInfo.get("MacAddress") == null){
            connectionTypes.setSelection(0);
        }
        else{
            connectionTypes.setSelection(1);
        }
    }

    private class ConnectionSelect implements AdapterView.OnItemSelectedListener{

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            String selection = (String)parent.getSelectedItem();
            setUpListView(selection.equalsIgnoreCase("Wifi"));
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }


    /**
     * Sets up the list view with the current printer settings.
     */
    private void setUpListView(boolean wifi){
        if(wifi) {
            settingsAdapter = new SettingsAdapter(MainActivity.printerInfoSettings);
        }
        else{
            settingsAdapter = new SettingsAdapter(MainActivity.bluetoothPrinterInfoSettings);
        }
        settingsList.setAdapter(settingsAdapter);
        settingsList.setOnItemClickListener(this);
    }

    /**
     * Adds the information used in the intents map to go the the necessary Activity when the setting is chosen.
     */
    private void setUpIntentsMap(){
        intents.put("Find Printer", Activity_FindPrinter.class);
    }

    /**
     * Associates each setting with its possible choices.
     */
    private void setUpOptions(){
        ArrayList<String> onOff = new ArrayList<>(2);
        onOff.add("On");
        onOff.add("Off");


        ArrayList<String> labelSizes = new ArrayList<String>(QLPrintSettings.LabelSize.values().length);
        for(QLPrintSettings.LabelSize labelSize : QLPrintSettings.LabelSize.values()){
            labelSizes.add(labelSize.name());
        }

        ArrayList<String> halftones = new ArrayList<String>(QLPrintSettings.Halftone.values().length);
        for(QLPrintSettings.Halftone halftone : PrintImageSettings.Halftone.values()){
            halftones.add(halftone.name());
        }

        ArrayList<String> resolutions = new ArrayList<String>(QLPrintSettings.Resolution.values().length);
        for(QLPrintSettings.Resolution resolution : QLPrintSettings.Resolution.values()){
            resolutions.add(resolution.name());
        }

        ArrayList<String> models = new ArrayList<>();
        for(PrinterModel model : PrinterModel.values()){
            if(model.name().startsWith("QL")){
                models.add(model.name());
            }
        }

        settingToOptions.put("Manual Model", models);
        settingToOptions.put("Model", models);
        settingToOptions.put("Manual IP Address", null);
    }

    /**
     * When a setting is chosen, the proper options are displayed and the selection becomes the new setting.
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Map.Entry<String, String> chosen = (Map.Entry<String, String>)parent.getItemAtPosition(position);
        ArrayList<String> choices = settingToOptions.get(chosen.getKey());

        if(chosen.getKey().equalsIgnoreCase("Model")){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Printer Model");
            boolean[] checkedItems = new boolean[settingToOptions.get("Model").size()];

            for (int i = 0; i < checkedItems.length; i++) {
                if(checkedModels.contains(PrinterModel.valueOf(settingToOptions.get("Model").get(i)))){
                    checkedItems[i] = true;
                }
            }

            builder.setMultiChoiceItems(settingToOptions.get("Model").toArray(new String[settingToOptions.get("Model").size()]), checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                    if(isChecked){
                        checkedModels.add(PrinterModel.valueOf(settingToOptions.get("Model").get(which)));
                        checkedModelStrings.add(settingToOptions.get("Model").get(which));
                    }
                    else{
                        checkedModels.remove(PrinterModel.valueOf(settingToOptions.get("Model").get(which)));
                        checkedModelStrings.remove(settingToOptions.get("Model").get(which));
                    }
                }
            });

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(checkedModels.isEmpty()){
                        Toast.makeText(getApplicationContext(), "No Model Selected", Toast.LENGTH_SHORT).show();
                        MainActivity.printerInfoSettings.put("Model", null);
                        setUpListView(true); //This is for models to search on wifi, so it can only happen if wifi is selected
                        return;
                    }
                    String selectedModels = checkedModelStrings.get(0);
                    for(int i=1; i<checkedModels.size(); i++){
                        selectedModels += "; " + checkedModelStrings.get(i);
                    }
                    MainActivity.printerInfoSettings.put("Model", selectedModels);
                    setUpListView(true);
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
        else if(chosen.getKey().equalsIgnoreCase("Manual Model")){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Printer Model");

            boolean wifi = ((String)connectionTypes.getSelectedItem()).equalsIgnoreCase("Wifi");

            builder.setSingleChoiceItems(settingToOptions.get("Manual Model").toArray(new String[settingToOptions.get("Manual Model").size()]), -1, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (wifi) {
                        MainActivity.printerInfoSettings.put("Manual Model", settingToOptions.get("Manual Model").get(which));
                    }
                    else{
                        MainActivity.bluetoothPrinterInfoSettings.put("Manual Model", settingToOptions.get("Manual Model").get(which));
                    }
                }
            });

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    MainActivity.printerInfoSettings.put("Find Printer", null);
                    MainActivity.printerInfoSettings.put("Model", null);

                    if(wifi){
                        MainActivity.bluetoothPrinterInfoSettings.put("MAC Address", null);
                        MainActivity.bluetoothPrinterInfoSettings.put("Manual Model", null);
                    }
                    else{
                        MainActivity.printerInfoSettings.put("Manual IP Address", null);
                        MainActivity.printerInfoSettings.put("Manual Model", null);
                    }
                    setUpListView(wifi);
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
        else if(choices != null){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(chosen.getKey());

            builder.setItems(choices.toArray(new CharSequence[choices.size()]), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(which == 0){
                        MainActivity.placedOrderPrinterSettings.put(chosen.getKey(), choices.get(which));
                    }
                    else{
                        MainActivity.placedOrderPrinterSettings.put(chosen.getKey(), choices.get(which));
                    }
                    setUpListView(true); //Only wifi has settings where this else clause will be entered
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
        else if(chosen.getKey().equalsIgnoreCase("Manual IP Address")){
            EditText ipInput = new EditText(this);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.manual_ip);
            builder.setView(ipInput);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    MainActivity.activePrinterInfo.put("ipAddress", ipInput.getText().toString());
                    MainActivity.printerInfoSettings.put("Manual IP Address", ipInput.getText().toString());
                    MainActivity.printerInfoSettings.put("Find Printer", null);
                    MainActivity.printerInfoSettings.put("Model", null);
                    MainActivity.bluetoothPrinterInfoSettings.put("MAC Address", null);
                    MainActivity.activePrinterInfo.put("MacAddress", null);
                    setUpListView(true);
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();

        }
        else if(chosen.getKey().equalsIgnoreCase("MAC Address")){
            EditText macInput = new EditText(this);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.enter_mac);
            builder.setView(macInput);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    MainActivity.activePrinterInfo.put("MacAddress", macInput.getText().toString());
                    MainActivity.bluetoothPrinterInfoSettings.put("MAC Address", macInput.getText().toString());
                    MainActivity.printerInfoSettings.put("Manual IP Address", null);
                    MainActivity.printerInfoSettings.put("Find Printer", null);
                    MainActivity.printerInfoSettings.put("Model", null);
                    MainActivity.activePrinterInfo.put("ipAddress", null);
                    setUpListView(false);
                }
            });

            AlertDialog alert = builder.create();
            alert.show();
        }
        else {
            String settingChosen = chosen.getKey();
            Intent intent = new Intent(this, (Class<?>) intents.get(settingChosen));
            startActivityForResult(intent, 1);
        }
    }

    /**
     * Sets up the list view after the printer is selected from the network search.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        setUpListView(true);
    }

}