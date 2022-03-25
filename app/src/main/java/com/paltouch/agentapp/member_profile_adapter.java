package com.paltouch.agentapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class member_profile_adapter extends BaseAdapter {
    private Context context;
    private ArrayList<member_profile_model>member_profile_model_arraylist;

    public member_profile_adapter(Context context, ArrayList<member_profile_model>member_profile_model_arraylist){
        this.context = context;
        this.member_profile_model_arraylist = member_profile_model_arraylist;
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
        return member_profile_model_arraylist.size();
    }

    @Override
    public Object getItem(int position) {
        return member_profile_model_arraylist.get(position);
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
            convertView = inflater.inflate(R.layout.list_item, null, true);

            holder.txtvdate = (TextView) convertView.findViewById(R.id.txtvdate);
            holder.txtreceipt_no = (TextView) convertView.findViewById(R.id.txtreceipt_no);
            holder.txtin = (TextView) convertView.findViewById(R.id.txtin);
            holder.txtout = (TextView) convertView.findViewById(R.id.txtout);
            holder.txtbal = (TextView) convertView.findViewById(R.id.txtbal);

            convertView.setTag(holder);
        }else {
            // the getTag returns the viewHolder object set as a tag to the view
            holder = (ViewHolder)convertView.getTag();
        }

        holder.txtvdate.setText(member_profile_model_arraylist.get(position).getV_date());
        holder.txtreceipt_no.setText(String.valueOf(member_profile_model_arraylist.get(position).getReceipt_number()));
        holder.txtin.setText(member_profile_model_arraylist.get(position).getMoney_in());
        holder.txtout.setText(member_profile_model_arraylist.get(position).getMoney_out());
        holder.txtbal.setText(member_profile_model_arraylist.get(position).getBalance());

        return convertView;
    }

    private class ViewHolder {

        protected TextView txtvdate,txtreceipt_no,txtin,txtout,txtbal;

    }
}
