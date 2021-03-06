package com.example.zengzy19585.carpool.navigate;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.navisdk.adapter.BNRouteGuideManager;
import com.baidu.navisdk.adapter.BNRouteGuideManager.CustomizedLayerItem;
import com.baidu.navisdk.adapter.BNRouteGuideManager.OnNavigationListener;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BNRoutePlanNode.CoordinateType;
import com.baidu.navisdk.adapter.BNaviBaseCallbackModel;
import com.baidu.navisdk.adapter.BaiduNaviCommonModule;
import com.baidu.navisdk.adapter.NaviModuleFactory;
import com.baidu.navisdk.adapter.NaviModuleImpl;
import com.example.zengzy19585.carpool.R;
import com.example.zengzy19585.carpool.appoint.ReceivingOrdersActivity;
import com.example.zengzy19585.carpool.utils.SharedPreferencesUtil;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class BNDemoGuideActivity extends Activity {

	private final String TAG = BNDemoGuideActivity.class.getName();
	private BNRoutePlanNode mBNRoutePlanNode = null;
	private BaiduNaviCommonModule mBaiduNaviCommonModule = null;
	private String call_serial, rec_mobile_num;
	private String serialNum;
	private SharedPreferencesUtil userInfo;
    // 定位相关
    LocationClient mLocClient;
    public MyLocationListenner myListener = new MyLocationListenner();
    private MyLocationConfiguration.LocationMode mCurrentMode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		userInfo = new SharedPreferencesUtil(getApplicationContext(), "userInfo");
        // 定位初始化
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(2000);
        mLocClient.setLocOption(option);
        mLocClient.start();

		createHandler();
		View view = null;
			//使用通用接口
			mBaiduNaviCommonModule = NaviModuleFactory.getNaviModuleManager().getNaviCommonModule(
					NaviModuleImpl.BNaviCommonModuleConstants.ROUTE_GUIDE_MODULE, this,
					BNaviBaseCallbackModel.BNaviBaseCallbackConstants.CALLBACK_ROUTEGUIDE_TYPE, mOnNavigationListener);
			if(mBaiduNaviCommonModule != null) {
				mBaiduNaviCommonModule.onCreate();
				view = mBaiduNaviCommonModule.getView();
			}

		if (view != null) {
			setContentView(view);
		}

		Intent intent = getIntent();
		if (intent != null) {
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				mBNRoutePlanNode = (BNRoutePlanNode) bundle.getSerializable(ReceivingOrdersActivity.ROUTE_PLAN_NODE);
			}
		}
		//显示自定义图标
		if (hd != null) {
			hd.sendEmptyMessageAtTime(MSG_SHOW, 5000);
		}

		AsyncHttpClient client = new AsyncHttpClient();
		RequestParams params = new RequestParams();
		SharedPreferencesUtil util = new SharedPreferencesUtil(BNDemoGuideActivity.this, "userInfo");
		serialNum = util.getStringValue("recOrderSerial");
		params.put("serial_num", serialNum);
		String url = "http://23.83.250.227:8080/order/get-by-serial.do";
		client.post(url, params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				try{
					JSONArray array = new JSONArray(new String(responseBody));
					JSONObject object = array.getJSONObject(0);
					rec_mobile_num = object.getString("rec_mobile_num");
					call_serial = object.getString("call_serial");
				}catch (Exception e){
					e.printStackTrace();
				}
				String url = "http://23.83.250.227:8080/friend/update-driver-serve.do";
				AsyncHttpClient client = new AsyncHttpClient();
				RequestParams params = new RequestParams();
				params.put("call_serial", call_serial);
				params.put("rec_mobile_num", rec_mobile_num);
				client.post(url, params, new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

					}

					@Override
					public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
						Toast.makeText(getApplicationContext(), "更新数据失败！", Toast.LENGTH_SHORT).show();
					}
				});
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
				Toast.makeText(getApplicationContext(), "获取订单失败！", Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
			if(mBaiduNaviCommonModule != null) {
				mBaiduNaviCommonModule.onResume();
			}

	}

	protected void onPause() {
		super.onPause();

			if(mBaiduNaviCommonModule != null) {
				mBaiduNaviCommonModule.onPause();
			}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(mBaiduNaviCommonModule != null) {
			mBaiduNaviCommonModule.onDestroy();
		}

	}

	@Override
	protected void onStop() {
		super.onStop();
			if(mBaiduNaviCommonModule != null) {
				mBaiduNaviCommonModule.onStop();
			}
	}

	@Override
	public void onBackPressed() {
			if(mBaiduNaviCommonModule != null) {
				mBaiduNaviCommonModule.onBackPressed(true);
			}
	}

	public void onConfigurationChanged(android.content.res.Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
			if(mBaiduNaviCommonModule != null) {
				mBaiduNaviCommonModule.onConfigurationChanged(newConfig);
			}
	}


	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
			if(mBaiduNaviCommonModule != null) {
				Bundle mBundle = new Bundle();
				mBundle.putInt(RouteGuideModuleConstants.KEY_TYPE_KEYCODE, keyCode);
				mBundle.putParcelable(RouteGuideModuleConstants.KEY_TYPE_EVENT, event);
				mBaiduNaviCommonModule.setModuleParams(RouteGuideModuleConstants.METHOD_TYPE_ON_KEY_DOWN, mBundle);
				try {
					Boolean ret = (Boolean)mBundle.get(RET_COMMON_MODULE);
					if(ret) {
						return true;
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		return super.onKeyDown(keyCode, event);
	}
	@Override
	protected void onStart() {
		super.onStart();
		// TODO Auto-generated method stub
			if(mBaiduNaviCommonModule != null) {
				mBaiduNaviCommonModule.onStart();
			}
	}
	private void addCustomizedLayerItems() {
		List<CustomizedLayerItem> items = new ArrayList<CustomizedLayerItem>();
		CustomizedLayerItem item1;
		if (mBNRoutePlanNode != null) {
			item1 = new CustomizedLayerItem(mBNRoutePlanNode.getLongitude(), mBNRoutePlanNode.getLatitude(),
					mBNRoutePlanNode.getCoordinateType(), getResources().getDrawable(R.drawable.ic_menu_send),
					CustomizedLayerItem.ALIGN_CENTER);
			items.add(item1);

			BNRouteGuideManager.getInstance().setCustomizedLayerItems(items);
		}
		BNRouteGuideManager.getInstance().showCustomizedLayer(true);
	}

	private static final int MSG_SHOW = 1;
	private static final int MSG_HIDE = 2;
	private static final int MSG_RESET_NODE = 3;
	private Handler hd = null;

	private void createHandler() {
		if (hd == null) {
			hd = new Handler(getMainLooper()) {
				public void handleMessage(android.os.Message msg) {
					if (msg.what == MSG_SHOW) {
						addCustomizedLayerItems();
					} else if (msg.what == MSG_HIDE) {
						BNRouteGuideManager.getInstance().showCustomizedLayer(false);
					} else if (msg.what == MSG_RESET_NODE) {
						BNRouteGuideManager.getInstance().resetEndNodeInNavi(
								new BNRoutePlanNode(116.21142, 40.85087, "百度大厦11", null, CoordinateType.GCJ02));
					}
				};
			};
		}
	}

	private OnNavigationListener mOnNavigationListener = new OnNavigationListener() {

		@Override
		public void onNaviGuideEnd() {
			//退出导航
			AsyncHttpClient client = new AsyncHttpClient();
			String url = "http://23.83.250.227:8080/order/end-order.do";
			RequestParams params = new RequestParams();
			params.put("serial_num", serialNum);
			client.post(url, params, new AsyncHttpResponseHandler() {
				@Override
				public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
					RatingDialog dialog = new RatingDialog(BNDemoGuideActivity.this, R.style.AppTheme);
					dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
					dialog.show();
				}

				@Override
				public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
					Toast.makeText(getApplicationContext(), "网络错误！", Toast.LENGTH_SHORT).show();
				}
			});
//			finish();
		}

		@Override
		public void notifyOtherAction(int actionType, int arg1, int arg2, Object obj) {

			if (actionType == 0) {
				//导航到达目的地 自动退出
				Log.i(TAG, "notifyOtherAction actionType = " + actionType + ",导航到达目的地！");
			}

			Log.i(TAG, "actionType:" + actionType + "arg1:" + arg1 + "arg2:" + arg2 + "obj:" + obj.toString());
		}

	};

	private final static String RET_COMMON_MODULE = "module.ret";

	private interface RouteGuideModuleConstants {
		final static int METHOD_TYPE_ON_KEY_DOWN = 0x01;
		final static String KEY_TYPE_KEYCODE = "keyCode";
		final static String KEY_TYPE_EVENT = "event";
	}

	private class RatingDialog extends Dialog{

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
			final RatingBar ratingBar = findViewById(R.id.driver_rating);
//			final EditText comment = findViewById(R.id.comment);
			Button commit = findViewById(R.id.commit);
			commit.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					double rating = ratingBar.getRating();
//					String str = comment.getText().toString();
					String url = "http://23.83.250.227:8080/order/dvr-finish-order.do";
					AsyncHttpClient client = new AsyncHttpClient();
					RequestParams params = new RequestParams();
					params.put("comment", "");
					params.put("serial_num", serialNum);
					params.put("call_serial", call_serial);
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

	/**
	 * 定位SDK监听函数
	 */
	private class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			updateDriverLoc(new LatLng(location.getLatitude(), location.getLongitude()));
		}

		public void onReceivePoi(BDLocation poiLocation) {
		}
	}
}
