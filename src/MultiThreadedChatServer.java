/*
1] ADD EXECUTOR THINGY THREADPOOL(LIMIT = WHATEVER)
2] MOVE STUFF AROUND AND MAKE IT WORK
3] UPDATE PROTOCOLS BASED ON SINGLETHREADED CHAT SERVER
*/
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.*;
import java.net.*;
import java.io.*;


//Create a server based on command line input, DONE
//server waits for clients to connect, DONE
//once the clients connect, it sends acknowledgement back, adds clinet to a list.
//it then sends an update to all previously connectd clients about the new user
//it regularly checks for heartbeats. If heartbeat not received, it will assume it's dead and remove it from the list.
//the client can initiate a chat with another client

public class MultiThreadedChatServer implements Runnable
{
	/**************************************************************************************************
	*											FIELDS												*
	**************************************************************************************************/	
	private long heartbeat_rate = 4000;//in milliseconds
	static ServerSocket serverSocket;
	static MultiThreadedChatServer server;
	private int port;//port
	static private int numClients = -1;//keeps track of number of clients for ID'ing purposes
	private int id;
	static ArrayList<Long> heartList = new ArrayList<Long>();//for checking the time passed for heartbeats
	static ArrayList<String> nameList = new ArrayList<String>();//for names
	static ArrayList<String> ipList = new ArrayList<String>();//for ip
	static ArrayList<Integer> portList = new ArrayList<Integer>();//for port	
	static ArrayList<Socket> socketList = new ArrayList<Socket>();//socket lists for accessing them later, e.g. when a client requests the list of group G
	static ArrayList<BufferedReader> readerList = new ArrayList<BufferedReader>();
	static ArrayList<ClientObject> clientList = new ArrayList<ClientObject>();
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
					server = new MultiThreadedChatServer(port);
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
			server = new MultiThreadedChatServer(0);//if none was specified, uses 0, which locates default			
			System.out.println("Port was not specified. Using free port " + server.serverSocket.getLocalPort());
		}
		System.out.println("Server Host Name:" + InetAddress.getLocalHost().getHostAddress());
		server.runServer();
	}
	/**************************************************************************************************
	*											CONSTRUCTOR												*
	**************************************************************************************************/
	public MultiThreadedChatServer() {
		this.id = numClients;
	}
	public MultiThreadedChatServer(int port) throws IOException
	{
		this.port = port;
		serverSocket = new ServerSocket(port);
		serverSocket.setReuseAddress(true);
	}
	/**************************************************************************************************
	*								SEPARATE THREAD TO MANAGE CLIENTS								*
	**************************************************************************************************/	
	public void run()
	{
		//register first
		//then continually look for heartbeats/messages
		try
		{
			String message = readerList.get(id).readLine();//will block
			PrintWriter printer = new PrintWriter(socketList.get(id).getOutputStream(),true);
			System.out.println("User #" + id + ":" + message);
			if(message.substring(0,1).equals("R"))//for registration
			{
				String name = message.substring(1,14).trim();//whatever
				System.out.println(name);
				if(nameList.contains(name))
				{							
					printer.println("U");
					socketList.remove(id);
					for(int j = 0; j <= numClients; j++)
					{
						int nameLength = nameList.get(j).length();
						if(nameLength < 10)
							printer.println("0"+nameLength+nameList.get(j) + "," + ipList.get(j) + "," + portList.get(j));
						else
							printer.println(nameLength+nameList.get(j) + "," + ipList.get(j) + "," + portList.get(j));
					}
					printer.println("\\0");							
					--numClients;
					return;//end thread
				}
				int spacePosition = message.indexOf(" ",14);
				String address = message.substring(14,spacePosition);
				System.out.println(address);
				int clientPort = Integer.parseInt(message.substring(spacePosition+1,message.length()));
				printer.println("A");//accepted
				//Adding data to respective lists
				heartList.add(System.currentTimeMillis());
				nameList.add(name);
				ipList.add(address);
				portList.add(clientPort);
			}
			while(true)
			{
				message = readerList.get(id).readLine();
				if(message.equals("get"))
				{
					System.out.println("Received a get request from user " + id);
					for(int j = 0; j <= numClients; j++)
					{
						int nameLength = nameList.get(j).length();
						if(nameLength < 10)
							printer.println("0"+nameLength+nameList.get(j) + "," + ipList.get(j) + "," + portList.get(j));
						else
							printer.println(nameLength+nameList.get(j) + "," + ipList.get(j) + "," + portList.get(j));
					}
					printer.println("\\0");
					System.out.println("List has been sent!");
				}
				if(message.equals("<3"))
				{
					//update heartbeat time
					heartList.set(id, System.currentTimeMillis());
				}
				if(System.currentTimeMillis()-heartList.get(id) > heartbeat_rate)
				{
					//NOTE: if server lags, then this solution will just kill everyone
					//terminate
					//send message
					System.out.println("User "+ id + " has been terminated");
					printer.println("E");			
					socketList.get(id).close();
					socketList.remove(id);
					heartList.remove(id);
					nameList.remove(id);
					ipList.remove(id);
					portList.remove(id);
					readerList.remove(id);
					--numClients;
					return;
				}
			}
		}
		catch(IOException e)
		{
			System.out.println("Client is dead!");
			socketList.remove(id);
			heartList.remove(id);
			nameList.remove(id);
			ipList.remove(id);
			portList.remove(id);
			readerList.remove(id);
			--numClients;			
		}
	}
	/**************************************************************************************************
	*								MAIN THREAD FOR CONNECTING NEW CLIENTS							*
	**************************************************************************************************/	
	public void runServer()
	{
		try
		{
			ExecutorService executor = Executors.newFixedThreadPool(10);
			Executors.newFixedThreadPool(4);
			while(true)
			{
				Socket socket = serverSocket.accept();
				socketList.add(socket);
				readerList.add(new BufferedReader(new InputStreamReader(socket.getInputStream())));
				++numClients;
				executor.execute(new MultiThreadedChatServer());
				//executor.execute(new MultiThreadedChatServer());
				//new Thread(new MultiThreadedChatServer()).start();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}			
	}
}