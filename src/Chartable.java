class Chartable{
	public String peptide;
	public String protein;
	public int graphNumber;
	public double[] intensities;
	public double A;
	public double B;
	public double chalf;
	public double b;
	public double adjRSquared;
	public int indexRemoved;
	
	
	public Chartable(
			String peptide, String protein, double[] intensities,
			double[] denaturants, double chalf, double b, double adjRSquared, int indexRemoved
			){
		this.peptide = peptide;
		this.protein = protein;

		//populate intensities
		this.intensities = intensities;

		//chalf is fifth from last (recall there is a space at end)
		this.chalf = chalf;

		//b is third from last (space)
		this.b = b;

		//adjusted R Squared is the last value
		this.adjRSquared = adjRSquared;

		this.graphNumber = -1;
		
		this.indexRemoved = indexRemoved;
		
		assignAAndB();
	}
	
	public void setGraphNumber(int graphNumber){
		this.graphNumber = graphNumber;
	}

	private final void assignAAndB(){
		//A is calculated from DataRun
		//B is calculated from DataRun
		/*
		 * determine if curve is oxidized or not based on heuristics
		 * assume to be non-oxidized if first two points average to be greater than 1.0
		 */
		boolean nonOx =  (intensities[0] + intensities[1])/2 > 1.0;
		if (nonOx){
			A = FFMath.max(intensities);
			B = FFMath.min(intensities);
		}
		else{
			A = FFMath.min(intensities);
			B = FFMath.max(intensities);
		}
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