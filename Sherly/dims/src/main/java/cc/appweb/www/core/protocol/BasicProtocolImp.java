package cc.appweb.www.core.protocol;

import android.util.Log;

/**
 * Created by Administrator on 2017/3/7.
 */
public abstract class BasicProtocolImp implements IProtocolBasic {

    public Object toSend = null;                 // 发送数据代理
    public Object toReceive = null;             // 接收数据代理

    /**
     * 将发送数据转换成byte[]并返回
     * */
    public byte[] getSendByte(){
        transToSend();
        return toSend.toString().getBytes();
    }

    /**
     * 将接收数据格式转换后返回
     * 数据类型需要调用者指定
     * */
    public Object getReceiveData(){
        transToRead();
        return toReceive;
    }

}
