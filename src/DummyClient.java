/*
THINGS LEFT TO DO: MAKE IT WORK
*/
import java.net.UnknownHostException;

public class DummyClient implements Runnable
{
	public static void main(String[] args)
	{
		try
		{
			ChatClient dummyClient = new ChatClient();
			new Thread(new DummyClient()).start();
			dummyClient.main(args);			
		}
		catch(UnknownHostException e)
		{
			System.out.println("errrr");
		}
	}
	public void run()
	{
		PrintWriter printer = new PrintWriter(new ,true)
		InputStream = new ByteArrayInputStream
		System.setin()
		System.out.println("localhost");
		System.out.println("65471");
	}
}