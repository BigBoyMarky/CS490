package edu.purdue.cs490;

import java.util.HashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class FIFOReliableBroadCast implements BroadcastReceiver{
	
	private Process currentProcess;
	private BroadcastReceiver receiver;
	private ConcurrentSkipListSet<Message> pending;
	private ConcurrentSkipListSet<Process> members;
	private ReliableBroadcast rblayer;
	private int seq;
	
	public FIFOReliableBroadCast(Process currentProcess, BroadcastReceiver br){
		init(currentProcess, br);
		new Thread(new Runnable() {
			HashMap<Process, Integer> delivered = new HashMap<Process,Integer>();
			
			public void run() {
				while(!pending.isEmpty()){
					for(Message m: pending){
						// register if not yet
						Process sender = m.getSender();
						if( !delivered.containsKey(sender) ){
							delivered.put(sender, 0);
						}
						if( delivered.get(sender)==m.getMessageNumber()){
							receiver.receive(m);
							delivered.put(sender, delivered.get(sender)+1);
						}
					}
				}
				pending.add(null);
			}

		}).start();
	}
	
	public void init(Process currentProcess, BroadcastReceiver br){
		this.currentProcess = currentProcess;
		this.receiver = br;
		rblayer = new ReliableBroadcast(currentProcess, this);
		pending = new ConcurrentSkipListSet<Message>();
		seq = 0;
	}
	
	public void addMember(Process member){
		members.add(member);
	}
	
	public void removeMember(Process member){
		members.remove(member);
	}
	
	public void FIFOBroadcast(Message m){
		m.setMessageNumber(seq++);
		m.setSender(this.currentProcess);
		rblayer.rbroadcast(m);
	}

	public void receive(Message m) {
		Process sender = m.getSender();
		if(!pending.contains(sender)){
			addMember(sender);
		}
		
		// put the message in the pending set
		pending.add(m);
	}
}