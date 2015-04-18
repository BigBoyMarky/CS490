import java.util.concurrent.ConcurrentHashMap;
public class VectorClock implements Serializable
{
	/*
	[a] {0,0,0}, {1,0,0}, {1,0,0}, {1,0,0}
	[b] {0,0,0}, {0,0,0}, {1,0,0}, {1,1,0}
	[c] {0,0,0}, {0,0,0}, {1,0,0}, {1,0,0}
	*/
	public boolean compare(VectorClock comparedClock)
	{
		//return true if 
		//return false if
	}
	public void increment()
	{
		//get largest
		//then increment the index
	}
}