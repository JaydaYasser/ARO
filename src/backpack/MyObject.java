package backpack;

public class MyObject implements Comparable<MyObject> {
	// Position de l'objet initiale (avant le tri).
	private int initialPosition; 
	private double value; 
	private double weight;

	public MyObject(int initialPosition, double weight, double value) {
		this.initialPosition = initialPosition;
		this.value = value;
		this.weight = weight;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	@Override
	public int compareTo(MyObject o) {
		double ratio1 = value / weight;
		double ratio2 = o.value / o.weight;
		
		return Double.compare(ratio1, ratio2);
	}

	public int getInitialPosition() {
		return initialPosition;
	}

	public void setInitialPosition(int initialPosition) {
		this.initialPosition = initialPosition;
	}
}
