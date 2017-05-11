package example.chy.com.servicebest;

/**
 * Created by Administrator on 2017/5/1 0008.
 */

//DownloadListener接口用于对下载过程中的各种状态进行监听和回调
public interface DownloadListener {
    //5个回调方法
    //通知当前的进度
    void onProgress(int progress);
    //通知下载成功事件
    void onSuccess();
    //通知下载失败事件
    void onFailed();
    //通知下载暂停事件
    void onPaused();
    //通知下载取消事件
    void onCanceled();
}
