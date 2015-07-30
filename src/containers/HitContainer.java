package containers;


public class HitContainer {

	
	public int CSVLineNumber;
	public String peptide;
	public String protein;
	
	public double controlMidpoint;
	public double controlAdjRsq;
	
	public double ligandMidpoint;
	public double ligandAdjRsq;
	
	public double deltaMidpoint;
	public double[] peptideDifferences;
	
	public HitContainer(int csvlinenumber, String peptide, String protein, double cMidpoint, double cAdjR, double lMidpoint
			,double lAdjR, double deltaMidpoint, double[] peptideDifferences){
		this.CSVLineNumber = csvlinenumber;
		this.peptide = peptide;
		this.protein = protein;
		this.controlMidpoint = cMidpoint;
		this.controlAdjRsq = cAdjR;
		this.ligandMidpoint = lMidpoint;
		this.ligandAdjRsq = lAdjR;
		this.deltaMidpoint = deltaMidpoint;
		this.peptideDifferences = peptideDifferences;
	}
	
}
