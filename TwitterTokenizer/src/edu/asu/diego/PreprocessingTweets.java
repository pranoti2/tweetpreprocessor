package edu.asu.diego;

import java.io.IOException;

public class PreprocessingTweets {

	/**
	 * Method to remove mentions from the tweet. 
	 * @param rawTweet input tweet in String format
	 * @return processed output string
	 */
	public String processTweetforMentions(String rawTweet){
		String processedTweet = null;
		MentionsUrlsFilter objMentionsFilter = new MentionsUrlsFilter();
		processedTweet = objMentionsFilter.filterMentions(rawTweet);
       
		return processedTweet;
	}
	
	/**
	 * Method to remove URLs and Unicode character words from the given tweet
	 * @param rawTweet input tweet in String format
	 * @return processed output string
	 */
	public String processTweetforURLnUnicode(String rawTweet){
		String processedTweet = null;
		MentionsUrlsFilter objMentionsFilter = new MentionsUrlsFilter();
		processedTweet = objMentionsFilter.filterURL(rawTweet);
		return processedTweet;
	}
	
	/**
	 * Method to process the lengthened words for example the method will process reallllllllllyyyyyy to reallyy
	 * @param rawTweet input tweet in String format
	 * @return processed output string
	 */
	public String processTweetforLengthening(String rawTweet){
		String processedTweet = null;
		LengtheningWords obj = new LengtheningWords();
		processedTweet = obj.lengthString(rawTweet);
		return processedTweet;
	}
	
	/**
	 * Method to process the tweet for expanding the twitter terminologies
	 * @param rawTweet input tweet in String format
	 * @return processed output string
	 * @throws Exception
	 */
	public String processTweetTerminologiesExpansion(String rawTweet) throws Exception {
		String processedTweet = null;
		ExpandTweets objExpandTweets = new ExpandTweets("data\\abbrevationsInTwitter.txt");
		processedTweet = objExpandTweets.processTweetExpansion(rawTweet);
		return processedTweet;
	}
	
}
