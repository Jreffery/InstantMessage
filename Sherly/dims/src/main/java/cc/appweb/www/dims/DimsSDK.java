package cc.appweb.www.dims;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import cc.appweb.www.Constant;
import cc.appweb.www.access.OnConnectedResultListener;
import cc.appweb.www.core.DimsAsyncTask;
import cc.appweb.www.service.IDimsConnectInterface;

/**
 * @author jefferygong
 * @since 2017-3-5
 * @version 1.0
 * sdk的公共接口
 */
public class DimsSDK {

    private static final String TAG = "DimsSDK";

    /** FLAG **/
    public static final int REMOTE_EX = -1;                     // 远程调用失败
    public static final int CONNECT_SUCCESS = 0;               // 连接成功
    public static final int CONNECT_TIMEOUT = 1;               // 连接超时
    public static final int CONNECT_SERVER_FAILED = 2;        // 获取服务失败
    public static final int CONNECT_NODE_SERVER_FAILED = 3;  // 节点服务器挂掉
    public static final int CONNECT_CHECK_APP_FAILED = 4;    // app key失败
    public static final int CONNECT_CHECK_USER_FAILED = 5;   // 用户校验失败
    public static final int CONNECT_UNKNOWN_FAILED = 6;      // 未知失败

    /** Context上下文 **/
    private Context mContext;
    /** appKey区分不同的应用 **/
    private String  mAppKey = null;
    /** 用户名，用以区分一条连接对应的用户 **/
    private String mUserName = null;
    /** 密码，用作校验，开发者自定义 **/
    private String mPassword = null;
    /** 连接结果回调监听者 **/
    private OnConnectedResultListener mOnConnectedResultListener = null;
    /** 连接服务的远程代理 **/
    private IDimsConnectInterface mDimsProxy = null;

    /**
     * 初始化sdk，在进程开始时调用
     * 获取appKey为连接作准备
     * */
    public void initSDK(Context appContext){
        try{
            mContext = appContext;
            // 获取app key
            ApplicationInfo appInfo = appContext.getPackageManager()
                    .getApplicationInfo(appContext.getPackageName(), PackageManager.GET_META_DATA);
            mAppKey = appInfo.metaData.getString(Constant.DIMS_APP_KEY);

        }catch (PackageManager.NameNotFoundException e){
            e.printStackTrace();
        }
    }

    /**
     * 设置用户名和密码
     * 宿主应用登录成功后调用，与宿主应用对应
     * */
    public void setUserAndPwd(String usr, String pwd){
        mUserName = usr;
        mPassword = pwd;
    }

    /**
     * 连接至服务器
     * */
    public void connect(){
        if(mAppKey == null){
            throw new RuntimeException("AppKey is not nullable, please check the AndroidManifest.");
        }
        if(mUserName == null || mPassword == null){
            throw new RuntimeException("Username or password is null.");
        }

        // 启动线程来创建连接服务
        DimsAsyncTask connectAsyncTask = new DimsAsyncTask() {
            private int connectResult;
            @Override
            protected void runTask() {
                // 绑定服务
                bindDimsConnectService();
                try{
                    connectResult = mDimsProxy.connectToDims(mAppKey, mUserName, mPassword);
                }catch (RemoteException e){
                    e.printStackTrace();
                    connectResult = REMOTE_EX;
                }
            }

            @Override
            protected void onRunEnd() {
                if(mOnConnectedResultListener != null){
                    mOnConnectedResultListener.onConnectedResult(connectResult);
                }
            }
        };
        connectAsyncTask.execute();
    }

    /**
     * 设置连接结果回调监听者
     * */
    public void setOnConnectedResultListener(OnConnectedResultListener listener){
        mOnConnectedResultListener = listener;
    }

    /**
     * 服务绑定回调
     * */
    private ServiceConnection mDimsServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "DimsService connected!");
            mDimsProxy = IDimsConnectInterface.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "DimsService disconnected!");
            mDimsProxy = null;
        }
    };

    /**
     * 连接服务
     * */
    private void bindDimsConnectService(){
        Intent intent = new Intent("cc.appweb.www.service.DimsService");
        mContext.bindService(intent, mDimsServiceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * 发送消息
     * */
    public void send(long msgID, String receiver, String data){
        try {
            mDimsProxy.send(msgID, receiver, data);
        }catch (RemoteException e){
            e.printStackTrace();
        }
    }
}
