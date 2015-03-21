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
* Well, we know that a ChatClient is a Process.

* Might need to change the parameters of ChannelInterface's methods to fit Message

BroadcastReceiver = receives broadcasts, makes sure that they are delivered according to specifications