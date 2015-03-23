package edu.purdue.cs490;

public interface FIFOReliableBroadcast
{
	public void init(Process currentProcess, BroadcastReceiver br);
	public void addMember(Process member);
	public void removeMember(Process member);
	public void FIFOBroadcast(Message m);
}