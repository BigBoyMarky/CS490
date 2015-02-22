import java.util.*;
import java.net.*;
import java.io.*;

//Create a server based on command line input, DONE
//server waits for clients to connect, DONE
//once the clients connect, it sends acknowledgement back, adds clinet to a list.
//it then sends an update to all previously connectd clients about the new user
//it regularly checks for heartbeats. If heartbeat not received, it will assume it's dead and remove it from the list.
//the client can initiate a chat with another client

public class SingleThreadedChatServer implements Runnable
{
	/**************************************************************************************************
	*											FIELDS												*
	**************************************************************************************************/	
	private long heartbeat_rate = 4000;//in milliseconds
	static ServerSocket serverSocket;
	static SingleThreadedChatServer server;
	private int port;//port
	static private int numClients = -1;//keeps track of number of clients for ID'ing purposes
	static ArrayList<Long> heartList = new ArrayList<Long>();//for checking the time passed for heartbeats
	static ArrayList<String> nameList = new ArrayList<String>();//for names
	static ArrayList<String> ipList = new ArrayList<String>();//for ip
	static ArrayList<Integer> portList = new ArrayList<Integer>();//for port	
	static ArrayList<Socket> socketList = new ArrayList<Socket>();//socket lists for accessing them later, e.g. when a client requests the list of group G
	static ArrayList<BufferedReader> readerList = new ArrayList<BufferedReader>();
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
		}
		System.out.println("Server Host Name:" + InetAddress.getLocalHost().getHostAddress());
		server.runServer();
	}
	/**************************************************************************************************
	*											CONSTRUCTOR												*
	**************************************************************************************************/
	public SingleThreadedChatServer()
	{
	}
	public SingleThreadedChatServer(int port) throws IOException
	{
		this.port = port;
		serverSocket = new ServerSocket(port);
		serverSocket.setReuseAddress(true);
	}
	/**************************************************************************************************
	*								SEPARATE THREAD TO CONNECT NEW CLIENTS							*
	**************************************************************************************************/	
	public void run()
	{
		try
		{
			while(true)
			{
				Socket socket = serverSocket.accept();
				socketList.add(socket);
				readerList.add(new BufferedReader(new InputStreamReader(socket.getInputStream())));
				++numClients;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	/**************************************************************************************************
	*										RECEIVE/PROCESS MESSAGES								*
	**************************************************************************************************/	
	public void runServer()
	{
		try
		{
			PrintWriter printer;
			BufferedReader reader;
			String message = "";
			new Thread(new SingleThreadedChatServer()).start();//for receiving connections
			
			while(true)
			{
				System.out.println(server.serverSocket.getLocalPort());
				for(int i = 0; i <= numClients; i++)//go through all sockets
				{
					message = readerList.get(i).readLine();
					System.out.println("User #" + i + ":" + message);
					if(message.substring(0,1).equals("R"))//for registration
					{
						printer = new PrintWriter(socketList.get(i).getOutputStream(),true);
						String name = message.substring(1,14).trim();//whatever
						if(nameList.contains(name))
						{							
							printer.println("U");
							socketList.remove(i);
							--numClients;
							--i;
							continue;
						}
						int spacePosition = message.indexOf(" ",14);
						String address = message.substring(14,spacePosition);
						int clientPort = Integer.parseInt(message.substring(spacePosition+1,message.length()));
						printer.println("A");//accepted
						//Adding data to respective lists
						heartList.add(System.currentTimeMillis());
						nameList.add(name);
						ipList.add(address);
						portList.add(clientPort);
					}
					if(message.equals("get"))
					{
						System.out.println("Received a get request from user " + i);
						printer = new PrintWriter(socketList.get(i).getOutputStream(),true);
						for(int j = 0; j <= numClients; j++)
						{
							int nameLength = nameList.get(j).length();
							if(nameLength < 10)
								printer.println("0"+nameLength+nameList.get(j) + "," + ipList.get(j) + "," + portList.get(j));
							else
								printer.println(nameLength+nameList.get(j) + "," + ipList.get(j) + "," + portList.get(j));								
						}
						printer.println("\\0");
						System.out.println("sent lust");
					}
					if(message.equals("<3"))
					{
						//update heartbeat time
						heartList.set(i, System.currentTimeMillis());
					}
					if(System.currentTimeMillis()-heartList.get(i) > heartbeat_rate)
					{
						//NOTE: if server lags, then this solution will just kill everyone
						//terminate
						//send message
						printer = new PrintWriter(socketList.get(i).getOutputStream(),true);
						System.out.println("User "+ i + " has been terminated");
						printer.println("You have been terminated! Enjoy the rest of your lonely existence");
						socketList.remove(i);
						--numClients;
						--i;
					}
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Will be more specific about exceptions later");
		}
	}
}