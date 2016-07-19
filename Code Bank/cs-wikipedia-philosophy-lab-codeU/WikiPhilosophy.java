package com.flatironschool.javacs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import org.jsoup.select.Elements;

public class WikiPhilosophy {
	
	final static WikiFetcher wf = new WikiFetcher();
	public static List<String> link_list = new ArrayList<String>();
	
	/**
	 * Tests a conjecture about Wikipedia and Philosophy.
	 * 
	 * https://en.wikipedia.org/wiki/Wikipedia:Getting_to_Philosophy
	 * 
	 * 1. Clicking on the first non-parenthesized, non-italicized link
     * 2. Ignoring external links, links to the current page, or red links
     * 3. Stopping when reaching "Philosophy", a page with no links or a page
     *    that does not exist, or when a loop occurs
	 * 
	 * @param args
	 * @throws IOException
	 */
	
	public static int isValid(Node node, List<String> text){
		//checking for italics
		Node start_node = node;
		
		
		while (start_node != null){
			String tag = start_node.nodeName();
			if (tag == "i" || tag == "em") {
				return 1;
			}
			
			start_node = start_node.parent();
			//System.out.println(start_node);
		}
		//System.out.println(text);
		for (String word : text){
			if (word.indexOf("(") != -1){
				text.clear();
				return 1;
			}
		
		
		
	} return 0;
	}
	
	public static void main(String[] args) throws IOException {
		
        // some example code to get you started
		
		String name = "" ;
		int count = 1;
		String url = "https://en.wikipedia.org/wiki/Java_(programming_language)";
		//String url = "https://en.wikipedia.org/wiki/Mathematics";

		outerloop:
		while (url != "https://en.wikipedia.org/wiki/Philosophy"){
			List<String> text = new ArrayList<String>();
			Elements paragraphs = wf.fetchWikipedia(url);

		
			Element firstPara = paragraphs.get(0);
			
			Iterable<Node> iter = new WikiNodeIterable(firstPara);
			innerloop:
			for (Node node: iter) {
				
				if (node instanceof TextNode){
					//System.out.println("TEXT THINGS");
					//System.out.println(node);
					text.add(node.toString());
				}
				//System.out.println("WORD LIST");
		
				
				if (node instanceof Element) {
					//System.out.println("TAG THINGS");
					//System.out.println(node);
					name = node.nodeName();
					
					if (link_list.contains("https://en.wikipedia.org"+ node.attr("href"))){
						System.out.println("Problem1");
						break outerloop;
					}
					
					if (name == "a" && isValid(node, text) == 0){
						//System.out.println("Makes it through check");
	
							url = "https://en.wikipedia.org"+ node.attr("href");
							System.out.println("GOING TO");
							System.out.println(url);
							count = count + 1;
							if (url.equals("https://en.wikipedia.org/wiki/Philosophy")){
								System.out.println("MADE IT TO END");
								break outerloop;
							}
				
							link_list.add(url);
							break innerloop;
							
									
						}
						
					}
					
			}
        
		}
		System.out.println(count);

        // the following throws an exception so the test fails
        // until you update the code
        //String msg = "Complete this lab by adding your code and removing this statement.";
        //throw new UnsupportedOperationException(msg);
	}
}
