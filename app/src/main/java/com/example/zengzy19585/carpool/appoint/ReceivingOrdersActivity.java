package com.example.zengzy19585.carpool.appoint;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import com.example.zengzy19585.carpool.R;
import com.example.zengzy19585.carpool.entity.Orders;
import com.example.zengzy19585.carpool.utils.GetDistanceUitl;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class ReceivingOrdersActivity extends AppCompatActivity {
    private GetDistanceUitl distanceUitl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiving_orders);
        setContentView(R.layout.activity_orders_manage);
        ListView listView = (ListView)findViewById(R.id.order_list);
        AsyncHttpClient client = new AsyncHttpClient();
        String url = "http://23.83.250.227:8080/order/get-all-undone-order.do";
        client.post(url, null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try{
                    JSONArray jsonArray = new JSONArray(responseBody);
                    ArrayList<Orders> orderses = new ArrayList<>();
                    for(int i = 0; i < jsonArray.length(); i++){
                        if(jsonArray.getJSONObject(i).getString("ori_lat")==null){
                            continue;
                        }
                        Orders order = new Orders();
                        order.setAptTime(jsonArray.getJSONObject(i).getString("apt_time"));
                        order.setSerialNum(jsonArray.getJSONObject(i).getString("serial_num"));
                        order.setDistance("2km");
                        order.setDestAddress("111");
                        order.setDestAddress("222");

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
