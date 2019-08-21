package com.android.app.appupdate;

import android.app.DownloadManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;


public class DownloadService extends Service {

    public String DOWNLOAD_PATH = Environment.getExternalStorageDirectory() + "/download/AppUpdate.apk";
    private String url;//下载链接
    private long refernece;

    private BroadcastReceiver receiver;
    private IntentFilter filter;

    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            url = intent.getStringExtra("url");
            if (url != null && !TextUtils.isEmpty(url)) {
                download(url);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public void download(String url) {
        if (new File(DOWNLOAD_PATH).exists()) {
            new File(DOWNLOAD_PATH).delete();
        }
        receiver();
        DownloadManager dManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        //这里的"AppUpdate.apk"要对应DOWNLOAD_PATH的"AppUpdate.apk"
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "AppUpdate.apk");
        request.setDescription("新版本下载中...");
        request.setTitle("版本更新");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setMimeType("application/vnd.android.package-archive");
        // 设置为可被媒体扫描器找到
        request.allowScanningByMediaScanner();
        // 设置为可见和可管理
        request.setVisibleInDownloadsUi(true);
        try {
            refernece = dManager.enqueue(request);
        } catch (Exception e) {
        }
    }

    public void receiver() {
        filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        receiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                long myDwonloadID = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (refernece == myDwonloadID) {
                    DownloadManager dManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                    setPermission(DOWNLOAD_PATH);
                    Intent install = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + getPackageName()));
                    install.setAction(Intent.ACTION_VIEW);
                    install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    install.addCategory(Intent.CATEGORY_DEFAULT);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//大于Android版本7.0
                        //这里的"com.android.app.appupdate.provider"需要和AndroidManifest.xml的provider节点下的authorities属性保持一致
                        Uri contentUri = FileProvider.getUriForFile(context, "com.android.app.appupdate.provider", new File(DOWNLOAD_PATH));
                        install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//这里不能是setFlags(),set会覆盖掉之前的flags
                        install.setDataAndType(contentUri, "application/vnd.android.package-archive");
                    } else {
                        try {
                            Uri downloadFileUri = dManager.getUriForDownloadedFile(refernece);
                            install.setDataAndType(downloadFileUri, "application/vnd.android.package-archive");
                        } catch (RuntimeException e) {
                            e.printStackTrace();
                        }
                    }
                    context.startActivity(install);
                }
            }
        };
        registerReceiver(receiver, filter);
    }

    /**
     * 提升读写权限
     *
     * @param filePath 文件路径
     */
    public static void setPermission(String filePath) {
        String command = "chmod " + "777" + " " + filePath;
        Runtime runtime = Runtime.getRuntime();
        try {
            runtime.exec(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onDestroy() {
        unregisterReceiver(receiver);
    }
}
