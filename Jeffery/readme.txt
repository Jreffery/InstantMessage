Jeffery's folder

//------节点服务器接入协议-------
//请求
{
	"type":8000,         //请求码
	"port":8010          //端口号，暂定
}

//响应
{
	"type":7000,
	"code":200,          //200成功，503错误
	"errMsg":null        //返回信息，如果有
}


//----------用户获取服务协议----------
//请求
{
	"type":8001,
	"appid":'1111111111111111',       //16位
	"usr":"xxxxxxxxxxxxx",        //appid应用下的用户名，开发者指定。
	"pwd":"xxxxxxxxxxxxx"        //appid应用下的密码，开发者指定。
	//服务器将校验，存在并检验成功则返回200。存在但检验不成功则返回失败，不存在则创建。
}

//响应
{
	"code":200,         //200成功，500用户校验失败，503无服务，一般为服务器压力过大，504appid失败
	"nodeMsg":'127.0.0.1-8010'     //节点信息
}


//-----------用户连接协议----------
//身份登入
{
	"type"：8002,
	"appid":"1111111111111111",
	"usr":"xxxxxxxxxxxxx",
	"pwd":"xxxxxxxxxxxxx"
}
//响应
{
	'type':7002,
	"code":200        //200成功，503无服务，一般为服务器压力过大
}


// -----------发送消息-------------
//发送方
{
	'type':8003,
	'data':'xxxxxxxxxxxx',
	'receiver':'xxxxxxxx',
	'msgID':xxxxx
}
//响应
{
	'type':7003,
	'code':200,
	'msgID':xxxx
}
//接收方
{
	'type'：7103,
	'data':'xxxxxxxxxxx',
	'sender':'xxxxxxxxx'
}

//------节点服务器发送转发消息给主服务器----
//发送者
{
	'type':8020,
	'receiver':'xxxxxxxx',
	'appid':'xxxxxxxx',
	'data':'xxxxxxxx',
	'sender':'xxxxxxxxx'
}
//响应无响应

// ----主服务器发送转发消息给节点服务器
{
	'type':7020,
	'receiver':'xxxxxxxx',
	'appid':'xxxxxxxx',
	'data':'xxxxxxxx',
	'sender':'xxxxxxxxx'
}



//------------心跳包--------------
//发送者，每隔10秒触发一次
{
	'type':8004
}
// 服务端不响应


//---------节点服务器向主服务器提交新增用户-----
// 发送方
{
	'type':8010,
	'usr':'xxxxxxxx',
	'appid':'xxxxxxxxxxxx'
}
// 响应
--无响应

//---------节点服务器向主服务器提交下线用户-----
{
	'type':8011,
	'usr':'xxxxxxxx',
	'appid':'xxxxxxxxxxxx'
}
// 响应
--无响应







//------------依赖包------------
--twisted
--redis
--MySQLdb