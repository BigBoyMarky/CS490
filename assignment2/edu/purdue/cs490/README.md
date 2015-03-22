How to Make
============
Stay in this directory, and type in "make" into the command line, because Unix.

How to Run
============

MultiThreadedChatServer
-----------------------
Call "java MultiThreadedChatServer"
That's it. If you want to specify a port number, you can specify it as an argument (e.g. "java MultiThreadedChatServer 4200") in the command line. The server will print out its port and IPv4 Address so you can connect clients to it from other computers.

ChatClient
-----------
Call "java ChatClient". 
1] The program will prompt for Hostname of the server. Look at "Server Host Name: " on the server's command line and copy that. 
2] The program will prompt for the server's port. Enter the port of the server. 
3] The program will prompt for a username, any name is valid. (note: if you're running multiple clients, you cannot have multiple clients sharing the same name. The client will prompt you to re-enter if the name you entered is a duplicate). 
Note: If you make a mistake, don't worry, entering incorrect information will cause the program to prompt for the Hostname/port/username again. (you can't change your name though)

How to Use
============

MultiThreadedChatServer
-----------------------
Once you run it, it does everything automatically :P

ChatClient
----------
1] Type in '\help' for a list of all commands.
2] When you connect with someone, entering anything that is not a command will send that String to the person you're connecting with.
3] Type in '\switch' to switch to another person to message.
//4] Type in '\everybody' to reliable broadcast.
//5] Type in '\fifo' to fifo broadcast.


Fixed Log
=================
Fixed Log is transferred to the commit messages every commit.
2. ChatClient no longer extends Process. ClientObject is now the class that extends Process. Both classes have been updated based on their new ancestries.
3. Created ChatClientMessage.java. ChatClientMessage implements Message.
1. Created Caster.java. This is our universal caster. It multicasts. You feed in the Hashmap of ClientObjects it needs to send to, along with your message, and the type of casting you want, and it does it for you. If you want to unicast, have a hashmap of size 1, if you have to broadcast, have a hashmap of all users.

Types:
0 = Best Effort
1 = Reliable
2 = FIFO

Very easy to update, simply add a new Receiver that reads the type, update your ChatClient to include that type, and you're good to go. Better than all the API interface mess we're given.
4. Only Caster sends ChatClientMessages. 
THINGS TO FIX:
4. Need to update Receiver to process ChatClientMessages based on their type.
2. ChatClient now implements BroadcastReceiver as according to assignment specifications.
3. Removed Reciever.java (receive just calls deliver())

//if we could, we should remove Process P, and just get ID, seriously...

We stopped following the interface and implemented it the following way.

Instead of having a ReliableBroadcaster, a FIFOBroadcaster, and so on, we decided to just have 1 universal Caster.

The Caster sends the message, with the ID of the sender, along with 

We kept BroadcastCount outside of the Caster class because of ReliableBroadcast and rebroadcasting same messages.


Issues:
================

ChatClient
----------------
//logis is that any message typed into Scanner will be delivered to the socket that the Client is currently connected to. If no sockets are connected, the chat client will just print "Unrecognized command!, Type ? to get a list of commands!"
1] Able to chat with yourself, not sure if should support this//for broadcasts, should remove self

MultiThreadedChatServer
----------------------- 
No issues so far..

ReliableBroadcast
------------------
According to the assignment, we're supposed to have a class that implements ReliableBroadcast that pushes notifications to the Client.

The ChatClient must implement the BroadcastReceiver interface, and should not poll the channel for messages. Instead, once the ReliableBroadcast object rbdelivers, the ChatClient calls the receive() method and displays it to the users.

Messages must be converted from String to Message objects...


public class ReliableBroadcaster implements ReliableBroadcast
{
	public void init(Process currentProcess, BroadcastReceiver br)
	{
		/*
		how do I init a process? What do you want me to initiailize?
		well since my ChatClient is both my process and receiver, there's no need for 2 separate parameters...
		but what do I initialize?
		well, what do I need in a Reliabelbroadcast
		I need a data structure, 
		*/
	}
	public void addMember(Process member)
	{
	}
	public void removeMember(Process member)
	{
	}
	public void rbroadcast(Message m)
	{
	}
}
public class BestEffortBroadcaster
{
	ChannelInterface channel;
	public BestEffortBroadcaster()
	{

	}
	public void bebroadcast(Message m)
	{
		for(int i = 0; i < )
	}
}

void receive message m()
{
}




* Well, we know that a ChatClient is a Process.

* Might need to change the parameters of ChannelInterface's methods to fit Message

BroadcastReceiver = receives broadcasts, makes sure that they are delivered according to specifications