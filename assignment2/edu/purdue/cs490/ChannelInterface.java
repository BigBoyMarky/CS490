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
	public void initClient(ClientObject interlocutor)
	{
		try
		{
			Socket clientSocket = new Socket(interlocutor.getIpAddress(), interlocutor.getPort());
			ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
			ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
			oos.flush();
			interlocutor = new ClientObject(interlocutor,clientSocket,ois,oos);
			System.out.printf("Chatting with %s\n",interlocutor.getName());
		}
		catch(Exception e)
		{
			e.printStackTrace();//will fill it up later
		}		
	}
	public void whisper(ClientObject interlocutor, Object message)
	{
		if(!interlocutor.getInitState())
			initClient(interlocutor);
		try
		{
			interlocutor.getOut().writeObject(message);
		}
		catch(Exception e)
		{
			e.printStackTrace();//will fill it up later
		}		
	}
	/*From client*/
	public void initInvitation(ServerSocket serverSocket)
	{
		try
		{
			Socket newSocket = serverSocket.accept();
			newSocket.setSoTimeout(50);
			socketList.add(newSocket);
			ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
			oisList.add(ois);			
		}
		catch(Exception e)
		{
			e.printStackTrace();//will fill it up later
		}		
	}
	public Object fromClient()
	{
		int size = oisList.size();
		for(int i = 0; i < size; i++)
		{
			try
			{
				Object message = oisList.get(i).readObject();
				return message;
			}
			catch(Exception e)
			{
				continue;
			}

		}
		return null;//if no messages found, return nothing
	}
}
