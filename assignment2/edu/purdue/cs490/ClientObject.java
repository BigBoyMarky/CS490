package edu.purdue.cs490;

import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.BufferedReader;
import java.io.ObjectOutputStream;
import java.net.Socket;
public class ClientObject extends Process implements Serializable
{
	//will be inherited from Process anyways :/
	//private String username;
	//private String ipAddress;
	//private int port;
	volatile private long heartbeat;
	private transient Socket socket;
	private transient ObjectInputStream ois;
	private transient ObjectOutputStream oos;
	private transient BufferedReader buffer;
	private boolean isSocketInit = false;
	public ClientObject(String username, String ipAddress, int port)
	{
		super(ipAddress, port, username);
		//information sent from the clientside
		/*
		this.ID = username;
		this.IP = ipAddress;
		this.port = port;*/
	}
	public ClientObject(ClientObject copy, Socket socket, ObjectInputStream ois, ObjectOutputStream oos)
	{//for the sigle-threaded serverside
		super(copy.getIP(),copy.getPort(),copy.getID());
		this.socket = socket;
		this.ois = ois;
		this.oos = oos;
		//isSocketInit = true;
	}
	public ClientObject(ClientObject copy, Socket socket, ObjectInputStream ois, ObjectOutputStream oos, BufferedReader buffer)
	{//for the sigle-threaded serverside
		super(copy.getIP(),copy.getPort(),copy.getID());
		this.socket = socket;
		this.ois = ois;
		this.oos = oos;
		this.buffer = buffer;
	}
	/* in essence, this is Process
	public String getName()
	{
		return username;
	}
	public String getIpAddress()
	{
		return ipAddress;
	}
	public int getPort()
	{
		return port;
	}*/
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
		//System.out.printf("Updating heart to %d.",timeStamp);
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