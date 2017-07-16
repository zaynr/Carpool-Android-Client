package com.example.zengzy19585.carpool.appoint;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class ReceivingOrdersActivity extends AppCompatActivity implements OnGetGeoCoderResultListener {

    private GeoCoder mSearch = null;
    private ArrayList<Orders> orders;
    private ArrayList<LatLng> points;
    private ListView listView;
    private int index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiving_orders);
        // 初始化搜索模块，注册事件监听
        mSearch = GeoCoder.newInstance();
        mSearch.setOnGetGeoCodeResultListener(this);
        listView = (ListView)findViewById(R.id.order_list);
        points = new ArrayList<>();
        orders = new ArrayList<>();
        AsyncHttpClient client = new AsyncHttpClient();
        String url = "http://23.83.250.227:8080/order/get-all-undone-order.do";
        client.post(url, null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try{
                    JSONArray jsonArray = new JSONArray(new String(responseBody));
                    for(int i = 0; i < jsonArray.length(); i++){
                        if(jsonArray.getJSONObject(i).getString("ori_lat")==null){
                            continue;
                        }
                        Orders order = new Orders();
                        order.setAptTime(jsonArray.getJSONObject(i).getString("apt_time"));
                        order.setSerialNum(jsonArray.getJSONObject(i).getString("serial_num"));
                        points.add(new LatLng(Double.parseDouble(jsonArray.getJSONObject(i).getString("ori_lat"))
                                , Double.parseDouble(jsonArray.getJSONObject(i).getString("ori_lng"))));
                        points.add(new LatLng(Double.parseDouble(jsonArray.getJSONObject(i).getString("des_lat"))
                                , Double.parseDouble(jsonArray.getJSONObject(i).getString("des_lng"))));
                        orders.add(order);
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
                if(points.size()!=0) {
                    mSearch.reverseGeoCode(new ReverseGeoCodeOption()
                            .location(points.get(0)));
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(getApplicationContext(), "网络错误！", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {
    }

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
        if(index % 2 == 0){
            orders.get(index/2).setOriAddress(reverseGeoCodeResult.getAddressDetail().street);
        } else {
            orders.get(index/2).setDestAddress(reverseGeoCodeResult.getAddressDetail().street);
            GetDistanceUtil util = new GetDistanceUtil(points.get(index - 1), points.get(index));
            orders.get(index/2).setDistance(String.valueOf(util.getDistance()) + " m");
        }
        index++;
        if(index == orders.size() * 2){
            listView.setAdapter(new RecOrderListViewAdapter(orders, getApplicationContext()));
        } else{
            mSearch.reverseGeoCode(new ReverseGeoCodeOption()
                    .location(points.get(index)));
        }
    }
}
