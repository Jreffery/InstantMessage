package cc.appweb.www.core;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import cc.appweb.www.Constant;

/**
 * @author jefferygong
 * @since 2017-3-7
 * @version 1.0
 * dims消息通知者
 * 通过发送广播的形式
 */
public class DimsNotifier {
    /** TAG **/
    public static final String TAG = "DimsNotifier";
    private static DimsNotifier ourInstance = new DimsNotifier();

    public static DimsNotifier getInstance() {
        return ourInstance;
    }

    private DimsNotifier() {
    }

    /**
     * 解析协议并发送广播
     * */
    public void notify(Context context, String data){
        try{
            // 根据协议发送广播，先大概写出来，后续优化设计模式
            JSONObject jsonObject = new JSONObject(data);
            int type = jsonObject.getInt("type");
            if(type == 7002){
                // 接入响应
                Log.i(TAG, "Connect to node server success.");
                Intent intent = new Intent();
                intent.setAction(Constant.BROADCAST_LOGIN_SUCCESS);   // 可能不成功
                context.sendBroadcast(intent);
            }else if(type == 7003){
                // 发送响应
                Intent intent = new Intent();
                intent.setAction(Constant.BROADCAST_SEND_SUCCESS);   // 可能不成功
                intent.putExtra("msgID", jsonObject.getLong("msgID"));
                context.sendBroadcast(intent);
            }else if( type == 7103){
                // 接收消息
                Intent intent = new Intent();
                intent.setAction(Constant.BROADCAST_RECEIVE_MESSAGE);
                intent.putExtra("sender", jsonObject.getString("sender"));
                intent.putExtra("data", jsonObject.getString("data"));
                Log.i(TAG, "收到消息：" + jsonObject.getString("data"));
                context.sendBroadcast(intent);
            }else{
                // 丢弃（后续考虑加入消息推送功能）
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    public void serverDown(Context context){
        Intent intent = new Intent();
        intent.setAction(Constant.BROADCAST_RECEIVE_SERVER_DOWN);
        context.sendBroadcast(intent);
    }
}
