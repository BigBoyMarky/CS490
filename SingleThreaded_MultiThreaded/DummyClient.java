import java.util.concurrent.*;

/*
THINGS LEFT TO DO: MAKE IT WORK
*/
public class DummyClient implements Runnable
{
	static ExecutorService executor = Executors.newFixedThreadPool(5);
	static String ip;
	private ChatClient client;
	static int port = 0;
	//volatile static ChatClient clientArray[] = new ChatClient[15];
	volatile static DummyClient dummyArray[] = new DummyClient[100];
	private int n;
	public static void main(String[] args)
	{
		try
		{
			ip = "localhost";
			port = Integer.parseInt(args[0]);
		}
		catch(Exception e)
		{
			System.out.println("Need to enter port of the server!");
			System.exit(0);
		}
		for(int i = 0; i < dummyArray.length; i++)
		{
			dummyArray[i] = new DummyClient(i);
			executor.execute(dummyArray[i]);
		}
		while(true)
		{
			for(int i = 0; i < dummyArray.length; i++)
			{
				executor.execute(dummyArray[i]);
			}
		}
	}
	public void run()
	{
		this.client.heartbeat(false);
	}
	public DummyClient(int n)
	{
		this.n = n;
		System.out.printf("%dth thread\n",n);
		String name = "user " + n;
		this.client = new ChatClient();		
		this.client.register(name,ip,port);			
	
	}
}
