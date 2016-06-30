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
            socketClient.start();
            BufferedReader buf = new BufferedReader(new InputStreamReader(System.in));
            String str = buf.readLine();
            while(!str.equals("exit")){
            	os.write(("002"+testName+"\n"+str).getBytes());
            	os.flush();
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
	
	public void run(){ 
		try{
	    	InputStream is = socket.getInputStream(); 
	        byte[] buf = new byte[50];
	        int len ;
	        while(status){
	        	StringBuffer buff = new StringBuffer();
	        	while((len = is.read(buf))!= -1){  
	        		buff.append(new String(buf));
	        		System.out.println(buff.toString());
	        	}        		
	        	
	        }
    	}catch(Exception e){
    		e.printStackTrace();
    	}
	}

	public void exitThread(){
		status = false;
	}

}
