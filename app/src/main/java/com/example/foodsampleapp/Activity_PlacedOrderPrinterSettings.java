/**
 * Displays the Settings for printing a placed order.
 */

package com.example.foodsampleapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.brother.sdk.lmprinter.PrinterModel;
import com.brother.sdk.lmprinter.setting.PrintImageSettings;
import com.brother.sdk.lmprinter.setting.QLPrintSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Activity_PlacedOrderPrinterSettings extends AppCompatActivity implements AdapterView.OnItemClickListener{

    private SettingsAdapter settingsAdapter;
    private ListView settingsList;

    private HashMap<String, Object> intents;
    private HashMap<String, ArrayList<String>> settingToOptions;
    public static ArrayList<String> checkedModelStrings;

    private ArrayList<PrinterModel> checkedModels;

    /**
     * Creates the maps and lists and sets up the listview.
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

        setContentView(R.layout.activity_placed_order_printer_settings);

        settingsList = findViewById(R.id.lv_placedPrinterSettings);
        //setUpIntentsMap();
        setUpOptions();
        setUpListView();
    }

    /**
     * Sets up the listview with the most up to date settings.
     */
    private void setUpListView(){
        settingsAdapter = new SettingsAdapter(MainActivity.placedOrderPrinterSettings);
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

        settingToOptions.put("Resolution", resolutions);
        settingToOptions.put("Halftone", halftones);
        settingToOptions.put("LabelSize", labelSizes);
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

        if(choices != null){
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
                    setUpListView();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }
}