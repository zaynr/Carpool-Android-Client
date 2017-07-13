package com.example.zengzy19585.carpool.appoint;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import com.example.zengzy19585.carpool.R;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class OrdersManageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders_manage);
        ListView listView = (ListView)findViewById(R.id.order_list);
        AsyncHttpClient client = new AsyncHttpClient();
        String url = "http://23.83.250.227:8080/order/get-all-undone-order.do";
        client.post(url, null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try{
                    JSONArray jsonArray = new JSONArray(responseBody);
                    for(int i = 0; i < jsonArray.length(); i++){

                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(getApplicationContext(), "网络错误！", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
