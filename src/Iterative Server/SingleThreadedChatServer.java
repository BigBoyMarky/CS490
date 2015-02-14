import java.util.*;
import java.net.*;
import java.io.*;

	//Create a server based on command line input, DONE
	//server waits for clients to connect, DONE
	//once the clients connect, it sends acknowledgement back, adds clinet to a list.
		//it then sends an update to all previously connectd clients about the new user
	//it regularly checks for heartbeats. If heartbeat not received, it will assume it's dead and remove it from the list.
	//the client can initiate a chat with another client
public class SingleThreadedChatServer// implements Runnable
{
	private long heartbeat_rate = 4000;//in milliseconds
	ServerSocket serverSocket;
	static SingleThreadedChatServer server;
	public static void main(String[] args) throws IOException
	{
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
		server.runServer();
	}

	public SingleThreadedChatServer(int port) throws IOException
	{
		serverSocket = new ServerSocket(port);
		serverSocket.setReuseAddress(true);
	}

	public void runServer()
	{
		ArrayList<ClientObject> clientList = new ArrayList<ClientObject>();
		try
		{
			while(true)
			{
				Socket socket = serverSocket.accept();
				PrintWriter printer = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String message = reader.readLine();
				clientList.add(new ClientObject(message, request.getRemoteAddr(), socket, heartbeat_rate));
				//'0' = getList(); '1' = heartbeat; '2' = ;
/*				String message = reader.readLine();
				if(message.equals("0"))
					getList();
				if(message.equals("1"))
					verifyHeartbeat();
*/			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

}