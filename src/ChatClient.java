import java.util.Scanner;
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
	1) When it connects with the server, it will send 4 Strings: 
		1) its name, which is the username of the chat client (marked with a "0" in front, e.g. "0Charlie")
		2) its local IPv4 address (marked with a "1" in front, e.g. "1127.0.0.0")
		3) its ServerSocket port for connecting with other clients directly (marked with a "2" in front, e.g. "24800")
		4) and "get". It will expect the server to send a list of users with the following format [clientPort clientName]. 
			(e.g. "4800 Charlie"; "64500 Mark"; "1248 Saranyu";) It will keep waiting until it sees "\\0", so be sure to println
			that after your for loop is done
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
	static long heartbeat_rate = 4000;
	static String HOST = "localhost";
	static int serverPort;//port of the server it's going to connect to
	static int clientPort;//port of the client's ServerSocket for chatting with other clients
	static String name;//name of the Client
	static String ip;//ip of the client
	static Socket socket;//socket for connecting purposes
	static ServerSocket serverSocket;//for connecting to other users directly???
	static PrintWriter heart;//printer to server
	static BufferedReader heartListener;//reader to server
	static PrintWriter printer;//printer to client
	static BufferedReader reader;//reader to client
	static Socket currentChatSocket;//the current Socket you're chatting in right now
	static boolean inChat;//once someone gets a message, they are forced in chat
	static Hashtable<String,Integer> listOfUsers = new Hashtable<String,Integer>();//hashtable of users for connecting to others
	/**************************************************************************************************
	*											MAIN METHOD											*
	**************************************************************************************************/
	public static void main(String[] args) throws UnknownHostException
	{
		/**********************************************************************************************
		*											INITIALIZATION									*
		**********************************************************************************************/
		System.out.print("Port of the server to connect to:");
		Scanner console = new Scanner(System.in);
		serverPort = console.nextInt();
		System.out.print("Your username:");
		name = console.next();
		ip = InetAddress.getLocalHost().getHostAddress();//gets local IP address
		try
		{
			Socket socket = new Socket(HOST, serverPort);//connects to the main server	
			heart = new PrintWriter(socket.getOutputStream(),true);
			heartListener = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			heart.println("0"+name);//sends name
			heart.println("1"+ip);//sends ip			
			String verification = heartListener.readLine();//waits for verification
			while(verification.equals("U"))//if name already in use, tell user to change name
			{
				System.out.println("Please select another username. The following are already in use.");
				getAndDisplay();				
				name = console.next();
				verification = heartListener.readLine();
			}
			new Thread(new ChatClient()).start();//for connecting with other clients and sending messages
			/******************************************************************************************
			*											HEARTBEAT									*
			*******************************************************************************************/			
			while(true)
			{
				Thread.sleep(heartbeat_rate);//sleeps for heartrate
				heart.println("<3");//sends message, isn't it adorable
			}		
		}
		catch(InterruptedException e)
		{
			System.err.println("Connection has been interrupted. Our heartbeat has stopped.");
		}
		catch(IOException e)	
		{
			System.err.println("Connection has been interrupted. Our heartbeat has stopped.");			
		}
	}

/*			printer = new PrintWriter(socket.getOutputStream(), true);//allows sending messages
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));//allows reading messages*/
	/**************************************************************************************************
	*										GET AND DISPLAY											*
	**************************************************************************************************/	
	public static void getAndDisplay()
	{
		String user;
		heart.println("get");
		try
		{
			System.out.println("Current people online:");
			while(!(user = heartListener.readLine()).equals("\\0"))//\\0 is used to mark end of list
			{
				int disjoint = user.indexOf(" ");////basically deserializes server's information.
				listOfUsers.put(user.substring(disjoint,user.length()),Integer.parseInt(user.substring(0,disjoint)));//adds user to hashtable for connection purposes
				//why hashtable on client? Because client is the one directly connecting to other clients >.>
				System.out.print(user.substring(disjoint,user.length()));//prints out user
			}
			System.out.println("\n================================================================================");
		}
		catch(IOException e)
		{
			e.printStackTrace();
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
		while(clientPort == -1){}//waits for serverSocket to be initialized. Once it's initialized, clientPort will have a value
		heart.println("2"+clientPort);//sends port		
		displayCommands();
		getAndDisplay();
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
						currentChatSocket = new Socket(HOST, listOfUsers.get(message.substring(2,message.length())));//listofusers.get returns an integer that is the port of the user
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