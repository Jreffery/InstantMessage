package cc.appweb.www.core.protocol;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author jefferygong
 * @since 2017-3-7
 * @version 1.0
 * 心跳包协议
 */
public class HeartBeatProtocol implements ISendableProtocol {

    private final int type = 8004;

    @Override
    public byte[] getSendByte() {
        byte[] sendByte = null;
        try{
            JSONObject sendData = new JSONObject();
            sendData.put("type", type);
            sendByte = sendData.toString().getBytes();
        }catch (JSONException e){
            e.printStackTrace();
        }
        return sendByte;
    }
}
