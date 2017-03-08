package cc.appweb.www.core;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;

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
    private SenderThread senderThread = null;
    /** 输出流 **/
    OutputStream outputStream = null;

    /** 单例构造方法 **/
    private Sender(OutputStream stream){
        outputStream = stream;
        // 开启独立线程进行发送
        senderThread = new SenderThread();
        senderThread.start();
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
        Log.i(TAG, "Sender.send()");
        senderThread.send(sendData);
    }
    /**
     * 发送工作线程
     * */
    class SenderThread extends Thread{
        /** 发送工作的Looper对象 **/
        Looper senderLooper = null;
        /** 发送工作的处理者 **/
        private Handler senderHandler = null;

        @Override
        public void run(){
            /** Looper对象，进行消息循环 **/
            Looper.prepare();
            senderLooper = Looper.myLooper();
            senderHandler = new Handler();
            final HeartBeatProtocol beatProtocol = new HeartBeatProtocol();
            senderHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try{
                        outputStream.write(beatProtocol.getSendByte());
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                    senderHandler.postDelayed(this, Constant.HEART_BEAT_FREQUENCY);
                }
            }, Constant.HEART_BEAT_FREQUENCY);
            Looper.loop();   // 循环
        }

        /**
         * 发送消息
         * */
        public void send(final byte[] sendData){
            Log.i(TAG, "Sender thread send data.");
            senderHandler.post(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "Sender loop send data.");
                    try{
                        outputStream.write(sendData);
                        outputStream.flush();
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
