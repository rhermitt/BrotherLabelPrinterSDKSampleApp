/**
 * Searches for Network Printers based on settings selected in Printer Settings Activity.
 */

package com.example.foodsampleapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.brother.ptouch.sdk.NetPrinter;
import com.brother.ptouch.sdk.Printer;
import com.brother.ptouch.sdk.PrinterInfo;
import com.example.foodsampleapp.common.Common;
import com.example.foodsampleapp.common.MsgDialog;

import java.util.ArrayList;

public class Activity_FindPrinter extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private final MsgDialog msgDialog = new MsgDialog(this);
    private ListView printersList;
    private ArrayList<String> mItems = null;
    private NetPrinter[] mNetPrinter;
    private SearchThread searchPrinter;

    /**
     * Initializes the listview, creates the dialog box, and starts the search thread.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_printer);

        printersList = findViewById(R.id.lv_availablePrinters);

        setDialog();

        searchPrinter = new SearchThread();
        searchPrinter.start();
    }

    /**
     * Creates the dialog box for the search.
     */
    private void setDialog() {
        msgDialog.showMsgNoButton(
                getString(R.string.netPrinterListTitle_label),
                getString(R.string.search_printer));
    }

    /**
     * Gets the list of network printers and sets the listener for clicking an item.
     * @param times
     * @return
     */
    private boolean netPrinterList(int times){
        boolean searchEnd = false;

        try{
            // clear the item list
            if (mItems != null) {
                mItems.clear();
            }

            // get net printers of the particular model
            mItems = new ArrayList<String>();
            Printer myPrinter = new Printer();
            PrinterInfo info = myPrinter.getPrinterInfo();
            myPrinter.setPrinterInfo(info);

            String[] printerModels = Activity_PrinterSettings.checkedModelStrings.toArray(new String[0]);
            for (int i = 0; i < printerModels.length; i++) {
                printerModels[i] = printerModels[i].replace('_', '-');
            }
            mNetPrinter = myPrinter.getNetPrinters(printerModels);
            //mNetPrinter = myPrinter.getNetPrinters("QL-820NWB");
            final int netPrinterCount = mNetPrinter.length;

            if (netPrinterCount > 0) {
                searchEnd = true;

                String dispBuff[] = new String[netPrinterCount];
                for (int i = 0; i < netPrinterCount; i++) {
                    dispBuff[i] = mNetPrinter[i].modelName + "\n\n"
                            + mNetPrinter[i].ipAddress + "\n"
                            + mNetPrinter[i].macAddress + "\n"
                            + mNetPrinter[i].serNo + "\n"
                            + mNetPrinter[i].nodeName;
                    mItems.add(dispBuff[i]);
                }
            }
            else if (netPrinterCount == 0
                    && times == (Common.SEARCH_TIMES - 1)) { // when no printer
                // is found
                String dispBuff[] = new String[1];
                dispBuff[0] = getString(R.string.no_network_device);
                mItems.add(dispBuff[0]);
                searchEnd = true;
            }

            if (searchEnd) {
                // list the result of searching for net printer
                this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final ArrayAdapter<String> fileList = new ArrayAdapter<String>(
                                Activity_FindPrinter.this,
                                android.R.layout.test_list_item, mItems);
                        Activity_FindPrinter.this.printersList.setAdapter(fileList);
                        Activity_FindPrinter.this.printersList.setOnItemClickListener(Activity_FindPrinter.this);
                    }
                });

            }
        } catch (Exception e) {
        }

        return searchEnd;

    }

    /**
     * When a printer is clicked, its information is put into the active printer info.
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(!((String)parent.getItemAtPosition(position)).equalsIgnoreCase(getString(R.string.no_network_device))) {
            MainActivity.activePrinterInfo.put("ipAddress", mNetPrinter[position].ipAddress);
            MainActivity.activePrinterInfo.put("macAddress", mNetPrinter[position].macAddress);
            MainActivity.activePrinterInfo.put("localName", "");
            MainActivity.activePrinterInfo.put("printer", mNetPrinter[position].modelName);
            MainActivity.printerInfoSettings.put("Find Printer", mNetPrinter[position].modelName);
            MainActivity.printerInfoSettings.put("Manual IP Address", null);

        }
        finish();
    }

    /**
     * Calls netPrinterList() multiple times in order to search for network printers.
     */
    private class SearchThread extends Thread {

        /* search for the printer for 10 times until printer has been found. */
        @Override
        public void run() {

            for (int i = 0; i < Common.SEARCH_TIMES; i++) {
                // search for net printer.
                if (netPrinterList(i)) {
                    msgDialog.close();
                    break;
                }
            }
            msgDialog.close();
        }
    }

    /**
     * Restarts the search when the refresh button is clicked.
     * @param view
     */
    public void onRefreshButtonClick(View view){
        setDialog();
        searchPrinter = new SearchThread();
        searchPrinter.start();
    }
}