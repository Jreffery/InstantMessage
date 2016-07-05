import java.io.*;  
import java.net.Socket; 
  
  
public class Client {  
    public static void main(String []args){
    	String host = "";
    	String port = "";
        try {
        	System.out.println("Try to connect the mainServer to get configured server..............");
            Socket s = new Socket("127.0.0.1", 8013);  
            OutputStream os = s.getOutputStream();   
            os.write("001".getBytes()); 
            try{
    	    	BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream())); 
    	        String buff ;
    	        boolean flag = false ;
    	        while((buff = reader.readLine())!= null){
    	        	if(flag){
    	        		port = buff;
    	        	}else{
    	        		host = buff;
    	        		flag = true;
    	        	}
    	        }        		
        	}catch(Exception e){
        		e.printStackTrace();
        	}finally{
        		System.out.println("You are gonging to connect "+host+":"+port);
        		s.close();
        	}
            
            System.out.println("Type 'Y' to connected or 'exit' to exit");
            BufferedReader buf = new BufferedReader(new InputStreamReader(System.in));
            String str = buf.readLine();
            while(true){
            	if(str.equals("Y")){
            		break;
            	}else if(str.equals("exit")){
            		System.out.println("Bye!");
            		return ;
            	}
            }
            
            s = new Socket(host, Integer.parseInt(port));
            System.out.println("Connecting.......");
            os = s.getOutputStream();   
            os.write("0".getBytes()); 
            BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream())); 
	        String buff ;
	        while((buff = reader.readLine())!= null){
	        	System.out.println(buff);
	        }     
        } catch (Exception e) {  
            e.printStackTrace();  
        }
        System.out.println("Connection lost.");
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
