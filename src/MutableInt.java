//This class was created because vectors cannot use ints as their template, only the predefined Integer class,
// which cannot be changed once initialized.

public class MutableInt {
	private int value;
	
	public MutableInt()
	{
		value = 0;
	}
	
	public MutableInt(int newValue)
	{
		value = newValue;
	}

	public int getValue()
	{
		return this.value;
	}
	
	public void changeValue(int newValue)
	{
		this.value = newValue;
	}

	public void decementValue()
	{
		this.value--;
	}
}
