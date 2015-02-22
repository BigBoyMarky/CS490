

import java.rmi.*;
import java.rmi.server.*;
import java.util.*;
 
public class ChatClient {
	
	public static void main (String[] argv) {
	    try {
	    	System.setSecurityManager(new RMISecurityManager());
	    	Scanner s=new Scanner(System.in);
	    	System.out.println("Enter your name: ");
		    String name=s.nextLine().trim();		    		    	
		    ChatHandlerInterface client = new ChatHandler(name);
		    
		    ChatHandlerInterface server = (ChatHandlerInterface)Naming.lookup("rmi://localhost/ABC");
		    
	    	server.registerClient(client);
	    	String msg="[System] "+client.getName()+" is online";
	    	server.send(msg);
	    	
	    	System.out.println("[System] To start chatting, type 1 follow by space and the name of the person you want to chat to i.e. 1 Gott");
	    	
	    	ChatHandlerInterface target = server;
	    	
	    	HeartbeatThread heart = new HeartbeatThread(server, name);
	    	heart.start();
	    	
	    	while(true){
	    		
	    		msg= s.nextLine().trim();
	    		target.send("["+client.getName()+"] " + msg);
	    		
	    		if(msg.indexOf('1')==0 && target==server){

		    		String targetName = msg.split(" ")[1];
		    		
		    		target = server.startChat(targetName);
		    		
		    		if( target != null){
		    			System.out.println("[System] Start sending message to " + targetName);
		    			msg="["+client.getName()+"] "+msg;
		    		}
		    		else{
		    			System.out.println("[System] Error: " + targetName + " is either offline or not found.");
			    			target = server;
			    		}
		    		}
		    	}
 
	    	}catch (Exception e) {
	    		System.out.println("[System] Server failed: " + e);
    	}
	}
	
}