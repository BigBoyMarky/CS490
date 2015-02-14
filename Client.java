import java.lang.*;

public class Client {

	private String name;
	private String ipAddress;
	private int port;
	private long heartbeattime;
	
	public Client (String n, String i, int p) {
		
		name = n;
		ipAddress = i;
		port = p;
		
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
	
}
