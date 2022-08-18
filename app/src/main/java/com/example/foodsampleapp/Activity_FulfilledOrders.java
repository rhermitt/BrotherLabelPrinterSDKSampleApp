/**
 * Maintains the view of fulfilled orders.
 */

package com.example.foodsampleapp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.brother.ptouch.sdk.Printer;
import com.brother.ptouch.sdk.PrinterInfo;
import com.brother.ptouch.sdk.PrinterStatus;
import com.brother.sdk.lmprinter.Channel;
import com.brother.sdk.lmprinter.GetStatusError;
import com.brother.sdk.lmprinter.GetStatusResult;
import com.brother.sdk.lmprinter.OpenChannelError;
import com.brother.sdk.lmprinter.PrinterDriver;
import com.brother.sdk.lmprinter.PrinterDriverGenerateResult;
import com.brother.sdk.lmprinter.PrinterDriverGenerator;
import com.example.foodsampleapp.common.Common;
import com.example.foodsampleapp.common.MsgDialog;
import com.example.foodsampleapp.common.MsgHandle;
import com.example.foodsampleapp.orders.Order;
import com.example.foodsampleapp.orders.OrderedItem;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

public class Activity_FulfilledOrders extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private Spinner orderNumberSpinner;
    private ListView itemsInOrder;
    private ArrayAdapter<Integer> spinnerAdapter;
    private OrderAdapter orderAdapter;

    private int templateKey;

    private TextView printerStatus;
    private Thread printConnectionThread;

    private boolean hasPermission;

    /**
     * Launches the screen for the user to choose to allow a bluetooth connection.
     */
    private ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if(isGranted){
                    hasPermission = true;
                }
                else{
                    hasPermission = false;
                    AlertDialog.Builder alert = new AlertDialog.Builder(this);
                    alert.setTitle(R.string.need_bluetooth);
                    alert.setMessage(R.string.cant_proceed);
                    alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

                    AlertDialog dialog = alert.create();
                    dialog.show();
                }
            });

    MsgHandle mHandle;
    MsgDialog mDialog;

    /**
     * Initializes the views, sets up the spinner with the first order, and sets up the listview with that order.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fulfilled_orders);

        orderNumberSpinner = findViewById(R.id.spnr_orderNumber);
        itemsInOrder = findViewById(R.id.lv_itemsInOrder);

        setUpSpinner();
        int firstOrderNumber = MainActivity.fulfilledOrders.getOrders().size() == 0 ? 0 : MainActivity.fulfilledOrders.getOrderNumbers().get(0);
        setUpListViewAdapter(firstOrderNumber);

        mDialog = new MsgDialog(this);
        mHandle = new MsgHandle(this, mDialog);

        printerStatus = findViewById(R.id.tv_printerStatusValueF);
        if(MainActivity.activePrinterInfo.get("ipAddress") == null){
            printerStatus.setTextColor(getResources().getColor(R.color.not_connected_color));
            printerStatus.setText(R.string.not_connected);
        }
        else if(MainActivity.printerInfoSettings.get("Manual IP Address") == null){
            printerStatus.setTextColor(getResources().getColor(R.color.connected_color));
            printerStatus.setText(R.string.connected);
        }
        else{
            printerStatus.setTextColor(getResources().getColor(R.color.not_connected_color));
            printerStatus.setText(R.string.not_connected);
        }


        //getPrinterStatus();
    }

    /**
     * Creates the adapter for the fulfilled order numbers and sets its listener.
     */
    private void setUpSpinner(){
        spinnerAdapter = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_dropdown_item, MainActivity.fulfilledOrders.getOrderNumbers());
        orderNumberSpinner.setAdapter(spinnerAdapter);
        orderNumberSpinner.setOnItemSelectedListener(new Activity_FulfilledOrders.SpinnerSelect());
    }

    /**
     * Sets up the adapter for the list view to contain the items in the given order.
     * @param orderNumber   Order number for the order to display.
     */
    private void setUpListViewAdapter(int orderNumber) {
        if (orderNumber == 0) {
            orderAdapter = new OrderAdapter(new HashMap<>());
        }
        else{
            orderAdapter = new OrderAdapter(getOrder(orderNumber).getItems());
        }
        itemsInOrder.setAdapter(orderAdapter);
        itemsInOrder.setOnItemClickListener(this);
    }



    private void getPrinterStatus() {
        if (MainActivity.activePrinterInfo.get("ipAddress") == null) {
            printerStatus.setText(R.string.not_connected);
        }
        else {
            printConnectionThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Channel channel = Channel.newWifiChannel(MainActivity.activePrinterInfo.get("ipAddress"));
                    PrinterDriverGenerateResult result = PrinterDriverGenerator.openChannel(channel);
                    if (result.getError().getCode() != OpenChannelError.ErrorCode.NoError) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                printerStatus.setText(R.string.not_connected);
                            }
                        });
                        return;
                    }
                    PrinterDriver printerDriver = result.getDriver();
                    GetStatusResult statusResult = printerDriver.getPrinterStatus();
                    if (statusResult.getError().getCode() != GetStatusError.ErrorCode.NoError) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                printerStatus.setText(R.string.not_connected);
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                printerStatus.setText(R.string.connected);
                            }
                        });
                    }
                }
            });
            printConnectionThread.start();
        }
    }



    /**
     * Finds the order with the given order number in the placed orders database.
     * @param orderNumber   The order to find.
     * @return  The Order object associated with orderNumber.
     */
    private Order getOrder(int orderNumber){
        for(Order order : MainActivity.fulfilledOrders.getOrders()){
            if(order.getOrderNumber() == orderNumber){
                return order;
            }
        }
        return null;
    }

    /**
     * When an item in the ListView is clicked, it will be printed.
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        OrderedItem clickedItem = orderAdapter.getItem(position).getKey();

        Order order = getOrder((int)orderNumberSpinner.getSelectedItem());
        int quantity = order.getItems().get(clickedItem);

        //If there is more than one of the same item, you can print multiple copies of the same label
        if(quantity > 1) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(R.string.print);
            alert.setMessage("Do you want to print "  + quantity + " labels or just one?");
            alert.setPositiveButton("MULTIPLE", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    printFromTemplate(clickedItem, (int) orderNumberSpinner.getSelectedItem(), quantity);
                }
            }).setNegativeButton("ONE", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    printFromTemplate(clickedItem, (int) orderNumberSpinner.getSelectedItem(), 1);
                }
            }).setNeutralButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            AlertDialog dialog = alert.create();
            dialog.show();
        }
        else{
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(R.string.print);
            alert.setMessage("Are you sure you want to print a label for " + clickedItem.getName() + "?");
            alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    printFromTemplate(clickedItem, (int) orderNumberSpinner.getSelectedItem(), 1);
                }
            }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            AlertDialog dialog = alert.create();
            dialog.show();
        }
    }

    /**
     * Prints a label for the specified item.
     * @param item  The item whose details should be printed.
     * @param orderNumber   The order number that the item is in. Used to print both the order number and the pickup time.
     * @param quantity  The number of labels to print. This depends on the user's selection and is not necessarily
     *                  the same number as the quantity in the order.
     */
    private void printFromTemplate(OrderedItem item, int orderNumber, int quantity){
        //Get the template key from the settings.
        try {
            templateKey = Integer.parseInt(MainActivity.fulfilledOrderPrinterSettings.get("Template Key"));
        }
        catch(NumberFormatException e){
            Toast.makeText(getApplicationContext(), R.string.valid_template_key, Toast.LENGTH_SHORT).show();
            return;
        }
        String ipAddress = MainActivity.activePrinterInfo.get("ipAddress");
        String macAddress = MainActivity.activePrinterInfo.get("MacAddress");

        //If there is no IP Address set, then no printer has been selected.
        if(ipAddress == null && macAddress == null){
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(R.string.no_printer_selected);
            alert.setMessage(R.string.please_connect);
            alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            AlertDialog dialog = alert.create();
            dialog.show();
            return;
        }

        boolean enteredOne = false;
        if(MainActivity.activePrinterInfo.get("MacAddress") != null){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED){
                requestPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT);
                enteredOne = true;
            }
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED){
                requestPermissionLauncher.launch(Manifest.permission.BLUETOOTH_SCAN);
                enteredOne = true;
            }
            if(!enteredOne){
                hasPermission = true;
            }
            if(!hasPermission){
                return;
            }
        }

        Printer printer = new Printer();
        PrinterInfo settings = printer.getPrinterInfo();
        printer.setMessageHandle(mHandle, Common.MSG_SDK_EVENT);

        Message msg = mHandle.obtainMessage(Common.MSG_PRINT_END);

        //Get the printer model - If the IP or MAC Address was put in manually, this comes from a manual input.
        //If the printer was obtained via the network search, then this comes from that selection.
        if(MainActivity.activePrinterInfo.get("ipAddress") != null) {
            if (MainActivity.printerInfoSettings.get("Manual IP Address") != null) {
                if (MainActivity.printerInfoSettings.get("Manual Model") == null) {
                    mHandle.setResult("To Use Manual Wifi Settings, You Must Input The IP Address AND Select a Manual Model");
                    msg = mHandle.obtainMessage(Common.MSG_PRINT_END);
                    mHandle.sendMessage(msg);
                    return;
                }
                settings.printerModel = PrinterInfo.Model.valueOf(MainActivity.printerInfoSettings.get("Manual Model"));
            } else {
                String modelName = MainActivity.activePrinterInfo.get("printer");
                String modelToUse = modelName.split(" ")[1];

                settings.printerModel = PrinterInfo.Model.valueOf(modelToUse.replace("-", "_"));
            }
        }
        else if(MainActivity.activePrinterInfo.get("MacAddress") != null){
            if (MainActivity.bluetoothPrinterInfoSettings.get("Manual Model") == null) {
                mHandle.setResult("To Print Via Bluetooth, You Must Input The MAC Address AND Select a Manual Model");
                msg = mHandle.obtainMessage(Common.MSG_PRINT_END);
                mHandle.sendMessage(msg);
                return;
            }
            settings.printerModel = PrinterInfo.Model.valueOf(MainActivity.bluetoothPrinterInfoSettings.get("Manual Model"));
        }

        settings.port = ipAddress == null ? PrinterInfo.Port.BLUETOOTH : PrinterInfo.Port.NET;
        if(ipAddress != null){
            settings.ipAddress = ipAddress;
        }
        else{
            settings.macAddress = macAddress;
        }
        settings.isAutoCut = MainActivity.fulfilledOrderPrinterSettings.get("AutoCut").equals("On");
        settings.numberOfCopies = quantity;
        settings.orientation = PrinterInfo.Orientation.PORTRAIT;
        settings.halftone = PrinterInfo.Halftone.valueOf(MainActivity.fulfilledOrderPrinterSettings.get("Halftone"));
        settings.printQuality = PrinterInfo.PrintQuality.valueOf(MainActivity.fulfilledOrderPrinterSettings.get("Print Quality"));

        printer.setPrinterInfo(settings);

        msg = mHandle.obtainMessage(Common.MSG_PRINT_START);
        mHandle.sendMessage(msg);

        Log.d("", "Entered The Print Thread");

        //Swaps out the items in the template and prints the label
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean error = false;
                Log.d("", "Entered The Run Thread");
                if (printer.startCommunication()) {
                    // The template key came from the settings.
                    if (printer.startPTTPrint(templateKey, null)) {
                        // Put the item name and order number on the label.
                        printer.replaceTextName(item.getName(), "ItemName");
                        printer.replaceTextName("Order Number: " + orderNumber, "OrderNumber");

                        // If there are special instructions for the item, put the semicolon separated version.
                        // If there aren't, the replace text name is necessary because otherwise the template default would stay.
                        if(item.hasSpecialInstructions()) {
                            printer.replaceTextName(item.getSpecialInstructionsString(), "SpecialInstructions");
                        }
                        else{
                            printer.replaceTextName(" ", "SpecialInstructions");
                        }

                        printer.replaceTextName(String.valueOf(item.getUpc()), "Barcode");

                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy hh:mm a");
                        Order order = getOrder(orderNumber);
                        if(order.getAsap()){
                            LocalDateTime current = LocalDateTime.now();
                            String today = current.format(formatter);
                            printer.replaceTextName("Printed at: " + today, "pickupDateTime");
                        }
                        else{
                            String date = order.getPickupTime().format(formatter);
                            printer.replaceTextName("For Pickup at: " + date, "pickupDateTime");
                        }

                        // Start print
                        PrinterStatus result = printer.flushPTTPrint();
                        if (result.errorCode != PrinterInfo.ErrorCode.ERROR_NONE) {
                            error = true;
                            mHandle.setResult(result.errorCode.toString());
                            mHandle.sendMessage(mHandle.obtainMessage(Common.MSG_PRINT_END));
                            Log.d("TAG", "ERROR - " + result.errorCode);
                        }
                    }
                    printer.endCommunication();

                    if(!error){
                        mHandle.setResult("Success");
                        Message mesg = mHandle.obtainMessage(Common.MSG_PRINT_END);
                        mHandle.sendMessage(mesg);
                    }
                }
                else{
                    mHandle.setResult("Could Not Connect to Printer");
                    Message mesg = mHandle.obtainMessage(Common.MSG_PRINT_END);
                    mHandle.sendMessage(mesg);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            printerStatus.setTextColor(getResources().getColor(R.color.not_connected_color));
                            printerStatus.setText(R.string.not_connected);
                        }
                    });
                }
            }
        }).start();
    }

    /**
     * Opens the Fulfilled Order Print Settings Activity.
     * @param view
     */
    public void onPrintSettingsButtonClick(View view){
        Intent intent = new Intent(this, Activity_FulfilledOrderPrinterSettings.class);
        startActivity(intent);
    }


    /**
     * Class implementing the On Item Selected Listener for the Order Number Spinner. Calls populateList
     * to update the table whenever a new order is selected.
     */
    private class SpinnerSelect implements AdapterView.OnItemSelectedListener{

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            int orderSelected = (int)parent.getSelectedItem();
            setUpListViewAdapter(orderSelected);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }
}