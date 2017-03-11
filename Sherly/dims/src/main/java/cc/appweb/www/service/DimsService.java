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
            int resultFlag = CONNECT_SUCCESS;
            JSONObject mainServiceResult = null;
            BufferedOutputStream mainServerOutput;
            BufferedInputStream mainServerInput;
            AccessServiceProtocol mainServerProtocol =  new AccessServiceProtocol();
            int responseCode = 0;
            StringBuilder mainServerBuilder = new StringBuilder();
            // 主服务器连接发送读取相关
            try {
                // 连接核心
                Socket accessServiceSocket = new Socket(Constant.serverIP, Constant.serverPort);
                mainServerOutput = new BufferedOutputStream(accessServiceSocket.getOutputStream());
                mainServerInput = new BufferedInputStream(accessServiceSocket.getInputStream());

                mainServerProtocol.appid = appId;
                mainServerProtocol.usr = usr;
                mainServerProtocol.pwd = pwd;

                // 发送接入请求
                mainServerOutput.write(mainServerProtocol.getSendByte());
                mainServerOutput.flush();
                // 接收响应
                byte[] buff = new byte[1024];
                int readByte = 0;
                while ((readByte = mainServerInput.read(buff)) > 0) {
                    mainServerBuilder.append(new String(buff, 0, readByte));
                }
                mainServerInput.close();
                mainServerOutput.close();
            }catch (UnknownHostException e){
                e.printStackTrace();
                resultFlag = CONNECT_SERVER_FAILED;
            }catch (IOException e){
                e.printStackTrace();
                resultFlag = CONNECT_TIMEOUT;
                return resultFlag;
            }

            // 解析协议
            try{
                mainServiceResult = (JSONObject) mainServerProtocol.getReceiveData(mainServerBuilder.toString());
                responseCode = mainServiceResult.getInt("code");
                if( responseCode == 200){

                }else if(responseCode == 500){
                    resultFlag = CONNECT_CHECK_USER_FAILED;
                }else if(responseCode == 503){
                    resultFlag = CONNECT_SERVER_FAILED;
                }else if(responseCode == 504){
                    resultFlag = CONNECT_CHECK_APP_FAILED;
                }else {
                    resultFlag = CONNECT_UNKNOWN_FAILED;
                }
            }catch (JSONException e){
                e.printStackTrace();
                resultFlag = CONNECT_SERVER_FAILED;
            }finally {
                if(responseCode != 200){
                    return resultFlag;
                }
            }

            // 节点服务器连接并登录
            try{
                String nodeInfo = mainServiceResult.getString("nodeMsg");
                int boundary = nodeInfo.indexOf('-');
                String nodeIp = nodeInfo.substring(0, boundary);
                int nodePort = Integer.parseInt(nodeInfo.substring(boundary+1, nodeInfo.length()));
                // 发起节点服务器连接
                Socket nodeSocket = new Socket(nodeIp, nodePort);
                // 启动sender 和  receiver
                mSender = Sender.getSenderInstance(nodeSocket.getOutputStream());
                mReceiver = Receiver.getReceiverInstance(mContext, nodeSocket.getInputStream());

                LoginProtocol loginProtocol = new LoginProtocol();
                loginProtocol.appid = appId;
                loginProtocol.usr = usr;
                loginProtocol.pwd = pwd;

                mSender.send(loginProtocol.getSendByte());
            }catch (JSONException e){
                e.printStackTrace();
                resultFlag = CONNECT_SERVER_FAILED;
            }catch (IOException e){
                e.printStackTrace();
                resultFlag = CONNECT_NODE_SERVER_FAILED;
            }

            return resultFlag;
        }
    }
}
