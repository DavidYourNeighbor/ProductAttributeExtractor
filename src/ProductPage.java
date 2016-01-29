import java.io.*;
import java.util.Map;
import java.util.Vector;

import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.*;

public class ProductPage {
	
	public String docLocation;
	public String docText;
	public String [] tokenizedText;
	public String [] POSTags;
	public String [] nounPhraseLabels;
	public Vector<String> nounPhrases;
		
	// Product page constructor
	public ProductPage(String tempLoc) throws Exception
	{
		docLocation = tempLoc;
		docText = "";
		nounPhrases = new Vector <String>();
		
		this.extractHTMLTextToString();		
		this.tokenizeText();
		this.tagPOS();
		this.labelPhrases();
		this.extractNounPhrases();		
	}
	
	//This function extracts the noun phrases of each product page
	private void extractNounPhrases()
	{
		String temp = "";
		nounPhrases = new Vector<String>();
		
		for(int i = 0; i < POSTags.length; i++)
		{
			if(POSTags[i].equals("NNP"))
			{
				if(temp.equals(""))
				{
					temp+=tokenizedText[i];
				}
				else
				{
					temp+=" ";
					temp+=tokenizedText[i];
				}
			}
			else
			{
				if(!temp.equals(""))
				{
					if(!isOneWord(temp))
					{
						nounPhrases.add(temp);
					}
					temp = "";
				}
			}
		}
		
		nounPhrases.add(temp);
	}
	
	// This function determines whether a string is a unigram (single word) or not
	private boolean isOneWord(String temp)
	{
		for(int i = 0; i < temp.length(); i++)
		{
			if(temp.charAt(i) == ' ')
				return false;
		}
		return true;
	}

	//This function returns the noun phrases for a Product Page
	public Vector<String> getNounPhrases()
	{
		return this.nounPhrases;
	}
	
	// This function function adds each word to a set of frequencies for the cluster of HTML product pages
	public void addToClusterFrequencies(String temp, Map<String, MutableInt> clusterFrequencies)
	{
		if(!clusterFrequencies.containsKey(temp))
		{
			MutableInt temp2 = new MutableInt(1);
			clusterFrequencies.put(temp, temp2);
		}
		else
		{
			MutableInt temp2 = clusterFrequencies.get(temp);
			temp2.changeValue(temp2.getValue());
			clusterFrequencies.put(temp, temp2);
		}
	}
	
	// This function tags different works as different POS (parts of speech)
	public void tagPOS()
	{
		InputStream modelIn = null;

		try {
		    modelIn = new FileInputStream("C:/Users/David Naber/Documents/IREP Project/en-pos-maxent.bin");
		    POSModel model = new POSModel(modelIn);
			POSTaggerME tagger = new POSTaggerME(model);
			POSTags = tagger.tag(tokenizedText);
		}
		catch (IOException e) {
		  // Model loading failed, handle the error
		  e.printStackTrace();
		}
		finally {
		  if (modelIn != null) {
		    try {
		      modelIn.close();
		    }
		    catch (IOException e) {
		    }
		  }
		}
	}
	
	// This function tokenizes the text from each page
	public void tokenizeText() throws Exception
	{
		InputStream modelIn = new FileInputStream("C:/Users/David Naber/Documents/IREP Project/en-token.bin");
		

		try {
		  TokenizerModel model = new TokenizerModel(modelIn);
			
			Tokenizer tokenizer = new TokenizerME(model);
			
			tokenizedText = tokenizer.tokenize(docText);
		}
		catch (IOException e) {
		  e.printStackTrace();
		}
		finally {
		  if (modelIn != null) {
		    try {
		      modelIn.close();
		    }
		    catch (IOException e) {
		    }
		  }
		}
	}
	
	// This function extracts the text from the HTML document into a string
	public void extractHTMLTextToString()
	{
		FileReader fr;
		try
		{
			fr = new FileReader(docLocation);
			try
			{
				docText = ArticleExtractor.INSTANCE.getText(fr);
			}
			catch (BoilerpipeProcessingException e)
			{
				System.out.println("Unfortunately, the boilerplate couldn't be extracted from the FileReader.");
			}
		}
		catch (FileNotFoundException e)
		{
			System.out.println("Unfortunately, this file couldn't be converted to a FileReader.");
		}
	}
	
	// This function labels all phrases with different POS (part of speech) tags
	public void labelPhrases()
	{		
		InputStream modelIn2 = null;
		ChunkerModel model = null;

		try {
		  modelIn2 = new FileInputStream("C:/Users/David Naber/Documents/IREP Project/en-chunker.bin");
		  model = new ChunkerModel(modelIn2);
		} catch (IOException e) {
		  // Model loading failed, handle the error
		  e.printStackTrace();
		} finally {
		  if (modelIn2 != null) {
		    try {
		      modelIn2.close();
		    } catch (IOException e) {
		    }
		  }
		}
		
		ChunkerME chunker = new ChunkerME(model);
		nounPhraseLabels = chunker.chunk(tokenizedText, POSTags);
	}
	
	// This function returns the location for a document
	public String getDocLocation()
	{
		return this.docLocation;
	}
	
	// This function prints the text of a document
	public void printDocText() 
	{
		System.out.println(docText);
		return;
	}	
	
	// This function returns the text of a document
	public String getDocText()
	{
		return this.docText;
	}
	
	// This function returns the length of a document's text string
	public int getDocTextLength()
	{
		return this.docText.length();
	}
	
	// This function returns the character at an index of the document's text string
	public char getDocTextCharAt(int index)
	{
		return this.docText.charAt(index);
	}
	
	//This function prints all tokenized text
	public void printTokenizedText()
	{
		for(int i = 0; i < tokenizedText.length; i++)
		{
			System.out.println(tokenizedText[i]);
		}
	}
	
	//This function prints all POS (part of speech) tags for the text
	public void printPOSTags()
	{
		for(int i = 0; i < POSTags.length; i++)
		{
			System.out.println(POSTags[i]);
		}
	}
	
	// This function prints out the labels of the noun phrases
	public void printNounPhraseLabels()
	{
		for(int i = 0; i < nounPhraseLabels.length; i++)
		{
			System.out.println(nounPhraseLabels[i]);
		}
	}
	
	// This function defines a hash function for a string
	public static int StringHasher(String s, int M)
	{
		//Use this function to implement the hashing of the shingles
	     int intLength = s.length() / 4;
	     int sum = 0;
	     
	     for (int j = 0; j < intLength; j++)
	     {
	    	 char c[] = s.substring(j * 4, (j * 4) + 4).toCharArray();
	    	 int mult = 1;
	    	 
	    	 for (int k = 0; k < c.length; k++)
	    	 {
	    		 sum += c[k] * mult;
	    		 mult *= 256;
	    	 }
	     }

	     char c[] = s.substring(intLength * 4).toCharArray();
	     int mult = 1;
	     
	     for (int k = 0; k < c.length; k++)
	     {
	    	sum += c[k] * mult;
	       	mult *= 256;
	     }

	     return(Math.abs(sum) % M);
	}
}
