package com.flatironschool.javacs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.lang3.text.WordUtils;
import org.jsoup.select.Elements;

import java.lang.Object;

import redis.clients.jedis.Jedis;

/**
 * Represents the results of a search query.
 *
 */
final class MyEntry implements Map.Entry<String, Integer> {
	private String key;
	private Integer value;

	public MyEntry(String key, Integer value) {
		this.key = key;
		this.value = value;
	}

	@Override
	public String getKey() {
		return key;
	}

	@Override
	public Integer getValue() {
		return value;
	}

	@Override
	public Integer setValue(Integer newValue) {
		value = newValue;
		return value;
	}
}

public class WikiSearch {

	// map from URLs that contain the term(s) to relevance score
	private Map<String, Integer> map;

	/**
	 * Constructor.
	 * 
	 * @param map
	 */
	public WikiSearch(Map<String, Integer> map) {
		this.map = map;
	}

	/**
	 * Looks up the relevance of a given URL.
	 * 
	 * @param url
	 * @return
	 */
	public Integer getRelevance(String url) {
		Integer relevance = map.get(url);
		return relevance == null ? 0 : relevance;
	}

	public Entry<String, Integer> getTopEntry() {
		List<Entry<String, Integer>> entries = sort();
		if (entries.size() != 0) {
			return entries.get(0);
		} else {
			return null;
		}
	}

	/**
	 * Prints the contents in order of term frequency.
	 * 
	 * @param map
	 */
	private void print() {
		List<Entry<String, Integer>> entries = sort();

		for (Entry<String, Integer> entry : entries) {
			System.out.println(entry);
		}
	}

	/**
	 * Computes the union of two search results.
	 * 
	 * @param that
	 * @return New WikiSearch object.
	 */
	public WikiSearch or(WikiSearch that) {
		// System.out.println("THIS");
		// System.out.println(this.map.keySet());
		// System.out.println(this.map.values());
		// System.out.println("THAT");
		// System.out.println(that.map.keySet());
		// System.out.println(that.map.values());

		Set<String> urls_1 = this.map.keySet();
		Set<String> urls_2 = that.map.keySet();
		Set<String> total_urls = new HashSet<String>(urls_1);
		total_urls.addAll(urls_2);

		// System.out.println(total_urls.addAll(urls_2));
		// System.out.println("MADE IT HERE");
		// System.out.println(urls_1);
		// System.out.println(urls_2);

		// System.out.println("UNION");
		// System.out.println(total_urls);
		Map<String, Integer> final_map = new HashMap<String, Integer>();
		for (String url : total_urls) {
			if (urls_1.contains(url) && urls_2.contains(url)) {
				final_map.put(url, this.map.get(url) + that.map.get(url));
			} else if (urls_1.contains(url)) {
				final_map.put(url, this.map.get(url));
			} else {
				final_map.put(url, that.map.get(url));
			}

		}
		WikiSearch wk = new WikiSearch(final_map);

		return wk;
	}

	/**
	 * Computes the intersection of two search results.
	 * 
	 * @param that
	 * @return New WikiSearch object.
	 */
	public WikiSearch and(WikiSearch that) {
		Set<String> urls_1 = this.map.keySet();
		Set<String> urls_2 = that.map.keySet();
		Set<String> total_urls = new HashSet<String>(urls_1);
		total_urls.retainAll(urls_2);

		Map<String, Integer> final_map = new HashMap<String, Integer>();
		for (String url : total_urls) {
			if (urls_1.contains(url) && urls_2.contains(url)) {
				final_map.put(url, this.map.get(url) + that.map.get(url));
			} else if (urls_1.contains(url)) {
				final_map.put(url, this.map.get(url));
			} else {
				final_map.put(url, that.map.get(url));
			}

		}
		WikiSearch wk = new WikiSearch(final_map);

		return wk;

	}

	/**
	 * Computes the intersection of two search results.
	 * 
	 * @param that
	 * @return New WikiSearch object.
	 */
	public WikiSearch minus(WikiSearch that) {
		Set<String> urls_1 = this.map.keySet();
		Set<String> urls_2 = that.map.keySet();
		Set<String> total_urls = new HashSet<String>(urls_1);
		total_urls.removeAll(urls_2);

		Map<String, Integer> final_map = new HashMap<String, Integer>();
		for (String url : total_urls) {
			if (urls_1.contains(url) && urls_2.contains(url)) {
				final_map.put(url, this.map.get(url) + that.map.get(url));
			} else if (urls_1.contains(url)) {
				final_map.put(url, this.map.get(url));
			} else {
				final_map.put(url, that.map.get(url));
			}

		}
		WikiSearch wk = new WikiSearch(final_map);

		return wk;

	}

	/**
	 * Computes the relevance of a search with multiple terms.
	 * 
	 * @param rel1:
	 *            relevance score for the first search
	 * @param rel2:
	 *            relevance score for the second search
	 * @return
	 */
	protected int totalRelevance(Integer rel1, Integer rel2) {
		// simple starting place: relevance is the sum of the term frequencies.
		return rel1 + rel2;
	}

	/**
	 * Sort the results by relevance.
	 * 
	 * @return List of entries with URL and relevance.
	 */

	public class List_Item implements Comparable<List_Item> {
		private final String url;
		private final int score;

		public List_Item(String url, int score) {
			this.url = url;
			this.score = score;
		}

		public int compareTo(List_Item that) {
			if (this.score < that.score) {
				return -1;
			}
			if (this.score > that.score) {
				return 1;
			}
			if (this.score == that.score) {
				return 0;
			}
			return 0;
		}
	}
	
	private static List<Entry<String, Integer>> final_list = new ArrayList<Entry<String, Integer>>();
//	for (int i = 0; i < map.size(); i++) {
//		Map.Entry<String, Integer> empty = new MyEntry("nothing", 0);
//		final_list.add(empty);
//	}
	public List<Entry<String, Integer>> sort() {
		// System.out.println("ORIGINAL LIST");
		// System.out.println(map.keySet());
		// System.out.println(map.values());
		
		final_list.clear();
		List<List_Item> sort_list = new ArrayList<List_Item>();
		for (String url : map.keySet()) {
			List_Item list_item = new List_Item(url, getRelevance(url));
			sort_list.add(list_item);
		}
		Collections.sort(sort_list);

		// System.out.println("SORTED LIST");
		// for (List_Item item: sort_list){
		// System.out.println(item.url);
		// }

		//List<Entry<String, Integer>> final_list = new ArrayList<Entry<String, Integer>>();
		
		// *** Fill final_list here ***
		
		for (int i = 0; i < map.size(); i++) {
			Map.Entry<String, Integer> empty = new MyEntry("nothing", 0);
			final_list.add(empty);
		}
		// for (Map.Entry<String, Integer> item: final_list){
		// System.out.println(item.getKey());
		// }

		for (Entry<String, Integer> entry : map.entrySet()) {
			String name = entry.getKey();
			// System.out.println("ENTRY CONSIDERED " + name );
			// int value = entry.getValue();
			// System.out.println("ENTRY SCORE " + value );
			for (List_Item item : sort_list) {
				if (item.url == name) {
					// System.out.println("MATCHES ITEM " + name);
					List_Item final_item = item;
					int index = sort_list.indexOf(final_item);
					final_list.remove(index);
					final_list.add(index, entry);
					// System.out.println("SHOULD BE AT INDEX " + index);

					// System.out.println("ADDED TO FINAL LIST");

					// for (Map.Entry<String, Integer> abc: final_list){
					// System.out.println(abc.getKey());
					// }
					break;
				}
			}

			// Integer score = item.score;
			// Map.Entry<String,Integer> item_to_entry = new
			// Map.Entry<String,Integer>(name,score);

		}
		Collections.reverse(final_list);
//		for (int i = 0; i < final_list.size(); i++) {
//			String title = final_list.get(i).getKey();
//			int value = final_list.get(i).getValue();
//			if (title.contains("https://en.wikipedia.org/wiki/")) {
//				System.out.println(findTitle(title) + " = " + value);
//			}
//		}
		//
		// }
		// System.out.println("FINAL LIST: " + final_list);
		return final_list;
	}

	private static String findTitle(String title) {
		String subTitle = title.substring(title.lastIndexOf('/'));
		if (subTitle.indexOf('_') != -1) {
			subTitle = subTitle.substring(1);
			subTitle = subTitle.replace('_', ' ');
			if (subTitle.indexOf(' ') != -1)
				WordUtils.capitalize(subTitle);
		} else {
			subTitle = subTitle.substring(1);
		}
		return subTitle;
	}

	/**
	 * Performs a search and makes a WikiSearch object.
	 * 
	 * @param term
	 * @param index
	 * @return
	 */
	public static WikiSearch search(String term, JedisIndex index) {
		System.out.println("Starting search........");

		Map<String, Integer> map = index.getCountsForms(term);
		return new WikiSearch(map);
	}

	public static void main(String[] args) throws IOException {

		String input = "";
		Scanner sc = new Scanner(System.in);
		do {
			System.out.print("Enter your query here: ");
			input = sc.nextLine();
			// make a JedisIndex
			Jedis jedis = JedisMaker.make();

			JedisIndex index = new JedisIndex(jedis);

			// search for the first term
			System.out.println("Query: " + input);
			WikiSearch search1 = search(input, index);

			if (search1.getTopEntry() != null) {
				// search1.print();
				for (int i = 0; i < final_list.size(); i++) {
					String title = final_list.get(i).getKey();
					int value = final_list.get(i).getValue();
					if (title.contains("https://en.wikipedia.org/wiki/")) {
						System.out.println(findTitle(title) + " = " + value);
					}
				}
				System.out.println("\n" + "Based on page similarity, you might also like to read these pages:");
				WikiFetcher wf = new WikiFetcher();
				String best_url = search1.getTopEntry().getKey();
				Elements paragraphs = wf.readWikipedia(best_url);
				List<String> recommendations = index.findMostSimilar(best_url, paragraphs);
				for (String page : recommendations) {
					System.out.println(page);
				}
			} else {
				System.out.println("Page for given term could not be found \nPlease try searching a different term");
			}

		} while (!input.equals(""));
		sc.close();

		// // search for the second term
		// String term2 = "programming";
		// System.out.println("Query: " + term2);
		// WikiSearch search2 = search(term2, index);
		// search2.print();
		//
		// // compute the intersection of the searches
		// System.out.println("Query: " + term1 + " AND " + term2);
		// WikiSearch intersection = search1.and(search2);
		// intersection.print();
	}
}
