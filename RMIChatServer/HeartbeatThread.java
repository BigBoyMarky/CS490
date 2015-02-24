import java.rmi.RemoteException;

public class HeartbeatThread extends Thread{
	private ChatHandlerInterface server;
	private String name;
	private Boolean alive;
	public void run(){
		while(alive){
			try {
				server.sendHeartBeat(name);
				Thread.sleep(5000);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void kill(){
		alive = false;
	}
	
	public HeartbeatThread(ChatHandlerInterface server,String name){
		this.server = server;
		this.name = name;
		alive = true;
	}
}
