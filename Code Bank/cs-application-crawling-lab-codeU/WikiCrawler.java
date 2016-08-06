package com.flatironschool.javacs;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Node;


import redis.clients.jedis.Jedis;


public class WikiCrawler {
	// keeps track of where we started
	private final String source;
	
	// the index where the results go
	private JedisIndex index;
	
	// queue of URLs to be indexed
	private Queue<String> queue = new LinkedList<String>();
	
	// fetcher used to get pages from Wikipedia
	final static WikiFetcher wf = new WikiFetcher();

	/**
	 * Constructor.
	 * 
	 * @param source
	 * @param index
	 */
	public WikiCrawler(String source, JedisIndex index) {
		this.source = source;
		this.index = index;
		queue.offer(source);
	}

	/**
	 * Returns the number of URLs in the queue.
	 * 
	 * @return
	 */
	public int queueSize() {
		return queue.size();	
	}

	/**
	 * Gets a URL from the queue and indexes it.
	 * @param b 
	 * 
	 * @return Number of pages indexed.
	 * @throws IOException
	 */
	public String crawl(boolean testing) throws IOException {
//		System.out.println("HERE IS QUEUE");
//		System.out.println(queue);
		if (queue.isEmpty()) {
            return null;
        }
        String url = queue.peek();
//        System.out.println("Crawling " + url);
//        System.out.println("HERE IS QUEUE NOW");
//		System.out.println(queue);
//		System.out.println("TESTING VALUE");
//		System.out.println(testing);
 
        if (testing==false && index.isIndexed(url)) {
            System.out.println("Already indexed.");
            return null;
        }
 
        Elements paragraphs;
        if (testing == true) {
        	queue.remove(url);
            paragraphs = wf.readWikipedia(url);
//            System.out.println("FETCHED PARAS FOR TRUE");
            index.indexPage(url, paragraphs);
//          System.out.println("INDEXED PAGE: " + url);
            queueInternalLinks(paragraphs);
//          System.out.println("QUEUED INTERNAL LINKS: " + url);
        } else {
        	queue.remove(url);
            paragraphs = wf.fetchWikipedia(url);
//            System.out.println("FETCHED PARAS FOR FALSE");
            index.indexPage(url, paragraphs);
//          System.out.println("INDEXED PAGE: " + url);
            queueInternalLinks(paragraphs);
//          System.out.println("QUEUED INTERNAL LINKS: " + url);
        }
        
        
        return url;
    }
	
	
	/**
	 * Parses paragraphs and adds internal links to the queue.
	 * 
	 * @param paragraphs
	 */
	// NOTE: absence of access level modifier means package-level
	void queueInternalLinks(Elements paragraphs) {
		for (Element paragraph: paragraphs) {        
	        			
		        			Iterable<Node> iter = new WikiNodeIterable(paragraph);
		        			for (Node node: iter) {
		        					
		        				if (node instanceof Element) {
		        					//System.out.println("TAG THINGS");
		        					//System.out.println(node);
		        					String name = node.nodeName();				
		        					
		        					if (name == "a" && node.attr("href").startsWith("/wiki")){
		        					//System.out.println("Makes it through check");
		        	
		        						String url = "https://en.wikipedia.org"+ node.attr("href");
	//	        						System.out.println("GOING TO");
	//	        						System.out.println(url);
		        						queue.add(url);
	        					}
	        				}
	        			}
	            	}

        }
	

	
	public static void main(String[] args) throws IOException {
		
		// make a WikiCrawler
		Jedis jedis = JedisMaker.make();
		JedisIndex index = new JedisIndex(jedis); 
		String source = "https://en.wikipedia.org/wiki/Java_(programming_language)";
		WikiCrawler wc = new WikiCrawler(source, index);
		
		// for testing purposes, load up the queue
		Elements paragraphs = wf.fetchWikipedia(source);
		wc.queueInternalLinks(paragraphs);

		// loop until we index a new page
		String res;
		do {
			res = wc.crawl(false);

            // REMOVE THIS BREAK STATEMENT WHEN crawl() IS WORKING
//            break;
		} while (res == null);
		
		Map<String, Integer> map = index.getCounts("the");
		for (Entry<String, Integer> entry: map.entrySet()) {
			System.out.println(entry);
		}
	}
}
