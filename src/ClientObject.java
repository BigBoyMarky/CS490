import java.io.Serializable;
public class ClientObject implements Serializable
{
	private String username;
	private String ipAddress;
	private int port;
	public ClientObject(String username, String ipAddress, int port)
	{
		this.username = username;
		this.ipAddress = ipAddress;
		this.port = port;
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
}