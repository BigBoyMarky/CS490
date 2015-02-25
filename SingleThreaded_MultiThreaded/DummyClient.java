/*
THINGS LEFT TO DO: MAKE IT WORK
*/
public class DummyClient implements Runnable
{
	static String ip;
	static int port = 0;
	volatile static ChatClient clientArray[] = new ChatClient[100000];	
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
		for(int i = 0; i < clientArray.length; i++)
		{
			try
			{
				Thread.sleep(1000);//to prevent i from being blocked constantly				
			}
			catch(Exception e)
			{
				System.out.println("Thread was interrupted derr");
			}
			new Thread(new DummyClient(i)).start();
		}
	}
	public void run()
	{
		try
		{
			System.out.printf("%dnth thread\n",n);
			String name = "user " + n;
			clientArray[n] = new ChatClient();		
			clientArray[n].register(name,ip,port);			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	public DummyClient(int n)
	{
		this.n = n;
	}
}