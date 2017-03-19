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
    public static final int serverPort = 8001;
    /** appKey的meta-data key **/
    public static final String DIMS_APP_KEY = "DIMS_APP_KEY";
    /** 心跳包的发送频率 **/
    public static final int HEART_BEAT_FREQUENCY = 100*1000;               // 100秒
    /** 登录成功的广播 **/
    public static final String BROADCAST_LOGIN_SUCCESS = "cc.appweb.www.dims.LOGIN_SUCCESS";
    /** 发送成功的广播 **/
    public static final String BROADCAST_SEND_SUCCESS = "cc.appweb.www.dims.SEND_SUCCESS";
    /** 接收到消息的广播 **/
    public static final String BROADCAST_RECEIVE_MESSAGE = "cc.appweb.www.dims.RECEIVE_MESSAGE";
    /** server down掉 **/
    public static final String BROADCAST_RECEIVE_SERVER_DOWN = "cc.appweb.www.dims.SERVER_DOWN";
    /** 接收协议的分隔符 **/
    public static byte[] RECEIVE_BOUNDARY = "#####".getBytes();

}
