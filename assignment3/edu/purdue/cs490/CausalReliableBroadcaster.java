package edu.purdue.cs490;
//import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.Set;
import java.util.Iterator;
import java.util.Comparator;

public class CausalReliableBroadcaster implements CausalReliableBroadcast
{
	private ReliableBroadcaster rb;
	private Process self;
	private BroadcastReceiver receiver;
	private VectorClock time;
	private ConcurrentSkipListSet<ChatClientMessage> pendingMessage;

	class MessageComparator implements Comparator<ChatClientMessage> {

		 @Override
		 public int compare(ChatClientMessage o1, ChatClientMessage o2) {
		 		return o1.getMessageContents().compareTo(o2.getMessageContents());
		 }
	}

	public CausalReliableBroadcaster(Process currentProcess, BroadcastReceiver br){
		init(currentProcess, br);
	}

	public void init(Process currentProcess, BroadcastReceiver br)
	{
		self = currentProcess;
		receiver = br;
		rb = new ReliableBroadcaster(self, receiver);
		pendingMessage = new ConcurrentSkipListSet<ChatClientMessage>();
		time = ((ClientObject)self).getVectorClock();
	}
	public void addMember(Process member)
	{

	}
	public void removeMember(Process member)
	{

	}
	public void crbroadcast(Message m)
	{
		m.setSender(self);
		((ChatClientMessage)m).setType(3);
		rb.rbroadcast(m);
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
			if(m.getSender() != self){
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

	public void deliver(){

		boolean more = true;

		while(more){
			more = false;
			for(ChatClientMessage s: pendingMessage){ 
				if(this.time.isBefore(s.getVectorClock())){   // if there is the message that has the earlier vector clock than the process
					pendingMessage.remove(s);
					receiver.receive(s);	// deliver that shit
					this.time.incrementVectorClock(s.getSender().getID()); // update the clock
					more = true; // might be more comrades
				}
			}
		}

	}
}
