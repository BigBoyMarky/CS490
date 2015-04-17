import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.Set;
import java.util.HashSet;

public class VectorClock {

    private ConcurrentMap<String, Integer> clock;

    public VectorClock(ArrayList<String> processes){
        // create an empty clock with those process names
        this.clock = new ConcurrentHashMap<String, Integer>();
        for(String name : processes){
            clock.put(name, 0);
        }
    }

    public VectorClock(VectorClock old, String name){
        // create a clock that chronologically follows the old clock
        this.clock = new ConcurrentHashMap<String, Integer>();
        for(String s : old.getClock().keySet()){
            this.clock.put(s, old.getTime(s));
        }
        this.clock.put(name, this.clock.get(name)+1);
    }

    public void set(String name, int time){
        this.clock.put(name,time);
    }

    public ConcurrentMap<String, Integer> getClock(){
        return this.clock;
    }

    public int getTime(String name){
        if(!this.clock.containsKey(name)){
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

    public static void main(String[] args){

        //Unit test

        ArrayList<String> a = new ArrayList<String>();
        a.add("A");
        a.add("B");
        a.add("C");

        ArrayList<String> b = new ArrayList<String>();
        b.add("D");

        VectorClock v1 = new VectorClock(a);
        VectorClock v2 = new VectorClock(b);

        VectorClock v15 = new VectorClock(v1, "C");
        System.out.println(v15.isBefore(v2));


        for(String s: v15.getClock().keySet()){
            System.out.println(s + " " + v15.getTime(s));
        }

        System.out.println();

        for(String s: v2.getClock().keySet()){
            System.out.println(s + " " + v2.getTime(s));
        }

    }
}
