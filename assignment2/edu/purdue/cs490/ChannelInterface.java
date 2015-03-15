package edu.purdue.cs490;

import java.net.SocketException;
import java.util.ArrayList;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.net.UnknownHostException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.ServerSocket;
import java.io.InterruptedIOException;

public class ChannelInterface
{
	/*To server*/
	private ObjectOutputStream heart;//printer to server
	private ObjectInputStream heartListener;
	private Socket socket;
	/*To client*/
	private PrintWriter printer;//printer to client
	private BufferedReader reader;//reader to client
	/*From client*/
	private ArrayList<Socket> socketList = new ArrayList<Socket>();//socketList of everyone chatting you
	private ArrayList<ObjectInputStream> oisList = new ArrayList<ObjectInputStream>();
	private ArrayList<String> nameList = new ArrayList<String>();
	public ChannelInterface()
	{//initialized all necessary things
	}

	/*To server*/
	public void initServer(String serverHost, int serverPort)
	{
		try
		{
			socket = new Socket(serverHost,serverPort);//creates socket to server
			heart = new ObjectOutputStream(socket.getOutputStream());//creates new oos
			heart.flush();//flushes header
			heartListener = new ObjectInputStream(socket.getInputStream());//creates new ois
		}
		catch(Exception e)
		{
			e.printStackTrace();//will fill it up later
		}
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
	public void toServer(Object message)
	{
		try
		{
			heart.writeObject(message);
			heart.flush();
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
	public ClientObject initClient(ClientObject interlocutor)
	{
		try
		{
			System.out.printf("In initClient\n");
			Socket clientSocket = new Socket(interlocutor.getIpAddress(), interlocutor.getPort());
			System.out.printf("IP:%s\tPort:%d\tSocket made\n",interlocutor.getIpAddress(),interlocutor.getPort());
			ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
			System.out.printf("oos made\n");						
			oos.flush();
			ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());//waiting for dat flush huh
			System.out.printf("ois made\n");
			oos.writeObject(interlocutor.getName());
			interlocutor = new ClientObject(interlocutor,clientSocket,ois,oos);
			//doesn't work bc pass by value, so I return it
			return interlocutor;
		}
		catch(Exception e)
		{
			e.printStackTrace();//will fill it up later
		}
		return null;
	}
	public void whisper(ClientObject interlocutor, Object message)
	{
		if(!interlocutor.getInitState())
			interlocutor = initClient(interlocutor);
		try
		{
			//System.out.printf("in whisper\n");
			System.out.printf("whispering:%s",message);
			interlocutor.getOut().writeObject(message);
			interlocutor.getOut().flush();//does flushing fix it?
			//System.out.printf("sent message\n");			
		}
		catch(Exception e)
		{
			e.printStackTrace();//will fill it up later
		}		
	}
	/*From client*/
	public void initInvitation(ServerSocket serverSocket) throws SocketException, IOException, SocketTimeoutException
	{
			Socket newSocket = serverSocket.accept();
			System.out.printf("initInvitation inited!");
			newSocket.setSoTimeout(50);
			socketList.add(newSocket);
			System.out.printf("added new socket!\n");			
			ObjectOutputStream oos = new ObjectOutputStream(newSocket.getOutputStream());
			oos.flush();//needs to flush
			System.out.printf("added new oos!\n");			
			ObjectInputStream ois = new ObjectInputStream(newSocket.getInputStream());
			System.out.printf("added new ois!\n");
			String name = "";
			try
			{
				name = (String) ois.readObject();
			}
			catch(ClassNotFoundException e)	
			{
				e.printStackTrace();
			}
			oisList.add(ois);
			nameList.add(name);
	}
	public void fromClient()
	{
		int size = oisList.size();
/*		if(size > 0)
		{
			System.out.printf("oisList size = %d\n",size);
		}
*/		for(int i = 0; i < size; i++)
		{
			try
			{
				String name =  nameList.get(i);
				String message = (String)oisList.get(i).readObject();//does this work now?
				System.out.printf("%s:%s\n",name,message);
			}
			catch(Exception e)
			{
				continue;
			}
		}
	}
}