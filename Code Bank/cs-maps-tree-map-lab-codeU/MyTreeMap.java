/**
 * 
 */
package com.flatironschool.javacs;

import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import static java. lang. Math.*;

/**
 * Implementation of a Map using a binary search tree.
 * 
 * @param <K>
 * @param <V>
 *
 */
public class MyTreeMap<K, V> implements Map<K, V> {

	private int size = 0;
	private Node root = null;

	/**
	 * Represents a node in the tree.
	 *
	 */
	protected class Node {
		public K key;
		public V value;
		public Node left = null;
		public Node right = null;
		
		/**
		 * @param key
		 * @param value
		 * @param left
		 * @param right
		 */
		public Node(K key, V value) {
			this.key = key;
			this.value = value;
		}
	}
		
	@Override
	public void clear() {
		size = 0;
		root = null;
	}

	@Override
	public boolean containsKey(Object target) {
		return findNode(target) != null;
	}

	/**
	 * Returns the entry that contains the target key, or null if there is none. 
	 * 
	 * @param target
	 */
	private Node findNode(Object target) {
		// some implementations can handle null as a key, but not this one
		Node new_root = root;
		if (target == null) {
            throw new NullPointerException();
	    }
		
		// something to make the compiler happy
		@SuppressWarnings("unchecked")
		Comparable<? super K> k = (Comparable<? super K>) target;
		
		// the actual search
		//System.out.println("LOOKING FOR");
		//System.out.println(k);
		
        while (k.compareTo(new_root.key) != 0){
        	//System.out.println("ROOT KEY "+ new_root.key );
        	//System.out.println(k.compareTo(new_root.key));
        	
        	if (k.compareTo(new_root.key) > 0 && new_root.right != null) {
        		//System.out.println("RIGHT: "+ new_root.right.key);
        		new_root = new_root.right; }

        	
        	else if (k.compareTo(new_root.key) < 0 && new_root.left != null) {
        		
        		//System.out.println("LEFT: " + new_root.left.key);
        		new_root = new_root.left; }
        	
        	else {
        		//System.out.println("NULL LEFT: " + new_root.left.key);
        		//System.out.println("NULL RIGHT: "+ new_root.right.key);
        		return null;
        			}
        	
        	} //System.out.println("FOUND " + new_root.key);
        return new_root;
        	}
        	
	
	
        	
        
	

	/**
	 * Compares two keys or two values, handling null correctly.
	 * 
	 * @param target
	 * @param obj
	 * @return
	 */
	private boolean equals(Object target, Object obj) {
		if (target == null) {
			return obj == null;
		}
		return target.equals(obj);
	}

	@Override
	public boolean containsValue(Object target) {
		Set<K> keys = keySet();
		for (K key: keys){
			Node node = findNode(key);
			if(equals(node.value, target) == true){
				return true;
			}
		} return false;
	}

	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		throw new UnsupportedOperationException();
	}

	@Override
	public V get(Object key) {
		Node node = findNode(key);
		if (node == null) {
			return null;
		}
		return node.value;
	}

	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	@Override
	public Set<K> keySet() {
		Set<K> set = new LinkedHashSet<K>();
        keySetHelper(root,set);
		return set;
	}
	
	public void keySetHelper(Node node,Set<K> set){
		if (node == null){
			return;
		}
		keySetHelper(node.left,set);
		set.add(node.key);
		keySetHelper(node.right, set);
		
	}
	

	@Override
	public V put(K key, V value) {
		if (key == null) {
			throw new NullPointerException();
		}
		if (root == null) {
			root = new Node(key, value);
			size++;
			return null;
		}
		return putHelper(root, key, value);
	}

	private V putHelper(Node node, K key, V value) {
		//System.out.println(this.keySet());
		//System.out.println("START WITH NODE" + root.key);
        if (containsKey(key)== true){
        	//System.out.println("FOUND KEY" + key);
        	V old_value = get(key);
        	Node change = findNode(key);
        	change.value = value;
        	return old_value;
        }
        else {
        	//System.out.println("NO FOUND KEY" + key);
        	Node cur_node = node;
        	Comparable<? super K> k = (Comparable<? super K>) key;
        	Node new_node = new Node(key,value);
        	int i = 0;
        	
        	while ( i < 50){
        		if(k.compareTo(cur_node.key) < 0){
        			//System.out.println("Less so going left " + k.compareTo(cur_node.key));
        			if(cur_node.left == null){
        				//System.out.println("Found empty spot done");
        				cur_node.left = new_node;
        				size++;
        				return null;
        			}
        			//System.out.println("Setting new cur_node to left");
        			cur_node = cur_node.left;
        			//System.out.println(cur_node.key);
        			i++;
        		} 
        		if(k.compareTo(cur_node.key) > 0){
        			//System.out.print("More so going right " + k.compareTo(cur_node.key));
        			if(cur_node.right == null){
        				//System.out.println("Found empty spot done");
        				cur_node.right = new_node;
        				size++ ;
        				return null;
        			}
        			//System.out.println("Setting new cur_node right");
        	
        			cur_node = cur_node.right;
        			//System.out.println(cur_node.key);
        			i++;
        		}
        	}
        } return null;
        
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> map) {
		for (Map.Entry<? extends K, ? extends V> entry: map.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public V remove(Object key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public Collection<V> values() {
		Set<V> set = new HashSet<V>();
		Deque<Node> stack = new LinkedList<Node>();
		stack.push(root);
		while (!stack.isEmpty()) {
			Node node = stack.pop();
			if (node == null) continue;
			set.add(node.value);
			stack.push(node.left);
			stack.push(node.right);
		}
		return set;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Map<String, Integer> map = new MyTreeMap<String, Integer>();
		map.put("Word1", 1);
		map.put("Word2", 2);
		Integer value = map.get("Word1");
		System.out.println(value);
		
		for (String key: map.keySet()) {
			System.out.println(key + ", " + map.get(key));
		}
	}

	/**
	 * Makes a node.
	 * 
	 * This is only here for testing purposes.  Should not be used otherwise.
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public MyTreeMap<K, V>.Node makeNode(K key, V value) {
		return new Node(key, value);
	}

	/**
	 * Sets the instance variables.
	 * 
	 * This is only here for testing purposes.  Should not be used otherwise.
	 * 
	 * @param node
	 * @param size
	 */
	public void setTree(Node node, int size ) {
		this.root = node;
		this.size = size;
	}

	/**
	 * Returns the height of the tree.
	 * 
	 * This is only here for testing purposes.  Should not be used otherwise.
	 * 
	 * @return
	 */
	public int height() {
		return heightHelper(root);
	}

	private int heightHelper(Node node) {
		if (node == null) {
			return 0;
		}
		int left = heightHelper(node.left);
		int right = heightHelper(node.right);
		return Math.max(left, right) + 1;
	}
}
