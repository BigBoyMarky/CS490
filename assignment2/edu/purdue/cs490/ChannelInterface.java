package edu.purdue.cs490;

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

public class ChannelInterface
{
	/*Unique to self*/
	private String name;
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
	public ChannelInterface(String name)
	{//initialized all necessary things
		this.name = name;
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
	public void initClient(ClientObject interlocuter)
	{
		try
		{
			if(!interlocuter.getInitState())//if not initialized
			{
				Socket clientSocket = new Socket(interlocuter.getIpAddress(), interlocuter.getPort());
				clientSocket.setSoTimeout(50);//don't forget this guy needs sotimeouttoo!
				ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
				oos.flush();
				oos.writeObject(name);//sneds name over
				oos.flush();
				ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());//waiting for dat flush huh
				//don't want to send name of guy you're communicating with!
				socketList.add(clientSocket);
				nameList.add(interlocuter.getName());
				oisList.add(ois);
				oosList.add(oos);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();//will fill it up later
		}
	}
	public void whisper(ClientObject interlocuter, Object message)
	{
		int index = nameList.indexOf(interlocuter.getName());
		if(index == -1)
		{
			this.initClient(interlocuter);
			index = nameList.indexOf(interlocuter.getName());//initiailizes it
		}
		ObjectOutputStream stream = oosList.get(index);
		try
		{
			oosList.get(index).writeObject(message);
			oosList.get(index).flush();//does flushing fix it?
		}
		catch(Exception e)
		{
			e.printStackTrace();//will fill it up later
		}		
	}
	/*From client*/
	public String initInvitation(ServerSocket serverSocket) throws SocketException, IOException, SocketTimeoutException
	{
			Socket newSocket = serverSocket.accept();
			newSocket.setSoTimeout(50);
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
			return name;
	}
	public String fromClient()
	{
		int size = oisList.size();
		for(int i = 0; i < size; i++)
		{
			try
			{
				String name =  nameList.get(i);
				String message = (String)oisList.get(i).readObject();//does this work now?
				return name+":"+message+"\n";//string to return
			}
			catch(SocketTimeoutException e)
			{
				continue;
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		return "";//if no message to print, then print nothing
	}
}