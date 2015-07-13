import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.text.TextFlow;


/**
 * Holds all the information needed for a SPROX dataset
 * 
 * @author jkarnuta
 *
 */
public class DataSet extends Task<Void> {

	private final File SPROXFile;
	private final File DenaturantFile;

	private final TextFlow output;

	private String[] header;
	private List<String[]> runs = new ArrayList<String[]>();

	private Double[] DenaturantConcentrations;


	public DataSet(File SPROXFile, File DenaturantFile,TextFlow output){
		this.SPROXFile = SPROXFile;
		this.DenaturantFile = DenaturantFile;
		this.output = output;
	}

	/**
	 * Takes the file defined in the constructor and digests it into individual runs along with a header
	 */
	public FFError load(){

		//tests if file is valid
		if(!validateFile(this.SPROXFile))
		{ 
			TextFlowWriter.writeError("SPROX File is not a CSV", this.output);
			return FFError.InvalidFile;
		}

		if(!validateFile(this.DenaturantFile))
		{ 
			TextFlowWriter.writeError("Denaturant File is not a CSV", this.output);
			return FFError.InvalidFile;
		}

		//reads SPROX file and parses header
		try (BufferedReader br = new BufferedReader(new FileReader(this.SPROXFile))){
			String line = br.readLine();
			this.header = line.split(",");

			while((line = br.readLine()) != null){
				runs.add(line.split(","));
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			TextFlowWriter.writeError(e.getMessage(), this.output);
			return FFError.ErrorParsingFile;
		} 

		//reads and parses denaturants file
		try (BufferedReader br = new BufferedReader(new FileReader(this.DenaturantFile))){
			String line = br.readLine();
			String[] buckets = line.split(",");
			DenaturantConcentrations = new Double[buckets.length];

			for (int i = 0; i < buckets.length ; i++){
				DenaturantConcentrations[i] = Double.parseDouble(buckets[i]);
			}

			if(br.readLine() != null){
				TextFlowWriter.writeError("CSV File is not one line", this.output);
				return FFError.ErrorParsingFile;
			}

		} catch (IOException e) {
			TextFlowWriter.writeError(e.getMessage(), this.output);
			return FFError.ErrorParsingFile;
		} catch(NumberFormatException e){
			TextFlowWriter.writeError("NumberFormatException:" +e.getMessage(), this.output);
			return FFError.InvalidDenaturants;
		}

		if(!header[0].toLowerCase().equals("sequence")){
			TextFlowWriter.writeError("CSV is invalid. Must be a standard SPROX CSV (first entry must be \"sequence\")", this.output);
			return FFError.InvalidFile;
		}
		return FFError.NoError;
	}

	/**
	 * 
	 * @param file to validate
	 * @return true iff path ends in .csv
	 */
	private boolean validateFile(File file){
		String path = file.getAbsolutePath();
		return path.substring(path.length()-4).equals(".csv");
	}

	/**
	 * Injection point for thread
	 */

	/**
	 * Takes the loaded file and digests it to determine midpoints and b values
	 * Called on FFModel start
	 */

	public FFError digest(){
		try {
			call();
			return FFError.NoError;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			return FFError.CalculationFailure;
		}
	}

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


		//update the header to account for new additions, chalf, chalfSD, b, bSD, adjRSquared
		List<String> headerList = Arrays.asList(this.header);
		headerList = new ArrayList<String>(headerList);
		headerList.add("C 1/2");
		headerList.add("C 1/2 SD");
		headerList.add("b");
		headerList.add("b SD");
		headerList.add("Adjusted R Squared");
		this.header = new String[headerList.size()];
		this.header = headerList.toArray(this.header);

		/*
		 * For each run in runs, calculate chalf and b. 
		 * Update the run in runs with the calculated values
		 */

		int totalIterations = runs.size();

		TextFlowWriter.writeLine("", this.output);
		TextFlowWriter.writeInfo("Calculating C 1/2 and b values for inputted file(s)", this.output);
		
		for (int i = 0; i < runs.size(); i++){
			String[] run = runs.get(i);
			DataRun r = new DataRun(run, this.DenaturantConcentrations);
			r.call();
			while(r.isRunning()){
			}
			String[] calculatedRun = r.getCalculatedValues();
			runs.set(i, calculatedRun);
			updateProgress(i, totalIterations);
		}
		return null;
	}


	/**
	 * Getter Methods
	 * 
	 */

	public String[] getHeader(){
		return header;
	}

	public ArrayList<String[]> getRuns(){
		return (ArrayList<String[]>) runs;
	}

	public Double[] getDenaturants(){
		return DenaturantConcentrations;
	}



	public static void main(String[] args){
		DataSet ds = new DataSet(new File("/Users/jkarnuta/Desktop/10-16-12 manA Control Data.csv"), new File("/Users/jkarnuta/Desktop/manATags.csv") ,new TextFlow());
		System.out.println(ds.load());
		try {
			ds.call();
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (String[] ele : ds.getRuns()){
			System.out.println(Arrays.toString(ele));
		}

	}

}
