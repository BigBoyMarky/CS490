import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
/*
THINGS LEFT TO DO: MAKE IT WORK
*/
public class DummyClient implements Runnable
{
	//static variables for everyone
	static ExecutorService executor = Executors.newFixedThreadPool(50);
	static String ip;
	static int port = 0;
	static private final int NUMBER_OF_CLIENTS = 100000;
	volatile static DummyClient dummyArray[] = new DummyClient[NUMBER_OF_CLIENTS];
	private static long timeElapsed = 0;
	private static String firstName = "a";
	static long throughputSum = 0;
	//local variables
	private ChatClient client;
	private String name;
	private int n;
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
		for(int i = 0; i < dummyArray.length; i++)
			dummyArray[i] = new DummyClient(i);
		timeElapsed = System.currentTimeMillis();//starting time now
		for(int i = 0; i < dummyArray.length; i++)
			executor.execute(dummyArray[i]);
		try
		{
			executor.awaitTermination(1,TimeUnit.DAYS);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		timeElapsed = System.currentTimeMillis()-timeElapsed;
		System.out.printf("Throughput: %f processes per second\n ",(float)(NUMBER_OF_CLIENTS)/(timeElapsed*1000));
	}
	public void run()
	{
		throughputSum+=this.client.dummy(name,ip,port);
	}
	public DummyClient(int n)
	{
		this.n = n;
		name = firstName + n;
		this.client = new ChatClient();

	}
}
