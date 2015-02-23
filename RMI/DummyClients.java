import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;


public class DummyClients {
	public static void main(String[] argv){
		
		Set<ChatHandlerInterface> dummies = new HashSet<ChatHandlerInterface>();
		String dummyname;
		try {
			String hostname;
			if(argv.length > 1){
				hostname = argv[0];
				dummyname = argv[1];
			}
			else{
				System.out.println("Please run DummyClients with 2 parameters: hostname and dummyname");
				return ;
				//hostname = "rmi://localhost/server";
			}
			ChatHandlerInterface server = (ChatHandlerInterface)Naming.lookup(hostname);
			long start = System.currentTimeMillis();
			long latency = 0;
			for(int i=0;i<2000;i++){
				//System.out.println(i);
				ChatHandlerInterface client = new ChatHandler(dummyname + i);
				dummies.add(client);
				long start2 = System.currentTimeMillis();
				server.registerClientExperiment(client);
				latency += System.currentTimeMillis() - start2;
				
				HeartbeatThread heart = new HeartbeatThread(server, dummyname + i);
				heart.start();
			}
			System.out.println(2000.0/(System.currentTimeMillis() - start));
			System.out.println(latency/2000.0);
			
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
