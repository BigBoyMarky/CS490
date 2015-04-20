package edu.purdue.cs490;

import java.util.HashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.Comparator;

public class FIFOReliableBroadcaster implements FIFOReliableBroadcast
{
	private final int FIFOBROADCAST_ID = 2;
	private Process currentProcess;
	private BroadcastReceiver client;
	private ConcurrentSkipListSet<Message> pending;
	private ConcurrentSkipListSet<Process> members;
	private ReliableBroadcaster rblayer;
	private HashMap<Process, Integer> delivered;
	private int seq;

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

	public FIFOReliableBroadcaster(Process currentProcess, BroadcastReceiver br)
	{
		init(currentProcess, br);
		delivered = new HashMap<Process,Integer>();
	}

	public void init(Process currentProcess, BroadcastReceiver br)
	{
		this.currentProcess = currentProcess;
		this.client = br;
		rblayer = new ReliableBroadcaster(currentProcess, br);
		pending = new ConcurrentSkipListSet<Message>(new MessageComparator());
		members = new ConcurrentSkipListSet<Process>(new ProcessComparator());
		seq = 0;
	}
	public void addMember(Process member)
	{
		members.add(member);
	}
	public void removeMember(Process member)
	{
		members.remove(member);
	}
	public void FIFOBroadcast(Message m)
	{
		m.setMessageNumber(seq++);
		m.setSender(this.currentProcess);
		m.setType(FIFOBROADCAST_ID);
		rblayer.rbroadcast(m);
	}
	public Message receive(Message pre)
	{
		Message m = rblayer.receive(pre);

		if(m==null)
			return null;

		Process sender = m.getSender();
		if(!pending.contains(sender))
		{
			addMember(sender);
			delivered.put(sender, 0);
		}

		if( delivered.get(sender)==m.getMessageNumber() )
		{
			delivered.put(sender, delivered.get(sender)+1);
			pending.remove(m);

			boolean sent = true;
			while(sent)
			{
				sent = false;
				for(Message s: pending)
				{
					if( s.getSender() == sender && delivered.get(sender)==s.getMessageNumber() )
					{
						pending.remove(s);
						sent = true;
						m.setMessageContents(m.getMessageContents().concat(s.getMessageContents()));
						delivered.put(sender, delivered.get(sender)+1);
					}
				}
			}

			// receiver.receive(m);
			rblayer.receive(m);
			return m;
		}
		else
		{
			pending.add(m);
			return null;
		}
	}
}
