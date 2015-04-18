package edu.purdue.cs490;

import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.BufferedReader;
import java.io.ObjectOutputStream;
import java.net.Socket;
public class ClientObject extends Process implements Serializable
{
	volatile private long heartbeat;
	private transient Socket socket;
	private transient ObjectInputStream ois;
	private transient ObjectOutputStream oos;
	private transient BufferedReader buffer;
	private boolean isSocketInit = false;
	public ClientObject(String username, String ipAddress, int port)
	{
		super(ipAddress, port, username);
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

}