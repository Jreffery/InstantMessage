package cc.appweb.www.core.protocol;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author jefferygong
 * @since 2017-3-7
 * @version 1.0
 * 心跳包协议
 */
public class HeartBeatProtocol extends BasicProtocolImp {

    private final int type = 8004;

    @Override
    public void transToSend() {
        try{
            JSONObject sendData = new JSONObject();
            sendData.put("type", type);
            toSend = sendData;
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    @Override
    public void transToRead() {
        // nothing
    }
}
