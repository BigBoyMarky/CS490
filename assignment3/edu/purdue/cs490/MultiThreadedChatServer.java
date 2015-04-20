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
	volatile private int numClients = -1;//keeps track of number of clients for ID'ing purposes
	volatile private ArrayList<String> keyList = new ArrayList<String>();
	volatile private ConcurrentHashMap<String,ClientObject> clientMap = new ConcurrentHashMap<String,ClientObject>();//hashmap used so we can check if there is a duplicate name easily
	private long currentTime;
	private long id = 0;
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
					System.out.printf("Port is out of range. Try a port between 1025 and 65535. This program will close.\n");
					return;
				}
			}
			catch(Exception e)
			{
					System.out.printf("Please enter valid input. We want integers only!\n");
			}
		}
		else
		{
			server = new MultiThreadedChatServer(0);//if none was specified, uses 0, which locates default
			System.out.printf("Port was not specified. Using free port %d\n",server.serverSocket.getLocalPort());
		}
		System.out.printf("Server Host Name:%s\n",InetAddress.getLocalHost().getHostAddress());
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
				if(message.equals("get"))
				{
					try
					{
						client.getOut().writeObject(clientMap);
						client.getOut().flush();
						client.getOut().reset();//apparently Object stream keeps cache, meaning if I send the same object reference (even if the object is now different), it'll send the previous version
					}
					catch(IOException e)//exception thrown when reading/writing/closing
					{
						System.out.printf("Unable to send clientMap...terminating user\n");
						clientMap.remove(client.getID());
						keyList.remove(client);
						numClients = keyList.size();
					}
					catch(NullPointerException e)
					{
						System.out.printf("Unable to send clientMap...terminating user\n");
						clientMap.remove(client.getID());
						keyList.remove(client);
						numClients = keyList.size();						
					}
					client.updateHeart(System.currentTimeMillis());//in case heartbeat sent same time
				}
				else if(message.equals("<3"))
				{
					client.updateHeart(System.currentTimeMillis());
				}
				else
				{
					System.out.printf("%s is an invalid message. Request from %s will be ignored.\n",message,client);
				}
				if(System.currentTimeMillis()-client.getHeart() > heartbeat_rate)
				{
					System.out.printf("Because of lack of heartbeat, user %s has been terminated. (IN WORKER THREAD)\n",client);
					clientMap.remove(client.getID());
					keyList.remove(client);
					numClients = keyList.size();
				}
			}
			/*
			catch(SocketException e)
			{
				System.err.println(e);
				System.out.printf("SocketException thrown, meaning most likely %s has disconnected.\n",client);

			}*/
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
		//n threads for managing messages, n being threadpool size of course! :P
		ExecutorService executor = Executors.newFixedThreadPool(THREADPOOL_SIZE);
		new Thread(new Runnable()//for managing the executor threads
		{
			public void run()
			{
				String message;
				while(true)
				{
					numClients = keyList.size();
					for(int i = 0; i < numClients; i++) //since has to update size anyways, right?
					{
						if(keyList.get(i) == null)
							break;//allows numClients to reset
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
								System.out.printf("Because of lack of heartbeat, user %s has been terminated. (IN MANAGER THREAD)\n", currentClient.getID());
								clientMap.remove(currentClient.getID());
								keyList.remove(i);
								numClients = keyList.size();
								--i;
							}
						}
						catch(SocketException e)
						{
							System.out.printf("%s's socket disconnected! S/he will be removed.\n",currentClient.getID());
							clientMap.remove(currentClient.getID());
							keyList.remove(i);
							numClients = keyList.size();
							--i;							
						}
						/*
						catch(EOFException e) {
							if ((System.currentTimeMillis() - currentClient.getHeart()) > heartbeat_rate)
							{
								System.out.printf("Because of lack of heartbeat, user %s has been terminated. (IN MANAGER THREAD)\n", currentClient.getID());
								clientMap.remove(currentClient.getID());
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
						*/
						catch(Exception e)
						{
							e.printStackTrace();
							clientMap.remove(currentClient.getID());
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
				socket = serverSocket.accept();//no timeout jk
				reader = new ObjectInputStream(socket.getInputStream());//so it reads from buffer
				writer = new ObjectOutputStream(socket.getOutputStream());
				writer.flush();
				String regKey = (String) reader.readObject();
				if(regKey.equals("reg"))//registration
				{
					ClientObject copyOf = (ClientObject)reader.readObject();
					socket.setSoTimeout(SOCKET_TIMEOUT);//1ms
					newClient = new ClientObject(copyOf, socket, reader, writer);
					String clientName = newClient.getID();
					if(clientMap.containsKey(clientName))
					{
						writer.writeObject("U");//invalid
						writer.flush();
						socket.close();
						System.out.println("Client's name was a duplicate. Client has been terminated.");
					}
					else
					{
						writer.writeObject(Integer.toString(id++));//is valid therefore id
						writer.flush();
						clientMap.put(clientName,newClient);
						keyList.add(clientName);
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
				break;
			}
		}
	}
}