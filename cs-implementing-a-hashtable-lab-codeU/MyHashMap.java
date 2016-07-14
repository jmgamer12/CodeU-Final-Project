/**
 * 
 */
package com.flatironschool.javacs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Implementation of a HashMap using a collection of MyLinearMap and
 * resizing when there are too many entries.
 * 
 * @author downey
 * @param <K>
 * @param <V>
 *
 */
public class MyHashMap<K, V> extends MyBetterMap<K, V> implements Map<K, V> {
	
	// average number of entries per map before we rehash
	protected static final double FACTOR = 1.0;

	@Override
	public V put(K key, V value) {
		V oldValue = super.put(key, value);
		
		//System.out.println("Put " + key + " in " + map + " size now " + map.size());
		
		// check if the number of elements per map exceeds the threshold
		if (size() > maps.size() * FACTOR) {
			rehash();
		}
		return oldValue;
	}

	/**
	 * Doubles the number of maps and rehashes the existing entries.
	 */
	/**
	 * 
	 */
	protected void rehash() {
		List<Map.Entry<K, V>> entry_list = new ArrayList<Map.Entry<K, V>>();
		int new_size = maps.size() * 2;
		int old_size = maps.size();
		for (int i=0; i<old_size; i++){
			MyLinearMap<K, V> sub_map = maps.get(i);
			entry_list.addAll(sub_map.getEntries());		
		}
		makeMaps(new_size);
		// int k = ( new_size / entry_list.size());
	
		for (Map.Entry<K, V> entry: entry_list){
			put(entry.getKey(),entry.getValue());	
			
		}
        
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Map<String, Integer> map = new MyHashMap<String, Integer>();
		for (int i=0; i<10; i++) {
			map.put(new Integer(i).toString(), i);
		}
		Integer value = map.get("3");
		System.out.println(value);
	}
}
