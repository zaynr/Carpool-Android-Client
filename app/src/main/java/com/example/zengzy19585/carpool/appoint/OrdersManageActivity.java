package com.example.zengzy19585.carpool.appoint;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.example.zengzy19585.carpool.R;
import com.example.zengzy19585.carpool.adapter.RecOrderListViewAdapter;
import com.example.zengzy19585.carpool.entity.Orders;
import com.example.zengzy19585.carpool.navigate.BNDemoGuideActivity;
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
    private SharedPreferencesUtil preferencesUtil;
    public MyLocationListenner myListener = new MyLocationListenner();
    private LocationClient mLocClient;
    private String call_serial, rec_mobile_num;
    private LatLng dest;
    private String serialNum;
    private SharedPreferencesUtil util;
    private RecOrderListViewAdapter adapter;

    public void packResponse(byte[] responseBody){
        try {
            orders.clear();
            JSONArray jsonArray = new JSONArray(new String(responseBody));
            for (int i = 0; i < jsonArray.length(); i++) {
                if (jsonArray.getJSONObject(i).getString("ori_lat") == null
                        || jsonArray.getJSONObject(i).getString("ori_lng") == null) {
                    continue;
                }
                Orders order = new Orders();
                JSONObject object = jsonArray.getJSONObject(i);
                order.setCallType(object.getString("call_type"));
                order.setCustomerName(object.getString("customer_name"));
                order.setCustomerMobileNum(object.getString("customer_mobile_number"));
                order.setStatus(object.getString("status"));
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
                order.setDistance("全程：" + String.valueOf(util.getDistance()) + "米");
                orders.add(order);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders_manage);
        listView = (ListView) findViewById(R.id.order_list);
        points = new ArrayList<>();
        orders = new ArrayList<>();
        util = new SharedPreferencesUtil(OrdersManageActivity.this, "userInfo");
        serialNum = util.getStringValue("currentCallSn");
        setTitle("查看订单");
        final SwipeRefreshLayout layout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        adapter = new RecOrderListViewAdapter(orders, getApplicationContext());
        final AsyncHttpClient client = new AsyncHttpClient();
        preferencesUtil = new SharedPreferencesUtil(getApplicationContext(), "userInfo");
        final String url;
        final RequestParams params = new RequestParams();
        layout.setRefreshing(true);
        if(preferencesUtil.getStringValue("userType").contains("driver")) {
            url = "http://23.83.250.227:8080/order/get-by-rec.do";
            params.put("rec_mobile_num", preferencesUtil.getStringValue("userName"));
        }
        //immediatePlace
        else if(preferencesUtil.getStringValue("callStatus").contains("immediatePlace")){
            setTitle("等待接单");
            url = "http://23.83.250.227:8080/order/get-all-undone-order.do";
        }
        else{
            url = "http://23.83.250.227:8080/order/get-by-call.do";
            params.put("call_serial", preferencesUtil.getStringValue("userName"));
        }
        client.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                packResponse(responseBody);
                listView.setAdapter(adapter);
                layout.setRefreshing(false);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(getApplicationContext(), "网络错误！", Toast.LENGTH_SHORT).show();
                layout.setRefreshing(false);
            }
        });
        layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                client.post(url, params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        packResponse(responseBody);
                        adapter.notifyDataSetChanged();
                        layout.setRefreshing(false);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Toast.makeText(getApplicationContext(), "网络错误！", Toast.LENGTH_SHORT).show();
                        layout.setRefreshing(false);
                    }
                });
            }
        });
        // 定位初始化
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(2000);
        mLocClient.setLocOption(option);
        mLocClient.start();
    }

    /**
     * 定位SDK监听函数
     */
    private class MyLocationListenner implements BDLocationListener {

        @Override
        public void onConnectHotSpotMessage(String s, int i) {

        }

        @Override
        public void onReceiveLocation(BDLocation location) {
            if(preferencesUtil.getStringValue("callStatus").contains("immediatePlace")) {
                AsyncHttpClient client = new AsyncHttpClient();
                RequestParams params = new RequestParams();
                params.put("serial_num", serialNum);
                String url = "http://23.83.250.227:8080/order/get-by-serial.do";
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
                                JSONObject object = jsonArray.getJSONObject(i);
                                order.setStatus(object.getString("status"));
                                if (object.getString("status").equals("1")) {
                                    packResponse(responseBody);
                                    rec_mobile_num = object.getString("rec_mobile_num");
                                    order.setDistance("司机手机：" + rec_mobile_num);
                                    adapter.notifyDataSetChanged();
                                    setTitle("已接单");
                                }
                                if (object.getString("status").equals("2")) {
                                    preferencesUtil.setStringValue("callStatus","fin");
                                    packResponse(responseBody);
                                    orders.add(order);
                                    adapter.notifyDataSetChanged();
                                    setTitle("已完成订单");
                                    RatingDialog dialog = new RatingDialog(OrdersManageActivity.this);
                                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                    dialog.show();
                                }
                                dest = new LatLng(object.getDouble("des_lat"), object.getDouble("des_lng"));
                            }
                        } catch (Exception e) {
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

        public void onReceivePoi(BDLocation poiLocation) {
        }
    }

    private class RatingDialog extends AlertDialog {

        public RatingDialog(@NonNull Context context) {
            super(context);
        }

        public RatingDialog(@NonNull Context context, @StyleRes int themeResId) {
            super(context, themeResId);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.driver_rating_dialog);
            final RatingBar ratingBar = (RatingBar) findViewById(R.id.driver_rating);
//            final EditText comment = (EditText) findViewById(R.id.comment);
            Button commit = (Button) findViewById(R.id.commit);
            commit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    double rating = ratingBar.getRating();
//                    String str = comment.getText().toString();
                    String url = "http://23.83.250.227:8080/order/cus-finish-order.do";
                    AsyncHttpClient client = new AsyncHttpClient();
                    RequestParams params = new RequestParams();
                    params.put("comment", "");
                    params.put("serial_num", serialNum);
                    params.put("rec_mobile_num", rec_mobile_num);
                    params.put("rating", rating);
                    client.post(url, params, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            Toast.makeText(getApplicationContext(), "评价成功！", Toast.LENGTH_SHORT).show();
                            finish();
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