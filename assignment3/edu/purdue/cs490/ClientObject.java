package edu.purdue.cs490;

import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.BufferedReader;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
public class ClientObject extends Process implements Serializable
{
	volatile private long heartbeat;
	private transient Socket socket;
	private transient ObjectInputStream ois;
	private transient ObjectOutputStream oos;
	private transient BufferedReader buffer;
	private VectorClock myVectorClock;
	private boolean isSocketInit = false;
	private String username;
	public ClientObject(String username, String id, String ipAddress, int port, ConcurrentHashMap<String,ClientObject> listOfPeople)
	{
		super(ipAddress, port, id);
		this.username = username;
		myVectorClock = new VectorClock(listOfPeople);
	}
	public ClientObject(ClientObject copy, Socket socket, ObjectInputStream ois, ObjectOutputStream oos)
	{//for the single-threaded serverside
		super(copy.getIP(),copy.getPort(),copy.getID());
		this.socket = socket;
		this.ois = ois;
		this.oos = oos;
		//isSocketInit = true;
	}
	public ClientObject(ClientObject copy, Socket socket, ObjectInputStream ois, ObjectOutputStream oos, BufferedReader buffer)
	{//for the single-threaded serverside
		super(copy.getIP(),copy.getPort(),copy.getID());
		this.socket = socket;
		this.ois = ois;
		this.oos = oos;
		this.buffer = buffer;
	}
	public long getHeart()
	{
		return heartbeat;
	}
	public ObjectInputStream getIn()
	{
		return ois;
	}
	public ObjectOutputStream getOut()
	{
		return oos;
	}
	public BufferedReader getBuffer()
	{
		return buffer;
	}
	public void updateHeart(long timeStamp)
	{
		heartbeat = timeStamp;
	}
	public void setName(String newName)
	{
		ID = newName;
	}
	public boolean getInitState()
	{
		return isSocketInit;
	}
	public void flipInitState()
	{
		isSocketInit = !isSocketInit;
	}
	public VectorClock getVectorClock()
	{
		return myVectorClock;
	}
	public String getName()
	{
		return username;
	}
}