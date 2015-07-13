import flanagan.analysis.RegressionFunction;


public class CHalfFunction implements RegressionFunction{

	public double A = 0.0d;
	public double B = 0.0d;
	
	@Override
	public double function(double[] p, double[] x) {
		double exponentialFactor = -(x[0] - p[0])/p[1];
		double y = A + (B-A)/(1+Math.exp(exponentialFactor));
		return y;
	}
	
	public void setA(double A){
		this.A = A;
	}
	
	public void setB(double B){
		this.B = B;
	}

	
	/**
	 * Used for calculating datapoints in FFGraphGenerator
	 * @param CHalf - calculated CHalf value from flanagan.analysis.regression
	 * @param b - calculated b value
	 * @param x - x value to calculate f(x)
	 * @return f(x)
	 */
	public double calculateYValue(double CHalf, double b, double x){
		double exponentialFactor = -(x-CHalf)/b;
		double y = A + (B-A)/(1+Math.exp(exponentialFactor));
		return y;
	}
	
	public static void main(String[] args){
		CHalfFunction chf = new CHalfFunction();
		chf.A = 1;
		chf.B = 0;
		System.out.println(chf.function(new double[]{0.2, 0.4}, new double[]{5}));
	}
}
