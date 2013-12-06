package edu.asu.diego;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

public class MentionsUrlsFilter {
	
	private static String url  =  "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
	private static String url2 = ".*(https?|ftp|file)://.*";

	public String filterMentions(String rawTweet){
		String processedTweet = rawTweet;
		int mentionIndex = -1;
		try{
			while((mentionIndex = processedTweet.indexOf('@'))!=-1){
				String finalTweet;
				finalTweet = (processedTweet.substring(0,mentionIndex)).trim();
				if(processedTweet.indexOf(' ', mentionIndex)!=-1)
					finalTweet +=  " " + (processedTweet.substring(processedTweet.indexOf(' ', mentionIndex))).trim();
				
				processedTweet = finalTweet.trim();
			}
		}catch(Exception e){
			System.out.println(rawTweet);
			e.printStackTrace();
		}
		return processedTweet;
	}

	public String filterURL(String rawTweet){
		String processedTweet = null;
		try{
			processedTweet = "";
			
			Pattern urlPattern = Pattern.compile(url);
			Pattern urlPattern2 = Pattern.compile(url2);
			
			String[] splitdata = rawTweet.split(" ");
			for(String word : splitdata){
				if(!(urlPattern.matcher(word).matches())){
					if(!urlPattern2.matcher(word).matches()){
						boolean addflag = true;
						for(int i=0;i<word.length();i++){
							if(Character.UnicodeBlock.of(word.charAt(i))!=Character.UnicodeBlock.BASIC_LATIN){
								addflag = false;
								break;
							}
						}
						if(addflag){
							processedTweet += " " + word;
							processedTweet = processedTweet.trim();
						}
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return processedTweet;
	}
	
//	public static void main(String[] args) {
//		MentionsUrlsFilter objMentionsFilter = new MentionsUrlsFilter();
//		try{
//			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
//			   System.out.print("> ");
//			   // Read user input
//			   
//			   String inputStr = br.readLine();
//			   
//			   while (!inputStr.equals("")) {
//			                    String processedTweet = objMentionsFilter.filterMentions(inputStr);
//			                    processedTweet = objMentionsFilter.filterURL(processedTweet);
//			                    
//			                    System.out.println("Processed is " + processedTweet);
//			                    System.out.print("\n> ");
//			                    inputStr = br.readLine();
//			   }
//			   br.close();
//			}catch(Exception e){
//				e.printStackTrace();
//			}
//		}
}
