
public class FFConstants {

	/*For FFGraphGenerator*/
	public static final double InitialCHalfValue = 1.0d;
	public static final double InitialBValue = 0.3d;
	
	/*For FFChartableComparator*/
	public static double ADJ_R_SQ_HEURISTIC = 0.7d;
	public static double MIDPOINT_HEURISTIC = 0.5d;
	
	/*Set above heuristics*/
	public static void setAdjustedRSquaredHeuristic(String set){
		try{
			ADJ_R_SQ_HEURISTIC = Double.parseDouble(set);
		}catch(Exception e){
			ADJ_R_SQ_HEURISTIC = 0.7d;
		}
	}
	
	public static void setMidPointHeuristic(String set){
		try{
			MIDPOINT_HEURISTIC = Double.parseDouble(set);
		}catch(Exception e){
			MIDPOINT_HEURISTIC = 0.5d;
		}
	}
	
	/*For FFChartableComparator and HTMLGenerator*/
	public static final String COMPARISON_FILENAME = "Comparison.csv";
}

