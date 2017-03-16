package cc.appweb.www.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import cc.appweb.www.Constant;
import cc.appweb.www.core.AccesserManager;
import cc.appweb.www.core.Receiver;
import cc.appweb.www.core.Sender;
import cc.appweb.www.core.protocol.AccessServiceProtocol;
import cc.appweb.www.core.protocol.LoginProtocol;
import cc.appweb.www.core.protocol.MessageProtocol;

/**
 * @author jefferygong
 * @since 2017-3-6
 * @version 1.0
 * DIMS运行服务，承担数据的转发与接收
 */
public class DimsService extends Service {

    /** FLAG **/
    public static final int CONNECT_SUCCESS = 0;               // 连接成功
    public static final int CONNECT_TIMEOUT = 1;               // 连接超时
    public static final int CONNECT_SERVER_FAILED = 2;        // 获取服务失败
    public static final int CONNECT_NODE_SERVER_FAILED = 3;  // 节点服务器挂掉
    public static final int CONNECT_CHECK_APP_FAILED = 4;    // app key失败
    public static final int CONNECT_CHECK_USER_FAILED = 5;   // 用户校验失败
    public static final int CONNECT_UNKNOWN_FAILED = 6;      // 未知失败
    private static final String TAG = "DimsService";
    /** binder实例 **/
    private DimsServiceImp mBinder = new DimsServiceImp();
    /** Context应用 **/
    Context mContext = null;
    /** sender实例 **/
    private Sender mSender = null;
    /** receiver **/
    private Receiver mReceiver = null;

    @Override
    public void onCreate(){
        Log.i(TAG, "DimsService run server!");
        mContext = this;
    }

    @Override
    public IBinder onBind(Intent intent){
        return mBinder;
    }

    /**
     * 继承stub类，实现aidl自定义函数
     **/
    class DimsServiceImp extends IDimsConnectInterface.Stub{
        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean,
                               float aFloat, double aDouble, String aString) throws RemoteException{
        }

        @Override
        public void send(long msgID, String receiver, String data){
            MessageProtocol protocol = new MessageProtocol();
            protocol.receiver = receiver;
            protocol.data = data;
            protocol.msgID = msgID;
            mSender.send(protocol.getSendByte());
        }

        /**
         * 实现全双工通信
         * */
        @Override
        public int connectToDims(String appId, String usr, String pwd){
            Log.i(TAG, "DimsService.connectToDims");
            AccesserManager accesserManager = AccesserManager.getInstance();
            accesserManager.initAppAndUser(appId, usr, pwd);
            int flag = accesserManager.initNodeServerInfo();
            if(flag != CONNECT_SUCCESS){
                return flag;
            }

            try{
                mSender = Sender.getSenderInstance(accesserManager.getSenderOutputStream(null));
                mReceiver = Receiver.getReceiverInstance(mContext, accesserManager.getReceiverInputStream(null));

                LoginProtocol loginProtocol = new LoginProtocol();
                loginProtocol.appid = appId;
                loginProtocol.usr = usr;
                loginProtocol.pwd = pwd;

                mSender.send(loginProtocol.getSendByte());
                return CONNECT_SUCCESS;
            }catch (IOException e){
                return CONNECT_NODE_SERVER_FAILED;
            }
        }
    }
}
