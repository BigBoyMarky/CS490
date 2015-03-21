package edu.purdue.cs490;

public class ChatClientMessage implements Message
{
	private String contents;
	private Process sender;
	private int messageNumber;
	public ChatClientMessage(Process sender, int messageNumber, String contents)
	{
		this.setSender(sender);
		this.setMessageNumber(messageNumber);
		this.setMessageContents(contents);
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