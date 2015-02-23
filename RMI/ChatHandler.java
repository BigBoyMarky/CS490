
import java.rmi.*;
import java.rmi.server.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
 
public class ChatHandler extends UnicastRemoteObject implements ChatHandlerInterface{
	
	private String name;
	private ConcurrentMap<String, ChatHandlerInterface> registeredClients;
	private ConcurrentMap<String, Long> lastHeartBeat;
	private long heartbeatRate;
	
	public ChatHandler(long heartbeatrate) throws RemoteException{
		this.registeredClients = new ConcurrentHashMap<String, ChatHandlerInterface>();
		this.lastHeartBeat = new ConcurrentHashMap<String, Long>();
		this.heartbeatRate = heartbeatrate;
	}
	
	public ChatHandler(String name) throws RemoteException{
		this.name = name;
	}
	
	public boolean isAlive(String name) throws RemoteException{
		return registeredClients.containsKey(name);
	}
	
	public synchronized void clearClient() throws RemoteException{
		
		Set<String> removed = new HashSet<String>();
		
		for(String clientName: lastHeartBeat.keySet()){
			if(System.currentTimeMillis() - lastHeartBeat.get(clientName) > heartbeatRate){
				registeredClients.remove(clientName);
				broadcast("[System] " + clientName +" has disconnected.");
				removed.add(clientName);
			}
		}
		for(String clientName: removed)
			lastHeartBeat.remove(clientName);
		
	}
	
	public synchronized void broadcast(String msg) throws RemoteException{
		for( ChatHandlerInterface c: registeredClients.values() ){
			try{
				c.send(msg);
			}
			catch(Exception e){
				// unfortunately, things happen.
			}
		}
	}
	

	@Override
	public synchronized String getName() throws RemoteException {
		return this.name;
	} 


	@Override
	public synchronized void send(String s) throws RemoteException {
		System.out.println(s);
	}

	@Override
	public void registerClient(ChatHandlerInterface c) throws RemoteException {
		broadcast("[System] "+c.getName()+" is online");
		registeredClients.put(c.getName(), c);
		c.send("[System] Connected to server");
		//getList(c);
	}
	
	@Override
	public ChatHandlerInterface startChat(String targetName)
			throws RemoteException {	
		return registeredClients.get(targetName);
	}

	@Override
	public void getList(ChatHandlerInterface target) throws RemoteException {
		target.send("[System] Online clients:");
		for( String clientName: registeredClients.keySet() )
			target.send("[System] " + clientName);
	}

	@Override
	public void sendHeartBeat(String name) throws RemoteException {
		System.out.println(name +" <3");
		lastHeartBeat.put(name, System.currentTimeMillis());
	}

}

/*

Scenarios:

Server created one ChatHandler and use it as a mainserver by default

1. Server <-> Client

   Client 
   ================================================== 
   grabbed this instance of the server.
   send registration message to it
   send heartbeat to it every certain period of time
   
   Server
   ==================================================
   register the client
   send confirmation to the client
   every time it receives the heartbeat, update it.

2. Client A => Client B

   Client A
   ================================================== 
   grabbed this instance of the server (if hasn't done so).
   call the startchat to receive the handler for chatting with Client B
   wait for Client B's response
   redirect message target to Client B handler until quit
   
   Client B
   ================================================== 
   be prompted of incoming chat request
   answer yes
   redirect message target to Client B handler until quit

*/