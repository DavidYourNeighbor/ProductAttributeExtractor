//@author David Naber
//This program will extract the most important noun phrases from a cluster of documents that describe the same product.

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

public class ProductAttributeExtractor extends ProductPage
{	
	public static int minimumClusterSize = 10;
	
	public static void main(String[] args) throws Exception
	{
		//Algorithm Plan:
		//	1. For every document cluster, we extract noun phrases. (openNLP https://opennlp.apache.org/ can be used) - DONE
		//	2. We compute similarities between noun phrases inside the same doc-cluster
		//	3. We cluster the noun phrases, with some agglomerative algorithm, have a look at http://nlp.stanford.edu/IR-book/html/htmledition/hierarchical-agglomerative-clustering-1.html#fig:clustersimilarities
		//	4. For every noun phrase cluster, we extract the most informative word or phrase by some information metric as the one used in the paper		
	
		Vector<ProductPage> productCluster1;
		Vector<ProductPage> productCluster2;
		Vector<ProductPage> productCluster3;
		Vector<ProductPage> allClusters = new Vector<ProductPage>();
		
		//TODO: write single function that initializes all documents in one call
		productCluster1 = initializeDocuments1();
		productCluster2 = initializeDocuments2();
		productCluster3 = initializeDocuments3();
		
		allClusters.addAll(productCluster1);
		allClusters.addAll(productCluster2);
		allClusters.addAll(productCluster3);
		
		Vector<String> nounPhrases1 = extractNounPhrases(productCluster1);
		Vector<String> nounPhrases2 = extractNounPhrases(productCluster2);
		Vector<String> nounPhrases3 = extractNounPhrases(productCluster3);
		Vector<String> allClustersNounPhrases = extractNounPhrases(allClusters);
		
		Vector<Vector<MutableDouble>> simMatrix1 = computeSimilarityMatrix(nounPhrases1);
		Vector<Vector<MutableDouble>> simMatrix2 = computeSimilarityMatrix(nounPhrases2);
		Vector<Vector<MutableDouble>> simMatrix3 = computeSimilarityMatrix(nounPhrases3);
		
		Vector<String> copyNounPhrases1 = new Vector<String>(nounPhrases1);
		Vector<String> copyNounPhrases2 = new Vector<String>(nounPhrases2);
		Vector<String> copyNounPhrases3 = new Vector<String>(nounPhrases3);
		
		Vector<Vector<String>> clusters1 = clusterNounPhrases(copyNounPhrases1, simMatrix1);
		Vector<Vector<String>> clusters2 = clusterNounPhrases(copyNounPhrases2, simMatrix2);
		Vector<Vector<String>> clusters3 = clusterNounPhrases(copyNounPhrases3, simMatrix3);
		
		pruneClusters(clusters1, clusters2, clusters3);
		
		Vector<String> productAttributes1 = extractInformativeNounPhrasesFromClusters(clusters1, allClustersNounPhrases, nounPhrases1);
		Vector<String> productAttributes2 = extractInformativeNounPhrasesFromClusters(clusters2, allClustersNounPhrases, nounPhrases2);
		Vector<String> productAttributes3 = extractInformativeNounPhrasesFromClusters(clusters3, allClustersNounPhrases, nounPhrases3);
		
		for(String i : productAttributes1)
		{
			System.out.println(i);
		}
		
		for(String j : productAttributes2)
		{
			System.out.println(j);
		}
		
		for(String k : productAttributes3)
		{
			System.out.println(k);
		}
	}
	
	//Extracts the informative nouns from all clusters
	public static Vector<String> extractInformativeNounPhrasesFromClusters(Vector<Vector<String>> clusters, Vector<String> allClustersNounPhrases, Vector<String> nounPhrases)
	{
		Vector<String> informativeNounPhrases = new Vector<String>();
		
		for(Vector<String> i : clusters)
		{
			String phrase = extractInformativeNounPhraseFromOneCluster(i, nounPhrases, allClustersNounPhrases);
			informativeNounPhrases.add(phrase);
		}
		
		return informativeNounPhrases;
	}

	//Extracts the informative noun from a single cluster
	public static String extractInformativeNounPhraseFromOneCluster(Vector<String> singleCluster, Vector<String> nounPhrases, Vector<String> allClustersNounPhrases)
	{
		String bestPhrase = "";
		double bestScore = 0.0;
		
		for(int i = 0; i < singleCluster.size(); i++)
		{
			if(nounPhraseScore(singleCluster.elementAt(i), nounPhrases, allClustersNounPhrases) > bestScore)
			{
				bestScore = nounPhraseScore(singleCluster.elementAt(i), nounPhrases, allClustersNounPhrases);
				bestPhrase = singleCluster.elementAt(i);
			}
		}
		
		return bestPhrase;
	}

	//Assigns a score to each noun phrase to rank in terms of importance
	public static double nounPhraseScore(String noun, Vector<String> nounPhrases, Vector<String> allClustersNounPhrases)
	{
		double score = 0.0;		
		double pofx = 0.0;
		double qofx = 0.0;
		
		pofx = numOfOccurrencesInCluster(noun, nounPhrases) / nounPhrases.size();
		qofx = numOfOccurrencesInCluster(noun, allClustersNounPhrases) / allClustersNounPhrases.size();
		
		score = java.lang.Math.log10(pofx / qofx);
		score *= pofx;
		
		return score;
	}
	
	//Prunes the clusters of each vector	
	public static void pruneClusters(Vector<Vector<String>> clusters1, Vector<Vector<String>> clusters2, Vector<Vector<String>> clusters3)
	{
		for(int i = 0; i < clusters1.size(); i++)
		{
			if(clusters1.elementAt(i).size() <= minimumClusterSize)
			{
				clusters1.remove(i);
			}
		}
		
		for(int j = 0; j < clusters2.size(); j++)
		{
			if(clusters2.elementAt(j).size() <= minimumClusterSize)
			{
				clusters2.remove(j);
			}
		}
		
		for(int k = 0; k < clusters3.size(); k++)
		{
			if(clusters3.elementAt(k).size() <= minimumClusterSize)
			{
				clusters3.remove(k);
			}
		}
	}
	
	//Clusters the noun phrases of the similarity matrix
	public static Vector<Vector<String>> clusterNounPhrases(Vector<String> nounPhrases, Vector<Vector<MutableDouble>> simMatrix)
	{
		Vector<Vector<String>> clusteredNounPhrases = new Vector<Vector<String>>();
		
		while(!nounPhrases.elementAt(0).isEmpty())
		{			
			IntPair indices = getHighestSimilarity(simMatrix);
			removeIndicesAndMergeClusters(indices, simMatrix, nounPhrases, clusteredNounPhrases);
		}
		
		return clusteredNounPhrases;
	}
	
	//Reduces similarity matrix and merges the clusters of product pages
	public static void removeIndicesAndMergeClusters(IntPair indices,	Vector<Vector<MutableDouble>> simMatrix, Vector<String> nounPhrases, Vector<Vector<String>> clusteredNounPhrases)
	{
		int i = indices.getA();
		int j = indices.getB();
		
		IntPair temp = isInAnotherCluster(nounPhrases.elementAt(j), clusteredNounPhrases);
		
		if(temp.getA() != -1)
		{
			clusteredNounPhrases.elementAt(temp.getA()).add(nounPhrases.elementAt(j));
		}
		else
		{
			Vector<String> tempCluster = new Vector<String>();
			tempCluster.add(nounPhrases.elementAt(i));
			tempCluster.add(nounPhrases.elementAt(j));
			clusteredNounPhrases.add(tempCluster);
		}
		
		nounPhrases.remove(j);
		
		for(int index = 0; index < simMatrix.size(); index++)
		{
			simMatrix.elementAt(index).remove(j);
		}
		
		simMatrix.remove(j);
	}
	
	//Finds whether a particular phrase is in another cluster
	public static IntPair isInAnotherCluster(String temp, Vector<Vector<String>> clusteredNounPhrases)
	{
		for(int i = 0; i < clusteredNounPhrases.size(); i++)
		{
			for(int j = 0; j < clusteredNounPhrases.elementAt(i).size(); j++)
			{
				if(temp.equals(clusteredNounPhrases.elementAt(i).elementAt(j)))
				{
					return new IntPair(i, j);
				}
			}
		}
		return new IntPair(-1, -1);
	}

	//Finds the highest similarity between two objects in the similarity matrix
	public static IntPair getHighestSimilarity(Vector<Vector<MutableDouble>> simMatrix)
	{
		IntPair indices = new IntPair(0, 0);
		double temp = 0.0;
		
		for(int i = 0; i < simMatrix.size(); i++)
		{
			for(int j = 0; j < simMatrix.elementAt(i).size(); j++)
			{
				if(simMatrix.elementAt(i).elementAt(j).getValue() > temp)
				{
					temp = simMatrix.elementAt(i).elementAt(j).getValue();
					indices.changeVals(i, j);
				}
			}
		}
		
		return indices;
	}

	//Finds the number of occurences in a phrase cluster -- TODO: Find more runtime-efficient way
	public static double numOfOccurrencesInCluster(String noun, Vector<String> nounPhrases)
	{
		int counter = 0;
		for(String i : nounPhrases)
		{
			if(i.equals(noun))
			{
				counter++;
			}
		}
		return (double) counter;
	}

	//Extracts noun phrases of a cluster of product pages
	public static Vector<String> extractNounPhrases(Vector<ProductPage> collectionOfDocuments)
	{
		Vector<String> nounPhrases = new Vector<String>();
		
		for(ProductPage i : collectionOfDocuments)
		{
			nounPhrases.addAll(i.getNounPhrases());
		}
		
		return nounPhrases;
	}
	
	//Finds similarity matrix
	public static Vector<Vector<MutableDouble>> computeSimilarityMatrix(Vector<String> nounPhrases)
	{
		Vector<Vector<MutableDouble>> similarityMatrix = new Vector<Vector<MutableDouble>>();
		
		for(int i = 0; i < nounPhrases.size(); i++)
		{
			Vector<MutableDouble> column = new Vector<MutableDouble>();
			for(int j = 0; j < nounPhrases.size(); j++)
			{
				double tempSim = computeSimilarityBetween(nounPhrases.elementAt(i), nounPhrases.elementAt(j));
				if(i!=j)
				{
					column.add(new MutableDouble(tempSim));
				}
				else
				{
					column.add(new MutableDouble());
				}
			}
			similarityMatrix.add(column);
		}
		
		return similarityMatrix;
	}
	
	//Finds similarity between two phrases
	public static double computeSimilarityBetween(String A, String B)
	{
		Vector<String> unigramsA = getUnigramsOfPhrase(A);
		Vector<String> unigramsB = getUnigramsOfPhrase(B);		
		
		if(unigramsA.size() + unigramsB.size() == 0)
		{
			return 0.0;
		}
		
		String[] unigramsAarr = new String[unigramsA.size()];
		String[] unigramsBarr = new String[unigramsB.size()];		
		unigramsA.copyInto(unigramsAarr);
		unigramsB.copyInto(unigramsBarr);
		
		Set<String> aSet = new HashSet<String>();
		Collections.addAll(aSet, unigramsAarr);			
		Set<String> bSet = new HashSet<String>();
		Collections.addAll(bSet, unigramsBarr);		
		Set<String> unigramsIntersection = new HashSet<String>();
		Collections.addAll(unigramsIntersection, unigramsAarr);
		unigramsIntersection.retainAll(bSet);
		
		double similarity = unigramsIntersection.size();
		similarity *= 2;
		similarity /= (unigramsA.size() + unigramsB.size());
		
		return similarity;
	}
	
	//Decomposes phrase into unigrams
	public static Vector<String> getUnigramsOfPhrase(String A)
	{
		Vector<String> temp = new Vector<String>();
		String tempString = "";
		for(int i = 0; i < A.length(); i++)
		{
			if(A.charAt(i) == ' ')
			{
				temp.add(tempString);
			}
			else if(i == A.length()-1)
			{
				tempString+=A.charAt(i);
				temp.add(tempString);
			}
			else
			{
				tempString+=A.charAt(i);
			}
		}
		
		return temp;
	}
	
	public ProductAttributeExtractor(String tempLoc, Map<String, MutableInt> clusterFrequencies, Vector<MutableInt> singleWordNounPhraseIndices, Vector<String> nounPhrases) throws Exception
	{
		super(tempLoc);
	}

	//Initialize third set of documents - used for testing
	public static Vector<ProductPage> initializeDocuments3() throws Exception
	{
		Vector<ProductPage> collectionOfDocuments = new Vector<ProductPage>();
		
		ProductPage testProductPage0 = new ProductPage("C:/Users/David Naber/Documents/IREP Project/product_pages/data/CollectedData/Asus MX299Q LED Monitor LCD Monitor Price Comparison0");
		ProductPage testProductPage1 = new ProductPage("C:/Users/David Naber/Documents/IREP Project/product_pages/data/CollectedData/Asus MX299Q LED Monitor LCD Monitor Price Comparison1");
		ProductPage testProductPage2 = new ProductPage("C:/Users/David Naber/Documents/IREP Project/product_pages/data/CollectedData/Asus MX299Q LED Monitor LCD Monitor Price Comparison2");
		ProductPage testProductPage3 = new ProductPage("C:/Users/David Naber/Documents/IREP Project/product_pages/data/CollectedData/Asus MX299Q LED Monitor LCD Monitor Price Comparison3");
		ProductPage testProductPage4 = new ProductPage("C:/Users/David Naber/Documents/IREP Project/product_pages/data/CollectedData/Asus MX299Q LED Monitor LCD Monitor Price Comparison4");
		ProductPage testProductPage5 = new ProductPage("C:/Users/David Naber/Documents/IREP Project/product_pages/data/CollectedData/Asus MX299Q LED Monitor LCD Monitor Price Comparison5");
		ProductPage testProductPage6 = new ProductPage("C:/Users/David Naber/Documents/IREP Project/product_pages/data/CollectedData/Asus MX299Q LED Monitor LCD Monitor Price Comparison6");
		ProductPage testProductPage7 = new ProductPage("C:/Users/David Naber/Documents/IREP Project/product_pages/data/CollectedData/Asus MX299Q LED Monitor LCD Monitor Price Comparison7");
		ProductPage testProductPage8 = new ProductPage("C:/Users/David Naber/Documents/IREP Project/product_pages/data/CollectedData/Asus MX299Q LED Monitor LCD Monitor Price Comparison8");
		ProductPage testProductPage9 = new ProductPage("C:/Users/David Naber/Documents/IREP Project/product_pages/data/CollectedData/Asus MX299Q LED Monitor LCD Monitor Price Comparison9");
																						
		collectionOfDocuments.add(testProductPage0);
		collectionOfDocuments.add(testProductPage1);
		collectionOfDocuments.add(testProductPage2);
		collectionOfDocuments.add(testProductPage3);
		collectionOfDocuments.add(testProductPage4);
		collectionOfDocuments.add(testProductPage5);
		collectionOfDocuments.add(testProductPage6);
		collectionOfDocuments.add(testProductPage7);
		collectionOfDocuments.add(testProductPage8);
		collectionOfDocuments.add(testProductPage9);
		
		System.out.println("Done initializing documents 3.");
		return collectionOfDocuments;
	}
	
	//Initialize second set of documents - used for testing
	public static Vector<ProductPage> initializeDocuments2() throws Exception
	{
		Vector<ProductPage> collectionOfDocuments = new Vector<ProductPage>();
		
		ProductPage testProductPage0 = new ProductPage("C:/Users/David Naber/Documents/IREP Project/product_pages/data/CollectedData/Lenovo IdeaPad Yoga 2 Pro Touchscreen Ultrabook Ultrabook Price Comparison0");
		ProductPage testProductPage1 = new ProductPage("C:/Users/David Naber/Documents/IREP Project/product_pages/data/CollectedData/Lenovo IdeaPad Yoga 2 Pro Touchscreen Ultrabook Ultrabook Price Comparison1");
		ProductPage testProductPage2 = new ProductPage("C:/Users/David Naber/Documents/IREP Project/product_pages/data/CollectedData/Lenovo IdeaPad Yoga 2 Pro Touchscreen Ultrabook Ultrabook Price Comparison2");
		ProductPage testProductPage3 = new ProductPage("C:/Users/David Naber/Documents/IREP Project/product_pages/data/CollectedData/Lenovo IdeaPad Yoga 2 Pro Touchscreen Ultrabook Ultrabook Price Comparison3");
		ProductPage testProductPage4 = new ProductPage("C:/Users/David Naber/Documents/IREP Project/product_pages/data/CollectedData/Lenovo IdeaPad Yoga 2 Pro Touchscreen Ultrabook Ultrabook Price Comparison4");
		ProductPage testProductPage5 = new ProductPage("C:/Users/David Naber/Documents/IREP Project/product_pages/data/CollectedData/Lenovo IdeaPad Yoga 2 Pro Touchscreen Ultrabook Ultrabook Price Comparison5");
		ProductPage testProductPage6 = new ProductPage("C:/Users/David Naber/Documents/IREP Project/product_pages/data/CollectedData/Lenovo IdeaPad Yoga 2 Pro Touchscreen Ultrabook Ultrabook Price Comparison6");
		ProductPage testProductPage7 = new ProductPage("C:/Users/David Naber/Documents/IREP Project/product_pages/data/CollectedData/Lenovo IdeaPad Yoga 2 Pro Touchscreen Ultrabook Ultrabook Price Comparison7");
		ProductPage testProductPage8 = new ProductPage("C:/Users/David Naber/Documents/IREP Project/product_pages/data/CollectedData/Lenovo IdeaPad Yoga 2 Pro Touchscreen Ultrabook Ultrabook Price Comparison8");
		ProductPage testProductPage9 = new ProductPage("C:/Users/David Naber/Documents/IREP Project/product_pages/data/CollectedData/Lenovo IdeaPad Yoga 2 Pro Touchscreen Ultrabook Ultrabook Price Comparison9");
																						
		collectionOfDocuments.add(testProductPage0);
		collectionOfDocuments.add(testProductPage1);
		collectionOfDocuments.add(testProductPage2);
		collectionOfDocuments.add(testProductPage3);
		collectionOfDocuments.add(testProductPage4);
		collectionOfDocuments.add(testProductPage5);
		collectionOfDocuments.add(testProductPage6);
		collectionOfDocuments.add(testProductPage7);
		collectionOfDocuments.add(testProductPage8);
		collectionOfDocuments.add(testProductPage9);
		
		System.out.println("Done initializing documents 2.");
		return collectionOfDocuments;
	}
	
	//Initializes the first set of documents - used for testing
	public static Vector<ProductPage> initializeDocuments1() throws Exception
	{
		Vector<ProductPage> collectionOfDocuments = new Vector<ProductPage>();
		
		ProductPage testProductPage0 = new ProductPage("C:/Users/David Naber/Documents/IREP Project/product_pages/data/CollectedData/4Gamers PS4 Camera TV Clip Gaming Sensor Holder Gaming Accessory Price Comparison0");
		ProductPage testProductPage1 = new ProductPage("C:/Users/David Naber/Documents/IREP Project/product_pages/data/CollectedData/4Gamers PS4 Camera TV Clip Gaming Sensor Holder Gaming Accessory Price Comparison1");
		ProductPage testProductPage2 = new ProductPage("C:/Users/David Naber/Documents/IREP Project/product_pages/data/CollectedData/4Gamers PS4 Camera TV Clip Gaming Sensor Holder Gaming Accessory Price Comparison2");
		ProductPage testProductPage3 = new ProductPage("C:/Users/David Naber/Documents/IREP Project/product_pages/data/CollectedData/4Gamers PS4 Camera TV Clip Gaming Sensor Holder Gaming Accessory Price Comparison3");
		ProductPage testProductPage4 = new ProductPage("C:/Users/David Naber/Documents/IREP Project/product_pages/data/CollectedData/4Gamers PS4 Camera TV Clip Gaming Sensor Holder Gaming Accessory Price Comparison4");
		ProductPage testProductPage5 = new ProductPage("C:/Users/David Naber/Documents/IREP Project/product_pages/data/CollectedData/4Gamers PS4 Camera TV Clip Gaming Sensor Holder Gaming Accessory Price Comparison5");
		ProductPage testProductPage6 = new ProductPage("C:/Users/David Naber/Documents/IREP Project/product_pages/data/CollectedData/4Gamers PS4 Camera TV Clip Gaming Sensor Holder Gaming Accessory Price Comparison6");
		ProductPage testProductPage7 = new ProductPage("C:/Users/David Naber/Documents/IREP Project/product_pages/data/CollectedData/4Gamers PS4 Camera TV Clip Gaming Sensor Holder Gaming Accessory Price Comparison7");
		ProductPage testProductPage8 = new ProductPage("C:/Users/David Naber/Documents/IREP Project/product_pages/data/CollectedData/4Gamers PS4 Camera TV Clip Gaming Sensor Holder Gaming Accessory Price Comparison8");
		ProductPage testProductPage9 = new ProductPage("C:/Users/David Naber/Documents/IREP Project/product_pages/data/CollectedData/4Gamers PS4 Camera TV Clip Gaming Sensor Holder Gaming Accessory Price Comparison9");
																						
		collectionOfDocuments.add(testProductPage0);
		collectionOfDocuments.add(testProductPage1);
		collectionOfDocuments.add(testProductPage2);
		collectionOfDocuments.add(testProductPage3);
		collectionOfDocuments.add(testProductPage4);
		collectionOfDocuments.add(testProductPage5);
		collectionOfDocuments.add(testProductPage6);
		collectionOfDocuments.add(testProductPage7);
		collectionOfDocuments.add(testProductPage8);
		collectionOfDocuments.add(testProductPage9);
		
		System.out.println("Done initializing documents 1.");
		return collectionOfDocuments;
	}

}
