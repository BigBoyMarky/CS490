package edu.purdue.cs490;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.net.Socket;
import java.net.SocketException;
import java.util.Map;
import java.io.ObjectOutputStream;

public class BEBroadcaster
{
	private final int BEBBROADCAST_ID = 0;
	private Process self;
	private BroadcastReceiver client;
	private ChannelInterface channel;
	public BEBroadcaster(Process self, BroadcastReceiver client)
	{
		this.self = self;
		this.client = client;
		this.channel = ((ChatClient)client).getChannel();
	}
	public void BEBroadcast(Message message)//sending to everyone = broadcast, so just multicasting to everyone, including self :P
	{
		message.setType(BEBBROADCAST_ID);
		ConcurrentHashMap<String,ClientObject> listOfUsers = ((ChatClient) client).getHashmap();
		Iterator availableUsers = listOfUsers.entrySet().iterator();//iterates through hashmap
		while(availableUsers.hasNext())
		{
			try
			{
				Map.Entry pair = (Map.Entry)availableUsers.next();
				channel.whisper((ClientObject)pair.getValue(),message);
			}
			catch(SocketException e)
			{
				continue;
			}
		}
	}

	public Message receive(Message m)
	{
		if(((ChatClientMessage) m).getType() == 0)
		{
			client.receive(m);
			return null;
		}
		else
			return m;
	}
}