//This class was created because vectors cannot use doubles as their template.

public class MutableDouble {
private double value;
	
	public MutableDouble()
	{
		value = 0.0;
	}
	
	public MutableDouble(double newValue)
	{
		value = newValue;
	}

	public double getValue()
	{
		return this.value;
	}
	
	public void changeValue(double newValue)
	{
		this.value = newValue;
	}

	public void decementValue()
	{
		this.value--;
	}
}
