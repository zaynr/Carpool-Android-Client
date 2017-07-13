package com.example.zengzy19585.carpool.account;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.zengzy19585.carpool.R;
import com.example.zengzy19585.carpool.utils.SharedPreferencesUtil;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import cz.msebera.android.httpclient.Header;

public class AccountCenter extends AppCompatActivity {
    private SharedPreferencesUtil userInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_center);
        userInfo = new SharedPreferencesUtil(getApplicationContext(), "userInfo");
        Button logOut = (Button) findViewById(R.id.log_out);
        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userInfo.setStringValue("userStatus", "notLoggedIn");
                userInfo.setStringValue("userName", "未登录");
                userInfo.setStringValue("userType", "visitor");
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        if(userInfo.getStringValue("userType").equals("customer")) {
            params.put("serial_num", userInfo.getStringValue("userName"));
            String url = "http://23.83.250.227:8080/customer/retrieve-customer-info.do";
            client.post(url, params, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                }
            });
        }
        else if(userInfo.getStringValue("userType").equals("driver")) {
            params.put("mobile_number", userInfo.getStringValue("userName"));
            String url = "http://23.83.250.227:8080/driver/retrieve-driver-info.do";
            client.post(url, params, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                }
            });
        }
    }
}
