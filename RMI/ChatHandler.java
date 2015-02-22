
import java.rmi.*;
import java.rmi.server.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
 
public class ChatHandler extends UnicastRemoteObject implements ChatHandlerInterface{
	
	private String name;
	private List<ChatHandlerInterface> registeredClients;
	private long lastHeartBeat;
	private long heartbeatRate;
	
	public ChatHandler(long heartbeatrate) throws RemoteException{
		this.registeredClients = new ArrayList<ChatHandlerInterface>();
		this.heartbeatRate = heartbeatrate;
	}
	
	public ChatHandler(String name) throws RemoteException{
		this.name = name;
		this.lastHeartBeat = System.currentTimeMillis();
	}
	
	public synchronized void clearClient() throws RemoteException{
		for(ChatHandlerInterface x: registeredClients){
			if(System.currentTimeMillis() - x.getHeartBeatTime() > heartbeatRate){
				registeredClients.remove(x);
				broadcast("[System] " + x.getName() +" has disconnected.");
			}
		}
	}
	
	public synchronized void broadcast(String msg) throws RemoteException{
		//for( ChatHandlerInterface c: registeredClients )
		//	c.send(msg);
	}
	

	@Override
	public synchronized String getName() throws RemoteException {
		return this.name;
	} 


	@Override
	public void send(String s) throws RemoteException {
		System.out.println(s);
	}

	@Override
	public synchronized void registerClient(ChatHandlerInterface c) throws RemoteException {
		for( ChatHandlerInterface i: registeredClients ){
			i.send("[System] "+c.getName()+" is online");
		}
		registeredClients.add(c);
		c.send("[System] Connected to server");
		getList(c);
	}
	
	@Override
	public synchronized ChatHandlerInterface startChat(String targetName)
			throws RemoteException {
		for( ChatHandlerInterface c: registeredClients ){
			if(c.getName().equals(targetName))
				return c;
		}
			
		return null;
	}

	@Override
	public synchronized void getList(ChatHandlerInterface target) throws RemoteException {
		target.send("[System] Online clients:");
		for( ChatHandlerInterface c: registeredClients )
			target.send("[System] " + c.getName());
	}

	@Override
	public synchronized void sendHeartBeat(String name) throws RemoteException {
		for( ChatHandlerInterface c: registeredClients )
			if(c.getName().equals(name))
				c.setHeartBeatTime();
	}

	@Override
	public synchronized void setHeartBeatTime() throws RemoteException {
		this.lastHeartBeat = System.currentTimeMillis();
	}

	@Override
	public synchronized long getHeartBeatTime() throws RemoteException {
		return this.lastHeartBeat;
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