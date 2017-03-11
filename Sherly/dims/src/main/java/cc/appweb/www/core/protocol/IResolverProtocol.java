package cc.appweb.www.core.protocol;

/**
 * @author jefferygong
 * @since 2017-3-6
 * @version 1.0
 * 可解析的协议
 */
public interface IResolverProtocol {
    Object getReceiveData(Object receiveData);
}
