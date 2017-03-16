package cc.appweb.www.core;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

import cc.appweb.www.Constant;

/**
 * @author jefferygong
 * @since 2017-3-7
 * @version 1.0
 * dims消息接收者
 */
public class Receiver {

    public static final String TAG = "Receiver";
    /** 单例 **/
    private static Receiver receiverInstance = null;
    /** 输入流 **/
    InputStream mInputStream = null;
    /** service上下文 **/
    Context mContext = null;
    /** 接收线程 **/
    private ReceiverThread mReceiverThread = null;

    private Receiver(Context context, InputStream inputStream){
        this.mInputStream = inputStream;
        mReceiverThread= new ReceiverThread();
        mReceiverThread.start();
        mContext = context;
    }

    /** 单例构造方法 **/
    public static Receiver getReceiverInstance(Context context, InputStream inputStream){
        if(receiverInstance == null){
            synchronized (Receiver.class){
                if(receiverInstance == null){
                    receiverInstance = new Receiver(context, inputStream);
                }
            }
        }
        return receiverInstance;
    }

    class ReceiverThread extends Thread{
        /** FLAG **/
        private static final int FIND_START_STATUS = 1;   //  等待"#####"
        private static final int FIND_DATA_STATUS = 2;   //  等待数据

        private static final int BUFF_SIZE = 1024;   //  缓冲大小


        @Override
        public void run(){
            // 循环
            int status = FIND_START_STATUS;
            int dataLength = 0;
            byte[] streamBuff = new byte[BUFF_SIZE];
            byte[] buff = new byte[BUFF_SIZE];
            int streamLength = 0;
            int readByte;
            int hadReceiveDataLength = 0;
            StringBuilder stringBuilder = new StringBuilder();
            while (true){
                try{
                    readByte = mInputStream.read(buff);
                    Log.i(TAG, "readbyte = "+readByte + " " + new String(buff, 0, readByte));
                    if(readByte == -1){
                        // 断开了重连
                        throw new IOException();
                    }
                    if(status == FIND_START_STATUS){

                        // 寻找
                        int copySize = readByte > BUFF_SIZE - streamLength ? BUFF_SIZE - streamLength : readByte;
                        System.arraycopy(buff, 0, streamBuff, streamLength, copySize);
                        streamLength += copySize;
                        /**
                         *  "#####xxxxx#####"
                         *  其中 xxxxx 第一字节为类型，一般代表字符串数据
                         *  后四字节代表大小
                         **/
                        while (streamLength >= 15){
                            for(int i=0; i<Constant.RECEIVE_BOUNDARY.length; i++){
                                if(streamBuff[i] != Constant.RECEIVE_BOUNDARY[i] || streamBuff[i+10] !=Constant.RECEIVE_BOUNDARY[i]){
                                    Log.i(TAG, "Receive byte failed!");
                                    throw new IOException();
                                }
                            }
                            dataLength = 0;
                            dataLength |= (streamBuff[6] & 0xFF) << 24;
                            dataLength |= (streamBuff[7] & 0xFF) << 16;
                            dataLength |= (streamBuff[8] & 0xFF) << 8;
                            dataLength |= (streamBuff[9] & 0xFF) << 0;

                            stringBuilder.delete(0, stringBuilder.length());
                            hadReceiveDataLength = 0;

                            if(dataLength <= streamLength - 15){
                                // 这里可以处理一条数据
                                stringBuilder.append(new String(streamBuff, 15, dataLength));
                                DimsNotifier.getInstance().notify(mContext, stringBuilder.toString());
                                //将剩余的copy下来
                                System.arraycopy(streamBuff, dataLength+15, streamBuff, 0, streamLength - dataLength -15);
                                streamLength = streamLength - dataLength -15;
                            }else {
                                stringBuilder.append(new String(streamBuff, 15, streamLength-15));
                                hadReceiveDataLength += streamLength-15;
                                if(copySize < readByte){
                                    stringBuilder.append(new String(buff,copySize,readByte-copySize));
                                    hadReceiveDataLength += readByte-copySize;
                                }
                                streamLength = 0;
                                status = FIND_DATA_STATUS;
                            }
                        }
                    }else if(status == FIND_DATA_STATUS){
                        if(readByte < dataLength - hadReceiveDataLength){
                            stringBuilder.append(new String(buff, 0, readByte));
                            hadReceiveDataLength += readByte;
                        }else {
                            // 完成一条数据
                            stringBuilder.append(new String(buff, 0, dataLength - hadReceiveDataLength));
                            DimsNotifier.getInstance().notify(mContext, stringBuilder.toString());

                            streamLength = readByte-dataLength+hadReceiveDataLength;
                            System.arraycopy(buff, dataLength - hadReceiveDataLength, streamBuff, 0, streamLength);
                            status = FIND_START_STATUS;
                        }
                    }
                }catch (IOException e){
                    synchronized (AccesserManager.getInstance().getSyncLock()){
                        try{
                            mInputStream = AccesserManager.getInstance().getReceiverInputStream(mInputStream);
                            if(mInputStream == null){
                                Log.i(TAG, "Network failed!");
                                break;
                            }
                        }catch (IOException e1){
                            Log.i(TAG, "Network failed!");
                            break;
                        }
                    }
                    e.printStackTrace();
                }
            }
        }
    }
}
