package edu.purdue.cs490;

public class Process
{
	String IP;
	int port;
	String ID;
	public Process(String IP, int port, String ID)
	{
		this.IP = IP;
		this.port = port;
		this.ID = ID;
	}
	public String getIP()
	{
		return IP;
	}
	public int getPort()
	{
		return port;
	}
	public String getID()
	{
		return ID;
	}
}
