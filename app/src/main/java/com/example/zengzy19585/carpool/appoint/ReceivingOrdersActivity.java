package com.example.zengzy19585.carpool.appoint;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.model.LatLng;
import com.baidu.navisdk.adapter.BNCommonSettingParam;
import com.baidu.navisdk.adapter.BNOuterLogUtil;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BNaviSettingManager;
import com.baidu.navisdk.adapter.BaiduNaviManager;
import com.baidu.navisdk.adapter.BaiduNaviManager.NaviInitListener;
import com.baidu.navisdk.adapter.BaiduNaviManager.TTSPlayMsgType;
import com.baidu.navisdk.adapter.BaiduNaviManager.TTSPlayStateListener;
import com.example.zengzy19585.carpool.MainActivity;
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class ReceivingOrdersActivity extends AppCompatActivity {

    private ArrayList<Orders> orders;
    private ListView listView;
    //navi vars
    private static final String APP_FOLDER_NAME = "Carpool";
    private String mSDCardPath = null;
    public static final String ROUTE_PLAN_NODE = "routePlanNode";
    private static final String[] authBaseArr = { Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION };
    private static final String[] authComArr = { Manifest.permission.READ_PHONE_STATE };
    private static final int authBaseRequestCode = 1;
    private static final int authComRequestCode = 2;
    private boolean hasInitSuccess = false;
    private boolean hasRequestComAuth = false;
    private int curIndex;
    private BNRoutePlanNode sNode = null;
    private BNRoutePlanNode eNode = null;
    private BNRoutePlanNode curNode = null;
    private SharedPreferencesUtil util;
    private SwipeRefreshLayout refreshLayout;

    public void packRes(byte[] responseBody){
        try {
            orders.clear();
            JSONArray jsonArray = new JSONArray(new String(responseBody));
            for (int i = 0; i < jsonArray.length(); i++) {
                if (jsonArray.getJSONObject(i).getString("ori_lat") == null) {
                    continue;
                }
                Orders order = new Orders();
                order.setCustomerRating(jsonArray.getJSONObject(i).getString("customer_rating"));
                order.setStatus(jsonArray.getJSONObject(i).getString("status"));
                order.setCallType(jsonArray.getJSONObject(i).getString("call_type"));
                order.setCallSerial(jsonArray.getJSONObject(i).getString("call_serial"));
                order.setCustomerName(jsonArray.getJSONObject(i).getString("customer_name"));
                order.setCustomerMobileNum(jsonArray.getJSONObject(i).getString("customer_mobile_number"));
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
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiving_orders);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        //init navi
        BNOuterLogUtil.setLogSwitcher(true);
        if (initDirs()) {
            initNavi();
        }
        // 初始化搜索模块，注册事件监听
        util = new SharedPreferencesUtil(getApplicationContext(), "userInfo");
        Intent intent = getIntent();
        LatLng curLatLng = new LatLng(intent.getDoubleExtra("curLat", 0), intent.getDoubleExtra("curLng", 0));
        curNode = new BNRoutePlanNode(curLatLng.longitude, curLatLng.latitude, null, null, BNRoutePlanNode.CoordinateType.WGS84);
        listView = (ListView) findViewById(R.id.order_list);
        orders = new ArrayList<>();
        final AsyncHttpClient client = new AsyncHttpClient();
        final String url = "http://23.83.250.227:8080/order/get-all-undone-order.do";
        final RecOrderListViewAdapter adapter = new RecOrderListViewAdapter(orders, getApplicationContext());
        refreshLayout.setRefreshing(true);
        client.post(url, null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                packRes(responseBody);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        //start navi
                        if (BaiduNaviManager.isNaviInited()) {
                            curIndex = i;
                            sNode = new BNRoutePlanNode(orders.get(i).getStart().longitude, orders.get(i).getStart().latitude
                                    , null, null, BNRoutePlanNode.CoordinateType.WGS84);
                            eNode = new BNRoutePlanNode(orders.get(i).getEnd().longitude, orders.get(i).getEnd().latitude
                                    , null, null, BNRoutePlanNode.CoordinateType.WGS84);
                            ConfirmOrder confirmOrder = new ConfirmOrder(ReceivingOrdersActivity.this);
                            confirmOrder.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            confirmOrder.show();
                        }
                    }
                });
                refreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(getApplicationContext(), "网络错误！", Toast.LENGTH_SHORT).show();
                refreshLayout.setRefreshing(false);
            }
        });
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                client.post(url, null, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        packRes(responseBody);
                        adapter.notifyDataSetChanged();
                        refreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Toast.makeText(getApplicationContext(), "网络错误！", Toast.LENGTH_SHORT).show();
                        refreshLayout.setRefreshing(false);
                    }
                });
            }
        });
    }

    private boolean initDirs() {
        mSDCardPath = getSdcardDir();
        if (mSDCardPath == null) {
            return false;
        }
        File f = new File(mSDCardPath, APP_FOLDER_NAME);
        if (!f.exists()) {
            try {
                f.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    String authinfo = null;

    /**
     * 内部TTS播报状态回传handler
     */
    private Handler ttsHandler = new Handler() {
        public void handleMessage(Message msg) {
            int type = msg.what;
            switch (type) {
                case TTSPlayMsgType.PLAY_START_MSG: {
                    // showToastMsg("Handler : TTS play start");
                    break;
                }
                case TTSPlayMsgType.PLAY_END_MSG: {
                    // showToastMsg("Handler : TTS play end");
                    break;
                }
                default:
                    break;
            }
        }
    };

    /**
     * 内部TTS播报状态回调接口
     */
    private TTSPlayStateListener ttsPlayStateListener = new TTSPlayStateListener() {

        @Override
        public void playEnd() {
            // showToastMsg("TTSPlayStateListener : TTS play end");
        }

        @Override
        public void playStart() {
            // showToastMsg("TTSPlayStateListener : TTS play start");
        }
    };

    private boolean hasBasePhoneAuth() {
        // TODO Auto-generated method stub

        PackageManager pm = this.getPackageManager();
        for (String auth : authBaseArr) {
            if (pm.checkPermission(auth, this.getPackageName()) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private boolean hasCompletePhoneAuth() {
        // TODO Auto-generated method stub

        PackageManager pm = this.getPackageManager();
        for (String auth : authComArr) {
            if (pm.checkPermission(auth, this.getPackageName()) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void initNavi() {

        // 申请权限
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            if (!hasBasePhoneAuth()) {
                this.requestPermissions(authBaseArr, authBaseRequestCode);
                return;
            }
        }

        BaiduNaviManager.getInstance().init(this, mSDCardPath, APP_FOLDER_NAME, new NaviInitListener() {
            @Override
            public void onAuthResult(int status, String msg) {
                if (0 == status) {
                    authinfo = "key校验成功!";
                } else {
                    authinfo = "key校验失败, " + msg;
                }
            }

            public void initSuccess() {
                Toast.makeText(ReceivingOrdersActivity.this, "初始化成功", Toast.LENGTH_SHORT).show();
                hasInitSuccess = true;
                initSetting();
            }

            public void initStart() {
                Toast.makeText(ReceivingOrdersActivity.this, "初始化开始", Toast.LENGTH_SHORT).show();
            }

            public void initFailed() {
                Toast.makeText(ReceivingOrdersActivity.this, "初始化失败", Toast.LENGTH_SHORT).show();
            }

        }, null, ttsHandler, ttsPlayStateListener);

    }

    private String getSdcardDir() {
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory().toString();
        }
        return null;
    }

    private BNRoutePlanNode.CoordinateType mCoordinateType = null;

    private void routeplanToNavi(BNRoutePlanNode.CoordinateType coType) {
        mCoordinateType = coType;
        if (!hasInitSuccess) {
            Toast.makeText(ReceivingOrdersActivity.this, "还未初始化!", Toast.LENGTH_SHORT).show();
        }
        // 权限申请
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            // 保证导航功能完备
            if (!hasCompletePhoneAuth()) {
                if (!hasRequestComAuth) {
                    hasRequestComAuth = true;
                    this.requestPermissions(authComArr, authComRequestCode);
                    return;
                } else {
                    Toast.makeText(ReceivingOrdersActivity.this, "没有完备的权限!", Toast.LENGTH_SHORT).show();
                }
            }

        }

        if (sNode != null && eNode != null) {
            List<BNRoutePlanNode> list = new ArrayList<>();
            list.add(curNode);
            list.add(sNode);
            list.add(eNode);
            BaiduNaviManager.getInstance().launchNavigator(this, list, 1, true, new ReceivingOrdersActivity.DemoRoutePlanListener(sNode));
        }
    }

    private class DemoRoutePlanListener implements BaiduNaviManager.RoutePlanListener {
        private BNRoutePlanNode mBNRoutePlanNode = null;
        private DemoRoutePlanListener(BNRoutePlanNode node) {
            mBNRoutePlanNode = node;
        }
        @Override
        public void onJumpToNavigator() {
            /*
             * 设置途径点以及resetEndNode会回调该接口
             */
            Intent intent = new Intent(ReceivingOrdersActivity.this, BNDemoGuideActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable(ROUTE_PLAN_NODE, mBNRoutePlanNode);
            intent.putExtras(bundle);
            startActivity(intent);
        }

        @Override
        public void onRoutePlanFailed() {
            // TODO Auto-generated method stub
            Toast.makeText(getApplicationContext(), "算路失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void initSetting() {
        BNaviSettingManager
                .setShowTotalRoadConditionBar(BNaviSettingManager.PreViewRoadCondition.ROAD_CONDITION_BAR_SHOW_ON);
        BNaviSettingManager.setVoiceMode(BNaviSettingManager.VoiceMode.Veteran);
        BNaviSettingManager.setRealRoadCondition(BNaviSettingManager.RealRoadCondition.NAVI_ITS_ON);
        Bundle bundle = new Bundle();
        // 必须设置APPID，否则会静音
        bundle.putString(BNCommonSettingParam.TTS_APP_ID, "9354030");
        BNaviSettingManager.setNaviSdkParam(bundle);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // TODO Auto-generated method stub
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == authBaseRequestCode) {
            for (int ret : grantResults) {
                if (ret != 0) {
                    Toast.makeText(getApplicationContext(), "缺少导航基本的权限!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            initNavi();
        } else if (requestCode == authComRequestCode) {
            for (int ret : grantResults) {
                if (ret == 0) {
                    continue;
                }
            }
            routeplanToNavi(mCoordinateType);
        }
    }

    private class ConfirmOrder extends Dialog{
        private ConfirmOrder(@NonNull Context context) {
            super(context);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.order_confirm_dialog);
            Button confirm, cancel;
            TextView confirmOri, confirmDis;
            confirmOri = findViewById(R.id.confirm_ori);
            confirmDis = findViewById(R.id.confirm_dis);
            GetDistanceUtil distanceUtil = new GetDistanceUtil(new LatLng(curNode.getLatitude(), curNode.getLongitude())
                    , new LatLng(sNode.getLatitude(), sNode.getLongitude()));
            confirmOri.setText(confirmOri.getText() + orders.get(curIndex).getOriAddress() + "\n" + confirmDis.getText() + String.valueOf(distanceUtil.getDistance()) + "米\n");
            confirmDis.setText("乘客姓名：" + orders.get(curIndex).getCustomerName() + "\n乘客评分：" + orders.get(curIndex).getCustomerRating());
            confirm = findViewById(R.id.confirm_action);
            cancel = findViewById(R.id.cancel_action);
            confirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(orders.get(curIndex).getCallType().equals("0")) {
                        AsyncHttpClient client = new AsyncHttpClient();
                        RequestParams recOrder = new RequestParams();
                        recOrder.put("serial_num", Integer.parseInt(orders.get(curIndex).getSerialNum()));
                        recOrder.put("rec_mobile_num", util.getStringValue("userName"));
                        util.setStringValue("recOrderSerial", orders.get(curIndex).getSerialNum());
                        util.setStringValue("callSerial", orders.get(curIndex).getCallSerial());
                        String url = "http://23.83.250.227:8080/order/confirm-order.do";
                        client.post(url, recOrder, new AsyncHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                routeplanToNavi(BNRoutePlanNode.CoordinateType.WGS84);
                                dismiss();
                                finish();
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                Toast.makeText(getApplicationContext(), "网络错误！", Toast.LENGTH_SHORT).show();
                                dismiss();
                            }
                        });
                        String updateServe = "http://23.83.250.227:8080/friend/update-driver-serve.do";
                        RequestParams updateServeParam = new RequestParams();
                        updateServeParam.put("rec_mobile_num", util.getStringValue("userName"));
                        updateServeParam.put("call_serial", orders.get(curIndex).getCallSerial());
                        client.post(updateServe, updateServeParam, new AsyncHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                Toast.makeText(getApplicationContext(), "网络错误！", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });

            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });
        }
    }
}
