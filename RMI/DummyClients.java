import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;


public class DummyClients {
	public static void main(String[] argv){
		
		Set<ChatHandlerInterface> dummies = new HashSet<ChatHandlerInterface>();
		
		try {
			ChatHandlerInterface server = (ChatHandlerInterface)Naming.lookup("rmi://localhost/rmichat");
			long start = System.currentTimeMillis();
			long latency = 0;
			for(int i=0;i<100000;i++){
				//System.out.println(i);
				ChatHandlerInterface client = new ChatHandler("client"+ i);
				dummies.add(client);
				long start2 = System.currentTimeMillis();
				server.registerClient(client);
				latency += System.currentTimeMillis() - start2;
			}
			System.out.println(System.currentTimeMillis() - start);
			System.out.println(latency/100000);
			
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
