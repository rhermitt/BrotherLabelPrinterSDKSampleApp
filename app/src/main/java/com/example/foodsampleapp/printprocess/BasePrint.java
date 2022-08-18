/**
 * BasePrint for printing
 *
 * @author Brother Industries, Ltd.
 * @version 2.2
 */

package com.example.foodsampleapp.printprocess;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Message;
import android.preference.PreferenceManager;

import com.brother.ptouch.sdk.CustomPaperInfo;
import com.brother.ptouch.sdk.JNIStatus.BatteryTernary;
import com.brother.ptouch.sdk.LabelInfo;
import com.brother.ptouch.sdk.PaperKind;
import com.brother.ptouch.sdk.Printer;
import com.brother.ptouch.sdk.PrinterInfo;
import com.brother.ptouch.sdk.PrinterInfo.ErrorCode;
import com.brother.ptouch.sdk.PrinterInfo.Model;
import com.brother.ptouch.sdk.PrinterStatus;
import com.brother.ptouch.sdk.Unit;
import com.example.foodsampleapp.R;
import com.example.foodsampleapp.common.Common;
import com.example.foodsampleapp.common.MsgDialog;
import com.example.foodsampleapp.common.MsgHandle;

import java.util.List;
import java.util.Map;

import static com.example.foodsampleapp.common.Common.UsbAuthorizationState.NOT_DETERMINED;
import static com.example.foodsampleapp.common.Common.UsbAuthorizationState.APPROVED;

@SuppressWarnings("ALL")
public abstract class BasePrint {
    private static final long BLE_RESOLVE_TIMEOUT = 5000;
    static Printer mPrinter;
    static boolean mCancel;
    final MsgHandle mHandle;
    final MsgDialog mDialog;
    private final SharedPreferences sharedPreferences;
    private final Context mContext;
    PrinterStatus mPrintResult;
    private boolean manualCustomPaperSettingsEnabled;
    private String customSetting;
    private PrinterInfo mPrinterInfo;

    BasePrint(Context context, MsgHandle handle, MsgDialog dialog) {

        mContext = context;
        mDialog = dialog;
        mHandle = handle;
        mDialog.setHandle(mHandle);
        sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        mCancel = false;
        // initialization for print
        mPrinterInfo = new PrinterInfo();
        mPrinter = new Printer();
        mPrinterInfo = mPrinter.getPrinterInfo();
        mPrinter.setMessageHandle(mHandle, Common.MSG_SDK_EVENT);
    }

    public static void cancel() {
        if (mPrinter != null)
            mPrinter.cancel();
        mCancel = true;
    }

    protected abstract void doPrint();

    public static class BasePrintResult {
        public final boolean success;
        public final String errorMessage;

        static BasePrintResult success() {
            return new BasePrintResult(true, "");
        }

        static BasePrintResult fail(String message) {
            return new BasePrintResult(false, message);
        }

        private BasePrintResult(boolean success, String errorMessage) {
            this.success = success;
            this.errorMessage = errorMessage;
        }
    }

    /**
     * set PrinterInfo
     *
     * @return setCustomPaper's result. can ignore other than raster print
     */
    public BasePrintResult setPrinterInfo() {
        getPreferences();
        BasePrintResult customPaperResult = setCustomPaper();
        boolean setPrinterInfoResult = mPrinter.setPrinterInfo(mPrinterInfo);
        if (mPrinterInfo.port == PrinterInfo.Port.USB) {
            while (true) {
                if (Common.mUsbAuthorizationState != NOT_DETERMINED)
                    break;
            }
        }
        if (!setPrinterInfoResult) {
            String errorMessage = mPrinter.getResult().errorCode.toString();
            return BasePrintResult.fail(errorMessage);
        }
        if (customPaperResult.success == false) {
            return customPaperResult;
        } else {
            return BasePrintResult.success();
        }
    }

    /**
     * get PrinterInfo
     */
    public PrinterInfo getPrinterInfo() {
        getPreferences();
        return mPrinterInfo;
    }

    /**
     * get Printer
     */
    public Printer getPrinter() {

        return mPrinter;
    }

    /**
     * get Printer
     */
    public PrinterStatus getPrintResult() {
        return mPrintResult;
    }

    /**
     * get Printer
     */
    public void setPrintResult(PrinterStatus printResult) {
        mPrintResult = printResult;
    }

    public void setBluetoothAdapter(BluetoothAdapter bluetoothAdapter) {
        mPrinter.setBluetooth(bluetoothAdapter);
        mPrinter.setBluetoothLowEnergy(mContext, bluetoothAdapter, BLE_RESOLVE_TIMEOUT);
    }

    @TargetApi(12)
    public UsbDevice getUsbDevice(UsbManager usbManager) {
        return mPrinter.getUsbDevice(usbManager);
    }

    /**
     * get the printer settings from the SharedPreferences
     */
    private void getPreferences() {
        if (mPrinterInfo == null) {
            mPrinterInfo = new PrinterInfo();
            return;
        }
        String input;
        mPrinterInfo.printerModel = PrinterInfo.Model.valueOf(sharedPreferences
                .getString("printerModel", ""));
        mPrinterInfo.port = PrinterInfo.Port.valueOf(sharedPreferences
                .getString("port", ""));
        mPrinterInfo.ipAddress = sharedPreferences.getString("address", "");
        mPrinterInfo.macAddress = sharedPreferences.getString("macAddress", "");
        mPrinterInfo.setLocalName(sharedPreferences.getString("localName", ""));

        try {
            if (isLabelPrinter(mPrinterInfo.printerModel)) {
                mPrinterInfo.paperSize = PrinterInfo.PaperSize.CUSTOM;
                switch (mPrinterInfo.printerModel) {
                    case QL_710W:
                    case QL_720NW:
                    case QL_800:
                    case QL_810W:
                    case QL_820NWB:
                        mPrinterInfo.labelNameIndex = LabelInfo.QL700.valueOf(
                                sharedPreferences.getString("paperSize", "")).ordinal();
                        mPrinterInfo.isAutoCut = Boolean.parseBoolean(sharedPreferences
                                .getString("autoCut", ""));
                        mPrinterInfo.isCutAtEnd = Boolean
                                .parseBoolean(sharedPreferences.getString("endCut", ""));
                        break;
                    case QL_1100:
                    case QL_1110NWB:
                        mPrinterInfo.labelNameIndex = LabelInfo.QL1100.valueOf(
                                sharedPreferences.getString("paperSize", "")).ordinal();
                        mPrinterInfo.isAutoCut = Boolean.parseBoolean(sharedPreferences
                                .getString("autoCut", ""));
                        mPrinterInfo.isCutAtEnd = Boolean
                                .parseBoolean(sharedPreferences.getString("endCut", ""));
                        break;
                    case QL_1115NWB:
                        mPrinterInfo.labelNameIndex = LabelInfo.QL1115.valueOf(
                                sharedPreferences.getString("paperSize", "")).ordinal();
                        mPrinterInfo.isAutoCut = Boolean.parseBoolean(sharedPreferences
                                .getString("autoCut", ""));
                        mPrinterInfo.isCutAtEnd = Boolean
                                .parseBoolean(sharedPreferences.getString("endCut", ""));
                        break;
                    case PT_E550W:
                    case PT_E500:
                    case PT_P750W:
                    case PT_P710BT:
                    case PT_P715eBT:
                    case PT_D800W:
                    case PT_E800W:
                    case PT_E850TKW:
                    case PT_P900W:
                    case PT_P950NW:
                    case PT_P910BT:
                        String paper = sharedPreferences.getString("paperSize", "");
                        mPrinterInfo.labelNameIndex = LabelInfo.PT.valueOf(paper)
                                .ordinal();
                        mPrinterInfo.isAutoCut = Boolean.parseBoolean(sharedPreferences
                                .getString("autoCut", ""));
                        mPrinterInfo.isCutAtEnd = Boolean
                                .parseBoolean(sharedPreferences.getString("endCut", ""));
                        mPrinterInfo.isHalfCut = Boolean.parseBoolean(sharedPreferences
                                .getString("halfCut", ""));
                        mPrinterInfo.isSpecialTape = Boolean
                                .parseBoolean(sharedPreferences.getString(
                                        "specialType", ""));
                        break;
                    case PT_P300BT:
                        mPrinterInfo.labelNameIndex = LabelInfo.PT3.valueOf(
                                sharedPreferences.getString("paperSize", "")).ordinal();
                        mPrinterInfo.isCutMark = Boolean
                                .parseBoolean(sharedPreferences.getString(
                                        "cutMark", ""));
                        mPrinterInfo.isCutAtEnd = Boolean
                                .parseBoolean(sharedPreferences.getString("endCut", ""));

                        input = sharedPreferences.getString("labelMargin", "");
                        if (input.equals(""))
                            input = "0";
                        mPrinterInfo.labelMargin = Integer.parseInt(input);

                        break;
                    default:
                        break;
                }
            } else {
                mPrinterInfo.paperSize = PrinterInfo.PaperSize
                        .valueOf(sharedPreferences.getString("paperSize", ""));
                switch (mPrinterInfo.printerModel) {
                    case TD_4410D:
                    case TD_4420DN:
                    case TD_4510D:
                    case TD_4520DN:
                    case TD_4550DNWB:
                        mPrinterInfo.isAutoCut = Boolean.parseBoolean(sharedPreferences.getString("autoCut", ""));
                        mPrinterInfo.isCutAtEnd = Boolean.parseBoolean(sharedPreferences.getString("endCut", ""));
                        break;
                    default:
                        break;
                }
            }
        }
        catch (IllegalArgumentException e) {
            // nop
        }
        mPrinterInfo.orientation = PrinterInfo.Orientation
                .valueOf(sharedPreferences.getString("orientation", ""));
        input = sharedPreferences.getString("numberOfCopies", "1");
        if (input.equals(""))
            input = "1";
        mPrinterInfo.numberOfCopies = Integer.parseInt(input);
        mPrinterInfo.halftone = PrinterInfo.Halftone.valueOf(sharedPreferences
                .getString("halftone", ""));
        mPrinterInfo.printMode = PrinterInfo.PrintMode
                .valueOf(sharedPreferences.getString("printMode", ""));
        mPrinterInfo.pjCarbon = Boolean.parseBoolean(sharedPreferences
                .getString("pjCarbon", ""));
        input = sharedPreferences.getString("pjDensity", "");
        if (input.equals(""))
            input = "5";
        mPrinterInfo.pjDensity = Integer.parseInt(input);
        mPrinterInfo.pjFeedMode = PrinterInfo.PjFeedMode
                .valueOf(sharedPreferences.getString("pjFeedMode", ""));
        mPrinterInfo.align = PrinterInfo.Align.valueOf(sharedPreferences
                .getString("align", ""));
        input = sharedPreferences.getString("leftMargin", "");
        if (input.equals(""))
            input = "0";
        mPrinterInfo.margin.left = Integer.parseInt(input);
        mPrinterInfo.valign = PrinterInfo.VAlign.valueOf(sharedPreferences
                .getString("valign", ""));
        input = sharedPreferences.getString("topMargin", "");
        if (input.equals(""))
            input = "0";
        mPrinterInfo.margin.top = Integer.parseInt(input);
        input = sharedPreferences.getString("customPaperWidth", "");
        if (input.equals(""))
            input = "0";
        mPrinterInfo.customPaperWidth = Integer.parseInt(input);

        input = sharedPreferences.getString("customPaperLength", "0");
        if (input.equals(""))
            input = "0";

        mPrinterInfo.customPaperLength = Integer.parseInt(input);
        input = sharedPreferences.getString("customFeed", "");
        if (input.equals(""))
            input = "0";
        mPrinterInfo.customFeed = Integer.parseInt(input);

        manualCustomPaperSettingsEnabled = Boolean.parseBoolean(sharedPreferences.getString("enableManualCustomPaperSettings", "false"));
        customSetting = sharedPreferences.getString("customSetting", "");
        mPrinterInfo.paperPosition = PrinterInfo.Align
                .valueOf(sharedPreferences.getString("paperPosition", "LEFT"));
        mPrinterInfo.dashLine = Boolean.parseBoolean(sharedPreferences
                .getString("dashLine", "false"));

        mPrinterInfo.rjDensity = Integer.parseInt(sharedPreferences.getString(
                "rjDensity", ""));
        mPrinterInfo.rotate180 = Boolean.parseBoolean(sharedPreferences
                .getString("rotate180", ""));
        mPrinterInfo.peelMode = Boolean.parseBoolean(sharedPreferences
                .getString("peelMode", ""));

        mPrinterInfo.mode9 = Boolean.parseBoolean(sharedPreferences.getString(
                "mode9", ""));
        mPrinterInfo.dashLine = Boolean.parseBoolean(sharedPreferences
                .getString("dashLine", ""));
        input = sharedPreferences.getString("pjSpeed", "2");
        mPrinterInfo.pjSpeed = Integer.parseInt(input);

        mPrinterInfo.pjPaperKind = PrinterInfo.PjPaperKind
                .valueOf(sharedPreferences.getString("pjPaperKind",
                        "PJ_CUT_PAPER"));

        mPrinterInfo.rollPrinterCase = PrinterInfo.PjRollCase
                .valueOf(sharedPreferences.getString("printerCase",
                        "PJ_ROLLCASE_OFF"));

        mPrinterInfo.skipStatusCheck = Boolean.parseBoolean(sharedPreferences
                .getString("skipStatusCheck", "false"));

        mPrinterInfo.checkPrintEnd = PrinterInfo.CheckPrintEnd
                .valueOf(sharedPreferences.getString("checkPrintEnd", "CPE_CHECK"));
        mPrinterInfo.printQuality = PrinterInfo.PrintQuality
                .valueOf(sharedPreferences.getString("printQuality",
                        "NORMAL"));
        mPrinterInfo.overwrite = Boolean.parseBoolean(sharedPreferences
                .getString("overwrite", "true"));

        mPrinterInfo.trimTapeAfterData = Boolean.parseBoolean(sharedPreferences
                .getString("trimTapeAfterData", "false"));

        input = sharedPreferences.getString("imageThresholding", "");
        if (input.equals(""))
            input = "127";
        mPrinterInfo.thresholdingValue = Integer.parseInt(input);

        input = sharedPreferences.getString("scaleValue", "");
        if (input.equals(""))
            input = "0";
        try {
            mPrinterInfo.scaleValue = Double.parseDouble(input);
        } catch (NumberFormatException e) {
            mPrinterInfo.scaleValue = 1.0;
        }

        if (mPrinterInfo.printerModel == Model.TD_4000
                || mPrinterInfo.printerModel == Model.TD_4100N) {
            mPrinterInfo.isAutoCut = Boolean.parseBoolean(sharedPreferences
                    .getString("autoCut", ""));
            mPrinterInfo.isCutAtEnd = Boolean.parseBoolean(sharedPreferences
                    .getString("endCut", ""));
        }

        input = sharedPreferences.getString("savePrnPath", "");
        mPrinterInfo.savePrnPath = input;


        mPrinterInfo.workPath = sharedPreferences.getString("workPath", "");
        mPrinterInfo.softFocusing = Boolean.parseBoolean(sharedPreferences
                .getString("softFocusing", "false"));
        mPrinterInfo.enabledTethering = Boolean.parseBoolean(sharedPreferences
                .getString("enabledTethering", "false"));
        mPrinterInfo.rawMode = Boolean.parseBoolean(sharedPreferences
                .getString("rawMode", "false"));


        input = sharedPreferences.getString("processTimeout", "");
        if (input.equals(""))
            input = "0";
        mPrinterInfo.timeout.processTimeoutSec = Integer.parseInt(input);

        input = sharedPreferences.getString("sendTimeout", "");
        if (input.equals(""))
            input = "60";
        mPrinterInfo.timeout.sendTimeoutSec = Integer.parseInt(input);

        input = sharedPreferences.getString("receiveTimeout", "");
        if (input.equals(""))
            input = "180";
        mPrinterInfo.timeout.receiveTimeoutSec = Integer.parseInt(input);

        input = sharedPreferences.getString("connectionTimeout", "");
        if (input.equals(""))
            input = "0";
        mPrinterInfo.timeout.connectionWaitMSec = Integer.parseInt(input);

        input = sharedPreferences.getString("closeWaitTime", "");
        if (input.equals(""))
            input = "3";
        mPrinterInfo.timeout.closeWaitDisusingStatusCheckSec = Integer.parseInt(input);

        mPrinterInfo.useLegacyHalftoneEngine = Boolean.parseBoolean(sharedPreferences
                .getString("useLegacyHalftoneEngine", "false"));
    }

    /**
     * Launch the thread to print
     */
    public void print() {
        mCancel = false;
        PrinterThread printTread = new PrinterThread();
        printTread.start();
    }

    /**
     * Launch the thread to get the printer's status
     */
    public void getPrinterStatus() {
        mCancel = false;
        getStatusThread getTread = new getStatusThread();
        getTread.start();
    }

    /**
     * Launch the thread to print
     */
    public void sendFile() {


        SendFileThread getTread = new SendFileThread();
        getTread.start();
    }

    /**
     * set custom paper for RJ and TD
     */
    private BasePrintResult setCustomPaper() {
        BasePrintResult result;
        switch (mPrinterInfo.printerModel) {
            case RJ_4030:
            case RJ_4030Ai:
            case RJ_4040:
            case RJ_3050:
            case RJ_3150:
            case TD_2020:
            case TD_2120N:
            case TD_2130N:
            case TD_4100N:
            case TD_4000:
            case RJ_2030:
            case RJ_2140:
            case RJ_2150:
            case RJ_2050:
            case RJ_3050Ai:
            case RJ_3150Ai:
            case RJ_4230B:
            case RJ_4250WB:
            case TD_4410D:
            case TD_4420DN:
            case TD_4510D:
            case TD_4520DN:
            case TD_4550DNWB:
            case RJ_3230B:
            case RJ_3250WB:
                if (manualCustomPaperSettingsEnabled) {
                    result = setManualCustomPaper(mPrinterInfo.printerModel);
                } else {
                    mPrinterInfo.customPaper = Common.CUSTOM_PAPER_FOLDER + customSetting;
                    result = setManualCustomPaper(null);
                }
                break;
            default:
                result = BasePrintResult.success();
                break;
        }
        return result;
    }

    private BasePrintResult setManualCustomPaper(Model printerModel) {
        if (printerModel == null) {
            mPrinterInfo.setCustomPaperInfo(null);
            return BasePrintResult.success();
        }

        PaperKind paperKind = PaperKind.valueOf(sharedPreferences.getString("rjPaperKind", "ROLL"));
        float width = parseFloat(sharedPreferences.getString("rjPaperWidth", ""), 0.0f);
        float length = parseFloat(sharedPreferences.getString("rjPaperLength", ""), 0.0f);
        float rightMargin = parseFloat(sharedPreferences.getString("rjPaperRightMargin", ""), 0.0f);
        float leftMargin = parseFloat(sharedPreferences.getString("rjPaperLeftMargin", ""), 0.0f);
        float topMargin = parseFloat(sharedPreferences.getString("rjPaperTopMargin", ""), 0.0f);
        float bottomMargin = parseFloat(sharedPreferences.getString("rjPaperBottomMargin", ""), 0.0f);
        float labelPitch = parseFloat(sharedPreferences.getString("rjPaperLabelPitch", ""), 0.0f);
        float markPosition = parseFloat(sharedPreferences.getString("rjPaperMarkPosition", ""), 0.0f);
        float markHeight = parseFloat(sharedPreferences.getString("rjPaperMarkHeight", ""), 0.0f);
        Unit unit = Unit.valueOf(sharedPreferences.getString("rjPaperUnit", "Mm"));

        CustomPaperInfo customPaperInfo;
        switch (paperKind) {
            case DIE_CUT:
                customPaperInfo = CustomPaperInfo.newCustomDieCutPaper(printerModel, unit, width, length, rightMargin, leftMargin, topMargin, bottomMargin, labelPitch);
                break;
            case MARKED_ROLL:
                customPaperInfo = CustomPaperInfo.newCustomMarkRollPaper(printerModel, unit, width, length, rightMargin, leftMargin, topMargin, bottomMargin, markPosition, markHeight);
                break;
            case ROLL:
            default:
                customPaperInfo = CustomPaperInfo.newCustomRollPaper(printerModel, unit, width, rightMargin, leftMargin, topMargin);
                break;
        }

        List<Map<CustomPaperInfo.ErrorParameter, CustomPaperInfo.ErrorDetail>> errors = customPaperInfo.validate();
        if (errors.isEmpty()) {
            mPrinterInfo.setCustomPaperInfo(customPaperInfo);
            return BasePrintResult.success();
        } else {
            // TODO: Humal Readable
            return BasePrintResult.fail(errors.toString());
        }
    }

    private float parseFloat(String s, float defaultValue) {
        try {
            return Float.parseFloat(s);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * get the end message of print
     */
    @SuppressWarnings("UnusedAssignment")
    public String showResult() {
        if (mPrintResult == null) {
            return "";
        }

        String result;
        if (mPrintResult.errorCode == ErrorCode.ERROR_NONE) {
            result = mContext.getString(R.string.error_message_none);
        } else {
            result = mPrintResult.errorCode.toString();
        }

        return result;
    }

    /**
     * show information of battery
     */
    public String getBattery() {

        if(mPrintResult.isACConnected == BatteryTernary.Yes) {
            return mContext.getString(R.string.ac_adapter);
        }

        if(mPrintResult.maxOfBatteryResidualQuantityLevel == 0){
            return mContext.getString(R.string.battery_full);
        }
        else if(mPrintResult.maxOfBatteryResidualQuantityLevel == 2){
            switch (mPrintResult.batteryResidualQuantityLevel) {
                case 0:
                    return mContext.getString(R.string.battery_weak);
                case 1:
                    return mContext.getString(R.string.battery_middle);
                case 2:
                    return mContext.getString(R.string.battery_full);
                default:
                    break;
            }
        }
        else if(mPrintResult.maxOfBatteryResidualQuantityLevel == 3){
            switch (mPrintResult.batteryResidualQuantityLevel) {
                case 0:
                    return mContext.getString(R.string.battery_charge);
                case 1:
                    return mContext.getString(R.string.battery_weak);
                case 2:
                    return mContext.getString(R.string.battery_middle);
                case 3:
                    return mContext.getString(R.string.battery_full);
                default:
                    break;
            }
        }
        else if(mPrintResult.maxOfBatteryResidualQuantityLevel == 4){
            switch (mPrintResult.batteryResidualQuantityLevel) {
                case 0:
                    return mContext.getString(R.string.battery_charge);
                case 1:
                    return mContext.getString(R.string.battery_weak);
                case 2:
                    return mContext.getString(R.string.battery_middle);
                case 3:
                    return mContext.getString(R.string.battery_middle);
                case 4:
                    return mContext.getString(R.string.battery_full);
                default:
                    break;
            }
        }
        else if(mPrintResult.maxOfBatteryResidualQuantityLevel == 100){
            if (mPrintResult.batteryResidualQuantityLevel > 80) {
                return mContext.getString(R.string.battery_full);
            } else if (30 <= mPrintResult.batteryResidualQuantityLevel
                    && mPrintResult.batteryResidualQuantityLevel <= 80) {
                return mContext.getString(R.string.battery_middle);
            } else if (0 <= mPrintResult.batteryResidualQuantityLevel
                    && mPrintResult.batteryResidualQuantityLevel < 30) {
                return mContext.getString(R.string.battery_weak);
            }
        }
        else {
            double ratio = (double)mPrintResult.batteryResidualQuantityLevel / mPrintResult.maxOfBatteryResidualQuantityLevel;
            if (ratio > 0.8) {
                return mContext.getString(R.string.battery_full);
            } else if (0.3 <= ratio && ratio <= 0.8) {
                return mContext.getString(R.string.battery_middle);
            } else if (0 <= ratio && ratio < 0.3) {
                return mContext.getString(R.string.battery_weak);
            }
        }
        return "";
    }

    public String getBatteryDetail() {
        if (mPrintResult == null) {
            return "";
        }
        return  String.format("%d/%d(AC=%s,BM=%s)",
                mPrintResult.batteryResidualQuantityLevel,
                mPrintResult.maxOfBatteryResidualQuantityLevel,
                mPrintResult.isACConnected.name(),
                mPrintResult.isBatteryMounted.name());
    }

    private boolean isLabelPrinter(PrinterInfo.Model model) {
        switch (model) {
            case QL_710W:
            case QL_720NW:
            case PT_E550W:
            case PT_E500:
            case PT_P750W:
            case PT_D800W:
            case PT_E800W:
            case PT_E850TKW:
            case PT_P900W:
            case PT_P950NW:
            case QL_810W:
            case QL_800:
            case QL_820NWB:
            case PT_P300BT:
            case QL_1100:
            case QL_1110NWB:
            case QL_1115NWB:
            case PT_P710BT:
            case PT_P715eBT:
            case PT_P910BT:
                return true;
            default:
                return false;
        }
    }

    /**
     * Thread for printing
     */
    private class PrinterThread extends Thread {
        @Override
        public void run() {

            // set info. for printing
            BasePrintResult setPrinterInfoResult = setPrinterInfo();
            if (setPrinterInfoResult.success == false) {
                mHandle.setResult(setPrinterInfoResult.errorMessage);
                mHandle.sendMessage(mHandle.obtainMessage(Common.MSG_PRINT_END));
                return;
            }

            // start message
            Message msg = mHandle.obtainMessage(Common.MSG_PRINT_START);
            mHandle.sendMessage(msg);

            mPrintResult = new PrinterStatus();

            mPrinter.startCommunication();
            if (!mCancel) {
                doPrint();
            } else {
                mPrintResult.errorCode = ErrorCode.ERROR_CANCEL;
            }
            mPrinter.endCommunication();

            // end message
            mHandle.setResult(showResult());
            mHandle.setBattery(getBatteryDetail());
            msg = mHandle.obtainMessage(Common.MSG_PRINT_END);
            mHandle.sendMessage(msg);
        }
    }

    /**
     * Thread for getting the printer's status
     */
    private class getStatusThread extends Thread {
        @Override
        public void run() {

            // set info. for printing
            setPrinterInfo();

            // start message
            Message msg = mHandle.obtainMessage(Common.MSG_PRINT_START);
            mHandle.sendMessage(msg);

            mPrintResult = new PrinterStatus();
            if (!mCancel) {
                mPrintResult = mPrinter.getPrinterStatus();
            } else {
                mPrintResult.errorCode = ErrorCode.ERROR_CANCEL;
            }
            // end message
            mHandle.setResult(showResult());
            mHandle.setBattery(getBatteryDetail());
            msg = mHandle.obtainMessage(Common.MSG_PRINT_END);
            mHandle.sendMessage(msg);

        }
    }

    /**
     * Thread for getting the printer's status
     */
    private class SendFileThread extends Thread {
        @Override
        public void run() {

            // set info. for printing
            BasePrintResult setPrinterInfoResult = setPrinterInfo();
            if (setPrinterInfoResult.success == false) {
                mHandle.setResult(setPrinterInfoResult.errorMessage);
                mHandle.sendMessage(mHandle.obtainMessage(Common.MSG_PRINT_END));
                return;
            }

            // start message
            Message msg = mHandle.obtainMessage(Common.MSG_PRINT_START);
            mHandle.sendMessage(msg);

            mPrintResult = new PrinterStatus();

            mPrinter.startCommunication();

            doPrint();

            mPrinter.endCommunication();
            // end message
            mHandle.setResult(showResult());
            mHandle.setBattery(getBatteryDetail());
            msg = mHandle.obtainMessage(Common.MSG_PRINT_END);
            mHandle.sendMessage(msg);

        }
    }
}
