package cc.appweb.www.core.protocol;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author jefferygong
 * @since 2017-3-6
 * @version 1.0
 * 获取服务协议
 */
public class AccessServiceProtocol extends BasicProtocolImp {

    // 发送端
    public final int type = 8001;       // 命令码
    public String appid = null;         // appID
    public String usr = null;           // 用户名
    public String pwd = null;           // 密码

    // 接收端
    public String receiveData = null;  // 接收的字符串数据

    @Override
    public void transToSend(){
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

    @Override
    public void transToRead(){
        try{
            JSONObject receiveDataJson = new JSONObject(receiveData);
            toReceive = receiveDataJson;
        }catch (JSONException e){
            e.printStackTrace();
        }
    }
}
