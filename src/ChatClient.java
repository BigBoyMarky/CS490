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

public class ChatClient implements Runnable
{

	//upon starting up, the ChatClient grabs the persons IP address and then requests for the name and port number
	//
	//the client attempts to initiate
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
	static PrintWriter printer;//printer
	static BufferedReader reader;//reader
	static Socket currentChatSocket;//the current Socket you're chatting in right now
	static boolean inChat;//once someone gets a message, they are forced in chat
	static Hashtable<String,Integer> listOfUsers = new Hashtable<String,Integer>();
	/**************************************************************************************************
	*											MAIN METHOD											*
	**************************************************************************************************/
	public static void main(String[] args) throws UnknownHostException
	{
		/**************************************************************************************************
		*											MAIN METHOD											*
		**************************************************************************************************/
		System.out.print("Port of the server to connect to:");
		Scanner console = new Scanner(System.in);
		serverPort = console.nextInt();
		System.out.print("Your username:");
		name = console.next();
		ip = InetAddress.getLocalHost().getHostAddress();//gets local IP address
		try
		{
			Socket socket = new Socket(HOST, serverPort);//connects to server	
			printer = new PrintWriter(socket.getOutputStream(), true);//allows sending messages
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));//allows reading messages				
			new Thread(new ChatClient()).start();//for connecting with other clients and sending messages
			heartbeat();//heartbeat
/*
			Will implement this after finishing server. This = initiating a chat session with another client
			while(true)
			{
				printer.println(console.nextLine());
			}
			ServerSocket serverSocket = new ServerSocket();
*/		
		}
		catch(Exception e)//need to better errors
		{
			e.printStackTrace();
		}

	}
	/**************************************************************************************************
	*											HEARTBEAT											*
	**************************************************************************************************/
	public static void heartbeat()
	{
		try
		{
			while(true)
			{
				Thread.sleep(heartbeat_rate);//sleeps for heartrate
				printer.println("<3");//sends message, isn't it adorable
			}
		}
		catch(InterruptedException e)
		{
			System.err.println("Connection has been interrupted. Our heartbeat has stopped.");
		}
	}
	/**************************************************************************************************
	*										GET AND DISPLAY											*
	**************************************************************************************************/	
	public static void getAndDisplay()
	{
		String user;
		printer.println("get");
		try{
			while(!(user = reader.readLine()).equals("\\0"))//basically deserializes server's information
			{
				//prints out user
				System.out.println(user);
				//adds user to hashtable for connection purposes
				int disjoint = user.indexOf(" ");
				listOfUsers.put(user.substring(disjoint,user.length()),Integer.parseInt(user.substring(0,disjoint)));//puts them into a hashtable
				//why hashtable on client? Because client is the one directly connecting to other clients >.>
			}
			System.out.println("done with get");
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
		System.out.println("Chatclient started");		
		printer.println("0"+name);//sends name
		printer.println("1"+ip);//sends ip
		while(clientPort == -1)
		{

		}
		printer.println("2"+clientPort);//sends port
		displayCommands();
		getAndDisplay();
		String command;
		Scanner console = new Scanner(System.in);
		try
		{
			while(true)
			{
				command = console.nextLine();
				System.out.println("Your command was:" + command);
				int size = command.length();
				if(command.substring(0,(size<2?size:2)).equals("hi") || inChat)
				{
					//parse for user name
					if(!inChat)
					{
						try
						{
							currentChatSocket = new Socket(HOST, listOfUsers.get(command.substring(2,command.length())));//listofusers.get returns an integer that is the port of the user
							inChat = true;
						}
						catch(NullPointerException e)
						{
							System.out.println("This user is not online. Check your spelling!");
							continue;
						}
						System.out.println("Chatting with " + command.substring(2,command.length()) + "\nType in \\q to quit");
					}
					String message;
					PrintWriter chatPrinter = new PrintWriter(currentChatSocket.getOutputStream(),true);
					if(!inChat)
						chatPrinter.println(name);
					while(!(message = console.nextLine()).equals("\\q"))
					{
						chatPrinter.println(message);
						//System.out.println(reader.readLine());
					}
					System.out.println("You have exited chat. Type in \'chatlist\' to see who else is online.");
				}
				else if(command.substring(0,size<8?size:8).equals("chatlist"))
					getAndDisplay();
				else if(command.substring(0,size<1?size:1).equals("?") ||command.substring(0,size<4?size:4).equals("help") )
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
	public class ChatServer implements Runnable
	{
		public ChatServer() throws IOException
		{
			serverSocket = new ServerSocket(0);
			serverSocket.setReuseAddress(true);
			serverSocket.setSoTimeout(1900);
			clientPort = serverSocket.getLocalPort();
		}
		public void run()
		{
			while(true)
			{
				System.out.println("Chatserver started");
				String name = "";
				try
				{
					Socket socket = serverSocket.accept();
					currentChatSocket = socket;
					inChat = true;
					System.out.println("You have received a chat message!");
	                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	                name = reader.readLine();
	                while(true)
	                	System.out.println(name +":" + reader.readLine());
				}
				catch(InterruptedIOException e)
				{
					System.out.println("No one chatted you :(");
					if(inChat)
					{
						try
						{
							System.out.println("You have received a chat message!");
			                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			                name = reader.readLine();
			                while(true)
			                	System.out.println(name +":" + reader.readLine());
						}
						catch(IOException f)
						{
							System.out.println(name + " has disconnected with you.");							
						}
					}

				}
				catch(IOException e)
				{
					System.out.println(name + " has disconnected with you.");
				}		
			}
		}
	}
}
