import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.concurrent.Task;
import flanagan.analysis.Regression;


public class DataRun extends Task<Void>{

	private final double[] denaturants;
	private final double[] intensities;
	private double[] calculatedValues;
	private boolean finished;

	public DataRun(double[] intensities, Double[] denaturants){
		
		//populate primitive double array
		this.denaturants = new double[denaturants.length];
		for (int i = 0; i < denaturants.length; i++){
			this.denaturants[i] = denaturants[i];
		}
		
		//populate the intensities array
		this.intensities = intensities;
		
		this.finished = false;
	}
	
	@SuppressWarnings("serial")
	private void calculateFit(){
		double[] x = this.denaturants;
		double[] y =  this.intensities;

		
		double A;
		double B;
		/*
		 * determine if curve is oxidized or not based on heuristics
		 * assume to be non-oxidized if first two points average to be greater than 1.0
		 */
		boolean nonOx =  (y[0] + y[1])/2 > 1.0;
		if (nonOx){
			A = FFMath.max(y);
			B = FFMath.min(y);
		}
		else{
			A = FFMath.min(y);
			B = FFMath.max(y);
		}
		
		// Instantiate CHalfFunction and assign A and B (knowns)
		CHalfFunction f = new CHalfFunction();
		f.setA(A);
		f.setB(B);
		
		//set inital estimates for chalf and b
		double[] estimates = new double[2];
		estimates[0] = 1d; // Chalf
		estimates[1] = 1d; // b
		
		//inital step sizes
		double[] step = new double[2];
		step[0] = 0.001d; //Chalf
		step[1] = 0.001d; // b
		
		//weights
		double[] weights = calculateWeights();
		
		Regression reg = new Regression(x,y, weights);
		reg.simplex(f,estimates, step);
		double[] bestEstimates = reg.getBestEstimates();
		double[] bestEstimatesSD = reg.getBestEstimatesStandardDeviations();
		System.out.println(Arrays.toString(reg.getWeights()));
		
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
		this.calculatedValues = convertedRun;
	}
	
	public double[] getCalculatedValues(){
		return this.calculatedValues;
	}

	@Override
	protected Void call() throws Exception {
		this.calculateFit();
		
		return null;
	}
	
	private double[] calculateWeights(){
		double[] weights = new double[this.intensities.length];
		
		for (int i = 0; i < weights.length; i++)
			weights[i] = 1d;
		
		return weights;
	}
	
	public static void main(String[] args){
		String[] t = "2.46633	1.75591	1.6659	1.15683	0.595836	0.595003	0.462354	0.458989".split("\\s+");
		double [] d = new double[t.length];
		for (int i = 0; i < t.length; i++)d[i] = Double.parseDouble(t[i]);
		DataRun r = new DataRun(d, 
				new Double[]{0.5	,1d,	1.25	,1.5,	1.75,	2d,	2.5, 3d});
		System.out.println(Arrays.toString(r.intensities)); 
		try {
			r.call();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println(Arrays.toString(r.getCalculatedValues()));
	}


}
