package containers;


public class SingleFit implements Comparable<SingleFit>{
	public final double[] array;
	public final double A;
	public final double B;
	public final int removedValue;
	public SingleFit(double[] arr, int removedValue, double A, double B){
		this.array = arr;
		this.removedValue = removedValue;
		this.A=A;
		this.B=B;
	}
	
	@Override
	public int compareTo(SingleFit o) {
		double thisRsq = this.array[4];
		double thatRsq = o.array[4];

		boolean lg = thisRsq < thatRsq;
		boolean eq = thisRsq == thatRsq;
		if(eq) return (this.removedValue < o.removedValue) ? 1 : -1;
		return lg ? 1 : -1;
	}
	
	public String toString(){
		String s = "";
		s += ("#Removed: "+removedValue+"\n");
		s += ("adjRsq: "+array[4]+"\n");
		return s;
	}
}