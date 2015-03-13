package edu.purdue.cs490;

import java.util.ArrayList;
import java.net.Socket;
import java.io.ObjectOutputStream;

public BEBroadcaster
{
	private ArrayList<Socket> socketList;
	private int socketListSize;
	public BEBroadcaster(ArrayList<Socket> socketList)
	{
		this.socketList = socketList;
		this.socketListSize = socketList.size();
	}
	public void BEBroadcast(String m)
	{
		ObjectOutputStream writer;
		for(int i = 0; i < socketListSize; i++)
		{
			writer = new ObjectOutputStream(socketList.get(i));
			writer.flush();
			writer.writeObject(m);
			writer.flush();
		}
	}
	public void init(Process thisProcess, BroadcastReceiver br)
	{
		
	}
	public void addMember(Process member)
	{

	}
	public void removeMember(Process member)
	{

	}
}