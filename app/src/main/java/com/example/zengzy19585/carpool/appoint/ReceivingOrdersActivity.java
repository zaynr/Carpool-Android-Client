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
import com.example.zengzy19585.carpool.utils.SharedPreferencesUtil;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class ReceivingOrdersActivity extends AppCompatActivity implements OnGetGeoCoderResultListener {

    private GeoCoder mSearch = null;
    private SharedPreferencesUtil preferencesUtil;
    private LatLng oriLatLng, destLatLng;
    private String oriAddress, destAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiving_orders);
        preferencesUtil = new SharedPreferencesUtil(getApplicationContext(), "userInfo");
        // 初始化搜索模块，注册事件监听
        mSearch = GeoCoder.newInstance();
        mSearch.setOnGetGeoCodeResultListener(this);
        //get orders
        getDispatchedOrder();
    }

    private void getDispatchedOrder(){
        AsyncHttpClient client = new AsyncHttpClient();
        String url = "http://23.83.250.227:8080/order/get-dispatched.do";
        RequestParams params = new RequestParams();
        params.put("rec_mobile_num", preferencesUtil.getStringValue("userName"));
        client.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }

    @Override
    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {
    }

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {

    }
}
