package com.example.zengzy19585.carpool.appoint;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.example.zengzy19585.carpool.R;
import com.example.zengzy19585.carpool.adapter.RecOrderListViewAdapter;
import com.example.zengzy19585.carpool.entity.Orders;
import com.example.zengzy19585.carpool.utils.GetDistanceUtil;
import com.example.zengzy19585.carpool.utils.SharedPreferencesUtil;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class OrdersManageActivity extends AppCompatActivity{

    private ArrayList<Orders> orders;
    private ArrayList<LatLng> points;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders_manage);
        listView = (ListView) findViewById(R.id.order_list);
        points = new ArrayList<>();
        orders = new ArrayList<>();
        AsyncHttpClient client = new AsyncHttpClient();
        SharedPreferencesUtil preferencesUtil = new SharedPreferencesUtil(getApplicationContext(), "userInfo");
        String url;
        RequestParams params = new RequestParams();
        if(preferencesUtil.getStringValue("userType").contains("driver")) {
            url = "http://23.83.250.227:8080/order/get-by-rec.do";
            params.put("rec_mobile_num", preferencesUtil.getStringValue("userName"));
        }
        else{
            url = "http://23.83.250.227:8080/order/get-by-call.do";
            params.put("call_serial", preferencesUtil.getStringValue("userName"));
        }
        client.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    JSONArray jsonArray = new JSONArray(new String(responseBody));
                    for (int i = 0; i < jsonArray.length(); i++) {
                        if (jsonArray.getJSONObject(i).getString("ori_lat") == null
                                || jsonArray.getJSONObject(i).getString("ori_lng") == null) {
                            continue;
                        }
                        Orders order = new Orders();
                        order.setOriAddress(jsonArray.getJSONObject(i).getString("ori_address"));
                        order.setDestAddress(jsonArray.getJSONObject(i).getString("des_address"));
                        order.setAptTime(jsonArray.getJSONObject(i).getString("apt_time"));
                        order.setSerialNum(jsonArray.getJSONObject(i).getString("serial_num"));
                        LatLng start = new LatLng(Double.parseDouble(jsonArray.getJSONObject(i).getString("ori_lat"))
                                , Double.parseDouble(jsonArray.getJSONObject(i).getString("ori_lng")));
                        LatLng end = new LatLng(Double.parseDouble(jsonArray.getJSONObject(i).getString("des_lat"))
                                , Double.parseDouble(jsonArray.getJSONObject(i).getString("des_lng")));
                        GetDistanceUtil util = new GetDistanceUtil(start, end);
                        order.setStart(start);
                        order.setEnd(end);
                        order.setDistance(String.valueOf(util.getDistance()) + "米");
                        orders.add(order);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                listView.setAdapter(new RecOrderListViewAdapter(orders, getApplicationContext()));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(getApplicationContext(), "网络错误！", Toast.LENGTH_SHORT).show();
            }
        });
    }

}