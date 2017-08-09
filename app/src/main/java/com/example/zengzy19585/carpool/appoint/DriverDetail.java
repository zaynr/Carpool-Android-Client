package com.example.zengzy19585.carpool.appoint;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.example.zengzy19585.carpool.MainActivity;
import com.example.zengzy19585.carpool.R;
import com.example.zengzy19585.carpool.entity.Orders;
import com.example.zengzy19585.carpool.utils.DrivingRouteOverlay;
import com.example.zengzy19585.carpool.utils.SharedPreferencesUtil;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class DriverDetail extends AppCompatActivity implements OnGetRoutePlanResultListener {
    private SharedPreferencesUtil userInfo;
    private EditText name, sex, mobile_num;
    private String serialNum, rec_mobile_num;
    private SharedPreferencesUtil preferencesUtil;

    // 定位相关
    LocationClient mLocClient;
    public MyLocationListenner myListener = new MyLocationListenner();
    private MyLocationConfiguration.LocationMode mCurrentMode;
    BitmapDescriptor mCurrentMarker;
    private int mCurrentDirection = 0;
    private double mCurrentLat = 0.0;
    private double mCurrentLon = 0.0;
    private float mCurrentAccracy;
    private LatLng ori, dest;
    private RouteLine route = null;

    MapView mMapView;
    BaiduMap mBaiduMap;

    // UI相关
    boolean isFirstLoc = true; // 是否首次定位
    private MyLocationData locData;
    // 搜索相关
    private RoutePlanSearch mSearchRoute = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_detail);
        Intent intent = getIntent();
        String mobileNumber = intent.getStringExtra("mobileNumber");
        name = (EditText) findViewById(R.id.textView);
        sex = (EditText) findViewById(R.id.textView3);
        mobile_num = (EditText) findViewById(R.id.textView2);
        preferencesUtil = new SharedPreferencesUtil(getApplicationContext(), "userInfo");
        serialNum = preferencesUtil.getStringValue("currentCallSn");

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        TextView view;
        view = (TextView) findViewById(R.id.textView10);
        view.setText("车牌号：");
        view = (TextView) findViewById(R.id.textView8);
        view.setText("评分：");
        params.put("mobile_number", mobileNumber);
        String url = "http://23.83.250.227:8080/driver/retrieve-driver-info.do";
        client.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    JSONObject object = new JSONObject(new String(responseBody));
                    name.setText(object.getString("driver_name"));
                    sex.setText(object.getString("car_plate"));
                    mobile_num.setText(object.getString("rating"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(getApplicationContext(), "网络错误", Toast.LENGTH_SHORT).show();
            }
        });
        // 初始化搜索模块，注册路径搜索事件监听
        mSearchRoute = RoutePlanSearch.newInstance();
        mSearchRoute.setOnGetRoutePlanResultListener(this);
        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.bmapView);
        mMapView.showScaleControl(true);
        mMapView.showZoomControls(false);
        mBaiduMap = mMapView.getMap();
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        // 定位初始化
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(2000);
        mLocClient.setLocOption(option);
        mCurrentMode = MyLocationConfiguration.LocationMode.FOLLOWING;
        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(
                mCurrentMode, true, mCurrentMarker));
        MapStatus.Builder builder = new MapStatus.Builder();
        builder.overlook(0);
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
        mLocClient.start();
    }

    @Override
    public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {

    }

    @Override
    public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {

    }

    @Override
    public void onGetMassTransitRouteResult(MassTransitRouteResult massTransitRouteResult) {

    }

    @Override
    public void onGetDrivingRouteResult(DrivingRouteResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(getApplicationContext(), "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            route = result.getRouteLines().get(0);
            DrivingRouteOverlay overlay = new DrivingRouteOverlay(mBaiduMap);
            mBaiduMap.setOnMarkerClickListener(overlay);
            overlay.setData(result.getRouteLines().get(0));
            overlay.addToMap();
            overlay.zoomToSpan();
            MapStatus.Builder builder = new MapStatus.Builder();
            builder.zoom(mBaiduMap.getMapStatus().zoom - 0.2f);
            mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
        }
    }

    @Override
    public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {

    }

    @Override
    public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {

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
                                if (object.getString("status").equals("1") && isFirstLoc) {
                                    isFirstLoc = false;
                                    mBaiduMap.clear();
                                    ori = new LatLng(object.getDouble("ori_lat"), object.getDouble("ori_lng"));
                                    dest = new LatLng(object.getDouble("des_lat"), object.getDouble("des_lng"));
                                    mSearchRoute.drivingSearch((new DrivingRoutePlanOption())
                                            .from(PlanNode.withLocation(ori)).to(PlanNode.withLocation(dest)));
                                }
                                if (object.getString("status").equals("2")) {
                                    preferencesUtil.setStringValue("callStatus","fin");
                                    RatingDialog dialog = new RatingDialog(DriverDetail.this);
                                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                    dialog.show();
                                }
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
            locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(mCurrentDirection).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
            if (isFirstLoc) {
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(new LatLng(mCurrentLat, mCurrentLon)).zoom(18.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
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
