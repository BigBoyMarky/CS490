package edu.purdue.cs490;

import java.io.Serializable;

public class ChatClientMessage implements Message, Serializable
{
	private String contents;
	private Process sender;
	private int messageNumber;
	private int type;
	//0 = BEB, 1 = Reliable, 2 = FIFO
	//if type == 0
		//print normally
	//if type == 1
		//keep copy, print normally
	//if type == 2
		//do something
	public ChatClientMessage(Process sender, int messageNumber, String contents, int type)
	{
		this.setSender(sender);
		this.setMessageNumber(messageNumber);
		this.setMessageContents(contents);
		this.type = type;
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
}