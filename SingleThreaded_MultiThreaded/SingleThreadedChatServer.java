/*
TODO: HANDLE 100K REQUESTS
//Handle NullPointerException for people who send null

*/
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.net.*;
import java.io.*;

public class SingleThreadedChatServer
{
	/**************************************************************************************************
	*											FIELDS												*
	**************************************************************************************************/	
	private int SOCKET_TIMEOUT = 10;//in milliseconds
	private long heartbeat_rate = 10000 + SOCKET_TIMEOUT;//in milliseconds
	private static SingleThreadedChatServer server;	
	private ServerSocket serverSocket;
	private int port;//port
	private int numClients = -1;//keeps track of number of clients for ID'ing purposes
	private ArrayList<String> keyList = new ArrayList<String>();	
	private ArrayList<ClientObject> clientList = new ArrayList<ClientObject>();
	private ConcurrentHashMap<String,ClientObject> clientMap = new ConcurrentHashMap<String,ClientObject>();//hashmap used so we can check if there is a duplicate name easily
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
	public SingleThreadedChatServer(int port) throws IOException
	{
		this.port = port;
		serverSocket = new ServerSocket(port);
		serverSocket.setReuseAddress(true);
		serverSocket.setSoTimeout(SOCKET_TIMEOUT);
	}
	/**************************************************************************************************
	*										PROCESSING MESSAGES										*
	**************************************************************************************************/		
	public void forLoop()
	{
		//System.out.println("forLoop START");		
		String message ="";
		String currentClient="";
		try
		{
			numClients = keyList.size();		
			for(int i = 0; i < numClients; i++)
			{
				currentClient = keyList.get(i);
				try
				{
					message = (String) clientMap.get(currentClient).getIn().readObject();//reads a String Object					
				}
				catch(SocketTimeoutException e)//exception thrown when reading/writing/closing
				{
					//e.printStackTrace();
					//System.out.println("FORLoop IOExceptioN");				
					//for(int j = 0; j < numClients; j++)
					//{
						clientMap.get(keyList.get(i)).getHeart();
						if(System.currentTimeMillis()-clientMap.get(keyList.get(i)).getHeart() > heartbeat_rate)
						{
							System.out.printf("Because of lack of heartbeat, user %s has been terminated. (in IOEXCEPT 1)\n",keyList.get(i));
							clientMap.remove(keyList.get(i));
							keyList.remove(i);
							numClients = keyList.size();//anytime we make a change we must update numClients to prevent indexoutofbounds
						}					
					//}
					continue;
				}
				catch(IOException e)				
				{
					e.printStackTrace();
				}
				//System.out.printf("clientMap's getIn() delays for %d milliseconds.\n",(System.currentTimeMillis()-currentTime));
				System.out.printf("%s sent the following message: %s\n",currentClient,message);
				if(message.equals("get"))
				{
					try
					{
						clientMap.get(currentClient).getOut().writeObject(clientMap);
						clientMap.get(currentClient).getOut().reset();//apparently Object stream keeps cache
						clientMap.get(currentClient).getOut().flush();
					}
					catch(IOException e)//exception thrown when reading/writing/closing
					{
						System.out.println("Unable to send clientMap...");				
						for(int j = 0; j < numClients; j++)
						{
							clientMap.get(keyList.get(j)).getHeart();
							if(System.currentTimeMillis()-clientMap.get(keyList.get(j)).getHeart() > heartbeat_rate)
							{
								System.out.printf("Because of lack of heartbeat, user %s has been terminated. (in IOEXCEPT 2)\n",keyList.get(j));
								clientMap.remove(keyList.get(j));
								keyList.remove(j);
								numClients = keyList.size();
							}					
						}
						continue;
					}							
					clientMap.get(currentClient).updateHeart(System.currentTimeMillis());//in case heartbeat sent same time
					//System.out.printf("Get took %d milliseconds.\n",(System.currentTimeMillis()-currentTime));
				}
				else if(message.equals("<3"))
				{
					clientMap.get(currentClient).updateHeart(System.currentTimeMillis());
				}
				else
				{
					System.out.printf("Invalid message. Request from %s will be ignored.",currentClient);
				}
				if(System.currentTimeMillis()-clientMap.get(keyList.get(i)).getHeart() > heartbeat_rate)
				{
					System.out.printf("Because of lack of heartbeat, user %s has been terminated (in FOR).\n",currentClient);
					clientMap.remove(currentClient);
					keyList.remove(i);
					numClients = keyList.size();					
				}
			}
		}
		catch(ClassNotFoundException e)
		{
			//System.out.println("rFORLoop CNFException's");			
			for(int i = 0; i < numClients; i++)
			{
				if(System.currentTimeMillis()-clientMap.get(keyList.get(i)).getHeart() > heartbeat_rate)
				{
					System.out.printf("Because of lack of heartbeat, user %s has been terminated. (in CNFEXCEPT)\n",keyList.get(i));
					clientMap.remove(keyList.get(i));
					keyList.remove(i);
					numClients = keyList.size();
				}					
			}				
		}		
	}
	/**************************************************************************************************
	*										THE ONLY THREAD 										*
	**************************************************************************************************/		
	public void runServer()
	{
		Socket socket;
		ObjectInputStream reader;
		ObjectOutputStream writer;
		ClientObject newClient;
		while(true)
		{
			try
			{
				socket = serverSocket.accept();//has timeout
				System.out.println("A new client has connected!");
				socket.setSoTimeout(SOCKET_TIMEOUT);//for socket.getInputStream() reading purposes
				reader = new ObjectInputStream(socket.getInputStream());
				
				writer = new ObjectOutputStream(socket.getOutputStream());
			
				writer.flush();//need to flush header for other side to start reading
		
				String regKey = (String) reader.readObject();

				if(regKey.equals("reg"))//registration
				{
					System.out.println("Client has sent a valid registration key.");
					newClient = new ClientObject((ClientObject)reader.readObject(), socket, reader, writer);
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
						newClient.updateHeart(System.currentTimeMillis());//for reg purposes
						System.out.printf("%s has successfully registered.\n",clientName);
					}
				}
				else
				{
					System.out.println("Client has sent an invalid registration key. We will not process the client's requests");
					continue;
				}
				this.forLoop();
				//System.out.println("regLoop's forLoop END");
			}
			catch(SocketTimeoutException e)//exception thrown when timeout, no worries
			{
				//System.out.println("regLoop STException");
				this.forLoop();
				//System.out.println("regLoop STException's forLoop END");				
				continue;//making the server truly single threaded
			}
			catch(IOException e)//exception thrown when a Socket is disconnected
			{
				//System.out.println("regLoop IOException");				
				this.forLoop();
				//System.out.println("regLoop IOException's forLoop END");				
				continue;
			}	
			catch(ClassNotFoundException e)		
			{
				//System.out.println("regLoop CNFException");				
				this.forLoop();
				//System.out.println("regLoop CNFException's forLoop END");				
				continue;
			}
		}
	}
}
