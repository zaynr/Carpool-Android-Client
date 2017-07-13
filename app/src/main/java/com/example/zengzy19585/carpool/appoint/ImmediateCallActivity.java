package com.example.zengzy19585.carpool.appoint;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
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
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.example.zengzy19585.carpool.R;
import com.example.zengzy19585.carpool.utils.DrivingRouteOverlay;
import com.example.zengzy19585.carpool.utils.OverlayManager;
import com.example.zengzy19585.carpool.utils.SharedPreferencesUtil;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class ImmediateCallActivity extends AppCompatActivity implements
        OnGetPoiSearchResultListener, OnGetSuggestionResultListener, OnGetRoutePlanResultListener {
    private PoiSearch mPoiSearch = null;
    private SuggestionSearch mSuggestionSearch = null;
    private List<String> suggest;
    private AutoCompleteTextView oriAuto, destAuto;
    private LatLng oriLatlng, destLatlng;
    private ArrayAdapter<String> sugAdapter = null;
    private RouteLine route = null;
    // 搜索相关
    private RoutePlanSearch mSearchRoute = null;    // 搜索模块，也可去掉地图模块独立使用

    MapView mMapView;
    BaiduMap mBaiduMap;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_immediate_call);
        Intent intent = getIntent();
        oriLatlng = new LatLng(intent.getDoubleExtra("curLat", 0), intent.getDoubleExtra("curLng", 0));
        // 初始化搜索模块，注册事件监听
        mSearchRoute = RoutePlanSearch.newInstance();
        mSearchRoute.setOnGetRoutePlanResultListener(this);
        // 初始化搜索模块，注册搜索事件监听
        mPoiSearch = PoiSearch.newInstance();
        mPoiSearch.setOnGetPoiSearchResultListener(this);

        // 初始化建议搜索模块，注册建议搜索事件监听
        mSuggestionSearch = SuggestionSearch.newInstance();
        mSuggestionSearch.setOnGetSuggestionResultListener(this);

        oriAuto = (AutoCompleteTextView) findViewById(R.id.poi_search_ori);
        destAuto = (AutoCompleteTextView) findViewById(R.id.poi_search_dest);
        sugAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line);
        oriAuto.setAdapter(sugAdapter);
        destAuto.setAdapter(sugAdapter);
        oriAuto.setThreshold(1);
        destAuto.setThreshold(1);
        oriAuto.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() <= 0) {
                    return;
                }
                mSuggestionSearch
                        .requestSuggestion((new SuggestionSearchOption())
                                .keyword(charSequence.toString()).city("杭州"));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        destAuto.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() <= 0) {
                    return;
                }
                mSuggestionSearch
                        .requestSuggestion((new SuggestionSearchOption())
                                .keyword(charSequence.toString()).city("杭州"));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        oriAuto.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mPoiSearch.searchInCity((new PoiCitySearchOption())
                        .city("杭州").keyword(adapterView.getItemAtPosition(i).toString()).pageNum(0));
            }
        });
        destAuto.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mPoiSearch.searchInCity((new PoiCitySearchOption())
                        .city("杭州").keyword(adapterView.getItemAtPosition(i).toString()).pageNum(0));
            }
        });

        final Button searchPoi = (Button) findViewById(R.id.search_poi);
        final Button placeOrder = (Button) findViewById(R.id.place_order);

        searchPoi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(destAuto.getText())) {
                    destAuto.setError("请输入终点");
                    destAuto.requestFocus();
                    return;
                }
                if(destLatlng==null){
                    Toast.makeText(getApplicationContext(), "未搜索到终点位置", Toast.LENGTH_SHORT).show();
                    return;
                }
                placeOrder.setClickable(true);
                mBaiduMap.clear();
                mSearchRoute.drivingSearch((new DrivingRoutePlanOption())
                        .from(PlanNode.withLocation(oriLatlng)).to(PlanNode.withLocation(destLatlng)));
            }
        });
        placeOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(destAuto.getText())) {
                    destAuto.setError("请输入终点");
                    destAuto.requestFocus();
                    return;
                }
                if(destLatlng==null){
                    Toast.makeText(getApplicationContext(), "未搜索到终点位置", Toast.LENGTH_SHORT).show();
                    return;
                }
                final SelectTypeDialog dialog = new SelectTypeDialog(ImmediateCallActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setOnItemInDlgClickListener(new OnItemInDlgClickListener() {
                    @Override
                    public void onItemClick(int position) {
                        switch (position){
                            case 0:
                                Date date = new Date();
                                AsyncHttpClient client = new AsyncHttpClient();
                                RequestParams params = new RequestParams();
                                SharedPreferencesUtil util = new SharedPreferencesUtil(getApplicationContext(), "userInfo");
                                String url = "http://23.83.250.227:8080/order/place-order.do";
                                params.put("apt_time", date.getTime());
                                params.put("ori_lat", oriLatlng.latitude);
                                params.put("ori_lng", oriLatlng.longitude);
                                params.put("des_lat", destLatlng.latitude);
                                params.put("des_lng", destLatlng.longitude);
                                params.put("call_serial", util.getStringValue("userName"));
                                params.put("call_type", 0);
                                client.post(url, params, new AsyncHttpResponseHandler() {
                                    @Override
                                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                        Toast.makeText(getApplicationContext(), "下单成功", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }

                                    @Override
                                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                        Toast.makeText(getApplicationContext(), "网络故障", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    }
                                });
                                break;
                            case 1:
                                TimePickDialog pickDialog = new TimePickDialog(ImmediateCallActivity.this);
                                pickDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                pickDialog.show();
                                dialog.dismiss();
                                break;
                        }
                    }
                });
                dialog.show();
            }
        });

        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.bmapView);
        mMapView.showScaleControl(true);
        mMapView.showZoomControls(false);
        mBaiduMap = mMapView.getMap();

        MapStatus.Builder builder = new MapStatus.Builder();
        builder.target(oriLatlng).zoom(18.0f);
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
    }

    @Override
    public void onGetPoiResult(PoiResult poiResult) {
        if(poiResult.getAllPoi()!=null) {
            mPoiSearch.searchPoiDetail((new PoiDetailSearchOption())
                    .poiUid(poiResult.getAllPoi().get(0).uid));
        }
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
            builder.zoom(mBaiduMap.getMapStatus().zoom - 0.5f);
            mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
        }

    }

    @Override
    public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {
        LatLng ll;
        if(oriAuto.isFocused()){
            ll = oriLatlng = new LatLng(poiDetailResult.getLocation().latitude
                    , poiDetailResult.getLocation().longitude);
        }else{
            ll = destLatlng = new LatLng(poiDetailResult.getLocation().latitude
                    , poiDetailResult.getLocation().longitude);
        }
        MapStatus.Builder builder = new MapStatus.Builder();
        mBaiduMap.clear();
        BitmapDescriptor bd = BitmapDescriptorFactory
                .fromResource(R.drawable.icon_gcoding);
        MarkerOptions ooA = new MarkerOptions().position(ll).icon(bd)
                .zIndex(9).draggable(true);
        mBaiduMap.addOverlay(ooA);
        builder.target(ll).zoom(18.0f);
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
    }

    @Override
    public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

    }

    @Override
    public void onGetSuggestionResult(SuggestionResult suggestionResult) {
        if (suggestionResult == null || suggestionResult.getAllSuggestions() == null) {
            return;
        }
        suggest = new ArrayList<>();
        for (SuggestionResult.SuggestionInfo info : suggestionResult.getAllSuggestions()) {
            if (info.key != null) {
                suggest.add(info.key);
            }
        }
        sugAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_dropdown_item_1line, suggest);
        oriAuto.setAdapter(sugAdapter);
        destAuto.setAdapter(sugAdapter);
        sugAdapter.notifyDataSetChanged();
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
    public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {

    }

    @Override
    public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {

    }

    // 响应DLg中的List item 点击
    interface OnItemInDlgClickListener {
        public void onItemClick(int position);
    }

    private class TimePickDialog extends Dialog{
        TimePicker timePicker;
        DatePicker datePicker;
        Button commitPick;
        Date date;

        public TimePickDialog(Context context){
            super(context);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_time_picker_dialog);
            timePicker = findViewById(R.id.time_picker);
            datePicker = findViewById(R.id.date_picker);
            commitPick = findViewById(R.id.commit_pick);
            commitPick.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    date = new Date(datePicker.getYear() - 1900, datePicker.getMonth()
                            , datePicker.getDayOfMonth(), timePicker.getCurrentHour()
                            , timePicker.getCurrentMinute());
                    Log.e("TIME", date.toString());
                    AsyncHttpClient client = new AsyncHttpClient();
                    RequestParams params = new RequestParams();
                    SharedPreferencesUtil util = new SharedPreferencesUtil(getApplicationContext(), "userInfo");
                    String url = "http://23.83.250.227:8080/order/place-order.do";
                    params.put("ori_lat", oriLatlng.latitude);
                    params.put("ori_lng", oriLatlng.longitude);
                    params.put("des_lat", destLatlng.latitude);
                    params.put("des_lng", destLatlng.longitude);
                    params.put("call_serial", util.getStringValue("userName"));
                    params.put("apt_time", date.getTime());
                    params.put("call_type", "1");
                    client.post(url, params, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            Toast.makeText(getApplicationContext(), "下单成功", Toast.LENGTH_SHORT).show();
                            finish();
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            Toast.makeText(getApplicationContext(), "网络故障", Toast.LENGTH_SHORT).show();
                            dismiss();
                        }
                    });
                    dismiss();
                }
            });
        }
    }

    private class SelectTypeDialog extends Dialog{
        ArrayAdapter<String> adapter;
        OnItemInDlgClickListener onItemInDlgClickListener;

        public SelectTypeDialog(Context context) {
            super(context);
            String[] option = {"立即叫车", "预约叫车"};
            adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, option);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_order_dialog);
            ListView ls = findViewById(R.id.order_options);
            ls.setAdapter(adapter);
            ls.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    onItemInDlgClickListener.onItemClick(i);
                    dismiss();
                }
            });
        }

        @Override
        public void setOnDismissListener(OnDismissListener listener) {
            super.setOnDismissListener(listener);
        }

        public void setOnItemInDlgClickListener(OnItemInDlgClickListener itemListener) {
            onItemInDlgClickListener = itemListener;
        }
    }

}
