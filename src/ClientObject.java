import java.lang.*;
import java.net.Socket;
import java.util.*;

public class ClientObject {

	private String name;
	private String ipAddress;
	private int port;
	private long heartbeat_rate;
	private final Socket socket;
	private long lastbeat;
	
	public ClientObject (String n, String i, int p, long h, Socket s) {
		name = n;
		ipAddress = i;
		port = p;
		heartbeat_rate = h;
		socket = s;
		lastbeat = System.currentTimeMillis();
		//(new Thread(this)).start();
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
	
	public Socket getSocket(){
		return socket;
	}
	
	public void setHeartBeatTime(long heartbeattime) {
		this.lastbeat = heartbeattime;
		
	}
	
	public long getLastBeat(){
		return lastbeat;
	}
	
}
