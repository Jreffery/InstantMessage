package cc.appweb.www.core.protocol;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author jefferygong
 * @since 2017-3-6
 * @version 1.0
 * 获取服务协议
 */
public class AccessServiceProtocol implements ISendableProtocol, IResolverProtocol {

    // 发送端
    public final int type = 8001;       // 命令码
    public String appid = null;         // appID
    public String usr = null;           // 用户名
    public String pwd = null;           // 密码

    @Override
    public byte[] getSendByte(){
        byte[] sendByte=null;
        try{
            JSONObject sendData = new JSONObject();
            sendData.put("type", type);
            sendData.put("appid", appid);
            sendData.put("usr", usr);
            sendData.put("pwd", pwd);
            sendByte = sendData.toString().getBytes();
        }catch (JSONException e){
            e.printStackTrace();
        }
        return sendByte;
    }

    @Override
    public Object getReceiveData(Object receiveData){
        JSONObject receiveObject = null;
        try{
            JSONObject receiveDataJson = new JSONObject((String)receiveData);
            receiveObject = receiveDataJson;
        }catch (JSONException e){
            e.printStackTrace();
        }
        return receiveObject;
    }
}
