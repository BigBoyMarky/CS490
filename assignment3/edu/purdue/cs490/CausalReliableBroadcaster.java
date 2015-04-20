package edu.purdue.cs490;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.Set;
import java.util.Iterator;
import java.util.Comparator;

public class CausalReliableBroadcaster implements CausalReliableBroadcast
{
	private final int CAUSAL_ID = 3;
	private ReliableBroadcaster rb;//Causal is built ontop of RB, therefore once we finish checking, we rbdeliver our msgs
	private Process self;//this is the ClientObject representation of our own ChatClient
	private BroadcastReceiver client;//this is the ChatClient ITSELF, which means when we call receive(), it will deliver it straight to the chatclient
	private VectorClock time;//the VC is initiailized when Causal is initialized and set to 0
	private ConcurrentSkipListSet<ChatClientMessage> pendingMessage;//skip list set of all pending messages

	class MessageComparator implements Comparator<ChatClientMessage> 
	{
		 @Override
		 public int compare(ChatClientMessage o1, ChatClientMessage o2) 
		 {
		 		return o1.getMessageContents().compareTo(o2.getMessageContents());
		 }
	}

	//constructor, just calls .init() to initialize
	public CausalReliableBroadcaster(Process currentProcess, BroadcastReceiver br)
	{
		init(currentProcess, br);
	}

	//initiailization here
	public void init(Process currentProcess, BroadcastReceiver br)
	{
		self = currentProcess;
		client = br;
		rb = new ReliableBroadcaster(self, client);
		pendingMessage = new ConcurrentSkipListSet<ChatClientMessage>();
		time = ((ClientObject)self).getVectorClock(); // assuming it's initialize in the self shit already. 
	}
	public void addMember(Process member)
	{
		//when you add a new Process, you'll have to update the VectorClock
		time.set(member.getID(),((ClientObject)member).getVectorClock().getTime(member.getID()));
	}
	public void removeMember(Process member)
	{
		//we do not need to remove the old VectorClock
		//we could do a safe clean up where upon every client does an "ack" about removing the member, we remove it
		//but eh
		//nah
	}
	public void crbroadcast(Message m)
	{
		m.setSender(self);// set sender 
		m.setType(CAUSAL_ID);
		m.setVectorClock(this.time);// set the clock
		client.receive(m);//auto delivery
		rb.rbroadcast(m);
		this.time.incrementVectorClock(self.getID());// increment the clock (coz auto delivery)		
	}
	public Message receive(Message throwItDownTheHole)
	{
		Message pre = rb.receive(throwItDownTheHole);
		if(pre==null)//if null it means the message was of a different type therefore it was delivered and we don't need to do anything except tell anything above us that it has already been delivered
				return null;
		if(((ChatClientMessage)pre).getType() == 3)//if type is Causal, then we do it
		{
			ChatClientMessage m = (ChatClientMessage)pre;
			m.setVectorClock(new VectorClock(m.getVectorClock(), this.time, self.getID())); //update the message's clock to be relative with the process
			if(m.getSender() != self)
			{
				pendingMessage.add(m);	// add to the pending set 
				deliver(); // check if any message is deliverable or not
				return null;
			}
		}
		else
		{
			return pre;//pass it up to whoever needs the message!
		}
		return null;
	}
	//just delivering to chat client
	public void deliver()
	{
		boolean more = true;
		while(more)
		{
			more = false;
			for(ChatClientMessage s: pendingMessage)
			{ 
				if(s.getVectorClock().isBefore(this.time))
				{// if there is the message that has the earlier vector clock than the process
					pendingMessage.remove(s);
					client.receive(s);// deliver that sh@t
					this.time.incrementVectorClock(s.getSender().getID());//update the clock only upon successful delivery
					more = true;// might be more comrades
				}
			}
		}
	}
}