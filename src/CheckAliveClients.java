import java.util.ArrayList;


public class CheckAliveClients implements Runnable{
	private Thread t;
	private String threadName;
	private ArrayList<ClientObject> clientList;
	private long heartbeat_rate;
	
	public CheckAliveClients( String name, ArrayList<ClientObject> clientList, long heartbeat_rate ){
		threadName = name;
       	System.out.println("Creating " +  threadName );
       	this.clientList = clientList;
	}
	public void run() {
		System.out.println("Running " +  threadName );
		try {
			while(true){
				long currenttime = System.currentTimeMillis();
				for( ClientObject c : clientList ){
					if( currenttime - c.getHeartBeatTime() > heartbeat_rate ){
						clientList.remove(c);
					}
				}
				Thread.sleep(500);
			}
		}
		catch (InterruptedException e) {
	         System.out.println("Thread " +  threadName + " interrupted.");
	    }
	}
	   
	public void start ()
	{
		System.out.println("Starting " +  threadName );
	    if (t == null)
	    {
	       t = new Thread (this, threadName);
	       t.start ();
	    }
	}
}