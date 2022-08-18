/**
 * Allows the user to select settings for printing a label for an item in a fulfilled order.
 */

package com.example.foodsampleapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.brother.ptouch.sdk.PrinterInfo;
import com.brother.sdk.lmprinter.PrinterModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Activity_FulfilledOrderPrinterSettings extends AppCompatActivity implements AdapterView.OnItemClickListener{

    private SettingsAdapter settingsAdapter;
    private ListView settingsList;

    private HashMap<String, Object> intents;
    private HashMap<String, ArrayList<String>> settingToOptions;
    public static ArrayList<String> checkedModelStrings;

    private ArrayList<PrinterModel> checkedModels;

    /**
     * Initializes the maps for the settings.
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

        setContentView(R.layout.activity_fulfilled_order_printer_settings);

        settingsList = findViewById(R.id.lv_fulfilledPrinterSettings);
        //setUpIntentsMap();
        setUpOptions();
        setUpListView();
    }

    /**
     * Updates the List View to contain the most recent settings.
     */
    private void setUpListView(){
        settingsAdapter = new SettingsAdapter(MainActivity.fulfilledOrderPrinterSettings);
        settingsList.setAdapter(settingsAdapter);
        settingsList.setOnItemClickListener(this);
    }


    /**
     * Associates each setting with its possible choices.
     */
    private void setUpOptions(){
        ArrayList<String> onOff = new ArrayList<>(2);
        onOff.add("On");
        onOff.add("Off");

        settingToOptions.put("AutoCut", onOff);

        ArrayList<String> halftones = new ArrayList<String>(PrinterInfo.Halftone.values().length);
        for(PrinterInfo.Halftone halftone : PrinterInfo.Halftone.values()){
            halftones.add(halftone.name());
        }

        ArrayList<String> printQualities = new ArrayList<>(PrinterInfo.PrintQuality.values().length);
        for(PrinterInfo.PrintQuality quality : PrinterInfo.PrintQuality.values()){
            printQualities.add(quality.name());
        }

        settingToOptions.put("Print Quality", printQualities);
        settingToOptions.put("Halftone", halftones);
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


        //Template Key uses a text box.
        if(chosen.getKey().equalsIgnoreCase("Template Key")){
            EditText keyInput = new EditText(this);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.template_key);
            builder.setView(keyInput);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    MainActivity.fulfilledOrderPrinterSettings.put("Template Key", keyInput.getText().toString());
                    setUpListView();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
        //Most settings have a multiple choice style selection.
        if(choices != null){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(chosen.getKey());

            builder.setItems(choices.toArray(new CharSequence[choices.size()]), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(which == 0){
                        MainActivity.fulfilledOrderPrinterSettings.put(chosen.getKey(), choices.get(which));
                    }
                    else{
                        MainActivity.fulfilledOrderPrinterSettings.put(chosen.getKey(), choices.get(which));
                    }
                    setUpListView();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }
}
