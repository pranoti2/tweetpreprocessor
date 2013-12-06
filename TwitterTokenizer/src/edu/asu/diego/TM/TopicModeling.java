package edu.asu.diego.TM;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import cc.mallet.optimize.StochasticMetaAscent;
import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.CharSequenceLowercase;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.pipe.iterator.CsvIterator;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.Alphabet;
import cc.mallet.types.FeatureSequence;
import cc.mallet.types.IDSorter;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.LabelSequence;
import edu.asu.diego.ExpandTweets;
import edu.asu.diego.MentionsUrlsFilter;


public class TopicModeling {
	
	private HashMap<String, String> dataInstances; 
	private HashMap<Integer,List<String>> topicsMap;
	private Set<String> stopwordSet;

	public TopicModeling(String filePath){
		topicsMap = new HashMap<Integer,List<String>>();
		loadStopword("data\\en.txt");
		readInputFile(filePath);
		processData();
		writeProcessedFile("data\\processedInputDataTweet.txt");
	}
	
	private void loadStopword(String filepath){
		stopwordSet = new HashSet<String>();
		try{
			BufferedReader reader = new BufferedReader(new FileReader(filepath));
			String line;
			while((line = reader.readLine())!=null){
				stopwordSet.add(line.trim());
			}
			reader.close();
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
 	public void applyTopicModeling(String filepath) throws IOException{
		ArrayList<Pipe> pipeList = new ArrayList<Pipe>();
		pipeList.add(new CharSequenceLowercase());
		pipeList.add(new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")));
		pipeList.add( new TokenSequenceRemoveStopwords(new File("data\\en.txt"), "UTF-8", false, false, false) );
		pipeList.add(new TokenSequence2FeatureSequence());
		
		InstanceList instances = new InstanceList (new SerialPipes(pipeList));
		
		//Reader fileReader = new BufferedReader(new FileReader("data\\ap.txt"));
		
		Reader fileReader = new BufferedReader(new FileReader(filepath));
        instances.addThruPipe(new CsvIterator (fileReader, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"),3, 2, 1)); // data, label, name fields

        // Create a model with 100 topics, alpha_t = 0.01, beta_w = 0.01
        //  Note that the first parameter is passed as the sum over topics, while
        //  the second is the parameter for a single dimension of the Dirichlet prior.
        int numTopics = 20;
        
        ParallelTopicModel model = new ParallelTopicModel(numTopics);//, 1.0, 0.01);

        // Use two parallel samplers, which each look at one half the corpus and combine
        //  statistics after every iteration.
        model.setNumThreads(2);
        // Run the model for 50 iterations and stop (this is for testing only, 
        //  for real applications, use 1000 to 2000 iterations)
        model.setNumIterations(50);
        model.addInstances(instances);

        model.estimate();

      

        // The data alphabet maps word IDs to strings
        Alphabet dataAlphabet = instances.getDataAlphabet();
        
        // Estimate the topic distribution of the first instance, 
        //  given the current Gibbs state.
        double[] topicDistribution = model.getTopicProbabilities(0);

        // Get an array of sorted sets of word ID/count pairs
        ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();
        
        
        BufferedWriter wr = new BufferedWriter(new FileWriter("data\\topics.txt"));
        for (int topic = 0; topic < numTopics; topic++) {
            Iterator<IDSorter> iterator = topicSortedWords.get(topic).iterator();
            
          
            while (iterator.hasNext()) {
                IDSorter idCountPair = iterator.next();
                wr.write(dataAlphabet.lookupObject(idCountPair.getID()) + " ");
            }
            wr.write("\n");
        }
        wr.close();
        
        Formatter out = new Formatter(new StringBuilder(), Locale.US);
        // Show the words and topics in the first instance
//        FeatureSequence tokens = (FeatureSequence) model.getData().get(0).instance.getData();
//        LabelSequence topics = model.getData().get(0).topicSequence;
//     
//        for (int position = 0; position < tokens.getLength(); position++) {
//        	out.format("%s-%d ", dataAlphabet.lookupObject(tokens.getIndexAtPosition(position)), topics.getIndexAtPosition(position));
//        }
//        System.out.println(out);
        
        
        // Show top 5 words in topics with proportions for the first document
        for (int topic = 0; topic < numTopics; topic++) {
            Iterator<IDSorter> iterator = topicSortedWords.get(topic).iterator();
            
            out = new Formatter(new StringBuilder(), Locale.US);
            out.format("%d\t%.3f\t", topic, topicDistribution[topic]);
            int rank = 0;
            while (iterator.hasNext() && rank < 5) {
                IDSorter idCountPair = iterator.next();
                
                out.format("%s ", dataAlphabet.lookupObject(idCountPair.getID()));
                rank++;
            }
            System.out.println(out);
        }
        
        // Create a new instance with high probability of topic 0
        StringBuilder topicZeroText = new StringBuilder();
        Iterator<IDSorter> iterator = topicSortedWords.get(0).iterator();

        int rank = 0;
        while (iterator.hasNext()) {
            IDSorter idCountPair = iterator.next();
            topicZeroText.append(dataAlphabet.lookupObject(idCountPair.getID()) + " ");
            rank++;
        }

        // Create a new instance named "test instance" with empty target and source fields.
        InstanceList testing = new InstanceList(instances.getPipe());
        testing.addThruPipe(new Instance(topicZeroText.toString(), null, "test instance", null));

        TopicInferencer inferencer = model.getInferencer();
        double[] testProbabilities = inferencer.getSampledDistribution(testing.get(0), 10, 1, 5);
        System.out.println("0\t" + testProbabilities[0]);

	}
	
	public static void main(String[] args) throws Exception {
		
		//process the dataset
		TopicModeling topicModelingObj = new TopicModeling("data\\UserTweetsData.txt");
		
		//build the model
		topicModelingObj.applyTopicModeling("data\\Trainingdata.txt");
		
		//from the generated topics
		if(topicModelingObj.readTopicFile()){
			//get the testing tweet
			System.out.print("> ");
		    // Read user input
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		    String inputStr = br.readLine();
		    while (!inputStr.equals("")) {
		    	
		    	//process the testing tweet from inbuilt model
		    	String testTweet = inputStr.toLowerCase();
		    	
		    	int[] probabilisticCount = new int[topicModelingObj.topicsMap.size()];
		    	int totalwordCount = 0;
		    	
		    	String[] splitdata = testTweet.split(" ");
		    	for(String word : splitdata){
		    		if(!topicModelingObj.stopwordSet.contains(word)){
		    			
		    			totalwordCount++;
		    			boolean foundInTopic = false;
		    			
			    		Iterator<Entry<Integer,List<String>>> it = topicModelingObj.topicsMap.entrySet().iterator();
			    		while(it.hasNext()){
			    			Entry<Integer, List<String>> e = it.next();
			    			List<String> topiclist = e.getValue();
			    			if(topiclist.contains(word)){
			    				foundInTopic = true;
			    				probabilisticCount[e.getKey() - 1]++;
			    			}
			    		}
			    		
			    		if(!foundInTopic)
			    			totalwordCount--;
		    		}
		    	}
		    	
		    	double[] probabilityArr = new double[topicModelingObj.topicsMap.size()];
		    	double highestProbability = 0; int highestProbabilityIndex = -1;
		    	for(int i=0;i<topicModelingObj.topicsMap.size();i++){
		    		if(totalwordCount != 0){
			    		probabilityArr[i] = (double)probabilisticCount[i]/(double)totalwordCount;
			    		if(highestProbability < probabilityArr[i]){
			    			highestProbability = probabilityArr[i];
			    			highestProbabilityIndex = i;
			    		}
			    		System.out.println(i+1 + "\t" + probabilityArr[i]);
		    		}
		    		else{
		    			highestProbabilityIndex = -1;
		    		}
		    	}
		    	
				//show the result - classification
		    	if(highestProbabilityIndex!=-1)
		    		System.out.println("Tweet belongs to topic " + (highestProbabilityIndex+1));
		    	else
		    		System.out.println("Difficult to judge this tweet");
		    	
	            System.out.print("\n> ");
                inputStr = br.readLine();
		   }
		   br.close();

		}else
		{
			throw new Exception("Topics not yet created .. incorrect call to method");
		}
	}
	
	private boolean readTopicFile(){
		boolean success = false;
		try{
			
			BufferedReader reader = new BufferedReader(new FileReader("data\\topics.txt"));
			String line;
			int topicCount = 1;
			while((line = reader.readLine())!=null){
				
				String[] wordlist = line.split(" ");
				List<String> topicwordList = new ArrayList<String>();
				for(String word : wordlist){
					topicwordList.add(word);
				}
				
				topicsMap.put(topicCount, topicwordList);
				topicCount++;
			}
			reader.close();
			success = true;
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return success;
	}

	private void readInputFile(String filepath){
		try{
			BufferedReader reader = new BufferedReader(new FileReader(filepath));
			String line;
			int counter =  1;
			dataInstances = new HashMap<String,String>();
			while((line = reader.readLine())!=null){
				String[] splitdata = line.split("\t");
				if(splitdata.length==2){
					dataInstances.put("ut" + counter, splitdata[1]);
					counter++;
				}
				else
					System.out.println("File not properly formatted............. line " + counter );
			}
			reader.close();
		}catch(IOException ex){
			ex.printStackTrace();
		}
	}
	
	private void writeProcessedFile(String filepath){
		try{
			BufferedWriter writer = new BufferedWriter(new FileWriter(filepath));
			Iterator<Entry<String, String>> instanceIterator = dataInstances.entrySet().iterator();
			while(instanceIterator.hasNext()){
				Entry<String, String> e = instanceIterator.next();
				if((e.getValue().trim().trim()).length() != 0){
					writer.write(e.getKey() + "\tX\t" +  e.getValue() + "\n");
				}
				
			}
			writer.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
 	private void processData(){
		if(dataInstances!=null){
			Iterator<Entry<String, String>> instanceIterator = dataInstances.entrySet().iterator();
			while(instanceIterator.hasNext()){
				Entry<String, String> e = instanceIterator.next();
				String instanceID = e.getKey();
				String rawTweet = e.getValue();
				
				String processedTweet = null;
				try{
					//remove @
					MentionsUrlsFilter objMentionUrl = new MentionsUrlsFilter();
					processedTweet = objMentionUrl.filterMentions(rawTweet);
					//remove URL
					processedTweet = objMentionUrl.filterURL(processedTweet);
					//expand tweets
					ExpandTweets objExpandTweets = new ExpandTweets("data\\abbrevationsInTwitter.txt");
					processedTweet = objExpandTweets.processTweetExpansion(processedTweet);
					
					if(processedTweet!=null)
						dataInstances.put(instanceID, processedTweet);
				}
				catch(Exception ex){
					ex.printStackTrace();
				}
			}
		}
		
	}
}
