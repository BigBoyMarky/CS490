package edu.purdue.cs490;

import java.util.ArrayList;
import java.net.Socket;
import java.io.ObjectOutputStream;

public BEBroadcaster
{
	private ChannelInterface channel;
	public BEBroadcaster(ChannelInterface channel)
	{
		this.channel = channel;
	}
	public void BEBroadcast(Message m)
	{
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