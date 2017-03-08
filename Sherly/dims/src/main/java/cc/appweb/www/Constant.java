package cc.appweb.www;

/**
 * @author jefferygong
 * @since 2017-3-5
 * @version 1.0
 * sdk的常量
 */
public class Constant {
    /** 系统ip地址 **/
    public static final String serverIP = "192.168.253.1";
    /** 系统的端口号 **/
    public static final int serverPort = 8000;
    /** appKey的meta-data key **/
    public static final String DIMS_APP_KEY = "DIMS_APP_KEY";
    /** 心跳包的发送频率 **/
    public static final int HEART_BEAT_FREQUENCY = 10*1000;
    /** 发送成功的广播 **/
    public static final String BROADCAST_SEND_SUCCESS = "cc.appweb.www.dims.SEND_SUCCESS";
    /** 接收到消息的广播 **/
    public static final String BROADCAST_RECEIVE_MESSAGE = "cc.appweb.www.dims.RECEIVE_MESSAGE";
}
