/**
 * Activity in which the vendor can view the orders that have been placed, mark them as fulfilled,
 * and print them.
 */

package com.example.foodsampleapp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.brother.sdk.lmprinter.Channel;
import com.brother.sdk.lmprinter.OpenChannelError;
import com.brother.sdk.lmprinter.PrintError;
import com.brother.sdk.lmprinter.PrinterDriver;
import com.brother.sdk.lmprinter.PrinterDriverGenerateResult;
import com.brother.sdk.lmprinter.PrinterDriverGenerator;
import com.brother.sdk.lmprinter.PrinterModel;
import com.brother.sdk.lmprinter.setting.QLPrintSettings;
import com.example.foodsampleapp.common.Common;
import com.example.foodsampleapp.common.MsgDialog;
import com.example.foodsampleapp.common.MsgHandle;
import com.example.foodsampleapp.orders.Order;
import com.example.foodsampleapp.orders.OrderedItem;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class Activity_PlacedOrders extends AppCompatActivity {

    private Spinner orderNumberSpinner;
    private ArrayAdapter<Integer> spinnerAdapter;
    private TableLayout orderContents;
    private ArrayList<TextView> textFields;
    private TextView customer;
    private TextView pickupInfo;

    private TextView printerStatus;

    private Bitmap orderImage;

    private boolean hasPermission;

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
     * Sets up the spinner and the textFields.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_placed_orders);

        orderNumberSpinner = findViewById(R.id.spnr_orderNumber);
        spinnerAdapter = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_dropdown_item, MainActivity.placedOrders.getOrderNumbers());
        orderNumberSpinner.setAdapter(spinnerAdapter);
        orderNumberSpinner.setOnItemSelectedListener(new SpinnerSelect());

        orderContents = findViewById(R.id.tv_orderContents);
        customer = findViewById(R.id.tv_customerName);
        pickupInfo = findViewById(R.id.tv_pickupDateTime);
        textFields = new ArrayList<TextView>(MainActivity.placedOrders.getOrderNumbers().size());

        mDialog = new MsgDialog(this);
        mHandle = new MsgHandle(this, mDialog);

        printerStatus = findViewById(R.id.tv_printerStatusValueP);


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
    }


    /**
     * Prints the order, with the item in black and the instructions in red.
     * @param view
     */
    public void onPrintOrderButtonClick(View view){
        if(MainActivity.activePrinterInfo.get("ipAddress") == null && MainActivity.activePrinterInfo.get("MacAddress") == null){
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

        if(orderNumberSpinner.getSelectedItem() == null){
            Toast.makeText(getApplicationContext(), "No Order Is Selected", Toast.LENGTH_SHORT).show();
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

        Order order = getOrder((int)orderNumberSpinner.getSelectedItem());
        if(order == null){
            return;
        }
        orderImage = orderToBitmapWithInstructions(order);
        new OrderInstructionsPrintThread().start();
    }

    /**
     * Converts the order to a bitmap image.
     * @param order The order to convert.ima
     * @return  The image.
     */
    private Bitmap orderToBitmapWithInstructions(Order order){
        Paint paint = new Paint();
        paint.setTextSize(56);

        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.LEFT);

        float baseline = -paint.ascent();
        int width = (int)(paint.measureText("Hamburger") + 0.5f);
        int height = (int) (baseline + paint.descent() + 0.5f);
        Paint.FontMetrics metric = paint.getFontMetrics();
        float textHeight = metric.descent - metric.ascent;
        Bitmap image = Bitmap.createBitmap(width + 500, height + (50 + ((int)textHeight) * getNumLines(order)), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);
        canvas.drawRect(0,0,width + 500, height + (200 + ((int)textHeight) * getNumLines(order)) + 1, paint);
        paint.setColor(Color.BLACK);

        canvas.drawRect(0, 0, width + 500, textHeight+20, paint);

        paint.setColor(Color.WHITE);

        Set<Map.Entry<OrderedItem, Integer>> itemsInOrder = order.getItems().entrySet();
        //canvas.drawText("Order Number: " + order.getOrderNumber(), 0, baseline, paint);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Order Number: " + order.getOrderNumber(), canvas.getWidth()/2, baseline+10, paint);

        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.BLACK);
        paint.setTextSize(48);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy hh:mm a");

        String placedDate = "Placed at "+ order.getPlacedTime().format(formatter);
        canvas.drawText(placedDate, 0, baseline + 100, paint);

        String pickupDate = order.getAsap() ? "Pickup ASAP" : "Pickup at " + order.getPickupTime().format(formatter);
        canvas.drawText(pickupDate, 0, baseline + 200, paint);

        paint.setTypeface(Typeface.DEFAULT_BOLD);
        canvas.drawText("Customer: " + order.getCustomerName(), 0, baseline + 300, paint);
        paint.setTypeface(Typeface.DEFAULT);
        int offset = 450;
        for(Map.Entry<OrderedItem, Integer> mapping : itemsInOrder){
            String line = mapping.getValue() + " X " + mapping.getKey().toString();
            canvas.drawText(line, 0, baseline + offset, paint);
            offset += 100;
            paint.setColor(Color.RED);
            for(String instruction : mapping.getKey().getSpecialInstructions()){
                canvas.drawText(instruction, 50, baseline + offset, paint);
                offset += 50;
            }
            paint.setColor(Color.BLACK);
            offset += 50;
        }

        return image;
    }

    /**
     * Opens the Placed Order Print Settings Activity.
     * @param view
     */
    public void onPrintSettingsButtonClick(View view){
        Intent intent = new Intent(this, Activity_PlacedOrderPrinterSettings.class);
        startActivity(intent);
    }

    /**
     * Gets the number of lines that will be printed with an order.
     * @param order The order that will be printed.
     * @return  The number of lines, including one for the item name and one for each instruction.
     */
    private int getNumLines(Order order){
        int numLines = 5;
        for (OrderedItem item : order.getItems().keySet()){
            numLines += (3 + item.getSpecialInstructions().length);
        }
        return numLines;
    }

    /**
     * Class that executes the printing of the image.
     */
    private class OrderInstructionsPrintThread extends Thread{

        /**
         * Prints the image.
         */
        @Override
        public void run(){
            Message msg = mHandle.obtainMessage(Common.MSG_PRINT_END);
//            if(MainActivity.activePrinterInfo.get("ipAddress") == null){
//                mHandle.setResult("No Printer Is Selected");
//                mHandle.sendMessage(msg);
//            }

            PrinterModel model = null;
            //The process of getting the model varies depending on whether the printer info was put in manually or obtained from a network search.
            if(MainActivity.activePrinterInfo.get("ipAddress") != null) {
                if (MainActivity.printerInfoSettings.get("Manual IP Address") != null) {
                    if (MainActivity.printerInfoSettings.get("Manual Model") == null) {
                        mHandle.setResult("To Use Manual Wifi Settings, You Must Input The IP Address AND Select a Manual Model");
                        msg = mHandle.obtainMessage(Common.MSG_PRINT_END);
                        mHandle.sendMessage(msg);
                        return;
                    }
                    model = PrinterModel.valueOf(MainActivity.printerInfoSettings.get("Manual Model"));
                } else {
                    String modelName = MainActivity.activePrinterInfo.get("printer");
                    String modelToUse = modelName.split(" ")[1];

                    model = PrinterModel.valueOf(modelToUse.replace("-", "_"));
                }
            }
            else if(MainActivity.activePrinterInfo.get("MacAddress") != null){
                if (MainActivity.bluetoothPrinterInfoSettings.get("Manual Model") == null) {
                    mHandle.setResult("To Print Via Bluetooth, You Must Input The MAC Address AND Select a Manual Model");
                    msg = mHandle.obtainMessage(Common.MSG_PRINT_END);
                    mHandle.sendMessage(msg);
                    return;
                }
                model = PrinterModel.valueOf(MainActivity.bluetoothPrinterInfoSettings.get("Manual Model"));
            }

            Channel channel = null;
            if(MainActivity.activePrinterInfo.get("ipAddress") != null) {
                channel = Channel.newWifiChannel(MainActivity.activePrinterInfo.get("ipAddress"));
            }
            else{
                channel = Channel.newBluetoothChannel(MainActivity.activePrinterInfo.get("MacAddress"),
                                                            BluetoothAdapter.getDefaultAdapter());
            }
            msg = mHandle.obtainMessage(Common.MSG_PRINT_START);
            mHandle.sendMessage(msg);

            PrinterDriverGenerateResult result = PrinterDriverGenerator.openChannel(channel);
            if (result.getError().getCode() != OpenChannelError.ErrorCode.NoError) {
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
                return;
            }

            File dir = getExternalFilesDir(null);

            PrinterDriver printerDriver = result.getDriver();

            QLPrintSettings settings = new QLPrintSettings(model);

            settings.setLabelSize(QLPrintSettings.LabelSize.valueOf(MainActivity.placedOrderPrinterSettings.get("LabelSize")));
            settings.setAutoCut(MainActivity.placedOrderPrinterSettings.get("AutoCut").equals("On"));
            settings.setHalftone(QLPrintSettings.Halftone.valueOf(MainActivity.placedOrderPrinterSettings.get("Halftone")));
            settings.setWorkPath(dir.toString());
            settings.setResolution(QLPrintSettings.Resolution.valueOf(MainActivity.placedOrderPrinterSettings.get("Resolution")));

            PrintError printError = printerDriver.printImage(orderImage, settings);
            if (printError.getCode() != PrintError.ErrorCode.NoError) {
                printerDriver.closeChannel();
                mHandle.setResult(printError.getCode().toString());
                mHandle.sendMessage(mHandle.obtainMessage(Common.MSG_PRINT_END));
                return;
            }

            printerDriver.closeChannel();

            mHandle.setResult("Success");
            msg = mHandle.obtainMessage(Common.MSG_PRINT_END);
            mHandle.sendMessage(msg);
        }

    }

    /**
     * When the Fulfill Order Button is clicked, the order is removed from the placed orders database
     * and the spinner is reset.
     * @param view
     */
    public void onFulfillOrderButtonClick(View view){
        int orderNumber = (int)orderNumberSpinner.getSelectedItem();
        Order orderToFulfill = getOrder(orderNumber);
        MainActivity.placedOrders.remove(orderToFulfill);
        if(MainActivity.placedOrders.getOrders().size() == 0){
            orderContents.removeAllViews();
        }
        MainActivity.fulfilledOrders.add(orderToFulfill);
        setUpSpinner();
        if(orderNumberSpinner.getSelectedItem() == null){
            customer.setText("");
            pickupInfo.setText("");
        }
    }

    /**
     * Assigns the adapter to the spinner and sets up the spinner's listener.
     */
    private void setUpSpinner(){
        spinnerAdapter = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_dropdown_item, MainActivity.placedOrders.getOrderNumbers());
        orderNumberSpinner.setAdapter(spinnerAdapter);
        orderNumberSpinner.setOnItemSelectedListener(new SpinnerSelect());
    }

    /**
     * Populates the table with all of the items in the order and the instructions for each one.
     * @param orderNumber
     */
    private void populateList(int orderNumber){
        Order order = getOrder(orderNumber);
        customer.setText(order.getCustomerName());

        if(order.getAsap()){
            pickupInfo.setText(R.string.for_pickup_asap);
        }
        else{
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy hh:mm a");
            pickupInfo.setText("For Pickup at " + order.getPickupTime().format(formatter));
        }

        for(OrderedItem item : order.getItems().keySet()){
            TableRow newRow = new TableRow(this);
            newRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

            TextView itemDescription = new TextView(this);
            itemDescription.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, (float)1.0));

            newRow.addView(itemDescription);
            orderContents.addView(newRow, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
            textFields.add(itemDescription);

            String itemContents = item.getName();
            itemContents = String.valueOf(order.getItems().get(item)) + " " + itemContents;
            for(String instruction : item.getSpecialInstructions()){
                itemContents += ("\n\t" + instruction);
            }
            itemDescription.setText(itemContents);
        }
    }

    /**
     * Finds the order with the given order number in the placed orders database.
     * @param orderNumber
     * @return
     */
    private Order getOrder(int orderNumber){
        for(Order order : MainActivity.placedOrders.getOrders()){
            if(order.getOrderNumber() == orderNumber){
                return order;
            }
        }
        return null;
    }

    /**
     * Class implementing the On Item Selected Listener for the Order Number Spinner. Calls populateList
     * to update the table whenever a new order is selected.
     */
    private class SpinnerSelect implements AdapterView.OnItemSelectedListener{

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            orderContents.removeAllViews();
            int orderNumber = (int)parent.getSelectedItem();
            populateList(orderNumber);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            orderContents.removeAllViews();
        }
    }
}