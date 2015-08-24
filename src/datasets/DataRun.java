package datasets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.concurrent.Task;
import regression.CHalfFunction;
import statics.FFConstants;
import statics.FFMath;
import containers.SingleFit;
import flanagan.analysis.Regression;

public class DataRun extends Task<SingleFit>{

	private final double[] denaturants;
	private final double[] intensities;

	public DataRun(double[] intensities, Double[] denaturants){
		
		//populate primitive double array
		this.denaturants = new double[denaturants.length];
		for (int i = 0; i < denaturants.length; i++){
			this.denaturants[i] = denaturants[i];
		}
		
		//populate the intensities array
		this.intensities = intensities;
	}
	
	@SuppressWarnings("serial")
	private double[] calculateFit(
			Double[] intensities,Double[] denaturants){
		
		double[] x = new double[denaturants.length];
		double[] y =  new double[intensities.length];
		for (int i = 0; i < x.length; i++){
			x[i] = denaturants[i];
			y[i] = intensities[i];
		}

		
		double[] AAndB = calculateAAndB(intensities);
		double A = AAndB[0];
		double B = AAndB[1];
		
		// Instantiate CHalfFunction and assign A and B (knowns)
		CHalfFunction f = new CHalfFunction();
		f.setA(A);
		f.setB(B);
		
		//set inital estimates for chalf and b
		double[] estimates = new double[2];
		estimates[0] = FFConstants.InitialCHalfValue; // Chalf
		estimates[1] = FFConstants.InitialBValue; // b, calculated using heuristics
		
		//inital step sizes
		double[] step = new double[2];
		step[0] = 0.001d; //Chalf
		step[1] = 0.001d; // b
		
		Regression reg = new Regression(x,y);
		reg.simplex(f,estimates, step);
		double[] bestEstimates = reg.getBestEstimates();
		double[] bestEstimatesSD = reg.getBestEstimatesStandardDeviations();
		
		double adjRSquared = reg.getAdjustedCoefficientOfDetermination();
		double chalf = bestEstimates[0];
		double b = bestEstimates[1];
		double chalfSD = bestEstimatesSD[0];
		double bSD = bestEstimatesSD[1];
		
		
		//makes the array containing, in order,
		//c1/2, c1/2 sd, b, b sd, adjrsq
		List<Double> calculatedRun = new ArrayList<Double>(){{
			add(chalf);
			add(chalfSD);
			add(b);
			add(bSD);
			add(adjRSquared);
		}};
		
		Double[] preConvertedRun = new Double[calculatedRun.size()];
		preConvertedRun = calculatedRun.toArray(preConvertedRun);
		double[] convertedRun = new double[preConvertedRun.length];
		for (int i = 0; i < preConvertedRun.length; i++){
			convertedRun[i] = preConvertedRun[i];
		}
		return convertedRun;
	}

	@Override
	protected SingleFit call() throws Exception {
		final double[] fIntensities = this.intensities;
		final double[] fDenaturants = this.denaturants;
		
		ArrayList<SingleFit> fitList = new ArrayList<SingleFit>();
		
		//add without removing intensities
		Double[] x = new Double[fDenaturants.length];
		Double[] y = new Double[fIntensities.length];
		
		for (int i = 0; i < x.length; i++){
			x[i] = fDenaturants[i];
			y[i] = fIntensities[i];
		}
		double[] AAndB = calculateAAndB(y);
		fitList.add(new SingleFit(
				this.calculateFit(y, x),-1,AAndB[0], AAndB[1]));
		
		//serially remove each value and recalculate fit
		for (int i = 0; i < this.intensities.length; i++){
			//populate new arrays
			ArrayList<Double> tempListIntensities = new ArrayList<Double>();
			Double[] newIntensities = new Double[this.intensities.length-1];
			ArrayList<Double> tempListDenaturants = new ArrayList<Double>();
			Double[] newDenaturants = new Double[this.intensities.length-1];
			for (int j = 0; j < this.intensities.length; j++){
				if (j == i) continue; //ignore the one we want to remove
				tempListIntensities.add(fIntensities[j]);
				tempListDenaturants.add(fDenaturants[j]);
			}
			newIntensities = tempListIntensities.toArray(newIntensities);
			newDenaturants = tempListDenaturants.toArray(newDenaturants);
			
			//Calculate A and B values
			AAndB = calculateAAndB(newIntensities);
			double A = AAndB[0];
			double B = AAndB[1];
			
			//calculate fit
			SingleFit fit = new SingleFit(this.calculateFit(newIntensities, newDenaturants), i, A, B);
			fitList.add(fit);
		}
		Collections.sort(fitList);
		return fitList.get(0); //return largest adjRsq
	}
	
	private double[] calculateAAndB(Double[] intensities2){
		double[] ret = new double[2];
		/*
		 * determine if curve is oxidized or not based on heuristics
		 * 
		 * Assume to be Non-Oxidized if first half of values average to be greater than the second half of values
		 */
		double A,B, secondHalfValuesSum;
		double firstHalfValuesSum = secondHalfValuesSum = 0;
		for (int i = 0; i <intensities2.length/2; i++){
			firstHalfValuesSum += intensities2[i];
		}
		//if odd, second loop contains value in middle
		for (int i = intensities2.length/2; i < intensities2.length; i++){
			secondHalfValuesSum += intensities2[i];
		}
		//average of first half intensities > average of second half intensities. Accounts for the fact that we could have odd intensities
		boolean nonOx =  firstHalfValuesSum/(intensities2.length/2) >= secondHalfValuesSum/(intensities2.length-(intensities2.length/2));
		if (nonOx){
			A = FFMath.max(intensities2);
			B = FFMath.min(intensities2);
		}
		else{
			A = FFMath.min(intensities2);
			B = FFMath.max(intensities2);
		}
		ret[0] = A;
		ret[1] = B;
		return ret;
	}
	
	public static void main(String[] args){
		String[] t = "2.62223	1.24223	1.03308	0.96041	0.81282	0.721857	0.516559	0.455449".split("\\s+");
		double [] d = new double[t.length];
		for (int i = 0; i < t.length; i++)d[i] = Double.parseDouble(t[i]);
		DataRun r = new DataRun(d, 
				new Double[]{0.5	,1d,	1.25	,1.5,	1.75,	2d,	2.5, 3d});
		try {
			System.out.println(r.call());;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}


}
