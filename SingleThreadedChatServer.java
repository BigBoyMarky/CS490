import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.Object;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SingleThreadedChatServer {
	
	static SingleThreadedChatServer server;
	private ServerSocket serverSocket = null;
	private int heartbeat_rate;
	static Thread myThread;
	
	private Vector<Client> connectedClients = new Vector<Client>();
	
	public static void main(String[] args) throws IOException
    {
        if(args.length > 0)
        {
            try
            {
                int port = Integer.parseInt(args[0]);
                if(port >=1025 && port <= 65535)
                {
                    server = new SingleThreadedChatServer(port);
                    myThread = new Thread(server);
                }
                else
                {
                    System.out.println("Port number is out of range. Try a port between 1025 and 65535. This program will end.");
                    return;
                }
            }
            catch(Exception e)
            {
                System.out.println("Invalid input. Please specify the port number with integers. Also make sure the port is between 1025 and 65535");
            }
        }
        else
        {
            server = new SingleThreadedChatServer();
            System.out.println("Port not specified. Using free port " + server.getLocalPort());
            myThread = new Thread(server);
        }
        myThread.start();
    }
	
	 public SingleThreadedChatServer(int port) throws IOException 
	 {
	        serverSocket = new ServerSocket(port);
	        serverSocket.setReuseAddress(true);
	 }
	
	 public SingleThreadedChatServer() throws IOException 
	 {
	        serverSocket = new ServerSocket(0);
	        serverSocket.setReuseAddress(true);
	 }
	 
	
	
	void run () {
		

		
		while ( true ) {
			Socket socket = serverSocket.accept();			
			PrintWriter print = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            String msg = reader.readLine();
            
            Pattern p = Pattern.compile("register<*,*,*>");
            
            Matcher m = p.matcher(msg);
            boolean b = m.matches();
            
            if (b) {
            	;	
            }
            
            else if( msg.equals("heartbeat")) {
            	
            }
            
            else if( msg.equals("get")){
            	
            }
            else {
            	System.out.println("Invalid message");
            }
            
            
            
            
            
            
            
            
            
            
       }
            /*
			while ( true ) {
				receive message m from client c;
				if (m == register <name , ip , port >)
				// process registration , and send an a c k n o w l e d g e m e n t / failure to c
				else if (( m == heartbeat <name >)
				if ( previous heartbeat < name > was received less than
				heartbeat_rate seconds ago )
				// keep client c alive
				else if (m == get )
				// send G to client c
				else
				// invalid message from client c , server closes connection ;
				}
			}
		*/
		
		
		
		while (true) {
		
			Message fromClient = (Message)input.readObject();
			
		}
		
	}
	
}
