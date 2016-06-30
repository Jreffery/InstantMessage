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