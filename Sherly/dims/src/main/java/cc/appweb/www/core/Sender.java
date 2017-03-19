package cc.appweb.www.core;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import cc.appweb.www.Constant;
import cc.appweb.www.access.INeedReConnected;
import cc.appweb.www.core.protocol.HeartBeatProtocol;

/**
 * @author jefferygong
 * @since 2017-3-7
 * @version 1.0
 * dims消息发送者
 * 在正常发送消息的情况下，还需要发送心跳包
 */
public class Sender {

    public static final String TAG = "Sender";
    /** 单例 **/
    private static Sender senderInstance = null;
    /** 工作线程 **/
    private SenderThread mSenderThread = null;

    /** 单例构造方法 **/
    private Sender(){
        // 开启独立线程进行发送
        mSenderThread = new SenderThread();
        mSenderThread.start();
        Log.i(TAG, "Sender thread run now.");
    }

    public static Sender getSenderInstance(){
        if(senderInstance == null){
            synchronized (Sender.class){
                if(senderInstance == null){
                    senderInstance = new Sender();
                }
            }
        }
        return senderInstance;
    }

    public void send(byte[] sendData){
        mSenderThread.send(sendData);
    }
    /**
     * 发送工作线程
     * */
    class SenderThread extends Thread implements INeedReConnected{
        /** 发送工作的Looper对象 **/
        private Looper mSenderLooper = null;
        /** 发送工作的处理者 **/
        private Handler mSenderHandler = null;
        /** 消息列表 **/
        final LinkedList<byte[]> mMsgQ = new LinkedList<>();
        /** 是否已post发送run **/
        boolean mIsPostToSend = false;
        /** 心跳包 **/
        final byte[] mHeartByte = new HeartBeatProtocol().getSendByte();
        /** 发送心跳包**/
        private final Runnable mHeartRun = new Runnable() {
            @Override
            public void run() {
                try{
                    synchronized (AccesserManager.getInstance().outputLock){
                        AccesserManager.getInstance().getOutputStream().write(mHeartByte);
                        AccesserManager.getInstance().getOutputStream().flush();
                    }
                }catch (IOException e){
                    e.printStackTrace();
                    AccesserManager.getInstance().reConnected();
                    reAccess();
                }
                mSenderHandler.postDelayed(this, Constant.HEART_BEAT_FREQUENCY);
            }
        };

        private final Runnable mSendRun = new Runnable() {
            @Override
            public void run() {
                synchronized (mMsgQ){
                    mIsPostToSend = false;
                    for (int i=0;i<mMsgQ.size();i++){
                        try{
                            synchronized (AccesserManager.getInstance().outputLock){
                                AccesserManager.getInstance().getOutputStream().write(mMsgQ.get(i));
                                AccesserManager.getInstance().getOutputStream().flush();
                            }
                        }catch (IOException e){
                            e.printStackTrace();
                            // 把已发送的消息删除
                            for(int j=0;j<i;j++){
                                mMsgQ.remove(j);
                            }
                            // 需要重连
                            AccesserManager.getInstance().reConnected();
                            reAccess();
                            return;
                        }
                    }
                    mMsgQ.clear();
                }
            }
        };

        @Override
        public void run(){
            /** Looper对象，进行消息循环 **/
            Looper.prepare();
            mSenderLooper = Looper.myLooper();
            mSenderHandler = new Handler();
            mSenderHandler.post(mSendRun);
            mSenderHandler.postDelayed(mHeartRun, Constant.HEART_BEAT_FREQUENCY);
            Looper.loop();   // 循环
        }

        /**
         * 发送消息
         * */
        public void send(byte[] sendData){
            synchronized (mMsgQ){
                mMsgQ.add(sendData);
                if(!mIsPostToSend){
                    if (mSenderHandler != null){
                        mIsPostToSend = true;
                        mSenderHandler.post(mSendRun);
                    }
                }
            }
        }

        /**
         * 重连机制
         * */
        public void reAccess(){
            mMsgQ.addFirst(AccesserManager.getInstance().getLoginByte());
            mSenderHandler.post(mSendRun);
        }
    }
}
