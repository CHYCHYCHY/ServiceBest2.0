package example.chy.com.servicebest;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private DownloadService.DownloadBinder downloadBinder;

    //创建一个ServiceConnection的匿名类
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {

        }

        //此方法用于获取DownloadBinder的实例
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            downloadBinder = (DownloadService.DownloadBinder) service;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button startDownload = (Button) findViewById(R.id.start_download);

        Button pauseDownload = (Button) findViewById(R.id.pause_download);

        Button cancelDownload = (Button) findViewById(R.id.cancel_download);

        startDownload.setOnClickListener(this);

        pauseDownload.setOnClickListener(this);

        cancelDownload.setOnClickListener(this);

        Intent intent = new Intent(this, DownloadService.class);//转到服务

        startService(intent); // 启动服务

        bindService(intent, connection, BIND_AUTO_CREATE); // 绑定服务
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager
                .PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    @Override
    public void onClick(View v) {
        if (downloadBinder == null) {
            return;
        }
        switch (v.getId()) {
            case R.id.start_download:
                //定义下载链接
                String url = "https://raw.githubusercontent.com/guolindev/eclipse/master/eclipse-inst-win64.exe";
                downloadBinder.startDownload(url);//开始下载
                break;

            //暂停下载
            case R.id.pause_download:
                downloadBinder.pauseDownload();  //暂停下载
                break;

            //取消下载
            case R.id.cancel_download:
                downloadBinder.cancelDownload();
                break;
            default:
                break;
        }
    }

    //此方法用于权限的申请
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "拒绝权限将无法使用程序", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }

    /**
     * 活动被销毁要对服务解绑，以防内存泄漏，
     * 在onDestroy()方法实现
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }
}
