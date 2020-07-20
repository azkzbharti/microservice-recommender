package com.ibm.research.msr.db.queries.partition;
import java.util.HashMap;
import java.util.Map;

public class Counter<T> {
    final Map<T, Integer> counts = new HashMap<>();

    public void add(T t) {
        counts.merge(t, 1, Integer::sum);
    }

    public int count(T t) {
        return counts.getOrDefault(t, 0);
    }
    
    
    public Map<T, Integer> get_counter(){
    	
    	return counts;
    	
    }
}