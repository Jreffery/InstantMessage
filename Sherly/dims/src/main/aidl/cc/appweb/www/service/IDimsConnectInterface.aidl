// IDimsConnectInterface.aidl
package cc.appweb.www.service;

// Declare any non-default types here with import statements

interface IDimsConnectInterface {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);

    /**
     * 连接至服务器
     **/
    int connectToDims(String appId, String usr, String pwd);
}
