                        DIME 1.0.0

启动步骤
1.启动唯一的主服务器
	/Jeffery/main_server目录下：
	python main.py [configure.xml]
	--[configure.xml] 是可选的配置文件，缺省的话会加载同目录下的configure.xml文件
	--该操作将监听本机的8001（hardcode）端口
	--接收来自nodeServer和user的所有请求
	--nodeServer连接后不会断开，user响应完会断开

2.节点服务器注册
	/Jeffery/node_server目录下：
	python main.py [configure.xml]
	--将随机选择端口（8002~8022）监听，作为节点服务器

	python main.py [configure1.xml]
	--启动第二个节点服务器，如遇监听端口与第一个节点服务器相同，请关闭后再次启动
	--启动完毕后，主服务器将拥有两个可用节点服务器
	--修改主服务器代码下的阈值，可以hardcode将同一个appid下的两个用户分布在不同的节点服务器上，用以测试

	此时，主服务器将记录跟踪该节点的状态，并适时为其分配用户连接

3.连接用户模拟测试
	/Sherly目录下：
	首先运行user2:
	python user2.py
	--连接登录成功后会一直等待user1发来信息

	python user1.py
	--发送的信息流路径为：user1->node1->main->node2->user2





------------------------------备忘------------------------------

00110101  ------->  'a'   :  编码
'a'  ------->  00110101   :  解码