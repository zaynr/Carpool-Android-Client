package com.example.zengzy19585.carpool.account;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.example.zengzy19585.carpool.R;
import com.example.zengzy19585.carpool.adapter.DriverServeListViweAdapter;
import com.example.zengzy19585.carpool.entity.Friends;
import com.example.zengzy19585.carpool.utils.SharedPreferencesUtil;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import cz.msebera.android.httpclient.Header;

public class FriendManageActivity extends AppCompatActivity {
    private SharedPreferencesUtil userInfo;
    private ListView listView;
    private SwipeRefreshLayout refreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_manage);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        userInfo = new SharedPreferencesUtil(getApplicationContext(), "userInfo");
        listView = (ListView) findViewById(R.id.friend_list);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.friend_refresh);
        final DriverServeListViweAdapter adapter;

        AsyncHttpClient client = new AsyncHttpClient();
        String url;
        RequestParams params = new RequestParams();
        if(userInfo.getStringValue("userType").contains("customer")) {
            url = "http://23.83.250.227:8080/friend/show-friends.do";
            params.put("userial1", userInfo.getStringValue("userName"));
        }
        else{
            url = "http://23.83.250.227:8080/friend/get-driver-serve.do";
            params.put("rec_mobile_num", userInfo.getStringValue("userName"));
        }
        final ArrayList<Friends> friendses = new ArrayList<>();
        adapter = new DriverServeListViweAdapter(FriendManageActivity.this, friendses);
        client.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if(userInfo.getStringValue("userType").contains("customer")) {
                }
                else{
                    try{
                        JSONArray array = new JSONArray(new String(responseBody));
                        for(int i = 0; i < array.length(); i++){
                            JSONObject object = array.getJSONObject(i);
                            Friends friends = new Friends();
                            friends.setType("driver");
                            friends.setCall_name(object.getString("call_name"));
                            friends.setCall_mobile_num(object.getString("call_mobile_num"));
                            friends.setServe_count(Integer.parseInt(object.getString("serve_count")));
                            friendses.add(friends);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    Collections.sort(friendses, new Comparator<Friends>() {
                        @Override
                        public int compare(Friends f1, Friends f2) {
                            return f1.getServe_count() - f2.getServe_count();
                        }
                    });
                    listView.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(getApplicationContext(), "网络错误！", Toast.LENGTH_SHORT).show();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

}
