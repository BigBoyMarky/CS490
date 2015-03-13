/*
1] ADD EXECUTOR THINGY THREADPOOL(LIMIT = WHATEVER)
2] MOVE STUFF AROUND AND MAKE IT WORK
3] UPDATE PROTOCOLS BASED ON SINGLETHREADED CHAT SERVER


Executor - receives message from any of the sockets, passes message to one of the ThreadPools for processing, resulting in up to n messages being processed

MultiThreadedChatServer = 
	1] Initialize server
	2] Create Threadpool
	3] 2 MAIN threads - serverSocket.accept() and ois.readObject()?
	4] Once ios.readObject() returns true, pass the Socket and OIS to method that handles the rest of the grunt work

which means we must minimize the steps in ois.hasMessage()

ExecutorService executor = Executors.newFixedThreadPool(10);
			Executors.newFixedThreadPool(4);
			while(true)
			{
				Socket socket = serverSocket.accept();
				socketList.add(socket);
				readerList.add(new BufferedReader(new InputStreamReader(socket.getInputStream())));
				++numClients;
				executor.execute(new MultiThreadedChatServer());

while()
{
	
}

public class Runner() implements Runnable
{
	int id;
	//or
	String id;
	public Runner(id)
	{
		this.id = id;

	}
	public void run()
	{
		//goes through a single iteration checking for messages
		//what about checking heartbeats?
		//have it scheduled so it'll automatically run every 5 seconds?
	}
}
*/


//Create a server based on command line input, DONE
//server waits for clients to connect, DONE
//once the clients connect, it sends acknowledgement back, adds clinet to a list.
//it then sends an update to all previously connectd clients about the new user
//it regularly checks for heartbeats. If heartbeat not received, it will assume it's dead and remove it from the list.
//the client can initiate a chat with another client

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.*;
import java.net.*;
import java.io.*;

public class MultiThreadedChatServer
{
	/**************************************************************************************************
	*											FIELDS												*
	**************************************************************************************************/	
	private int THREADPOOL_SIZE = 4;
	private int SOCKET_TIMEOUT = 1;//in milliseconds
	private long heartbeat_rate = 6000;//in milliseconds
	private static MultiThreadedChatServer server;	
	private ServerSocket serverSocket;
	private int port;//port
	private int numClients = -1;//keeps track of number of clients for ID'ing purposes
	volatile private ArrayList<String> keyList = new ArrayList<String>();	
	volatile private ConcurrentHashMap<String,ClientObject> clientMap = new ConcurrentHashMap<String,ClientObject>();//hashmap used so we can check if there is a duplicate name easily
	private long currentTime;
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
	public MultiThreadedChatServer(int port) throws IOException
	{
		this.port = port;
		serverSocket = new ServerSocket(port);
		serverSocket.setReuseAddress(true);
	}
	/**************************************************************************************************
	*								SEPARATE THREAD TO MANAGE CLIENTS								*
	**************************************************************************************************/	
	public class Runner implements Runnable
	{
		private String message;
		private ClientObject client;
		public Runner(ClientObject client, String message){
			this.client = client;
			this.message = message;
		}
		public void run()
		{
			try
			{
				System.out.printf("%s has sent the following message:%s\n",client.getName(),message);
				if(message.equals("get"))
				{
					try
					{
						client.getOut().writeObject(clientMap);
						client.getOut().reset();//apparently Object stream keeps cache
						client.getOut().flush();
					}
					catch(IOException e)//exception thrown when reading/writing/closing
					{
						System.out.println("Unable to send clientMap...");
					}							
					client.updateHeart(System.currentTimeMillis());//in case heartbeat sent same time
					//System.out.printf("Get took %d milliseconds.\n",(System.currentTimeMillis()-currentTime));
				}
				else if(message.equals("<3"))
				{
					client.updateHeart(System.currentTimeMillis());
				}
				else
				{
					System.out.printf("Invalid message. Request from %s will be ignored.",client);
				}
				if(System.currentTimeMillis()-client.getHeart() > heartbeat_rate)
				{
					System.out.printf("Because of lack of heartbeat, user %s has been terminated. (IN WORKER THREAD)\n",client);
					clientMap.remove(client.getName());
					keyList.remove(client);
					numClients = keyList.size();
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	/**************************************************************************************************
	*								MAIN THREAD FOR CONNECTING NEW CLIENTS							*
	**************************************************************************************************/	
	public void runServer()
	{
		Socket socket;
		ObjectInputStream reader;
		ObjectOutputStream writer;
		ClientObject newClient;
		long timeElapsed = 0;
		//1 thread for accepting new clients
		//1 thread for peeping at the inputstream
		//n threads for managing messages
		ExecutorService executor = Executors.newFixedThreadPool(THREADPOOL_SIZE);
		new Thread(new Runnable()//for managing the executor threads
		{
			public void run()
			{
				String message;
				//checks if each
				//iterates through key
				while(true)
				{
					int numClients = keyList.size();
					for(int i = 0; i < numClients; i++) //since has to update size anyways, right?
					{
						ClientObject currentClient = clientMap.get(keyList.get(i));
						try
						{
							message = (String) currentClient.getIn().readObject();
							executor.execute(new Runner(currentClient, message));
						}
						catch(SocketTimeoutException e)
						{
							if ((System.currentTimeMillis() - currentClient.getHeart()) > heartbeat_rate)
							{
								System.out.printf("%sBecause of lack of heartbeat, user %s has been terminated. (IN MANAGER THREAD)\n",new Date(), currentClient.getName());
								clientMap.remove(currentClient.getName());
								keyList.remove(i);
								numClients = keyList.size();
								--i;
							}
						}
						catch(Exception e)
						{
							e.printStackTrace();
							System.out.println("Unable to read input stream");
							clientMap.remove(currentClient.getName());
							keyList.remove(i);
							numClients = keyList.size();
							--i;

						}
					}

				}
				}
		}).start();

		while(true)
		{
			try
			{
				socket =serverSocket.accept();//no timeout jk
				System.out.println("A new client has connected!");
				reader = new ObjectInputStream(socket.getInputStream());//so it reads from buffer
				writer = new ObjectOutputStream(socket.getOutputStream());
				writer.flush();
				System.out.printf("Ready? %s\n",reader.available());
				//will need to separate this later
				String regKey = (String) reader.readObject();				
				if(regKey.equals("reg"))//registration
				{
					System.out.println("Client has sent a valid registration key.");
					ClientObject copyOf = (ClientObject)reader.readObject();
					socket.setSoTimeout(SOCKET_TIMEOUT);//1ms					
					newClient = new ClientObject(copyOf, socket, reader, writer);
					String clientName = newClient.getName();
					System.out.printf("Client name is:%s\n",clientName);
					if(clientMap.containsKey(clientName))
					{
						writer.writeObject("U");//invalid
						writer.flush();
						socket.close();
						System.out.println("Client's name was a duplicate. Client has been terminated.");
					}
					else
					{
						writer.writeObject("A");//valid
						writer.flush();
						clientMap.put(clientName,newClient);
						keyList.add(clientName);
						System.out.printf(keyList.get(0));
						newClient.updateHeart(System.currentTimeMillis());//for reg purposes
						System.out.printf("%s has successfully registered.\n",clientName);
					}
				}
				else
				{
					System.out.println("Client has sent an invalid registration key. We will not process the client's requests");
					continue;
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}