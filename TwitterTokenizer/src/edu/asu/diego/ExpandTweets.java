package edu.asu.diego;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class ExpandTweets {
	
	private Map<String,String> expansionMap;

	private Map<String, String> loadExpansionDict(String filePath) throws Exception{
		expansionMap = new HashMap<String, String>();
		try{
			BufferedReader reader = new BufferedReader(new FileReader(filePath));
			String line = null;
			while((line = reader.readLine())!=null){
				String[] splitdata = line.split("\t");
				if(splitdata.length == 2)
				{
					if(!expansionMap.containsKey(splitdata[0])){
						expansionMap.put(splitdata[0], splitdata[1]);
					}
					else{
						reader.close();
						throw new Exception("File not correctly structured - repeatations found in file");
					}
				}
				else{
					reader.close();
					throw new Exception("File structure not proper");
				}
			}
			reader.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}
		return expansionMap;
	}
	
	public ExpandTweets(String filePath) throws Exception{
		expansionMap = loadExpansionDict(filePath);
	}
	
	public String processTweetExpansion(String rawTweet) throws Exception{
		String processedTweet = null;
		if(rawTweet!=null){
			String[] splitdata = rawTweet.split(" ");
			processedTweet = "";
			for(String word : splitdata){
				word = word.trim();
				if(expansionMap.containsKey(word))
					processedTweet += " " + expansionMap.get(word);
				else
					processedTweet += " " + word;
			}
		}
		else
			throw new Exception("Raw Tweet cannot be null");
		
		return processedTweet;
	}
	
//	public static void main(String[] args) throws IOException {
//		try{
//		   ExpandTweets objExpandTweets = new ExpandTweets("data\\abbrevationsInTwitter.txt");
//		   BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
//		   System.out.print("> ");
//		   // Read user input
//		   String inputStr = br.readLine();
//		   while (!inputStr.equals("")) {
//		                    String processedTweet = objExpandTweets.processTweetExpansion(inputStr);
//		                    System.out.println("Expansion is " + processedTweet);
//		                    System.out.print("\n> ");
//		                    inputStr = br.readLine();
//		   }
//		   br.close();
//		}catch(Exception e){
//			e.printStackTrace();
//		}
//	}
}
