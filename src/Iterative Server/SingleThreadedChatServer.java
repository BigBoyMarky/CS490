import java.util.*;
import java.net.*;
import java.io.*;

public class SingleThreadedChatServer
{
	static SingleThreadedChatServer server;
	static String serverName;
	ServerSocket serverSocket;
	public static void main(String[] args) throws IOException
	{
		//allows the creator of the server to assign a name and a port to the server
		if(args.length > 0)
		{
			try
			{
				int port = Integer.parseInt(args[0]);
				if(port >= 1025  && port <= 65535)
				{
					server = new SingleThreadedChatServer(port);
				}
				else
				{
					System.out.println("Port is out of range. Try a port between 1025 and 65535. The program will end.");
					return;
				}
			}
			catch(Exception e)
			{
					System.out.println("Invalid input. Please specify a port using only integers between 1025 and 65535.");
			}
		}
		else
		{
			server = new SingleThreadedChatServer(0);//if none specified, uses 0, which locates one automatically
			System.out.println("Port was not specified. Using free port " + server.serverSocket.getLocalPort());
		}
		server.runServer();
	}

    public SafeWalkServer(int port) throws IOException 
    {
        serverSocket = new ServerSocket(port);
        serverSocket.setReuseAddress(true);
    }

    public void runServer()
    {
    	
    }
}