import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javafx.scene.text.TextFlow;


/**
 * Holds all the information needed for a SPROX dataset
 * 
 * @author jkarnuta
 *
 */
public class SingletDataSet extends AbstractDataSet {

	public SingletDataSet(String SPROXFile, String DenaturantFile,TextFlow output){
		super(SPROXFile, DenaturantFile, output);
	}

	/**
	 * Takes the file defined in the constructor and digests it into individual runs along with a header
	 */
	public FFError load(){
		/*
		 * SAME AS DualSingletDataSet
		 */

		//tests if SPROX file is valid
		int SPROXValid;
		try{
			SPROXValid = FFFileValidator.VALIDATE_SPROX(super.getSPROX1File());
		}catch(IOException e){
			SPROXValid = -1;
			TextFlowWriter.writeError(e.getMessage(), super.output);
		}
		if(SPROXValid == -1)
		{ 
			TextFlowWriter.writeError("SPROX File is not a CSV", this.output);
			return FFError.InvalidFile;
		}
		super.setOffset1(SPROXValid);

		//tests if DenaturantsFile is valid
		boolean DenaturantsValid;
		try{
			DenaturantsValid = FFFileValidator.VALIDATE_DENATURANTS(super.getDenaturantFile());
		}catch(IOException e){
			TextFlowWriter.writeError(e.getMessage(), super.output);
			DenaturantsValid = false;
		}

		if(!DenaturantsValid)
		{ 
			TextFlowWriter.writeError("Denaturant File is not a CSV", this.output);
			return FFError.InvalidFile;
		}

		//reads SPROX file and parses header
		try (BufferedReader br = new BufferedReader(new FileReader(super.getSPROX1File()))){
			String line;
			for (int i = 0; i <= SPROXValid; i++){
				line = br.readLine();
				super.headers1.add(line.split(","));
			}

			while((line = br.readLine()) != null){
				runs1.add(line.split(","));
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			TextFlowWriter.writeError(e.getMessage(), this.output);
			return FFError.ErrorParsingFile;
		} 

		//if this point is reached, rely only on output of loadDenaturants
		return super.loadDenaturants();
	}



	/**
	 * Injection point for thread
	 */


	@Override
	protected Void call() throws Exception {
		//		System.out.println(Arrays.toString(header));
		//		for (String[] ele : runs) System.out.println(Arrays.toString(ele));
		//		System.out.println(Arrays.toString(DenaturantConcentrations));
		/*
		 * header = 1st line of file
		 */

		/*
		 * runs = List of String arrays, one array per run
		 * 0 = Peptide
		 * 1 = Protein
		 * 2 = Integral Sum
		 * 3 = Retention Time
		 * 4.. 4 + Denats.length = intensities at corresponding denaturant
		 */

		/*
		 * DenaturantConcentrations = Double array of included denatuant concentrations
		 */


		//update the last row in headers to account for new additions, chalf, chalfSD, b, bSD, adjRSquared
		List<String> headerList = FFOperations.asList(this.headers1.get(this.headers1.size()-1));
		headerList.addAll(FFOperations.getHeaderAdditions());
		String[] tempLastHeader = new String[headerList.size()];
		tempLastHeader = headerList.toArray(tempLastHeader);
		this.headers1.remove(this.headers1.size()-1);
		this.headers1.add(tempLastHeader);

		/*
		 * For each run in runs, calculate chalf and b. 
		 * Update the run in runs with the calculated values
		 */

		final int totalIterations = runs1.size();
		final int numberIdentifiers = 4;

		TextFlowWriter.writeLine("", super.output);
		TextFlowWriter.writeInfo("Calculating C 1/2 and b values for inputted file(s)", this.output);
		//necessary for removing previous line
		TextFlowWriter.writeLine("", super.output);
		for (int i = 0; i < runs1.size(); i++){
			String[] run = runs1.get(i);
			TextFlowWriter.removeLast(super.output);
			TextFlowWriter.writeInfo("Calculating #"+(i+1)+
					" / "+runs1.size(), super.output);
			try{
				if(run[0].length() > 0){
					SingletPeptideContainer pc = new SingletPeptideContainer(run, super.getDenaturants(), numberIdentifiers);
					DataRun r = new DataRun(pc.intensities, this.DenaturantConcentrations);
					r.call();
					while(r.isRunning()){
					}
					double[] calculatedRun = r.getCalculatedValues();
					pc.parseCalculatedValues(calculatedRun);
					//append calculatedRun to data, add a space at end
					String[] newRun = new String[calculatedRun.length + run.length + 1];
					for (int j = 0; j < run.length; j ++){
						newRun[j] = run[j];
					}
					for (int j = run.length; j < calculatedRun.length + run.length; j++){
						newRun[j] = String.valueOf(calculatedRun[j - run.length]);
					}
					newRun[newRun.length-1] = "";
					runs1.set(i, newRun);
					super.addChartable1(pc.toChartable());
				}
			}
			catch(ArrayIndexOutOfBoundsException e){
				continue;
			}
			updateProgress(i+1, totalIterations);
		}
		TextFlowWriter.removeLast(super.output);
		return null;
	}
	/*Contains the peptide runs. Works as expected*/
	public class SingletPeptideContainer {
		public String peptide;
		public String protein;
		public double[] denaturants;

		/*Run 1*/
		public double intsum;
		public double rt;
		public double[] intensities;
		
		/*Calculated Values*/
		public double chalf;
		public double b;
		public double adjRSq;
			
		public SingletPeptideContainer(String[] list, Double[] denaturants
				, final int offset){
			this.intensities = new double[denaturants.length];
			this.denaturants = new double[denaturants.length];
			for (int i = 0; i < denaturants.length; i++)
				this.denaturants[i] = denaturants[i];
			
			this.peptide = list[0];
			this.protein = list[1];
			this.intsum = Double.parseDouble(list[2]);
			this.rt = Double.parseDouble(list[3]);
			
			for (int i = 0; i < denaturants.length; i++){
				this.intensities[i] = Double.parseDouble(list[i+offset]);
			}
			
		}

		public Chartable toChartable() {
			return new Chartable(this.peptide, this.protein, this.intensities,
					this.denaturants,this.chalf, this.b, this.adjRSq);
		}
		
		public void parseCalculatedValues(double[] arr){
			/*
			 * Will always present chalf, chalf sd, b, b sd, adjrsq
			 */
			this.chalf = arr[0];
			this.b = arr[2];
			this.adjRSq = arr[4];
		}
	}


	public static void main(String[] args){
		SingletDataSet ds = new SingletDataSet("/Users/jkarnuta/Desktop/10-16-12 manA Control Data.csv", "/Users/jkarnuta/Desktop/manATags.csv" ,new TextFlow());
		System.out.println(ds.load());
		try {
			ds.call();
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (String[] ele : ds.getRuns1()){
			System.out.println(Arrays.toString(ele));
		}

	}

}
