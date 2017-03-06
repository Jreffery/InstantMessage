package cc.appweb.www.access;

/**
 * @author jefferygong
 * @since 2017-3-5
 * @version 1.0
 * 系统接入接口
 */
public interface Accesser {
    /**
     * 连接系统
     * */
    void connectDims();

    /**
     * 断开连接
     * */
    void disconnectDims();
}
