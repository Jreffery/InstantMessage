package cc.appweb.www.core.protocol;

/**
 * @author jefferygong
 * @since 2017-3-6
 * @version 1.0
 * DIMS服务协议的基本接口
 * 适用于需要等待响应的协议
 */
public interface IProtocolBasic {
    /**
     * 转为可读的数据结构
     * */
    void transToRead();

    /**
     * 转为可发送的数据结构
     * */
    void transToSend();

}
