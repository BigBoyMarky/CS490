//Woah woah woah. Is this really what engineering is? We learn the ultimate power of creation and stuff, but in the end all we really are
//are just laborers that build other people's dreams for them? We have to deal with overly nitpicky customers who don't even have
//a clue of what they want with their unreasonable demands? What happened to making things because we were passionate about it?
//What happened to creating NEW technology that hasn't existed yet? What happened to going above and beyond and laughing at "just good enough"?
//Is this what we're getting trained to be? Laborers who will listen to their bosses demands? Please, please tell me, what is teh
//major that gets to DREAM and build their own dreams?

//Seriously...as people with the ultimate power of creations, we should be the most CAPABLE people in the world, not only in building
//things, but also DREAMING up of things. We should be learning how to break down a seemingly impossible problem, we should be learning
//about teamwork, we should be trained, not just hah be content with good enough, in fact you guys make it feel like going above and beyond
//is a scary thing to do, No we should be trained to always go above and beyond. We shouldn't cower to others, we should be GLAD
//to help others. The world needs to advanced forward and become a meritocracy. We are the greatest thinkers in the world, the
//smartest and brightest, and yet we're the ones serving those who can't even do fucking Calculus. Fuck this shit man.

//Choosing an API over your own methods is fine, but choosing an API over your own methods "because that is what real software engineers do"
//is complete bullshit. If your method is as efficient as a popular API's implementation, then there's no fucking reason to use the API's method
//Unless of course, you're one of those people who can't actually analyze the efficiency of their own code..

//"Using tried and true API's is what makrs a true software engineer"
//"Haha, that's funny. Anyone can mix and match API's. That's not true engineering. It's like saying buying parts and building your own"
//"desktop computer is engineering! REAL engineering is MAKING the new parts that opens up new boundaries in our technology,
//PSEUDO engineering is using existing parts to build something"

//After I took my first engineering class, I stopped taking them. I didn't like their way of thinking. They taught about industry
//standards and how this is the "right" way of doing it without explaining why. Where's that fearless sense of inquiry that begets
//the question: "WHY is it considered the 'right' way to do it? And if it's already pretty good, how can we improve on it?"

//You want to build something? 
//Just doing "Good enough" is not innovative. "Good enough" will be "good enough", but it will never ever scrape the edge of our
//technological boundaries. When I hear engineers spouting that our goal is to be "good enough", I feel that they have lost their
//sense of idealism, their sense of drive and innovation. That isn't the attitude of innovators that envision new technology, that's
//the attitude of manual laborers drafted to build someone else's visions for them.
//Pam Graf LWSN 1151, Mail/Copy Room for Key Fob//pgraf@purdue.edu//oatmeal + honey + bananas + grain/oats + (soy)milk

import java.util.*;
import java.net.*;
import java.io.*;

//Create a server based on command line input, DONE
//server waits for clients to connect, DONE
//once the clients connect, it sends acknowledgement back, adds clinet to a list.
//it then sends an update to all previously connectd clients about the new user
//it regularly checks for heartbeats. If heartbeat not received, it will assume it's dead and remove it from the list.
//the client can initiate a chat with another client

public class SingleThreadedChatServer implements Runnable
{
	/**************************************************************************************************
	*											FIELDS												*
	**************************************************************************************************/	
	private long heartbeat_rate = 4000;//in milliseconds
	ServerSocket serverSocket;
	static SingleThreadedChatServer server;
	private int port;//port
	private int numClients = 0;//keeps track of number of clients for ID'ing purposes
	static ArrayList<ClientObject> clientList = new ArrayList<ClientObject>();//the lists = the group G
	static ArrayList<Socket> socketList = new ArrayList<Socket>();//socket lists for accessing them later, e.g. when a client requests the list of group G

	/**************************************************************************************************
	*											MAIN METHOD											*
	**************************************************************************************************/
	public static void main(String[] args) throws IOException
	{
		//initialization
		if(args.length > 0)
		{
			try
			{
				int port = Integer.parseInt(args[0]);
				if(port >= 1025 && port <= 65535)
					server = new SingleThreadedChatServer(port);
				else
				{
					System.out.println("Port is out of range. Try a port between 1025 and 65535. This program will close.");
					return;
				}
			}
			catch(Exception e)
			{
					System.out.println("Please enter valid input. We want integers only!");
			}
		}
		else
		{
			server = new SingleThreadedChatServer(0);//if none was specified, uses 0, which locates default			
			System.out.println("Port was not specified. Using free port " + server.serverSocket.getLocalPort());
		}
		System.out.println("Server Host Name:" + InetAddress.getLocalHost().getHostAddress());
		server.runServer();
	}
	/**************************************************************************************************
	*											CONSTRUCTOR												*
	**************************************************************************************************/
	public SingleThreadedChatServer(int port) throws IOException
	{
		this.port = port;
		serverSocket = new ServerSocket(port);
		serverSocket.setReuseAddress(true);
	}

	/**************************************************************************************************
	*										ClientObject INNER CLASS								*
	**************************************************************************************************/
	public class ClientObject implements Runnable
	{
				/****************************************************************************
				*							ClientObject's FIELDS							*
				*****************************************************************************/
		private String name;
		private String address;
		private int port;
		private int id;//for tracking and management in the server
		private boolean alive;//whether or not this guy is alive
				/****************************************************************************
				*							ClientObject's CONSTRUCTOR						*
				*****************************************************************************/		
		public ClientObject(String name, String address,int port, int id)
		{
			this.name = name;
			this.address = address;
			this.port = port;
			this.id = id;
			alive = true;
		}
	/**************************************************************************************************
	*											heartbeats 											*
	**************************************************************************************************/			
		public void run()
		{
			try
			{
				BufferedReader reader = new BufferedReader(new InputStreamReader(socketList.get(this.id).getInputStream()));
				while(this.alive)
				{
					//flips switch off, waits for heartbeat, if receive a message, flips switch back on. Otherwise finish execution
					this.alive = false;
					Thread.sleep(heartbeat_rate);//in essence our timer
					String message;
					if((message=reader.readLine()) != null)//if not null, message received
						this.alive = true;//flips switch on
					if(message.equals("get"))//if message was "get", sendList()
						sendList();
				}
				//remove from list
				clientList.remove(this.id);
				//thread is done executing and therefore dies		
			}
			catch(Exception e)
			{
				e.printStackTrace();
				//could either be IOException or InterruptedException
			}
		}
	/**************************************************************************************************
	*											SEND LIST											*
	**************************************************************************************************/
		public void sendList()
		{
			try
			{
				PrintWriter printer = new PrintWriter(socketList.get(this.id).getOutputStream(),true);//creates a new PrintWriter object to service this specific socket
				for(int i= 0; i < clientList.size(); i++)//prints out all clients on list
					printer.println(clientList.get(i).serialize());
				printer.println("\\0");//null terminator to tell the guy reading the list that's it's done and no more messages not bc of latency
			}
			catch(IOException e)
			{
				System.err.println(e);
			}
		}		
	/**************************************************************************************************
	*										SERIALIZING	CLIENTOBJECT								*
	**************************************************************************************************/	
		public String serialize()
		{
			return (this.port + " " + this.name);//serializing is just turning it into a string :P
		}

		public String getName()
		{
			return (this.name);
		}
	}
	/**************************************************************************************************
	*									END OF ClientObject INNER CLASS								*
	**************************************************************************************************/	


	/*
	*/

	/**************************************************************************************************
	*								SEPARATE THREAD TO CONNECT NEW CLIENTS							*
	**************************************************************************************************/	
	public void run()
	{
		try
		{
			while(true)
			{
				Socket socket = serverSocket.accept();
				socketList.add(socket);
				++numClients;
			}

		}
		catch(Exception e)
		{
			//Sockets
		}
	}

	/**************************************************************************************************
	*										RECEIVE/PROCESS MESSAGES								*
	**************************************************************************************************/	
	public void runServer()
	{
		try
		{
			PrintWriter printer;
			BufferedReader reader;
			String message = "";
			ArrayList<Long> heartList = new ArrayList<Long>();//for checking the time passed for heartbeats
			ArrayList<Socket> socketList = new ArrayList<Socket>();//for connecting with the socket for communication
			ArrayList<String> nameList = new ArrayList<String>();//for names
			ArrayList<String> ipList = new ArrayList<String>();//for ip
			ArrayList<Integer> portList = new ArrayList<Integer>();//for port
			new Thread().start();//for receiving connections
			while(true)
			{
				for(int i = 0; i < numClients; i++)//go through all sockets
				{
					reader = new BufferedReader(new InputStreamReader(socketList.get(i).getInputStream()));
					message = reader.readLine();
					if(message.substring(0,1).equals("R"))//for registration
					{
						printer = new PrintWriter(socketList.get(i).getOutputStream(),true);
						String name = message.substring(1,14).trim();//whatever
						if(nameList.contains(name))
						{							
							printer.println("U");
							socketList.remove(i);
							--numClients;
							--i;
							continue;
						}
						int spacePosition = message.indexOf(" ",14);
						String address = message.substring(14,spacePosition);
						spacePosition = message.indexOf(" ", spacePosition+1);
						int port = Integer.parseInt(message.substring(spacePosition,message.length()));
						heartList.add(System.currentTimeMillis());
						nameList.add(name);
						ipList.add(address);
						portList.add(port);
					}
					else if(message.equals("get"))
					{
						printer = new PrintWriter(socketList.get(i).getOutputStream(),true);
						for(int j = 0; j < numClients; j++)
							printer.println(nameList.get(j) + "," + ipList.get(j) + "," + portList.get(j));
					}
					else if(message.equals("<3"))
					{
						//update heartbeat time
						heartList.set(i, System.currentTimeMillis());
					}
					else if(System.currentTimeMillis()-heartList.get(i) > heartbeat_rate)
					{
						//NOTE: if server lags, then this solution will just kill everyone
						//terminate
						//send message
						printer = new PrintWriter(socketList.get(i).getOutputStream(),true);
						printer.println("You have been terminated! Enjoy the rest of your lonely existence");
						socketList.remove(i);
						--numClients;
						--i;
					}
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Will be more specific about exceptions later");
		}
	}
}