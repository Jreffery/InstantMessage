package cc.appweb.www.core;

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
import cc.appweb.www.service.DimsService;

/**
 * @author jefferygong
 * @since 2017-3-7
 * @version 1.0
 * 连接管理器
 */
public class AccesserManager {

    /** 单例 **/
    private static AccesserManager ourInstance = new AccesserManager();
    /** 重设节点服务器周期 **/
    private static int reSetFrequency = 20;
    /** 同步锁对象 **/
    private SyncLock mSyncLock = new SyncLock();
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


    public static AccesserManager getInstance() {
        return ourInstance;
    }

    private AccesserManager() {
    }

    /**
     * 初始化用户信息
     * */
    public void initAppAndUser(String appId, String userName, String pwd){
        mAppId = appId;
        mUserName = userName;
        mPwd = pwd;
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
     * 返回接收者输入流
     * 该函数不考虑同步
     * **/
    public InputStream getReceiverInputStream(InputStream oldInput) throws IOException{
        if(mNodeSocket == null){
            mNodeSocket = new Socket(mNodeServerIpAddr, mNodeServerPort);
            mNodeInput = mNodeSocket.getInputStream();
            mNodeOutput = mNodeSocket.getOutputStream();
        }
        if(oldInput == null || oldInput != mNodeInput){
            return mNodeInput;
        }
        mReAccessTime ++ ;
        if(mReAccessTime % reSetFrequency == 0){
            //重设节点服务器
            int flag = initNodeServerInfo();
            if(flag != DimsService.CONNECT_SUCCESS){
                return null;
            }
        }
        mNodeSocket = new Socket(mNodeServerIpAddr, mNodeServerPort);
        mNodeInput = mNodeSocket.getInputStream();
        mNodeOutput = mNodeSocket.getOutputStream();
        return mNodeInput;
    }

    /**
     * 返回发送者输出流
     * 该函数不考虑同步
     * **/
    public OutputStream getSenderOutputStream(OutputStream oldOutput) throws IOException{
        if(mNodeSocket == null){
            mNodeSocket = new Socket(mNodeServerIpAddr, mNodeServerPort);
            mNodeInput = mNodeSocket.getInputStream();
            mNodeOutput = mNodeSocket.getOutputStream();
        }
        if(oldOutput == null || oldOutput != mNodeOutput){
            return mNodeOutput;
        }

        mReAccessTime ++ ;
        if(mReAccessTime % reSetFrequency == 0){
            //重设节点服务器
            int flag = initNodeServerInfo();
            if(flag != DimsService.CONNECT_SUCCESS){
                return null;
            }
        }

        mNodeSocket = new Socket(mNodeServerIpAddr, mNodeServerPort);
        mNodeInput = mNodeSocket.getInputStream();
        mNodeOutput = mNodeSocket.getOutputStream();
        return mNodeOutput;
    }

    /** 获取同步锁 **/
    public SyncLock getSyncLock(){
        return mSyncLock;
    }

    // 同步类
    class SyncLock{

    }

}
