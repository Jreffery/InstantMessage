package cc.appweb.www.core;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import cc.appweb.www.Constant;
import cc.appweb.www.core.protocol.AccessServiceProtocol;
import cc.appweb.www.core.protocol.ISendableProtocol;
import cc.appweb.www.core.protocol.LoginProtocol;
import cc.appweb.www.service.DimsService;

/**
 * @author jefferygong
 * @since 2017-3-7
 * @version 1.0
 * 连接管理器
 */
public class AccesserManager {

    /** TAG **/
    private static final String TAG = "AccesserManager";
    /**Context**/
    private Context mContext;
    /** 单例 **/
    private static AccesserManager ourInstance = new AccesserManager();
    /** 重设节点服务器周期 **/
    private static int reSetFrequency = 20;;
    /** 主服务器ip地址 **/
    private String mMainServerIpAddr = Constant.serverIP;
    /** 主服务器port端口号 **/
    private int mMainServerPort = Constant.serverPort;
    /** 节点服务器ip地址 **/
    private String mNodeServerIpAddr;
    /** 节点服务器port端口 **/
    private int mNodeServerPort;
    /** 节点服务器的socket对象 **/
    private Socket mNodeSocket = null;
    /** 节点服务器输出流 **/
    private OutputStream mNodeOutput = null;
    /** 节点服务器输入流 **/
    private InputStream mNodeInput = null;
    /**重连次数**/
    private int mReAccessTime = 0;
    /** appid **/
    private String mAppId;
    /** username **/
    private String mUserName;
    /** pwd **/
    private String mPwd;
    /** 登录协议 **/
    private LoginProtocol loginProtocol;

    /** 发送者 **/
    private Sender mSender;
    /** 接收者 **/
    private Receiver mReceiver;


    public static AccesserManager getInstance() {
        return ourInstance;
    }

    private AccesserManager() {
    }

    public void setContext(Context context){
        mContext = context;
    }

    /**
     * 初始化用户信息
     * */
    public void initAppAndUser(String appId, String userName, String pwd){
        mAppId = appId;
        mUserName = userName;
        mPwd = pwd;
        loginProtocol = new LoginProtocol();
        loginProtocol.appid = appId;
        loginProtocol.usr = userName;
        loginProtocol.pwd = pwd;
    }

    public byte[] getLoginByte(){
        return loginProtocol.getSendByte();
    }

    /**
     * 获取主服务器响应
     * 断线之后不会马上调用该函数进行重设
     * 在连接一定次数的节点服务器后进行重新调用
     * */
    public int initNodeServerInfo(){
        int resultFlag = DimsService.CONNECT_SUCCESS;
        JSONObject mainServiceResult;
        BufferedOutputStream mainServerOutput;
        BufferedInputStream mainServerInput;
        AccessServiceProtocol mainServerProtocol =  new AccessServiceProtocol();
        int responseCode;
        StringBuilder mainServerBuilder = new StringBuilder();
        try {
            // 连接核心
            Socket accessServiceSocket = new Socket(mMainServerIpAddr, mMainServerPort);
            mainServerOutput = new BufferedOutputStream(accessServiceSocket.getOutputStream());
            mainServerInput = new BufferedInputStream(accessServiceSocket.getInputStream());

            mainServerProtocol.appid = mAppId;
            mainServerProtocol.usr = mUserName;
            mainServerProtocol.pwd = mPwd;

            // 发送接入请求
            mainServerOutput.write(mainServerProtocol.getSendByte());
            mainServerOutput.flush();
            // 接收响应
            byte[] buff = new byte[1024];
            int readByte;
            while ((readByte = mainServerInput.read(buff)) > 0) {
                mainServerBuilder.append(new String(buff, 0, readByte));
            }
            mainServerInput.close();
            mainServerOutput.close();
        }catch (UnknownHostException e){
            e.printStackTrace();
            resultFlag = DimsService.CONNECT_SERVER_FAILED;
        }catch (IOException e){
            e.printStackTrace();
            resultFlag = DimsService.CONNECT_TIMEOUT;
            return resultFlag;
        }
        Log.i(TAG, "Main Server response = " + mainServerBuilder.toString());
        // 解析协议
        try{
            mainServiceResult = (JSONObject) mainServerProtocol.getReceiveData(mainServerBuilder.toString());
            responseCode = mainServiceResult.getInt("code");
            if( responseCode == 200){
                // 获取节点服务器信息
                String nodeInfo = mainServiceResult.getString("nodeMsg");
                int boundary = nodeInfo.indexOf('-');
                mNodeServerIpAddr = nodeInfo.substring(0, boundary);
                mNodeServerPort = Integer.parseInt(nodeInfo.substring(boundary+1, nodeInfo.length()));
            }else if(responseCode == 500){
                resultFlag = DimsService.CONNECT_CHECK_USER_FAILED;
            }else if(responseCode == 503){
                resultFlag = DimsService.CONNECT_SERVER_FAILED;
            }else if(responseCode == 504){
                resultFlag = DimsService.CONNECT_CHECK_APP_FAILED;
            }else {
                resultFlag = DimsService.CONNECT_UNKNOWN_FAILED;
            }
        }catch (JSONException e){
            e.printStackTrace();
            resultFlag = DimsService.CONNECT_SERVER_FAILED;
        }finally {
            return resultFlag;
        }
    }

    /**
     * 重设输入输出流
     * */
    private int setNewStream() {
        if (!isNetworkAvailable()){
            DimsNotifier.getInstance().serverDown(mContext);
            return -1;
        }
        try{
            mNodeSocket = new Socket(mNodeServerIpAddr, mNodeServerPort);
            mNodeInput = mNodeSocket.getInputStream();
            mNodeOutput = mNodeSocket.getOutputStream();
        }catch (UnknownHostException e){
            // dns出问题，由于现在用的是ip，应该不会出问题
        }catch (IOException e){
            // 无法连接
            // 没流量
            // 信号不好
            // 没开网络
            mReAccessTime ++;
            if(mReAccessTime % reSetFrequency == 0){
                int flag = initNodeServerInfo();
                if(flag == DimsService.CONNECT_SUCCESS){
                    // 服务失败
                    DimsNotifier.getInstance().serverDown(mContext);
                    return -1;
                }
            }
            return setNewStream();
        }
        return 0;
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivity = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected())
            {
                // 当前网络是连接的
                if (info.getState() == NetworkInfo.State.CONNECTED)
                {
                    // 当前所连接的网络可用
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 开始发送接收线程
     * */
    public void start(){
        //初始化socket
        if(setNewStream()== 0){
            // 获取sender和receiver即可
            mSender = Sender.getSenderInstance();
            mReceiver = Receiver.getReceiverInstance(mContext);
            mSender.send(getLoginByte());
        }
    }

    /**
     * 获得输入流
     * */
    public final String inputLock = new String("");
    public InputStream getInputStream(){
        return mNodeInput;
    }

    public void send(byte[] sendByte){
        mSender.send(sendByte);
    }

    /**
     * 获得输出流
     * */
    public final String outputLock = new String("");
    public OutputStream getOutputStream(){
        return mNodeOutput;
    }

    /**
     * 发送者或者接收者触发的需要重连机制
     * */
    public int reConnected(){
        synchronized (outputLock){
            synchronized (inputLock){
                try {
                    mNodeOutput.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
                try {
                    mNodeInput.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
                return setNewStream();
            }
        }
    }

}
