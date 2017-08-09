package com.example.zengzy19585.carpool.account;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
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
    private ArrayList<Friends> friendses = null;

    public void packDriverServe(byte[] responseBody){
        try{
            friendses.clear();
            JSONArray array = new JSONArray(new String(responseBody));
            for(int i = 0; i < array.length(); i++){
                JSONObject object = array.getJSONObject(i);
                Friends friends = new Friends();
                friends.setType("driver");
                friends.setCall_name("用户名：" + object.getString("call_name"));
                friends.setCall_mobile_num("手机号：" + object.getString("call_mobile_num"));
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
    }

    public void packCustomerFriend(byte[] responseBody){
        try{
            friendses.clear();
            JSONArray array = new JSONArray(new String(responseBody));
            for(int i = 0; i < array.length(); i++){
                JSONObject object = array.getJSONObject(i);
                Friends friends = new Friends();
                friends.setType("customer");
                friends.setCall_name(object.getString("user_name"));
                friends.setCall_mobile_num("手机号：" + object.getString("mobile_number"));
                friendses.add(friends);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_manage);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        userInfo = new SharedPreferencesUtil(getApplicationContext(), "userInfo");
        listView = (ListView) findViewById(R.id.friend_list);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.friend_refresh);

        final AsyncHttpClient client = new AsyncHttpClient();
        final String url;
        final RequestParams params = new RequestParams();
        if(userInfo.getStringValue("userType").contains("customer")) {
            url = "http://23.83.250.227:8080/friend/show-friends.do";
            params.put("userial1", userInfo.getStringValue("userName"));
            setTitle("查看好友");
        }
        else{
            url = "http://23.83.250.227:8080/friend/get-driver-serve.do";
            params.put("rec_mobile_num", userInfo.getStringValue("userName"));
            setTitle("查看常客");
        }
        friendses = new ArrayList<>();
        refreshLayout.setRefreshing(true);
        final DriverServeListViweAdapter adapter = new DriverServeListViweAdapter(FriendManageActivity.this, friendses);
        client.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if(userInfo.getStringValue("userType").contains("customer")) {
                    packCustomerFriend(responseBody);
                    listView.setAdapter(adapter);
                }
                else{
                    packDriverServe(responseBody);
                    listView.setAdapter(adapter);
                }
                refreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(getApplicationContext(), "网络错误！", Toast.LENGTH_SHORT).show();
                refreshLayout.setRefreshing(false);
            }
        });

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                client.post(url, params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        if(userInfo.getStringValue("userType").contains("customer")) {
                            packCustomerFriend(responseBody);
                            adapter.notifyDataSetChanged();
                        }
                        else{
                            packDriverServe(responseBody);
                            adapter.notifyDataSetChanged();
                        }
                        refreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Toast.makeText(getApplicationContext(), "网络错误！", Toast.LENGTH_SHORT).show();
                        refreshLayout.setRefreshing(false);
                    }
                });
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddFriendDialog dialog = new AddFriendDialog(FriendManageActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.show();
            }
        });
    }

    private class AddFriendDialog extends Dialog{

        public AddFriendDialog(@NonNull Context context) {
            super(context);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.update_password_dialog);
            final EditText oldPwd;
            TextView textView = findViewById(R.id.pwd_label);
            LinearLayout layout = findViewById(R.id.linearLayout);
            layout.setVisibility(View.INVISIBLE);
            Button commit;
            textView.setText("添加好友的工号：");
            commit = findViewById(R.id.commit);
            oldPwd = findViewById(R.id.new_pwd);
            oldPwd.setInputType(InputType.TYPE_CLASS_NUMBER);
            commit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String url = "http://23.83.250.227:8080/friend/accept-request.do";
                    AsyncHttpClient client = new AsyncHttpClient();
                    RequestParams params = new RequestParams();
                    params.put("userial1", userInfo.getStringValue("userName"));
                    params.put("userial2", oldPwd.getText());
                    client.post(url, params, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            if(new String(responseBody).contains("success")){
                                Toast.makeText(getApplicationContext(), "添加成功！", Toast.LENGTH_SHORT).show();
                                dismiss();
                            }
                            else if(new String(responseBody).contains("no_such_man")){
                                Toast.makeText(getApplicationContext(), "查无此人！", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                Toast.makeText(getApplicationContext(), "添加失败！", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            Toast.makeText(getApplicationContext(), "网络错误！", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
    }

}
