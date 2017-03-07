package cc.appweb.www.core.protocol;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2017/3/7.
 */
public class MessageProtocol extends BasicProtocolImp {
    public final int type = 8003;
    public String receiver = null;
    public int msgID = 0;
    public String data = null;

    @Override
    public void transToRead() {
        // nothing
    }

    @Override
    public void transToSend() {
        try{
            JSONObject sendData = new JSONObject();
            sendData.put("type", type);
            sendData.put("receiver", receiver);
            sendData.put("data", data);
            sendData.put("msgID", msgID);
            toSend = sendData;
        }catch (JSONException e){
            e.printStackTrace();
        }
    }
}
