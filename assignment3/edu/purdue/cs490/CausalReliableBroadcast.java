package edu.purdue.cs490;

public interface CausalReliableBroadcast
{
	public void init(Process currentProcess, BroadcastReceiver br);
	public void addMember(Process member);
	public void removeMember(Process member);
	public void crbroadcast(Message m);
}