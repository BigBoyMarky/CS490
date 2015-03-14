public class Receiver implements BroadcastReceiver
{
	private ObjectInputStream heartListener;//reader to server
	private ServerSocket serverSocket;//for connecting to other users directly???
	private Socket socket;//socket for connecting purposes

	public Receiver()
	{

	}
	public void initServer(Socket socket)
	{
		heartListener = new ObjectInputStream(socket.getInputStream());//creates new ois		
	}
}