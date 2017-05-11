package example.chy.com.servicebest;

import android.os.AsyncTask;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/5/1
 */

    //3个泛型参数，第一个参数String表示执行时传入一个字符串给后台，第二个参数Integer表示整型数据为进度的显示单位
    //第三个参数Integer表示整型数据去反馈执行结果
    public class DownloadTask extends AsyncTask<String,Integer,Integer>{
    //4个下载的状态，为整型常量
    //下载成功
    public static final int TYPE_SUCCESS=0;
    //下载失败
    public static final int TYPE_FAILED=1;
    //暂停下载
    public static final int TYPE_PAUSED=2;
    //取消下载
    public static final int TYPE_CANCELED=3;

    private DownloadListener listener;

    private boolean isCanceled=false;  //boolean变量，起始为false，取消操作

    private boolean isPaused=false;    //boolean变量，起始为false，暂停操作

    private int lastProgress;

    public DownloadTask(DownloadListener listener){
        this.listener=listener;
    }//将下载状态回调

    //重写3个方法，分别为doInBackground方法，doInBackground方法，onPostExecute方法

    //用于后台执行的下载编辑
    @Override
    protected Integer doInBackground(String... params) {
        InputStream is = null;

        RandomAccessFile savedFile = null;

        File file = null;
        try {
            long downloadedLength = 0; // 记录已下载的文件长度
            String downloadUrl = params[0];//获取下载的URL地址
            String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));//通过URL解析下载的文件名
            //将文件保存到Download目录下
            String directory = Environment.getExternalStoragePublicDirectory(Environment.
                    DIRECTORY_DOWNLOADS).getPath();
            file = new File(directory + fileName);

            //判断Download目录下是否存在下载的文件
            if (file.exists()) {
                downloadedLength = file.length();//读取已下载的字节数
            }
            long contentLength = getContentLength(downloadUrl);//获取待下载文件的总长度，赋值给contentLength
            //判断文件是否有问题
            if (contentLength == 0) {
                return TYPE_FAILED;  //文件长度为0返回下载失败
            } else if (contentLength == downloadedLength) {
                // 已下载字节和文件总字节相等，说明已经下载完成了
                return TYPE_SUCCESS;
            }
            OkHttpClient client = new OkHttpClient();//用于发送网络请求
            Request request = new Request.Builder()
                    // 断点下载，指定从哪个字节开始下载
                    .addHeader("RANGE", "bytes=" + downloadedLength + "-")
                    .url(downloadUrl)
                    .build();
            Response response = client.newCall(request).execute();
            if (response != null) {
                is = response.body().byteStream();
                savedFile = new RandomAccessFile(file, "rw");
                savedFile.seek(downloadedLength); // 跳过已下载的字节
                byte[] b = new byte[1024];
                int total = 0;
                int len;
                while ((len = is.read(b)) != -1) {
                    //判断是否有暂停或取消操作
                    if (isCanceled) {
                        return TYPE_CANCELED;//中断下载
                    } else if (isPaused) {
                        return TYPE_PAUSED;  //中断下载
                    } else {
                        total += len;
                        savedFile.write(b, 0, len);
                        // 计算已下载的百分比
                        int progress = (int) ((total + downloadedLength) * 100 / contentLength);
                        publishProgress(progress);
                    }
                }
                response.body().close();
                return TYPE_SUCCESS;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (savedFile != null) {
                    savedFile.close();
                }
                if (isCanceled && file != null) {
                    file.delete();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return TYPE_FAILED;
    }

    //用于在界面上更新最新的下载进度
    protected void onProgressUpdate(Integer... values){
        int progress = values[0];   //获取当前的下载进度
        //判断是否与上一次的下载进度有变化
        if (progress > lastProgress) {
            //调用Downloadlistener的onProgress方法通知下载进度更新
            listener.onProgress(progress);
            //获取最新的进度
            lastProgress = progress;
        }
    }

    //通知最终的下载结果
    @Override
    protected void onPostExecute(Integer status) {
        switch (status) {
            //下载成功
            case TYPE_SUCCESS:
                listener.onSuccess();
                break;

            //下载失败
            case TYPE_FAILED:
                listener.onFailed();
                break;

            //下载暂停
            case TYPE_PAUSED:
                listener.onPaused();
                break;

            //下载取消
            case TYPE_CANCELED:
                listener.onCanceled();
            default:
                break;
        }
    }

    public void pauseDownload() {
        isPaused = true;
    }


    public void cancelDownload() {
        isCanceled = true;
    }

    private long getContentLength(String downloadUrl) throws IOException {
        //创建OkHttpClient实例
        OkHttpClient client = new OkHttpClient();

        //发起Http请求需要创建一个Request对象
        Request request = new Request.Builder()
                .url(downloadUrl)   //设置目标的网络地址，网络地址为downloadUrl
                .build();

        /**
         * 调用OkHttpClient的newCall()方法创建一个call对象，并
         * 调用它的execute方法来发送请求并获取服务器返回的数据
         */
        Response response = client.newCall(request).execute();
        if (response != null && response.isSuccessful()) {
            long contentLength = response.body().contentLength();
            response.close();
            return contentLength;
        }
        return 0;
}
}