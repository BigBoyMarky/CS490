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
		//ChatClientMessage message = new ChatClientMessage(self, type, m, broadcastCount);//converts String to message
		ConcurrentHashMap<String,ClientObject> listOfUsers = ((ChatClient) client).getHashmap();
		Iterator availableUsers = listOfUsers.entrySet().iterator();//iterates through hashmap
		while(availableUsers.hasNext())
		{
			try
			{
				Map.Entry pair = (Map.Entry)availableUsers.next();
				System.out.printf("In BEB right now\n");
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
		System.out.printf("IN BEBROADCASTER\n");
		System.out.printf("m type = %d",((ChatClientMessage) m).getType());
		if(((ChatClientMessage) m).getType() == 0)
		{
			System.out.printf("client.receive\n");
			client.receive(m);
			return null;
		}
		else
			return m;
	}
}