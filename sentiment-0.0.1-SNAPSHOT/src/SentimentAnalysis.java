
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;

import uk.ac.wlv.sentistrength.*;
import au.com.bytecode.opencsv.*;

@ManagedBean
@ViewScoped
public class SentimentAnalysis {
  
	private UploadedFile uploadedFile;
	private String fileName;
	private List<String> scores = new ArrayList<String>();
	private OutputStream output;
	private String newFileName;
	private static String DOWNLOAD_DIRECTORY = "/Users/rorysie/Documents/OU/Projecten/JavaEE/files/";
	private static String SENTISTRENGTH_DIRECTORY ="/Users/rorysie/Documents/OU/Tools/SentiStrength/SentStrength_Data_NL/";
	public boolean downloadReady = false;
	private StreamedContent file;
	
	public boolean getDownloadReady() {
		return downloadReady;
	}
	
	public StreamedContent getFile() {  
        return file;  
    }
	
	public String getFileName() {
		return fileName;
	}
	
	public List<String> getScores() {
		return scores;
	}
  
	private void readFile(UploadedFile uploadedfile) throws IOException{
		//create a CSV file to write to
		CSVWriter csvWriter = createFile();
		
		
		//get the current Faces context (browser window) to post messages to 
		/*
		FacesContext context = FacesContext.getCurrentInstance();
		//feedback successful upload		
		String text = "File successfully copied to" + file.getAbsolutePath() + "!";
		context.addMessage(null, new FacesMessage("Successful", text));
		*/
		
		//get the input stream
		InputStream input = uploadedFile.getInputstream();
		
		//create a BufferedReader to read the lines
		BufferedReader br = new BufferedReader(new InputStreamReader(input));
		
		//create new SentiStrength instance
		SentiStrength sentiStrength = new SentiStrength();
		//use the command below if you do want an explanation of the sentiment
		String ssthInitialisation[] = {"sentidata", SENTISTRENGTH_DIRECTORY, "explain"};
		//use the command below if you do not want an explanation of the sentiment
		//String ssthInitialisation[] = {"sentidata", SENTISTRENGTH_DIRECTORY};
		//Initialise
		sentiStrength.initialise(ssthInitialisation); 
		
		//analyse and Write the CSV file
		analyseAndWriteCSV(br, csvWriter, sentiStrength);
		
		//close the input stream
		input.close();
		//close the CSV file
		csvWriter.close();
	}

	private void analyseAndWriteCSV(BufferedReader br, CSVWriter csvWriter, SentiStrength sentiStrength) throws IOException{
		String line = "";
		String score = "";
		while(br.ready()){
			//read the sentence
			line = br.readLine();
			//compute the score
			score = sentiStrength.computeSentimentScores(line);
			//append the score to a global List of scores
			scores.add(score);
			//write the score to the CSV file
			writeScoreToCSV(line, score, csvWriter);
			//print results
			FacesContext context = FacesContext.getCurrentInstance();
			//context.addMessage(null, new FacesMessage("Text analysed:", line));
			context.addMessage(null, new FacesMessage("Sentiment score", score));
		}
		br.close();
	}
  
	//creates a new file to write to
	private CSVWriter createFile() throws IOException{
		//set the file name
		String prefix = FilenameUtils.getBaseName(uploadedFile.getFileName()); 
		String suffix = FilenameUtils.getExtension(uploadedFile.getFileName());
		//newFileName = prefix+"."+suffix;
		
		//create the current date String
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmSS");
        Date currDate = new Date();
        String currDateString = sdf.format(currDate);
        
        /*
        File file = new File("/Users/rorysie/Documents/OU/Projecten/JavaEE/files/"+prefix+currDateString+".csv");
		file.createNewFile();
		*/
		
		//create a CSVWriter to write the scores to
		CSVWriter outputWriter = new CSVWriter(new FileWriter(DOWNLOAD_DIRECTORY + prefix + currDateString+".csv"));
		
		//file = new DefaultStreamedContent(new FileInputStream(newFileName), "text/plain");
		//downloadReady = true;
		//RequestContext context = RequestContext.getCurrentInstance();
		//context.update("form");
		return outputWriter;
	}
	
	//writes a line to a comma-separated file with positive value on position 0, and negative value on position 1
	private void writeScoreToCSV(String line, String score, CSVWriter outputWriter) throws IOException{ 
		String[] scoreArray = new String[2];
		String[] entries = new String[5];
		//split the score String into an array 
		scoreArray = score.split(" ");
		//save the values
		entries[0] = line;
		entries[1] = scoreArray[0];
		entries[2] = scoreArray[1];
		int sum = Integer.parseInt(scoreArray[0]) + Integer.parseInt(scoreArray[1]);
		//System.out.println("<begin>" + Integer.toString(sum) + "<end>");
		entries[3] = Integer.toString(sum);
		entries[4] = scoreArray[2];
		
		outputWriter.writeNext(entries); 		
	}
	
	private void readTweets(){
		//while !eof
	  	int sentiment = 0;
	  	//String tweet = readTweet()
	  	//String tweetText = extractTweet(tweet);
	  	//int sentiment = getSentiment(tweetText);
	  	//saveSentiment(sentiment);
	}
  
	private int getSentiment(String tweetText){
		int sentimentIndex = 0;
		return sentimentIndex;
	}
  
	private void saveSentiment(int sentiment){
	  
	}
  
	public void analyse(FileUploadEvent event) throws IOException {
		uploadedFile = event.getFile();
		
		/*
		String text = "Your file was successfully uploaded!";
		FacesContext context = FacesContext.getCurrentInstance();
		context.addMessage(null, new FacesMessage("Successful", text));
		*/
		readFile(uploadedFile);
        
		//String XMLString = readXML(XMLFile);
		//DocumentBuilder db = XMLToDocumentBuilder(XMLString);
		//readTweets
	}    
}
