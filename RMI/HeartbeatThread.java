import java.rmi.RemoteException;

public class HeartbeatThread extends Thread{
	private ChatHandlerInterface server;
	private String name;
	public void run(){
		while(true){
			try {
				server.sendHeartBeat(name);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public HeartbeatThread(ChatHandlerInterface server,String name){
		this.server = server;
		this.name = name;
	}
}
