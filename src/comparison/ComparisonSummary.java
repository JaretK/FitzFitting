package comparison;

import java.util.List;
import containers.*;

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
	public int numberPassedDifferenceAnalysis;
	public int numberHits;
	public List<HitContainer> hitList;
	public List<Double> deltaMidpointList;
	
	public ComparisonSummary(int iComparedPeptides, boolean bAllCompared, int iClean, int iSignificant, 
			int iHits, List<HitContainer> hitList,List<Double> deltaMidpointList, int diffAnalysis){
		this.numberComparedPeptides = iComparedPeptides;
		this.allCompared = bAllCompared;
		this.numberClean = iClean;
		this.numberSignificant = iSignificant;
		this.numberHits = iHits;
		this.hitList = hitList;
		this.deltaMidpointList = deltaMidpointList;
		this.numberPassedDifferenceAnalysis = diffAnalysis;
	}
}
