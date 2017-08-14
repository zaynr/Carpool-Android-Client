package com.example.zengzy19585.carpool;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.navisdk.adapter.BNOuterLogUtil;
import com.example.zengzy19585.carpool.account.AccountCenter;
import com.example.zengzy19585.carpool.account.FriendManageActivity;
import com.example.zengzy19585.carpool.account.LoginActivity;
import com.example.zengzy19585.carpool.appoint.ImmediateCallActivity;
import com.example.zengzy19585.carpool.appoint.OrdersManageActivity;
import com.example.zengzy19585.carpool.appoint.ReceivingOrdersActivity;
import com.example.zengzy19585.carpool.utils.SharedPreferencesUtil;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;

import cz.msebera.android.httpclient.Header;

import static anet.channel.util.Utils.context;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private SharedPreferencesUtil userInfo;
    private String userType, userStatus, userName;

    // 定位相关
    LocationClient mLocClient;
    public MyLocationListenner myListener = new MyLocationListenner();
    private MyLocationConfiguration.LocationMode mCurrentMode;
    BitmapDescriptor mCurrentMarker;
    private int mCurrentDirection = 0;
    private double mCurrentLat = 0.0;
    private double mCurrentLon = 0.0;
    private float mCurrentAccracy;
    private static final int ACCESS_COARSE_LOCATION =100;
    private static final int ACCESS_FINE_LOCATION =101;

    MapView mMapView;
    BaiduMap mBaiduMap;

    // UI相关
    boolean isFirstLoc = true; // 是否首次定位
    private MyLocationData locData;

    @Override
    protected void onStart() {
        super.onStart();
        setUserInfo();
        startLocating();
    }

    private void getAndShowDriverLoc(){
        if(userInfo.getStringValue("userType").equals("driver")){
            return;
        }
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
    }

    private void updateDriverLoc(final LatLng latLng){
        if(!userInfo.getStringValue("userType").equals("driver")){
            return;
        }
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("mobile_number", userInfo.getStringValue("userName"));
        params.put("lat", String.valueOf(latLng.latitude));
        params.put("lng", String.valueOf(latLng.longitude));
        String url = "http://23.83.250.227:8080/driver//updating-driver-loc.do";
        client.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(getApplicationContext(), "同步位置失败！", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setUserInfo(){
        TextView textView;
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        Menu menu = navigationView.getMenu();
        View view = navigationView.getHeaderView(0);
        userType = userInfo.getStringValue("userType");
        userStatus = userInfo.getStringValue("userStatus");
        if(userType.equals("customer")){
            userName = "恒生员工" + userInfo.getStringValue("userName");
            textView = view.findViewById(R.id.user_type);
            textView.setText("乘客");
            textView = view.findViewById(R.id.user_name);
            textView.setText(userName);
            menu.findItem(R.id.nav_friends).setTitle("好友");
        } else if(userType.equals("driver")){
            userName = "司机用户" + userInfo.getStringValue("userName");
            textView = view.findViewById(R.id.user_type);
            textView.setText("司机");
            textView = view.findViewById(R.id.user_name);
            textView.setText(userName);
            menu.findItem(R.id.nav_friends).setTitle("常客");
        } else {
            textView = view.findViewById(R.id.user_type);
            textView.setText("访客");
            textView = view.findViewById(R.id.user_name);
            textView.setText(userName);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
        mLocClient.stop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        userInfo = new SharedPreferencesUtil(getApplicationContext(), "userInfo");

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(userInfo.getStringValue("userType").equals("customer")) {
                    if(userInfo.getStringValue("callStatus").contains("immediatePlace")) {
                        Intent intent = new Intent(getApplication(), OrdersManageActivity.class);
                        startActivity(intent);
                        onPause();
                    }
                    else {
                        Intent intent = new Intent(getApplication(), ImmediateCallActivity.class);
                        intent.putExtra("curLat", mCurrentLat);
                        intent.putExtra("curLng", mCurrentLon);
                        startActivity(intent);
                        onPause();
                    }
                }
                else if(userInfo.getStringValue("userType").equals("driver")) {
                    Intent intent = new Intent(getApplication(), ReceivingOrdersActivity.class);
                    intent.putExtra("curLat", mCurrentLat);
                    intent.putExtra("curLng", mCurrentLon);
                    startActivity(intent);
                    onPause();
                }
                else{
                    Toast.makeText(getApplicationContext(), "请先登录", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplication(), LoginActivity.class);
                    startActivity(intent);
                    onPause();
                }
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

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
        mLocClient.start();

        //init push service
        PushAgent mPushAgent = PushAgent.getInstance(this);
        mPushAgent.onAppStart();
        //注册推送服务，每次调用register方法都会回调该接口
        mPushAgent.register(new IUmengRegisterCallback() {

            @Override
            public void onSuccess(String deviceToken) {
            }

            @Override
            public void onFailure(String s, String s1) {

            }
        });

        Context mContext = MainActivity.this;

        if(mContext.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // 申请一个（或多个）权限，并提供用于回调返回的获取码（用户定义）
            Toast.makeText(getApplicationContext(), "请打开定位权限！", Toast.LENGTH_SHORT).show();
            finish();
        }
        if(mContext.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // 申请一个（或多个）权限，并提供用于回调返回的获取码（用户定义）
            Toast.makeText(getApplicationContext(), "请打开定位权限！", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    public void startLocating(){
        mMapView.onResume();
        mLocClient.start();

        mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(
                mCurrentMode, true, mCurrentMarker));
        MapStatus.Builder builder = new MapStatus.Builder();
        builder.overlook(0);
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_account) {
            if(userInfo.getStringValue("userStatus").contains("loggedIn")){
                Intent intent = new Intent(this, AccountCenter.class);
                startActivity(intent);
                onPause();
            } else{
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                onPause();
            }
        } else if (id == R.id.nav_friends) {
            if(userInfo.getStringValue("userStatus").contains("loggedIn")) {
                Intent intent = new Intent(this, FriendManageActivity.class);
                startActivity(intent);
                onPause();
            }
            else {
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                onPause();
            }
        } else if (id == R.id.nav_manage) {
            if(userInfo.getStringValue("userStatus").contains("loggedIn")) {
                Intent intent = new Intent(this, OrdersManageActivity.class);
                startActivity(intent);
                onPause();
            } else {
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                onPause();
            }
        } else if (id == R.id.nav_share) {
            Intent it = new Intent(Intent.ACTION_SEND);
            it.putExtra(Intent.EXTRA_TEXT, "Zayn 的拼车 APP, 欢迎试用: https://github.com/zaynr/Carpool-Android-Client.git");
            it.setType("text/plain");
            startActivity(Intent.createChooser(it, "分享给你的朋友"));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
            // map view 销毁后不在处理新接收的位置
            if (location == null || mMapView == null) {
                return;
            }
            mCurrentLat = location.getLatitude();
            mCurrentLon = location.getLongitude();
            mCurrentAccracy = location.getRadius();
            locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(mCurrentDirection).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
            if (isFirstLoc) {
                isFirstLoc = false;
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(new LatLng(mCurrentLat, mCurrentLon)).zoom(18.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }
            updateDriverLoc(new LatLng(mCurrentLat, mCurrentLon));
        }

        public void onReceivePoi(BDLocation poiLocation) {
        }
    }

}
