package edu.purdue.cs490;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.Comparator;

public class ReliableBroadcaster implements ReliableBroadcast
{
	private final int RBROADCAST_ID = 1;
	private Process currentProcess;
	private BroadcastReceiver receiver;
	private ConcurrentSkipListSet<Process> members;
	private BEBroadcaster beblayer;
	private ConcurrentSkipListSet<Message> receivedMessage;
	
	class ProcessComparator implements Comparator<Process>
	{

		 @Override 
		 public int compare(Process o1, Process o2)
		 {
		 		return o1.getID().compareTo(o2.getID());
		 }
	} 

	class MessageComparator implements Comparator<Message>
	{
		 @Override 
		 public int compare(Message o1, Message o2) 
		 {
		 		return o1.getMessageContents().compareTo(o2.getMessageContents());
		 }
	}
	
	public ReliableBroadcaster(Process currentProcess, BroadcastReceiver br)
	{
		init(currentProcess, br);
	}
	
	public void init(Process currentProcess, BroadcastReceiver br)
	{
		this.currentProcess = currentProcess;
		this.receiver = br;
		beblayer = new BEBroadcaster(currentProcess, br);
		receivedMessage = new ConcurrentSkipListSet<Message>(new MessageComparator());
		members = new ConcurrentSkipListSet<Process>(new ProcessComparator());
	}
	
	public void addMember(Process member)
	{
		members.add(member);
	}
	
	public void removeMember(Process member)
	{
		members.remove(member);
	}
	
	public void rbroadcast(Message m)
	{
		m.setSender(currentProcess);
		m.setType(RBROADCAST_ID);
		beblayer.BEBroadcast(m);
	}
	
	public Message receive(Message pre)
	{
		Message m = beblayer.receive(pre);
		if(m==null)
			return null;
		if(m!=null)
			receivedMessage.add(m);
		if(((ChatClientMessage)m).getType()==1)
		{
			((ChatClientMessage)m).setType(((ChatClientMessage)m).getType()-1);
			beblayer.receive(m);
			return null;
		}
		return m;
	}

	public void failHandler(Collection<ClientObject> members){
		ConcurrentSkipListSet<Message> removed = new ConcurrentSkipListSet<Message>();
		for(Message m : receivedMessage){
			boolean found = false;
			for(ClientObject mem: members ){
				if( (ClientObject)m.getSender() == mem){
					found = true;
					break;
				}
			}
			if(!found){
				System.out.println("NOTFOUND!");
				Message newMessage = new ChatClientMessage(currentProcess, m.getMessageContents());
				newMessage.setMessageNumber(0);
				newMessage.setType(1);

				rbroadcast(newMessage);
				removed.add(m);
			}
		}
		for(Message m: removed)
			receivedMessage.remove(m);
	}
}