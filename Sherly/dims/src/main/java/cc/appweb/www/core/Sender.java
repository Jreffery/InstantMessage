package cc.appweb.www.core;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import cc.appweb.www.Constant;
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
    /** 输出流 **/
    OutputStream mOutputStream = null;

    /** 单例构造方法 **/
    private Sender(OutputStream stream){
        mOutputStream = stream;
        // 开启独立线程进行发送
        mSenderThread = new SenderThread();
        mSenderThread.start();
        Log.i(TAG, "Sender thread run now.");
    };

    public static Sender getSenderInstance(OutputStream stream){
        if(senderInstance == null){
            synchronized (Sender.class){
                if(senderInstance == null){
                    senderInstance = new Sender(stream);
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
    class SenderThread extends Thread{
        /** 发送工作的Looper对象 **/
        private Looper mSenderLooper = null;
        /** 发送工作的处理者 **/
        private Handler mSenderHandler = null;
        /** senderHandler未初始化前的消息队列 **/
        private ArrayList<byte[]> mMessageQueueUnInit = null;

        @Override
        public void run(){
            /** Looper对象，进行消息循环 **/
            Looper.prepare();
            mSenderLooper = Looper.myLooper();
            synchronized (SenderThread.class){
                mSenderHandler = new Handler();
                if(mMessageQueueUnInit != null){
                    for (int i = 0; i < mMessageQueueUnInit.size(); i++){
                        final byte[] data = mMessageQueueUnInit.get(i);
                        mSenderHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                try{
                                    mOutputStream.write(data);
                                    mOutputStream.flush();
                                }catch (IOException e){
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }
            }
            final HeartBeatProtocol beatProtocol = new HeartBeatProtocol();
            mSenderHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try{
                        mOutputStream.write(beatProtocol.getSendByte());
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                    mSenderHandler.postDelayed(this, Constant.HEART_BEAT_FREQUENCY);
                }
            }, Constant.HEART_BEAT_FREQUENCY);
            Looper.loop();   // 循环
        }

        /**
         * 发送消息
         * */
        public void send(final byte[] sendData){
            // 此时senderHandler可能还没初始化
            if(mSenderHandler == null){
                synchronized (SenderThread.class){
                    if (mSenderHandler == null){
                        if (mMessageQueueUnInit == null){
                            mMessageQueueUnInit = new ArrayList<>();
                        }
                        mMessageQueueUnInit.add(sendData);
                        return;
                    }
                }
            }
            mSenderHandler.post(new Runnable() {
                @Override
                public void run() {
                    try{
                        mOutputStream.write(sendData);
                        mOutputStream.flush();
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
