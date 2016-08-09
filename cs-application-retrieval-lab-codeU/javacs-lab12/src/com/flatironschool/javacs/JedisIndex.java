package com.flatironschool.javacs;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.jsoup.select.Elements;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

/**
 * Represents a Redis-backed web search index.
 * 
 */
public class JedisIndex {

	private Jedis jedis;

	/**
	 * Constructor.
	 * 
	 * @param jedis
	 */
	public JedisIndex(Jedis jedis) {
		this.jedis = jedis;
	}
	
	/**
	 * Returns the Redis key for a given search term.
	 * 
	 * @return Redis key.
	 */
	private String urlSetKey(String term) {
		return "URLSet:" + term;
	}
	
	/**
	 * Returns the Redis key for a URL's TermCounter.
	 * 
	 * @return Redis key.
	 */
	private String termCounterKey(String url) {
		return "TermCounter:" + url;
	}

	/**
	 * Checks whether we have a TermCounter for a given URL.
	 * 
	 * @param url
	 * @return
	 */
	public boolean isIndexed(String url) {
		String redisKey = termCounterKey(url);
		return jedis.exists(redisKey);
	}
	
	/**
	 * Adds a URL to the set associated with `term`.
	 * 
	 * @param term
	 * @param tc
	 */
	public void add(String term, TermCounter tc) {
		jedis.sadd(urlSetKey(term), tc.getLabel());
	}

	/**
	 * Looks up a search term and returns a set of URLs.
	 * 
	 * @param term
	 * @return Set of URLs.
	 */
	public Set<String> getURLs(String term) {
		Set<String> set = jedis.smembers(urlSetKey(term));
		return set;
	}

	/**
	 * Looks up a term and returns a map from URL to count.
	 * 
	 * @param term
	 * @return Map from URL to count.
	 */
	public Map<String, Integer> getCounts(String term) {
		Map<String, Integer> map = new HashMap<String, Integer>();
		Set<String> urls = getURLs(term);
		for (String url: urls) {
			Integer count = getCount(url, term);
			map.put(url, count);
		}
		return map;
	}

	/**
	 * Looks up a term and returns a map from URL to count.
	 * 
	 * @param term
	 * @return Map from URL to count.
	 */
	public Map<String, Integer> getCountsFaster(String term) {
		// convert the set of strings to a list so we get the
		// same traversal order every time
		List<String> urls = new ArrayList<String>();
		urls.addAll(getURLs(term));

		// construct a transaction to perform all lookups
		Transaction t = jedis.multi();
		for (String url: urls) {
			String redisKey = termCounterKey(url);
			t.hget(redisKey, term);
		}
		List<Object> res = t.exec();

		// iterate the results and make the map
		Map<String, Integer> map = new HashMap<String, Integer>();
		int i = 0;
		for (String url: urls) {
//			System.out.println(url);
			Integer count = new Integer((String) res.get(i++));
			map.put(url, count);
		}
		return map;
	}

	/**
	 * Returns the number of times the given term appears at the given URL.
	 * 
	 * @param url
	 * @param term
	 * @return
	 */
	
	public Integer getCount(String url, String term) {
		String redisKey = termCounterKey(url);
		String count = jedis.hget(redisKey, term);
		return new Integer(count);
	}
	
	public Map<String, Integer> getCountsForms(String term){
//		System.out.println("Start debugging here.....");
		Map<String, Integer> counter = new HashMap<String, Integer>();
		
		// convert the set of strings to a list so we get the
		// same traversal order every time
		List<String> URLs = new ArrayList<String>();
		URLs.addAll(getURLs(term));
//		System.out.println("URLS THAT HAVE TERM: " + term);
//		System.out.println(URLs);
		
		// count from single word for every URL
		Map<String, Integer> tdidf = getTDIDFCounts(term);
//		System.out.println("Original tdidf counts: " + tdidf);
		
		ArrayList<String> forms = new ArrayList<String>();
		String last_char = term.substring(term.length() - 1);
//		System.out.println("Last letter of word: " + last_char);
		
		// forming dict of terms to add
		if (last_char.equals("e")){
			term = term.substring(0, term.length()-1);
//			System.out.println("WORD WITHOUT E: "+ term);
			forms.addAll(Arrays.asList(term+"ing",term+"s",term+"ed",term+"able"));
		}
		
		else{
			forms.addAll(Arrays.asList(term+"ing",term+"s",term+"ed",term+"able",term+last_char+"ing",term+last_char+"ed",term+last_char+"able"));
		}
		
//		System.out.println("DIFFERENT WORD FORMS: " + forms);
		
		for (String URL: URLs){
				
	//		System.out.println(tdidf.get("https://en.wikipedia.org/wiki/Consciousness"));
	//		System.out.println("tdidf score of word for URL: "+ URL.substring(30) + tdidf.get(URL));
			String title = URL.substring(30);
			double final_count = tdidf.get(URL);
			
			// adding higher relevance if in title
			if (title.toLowerCase().contains(term.toLowerCase())){
				final_count = final_count + 10;
			}
				
	
			// set how much value the forms should have
			double score = 0.75;
			
			for(String form:forms){
	//			System.out.println("Considering form: " + form);
				Map<String,Integer> form_tdidf = getTDIDFCounts(form);
	//			System.out.println("Number of entries for word map: " + form_tdidf.size());
	//			System.out.println("Getting URL entry: "+ form_tdidf.get(URL));
				if (form_tdidf.get(URL) != null){
				final_count = final_count + score*form_tdidf.get(URL);
	//			System.out.println("Count after adding "+ form + final_count);
				}
			}
	//		System.out.println("Total count for URL: "+ final_count);
			counter.put(URL, (int)final_count);	
		}
//		System.out.println("FINAL MAP:" + tdidf);
		return counter;
		
	}
	
	// gets relevance score based on tf-idf
	
	public Map<String, Integer> getTDIDFCounts(String term) {
		Map<String, Integer> counter = new HashMap<String, Integer>();
		
		// convert the set of strings to a list so we get the
		// same traversal order every time
		List<String> URLs = new ArrayList<String>();
		URLs.addAll(getURLs(term));
//        System.out.println("URLS THAT HAVE TERM: " + term);
//        System.out.println(URLs);
        
        //set this according to actual number
        int total_docs = getURLs("a").size();
//        System.out.println("TOTAL NUMBER OF DOCS: " + total_docs);
        
        int containing_docs = URLs.size();
        int df = containing_docs/total_docs;
//        System.out.println("DF: " + df);
        Transaction t = jedis.multi();
        for(String URL: URLs){
        	String redisKey = termCounterKey(URL);
			t.hget(redisKey, term);
        }
        List<Object> res = t.exec();
        
        int i = 0;
        for(String URL: URLs){
        	Integer count = new Integer((String) res.get(i++));
			counter.put(URL, count);
        }
        
//        for (String URL: URLs){
//        	double count = 0;
//        	int tf = getCount(URL,term);
//        	
//        	if (tf != 0){
// 
////        	System.out.println("TF: "+ tf); 
//        		count = tf;
//        	//handling divide by zero cases
//        		if(df != 0){
//        	count = Math.log(tf) - Math.log(1/df);
//        			}
//        	}
////        	System.out.println("RELEVANCE SCORE of " + URL+ "is: "+ count);
//        	counter.put(URL, (int)Math.round(count));
//        	
//        	
//        }
		return counter;
	}
		

	/**
	 * Add a page to the index.
	 * 
	 * @param url         URL of the page.
	 * @param paragraphs  Collection of elements that should be indexed.
	 */
	public void indexPage(String url, Elements paragraphs) {
		System.out.println("Indexing " + url);
		
		// make a TermCounter and count the terms in the paragraphs
		TermCounter tc = new TermCounter(url);
		tc.processElements(paragraphs);
		
		// push the contents of the TermCounter to Redis
		pushTermCounterToRedis(tc);

	}
	
	public List<String> getSentence(String url,Elements paragraphs){
		TermCounter tc = new TermCounter(url);
		tc.processElements(paragraphs);
		List<String> sentence = tc.getFirstSentence();
		return sentence;
	}
	
	
	public Integer findSimilarity(List<String> sentence1, List<String> sentence2){
		int count = 0;
		for (String word: sentence2){
			if (sentence1.contains(word) == true){
				count = count + 1;
			}
		}
		return count;
	}
	
	public List<String> findMostSimilar(String url, Elements paragraphs) throws IOException{
		List<String> most_similar = new ArrayList<String>();
		WikiFetcher wf = new WikiFetcher();
		
		List<String> sent1 = getSentence(url, paragraphs);
		List<String> sent2;
		Elements paragraphs2;
		int similarity = 0;
		
//		System.out.println("ALL KEYS: " + termCounterKeys());
		
		// might not be the right list of all urls in the index test this!
		for (String index_url: termCounterKeys()){
			String actual = index_url.substring(12);
//			System.out.println(actual);
			paragraphs2 = wf.readWikipedia(actual);
			sent2 = getSentence(actual, paragraphs2);
			similarity = findSimilarity(sent1, sent2);
			if (similarity > 10){
				most_similar.add(actual);
			}
		}
		return most_similar;
	}

	/**
	 * Pushes the contents of the TermCounter to Redis.
	 * 
	 * @param tc
	 * @return List of return values from Redis.
	 */
	public List<Object> pushTermCounterToRedis(TermCounter tc) {
		Transaction t = jedis.multi();
		
		String url = tc.getLabel();
		String hashname = termCounterKey(url);
		
		// if this page has already been indexed; delete the old hash
		t.del(hashname);

		// for each term, add an entry in the termcounter and a new
		// member of the index
		for (String term: tc.keySet()) {
			Integer count = tc.get(term);
			t.hset(hashname, term, count.toString());
			t.sadd(urlSetKey(term), url);
		}
		List<Object> res = t.exec();
		return res;
	}

	/**
	 * Prints the contents of the index.
	 * 
	 * Should be used for development and testing, not production.
	 */
	public void printIndex() {
		// loop through the search terms
		for (String term: termSet()) {
			System.out.println(term);
			
			// for each term, print the pages where it appears
			Set<String> urls = getURLs(term);
			for (String url: urls) {
				Integer count = getCount(url, term);
				System.out.println("    " + url + " " + count);
			}
		}
	}

	/**
	 * Returns the set of terms that have been indexed.
	 * 
	 * Should be used for development and testing, not production.
	 * 
	 * @return
	 */
	public Set<String> termSet() {
		Set<String> keys = urlSetKeys();
		Set<String> terms = new HashSet<String>();
		for (String key: keys) {
			String[] array = key.split(":");
			if (array.length < 2) {
				terms.add("");
			} else {
				terms.add(array[1]);
			}
		}
		return terms;
	}

	/**
	 * Returns URLSet keys for the terms that have been indexed.
	 * 
	 * Should be used for development and testing, not production.
	 * 
	 * @return
	 */
	public Set<String> urlSetKeys() {
		return jedis.keys("URLSet:*");
	}

	/**
	 * Returns TermCounter keys for the URLS that have been indexed.
	 * 
	 * Should be used for development and testing, not production.
	 * 
	 * @return
	 */
	public Set<String> termCounterKeys() {
		return jedis.keys("TermCounter:*");
	}

	/**
	 * Deletes all URLSet objects from the database.
	 * 
	 * Should be used for development and testing, not production.
	 * 
	 * @return
	 */
	public void deleteURLSets() {
		Set<String> keys = urlSetKeys();
		Transaction t = jedis.multi();
		for (String key: keys) {
			t.del(key);
		}
		t.exec();
	}

	/**
	 * Deletes all URLSet objects from the database.
	 * 
	 * Should be used for development and testing, not production.
	 * 
	 * @return
	 */
	public void deleteTermCounters() {
		Set<String> keys = termCounterKeys();
		Transaction t = jedis.multi();
		for (String key: keys) {
			t.del(key);
		}
		t.exec();
	}

	/**
	 * Deletes all keys from the database.
	 * 
	 * Should be used for development and testing, not production.
	 * 
	 * @return
	 */
	public void deleteAllKeys() {
		Set<String> keys = jedis.keys("*");
		Transaction t = jedis.multi();
		for (String key: keys) {
			t.del(key);
		}
		t.exec();
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		Jedis jedis = JedisMaker.make();

		JedisIndex index = new JedisIndex(jedis);
		
		//index.deleteTermCounters();
		//index.deleteURLSets();
        index.deleteAllKeys();		
        loadIndex(index);
		
//		Map<String, Integer> map = index.getCountsFaster("the");
//		for (Entry<String, Integer> entry: map.entrySet()) {
//			System.out.println(entry);
//		}
	}

	/**
	 * Stores two pages in the index for testing purposes.
	 * 
	 * @return
	 * @throws IOException
	 */
	private static void loadIndex(JedisIndex index) throws IOException {
		WikiFetcher wf = new WikiFetcher();
	

		String url = "https://en.wikipedia.org/wiki/Java_(programming_language)";
		Elements paragraphs = wf.readWikipedia(url);
		index.indexPage(url, paragraphs);
		
		url = "https://en.wikipedia.org/wiki/Programming_language";
		paragraphs = wf.readWikipedia(url);
		index.indexPage(url, paragraphs);
		
		url = "https://en.wikipedia.org/wiki/Awareness";
		paragraphs = wf.readWikipedia(url);
		index.indexPage(url, paragraphs);
		
		url = "https://en.wikipedia.org/wiki/Consciousness";
		paragraphs = wf.readWikipedia(url);
		index.indexPage(url, paragraphs);
		
	    url = "https://en.wikipedia.org/wiki/Science";
	    paragraphs = wf.readWikipedia(url);
	    index.indexPage(url, paragraphs);
//		
	    url = "https://en.wikipedia.org/wiki/Mathematics";
	    paragraphs = wf.readWikipedia(url);
  	    index.indexPage(url, paragraphs);

		url = "https://en.wikipedia.org/wiki/Modern_philosophy";
		paragraphs = wf.readWikipedia(url);
		index.indexPage(url, paragraphs);
		
		url = "https://en.wikipedia.org/wiki/Philosophy";
		paragraphs = wf.readWikipedia(url);
		index.indexPage(url, paragraphs);
		
		
		
		
		
		
		
//		List<String> similar = index.findMostSimilar(url, paragraphs);
//		System.out.println("SIMILAR: "+ similar);
		

//		
//		List<String> sent1 = index.getSentence(url, paragraphs);
//		System.out.println("This is sent1" + sent1);
//		
//		List<String> sent2 = index.getSentence(url1, paragraphs1);
//		System.out.println("This is sent2" + sent2);
//		
//		int count = index.findSimilarity(sent1,sent2);
//		System.out.println("Difference is: " + count);
		
		
	}

	
}
