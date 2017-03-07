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
    InputStream inputStream = null;
    /** service上下文 **/
    Context mContext = null;

    private Receiver(Context context, InputStream inputStream){
        this.inputStream = inputStream;
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
        @Override
        public void run(){
            // 循环
            while (true){
                byte[] buff = new byte[1024];
                int readByte = 0;
                StringBuilder stringBuilder = new StringBuilder();
                try{
                    while ((readByte = inputStream.read(buff)) > 0 ){
                        stringBuilder.append(new String(buff, 0, readByte));
                    }
                    // 根据协议发送广播，先大概写出来，后续优化设计模式
                    JSONObject jsonObject = new JSONObject(stringBuilder.toString());
                    int type = jsonObject.getInt("type");
                    if(type == 7002){
                        // 接入响应
                        Log.d(TAG, "Connect to node server success.");
                    }else if(type == 7003){
                        // 发送响应
                        Intent intent = new Intent();
                        intent.setAction(Constant.BROADCAST_SEND_SUCCESS);   // 可能不成功
                        intent.putExtra("msgID", jsonObject.getLong("msgID"));
                        mContext.sendBroadcast(intent);
                    }else if( type == 7103){
                        // 接收消息
                        Intent intent = new Intent();
                        intent.setAction(Constant.BROADCAST_RECEIVE_MESSAGE);
                        intent.putExtra("sender", jsonObject.getString("sender"));
                        intent.putExtra("data", jsonObject.getString("data"));
                        mContext.sendBroadcast(intent);
                    }else{
                        // 丢弃（后续考虑加入消息推送功能）
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
        }
    }
}
