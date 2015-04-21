package edu.purdue.cs490;

import java.net.ConnectException;
import java.util.InputMismatchException;
import java.net.SocketException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Collection;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.ServerSocket;
import java.util.Hashtable;
import java.io.InterruptedIOException;
import java.util.ArrayList;

	/**************************************************************************************************
	*									SPECIFICATIONS FOR SERVER SIDE								*
	**************************************************************************************************/
	/**
	1) When it connects with the server, it will send 2 Strings:
		1) "R"[username][spaces until 13 characters][local ipv4 address][space][port #], e.g.: RCharlie      127.0.0.1 42691
		2) and "get". It will expect the server to send a list of users with the following format for each single user
			[length of name][username][local ipv4 address][space][port #], e.g.: 07Charlie127.0.0.1 42691.
			It will keep reading users and waiting until it sees "\\0", so be sure to println that after your for loop is done
		3) If registration is valid, it expects a response. It can be any response, but if it's "U", it will disconnect.
	2) It will continually send "<3" every heartbeat_rate. It's its heartbeat.
	3) It can send "get" anytime after it connects with the server for any number of times.
	4) Any number of clients can try to connect to the server
	5) Do NOT allow multiple clients with the same name. Right during the connection, the client will listen for a message. If
		the message is "U", it will tell the user to change his/her username. (aka send "U" back if name is a repeat). The client
		will also send a "get" so the user can know which usernames are still available
	*/
//should make currentInterlocuter back to normal after broadcasting
//printing on sender's side is a bit slow...actually really slow...not sure why.
public class ChatClient implements Runnable, BroadcastReceiver
{
	/**************************************************************************************************
	*											FIELDS												*
	**************************************************************************************************/
	private ChannelInterface channel;
	private static long heartbeat_rate = 5000;
	private static int SOCKET_TIMEOUT = 100;//socket SOCKET_TIMEOUT
	private String host;
	private String name;//name of the Client
	private String id;
	private int serverPort;//port of the server it's going to connect to
	private int clientPort;//port of the client's ServerSocket for chatting with other clients
	private String ip;//ip of the client
	private Socket socket;//socket for connecting purposes
	private ServerSocket serverSocket;//for connecting to other users directly???
	private ObjectOutputStream heart;//printer to server
	private ObjectInputStream heartListener;//reader to server
	private Socket currentChatSocket;//the current Socket you're chatting in right now
	private boolean inChat;//once someone gets a message, they are forced in chat
	private long heartbeat = 0;
	private ClientObject myClientObject;//object representing this specific client for server purposes
	private ConcurrentHashMap<String,ClientObject> listOfUsers = new ConcurrentHashMap<String,ClientObject>();//hashmap of users for connecting to others
	private String[] commands = {"\\hey","\\switch","\\list","\\rb","\\help", "\\fifo", "\\beb", "\\OVER9000", "\\print"};//list of available commands
	private ClientObject currentInterlocuter;
	private int numInterlocuters = 0;
	private boolean firstCrashReport = true;
	private ReliableBroadcaster rb;
	private FIFOReliableBroadcaster fifo;
	private BEBroadcaster beb;
	private CausalReliableBroadcaster cob;
	private int messageCounter = 0;//for fun
	private int broadcastCounter = 0;//only useful one for our purposes
	private VectorClock myVectorClock;
	/**************************************************************************************************
	*											MAIN METHOD											*
	**************************************************************************************************/
	public static void main(String[] args)
	{
		try
		{
			ChatClient myChatClient = new ChatClient(/*"-1",-1,"-1"*/);//because of the fucking superclass's constructor requirements
			myChatClient.register();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	/************************************************************************************************
	*											INITIALIZATION									*
	*************************************************************************************************/
	public void register() throws Exception
	{
		Scanner console = new Scanner(System.in);
		while(true)
		{
			System.out.print("Hostname of the server you want to connect to:");
			this.host = console.next();
			//check validity
			System.out.print("Port of the server to connect to:");
			while(true)//checking validity
			{
				try
				{
					this.serverPort = console.nextInt();
					if(this.serverPort >= 1025 && serverPort <= 65535)
						break;
					else
						System.out.printf("Invalid port number! Ports are between 1025 and 65535.\n");
				}
				catch(InputMismatchException e)//if input is not a number
				{
					System.out.printf("We use integers for our ports. Enter one between 1025 and 65535.\n");
					console.nextLine();
					//catching doesn't get rid of the exception?
				}
			}
			console.nextLine();
			System.out.print("Enter your name:");//no need to check for validity bc any name is valid here :)
			name = console.nextLine();
			channel = new ChannelInterface(this, name);
			clientPort = channel.getClientPort();
			if(channel.initServer(host,serverPort))
				break;
			else
				System.out.printf("Connection refused. Are you sure you entered the correct information? Please try again!\n");
		}
		//while(clientPort == -1){}//waits for serverSocket to be initialized. Once it's initialized, clientPort will have a value
		myClientObject = new ClientObject(name, InetAddress.getLocalHost().getHostAddress(), clientPort, listOfUsers);
		beb = new BEBroadcaster(myClientObject, this);
		rb = new ReliableBroadcaster(myClientObject, this);
		fifo = new FIFOReliableBroadcaster(myClientObject, this);
		cob = new CausalReliableBroadcaster(myClientObject, this);
		try
		{
			channel.toServer("reg");
			channel.toServer(myClientObject);
			//String verification = (String) channel.fromServer();
			//long verification = (long) channel.fromServer();
			String uuu = (String) channel.fromServer();
			//while(verification == -1)
			while(uuu.equals("U"))
			{
				System.out.println("Registration failed because you have the same name as another user");
				System.out.println("Enter your username again!");
				myClientObject.setName(console.nextLine());
				channel.closeServer();
				channel.initServer(host,serverPort);
				channel.toServer("reg");
				channel.toServer(myClientObject);
				//verification = (long)channel.fromServer();
				uuu = (String)channel.fromServer();
				System.out.println(uuu);
			}
			System.out.println(uuu);
			id = uuu;
			myClientObject.setRealID(id);
			//System.out.print("Your ID = " + id + "\n");
			new Thread(this).start();
			System.out.println("Verified!");
			displayCommands();
			new Thread(new Runnable() {

				public void run(){

					try{
						channel.toServer("get");
					}
					catch(SocketException e){
						e.printStackTrace();
					}
					ConcurrentHashMap<String, ClientObject> members = (ConcurrentHashMap<String, ClientObject>) channel.fromServer();

					rb.failHandler(members.values());

					try{
						Thread.sleep(4000);
					}
					catch(Exception e){
						e.printStackTrace();
					}
				}

			}).start();

			this.heartbeat();
		}
		catch(SocketException e)
		{
			System.out.printf("The server you were trying to register with has crashed.\n");
			System.out.printf("I'd recommend restarting this program and finding a new, active server\n");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	/**********************************************************************************************
	*											HEARTBEAT									*
	***********************************************************************************************/
	public void heartbeat()
	{
		try
		{
			while(true)
			{
				if(System.currentTimeMillis()-heartbeat > heartbeat_rate)
				{
					heartbeat = System.currentTimeMillis();
					channel.toServer("<3");
				}
			}
		}
		catch(SocketException e)
		{
			if(firstCrashReport)
			{
				System.out.printf("The server has crashed.\nYou can check your current list of clients to see who else is still online.\n");
				//System.out.printf("However, the following clients are still online:\n");
				//list all online clients
				/*
				Iterator availableUsers = listOfUsers.entrySet().iterator();
				int counter = 1;
				while(availableUsers.hasNext())
				{
					try
					{
						Map.Entry pair = (Map.Entry)availableUsers.next();
						//check if online by sending an empty message
						channel.whisper((ClientObject)pair.getValue(),"");//sends nothing? hope it works :o
						System.out.printf("%d. %s\t",counter++,pair.getKey());
					}
					catch(SocketException f)
					{
						continue;
					}
				}
				*/
				System.out.printf("But I'd recommend restarting this program and finding a new, active server\n");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

	}
	/**************************************************************************************************
	*										GET AND DISPLAY											*
	**************************************************************************************************/
	public void getAndDisplay()
	{
		get();
		display();
	}
	public void get()
	{
		try
		{
			String user;
			channel.toServer("get");
			System.out.println("Current people online:");
			//needs InvalidProtoclException
			listOfUsers = (ConcurrentHashMap<String, ClientObject>)channel.fromServer();
			channel.updateHashmap(listOfUsers);
		}
		catch(SocketException e)
		{
			if(firstCrashReport)
			{
				System.out.printf("The server has crashed. I was unable to get the list of clients. Look at the last updated list for potential chatmates.");
				System.out.printf("I'd recommend restarting this program and finding a new, active server\n");
				firstCrashReport = false;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	public void display()
	{
			Iterator availableUsers = listOfUsers.entrySet().iterator();
			int counter = 1;
			while(availableUsers.hasNext())
			{
				Map.Entry pair = (Map.Entry)availableUsers.next();
				System.out.printf("%d. %s\t",counter++,pair.getKey());
			}
			System.out.println("\n================================================================================");
	}
	/**************************************************************************************************
	*								PRINTING AVAILABLE COMMANDS										*
	**************************************************************************************************/
	public static void displayCommands()
	{
		System.out.println("Available commands:\n\\hey [user] = initiates chat session with [user]");
		System.out.println("\\switch [user] = switches to another user to send messages to");
		System.out.println("\\list = shows you the list of all available users on this server");
		System.out.println("\\rb [message] = allow everyone to hear what you want to say");
		System.out.println("\\fifo [message] = allow everyone to hear what you want to say in FIFO guaranteed order");
		System.out.println("\\beb [message] = bebdeliver everyone!");
		System.out.println("\\help = shows available commands");
	}

	/**************************************************************************************************
	*								THREAD THAT THE USER INTERACTS WITH								*
	**************************************************************************************************/
	/*Checks if user inputted a command, then extracts it*/
	public String isCommand(String input)
	{
		for(int i = 0; i < commands.length; i++)
		{
			if(input.indexOf(commands[i]) == 0)//checks if it is a command
				return commands[i];//if it is, return it
		}
		return "";//else return nothing
	}
	/*Based on commands, does special actions*/
	public void executeCommand(String command, String message)
	{
		if(command.equals(commands[0]))
		{//command == hey; initialize new socket and add to socketList
			currentInterlocuter = listOfUsers.get(message);
			if(currentInterlocuter != null)
			{
				if(!currentInterlocuter.getInitState())//if not already initiailized
				{
					try
					{
						channel.initClient(currentInterlocuter);//stuck in here
						currentInterlocuter.flipInitState();//what happens if initClient fails?
					}
					catch(ConnectException e)
					{
						System.out.printf("Looks like the client is offline! Unable to invite %s to chat!\n",currentInterlocuter.getID());
					}
				}
				System.out.printf("Chatting with %s\n",message);
			}
			else
			{
				System.out.printf("%s is not online. Please check your spelling!\n",message);
			}
		}
		if(command.equals(commands[1]))
		{//switch; switch socket to user
			currentInterlocuter = listOfUsers.get(message);
			if(currentInterlocuter == null)
			{
				System.out.printf("%s is not online. Please check your spelling!\n",message);
			}
		}
		if(command.equals(commands[2]))
		{//list
			if(firstCrashReport)
				getAndDisplay();
			else
			{
				System.out.printf("Server is offline.\nThis is the last list retrieved:\n");
				display();
			}
		}
		if(command.equals(commands[3]))
		{//everybody
			ChatClientMessage myM = new ChatClientMessage(myClientObject,0,message,1);
			rb.rbroadcast(myM);
			//rb.broadcast(message);
			//rb.broadcast attaches type 1 to ChatClientMessage, if receiver receives it, then saves one
			//System.out.printf("RB");
		}
		if(command.equals(commands[4]))
		{//help and ?
			displayCommands();
		}
		if(command.equals(commands[5]))
		{//fifo
			ChatClientMessage myM = new ChatClientMessage(myClientObject,0,message,2);
			fifo.FIFOBroadcast(myM);
		}
		if(command.equals(commands[6]))
		{
			ChatClientMessage myM = new ChatClientMessage(myClientObject,0,message,0);
			beb.BEBroadcast(myM);
		}
		//SECRET COMMANDS FOR MASTER USERS ONLY :D
		if(command.equals(commands[7]))
		{
			this.tenThousandsBroadcast(0);
		}
		if(commands.equals(commands[8]))
		{
			//for emergency cases if messages were lost
			channel.forcePrint();
		}
		if(command.equals(""))
		{//normal typing
			try
			{
				if(currentInterlocuter != null)
				{
					if(listOfUsers.containsValue(currentInterlocuter))
					{
						ChatClientMessage myM = new ChatClientMessage(myClientObject,0,message,0);
						channel.whisper(currentInterlocuter,myM);
					}
					else
					{
						System.out.printf("%s is offline! Look for someone new to chat with\n",currentInterlocuter.getID());
						currentInterlocuter = null;//turns to null for you!
					}
				}
				else
					System.out.println("Unrecognized command! Enter \'\\help\' for a list of commands.");
			}
			catch(SocketException e)
			{
				System.out.printf("%s cannot be reached!\n",currentInterlocuter.getID());
			}
		}
	}
	public void run()
	{
		String message;//the message string we're going to be dealing with mainly
		String command;//if it is a valid command, command!
		Scanner console = new Scanner(System.in);
		while(true)
		{
			message = console.nextLine();
			command = isCommand(message);
			if(command.length() != 0)
			{
				if(message.length() > command.length())
				{
					message = message.substring(command.length()+1,message.length());//keeps rest of message
				}
				else
					message = "";
			}
			executeCommand(command,message);
		}
	}
	/**************************************************************************************************
	*								GRABBING CHAT FROM OTHER CLIENTS								*
	**************************************************************************************************/
	public ChatClient()
	{
	}
	/**************************************************************************************************
	*								BROADCASTRECEIVER IMPLEMENTATION HERE							*
	**************************************************************************************************/
	public void receive(Message m)
	{
		//prints out message
		System.out.printf("%s:%s\n",m.getSender().getID(),m.getMessageContents());
	}
	public ConcurrentHashMap<String, ClientObject> getHashmap()
	{
		return listOfUsers;
	}
	public ArrayList<String> getNames()
	{
		ArrayList<String> listOfNames = new ArrayList<String>();
		Iterator availableUsers = listOfUsers.entrySet().iterator();
		int counter = 1;
		while(availableUsers.hasNext())
		{
			Map.Entry pair = (Map.Entry)availableUsers.next();
			listOfNames.add(pair.getKey().toString());
		}
		return listOfNames;
	}
	public void setInterlocuter(ClientObject interlocuter)
	{
		currentInterlocuter = interlocuter;
	}

	public void tenThousandsBroadcast(int type)
	{

		for(int i=0;i<10000;i++)
		{

			if(type == 2)
			{
				Message m = new ChatClientMessage(myClientObject, i, Integer.toString(i), type);
				cob.crbroadcast(m);
			}
			else {
				Message m = new ChatClientMessage(myClientObject, Integer.toString(i), type, new VectorClock(listOfUsers));
				if(type==1){
					rb.rbroadcast(m);
				}
				else{
					fifo.FIFOBroadcast(m);
				}
			}
		}
	}
	public ChannelInterface getChannel()
	{
		return channel;
	}
	public FIFOReliableBroadcaster getFIFO()
	{
		return fifo;
	}
	public CausalReliableBroadcaster getCO()
	{
		return cob;
	}
}
