package edu.purdue.cs490;
//import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.Set;
import java.util.Iterator;

public class CausalReliableBroadcaster implements CausalReliableBroadcast
{
	private ReliableBroadcaster rb;
	private Process self;
	private BroadcastReceiver receiver;
	//private ConcurrentHashMap<String, VectorClock> pendingSet;	
	private ConcurrentSkipListMap<String, VectorClock> pendingSet;
	public void init(Process currentProcess, BroadcastReceiver br)
	{
		self = currentProcess;
		receiver = br;
		rb = new ReliableBroadcaster(self, receiver);
		pendingSet = new ConcurrentSkipListMap<String, VectorClock>();			
	}
	public void addMember(Process member)
	{

	}
	public void removeMember(Process member)
	{

	}
	public void crbroadcast(Message m)
	{
		rb.rbroadcast(m);
		((ChatClientMessage)m).getVectorClock().incrementVectorClock(self.getID());
	}
	public Message receive(Message throwItDownTheHole)
	{
		Message m = rb.receive(throwItDownTheHole);
		if(m==null)//if null it means the message was of a different type therefore it was delivered and we don't need to do anything except tell anything above us that it has already been delivered
				return null;
		else
		{
			if(((ChatClientMessage)m).getType() == 3)//if type is Causal, then we do it
			{
				//insert algorithm here
				if(self.getVectorClock().isBefore(((ChatClientMessage) m).getVectorClock()))
				{
					//it means it's causally in order, so we just deliver it to rb
					((ChatClientMessage)m).setType(((ChatClientMessage)m).getType()-1);
					rb.receive(m);//then rb will deliver to beb, then print to client, but we also guarantee the rb and beb guarantees
					//update vector clock based on sender
					self.getVectorClock().incrementVectorClock(self.getID());
					//check the set to see if any are in order now
			        Set s = pendingSet.keySet();
        			Iterator i = s.iterator();
			        while (i.hasNext()) 
			        {
			        	if(pendingSet.get((String)i.next()).isBefore(self.getVectorClock()))
			        	{
							((ChatClientMessage)m).setType(((ChatClientMessage)m).getType()-1);
							rb.receive(m);//then rb will deliver to beb, then print to client, but we also guarantee the rb and beb guarantees			        	
			        	}
			        }
					return null;
				}
				else
				{
					//we put it into a set and return null
					pendingSet.put(self.getID(),((ChatClientMessage)m).getVectorClock());
					return null;
				}
			}
			else
			{
				return m;//pass it up to whoever needs the message!
			}
		}
	}
		//rbdeliver
		//check if message VC preceds local VC by checking if the vector clock is equal at the origin's index
		//if yes, deliver to client
			//if it was in set, remove from set
			//increment the vector clock index based on the origin of message
		//else, put in set and wait
}