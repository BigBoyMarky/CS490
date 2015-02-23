
import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.server.*;
import java.util.*;

public class ChatClient {

	public static void main(String[] argv) {
		try {
			System.setSecurityManager(new RMISecurityManager());
			Scanner s = new Scanner(System.in);
			System.out.println("Enter your name: ");
			String name = s.nextLine().trim();
			ChatHandlerInterface client = new ChatHandler(name);

			ChatHandlerInterface server;
			
			String hostname;
			
			if(argv.length > 0)
				hostname = argv[0];
				
			else
				hostname = "rmi://localhost/server";
				
			server = (ChatHandlerInterface) Naming
					.lookup(hostname);

			server.registerClient(client);
			String msg = "[System] " + client.getName() + " is online";
			server.send(msg);

			System.out
					.println("[System] To start chatting, type :1 follow by space and the name of the person you want to chat to i.e. :1 Gott");
			System.out
					.println("[System] To start chatting, type :2 to stop sending message to your current version.");
			System.out
					.println("[System] To start chatting, type :3 to show the online users.");

			ChatHandlerInterface target = server;

			HeartbeatThread heart = new HeartbeatThread(server, name);
			heart.start();

			String targetName = null;

			while (true) {
				try {
					msg = s.nextLine().trim();

					/*
					 * type :1 <name> to chat another person type :2 to cancel
					 * type :3 to get the list
					 */

					if (msg.indexOf(':') == 0) {

						if (msg.indexOf('1') == 1) {

							targetName = msg.split(" ")[1];

							target = server.startChat(targetName);

							if (target != null) {
								System.out
										.println("[System] Start sending message to "
												+ targetName);
							} else {
								System.out.println("[System] Error: "
										+ targetName
										+ " is either offline or not found.");
								target = server;
							}
						}

						else if (msg.indexOf('2') == 1) {

							target = server;
						}

						else if (msg.indexOf('3') == 1) {

							server.getList(client);
						}

						else
							target.send("[" + name + "] " + msg);
					}

					else {
						if (target != server && server.isAlive(targetName)) {
							target.send("[" + name + "] " + msg);
						} else if (target != server) {
							target = server;
							System.out.println("[System] " + targetName
									+ " is offline. Closing chat session.");
						} else
							target.send("[" + name + "] " + msg);
					}
				} catch (Exception e) {
					System.err.println(e);
				}

			}

		} catch (RemoteException | MalformedURLException | NotBoundException e) {
			System.out.println("[System] Failed " + e);
		}
	}

}