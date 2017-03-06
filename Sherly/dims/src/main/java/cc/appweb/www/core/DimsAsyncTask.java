package cc.appweb.www.core;

import android.os.Handler;
import android.util.Log;

/**
 * @author jefferygong
 * @since 2017-3-6
 * @version 1.0
 * dims单次异步任务
 * 基于Looper与handler机制，请保证在主线程中执行运行
 */
public abstract class DimsAsyncTask {

    private static final String TAG = "DimsAsyncTask";
    /** 异步执行的部分 **/
    protected abstract void runTask();
    /** 切换回主线程 **/
    protected abstract void onRunEnd();
    /**
     * 异步进行中的回调
     * 可重写或不重写
     * **/
    protected void onRunning(Object object){
        // nothing
    }

    /**
     * 从子线程中传递到主线程中onRunning方法
     * */
    protected final void postOnRunning(final Object object){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                onRunning(object);
            }
        });
    }

    /** Thread Task 线程主体 **/
    private Thread mTaskWorker = null;
    /** 消息处理handler **/
    private Handler mHandler = null;

    /**
     * 执行
     * */
    public void execute(){
        if(mTaskWorker != null){
            throw new RuntimeException("One DimsAsyncTask only can execute once.");
        }
        mHandler = new Handler();
        mTaskWorker = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "DimsAsyncTask run.");
                runTask();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "DimsAsyncTask is onRunEnd.");
                        onRunEnd();
                    }
                });
            }
        });
        mTaskWorker.start();
    }
}
