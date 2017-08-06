package com.example.zengzy19585.carpool.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.zengzy19585.carpool.R;
import com.example.zengzy19585.carpool.entity.Friends;

import java.util.ArrayList;

/**
 * Created by zaynr on 2017/8/5.
 */

public class DriverServeListViweAdapter extends BaseAdapter {
    private ArrayList<Friends> friendses;
    private Context context;

    private class ViewHolder{
        TextView friendName, friendContact, serveCount;
    }

    public DriverServeListViweAdapter(Context context, ArrayList<Friends> friendses){
        this.context = context;
        this.friendses = friendses;
    }

    @Override
    public int getCount() {
        return friendses.size();
    }

    @Override
    public Object getItem(int i) {
        return friendses.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Friends friends = friendses.get(i);
        ViewHolder viewHolder;
        if(view == null){
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(context);
            view = inflater.inflate(R.layout.show_friend_list, viewGroup, false);
            viewHolder.friendContact = view.findViewById(R.id.friend_contact);
            viewHolder.friendName = view.findViewById(R.id.friend_name);
            viewHolder.serveCount = view.findViewById(R.id.serve_count);

            view.setTag(viewHolder);
        }
        else{
            viewHolder = (ViewHolder) view.getTag();
        }

        if(friendses.get(i).getType().contains("driver")){
            viewHolder.friendName.setText(friendses.get(i).getCall_name());
            viewHolder.friendContact.setText(friendses.get(i).getCall_mobile_num());
            viewHolder.serveCount.setText("服务次数：" + String.valueOf(friendses.get(i).getServe_count()) + "\n");
        }
        else{
            viewHolder.friendName.setText(friendses.get(i).getCall_name());
            viewHolder.friendContact.setText(friendses.get(i).getCall_mobile_num());
        }
        return view;
    }
}
