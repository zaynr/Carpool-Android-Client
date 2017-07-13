package com.example.zengzy19585.carpool.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.zengzy19585.carpool.R;
import com.example.zengzy19585.carpool.entity.Orders;

import java.util.ArrayList;

/**
 * Created by zaynr on 2017/7/13.
 */

public class RecOrderListViewAdapter extends BaseAdapter {
    private ArrayList<Orders> orders;
    private Context context;

    public RecOrderListViewAdapter(ArrayList<Orders> orders, Context context){
        this.orders = orders;
        this.context = context;
    }

    private class ViewHolder{
        TextView serialNum, oriAddress, destAddress, distance, aptTime;
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        // Get the data item for this position
        Orders order = orders.get(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.rec_order_list_adapter, viewGroup, false);
            viewHolder.serialNum = convertView.findViewById(R.id.order_serial_num);
            viewHolder.oriAddress = convertView.findViewById(R.id.ori_address);
            viewHolder.destAddress = convertView.findViewById(R.id.dest_address);
            viewHolder.distance = convertView.findViewById(R.id.distance);
            viewHolder.aptTime = convertView.findViewById(R.id.order_apt_time);

            result=convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }

        viewHolder.serialNum.setText(order.getSerialNum());
        viewHolder.oriAddress.setText(order.getOriAddress());
        viewHolder.destAddress.setText(order.getDestAddress());
        viewHolder.distance.setText(order.getDistance());
        viewHolder.aptTime.setText(order.getAptTime());
        // Return the completed view to render on screen
        return convertView;
    }
}
