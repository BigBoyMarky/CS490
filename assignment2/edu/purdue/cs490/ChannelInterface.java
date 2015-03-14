import java.net.SocketException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.net.UnknownHostException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.ServerSocket;
import java.io.InterruptedIOException;

public class ChannelInterface implements BroadcastReceiver
{
	private String host;
	private String name;//name of the Client
	private int serverPort;//port of the server it's going to connect to
	private int clientPort;//port of the client's ServerSocket for chatting with other clients
	private String ip;//ip of the client
	private ObjectOutputStream heart;//printer to server
	private PrintWriter printer;//printer to client
	private BufferedReader reader;//reader to client
	private Socket currentChatSocket;//the current Socket you're chatting in right now
	private boolean inChat;//once someone gets a message, they are forced in chat

	public ChannelInterface()
	{

	}
	public void initServer(String serverHost, String serverPort)
	{
		try
		{
			socket = new Socket(serverHost,serverPort);//creates socket to server
			heart = new ObjectOutputStream(socket.getOutputStream());//creates new oos
			heart.flush();//flushes header
		}
		catch(Exception e)
		{
			e.printStackTrace();//will fill it up later
		}
	}

	public void toServer(Object message)
	{
		try
		{
			heart.writeObject(message);
			heart.flush();			
		}
		catch(Exception e)
		{
			e.printStackTrace();//will fill it up later
		}
	}

	public void whisper(Object message)
	{

	}

	public void multicast(Object message)
	{

	}

	public void broadcast(Object message)
	{
		
	}	
}