import java.io.Serializable;
public class ClientObject implements Serializable
{
	private String username;
	private String ipAddress;
	private int port;
	private long heartbeat;
	public ClientObject(String username, String ipAddress, int port)
	{
		this.username = username;
		this.ipAddress = ipAddress;
		this.port = port;
	}
	public ClientObject(ClientObject objectToCopy)
	{
		this.username = objectToCopy.getName();
		this.ipAddress = objectToCopy.getIpAddress();
		this.port = objectToCopy.getPort();
	}
	public String getName()
	{
		return username;
	}
	public String getIpAddress()
	{
		return ipAddress;
	}
	public int getPort()
	{
		return port;
	}
	public long getHeart()
	{
		return heartbeat;
	}
	public void updateHeart(long timeStamp)
	{
		heartbeat = timeStamp;
	}
}