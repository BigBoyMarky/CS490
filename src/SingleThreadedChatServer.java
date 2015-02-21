import java.util.*;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
import java.net.*;
import java.io.*;

	//Create a server based on command line input, DONE
	//server waits for clients to connect, DONE
	//once the clients connect, it sends acknowledgement back, adds clinet to a list.
		//it then sends an update to all previously connectd clients about the new user
	//it regularly checks for heartbeats. If heartbeat not received, it will assume it's dead and remove it from the list.
	//the client can initiate a chat with another client

public class SingleThreadedChatServer// implements Runnable
{
	/**************************************************************************************************
	*											FIELDS												*
	**************************************************************************************************/	
	private long heartbeat_rate = 4000;//in milliseconds
	ServerSocket serverSocket;
	static SingleThreadedChatServer server;
	private int port;//port
	private int numClients = 0;//keeps track of number of clients for ID'ing purposes
	static ArrayList<ClientObject> clientList = new ArrayList<ClientObject>();//the lists = the group G
	static ArrayList<Socket> socketList = new ArrayList<Socket>();//socket lists for accessing them later, e.g. when a client requests the list of group G

	/**************************************************************************************************
	*											MAIN METHOD											*
	**************************************************************************************************/
	public static void main(String[] args) throws IOException
	{
		//initialization
		if(args.length > 0)
		{
			try
			{
				int port = Integer.parseInt(args[0]);
				if(port >= 1025 && port <= 65535)
					server = new SingleThreadedChatServer(port);
				else
				{
					System.out.println("Port is out of range. Try a port between 1025 and 65535. This program will close.");
					return;
				}
			}
			catch(Exception e)
			{
					System.out.println("Please enter valid input. We want integers only!");
			}			
		}
		else
		{
			server = new SingleThreadedChatServer(0);//if none was specified, uses 0, which locates default
			System.out.println("Port was not specified. Using free port " + server.serverSocket.getLocalPort());
			System.out.println("The IP address of this server is " + InetAddress.getLocalHost().getHostAddress());		
		}
		server.runServer();
	}
	/**************************************************************************************************
	*											CONSTRUCTOR												*
	**************************************************************************************************/
	public SingleThreadedChatServer(int port) throws IOException
	{
		this.port = port;
		serverSocket = new ServerSocket(port);
		serverSocket.setReuseAddress(true);
	}

	/**************************************************************************************************
	*										ClientObject INNER CLASS								*
	**************************************************************************************************/
	public class ClientObject implements Runnable
	{
				/****************************************************************************
				*							ClientObject's FIELDS							*
				*****************************************************************************/
		private String name;
		private String address;
		private int port;
		private int id;
		private boolean alive;
				/****************************************************************************
				*							ClientObject's CONSTRUCTOR						*
				*****************************************************************************/		
		public ClientObject(String name, String address,int port, int id)
		{
			this.name = name;
			this.address = address;
			this.port = port;
			this.id = id;
			alive = true;
		}
	/**************************************************************************************************
	*											heartbeats 											*
	**************************************************************************************************/			
		public void run()
		{
			try
			{
				BufferedReader reader = new BufferedReader(new InputStreamReader(socketList.get(this.id).getInputStream()));
				while(this.alive)
				{
					//flips switch off, waits for heartbeat, if receive a message, flips switch back on. Otherwise finish execution
					this.alive = false;
					Thread.sleep(heartbeat_rate);//in essence our timer
					String message;
					if((message=reader.readLine()) != null)//if not null, message received
						this.alive = true;//flips switch on
					if(message.equals("get"))//if message was "get", sendList()
						sendList();
				}
				//remove from list
				clientList.remove(this.id);
				//thread is done executing and therefore dies				
			}
			catch(Exception e)
			{
				e.printStackTrace();
				//could either be IOException or InterruptedException
			}
		}
	/**************************************************************************************************
	*											SEND LIST											*
	**************************************************************************************************/
		public void sendList()
		{
			try
			{
				PrintWriter printer = new PrintWriter(socketList.get(this.id).getOutputStream(),true);//creates a new PrintWriter object to service this specific socket
				for(int i= 0; i < clientList.size(); i++)//prints out all clients on list
					printer.println(clientList.get(i).serialize());
				printer.println("\\0");//null terminator to tell the guy reading the list that's it's done and no more messages not bc of latency
			}
			catch(IOException e)
			{
				System.err.println(e);
			}
		}		
	/**************************************************************************************************
	*										SERIALIZING	CLIENTOBJECT								*
	**************************************************************************************************/	
		public String serialize()
		{
			return (this.name + " " + this.port);//serializing is just turning it into a string :P
		}
	}
	/**************************************************************************************************
	*									END OF ClientObject INNER CLASS								*
	**************************************************************************************************/	

	/**************************************************************************************************
	*										RECEIVE/PROCESS MESSAGES								*
	**************************************************************************************************/	
	public void runServer()
	{
		//initialization
		clientList = new ArrayList<ClientObject>();
		socketList = new ArrayList<Socket>();
		try
		{
			while(true)
			{
				Socket socket = serverSocket.accept();//waits for new request, if got it, continue
				PrintWriter printer = new PrintWriter(socket.getOutputStream(),true);//for sending messages
				BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));//for reading messages
				String name = "";
				String address = "";
				String message, fifoID, actualMessage;
				int port = -1;
				//registering the client here
				while(name.equals("") || port == -1 || address.equals(""))//if any of them are uninitialized, it means client not done reg
				{
					message = reader.readLine();//first message is read
					fifoID = message.substring(0,1);//to guarantee FIFO oooooohhhh
					actualMessage = message.substring(1,message.length()-1);//actual message received
					if(fifoID.equals("0"))//name
						name = actualMessage;
					if(fifoID.equals("1"))//address
						address = actualMessage;
					if(fifoID.equals("2"))//port
					{
						try
						{
							port = Integer.parseInt(actualMessage);//have to convert from string to int
						}
						catch(NumberFormatException e)
						{
							System.err.println(e);
						}
					}
				}
				//once it's reg'd, we add the client's socket to the list, add the client to the list, then start its heartbeat
				//this loop ends, and we're ready to service the next client
				//clientList.add(new Thread(new ClientObject(name, address, port)).start);
				++numClients;//used to keep track of Clients and their sockets. It's their id
				socketList.add(socket);										
				clientList.add(new ClientObject(name, address, port, numClients));
				new Thread(clientList.get(clientList.size()-1)).start();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Will be more specific about exceptions later");
		}		
//old code might remove later
/*		CheckAliveClients heartbeatcheck = new CheckAliveClients( "check thread", clientList, heartbeat_rate );
		heartbeatcheck.start();		
		try
		{
			while(true)
			{
				Socket socket = serverSocket.accept();
				PrintWriter printer = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String message = reader.readLine();
				
				//'0' = getList(); '1' = heartbeat; '2' = ;
				
				// reg_name_ip_port
				
				Pattern p = Pattern.compile("reg_(.)+_(.)+");
				Matcher m = p.matcher(message);
				boolean regMessage = m.matches();
				
				if( regMessage ){
					String[] info = message.split("_");
					clientList.add(new ClientObject(info[0], info[1], this.port, heartbeat_rate, socket));
					printer.println("created!");
					printer.flush();
				}
				
				if(message.equals("0")){
					sendList(socket, clientList);
				}
				if(message.equals("1")){
					putHeartbeat(socket, clientList);
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}*/
//end of old code to remove later
	}

//old code
	/**************************************************************************************************
	*											SEND LIST											*
	**************************************************************************************************/
/*	public void sendList(Socket socket, ArrayList<ClientObject> clientList){
		
		SocketAddress addr = socket.getRemoteSocketAddress();
		for( ClientObject c: clientList ){
			if(addr.equals(c.getSocket().getRemoteSocketAddress())){
				// SERIALIZE THE ARRAYLIST
				break;
			}
		}
	}*/
	//end of old code
//old code might remove
	/*public void putHeartbeat(Socket socket, ArrayList<ClientObject> clientList){
		long currenttime = System.currentTimeMillis();
		SocketAddress addr = socket.getRemoteSocketAddress();
		for( ClientObject c: clientList ){
			if(addr.equals(c.getSocket().getRemoteSocketAddress())){
				if( currenttime - c.getLastBeat() <= heartbeat_rate ){
					c.setHeartBeatTime(System.currentTimeMillis());
				}
				break;
			}
		}
	}*/
	//end of old code
}