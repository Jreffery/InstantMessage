package cc.appweb.www.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import cc.appweb.www.Constant;
import cc.appweb.www.core.protocol.AccessServiceProtocol;

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

    @Override
    public void onCreate(){
        Log.d(TAG, "DimsService run server!");
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

        /**
         * 实现全双工通信
         * */
        @Override
        public int connectToDims(String appId, String usr, String pwd){
            int resultFlag = CONNECT_SUCCESS;
            try{
                // 连接核心
                Socket accessServiceSocket = new Socket(Constant.serverIP, Constant.serverPort);
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(accessServiceSocket.getOutputStream());
                BufferedInputStream bufferedInputStream = new BufferedInputStream(accessServiceSocket.getInputStream());

                AccessServiceProtocol protocol = new AccessServiceProtocol();
                protocol.appid = appId;
                protocol.usr = usr;
                protocol.pwd = pwd;

                // 发送接入请求
                bufferedOutputStream.write(protocol.getSendByte());
                // 接收响应
                byte[] buff = new byte[1024];
                int readByte = 0;
                StringBuilder stringBuilder = new StringBuilder();
                while ((readByte = bufferedInputStream.read(buff)) > 0 ){
                    stringBuilder.append(new String(buff, 0, readByte));
                }
                protocol.toReceive = stringBuilder.toString();
                JSONObject accessServiceResult = (JSONObject) protocol.getReceiveData();

                int responseCode = accessServiceResult.getInt("code");
                if( responseCode == 200){
                    // 主服务器成功
                    String nodeInfo = accessServiceResult.getString("nodeMsg");
                    int boundary = nodeInfo.indexOf('-');
                    String nodeIp = nodeInfo.substring(0, boundary - 1);
                    int nodePort = Integer.parseInt(nodeInfo.substring(boundary+1, nodeInfo.length()-1));

                    // 发起节点服务器连接
                    
                    // 启动sender 和  receiver

                }else if(responseCode == 503){
                    resultFlag = CONNECT_SERVER_FAILED;
                }else {
                    resultFlag = CONNECT_UNKNOWN_FAILED;
                }

            }catch (UnknownHostException e){
                resultFlag = CONNECT_SERVER_FAILED;
            }catch (IOException e){
                resultFlag = CONNECT_TIMEOUT;
            }finally {
                return  resultFlag;
            }
        }
    }
}
