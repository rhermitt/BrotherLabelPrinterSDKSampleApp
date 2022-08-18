/**
 * The main menu. This class also holds global information used throughout the app such as placed orders.
 */

package com.example.foodsampleapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.brother.ptouch.sdk.LabelInfo;
import com.brother.ptouch.sdk.Printer;
import com.brother.ptouch.sdk.PrinterInfo;
import com.brother.sdk.lmprinter.Channel;
import com.brother.sdk.lmprinter.OpenChannelError;
import com.brother.sdk.lmprinter.PrintError;
import com.brother.sdk.lmprinter.PrinterDriver;
import com.brother.sdk.lmprinter.PrinterDriverGenerateResult;
import com.brother.sdk.lmprinter.PrinterDriverGenerator;
import com.brother.sdk.lmprinter.setting.QLPrintSettings;
import com.brother.sdk.lmprinter.PrinterModel;

import com.example.foodsampleapp.common.Common;
import com.example.foodsampleapp.common.MsgDialog;
import com.example.foodsampleapp.common.MsgHandle;
import com.example.foodsampleapp.orders.Item;
import com.example.foodsampleapp.orders.Order;
import com.example.foodsampleapp.orders.OrdersDB;
import com.example.foodsampleapp.printprocess.PrinterModelInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity {

    MsgHandle mHandle;
    MsgDialog mDialog;

    public static ArrayList<Item> availableItems;
    public static OrdersDB placedOrders;
    public static Order currentOrder;
    public static OrdersDB fulfilledOrders;
    public static HashMap<String, String> printerInfoSettings;
    public static HashMap<String, String> bluetoothPrinterInfoSettings;
    public static HashMap<String, String> placedOrderPrinterSettings;
    public static HashMap<String, String> fulfilledOrderPrinterSettings;
    public static HashMap<String, String> activePrinterInfo;

    /**
     * Initializes all of the global information and sets the event handlers for the buttons.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnVendor = findViewById(R.id.btn_vendorLogin);
        btnVendor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent vendorIntent = new Intent(MainActivity.this, Activity_VendorMenu.class);
                startActivity(vendorIntent);
            }
        });

        Button btnCustomer = findViewById(R.id.btn_customerLogin);
        btnCustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent customerIntent = new Intent(MainActivity.this, Activity_CustomerMenu.class);
                startActivity(customerIntent);
            }
        });

        currentOrder = new Order();

        availableItems = new ArrayList<>();
        populateItems();

        placedOrders = new OrdersDB();
        fulfilledOrders = new OrdersDB();

        setPreferences();

        setUpPrinterSettings();

        mDialog = new MsgDialog(this);
        mHandle = new MsgHandle(this, mDialog);
    }

    /**
     * Sets the default values for all the printer settings.
     */
    private void setUpPrinterSettings(){
        placedOrderPrinterSettings = new HashMap<>();
        printerInfoSettings = new HashMap<>();
        fulfilledOrderPrinterSettings = new HashMap<>();
        bluetoothPrinterInfoSettings = new HashMap<>();

        printerInfoSettings.put("Find Printer", null);
        placedOrderPrinterSettings.put("AutoCut", "On");
        placedOrderPrinterSettings.put("LabelSize", QLPrintSettings.LabelSize.RollW62RB.name());
        printerInfoSettings.put("Manual IP Address", null);
        placedOrderPrinterSettings.put("Halftone", new QLPrintSettings(PrinterModel.QL_820NWB).getHalftone().name());
        placedOrderPrinterSettings.put("Resolution", new QLPrintSettings(PrinterModel.QL_820NWB).getResolution().name());
        printerInfoSettings.put("Model", PrinterModel.QL_820NWB.name());
        printerInfoSettings.put("Manual Model", null);

        //fulfilledOrderPrinterSettings.put("LabelSize", LabelInfo.QL700.W62RB.name());
        fulfilledOrderPrinterSettings.put("AutoCut", "On");
        fulfilledOrderPrinterSettings.put("Template Key", String.valueOf(15));
        fulfilledOrderPrinterSettings.put("Halftone", "ERRORDIFFUSION");
        fulfilledOrderPrinterSettings.put("Print Quality", "NORMAL");

        activePrinterInfo = new HashMap<>();
        activePrinterInfo.put("ipAddress", null);
        activePrinterInfo.put("MacAddress", null);

        bluetoothPrinterInfoSettings.put("MAC Address", null);
        bluetoothPrinterInfoSettings.put("Manual Model", PrinterModel.QL_820NWB.name());
    }

    private void setPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        // initialization for print
        Printer printer = new Printer();
        PrinterInfo printerInfo = printer.getPrinterInfo();
        if (printerInfo == null) {
            printerInfo = new PrinterInfo();
            printer.setPrinterInfo(printerInfo);

        }
        if (sharedPreferences.getString("printerModel", "").equals("")) {
            String printerModel = printerInfo.printerModel.toString();
            PrinterModelInfo.Model model = PrinterModelInfo.Model.valueOf(printerModel);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("printerModel", printerModel);
            editor.putString("port", printerInfo.port.toString());
            editor.putString("address", printerInfo.ipAddress);
            editor.putString("macAddress", printerInfo.macAddress);
            editor.putString("localName", printerInfo.getLocalName());

            // Override SDK default paper size
            editor.putString("paperSize", model.getDefaultPaperSize());

            editor.putString("orientation", printerInfo.orientation.toString());
            editor.putString("numberOfCopies",
                    Integer.toString(printerInfo.numberOfCopies));
            editor.putString("halftone", printerInfo.halftone.toString());
            editor.putString("printMode", printerInfo.printMode.toString());
            editor.putString("pjCarbon", Boolean.toString(printerInfo.pjCarbon));
            editor.putString("pjDensity",
                    Integer.toString(printerInfo.pjDensity));
            editor.putString("pjFeedMode", printerInfo.pjFeedMode.toString());
            editor.putString("align", printerInfo.align.toString());
            editor.putString("leftMargin",
                    Integer.toString(printerInfo.margin.left));
            editor.putString("valign", printerInfo.valign.toString());
            editor.putString("topMargin",
                    Integer.toString(printerInfo.margin.top));
            editor.putString("customPaperWidth",
                    Integer.toString(printerInfo.customPaperWidth));
            editor.putString("customPaperLength",
                    Integer.toString(printerInfo.customPaperLength));
            editor.putString("customFeed",
                    Integer.toString(printerInfo.customFeed));
            editor.putString("paperPosition",
                    printerInfo.paperPosition.toString());
            editor.putString("customSetting",
                    sharedPreferences.getString("customSetting", ""));
            editor.putString("rjDensity",
                    Integer.toString(printerInfo.rjDensity));
            editor.putString("rotate180",
                    Boolean.toString(printerInfo.rotate180));
            editor.putString("dashLine", Boolean.toString(printerInfo.dashLine));

            editor.putString("peelMode", Boolean.toString(printerInfo.peelMode));
            editor.putString("mode9", Boolean.toString(printerInfo.mode9));
            editor.putString("pjSpeed", Integer.toString(printerInfo.pjSpeed));
            editor.putString("pjPaperKind", printerInfo.pjPaperKind.toString());
            editor.putString("printerCase",
                    printerInfo.rollPrinterCase.toString());
            editor.putString("printQuality", printerInfo.printQuality.toString());
            editor.putString("skipStatusCheck",
                    Boolean.toString(printerInfo.skipStatusCheck));
            editor.putString("checkPrintEnd", printerInfo.checkPrintEnd.toString());
            editor.putString("imageThresholding",
                    Integer.toString(printerInfo.thresholdingValue));
            editor.putString("scaleValue",
                    Double.toString(printerInfo.scaleValue));
            editor.putString("trimTapeAfterData",
                    Boolean.toString(printerInfo.trimTapeAfterData));
            editor.putString("enabledTethering",
                    Boolean.toString(printerInfo.enabledTethering));


            editor.putString("processTimeout",
                    Integer.toString(printerInfo.timeout.processTimeoutSec));
            editor.putString("sendTimeout",
                    Integer.toString(printerInfo.timeout.sendTimeoutSec));
            editor.putString("receiveTimeout",
                    Integer.toString(printerInfo.timeout.receiveTimeoutSec));
            editor.putString("connectionTimeout",
                    Integer.toString(printerInfo.timeout.connectionWaitMSec));
            editor.putString("closeWaitTime",
                    Integer.toString(printerInfo.timeout.closeWaitDisusingStatusCheckSec));

            editor.putString("overwrite",
                    Boolean.toString(printerInfo.overwrite));
            editor.putString("savePrnPath", printerInfo.savePrnPath);
            editor.putString("softFocusing",
                    Boolean.toString(printerInfo.softFocusing));
            editor.putString("rawMode",
                    Boolean.toString(printerInfo.rawMode));
            editor.putString("workPath", printerInfo.workPath);
            editor.putString("useLegacyHalftoneEngine",
                    Boolean.toString(printerInfo.useLegacyHalftoneEngine));
            editor.apply();
        }

    }

    /**
     * Loads items that have already been created from the csv file.
     */
    private void populateItems(){
        String path = getFilesDir() + "" + File.separatorChar + "itemsforsale.csv";
        File file = new File(path);
        if(!file.exists()){
             try {
                 FileOutputStream outputStream = new FileOutputStream(file);
                 OutputStreamWriter outputWriter = new OutputStreamWriter(outputStream);
                 outputWriter.write("Name,Price,Instructions");
                 outputWriter.close();
             }
             catch(Exception e){
                 e.printStackTrace();
                 return;
             }
        }

        BufferedReader reader = null;
        try {
            FileReader fileReader = new FileReader(path);
            reader = new BufferedReader(fileReader);
        }
        catch(Exception e){
            e.printStackTrace();
            return;
        }

        String line = "";

        try{
            while((line = reader.readLine()) != null){
                String[] items = line.split(",");

                String name = items[0];
                double price = 0.0;
                try{
                    price = Double.parseDouble(items[1]);
                }
                catch (NumberFormatException e){
                    continue;
                }
                String instructions = items[2];

                availableItems.add(new Item(name, instructions, price));
            }
        }
        catch( IOException e){
            Log.e("", "Exception in populate when reading");
        }

        try {
            reader.close();
        }
        catch (IOException e){
            Log.e("", "Exception in populate when closing");
        }
    }


    private class SamplePrintThread extends Thread{

        @Override
        public void run(){
            Channel channel = Channel.newWifiChannel("192.168.1.97");

            Message msg = mHandle.obtainMessage(Common.MSG_PRINT_START);
            mHandle.sendMessage(msg);

            PrinterDriverGenerateResult result = PrinterDriverGenerator.openChannel(channel);
            if (result.getError().getCode() != OpenChannelError.ErrorCode.NoError) {
                Log.e("", "Error - Open Channel: " + result.getError().getCode());
                return;
            }
            Log.d("", "Success - Open Channel");

            File dir = getExternalFilesDir(null);
            File file = new File(dir, "pricetag2.bmp");

            PrinterDriver printerDriver = result.getDriver();

            PrinterModel model = PrinterModel.QL_820NWB;
            QLPrintSettings settings = new QLPrintSettings(model);

            settings.setLabelSize(QLPrintSettings.LabelSize.RollW62RB);
            settings.setAutoCut(true);
            settings.setWorkPath(dir.toString());

            PrintError printError = printerDriver.printImage("/storage/emulated/0/Download/pricetag2.bmp", settings);
            //PrintError printError = printerDriver.printImage(file.toString(), settings);
            if (printError.getCode() != PrintError.ErrorCode.NoError) {
                printerDriver.closeChannel();
                mHandle.setResult(printError.getCode().toString());
                mHandle.sendMessage(mHandle.obtainMessage(Common.MSG_PRINT_END));
                return;
            }

            /**THis is some Code to test the printer connection status**/
            //GetStatusResult status = printerDriver.getPrinterStatus();
            //GetStatusError.ErrorCode errorCode = status.getError().getCode();
            //if(errorCode == GetStatusError.ErrorCode.NoError){
            //    Log.d("", "Connected! No Error!");
            //}
            //else{
            //    Log.e("","WE HAVE AN ERROR CODE!");
            //}



            printerDriver.closeChannel();

            mHandle.setResult("Success");
            msg = mHandle.obtainMessage(Common.MSG_PRINT_END);
            mHandle.sendMessage(msg);
        }

    }
}