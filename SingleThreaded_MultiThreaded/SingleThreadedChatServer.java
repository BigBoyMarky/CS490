import java.util.*;
import java.net.*;
import java.io.*;

public class SingleThreadedChatServer
{
	/**************************************************************************************************
	*											FIELDS												*
	**************************************************************************************************/	
	private int SOCKET_TIMEOUT = 100;//in milliseconds
	private long heartbeat_rate = 5000;//in milliseconds
	private static SingleThreadedChatServer server;	
	private ServerSocket serverSocket;
	private int port;//port
	private int numClients = -1;//keeps track of number of clients for ID'ing purposes
	private ArrayList<Socket> socketList = new ArrayList<Socket>();//socket lists for accessing them later, e.g. when a client requests the list of group G
	private ArrayList<ObjectInputStream> readerList = new ArrayList<ObjectInputStream>();//for receiving messages. There is no ObjectOutputStream list because the Client will only send 1 object once
	private ArrayList<String> keyList = new ArrayList<String>();	
	private HashMap<String,ClientObject> clientMap = new HashMap<String,ClientObject>();//hashmap used so we can check if there is a duplicate name easily
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
		serverSocket.setSoTimeout(SOCKET_TIMEOUT);
	}
	/**************************************************************************************************
	*										THE ONLY THREAD 										*
	**************************************************************************************************/		
	public void runServer()
	{
		Socket socket;
		String message;
		ObjectOutputStream writer;
		ClientObject newClient;
		while(true)
		{
			try
			{
				socket = serverSocket.accept();//has timeout
				socket.setSoTimeout(SOCKET_TIMEOUT);//for socket.getInputStream() reading purposes
				socketList.add(socket);
				readerList.add(new ObjectInputStream(socket.getInputStream()));
				++numClients;

				for(int i = 0; i < numClients; i++)
				{
					message = (String) readerList.get(i).readObject();//reads a String Object
					if(message.equals("reg"))//registration
					{
						newClient = new ClientObject((ClientObject)readerList.get(i).readObject());
						String clientName = newClient.getName();
						if(clientMap.containsKey(clientName))
						{
							writer = new ObjectOutputStream(socketList.get(i).getOutputStream());
							writer.writeObject("U");
						}
						else
						{
							clientMap.put(clientName,newClient);
							keyList.add(clientName);
						}
					}
					if(message.equals("get"))
					{
						writer = new ObjectOutputStream(socketList.get(i).getOutputStream());
						writer.writeObject(clientMap);
					}
					if(message.equals("<3"))
					{
						clientMap.get(i).updateHeart(System.currentTimeMillis());
					}
					if(System.currentTimeMillis()-clientMap.get(i).getHeart() > heartbeat_rate)
					{
						System.out.println("User "+ i + " has been terminated");
						clientMap.remove(keyList.get(i));
						keyList.remove(i);
						socketList.get(i).close();
						socketList.remove(i);
					}
				}				
			}
			catch(SocketTimeoutException e)//exception thrown when timeout, no worries
			{
				continue;//making the server truly single threaded
			}
			catch(IOException e)//exception thrown when a Socket is disconnected
			{
				for(int i = 0; i < numClients; i++)
				{
					if(System.currentTimeMillis()-clientMap.get(i).getHeart() > heartbeat_rate)
					{
						System.out.println("User "+ i + " has been terminated");
						clientMap.remove(keyList.get(i));
						keyList.remove(i);
						//socketList.get(i).close();
						socketList.remove(i);
					}					
				}
				continue;//heartbeat rate will automatically remove the clients that have dead
			}
			catch(ClassNotFoundException e)
			{
				for(int i = 0; i < numClients; i++)
				{
					if(System.currentTimeMillis()-clientMap.get(i).getHeart() > heartbeat_rate)
					{
						System.out.println("User "+ i + " has been terminated");
						clientMap.remove(keyList.get(i));
						keyList.remove(i);
						//socketList.get(i).close();
						socketList.remove(i);
					}					
				}				
				continue;//meaning sent out of order, or sending something invalid
			}
		}
	}
}