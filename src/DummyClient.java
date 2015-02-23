/*
THINGS LEFT TO DO: MAKE IT WORK
*/
import java.net.UnknownHostException;

public class DummyClient implements Runnable
{
	static String name = "a";
	public static void main(String[] args)
	{
		for (int i = 0; i < 10; i++) {
			name+=i;
			System.out.printf("index:%d:%s",i,name);
			new Thread(new DummyClient()).start();
		name = "a";	
		}
	}
	public void run()
	{
		ChatClient dummyClient = new ChatClient("localhost", 5578, name);	
			//System.out.println(name);
		String[] sucks = {};
		try
		{
			dummyClient.main(sucks);
		}
		catch(UnknownHostException e)
		{
			System.out.println("er");
		}
		/*PrintWriter printer = new PrintWriter(new ,true)
		InputStream = new ByteArrayInputStream
		System.setin()
		System.out.println("localhost");
		System.out.println("65471");*/
	}
}