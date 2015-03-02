import java.util.concurrent.*;

/*
THINGS LEFT TO DO: MAKE IT WORK
*/
public class DummyClient implements Runnable
{
	static ExecutorService executor = Executors.newFixedThreadPool(50);
	static String ip;
	private ChatClient client;
	static int port = 0;
	//volatile static ChatClient clientArray[] = new ChatClient[15];
	volatile static DummyClient dummyArray[] = new DummyClient[100000];
	private int n;
	private static long timeElapsed = 0;
	private long heartbeatKeep = 0;
	private static String firstName = "a";
	public static void main(String[] args)
	{
		try
		{
			firstName = args[0];
			ip = args[1];
			port = Integer.parseInt(args[2]);
		}
		catch(Exception e)
		{
			System.out.println("Enter:[name][server ip][server port]");
			System.exit(0);
		}
		//Start time
		timeElapsed = System.currentTimeMillis();
		int j = 0;
		int k = 0;
		for(int i = 0; i < dummyArray.length; i++)
		{
			dummyArray[i] = new DummyClient(i);
			executor.execute(dummyArray[i]);
			while(j < i)
			{
				executor.execute(dummyArray[j]);
				j++;
				k++;
				if(j == i)
					j = 0;
				if(k >= 25)
				{
					k= 0;
					break;
				}

			}
		}
		timeElapsed = System.currentTimeMillis()-timeElapsed;
		System.out.printf("Time taken:%d\n",timeElapsed);//for calculating throughput I guess
	//	while(true)
	//	{
	//		for(int i = 0; i < dummyArray.length; i++)
	//		{
	//			executor.execute(dummyArray[i]);
	//		}
	//	}
	}
	public void run()
	{
		this.client.heartbeat(false);
	}
	public DummyClient(int n)
	{
		this.n = n;
		System.out.printf("%dth thread\n",n);
		String name = firstName + n;
		this.client = new ChatClient();		
		this.client.register(name,ip,port);			
	
	}
}
