package com.android.app.appupdate;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION = 1;
    private String[] mPermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private UpdatePopup mUpdatePopup;
    private Activity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.mActivity = this;
        TextView tvVersionName = findViewById(R.id.tv_version_name);
        tvVersionName.setText("当前版本：v" + getAppVersionName());
        if (TextUtils.equals(getAppVersionName(), "1.0.0")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(this, mPermission, REQUEST_PERMISSION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSION:
                //权限
                boolean permission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (!permission) {
                    Toast.makeText(this, "请前往设置给予存储权限", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    if (TextUtils.equals(getAppVersionName(), "1.0.0")) {//版本更新
                        mUpdatePopup = new UpdatePopup(this);
                        mUpdatePopup.setOutSideDismiss(false);
                        mUpdatePopup.showPopupWindow();
                        mUpdatePopup.findViewById(R.id.tv_skip).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mUpdatePopup.dismiss();
                            }
                        });
                        mUpdatePopup.findViewById(R.id.tv_update).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(mActivity, DownloadService.class);
                                intent.putExtra("url", "https://github.com/fanhenghao/androidProject/raw/master/technology_rel1.0.1.apk");
//                            intent.putExtra("url", "http://yunchudian.oss-cn-shanghai.aliyuncs.com/mobiletest/yunchudianManagerOnline.apk");
                                mActivity.startService(intent);
                                Toast.makeText(mActivity, "后台更新下载中...", Toast.LENGTH_SHORT).show();
                                mUpdatePopup.dismiss();
                            }
                        });
                    }
                }
                break;
        }
    }

    public String getAppVersionName() {
        String versionName = "";
        try {
            PackageManager packageManager = getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(getApplication().getPackageName(), 0);
            versionName = packageInfo.versionName;
            if (TextUtils.isEmpty(versionName)) {
                return "";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return versionName;
    }
}
