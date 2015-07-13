import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.concurrent.Task;
import flanagan.analysis.Regression;


public class DataRun extends Task<Void>{

	private final String peptide;
	private final String protein;
	private final String intsum;
	private final String rt;
	private final double[] denaturants;
	private final double[] intensities;
	private String[] calculatedValues;

	/*
	 * 4 Identifiers:
	 * Peptide, Protein, Int Sum, RT
	 */
	private static final int numIdentifiers = 4;

	public DataRun(String[] run, Double[] denaturants){
		this.peptide = run[0];
		this.protein = run[1];
		this.intsum = run[2];
		this.rt = run[3];
		
		//populate primitive double array
		this.denaturants = new double[denaturants.length];
		for (int i = 0; i < denaturants.length; i++){
			this.denaturants[i] = denaturants[i];
		}
		
		int numIntensities = denaturants.length;
		this.intensities = new double[numIntensities];
		for (int i = numIdentifiers; i < numIdentifiers + numIntensities; i++){
			//Offset by numIdentifiers
			this.intensities[i - numIdentifiers] = Double.parseDouble(run[i]);
		}
		
		
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
		
		Regression reg = new Regression(x,y);
		reg.simplex(f,estimates, step);
		double[] bestEstimates = reg.getBestEstimates();
		double[] bestEstimatesSD = reg.getBestEstimatesStandardDeviations();
		
		double adjRSquared = reg.getAdjustedCoefficientOfDetermination();
		double chalf = bestEstimates[0];
		double b = bestEstimates[1];
		double chalfSD = bestEstimatesSD[0];
		double bSD = bestEstimatesSD[1];
		
		
		//get the 
		List<String> calculatedRun = new ArrayList<String>(){{
			add(peptide);
			add(protein);
			add(intsum);
			add(rt);
			for (double ele : intensities){
				add(String.valueOf(ele));
			}
			add(String.valueOf(chalf));
			add(String.valueOf(chalfSD));
			add(String.valueOf(b));
			add(String.valueOf(bSD));
			add(String.valueOf(adjRSquared));
		}};
		
		String[] returnRun = new String[calculatedRun.size()];
		returnRun = calculatedRun.toArray(returnRun);
		this.calculatedValues = returnRun;
	}
	
	public String[] getCalculatedValues(){
		return this.calculatedValues;
	}

	@Override
	protected Void call() throws Exception {
		this.calculateFit();
		return null;
	}
	
	public static void main(String[] args){
		String[] t = "AAAEGPM(OX)K	YJL052W	196792.1683	16.067245	1.510101143	0.738887242	0.907143504	0.920527861	1.083988819	1.091062484	0.817365849	1.202946446".split("\\s+");
		DataRun r = new DataRun(t, 
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
