package edu.purdue.cs490
import java.util.concurrent.ConcurrentHashmap;

public class CausalReliableBroadcaster implements CausalReliableBroadcast
{
	private ReliableBroadcaster rb;
	private Process self;
	private BroadcastReceiver receiver;
	
	public void init(Process currentProcess, BroadcastReceiver br)
	{
		self = currentProcess;
		receiver = br;
		rb = new ReliableBroadcaster(self, receiver);
	}
	public void addMember(Process member)
	{

	}
	public void removeMember(Process member)
	{

	}
	public void crbroadcast(Message m)
	{
		//rbroadcast
		rb.rbroadcast(m);
		m.incrementVectorClock();
		//update vector clock by incrementing this process by 1
	}
	public Message receive(Message pre)
	{
		Message m = rb.receive(pre);
		if(m==null)
				return null;
		else
				
		return m;
	}


		//rbdeliver
		//check if message VC preceds local VC by checking if the vector clock is equal at the origin's index
		//if yes, deliver to client
			//if it was in set, remove from set
			//increment the vector clock index based on the origin of message
		//else, put in set and wait
}