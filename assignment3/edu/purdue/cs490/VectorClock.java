package edu.purdue.cs490;
import java.util.List;
import java.util.ArrayList;
import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

public class VectorClock implements Serializable
{
    private ConcurrentHashMap<String, Integer> clock;

    public VectorClock(ConcurrentHashMap<String, ClientObject> listOfUsers)
    {
        // create an empty clock with those process names
        Iterator availableUsers = listOfUsers.entrySet().iterator();
        while(availableUsers.hasNext())
        {
            Map.Entry pair = (Map.Entry)availableUsers.next();
            clock.put((String)pair.getKey(),0);
        }
    }
    // 0,0,0

    public VectorClock(VectorClock old, String name)
    {
        // create a clock that chronologically follows the old clock
        this.clock = new ConcurrentHashMap<String, Integer>();
        for(String s : old.getClock().keySet()){
            this.clock.put(s, old.getTime(s));
        }
        this.clock.put(name, this.clock.get(name)+1);
    }

    public VectorClock(VectorClock v1, VectorClock v2, String name)
    {
        this.clock = new ConcurrentHashMap<String, Integer>();
        Set<String> allkeys = union(v1.getClock().keySet(), v2.getClock().keySet());
        for(String s : allkeys){
            this.clock.put(s, Math.max(v1.getTime(s),v2.getTime(s)));
        }
        this.clock.put(name, this.clock.get(name)+1);
    }

    public void set(String name, int time)
    {
        this.clock.put(name,time);
    }

    public void remove(String name)
    {
        this.clock.remove(name);
    }

    public ConcurrentHashMap<String, Integer> getClock()
    {
        return this.clock;
    }

    public int getTime(String name)
    {
        if(!this.clock.containsKey(name))
        {
            this.set(name,0);
        }
        return this.clock.get(name);
    }

    public Set<String> union(Set<String> l1, Set<String> l2){
        Set<String> set = new HashSet<String>();
        set.addAll(l1);
        set.addAll(l2);
        return set;
    }

    public boolean isBefore(VectorClock another){
        // return whetehr this clock comes before another clock
        boolean ret = true;
        Set<String> allkeys = union(this.clock.keySet(), another.getClock().keySet());

        for(String name : allkeys){
            if(this.getTime(name) > another.getTime(name)){
                ret = false;
            }
        }
        return ret;
    }
    public VectorClock incrementVectorClock(String sender)
    {
        //increments it by the protocol which is, it's ID along with
        this.set(sender,this.getTime(sender)+1);
        return null;
    }

/*
	public boolean compare(VectorClock comparedClock)
	{
		//return true if 
		//return false if
	}
	public void increment()
	{
		//get largest
		//then increment the index
	}*/
    public static void main(String[] args){

        //Unit test
/*
        ArrayList<String> a = new ArrayList<String>();
        a.add("A");
        a.add("B");
        a.add("C");

        ArrayList<String> b = new ArrayList<String>();
        b.add("D");

        VectorClock v1 = new VectorClock(a);
        VectorClock v2 = new VectorClock(b);

        VectorClock v15 = new VectorClock(v1, "C");
        System.out.println(v2.isBefore(v15));

        VectorClock v4 = new VectorClock(v15, v2, "A");
        VectorClock v5 = new VectorClock(v15, v4, "C");


        for(String s: v4.getClock().keySet()){
            System.out.println(s + " " + v4.getTime(s));
        }

        System.out.println();

        for(String s: v5.getClock().keySet()){
            System.out.println(s + " " + v5.getTime(s));
        }*/


    }
}
