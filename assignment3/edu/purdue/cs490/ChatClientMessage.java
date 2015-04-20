package edu.purdue.cs490;
import java.io.Serializable;

public class ChatClientMessage implements Message, Serializable
{
	private String contents;//the contents
	private Process sender;//the sender, it is initialized with ChatClientObject (which extends Process)
	private int messageNumber;//for FIFO
	private VectorClock myVectorClock;//for Causal	
	private int type;//to determine type of message sent
	//0 = BEB, 1 = Reliable, 2 = FIFO, 3 = Causal
	//if type == 0
		//print normally
	//if type == 1
		//keep copy, print normally
	//if type == 2
		//do something
	//if type == 3
		//initiailize VectorClock	
	public ChatClientMessage(Process sender, int messageNumber, String contents, int type)
	{
		this.setSender(sender);
		this.setMessageNumber(messageNumber);
		this.setMessageContents(contents);
		this.type = type;
		if(type == 2)
			messageNumber = 0;
	}
	public ChatClientMessage(Process sender, String contents, int type, VectorClock myVectorClock)
	{
		this.setSender(sender);
		this.setMessageContents(contents);
		this.type = type;
		this.myVectorClock = myVectorClock;
	}
	public int getType()
	{
		return type;
	}
	public int getMessageNumber()
	{
		return messageNumber;
	}
	public void setMessageNumber(int messageNumber)
	{
		this.messageNumber = messageNumber;
	}
	public String getMessageContents()
	{
		return contents;
	}
	public void setMessageContents(String contents)
	{
		this.contents = contents;
	}
	public Process getSender()
	{
		return sender;
	}
	public void setSender(Process sender)
	{
		this.sender = sender;
	}
	public VectorClock getVectorClock()
	{
		return myVectorClock;
	}
	public VectorClock incrementVectorClock()
	{
		//increments it by the protocol which is, it's ID along with
		myVectorClock.set(sender.getID(),myVectorClock.getTime(sender.getID())+1);
	}
}