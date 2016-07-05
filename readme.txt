                        DMIE 1.0.0

启动Server
--请确保本地已安装 Python2.7
--请确保已安装 Python 模块 twisted
  推荐使用 pip 安装Python模块
  pip install twisted

--环境配置好后，即可启动服务
--进入 Jeffery 文件夹，启动命令
  python main.py



连接服务
--请确保本地已安装Java
--进入 Sherly 文件夹，启动命令（如果没有.class文件，需要自行编译一次）
  [终端1]java Client client1 client2
  [终端2]java Client client2 client1

--解释：运行参数arg1, arg2 分别为用户本身，消息发送对象。也就是说，终端1运行的效果是client1将发消息给client2
--注意：在启动一个client时，请确保arg2所表示的用户将连上服务


-------------------------------2016.7.5更新-----------------------------------
mainServer：
--添加xml配置文件，提供可用nodeServer（host, port）等配置信息
--添加mainServer数据协议：
  001:请求分配服务，返回nodeServer（host, port）
  更多：待完善
--issue:实时调度算法，开放更多协议代码，完善配置文件的格式
--启动：python main.py [configure.xml] (参数指明配置文件)


nodeServer：
--接受client连接，返回提示信息
--issue:连接mainServer并提供数据，完善与client数据传输协议，实现client间消息通信
--启动：python node.py xxxx (端口号，需与配置文件的一致)


--Client:
--修改为先连接mainServer,再连接nodeServer
--删除数据输入，以后添加
--issue：判断每一次数据输入流的结束，加密传输数据（与nodeServer协商），代码向安卓类过渡
--启动：java Client 


