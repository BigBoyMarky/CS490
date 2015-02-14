import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
	private int port;
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
		this.port = port;
		serverSocket = new ServerSocket(port);
		serverSocket.setReuseAddress(true);
	}

	public void runServer()
	{
		ArrayList<ClientObject> clientList = new ArrayList<ClientObject>();
		CheckAliveClients heartbeatcheck = new CheckAliveClients( "check thread", clientList, heartbeat_rate );
		heartbeatcheck.start();
		
		try
		{
			while(true)
			{
				Socket socket = serverSocket.accept();
				PrintWriter printer = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String message = reader.readLine();
				
				
				//'0' = getList(); '1' = heartbeat; '2' = ;
				
				// reg_name_ip_port
				
				Pattern p = Pattern.compile("reg_(.)+_(.)+");
				Matcher m = p.matcher(message);
				boolean regMessage = m.matches();
				
				if( regMessage ){
					String[] info = message.split("_");
					clientList.add(new ClientObject(info[0], info[1], this.port, heartbeat_rate, socket));
					printer.println("created!");
					printer.flush();
				}
				
				if(message.equals("0")){
					sendList(socket, clientList);
				}
				if(message.equals("1")){
					putHeartbeat(socket, clientList);
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
	
	public void sendList(Socket socket, ArrayList<ClientObject> clientList){
		
		SocketAddress addr = socket.getRemoteSocketAddress();
		for( ClientObject c: clientList ){
			if(addr.equals(c.getSocket().getRemoteSocketAddress())){
				// SERIALIZE THE ARRAYLIST
				break;
			}
		}
	}
	
	public void putHeartbeat(Socket socket, ArrayList<ClientObject> clientList){
		long currenttime = System.currentTimeMillis();
		SocketAddress addr = socket.getRemoteSocketAddress();
		for( ClientObject c: clientList ){
			if(addr.equals(c.getSocket().getRemoteSocketAddress())){
				if( currenttime - c.getLastBeat() <= heartbeat_rate ){
					c.setHeartBeatTime(System.currentTimeMillis());
				}
				break;
			}
		}
	}

}



