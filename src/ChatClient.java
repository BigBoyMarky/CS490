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

public class ChatClient implements Runnable
{

	//upon starting up, the ChatClient grabs the persons IP address and then requests for the name and port number
	//
	//the client attempts to initiate
	/**************************************************************************************************
	*											FIELDS												*
	**************************************************************************************************/
	static int heartbeat_rate = 4000;
	static String HOST = "localhost";
	static int serverPort;//port of the server it's going to connect to
	static int clientPort = 0;//port of the client's ServerSocket for chatting with other clients
	static String name;//name of the Client
	static String ip;//ip of the client
	static Socket socket;
	static PrintWriter printer;
	static BufferedReader reader;
	static Hashtable<String,Integer> listOfUsers = new Hashtable<String,Integer>();
	/**************************************************************************************************
	*											MAIN METHOD											*
	**************************************************************************************************/
	public static void main(String[] args) throws UnknownHostException
	{
		System.out.print("Port of the server to connect to:");
		Scanner console = new Scanner(System.in);
		serverPort = console.nextInt();
		System.out.print("Your username:");
		name = console.next();
		ip = InetAddress.getLocalHost().getHostAddress();
		try
		{
			Socket socket = new Socket(HOST, serverPort);
			printer = new PrintWriter(socket.getOutputStream(), true);
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			printer.println("0"+name);
			printer.println("1"+ip);
			printer.println("2"+clientPort);
			new Thread(new ChatClient()).start();//for connecting with other chat
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
		catch(Exception e)
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
				Thread.sleep(heartbeat_rate);
				printer.println("3");
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
			while(!(user = reader.readLine()).equals("\0"))
			{
				//prints out user
				System.out.println(user);
				//adds user to hashtable for connection purposes
				int disjoint = user.indexOf(" ");
				listOfUsers.put(user.substring(disjoint,user.length()-1),Integer.parseInt(user.substring(0,disjoint)));
			}
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
		displayCommands();
		String command;
		Scanner console = new Scanner(System.in);
		try
		{
			while(true)
			{
				command = console.nextLine();
				if(command.substring(0,2).equals("hi"))
				{
					//parse for user name
					Socket socket = new Socket(HOST, listOfUsers.get(command.substring(2,command.length()-1)));//listOfUsers.get returns an integer that is the port of the user
					System.out.println("Chatting with " + command.substring(2,command.length()-1) + "\nType in \\q to quit");
					String message;
					while(!(message = console.nextLine()).equals("\\q"))
					{
						printer.println(message);
						System.out.println(reader.readLine());
					}
					System.out.println("You have exited chat. Type in \'chatlist\' to see who else is online.");
				}
				else if(command.substring(0,7).equals("chatlist"))
					getAndDisplay();
				else if(command.substring(0,4).equals("help") || command.substring(0,1).equals("?"))
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
}
