import java.net.InetAddress;
import java.net.UnknownHostException;

public class ChatClient {

	private Client client;
	
	public ChatClient(String name) throws UnknownHostException {

		client = new Client(name, InetAddress.getLocalHost().toString(), 123);

	}
	
}
