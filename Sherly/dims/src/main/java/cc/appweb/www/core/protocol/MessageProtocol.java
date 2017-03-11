package cc.appweb.www.core.protocol;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author jefferygong
 * @since 2017-3-6
 * @version 1.0
 * 发送消息协议
 */
public class MessageProtocol implements ISendableProtocol{
    public final int type = 8003;
    public String receiver = null;
    public long msgID = 0;
    public String data = null;

    @Override
    public byte[] getSendByte(){
        byte[] sendByte = null;
        try{
            JSONObject sendData = new JSONObject();
            sendData.put("type", type);
            sendData.put("receiver", receiver);
            sendData.put("data", data);
            sendData.put("msgID", msgID);
            sendByte = sendData.toString().getBytes();
        }catch (JSONException e){
            e.printStackTrace();
        }
        return sendByte;
    }
}
