package com.example.zengzy19585.carpool.appoint;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
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
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
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

import java.util.ArrayList;
import java.util.List;

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
    private ArrayList<LatLng> latlngs;

    MapView mMapView;
    BaiduMap mBaiduMap;

    // UI相关
    boolean isFirstLoc = true; // 是否首次定位
    private MyLocationData locData;
    // 搜索相关
    private RoutePlanSearch mSearchRoute = null;
    private Polyline mPolyline;
    private Marker mMoveMarker;
    private Handler mHandler;
    private String mobileNumber;


    // 通过设置间隔时间和距离可以控制速度和图标移动的距离
    private static final int TIME_INTERVAL = 80;
    private static final double DISTANCE = 0.00002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_detail);
        latlngs = new ArrayList<>();
        Intent intent = getIntent();
        mobileNumber = intent.getStringExtra("mobileNumber");
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
     * 根据点获取图标转的角度
     */
    private double getAngle(int startIndex) {
        if ((startIndex + 1) >= mPolyline.getPoints().size()) {
            throw new RuntimeException("index out of bonds");
        }
        LatLng startPoint = mPolyline.getPoints().get(startIndex);
        LatLng endPoint = mPolyline.getPoints().get(startIndex + 1);
        return getAngle(startPoint, endPoint);
    }


    /**
     * 计算x方向每次移动的距离
     */
    private double getXMoveDistance(double slope) {
        if (slope == Double.MAX_VALUE) {
            return DISTANCE;
        }
        return Math.abs((DISTANCE * slope) / Math.sqrt(1 + slope * slope));
    }

    /**
     * 循环进行移动逻辑
     */
    public void moveLooper() {
        new Thread() {

            public void run() {

                while (true) {

                    for (int i = 0; i < latlngs.size() - 1; i++) {


                        final LatLng startPoint = latlngs.get(i);
                        final LatLng endPoint = latlngs.get(i + 1);
                        mMoveMarker
                                .setPosition(startPoint);

                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                // refresh marker's rotate
                                if (mMapView == null) {
                                    return;
                                }
                                mMoveMarker.setRotate((float) getAngle(startPoint,
                                        endPoint));
                            }
                        });
                        double slope = getSlope(startPoint, endPoint);
                        // 是不是正向的标示
                        boolean isReverse = (startPoint.latitude > endPoint.latitude);

                        double intercept = getInterception(slope, startPoint);

                        double xMoveDistance = isReverse ? getXMoveDistance(slope) :
                                -1 * getXMoveDistance(slope);


                        for (double j = startPoint.latitude; !((j > endPoint.latitude) ^ isReverse);
                             j = j - xMoveDistance) {
                            LatLng latLng = null;
                            if (slope == Double.MAX_VALUE) {
                                latLng = new LatLng(j, startPoint.longitude);
                            } else {
                                latLng = new LatLng(j, (j - intercept) / slope);
                            }

                            final LatLng finalLatLng = latLng;
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (mMapView == null) {
                                        return;
                                    }
                                    mMoveMarker.setPosition(finalLatLng);
                                }
                            });
                            try {
                                Thread.sleep(TIME_INTERVAL);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                }
            }

        }.start();
    }

    /**
     * 根据两点算取图标转的角度
     */
    private double getAngle(LatLng fromPoint, LatLng toPoint) {
        double slope = getSlope(fromPoint, toPoint);
        if (slope == Double.MAX_VALUE) {
            if (toPoint.latitude > fromPoint.latitude) {
                return 0;
            } else {
                return 180;
            }
        }
        float deltAngle = 0;
        if ((toPoint.latitude - fromPoint.latitude) * slope < 0) {
            deltAngle = 180;
        }
        double radio = Math.atan(slope);
        double angle = 180 * (radio / Math.PI) + deltAngle - 90;
        return angle;
    }

    /**
     * 根据点和斜率算取截距
     */
    private double getInterception(double slope, LatLng point) {

        double interception = point.latitude - slope * point.longitude;
        return interception;
    }

    /**
     * 算斜率
     */
    private double getSlope(LatLng fromPoint, LatLng toPoint) {
        if (toPoint.longitude == fromPoint.longitude) {
            return Double.MAX_VALUE;
        }
        double slope = ((toPoint.latitude - fromPoint.latitude) / (toPoint.longitude - fromPoint.longitude));
        return slope;

    }

    /**
     * 定位SDK监听函数
     */
    private class MyLocationListenner implements BDLocationListener {

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
                params.put("mobile_number", mobileNumber);
                url = "http://23.83.250.227:8080/driver/retrieve-driver-info.do";
                client.post(url, params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        try {
                            JSONObject object = new JSONObject(new String(responseBody));
                            LatLng latLng = new LatLng(object.getDouble("lat"), object.getDouble("lng"));
                            latlngs.add(latLng);
                            //add car overlay
                            OverlayOptions markerOptions;
                            markerOptions = new MarkerOptions().flat(true).anchor(0.5f, 0.5f)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.arrow)).position(latlngs.get(0))
                                    .rotate((float) getAngle(0));
                            mMoveMarker = (Marker) mBaiduMap.addOverlay(markerOptions);
                            moveLooper();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Toast.makeText(getApplicationContext(), "网络错误", Toast.LENGTH_SHORT).show();
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
