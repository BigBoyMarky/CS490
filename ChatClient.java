import java.net.InetAddress;
import java.net.UnknownHostException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ChatClient {

	private Client client;
	
	public ChatClient(String name) throws UnknownHostException {

		client = new Client(name, InetAddress.getLocalHost().toString(), 123);

	}
	
}
