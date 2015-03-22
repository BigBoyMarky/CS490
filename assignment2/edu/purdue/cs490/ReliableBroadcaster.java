package edu.purdue.cs490;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;

public class ReliableBroadcaster implements ReliableBroadcast {
	
	private Process currentProcess;
	private BroadcastReceiver receiver;
	private ConcurrentSkipListSet<Process> members;
	private BEBroadcaster beblayer;
	private ConcurrentSkipListSet<Message> receivedMessage;
	
	public ReliableBroadcaster(Process currentProcess, BroadcastReceiver br){
		init(currentProcess, br);
		
	}
	
	public void init(Process currentProcess, BroadcastReceiver br){
		this.currentProcess = currentProcess;
		this.receiver = br;
		beblayer = new BEBroadcaster(currentProcess, this);
	}
	
	public void addMember(Process member){
		members.add(member);
	}
	
	public void removeMember(Process member){
		members.remove(member);
	}
	
	public void rbroadcast(Message m) {
		m.setSender(currentProcess);
		beblayer.BEBroadcast(m);
	}
	
	public Message receive(Message pre) {
		Message m = beblayer.receive(pre);
		if(m!=null)
			receivedMessage.add(m);
		return null;
	}

	public void failHandler(List<Process> members){
		for(Message m : receivedMessage){
			boolean found = false;
			for(Process mem: members ){
				if(m.getSender()==mem){
					found = true;
					break;
				}
			}
			if(!found){
				Message newMessage = new ChatClientMessage(currentProcess, 0, m.getMessageContents(), 1);
				rbroadcast(newMessage);
			}
		}
	}
}
