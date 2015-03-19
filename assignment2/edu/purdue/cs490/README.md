Fixed Log
=================
Fixed Log is transferred to the commit messages every commit.

Issues:
================

ChatClient
----------------
//logis is that any message typed into Scanner will be delivered to the socket that the Client is currently connected to. If no sockets are connected, the chat client will just print "Unrecognized command!, Type ? to get a list of commands!"
1] Client to Server connection is not robust, if the server terminates, the ChatClient keeps repeating (java.net.SocketException: connection reset). What SHOULD happen is if the server disconnects, the client notifies the user about it (e.g. "The main server has disconnected; however, the other clients are still online. What this means is that we can no longer add new people to the chatroom, but you can still continue to chat with those that are already in. We suggest you to search for a new server."). Exception is thrown @ line 69 from ChannelInterface, @ line 138 from ChatClient
2] Able to chat with yourself, not sure if should support this//for broadcasts, should remove self
3] Magic number on line 133 in ChannelInterface, remove all the magic numbers!
4] Chatting with other clients, upon disconnecting, nothing is done.
5] What happens if cannot connect to server bc invalid inputs?


MultiThreadedChatServer
----------------------- 
No issues so far..

ReliableBroadcast
------------------
* Well, we know that a ChatClient is a Process.

* Might need to change the parameters of ChannelInterface's methods to fit Message

BroadcastReceiver = receives broadcasts, makes sure that they are delivered according to specifications