import java.io.*;  
import java.net.InetAddress;  
import java.net.Socket;  
  
  
public class Client {  
      
      
    public static void main(String []args){ 
    	String selfName = args[0];
    	String testName = args[1];
        try {  
            Socket s = new Socket("127.0.0.1", 8013);  
            OutputStream os = s.getOutputStream();   
            os.write(("001"+selfName).getBytes()); 
            SocketClient socketClient = new SocketClient(s);
            WriteClient writeClient = new WriteClient(s);
            socketClient.start();
            writeClient.start();
            BufferedReader buf = new BufferedReader(new InputStreamReader(System.in));
            String str = buf.readLine();
            while(!str.equals("exit")){
            	writeClient.sendMethod(testName+"\n"+str);
            	str = buf.readLine();
            }
            socketClient.exitThread();

        } catch (Exception e) {  
            e.printStackTrace();  
        }  
    }  
  
}  

class SocketClient extends Thread{
	private Socket socket;
	private boolean status = true;
	private boolean send = false;
	private String sendData = null;

	public SocketClient(Socket s){
		socket = s ;
	}
	
	@Override
	public void run(){ 
		try{
	    	InputStream is = socket.getInputStream(); 
	        byte[] buf=new byte[1024];
	        int len ;
	        while(status){
	        	StringBuffer buff = new StringBuffer();
	        	while((len = is.read(buf))!= -1){   //阻塞
	        		buff.append(buf);
	        	}        		
	        	System.out.println("Received data：" + buff.toString());
	        }
    	}catch(Exception e){
    		e.printStackTrace();
    	}
	}

	public void exitThread(){
		status = false;
	}

}

class WriteClient extends Thread {
	private Socket socket;
	private boolean status = true;
	private boolean send = false;
	private String sendData = null;

	public WriteClient(Socket s){
		socket = s ;
	}
	
	@Override
	public void run(){ 
		try{
			OutputStream os = socket.getOutputStream();  
	        while(status){
	        	if(send){
	        		os.write(("002"+sendData).getBytes());
	        		send = false;
	        	}
	        }
    	}catch(Exception e){
    		e.printStackTrace();
    	}
	}

	public void sendMethod(String sendData){
	        	System.out.println("test");
		send = true;
		this.sendData = sendData;
	}

	public void exitThread(){
		status = false;
	}

}