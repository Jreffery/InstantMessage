package cc.appweb.www.core.protocol;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2017/3/8.
 */
public class LoginProtocol extends BasicProtocolImp {
    public final int type = 8002;
    public String appid = null;
    public String usr = null;
    public String pwd = null;

    @Override
    public void transToRead() {

    }

    @Override
    public void transToSend() {
        try{
            JSONObject sendData = new JSONObject();
            sendData.put("type", type);
            sendData.put("appid", appid);
            sendData.put("usr", usr);
            sendData.put("pwd", pwd);
            toSend = sendData;
        }catch (JSONException e){
            e.printStackTrace();
        }
    }
}
