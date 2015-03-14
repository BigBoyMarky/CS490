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

public class ChannelInterface
{
	private String host;
	private String name;//name of the Client
	private int serverPort;//port of the server it's going to connect to
	private int clientPort;//port of the client's ServerSocket for chatting with other clients
	private String ip;//ip of the client
	private ObjectOutputStream heart;//printer to server
	private ObjectInputStream heartListener;
	private Serversocket serverSocket;
	private PrintWriter printer;//printer to client
	private BufferedReader reader;//reader to client
	private Socket currentChatSocket;//the current Socket you're chatting in right now
	private boolean inChat;//once someone gets a message, they are forced in chat
	private ArrayList<Socket> socketList = new ArrayList<Socket>();
	private ArrayList<ObjectOutputStream> oosList = new ArrayList<ObjectOutputStream>();
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
			heartListener = new ObjectInputStream(socket.getInputStream());//creates new ois

		}
		catch(Exception e)
		{
			e.printStackTrace();//will fill it up later
		}
	}
	public void closeServer()
	{
		heart.writeObject("Close");
		heart.flush();
		socket.close();
		heart.close();
		heartListener.close();
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
	public Object fromServer()//how object is interpreted is based on guy whose calling this
	{
		try
		{
			return heartListener.readObject();
		}
		catch(Exception e)
		{
			e.printStackTrace();//will fill it up later
		}
	}
	public void initClient(ClientObject interlocutor)
	{
		Socket clientSocket = new Socket(interlocutor.getIpAddress(), interlocutor.getPort());
		ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
		ObjectInputStream oos = new ObjectOutputStream(socket.getOutputStream());
		oos.flush();
		interlocutor = new ClientObject(interlocutor,clientSocket,ois,oos);
		System.out.printf("Chatting with %s\n",interlocutor.getName());
	}
	public void whisper(ClientObject interlocutor, Object message)
	{
		if(!interlocutor.getInitState())
			initClient(interlocutor);
		interlocutor.getOut().writeObject(message);
	}
}
