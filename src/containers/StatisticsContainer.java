package containers;


public class StatisticsContainer {

	private int N;
	private double mean, median, std, tenth, ninetieth;
	
	public StatisticsContainer(int N,double mean, double median, double std, double th10, double th90){
		this.setN(N);
		this.mean = mean;
		this.median = median;
		this.std = std;
		this.setTenth(th10);
		this.setNinetieth(th90);
	}


	/**
	 * @return the n
	 */
	public int getN() {
		return N;
	}

	/**
	 * @param n the n to set
	 */
	public void setN(int n) {
		N = n;
	}

	/**
	 * @return the mean
	 */
	public double getMean() {
		return mean;
	}

	/**
	 * @param mean the mean to set
	 */
	public void setMean(double mean) {
		this.mean = mean;
	}

	/**
	 * @return the median
	 */
	public double getMedian() {
		return median;
	}

	/**
	 * @param the median to set
	 */
	public void setMedian(double median) {
		this.median = median;
	}

	/**
	 * @return the std
	 */
	public double getStd() {
		return std;
	}

	/**
	 * @param std the std to set
	 */
	public void setStd(double std) {
		this.std = std;
	}

	/**
	 * @return the tenth percentile
	 */
	public double getTenth() {
		return tenth;
	}

	/**
	 * @param tenth the tenth to set
	 */
	public void setTenth(double tenth) {
		this.tenth = tenth;
	}

	/**
	 * @return the ninetieth percentile
	 */
	public double getNinetieth() {
		return ninetieth;
	}

	/**
	 * @param ninetieth the ninetieth to set
	 */
	public void setNinetieth(double ninetieth) {
		this.ninetieth = ninetieth;
	}
	
	
}
