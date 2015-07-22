
/**
 * Object passed between FFChartableComparator and dual instances of AbstractFFModel. 
 * 
 * Contains number of compared peptides, total success (all compared), number clean, number significant, and the number hits
 * @author jkarnuta
 *
 */
public class ComparisonSummary {

	public int numberComparedPeptides;
	public boolean allCompared;
	public int numberClean;
	public int numberSignificant;
	public int numberHits;
	
	public ComparisonSummary(int iComparedPeptides, boolean bAllCompared, int iClean, int iSignificant, int iHits){
		this.numberComparedPeptides = iComparedPeptides;
		this.allCompared = bAllCompared;
		this.numberClean = iClean;
		this.numberSignificant = iSignificant;
		this.numberHits = iHits;
	}
}
