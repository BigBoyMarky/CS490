public class FIFOReliableBroadCast{
    public void init(Process currentProcess, BroadcastReceiver br);
    public void addMember(Process member);
    public void removeMember(Process member);
    public void FIFObroadcast(Message m) ;
}
