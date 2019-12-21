package tz.co.nezatech.apps.twigapmt.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import tz.co.nezatech.apps.twigapmt.model.IdName;

public class IdNameSpinnerAdapter extends ArrayAdapter<IdName> {

    private Context context;
    private IdName[] values;

    public IdNameSpinnerAdapter(Context context, int textViewResourceId, IdName[] values) {
        super(context, textViewResourceId, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public int getCount(){
        return values.length;
    }

    @Override
    public IdName getItem(int position){
        return values[position];
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView label = (TextView) super.getView(position, convertView, parent);
        label.setTextColor(Color.BLACK);
        label.setText(values[position].getName());
        return label;
    }

    @Override
    public View getDropDownView(int position, View convertView,ViewGroup parent) {
        TextView label = (TextView) super.getDropDownView(position, convertView, parent);
        label.setTextColor(Color.BLACK);
        label.setText(values[position].getName());
        return label;
    }
}