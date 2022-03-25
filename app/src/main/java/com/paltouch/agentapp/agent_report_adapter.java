package com.paltouch.agentapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class agent_report_adapter extends BaseAdapter {
    private Context context;
    private ArrayList<agent_report_model> agent_report_model_arraylist;

    public agent_report_adapter(Context context, ArrayList<agent_report_model>agemt_report_model_arraylist){
        this.context = context;
        this.agent_report_model_arraylist = agemt_report_model_arraylist;
    }

    @Override
    public int getViewTypeCount() {
        return getCount();
    }
    @Override
    public int getItemViewType(int position) {

        return position;
    }

    @Override
    public int getCount() {
        return agent_report_model_arraylist.size();
    }

    @Override
    public Object getItem(int position) {
        return agent_report_model_arraylist.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.agent_report_itemlist, null, true);

            holder.txt_full_name = (TextView) convertView.findViewById(R.id.txt_fullname);
            holder.txt_account_name = (TextView) convertView.findViewById(R.id.txt_account_name);
            holder.txt_amount = (TextView) convertView.findViewById(R.id.txt_amount);

            convertView.setTag(holder);
        }else {
            // the getTag returns the viewHolder object set as a tag to the view
            holder = (ViewHolder)convertView.getTag();
        }

        holder.txt_full_name.setText(agent_report_model_arraylist.get(position).getFull_name());
        holder.txt_account_name.setText(String.valueOf(agent_report_model_arraylist.get(position).getAccount_name()));
        holder.txt_amount.setText(agent_report_model_arraylist.get(position).getAmount());

        return convertView;
    }

    private class ViewHolder {

        protected TextView txt_full_name,txt_account_name,txt_amount;

    }
}
