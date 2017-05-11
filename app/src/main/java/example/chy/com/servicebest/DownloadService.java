package example.chy.com.servicebest;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import java.io.File;

public class DownloadService extends Service {
    private DownloadTask downloadTask;

    //下载链接
    private String downloadUrl;

    //创建DownloadListener的匿名类实例
    private DownloadListener listener = new DownloadListener(){
        @Override
        public void onProgress(int progress) {
            /**
             * 调用getNotificationManager()方法构建一个显示下载进度的通知，调用
             * getNotificationManager的notify()方法触发此通知
             * Downloading...表示正在下载，progress为下载的进度条
             */
            getNotificationManager().notify(1, getNotification("Downloading...", progress));
        }

        @Override
        public void onSuccess() {
            downloadTask = null;
            // 下载成功时将前台服务通知关闭，并创建一个下载成功的通知
            stopForeground(true);//关闭下载中的前台通知
            /**
             * 调用getNotificationManager()方法构建一个显示下载成功的通知，调用
             * getNotificationManager的notify()方法触发此通知
             * Downloading Success表示下载成功
             */
            getNotificationManager().notify(1, getNotification("Download Success", -1));
            Toast.makeText(DownloadService.this, "Download Success", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFailed() {
            downloadTask = null;
            // 下载失败时将前台服务通知关闭，并创建一个下载失败的通知
            /**
             * 调用getNotificationManager()方法构建一个显示下载失败的通知，调用
             * getNotificationManager的notify()方法触发此通知
             * Downloading Failed表示下载失败
             * 并弹出一个Toast在活动中提示下载失败
             */
            stopForeground(true);
            getNotificationManager().notify(1, getNotification("Download Failed", -1));
            //提示下载失败
            Toast.makeText(DownloadService.this, "Download Failed", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPaused() {
            /**
             * 下载暂停，不关闭通知，
             * 并弹出一个Toast在活动中提示下载暂停
             */
            downloadTask = null;
            Toast.makeText(DownloadService.this, "Paused", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCanceled() {
            /**
             * 取消下载，关闭通知，
             * 并弹出一个Toast在活动中提示下载暂停
             */
            downloadTask = null;
            stopForeground(true);

            Toast.makeText(DownloadService.this, "Canceled", Toast.LENGTH_SHORT).show();
        }
    };

    /**
     * 创建一个DownloadBinder让DownloadService可以与活动进行通信，提供了
     * startDownload(),pauseDownload(),cancelDownload()3个方法分别用于
     * 开始下载，暂停下载，取消下载。
     */
    private DownloadBinder mBinder = new DownloadBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    class DownloadBinder extends Binder {

        public void startDownload(String url) {
            if (downloadTask == null) {
                downloadUrl = url;    //获取链接
                /**
                *创建DownloadTask的实例，将DownloadListener作为参数传入
                 * 调用execute()方法开启下载,
                 * 将下载文件的URL地址传入到调用execute()方法中
                 **/
                downloadTask = new DownloadTask(listener);
                downloadTask.execute(downloadUrl);
                //在系统状态栏创建一个运行的通知
                startForeground(1, getNotification("Downloading...", 0));
                //触发Toast，显示正在下载
                Toast.makeText(DownloadService.this, "Downloading...", Toast.LENGTH_SHORT).show();
            }
        }

        //简单调用DownloadTask中的pauseDownload()方法
        public void pauseDownload() {
            if (downloadTask != null) {
                downloadTask.pauseDownload();
            }
        }

        public void cancelDownload() {
            if (downloadTask != null) {
                //简单调用DownloadTask中的cancelDownload()方法
                downloadTask.cancelDownload();
            } else {
                if (downloadUrl != null) {
                    // 取消下载时需将文件删除，并将通知关闭
                    String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));  //获取文件名

                    //获取路径
                    String directory = Environment.getExternalStoragePublicDirectory(Environment
                            .DIRECTORY_DOWNLOADS).getPath();

                    //创建一个文件对象
                    File file = new File(directory + fileName);
                    //判断文件是否存在
                    if (file.exists()) {//文件存在
                        file.delete();//删除文件
                    }
                    getNotificationManager().cancel(1);//通知自动取消
                    stopForeground(true);
                    Toast.makeText(DownloadService.this, "Canceled", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    //getNotification()方法用于构建所使用到的通知
    private Notification getNotification(String title, int progress) {
        Intent intent = new Intent(this, MainActivity.class);
        //创建PendingIntent对象，PendingIntent相当于延迟的Intent，在
        //\合适的时机执行某个动作
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
        //获得PendingIntent实例pi
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        builder.setContentIntent(pi);
        builder.setContentTitle(title);
        if (progress >= 0) {
            // 当progress大于或等于0时才需显示下载进度
            builder.setContentText(progress + "%");

            /**第一个参数100为传入通知的最大进度
            *第二个参数progress为当前下载的进度
             * 第三个参数表示是否使用模糊进度条，本题目不使用
            **/
            builder.setProgress(100, progress, false);
        }
        return builder.build();
    }
}
