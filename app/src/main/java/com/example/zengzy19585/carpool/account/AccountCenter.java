package com.example.zengzy19585.carpool.account;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.Text;
import com.example.zengzy19585.carpool.R;
import com.example.zengzy19585.carpool.utils.Md5Generator;
import com.example.zengzy19585.carpool.utils.SharedPreferencesUtil;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.math.BigInteger;
import java.security.MessageDigest;

import cz.msebera.android.httpclient.Header;

public class AccountCenter extends AppCompatActivity {
    private SharedPreferencesUtil userInfo;
    private EditText name, sex, mobile_num;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_center);
        userInfo = new SharedPreferencesUtil(getApplicationContext(), "userInfo");
        final Button logOut = (Button) findViewById(R.id.log_out);
        final Button update = (Button) findViewById(R.id.update);
        final Button password = (Button) findViewById(R.id.password);
        name = (EditText) findViewById(R.id.textView);
        sex = (EditText) findViewById(R.id.textView3);
        mobile_num = (EditText) findViewById(R.id.textView2);
        password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdatePasswordDialog dialog = new UpdatePasswordDialog(AccountCenter.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.show();
            }
        });
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(update.getText().toString().contains("更改")){
                    name.setFocusableInTouchMode(true);
                    sex.setFocusableInTouchMode(true);
                    mobile_num.setFocusableInTouchMode(true);
                    update.setText("提交");
                }
                else{
                    name.setFocusable(false);
                    sex.setFocusable(false);
                    mobile_num.setFocusable(false);
                    AsyncHttpClient client = new AsyncHttpClient();
                    String url = "http://23.83.250.227:8080/customer/update-user-info.do";
                    RequestParams params = new RequestParams();
                    params.add("serial_num", userInfo.getStringValue("userName"));
                    params.add("mobile_number", mobile_num.getText().toString());
                    params.add("sex", sex.getText().toString());
                    params.add("user_name", name.getText().toString());
                    client.post(url, params, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            Toast.makeText(getApplicationContext(), "个人信息更新成功"
                                    , Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                        }
                    });
                    update.setText("更改基本信息");
                }
            }
        });
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
                    try{
                        JSONObject object = new JSONObject(new String(responseBody));
                        name.setText(object.getString("user_name"));
                        sex.setText(object.getString("sex"));
                        mobile_num.setText(object.getString("mobile_number"));
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Toast.makeText(getApplicationContext(), "网络错误"
                            , Toast.LENGTH_SHORT).show();
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

    private class UpdatePasswordDialog extends Dialog{

        public UpdatePasswordDialog(@NonNull Context context) {
            super(context);
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.update_password_dialog);
            final EditText newPwd, oldPwd;
            Button commit;
            commit = findViewById(R.id.commit);
            newPwd = findViewById(R.id.new_pwd);
            oldPwd = findViewById(R.id.old_pwd);
            commit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AsyncHttpClient client = new AsyncHttpClient();
                    String url = "http://23.83.250.227:8080/customer/update-password.do";
                    RequestParams params = new RequestParams();
                    params.put("new_pwd", Md5Generator.generate(newPwd.getText().toString()));
                    params.put("old_pwd", Md5Generator.generate(oldPwd.getText().toString()));
                    params.put("serial_num", userInfo.getStringValue("userName"));
                    client.post(url, params, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            if(new String(responseBody).contains("success")){
                                Toast.makeText(getApplicationContext(), "密码更改成功"
                                        , Toast.LENGTH_SHORT).show();
                                dismiss();
                            }
                            else{
                                Toast.makeText(getApplicationContext(), "原始密码错误"
                                        , Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            Toast.makeText(getApplicationContext(), "网络错误"
                                    , Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
    }
}
