/*
TESTS TO DO
1] SERVER CLOSES
2] OTHER CLIENT CLOSES
3] SERVER TERMINATES CLIENT
testing git functionality on cs lab computers
*/
import java.net.SocketException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Iterator;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.ServerSocket;
import java.util.Hashtable;
import java.io.InterruptedIOException;

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
public class ChatClient implements Runnable
{
	/**************************************************************************************************
	*											FIELDS												*
	**************************************************************************************************/
	private static long heartbeat_rate = 5000;
	private String host;
	private String name;//name of the Client
	private int serverPort;//port of the server it's going to connect to
	private int clientPort;//port of the client's ServerSocket for chatting with other clients
	private String ip;//ip of the client
	private Socket socket;//socket for connecting purposes
	private ServerSocket serverSocket;//for connecting to other users directly???
	private ObjectOutputStream heart;//printer to server
	private ObjectInputStream heartListener;//reader to server
	private PrintWriter printer;//printer to client
	private BufferedReader reader;//reader to client
	private Socket currentChatSocket;//the current Socket you're chatting in right now
	private boolean inChat;//once someone gets a message, they are forced in chat
	private ClientObject myClientObject;//object representing this specific client for server purposes
	private ConcurrentHashMap<String,ClientObject> listOfUsers = new ConcurrentHashMap<String,ClientObject>();//hashmap of users for connecting to others
	/**************************************************************************************************
	*											MAIN METHOD											*
	**************************************************************************************************/
	public static void main(String[] args)
	{
		ChatClient myChatClient = new ChatClient();
		myChatClient.register();
		System.exit(0);
	}
	/************************************************************************************************
	*											INITIALIZATION									*
	*************************************************************************************************/
	public void register()
	{
		System.out.print("Hostname of the server you want to connect to:");
		Scanner console = new Scanner(System.in);
		host = console.next();
		//check validity
		System.out.print("Port of the server to connect to:");
		//check validity
		serverPort = console.nextInt();
		console.nextLine();
		System.out.print("Enter your name:");
		name = console.nextLine();
		System.out.println("made client object");
		this.heartbeat();
	}
	public void register(String name, String ipAddress, int port)
	{
		this.name = name;
		host = ipAddress;
		serverPort = port;
		this.heartbeat();
	}
	/**********************************************************************************************
	*											HEARTBEAT									*
	***********************************************************************************************/	
	public void heartbeat()
	{
		long firstAttempt = System.currentTimeMillis();
		long currentAttempt;
		while(true)
		{
			try
			{
				//System.out.println("in try loop");
				Socket socket = new Socket(host,serverPort);
				//System.out.println("made socket");
				heart = new ObjectOutputStream(socket.getOutputStream());
				heart.flush();
				//System.out.println("made otputstream");
				heartListener = new ObjectInputStream(socket.getInputStream());
				//System.out.println("made inputstream");				
				new Thread(this).start();//create a new thread for sending messages
				//System.out.println("made new thread");
				while(true)
				{
					try
					{
						Thread.sleep(heartbeat_rate);//sleeps for heartrate
						System.out.println("sending hearts <3");
						heart.writeObject("<3");//sends message, isn't it adorable
						heart.flush();
					}
					catch(InterruptedException e)
					{
						continue;
					}
				}
			}
			catch(SocketException e)//exception for not being able to connect to server; attempt to try for 5 seconds then try again
			{				
				System.out.print("Attempting connecting with server...\n");
				//System.out.println(e);
				currentAttempt = System.currentTimeMillis();
				if(currentAttempt-firstAttempt > 1000)//5 seconds too long
				{
					System.out.println("Server seems to be unavailable. Try again later?");
					//chatThread.interrupt();
					return;
				}
			}
			catch(IOException e)
			{
				System.out.println("can't connect...");
				continue;
			}
			//exception for something else here
		}
	}
	/**************************************************************************************************
	*										GET AND DISPLAY											*
	**************************************************************************************************/	
	public void getAndDisplay()
	{
		try
		{
			String user;
			heart.writeObject("get");
			heart.flush();
			//System.out.println("sent get");			
			//while(!heartListener.ready()){};
			System.out.println("Current people online:");
			//needs InvalidProtoclException
			listOfUsers = (ConcurrentHashMap)heartListener.readObject();
			//iterate through the hashmap
			Iterator availableUsers = listOfUsers.entrySet().iterator();
			int counter = 1;
			while(availableUsers.hasNext())
			{
				Map.Entry pair = (Map.Entry)availableUsers.next();
				System.out.printf("%d. %s\t",counter++,pair.getKey());
			}
			System.out.println("\n================================================================================");
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		catch(ClassNotFoundException e)
		{
			System.out.println("Oh my god.");
		}
		catch(ClassCastException e)
		{//means that the object we read is not actually what we read...haha
			System.out.printf("The server is sending an object that we cannot read. This is what was sent: %s",listOfUsers.toString());
		}
	}
	/**************************************************************************************************
	*								PRINTING AVAILABLE COMMANDS										*
	**************************************************************************************************/	
	public static void displayCommands()
	{
		System.out.println("Available commands:\nhi [user] = initiates chat session with [user]");
		System.out.println("chatlist = shows you the list of all available users on this server");
		System.out.println("help = shows available commands (same as \'?\')\n? = shows available commands (same as \'help\')");
	}
	/**************************************************************************************************
	*								INITIALIZE CHAT WITH OTHER CLIENTS								*
	**************************************************************************************************/
	public void run()
	{
		System.out.println("new thread created!");
		while(clientPort == -1){}//waits for serverSocket to be initialized. Once it's initialized, clientPort will have a value
		try
		{
			myClientObject = new ClientObject(name, InetAddress.getLocalHost().getHostAddress(), clientPort);			
			heart.writeObject("reg");	
			heart.flush();
			System.out.println("sent reg");			
			heart.writeObject(myClientObject);
			heart.flush();
			System.out.println("sent object");
		}
		catch(IOException e)
		{
			System.out.println("Server is not responding. Will attempt to reconnect");
			//reconnect here
		}
		/*
		catch(UnknownHostException e)
		{
			System.out.println("This might be a problem. We can't identify your IP Address...");
			System.exit(0);
		}*/
		try
		{
			String verification = (String) heartListener.readObject();//if receive "A" means good, if receive "U" means bad
			while(verification.equals("U"))
			{
				System.out.println("Registration failed because you have the same name as another user");
				Scanner console = new Scanner(System.in);
				System.out.println("Enter your username again!");
				myClientObject.setName(console.nextLine());
				Socket socket = new Socket(host, serverPort);
				heart = new ObjectOutputStream(socket.getOutputStream());
				heart.flush();
				heartListener = new ObjectInputStream(socket.getInputStream());
				heart.writeObject("reg");
				heart.flush();
				heart.writeObject(myClientObject);
				heart.flush();
				verification = (String)heartListener.readObject();
			}
			System.out.println("Verified!");
		}
		catch(IOException e)
		{
			System.out.println("Could not read from server...");
		}
		catch(ClassNotFoundException e)
		{
			//fatal error man
			System.out.println("Oh my god.");
		}
		displayCommands();
		//this.getAndDisplay();
		String message;//the message string we're going to be dealing with mainly
		Scanner console = new Scanner(System.in);
		try
		{
			while(true)
			{
				message = console.nextLine();				
				if(inChat)
				{
					//I'm surprised there's no structure for: action -> boolean -> action -> repeat based on boolean
					printer.println(message);
					while(!(message = console.nextLine()).equals("\\q"))
					{
						//System.out.print("You:");
						printer.println(message);
					}
					System.out.println("You have exited chat. Type in \'chatlist\' to see who else is online.");
					inChat = false;
					continue;
				}
				System.out.println("Your command was:" + message);
				int size = message.length();
				if(message.substring(0,(size<2?size:2)).equals("hi"))//ternary operators to prevent index out of bounds
				{
					try
					{
						ClientObject personYourChattingWith = listOfUsers.get(message.substring(3,message.length()));
						currentChatSocket = new Socket(personYourChattingWith.getIpAddress(),personYourChattingWith.getPort());
						System.out.println("Chatting with " + message.substring(2,message.length()) + "\nType in \\q to quit");					
					}
					catch(NullPointerException e)
					{
						System.out.println("This user is not online. Check your spelling!");
						continue;
					}
					inChat = true;
					printer = new PrintWriter(currentChatSocket.getOutputStream(),true);
					reader = new BufferedReader(new InputStreamReader(currentChatSocket.getInputStream()));
					printer.println(name);
				}
				else if(message.substring(0,size<8?size:8).equals("chatlist"))
					getAndDisplay();
				else if(message.substring(0,size<1?size:1).equals("?") ||message.substring(0,size<4?size:4).equals("help") )
					displayCommands();
				else
					System.out.println("Command not recognized. Type in \'?\' or \'help\' for a list of available commands.");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Sorry. I'm terrible at catching exceptions. :(");
		}
	}
	/**************************************************************************************************
	*								GRABBING CHAT FROM OTHER CLIENTS								*
	**************************************************************************************************/
	public ChatClient()
	{
		try
		{
			new Thread(new ChatServer()).start();//for waiting for other clients to connect and receiving messages					
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}

	public class ChatServer implements Runnable//ChatClient creates a ChatServer, which means two threads are started in main
	{
		public ChatServer() throws IOException
		{
			serverSocket = new ServerSocket(0);//initializes serverSocket
			serverSocket.setReuseAddress(true);
			serverSocket.setSoTimeout(100);//sets a timeout for serverSocket.accept() so when WE initialize contact, we can continue on this thread
			clientPort = serverSocket.getLocalPort();//clientPort is set up
		}
		public void run()
		{
			String user = "";//user will be the name displayed when chatting e.g. Charlie: hi
			try
			{
				while(true)
				{
					try
					{
						while(!inChat)//while you did not initialize chat, wait for a request
						{
							Socket socket = serverSocket.accept();
							currentChatSocket = socket;//makes the socket universal so reader/printer can do it write
							inChat = true;//you're now in chat, no longer focused on waiting for requests, only listening to messages
							System.out.println("You have received a chat message!");
							reader = new BufferedReader(new InputStreamReader(currentChatSocket.getInputStream()));
							printer = new PrintWriter(currentChatSocket.getOutputStream(),true);
							printer.println(name);//sends your name
							user = reader.readLine(); //read for name
						}
					}
					catch(InterruptedIOException e)
					{
						continue;
					}
					if(user.equals(""))//for when you initialize chat, the first message you'll receive is name (as seen above)
						user = reader.readLine();
					System.out.println(user + ":" + reader.readLine());//continue listening for messages
				}			
			}
			catch(IOException e)
			{
				System.out.println(user + " has disconnected with you.");											
			}
		}
	}
}
