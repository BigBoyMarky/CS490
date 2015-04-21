###CAUSAL RELIABLE BROADCAST

####Members
- Charlie Su
- Gott Phusit
- Mark Yang

How to Make
============
Stay in this directory, and type in "make" into the command line, because Unix.

How to Run
============

MultiThreadedChatServer
-----------------------
Call "java edu/purdue/cs490/MultiThreadedChatServer"
That's it. If you want to specify a port number, you can specify it as an argument (e.g. "java MultiThreadedChatServer 4200") in the command line. The server will print out its port and IPv4 Address so you can connect clients to it from other computers.

ChatClient
-----------
Call "java edu/purdue/cs490/ChatClient". 
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
4] Type in '\beb' to be broadcast.
5] Type in '\rb' to reliable broadcast.
6] Type in '\fifo' to fifo broadcast.
7] Type in '\causal' to causal broadcast.
8] Type in '\OVER9000' to send 10,000 messages and get throughput
 
How to test
-----------
1] when every client is up and ready, make sure you do '\list' for every client.
2] For each client, call '\OVER9000'.
3] When 100,000 messages are received, the throughput will be printed.
4] If you want to know the average throughput when you don't have 100,000 messages, do '\print'