package edu.purdue.cs490;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.net.Socket;
import java.net.SocketException;
import java.util.Map;
import java.io.ObjectOutputStream;

public class Caster
{
	private ChannelInterface channel;
	private ClientObject self;
	//private int broadcastCount = 0;

	public Caster(ClientObject self, ChannelInterface channel)
	{
		this.self = self;
		this.channel = channel;
	}

	public void multicast(int type, ConcurrentHashMap listOfUsers, String m, int broadcastCount)//sending to everyone = broadcast, so just multicasting to everyone, including self :P
	{
		ChatClientMessage message = new ChatClientMessage(self, type, m, broadcastCount);//converts String to message
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
}