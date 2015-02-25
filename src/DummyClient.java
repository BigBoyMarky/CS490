/*
THINGS LEFT TO DO: MAKE IT WORK
*/
import java.net.UnknownHostException;

public class DummyClient implements Runnable
{
	static String name = "a";
	static int port = 0;
	public static void main(String[] args)
	{
		try
		{
			port = Integer.parseInt(args[0]);
		}
		catch(Exception e)
		{
			System.out.println("Need to enter port of the server!");
		}
		for (int i = 0; i < 10; i++) {
			name+=i;
			System.out.printf("index:%d:%s\n",i,name);
			try{Thread.sleep(100);}catch(InterruptedException e) {Thread.currentThread().interrupt();}
			new Thread(new DummyClient()).start();
			name = "a";	
		}
	}
	public void run()
	{
		ChatClient dummyClient = new ChatClient();	
		dummyClient.register(name, "localhost", port);
			//System.out.println(name);
		/*PrintWriter printer = new PrintWriter(new ,true)
		InputStream = new ByteArrayInputStream
		System.setin()
		System.out.println("localhost");
		System.out.println("65471");*/
	}
}