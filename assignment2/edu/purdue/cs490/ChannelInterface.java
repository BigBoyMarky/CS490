//128.10.25.121
package edu.purdue.cs490;
import java.util.concurrent.ConcurrentHashMap;
import java.net.ConnectException;
import java.net.SocketException;
import java.util.ArrayList;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.net.UnknownHostException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.ServerSocket;
import java.io.InterruptedIOException;
import java.io.EOFException;

public class ChannelInterface implements Runnable
{
	/*Unique to self*/
	private String name;
	private ChatClient self;
	private ConcurrentHashMap<String, ClientObject> listOfUsers;
	private int numInterlocuters = 0;
	private static int SOCKET_TIMEOUT = 100;
	/*To server*/
	private ObjectOutputStream heart;//printer to server
	private ObjectInputStream heartListener;
	private Socket socket;
	/*To client*/
	/*From client*/
	private ArrayList<Socket> socketList = new ArrayList<Socket>();//socketList of everyone chatting you
	private ArrayList<ObjectOutputStream> oosList = new ArrayList<ObjectOutputStream>();
	private ArrayList<ObjectInputStream> oisList = new ArrayList<ObjectInputStream>();
	private ArrayList<String> nameList = new ArrayList<String>();
	private ServerSocket serverSocket;
	/*for Broadcasts only*/

	private double totaltime;
	private double count;

	public ChannelInterface(ChatClient self, String name) throws IOException
	{//initialized all necessary things
		this.name = name;
		this.self = self;
		listOfUsers = self.getHashmap();
		totaltime = 0;
		count = 0;
		serverSocket = new ServerSocket(0);//initializes serverSocket
		serverSocket.setReuseAddress(true);
		serverSocket.setSoTimeout(SOCKET_TIMEOUT);//sets a SOCKET_TIMEOUT for serverSocket.accept() so when WE initialize contact, we can continue on this thread
		new Thread(this).start();
	}
	public int getClientPort()
	{
		return serverSocket.getLocalPort();//clientPort is set up		
	}
	/*To server*/
	public boolean initServer(String serverHost, int serverPort)
	{
		boolean success = false;
		try
		{
			socket = new Socket(serverHost,serverPort);//creates socket to server
			heart = new ObjectOutputStream(socket.getOutputStream());//creates new oos
			heart.flush();//flushes header
			heartListener = new ObjectInputStream(socket.getInputStream());//creates new ois
			return !success;
		}
		catch(ConnectException e)
		{
			return success;
		}
		catch(Exception e)
		{
			e.printStackTrace();//will fill it up later
		}
		return success;
	}
	public void closeServer()
	{
		try
		{
			heart.writeObject("Close");
			heart.flush();
			socket.close();
			heart.close();
			heartListener.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();//will fill it up later
		}

	}
	public void toServer(Object message) throws SocketException
	{
		try
		{
			heart.writeObject(message);
			heart.flush();
		}
		catch(SocketException e)
		{
			throw e;
		}
		catch(Exception e)
		{
			e.printStackTrace();//will fill it up later
		}
	}
	public Object fromServer()//how object is interpreted is based on guy whose calling this
	{
		try
		{
			return heartListener.readObject();
		}
		catch(Exception e)
		{
			e.printStackTrace();//will fill it up later
		}
		return null;//if no message or there is an error
	}

	/*To client*/
	public void initClient(ClientObject interlocuter) throws ConnectException
	{
		try
		{
			if(!interlocuter.getInitState())//if not initialized
			{
				Socket clientSocket = new Socket(interlocuter.getIP(), interlocuter.getPort());
				clientSocket.setSoTimeout(SOCKET_TIMEOUT);//don't forget this guy needs sotimeouttoo!
				ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
				oos.flush();
				oos.writeObject(name);//sneds name over
				oos.flush();
				ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());//waiting for dat flush huh
				//don't want to send name of guy you're communicating with!
				socketList.add(clientSocket);
				nameList.add(interlocuter.getID());
				oisList.add(ois);
				oosList.add(oos);
			}System.out.println(interlocuter.getID());
		}
		catch(ConnectException e)
		{
			throw e;
		}
		catch(Exception e)
		{
			e.printStackTrace();//will fill it up later
		}
	}
	public void whisper(ClientObject interlocuter, Object message) throws SocketException
	{
		try
		{
			int index = nameList.indexOf(interlocuter.getID());
			if(index == -1)
			{
				this.initClient(interlocuter);
				index = nameList.indexOf(interlocuter.getID());//initiailizes it
			}
			ObjectOutputStream stream = oosList.get(index);
			oosList.get(index).writeObject(message);
			oosList.get(index).flush();//does flushing fix it?
		}
		catch(ConnectException e)
		{
			System.out.printf("%s has disconnected. Try refreshing and seeing if s/he returns!\n", interlocuter.getID());
		}
		catch(SocketException e)
		{
			System.out.printf("Looks like the client is offline! Unable to send message to %s\n", interlocuter.getID());
		}
		catch(Exception e)
		{

			e.printStackTrace();//will fill it up later
		}
	}
	public void run()
	{
		String name = null;
		while(true)
		{
			try
			{
				if(numInterlocuters == 0)
				{
					name = this.initInvitation();
					self.get();
					self.setInterlocuter(listOfUsers.get(name));
					numInterlocuters++;
					listOfUsers.get(name).flipInitState();//wtf
				}//I seriously wish we can say "delete this statement, so you don't need to check in the future anymore..." It's only 1 time use bro..
				else
				{
					name = this.initInvitation();
					//null pointer exception is thrown at line 200. 
					listOfUsers.get(name).flipInitState();//is flipping safe? Will we ever get a new socket request from something that's already made?
				}
			}
			catch(SocketTimeoutException e)
			{
				this.fromClient();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}		
	}
	/*From client*/
	public String initInvitation() throws SocketException, IOException, SocketTimeoutException
	{
		Socket newSocket = serverSocket.accept();
		//System.out.printf("In initinivation!\n");
		newSocket.setSoTimeout(SOCKET_TIMEOUT);
		ObjectOutputStream oos = new ObjectOutputStream(newSocket.getOutputStream());
		oos.flush();//needs to flush
		ObjectInputStream ois = new ObjectInputStream(newSocket.getInputStream());
		String name = "";
		try
		{
			name = (String) ois.readObject();
		}
		catch(ClassNotFoundException e)	
		{
			e.printStackTrace();
		}
		socketList.add(newSocket);			
		oisList.add(ois);
		oosList.add(oos);
		nameList.add(name);
		System.out.printf("Registered %s\n",name);
		/*NEW CODE HERE, ELIMINATE IF RUN OUT OF TIME*/
		this.toServer("get");
		listOfUsers =  (ConcurrentHashMap<String, ClientObject>) this.fromServer();
		/*END*/
		return name;
	}
	public void fromClient()
	{
		int size = oisList.size();
		for(int i = 0; i < size; i++)
		{
			String name = "";
			try
			{
				name = nameList.get(i);
			}
			catch(IndexOutOfBoundsException e)
			{
				size = oisList.size();
				i = 0;
			}			
			try
			{
				ChatClientMessage message = (ChatClientMessage)oisList.get(i).readObject();
				long start = System.currentTimeMillis();
				self.getFIFO().receive(message);
				count++;
				totaltime += System.currentTimeMillis() - start;
				if(count == 100000){//100,000 total messages received
					System.out.println("AVG.THROUGHPUT: " + totaltime/count);
				}
			}
			catch(SocketTimeoutException e)
			{
				continue;
			}
			catch(SocketException e)
			{
				System.out.printf("%s is unreachable!\n",name);
				oisList.remove(i);//remove from list
				nameList.remove(i);
				oosList.remove(i);
				//return "\\" + name;//\\ are reserved, so no way user can mistype this
			}
			catch(EOFException e)
			{
				System.out.printf("%s is unreachable!\n",name);
				oisList.remove(i);//remove from list
				nameList.remove(i);
				oosList.remove(i);				
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		//return "";//if no message to print, then print nothing
	}
	public void updateHashmap(ConcurrentHashMap<String, ClientObject> newMap)
	{
		listOfUsers = newMap;
	}
	public void forcePrint()
	{
		System.out.printf("FORCED EXIT AT %d\nAVG. THROUGHPUT: %d\n", count, (totaltime/count));
	}
}