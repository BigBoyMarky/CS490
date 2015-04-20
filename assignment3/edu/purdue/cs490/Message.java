package edu.purdue.cs490;

public interface Message
{
	int getMessageNumber();
	void setMessageNumber(int messageNumber);
	String getMessageContents();
	void setMessageContents(String contents);
	Process getSender();
	void setSender(Process sender);
	//our own little things
	int getType();
	void setType(int type);
	VectorClock getVectorClock();
	void setVectorClock(VectorClock vc);
}