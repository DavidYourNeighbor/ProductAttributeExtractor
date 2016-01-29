//This class is a pair of integers, necessary for the Similarity Matrix in the attribute extraction algorithm

public class IntPair
{	
	private int a;
	private int b;
		
	public IntPair(int aa, int bb)
	{
		a = aa;
		b = bb;
	}
	
	public void changeVals(int aa, int bb)
	{
		a = aa;
		b = bb;
	}
		
	public int getA()
	{
		return a;
	}
		
	public int getB()
	{
		return b;
	}
}
