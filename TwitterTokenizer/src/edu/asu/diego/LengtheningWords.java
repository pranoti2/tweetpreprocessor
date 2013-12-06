package edu.asu.diego;

import java.io.IOException;

public class LengtheningWords {

	public String lengthString(String rawTweet){
		
		StringBuilder processStringBuilder = new StringBuilder();
		String[] splitwords = rawTweet.split(" ");
		for(String word : splitwords){
			word = lengthenWord(word);
			processStringBuilder.append(" " +word);
		}
		
		return processStringBuilder.toString();
	}
	
	private String lengthenWord(String word){
		String processedWord = null;
		int currentIndex = 0;
		int nextIndex = currentIndex + 1;
		while(currentIndex < word.length()){
			
			while((nextIndex < word.length()) && (word.charAt(currentIndex) == word.charAt(nextIndex))){
				nextIndex++;
			}
			
			if(nextIndex - currentIndex > 2){
				
				StringBuilder newStr = new StringBuilder();
				newStr.append(word.substring(0,currentIndex+2));
				if(!(nextIndex-1 >= word.length()))
					newStr.append(word.substring(nextIndex,word.length()));
				word = newStr.toString();
			}
			
			currentIndex++;
			nextIndex = currentIndex + 1;
		}
		processedWord = word;
		return processedWord;
	}
	
//	public static void main(String[] args) throws IOException {
//		LengtheningWords obj = new LengtheningWords();
//		String processedTweet = obj.lengthString("This is reallllllllllllly cooooooooollllllllll stuff!!!");
//		System.out.println(processedTweet);
//		String processedword = obj.lengthenWord("cooolllllllllll");
//		System.out.println(processedword);
//		System.out.println(obj.lengthenWord("happpy"));
//		System.out.println(obj.lengthenWord("hurry"));
//		System.out.println(obj.lengthenWord("vvvvvh"));
//	}
}
