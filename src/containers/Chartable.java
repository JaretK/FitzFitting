package containers;


/**
 * Class that contains all the relevant information for a peptide in each experiment, including midpoint (chalf, c 1/2) values and b values
 * @author jkarnuta
 *
 */
public class Chartable{
	public String peptide;
	public String protein;
	public String experiment;
	public double intsum;
	public double RT;
	public int graphNumber;
	public double[] intensities;
	public double A;
	public double B;
	public double chalf;
	public double chalfSD;
	public double b;
	public double bSD;
	public double adjRSquared;
	public int indexRemoved;
	
	
	public Chartable(
			String peptide, String protein,String experiment, double intsum, double RT, double[] intensities,
			double[] denaturants, double chalf, double chalfSD ,double b, double bSD ,
			double adjRSquared, int indexRemoved, double A, double B){
		this.peptide = peptide;
		this.protein = protein;
		this.experiment = experiment;
		this.intsum = intsum;
		this.RT = RT;

		//populate intensities
		this.intensities = intensities;

		//chalf is fifth from last (recall there is a space at end)
		this.chalf = chalf;
		this.chalfSD = chalfSD;

		//b is third from last (space)
		this.b = b;
		this.bSD = bSD;

		//adjusted R Squared is the last value
		this.adjRSquared = adjRSquared;

		this.graphNumber = -1;
		
		this.indexRemoved = indexRemoved;
		
		this. A = A;
		this. B = B;
	}
	
	public void setGraphNumber(int graphNumber){
		this.graphNumber = graphNumber;
	}

	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("Chartable object:\n");
		sb.append("peptide (protein): "+peptide+" ("+protein+")\n");
		sb.append("C 1/2: "+chalf+"\n");
		sb.append("b: "+b+"\n");
		sb.append("A: "+A+"\n");
		sb.append("B: "+B+"\n");
		sb.append("Adjusted R Squared: "+adjRSquared+"\n");
		return sb.toString();
	}
}