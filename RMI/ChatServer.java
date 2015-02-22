

import java.rmi.*;
import java.rmi.server.*;
import java.util.*;
 
public class ChatServer {
public static void main (String[] argv) {
	long heartbeatrate = 1000;
    try {
	    	System.setSecurityManager(new RMISecurityManager());
	    	Scanner s=new Scanner(System.in);
	    	System.out.println("Starting Server...");
	    	//String name=s.nextLine().trim();
 
	    	ChatHandler server = new ChatHandler(heartbeatrate);	
 
	    	Naming.rebind("rmi://localhost/ABC", server);
 
	    	System.out.println("[System] Chat Remote Object is ready:");
 
	    	while(true){
				server.clearClient();
				Thread.sleep(200);
	    	}
 
    	}catch (Exception e) {
    		System.out.println("[System] Server failed: " + e);
    	}
	}
}