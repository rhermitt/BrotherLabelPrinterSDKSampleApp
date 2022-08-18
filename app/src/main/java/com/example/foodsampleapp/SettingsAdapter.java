/**
 * Adapter for the settings listview.
 */

package com.example.foodsampleapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SettingsAdapter extends BaseAdapter {

    private ArrayList<Map.Entry<String, String>> settings;

    public SettingsAdapter(HashMap<String, String> printerSettings){
        this.settings = new ArrayList<>();
        this.settings.addAll(printerSettings.entrySet());
    }

    @Override
    public int getCount() {
        return settings.size();
    }

    @Override
    public Object getItem(int position) {
        return settings.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View result;

        if(convertView == null){
            result = LayoutInflater.from(parent.getContext()).inflate(R.layout.settings_menu_row, parent, false);
        }
        else{
            result = convertView;
        }

        TextView setting = (TextView)result.findViewById(R.id.tv_settingName);
        TextView value = (TextView)result.findViewById(R.id.tv_settingValue);

        setting.setText(this.settings.get(position).getKey());



        String valueForSetting = this.settings.get(position).getValue();
        if(valueForSetting == null){
            value.setText("");
        }
        else{
            value.setText(valueForSetting);
        }

        return result;
    }
}
