import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class DummyClients {
	public static void main(String[] argv) {

		final String dummyName;
		try {
			String hostname;
			if (argv.length > 1) {
				hostname = argv[0];
				dummyName = argv[1];
			} else {
				hostname = "rmi://localhost/server";
				dummyName = "olo";
			}
			final ChatHandlerInterface server = (ChatHandlerInterface) Naming
					.lookup(hostname);
			long start = System.currentTimeMillis();
			long latency = 0;

			final int THREAD_NUMBER = 10;
			final int CLIENT_PER_THREAD = 100000/THREAD_NUMBER;
			
			final BlockingQueue<Long> queue = new ArrayBlockingQueue<Long>(THREAD_NUMBER);
			
			
			for (int i = 0; i < THREAD_NUMBER; i++) {
				final int d = i;
				new Thread(new Runnable() {
					Set<ChatHandlerInterface> dummies = new HashSet<ChatHandlerInterface>();
					public void run() {
						try {
							long total = 0;
							for (int j = d * CLIENT_PER_THREAD; j < (d + 1) * CLIENT_PER_THREAD; j++) {
								ChatHandlerInterface client = new ChatHandler(dummyName	+ ((d * CLIENT_PER_THREAD) + j));
								dummies.add(client);
								long start = System.currentTimeMillis();
								server.registerClientExperiment(client);
								long end = System.currentTimeMillis();
								total+= end-start;
							}
							queue.put(total);
							
							while (true) {
								long start = System.currentTimeMillis();
								for (ChatHandlerInterface c : dummies) {
									server.sendHeartBeat(c.getName());
								}
								long end = System.currentTimeMillis();
								Thread.sleep(Math.max(5000-end+start,0));
							}
						} catch (RemoteException | InterruptedException e) {
						
						} 
					}

				}).start();
				System.out.println("Thread " + i + " spinned");
			}
			
			
			long sum = 0;
			for(int i=0;i<THREAD_NUMBER;i++){
				sum += queue.take();
			}
			System.out.println(100000.0 / (System.currentTimeMillis() - start));
			System.out.println(sum / 100000.0);

		} catch (MalformedURLException | RemoteException | NotBoundException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
