package com.flatironschool.javacs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;


/**
 * Encapsulates a map from search term to frequency (count).
 * 
 * @author downey
 *
 */
public class TermCounter {
	
	private Map<String, Integer> map;
	private List<String> first_sentence;
	private String label;
	
	public TermCounter(String label) {
		this.label = label;
		this.map = new TreeMap<String, Integer>();
		this.first_sentence = new ArrayList<String>();
		//store all words in first paragraph, rank based on sentence, remember to process
	}
	
	public String getLabel() {
		return label;
	}
	
	/**
	 * Returns the total of all counts.
	 * 
	 * @return
	 */
	public int size() {
		int total = 0;
		for (Integer value: map.values()) {
			total += value;
		}
		return total;
	}

	/**
	 * Takes a collection of Elements and counts their words.
	 * 
	 * @param paragraphs
	 */
	public void processElements(Elements paragraphs) {
		int i=1;
		for (Node node: paragraphs) {
			processTree(node, i);
			
			//or instead get first sentence, from first 20 words-2 lines
			// saving the word set of the first para to give higher relevance
			
			i = i + 1;
		}
	}
	
	
	
	/**
	 * Finds TextNodes in a DOM tree and counts their words.
	 * 
	 * @param root
	 */
	public void processTree(Node root, int i) {
		// NOTE: we could use select to find the TextNodes, but since
		// we already have a tree iterator, let's use it.
		for (Node node: new WikiNodeIterable(root)) {
			if (node instanceof TextNode) {
				processText(((TextNode) node).text(), i);
			}
		}
	}

	/**
	 * Splits `text` into words and counts them.
	 * 
	 * @param text  The text to process.
	 */
	public void processText(String text,int i) {
//		System.out.println(text);
		// replace punctuation with spaces, convert to lower case, and split on whitespace
		String[] array = text.replaceAll("\\pP", " ").toLowerCase().split("\\s+");
		
		
//		System.out.println(array.length);
		for (int k=0; k<array.length; k++) {
			String term = array[k];
			if(i == 1){
				if(first_sentence.size() <25){
					first_sentence.add(term.toString());
				}
				}
			
			incrementTermCount(term, i);
		}
		
		
		
		//possibly process here
	}

	/**
	 * Increments the counter associated with `term`.
	 * 
	 * @param term
	 */
	
	// added greater relevance for words that show up EARLIER on
	public void incrementTermCount(String term, int i) {
		
		// add 3x for first para
		if (i == 1){
			put(term, get(term) + 3);
		}
		// 2x for second para
		else if (i == 2){
			put(term, get(term) + 2);
		}
		// 1x for everythin else
		else{
			put(term, get(term) + 1);
		}
	}
	
	public List<String> getFirstSentence(){
		return this.first_sentence;
	}
	

	/**
	 * Adds a term to the map with a given count.
	 * 
	 * @param term
	 * @param count
	 */
	public void put(String term, int count) {
		map.put(term, count);
	}

	/**
	 * Returns the count associated with this term, or 0 if it is unseen.
	 * 
	 * @param term
	 * @return
	 */
	public Integer get(String term) {
		Integer count = map.get(term);
		return count == null ? 0 : count;
	}

	/**
	 * Returns the set of terms that have been counted.
	 * 
	 * @return
	 */
	public Set<String> keySet() {
		return map.keySet();
	}
	
	/**
	 * Print the terms and their counts in arbitrary order.
	 */
	public void printCounts() {
		for (String key: keySet()) {
			Integer count = get(key);
			System.out.println(key + ", " + count);
		}
		System.out.println("Total of all counts = " + size());
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		String url = "https://en.wikipedia.org/wiki/Java_(programming_language)";
		
		WikiFetcher wf = new WikiFetcher();
		Elements paragraphs = wf.fetchWikipedia(url);
		
		TermCounter counter = new TermCounter(url.toString());
		counter.processElements(paragraphs);
//		System.out.println(counter.getFirstSentence());
		counter.printCounts();
	}
}
