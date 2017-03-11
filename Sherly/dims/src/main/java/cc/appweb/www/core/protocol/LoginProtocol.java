package cc.appweb.www.core.protocol;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author jefferygong
 * @since 2017-3-6
 * @version 1.0
 * 登录协议
 */
public class LoginProtocol implements ISendableProtocol {
    public final int type = 8002;
    public String appid = null;
    public String usr = null;
    public String pwd = null;

    @Override
    public byte[] getSendByte(){
        byte[] sendByte = null;
        try{
            JSONObject sendData = new JSONObject();
            sendData.put("type", type);
            sendData.put("appid", appid);
            sendData.put("usr", usr);
            sendData.put("pwd", pwd);
            sendByte = sendByte.toString().getBytes();
        }catch (JSONException e){
            e.printStackTrace();
        }
        return sendByte;
    }
}
