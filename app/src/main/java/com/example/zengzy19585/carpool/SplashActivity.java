package com.example.zengzy19585.carpool;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class SplashActivity extends AppCompatActivity {


    private static final String[] authBaseArr = { Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS };
    private static final int authBaseRequestCode = 1;

    private boolean hasCompletePhoneAuth() {
        // TODO Auto-generated method stub
        PackageManager pm = this.getPackageManager();
        for (String auth : authBaseArr) {
            if (pm.checkPermission(auth, this.getPackageName()) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public void runMain(){
        //延时跳转到主页面，splash用来做引导页
        Handler handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                    Intent intent=new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
        },1000*3);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        // 保证导航功能完备
        if (android.os.Build.VERSION.SDK_INT >= 23) {
                if (!hasCompletePhoneAuth()) {
                SplashActivity.this.requestPermissions(authBaseArr, authBaseRequestCode);
            }
            else{
                runMain();
            }
        }
        else{
            runMain();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        runMain();
    }
}
