package statistics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import statics.FFConstants;
import containers.Chartable;
import containers.StatisticsContainer;

public class FFStatistics {

	private int N;
	private double mean, median;
	private List<Double> values;

	public FFStatistics(){
	}

	public StatisticsContainer getStatistics(List<Double> vals){
		N = 0;
		mean = median = 0.0d;
		values = new ArrayList<Double>();
		List<Double> tempVals = new ArrayList<Double>(vals);
		for (Double ele : tempVals){
			if (ele > FFConstants.CLEAN_UPPER_BOUND | ele < FFConstants.CLEAN_LOWER_BOUND){//bitwise OR
				vals.remove(ele);
			}
		}

		this.N = vals.size();
		this.values = vals;
		Collections.sort(values);

		this.mean = calculateMean();
		this.median = calculateMedian(this.values);
		double standardDeviation = calculateStandardDeviation();
		double tenthPercentile = calculatePercentile(10);
		double ninetiethPercentile = calculatePercentile(90);

		return new StatisticsContainer(this.N, this.mean, median,
				standardDeviation, tenthPercentile, ninetiethPercentile);
	}

	public StatisticsContainer getIntensitiesStatistics(List<Chartable> charts){
		List<Double> vals = new ArrayList<Double>();
		for (Chartable c : charts){
			for (int i = 0; i < c.intensities.length; i++){
				vals.add(c.intensities[i]);
			}
		}
		return getStatistics(vals);
	}

	public StatisticsContainer getbStatistics(List<Chartable> charts){
		List<Double> vals = new ArrayList<Double>();
		for (Chartable c : charts){
			vals.add(c.b);
		}
		return getStatistics(vals);
	}

	public StatisticsContainer getMidpointStatistics(List<Chartable> charts){
		List<Double> vals = new ArrayList<Double>();
		for (Chartable c : charts){
			vals.add(c.chalf);
		}
		return getStatistics(vals);
	}

	public StatisticsContainer getAdjustedRSquaredStatistics(List<Chartable> charts){
		List<Double> vals = new ArrayList<Double>();
		for (Chartable c : charts){
			vals.add(c.adjRSquared);
		}
		return getStatistics(vals);
	}

	public StatisticsContainer getAStatistics(List<Chartable> charts){
		List<Double> vals = new ArrayList<Double>();
		for (Chartable c : charts){
			vals.add(c.A);
		}
		return getStatistics(vals);
	}

	public StatisticsContainer getBStatistics(List<Chartable> charts){
		List<Double> vals = new ArrayList<Double>();
		for (Chartable c : charts){
			vals.add(c.B);
		}
		return getStatistics(vals);
	}

	private double calculateMean(){
		double total = 0;
		int numCalculatedValues = 0;
		for (Double val : this.values) {
			if(!val.isNaN()){
				numCalculatedValues ++;
				total += val;
			}
		}
		return total / numCalculatedValues;
	}

	private double calculatePercentile(double k){
		int bucket = (int)Math.ceil(k*0.01*this.N)-1;

		if(bucket > this.values.size()) return this.values.get(this.N-1);
		if(bucket < 0) return this.values.get(0);

		return this.values.get(bucket);
	}

	private double calculateMedian(List<Double> vals){
		int size = vals.size();
		int middle = size / 2;
		if (size%2 == 1) {
			return vals.get(middle);
		} else {
			return (vals.get(middle-1) + vals.get(middle)) / 2.0;
		}
	}

	private double calculateStandardDeviation(){
		double sumDifferenceSquared = 0;
		double numCalculatedValues = 0;
		//only calculate standardDeviation for non NaN values
		for (Double d : this.values){
			if (!d.isNaN()){
				numCalculatedValues++;
				sumDifferenceSquared += Math.pow((d-this.mean), 2.0);
			}
		}
		//as we are calculating a POPULATION statistic, divide by N instead of (N-1)
		double variance = sumDifferenceSquared/numCalculatedValues;
		double standardDeviation = Math.sqrt(variance);
		return standardDeviation;
	}


	public static void main(String[] args){
		FFStatistics f = new FFStatistics();
		double[] v = new double[]{43, 54, 56, 61, 62, 66, 68, 69, 69, 70, 71, 72, 77, 78, 79, 85, 87, 88, 89, 93, 95, 96, 98, 99, 99};
		List<Double> vals = new ArrayList<Double>();
		for (int i = 0; i < v.length; i++){
			vals.add(v[i]);
		}
		f.getStatistics(vals);
		System.out.println(f.values);
	}
}
