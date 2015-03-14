package edu.purdue.cs490;

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
				//System.out.printf("%s says:%s\n",client.getName(),message);
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
						clientMap.remove(client.getName());
						keyList.remove(client);
						numClients = keyList.size();
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
				}System.out.println("before kicking clients if statement");
				if(System.currentTimeMillis()-client.getHeart() > heartbeat_rate)
				{System.out.println("5576423578243654783567821345782134");
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
								System.out.printf("Because of lack of heartbeat, user %s has been terminated. (IN MANAGER THREAD)\n", currentClient.getName());
								clientMap.remove(currentClient.getName());
								keyList.remove(i);
								numClients = keyList.size();
								--i;
							}
						}
						catch(EOFException e) {
							if ((System.currentTimeMillis() - currentClient.getHeart()) > heartbeat_rate)
							{
								System.out.printf("Because of lack of heartbeat, user %s has been terminated. (IN MANAGER THREAD)\n", currentClient.getName());
								clientMap.remove(currentClient.getName());
								keyList.remove(i);
								numClients = keyList.size();
								--i;
							}
						}
						catch(NullPointerException e) {
							System.out.println("Null Pointer");
							e.printStackTrace();
							System.exit(0);
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

		//int numFiles = 0;
		while(true)
		{
			try
			{
				socket = serverSocket.accept();//no timeout jk
				//numFiles++;
				//System.out.println("A new client has connected!");
				reader = new ObjectInputStream(socket.getInputStream());//so it reads from buffer
				writer = new ObjectOutputStream(socket.getOutputStream());
				writer.flush();
				//System.out.printf("Ready? %s\n",reader.available());
				//will need to separate this later
				String regKey = (String) reader.readObject();
				if(regKey.equals("reg"))//registration
				{
					//System.out.println("Client has sent a valid registration key.");
					ClientObject copyOf = (ClientObject)reader.readObject();
					socket.setSoTimeout(SOCKET_TIMEOUT);//1ms
					newClient = new ClientObject(copyOf, socket, reader, writer);
					String clientName = newClient.getName();
					//System.out.printf("Client name is:%s\n",clientName);
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
						//System.out.printf(keyList.get(0));
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
				//System.out.printf("Num sockets opened:%d",numFiles);
				e.printStackTrace();
				break;
			}
		}
	}
}
