import java.lang.*;

public class ClientObject implements Runnable {

	private String name;
	private String ipAddress;
	private int port;
	private long heartbeat_rate;
	
	public ClientObject (String n, String i, int p, long h) {
		name = n;
		ipAddress = i;
		port = p;
		heartbeat_rate = h;
		(new Thread(this)).start();
	}
	
	public String getName() {
		return name;
	}
	
	public String getIP() {
		
		return ipAddress;
		
	}
	
	public int getPort() {
		
		return port;
		
	}
	
	public long getHeartBeatTime() {
		
		return heartbeattime;
		
	}
	
	public String heartbeat() {
		
		heartbeattime = System.currentTimeMillis();
		return "heartbeat";
				
	}
	
	public void run()
	{
		do
		{
			this.sleep(heartbeat_rate);
		} while(gotMessage);

	}
}
