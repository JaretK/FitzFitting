import java.io.File;
import java.io.FileWriter;

import javafx.concurrent.Task;
import statics.TextFlowWriter;
import containers.Chartable;
import containers.HitContainer;


public class HitSave extends Task<Boolean>{

	private final AbstractFFModel model;
	private static final String SAVE_FILENAME = "Hit List.csv";
	private static final String CSV_ROW_NUMBER_HEADER = "Peptide #,";
	
	public HitSave(AbstractFFModel model){
		this.model = model;
	}


	private void save() throws Exception{

		//make file
		File f = new File(model.superPath + SAVE_FILENAME);

		//make filewriter
		FileWriter fw = new FileWriter(f);

		/*
		 * Write header. Header will be the last header line, cut off at the appropriate location
		 * To calculate the last location, the following formula will be used
		 * 
		 * last index = 3 + (2 + denaturants.length + 5 + 1 )*2 
		 * 
		 */

		String[] lastHeader = model.data.headers1.get(model.data.headers1.size()-1);
		
		int lastIndex = 3 + (2 + model.data.DenaturantConcentrations.length + 5 + 1)*2;
		String header = CSV_ROW_NUMBER_HEADER + stringArrayToCSV(lastHeader, lastIndex);
		header = header += ",Delta Midpoint,";
		fw.write(header+"\n");
		
		/*
		 * Write each line. Use the HitContainers contained in model.compSummary 
		 */
		if(model.compSummary == null || model instanceof FFModelSinglet){
			TextFlowWriter.writeError("Incorrect model passed into HitSave", model.output);
		}
		
		int overallProgress = 0;
		int totalIterations = model.compSummary.hitList.size();
		int lastIndexFound = 0; //boost runtime speed, best case O(N) from O(N^2)
		boolean eofChartables = false;
		for(HitContainer hit : model.compSummary.hitList){
			//update progress
			overallProgress++;
			updateProgress(overallProgress, totalIterations);
			
			Chartable c1;
			Chartable c2 = c1 = null;
			//get chartables from hit
			for (int i = lastIndexFound; i < model.data.chartables1.size(); i++){
				lastIndexFound++; //expect mode.compSummary.hitList to be in sync with model.data.chartablesN
				c1 = model.data.chartables1.get(i);
				c2 = model.data.chartables2.get(i);
				boolean samePeptides = c1.peptide.equals(c2.peptide);
				boolean peptideHit = c1.peptide.equals(hit.peptide);
				if(samePeptides && peptideHit) break;
				if(i == model.data.chartables1.size()-1) eofChartables = true;
				
			}
			if(eofChartables) break; //only happens if run out of chartables
			//c1 and c2 contain peptide hits and are the same
			//parse c1 and c2 into string
			String line = createLine(c1,c2);
			fw.write(line);
		}
		
		fw.flush();
		fw.close();
	}

	

	@Override
	protected Boolean call(){
		try{
			this.save();
			return true;
		} catch(Exception e){
			TextFlowWriter.writeError(e.getMessage(), model.output);
			return false;
		}
	}

	/**
	 * Converts two chartables into one line. Guaranteed that both chartables contain
	 * @param c1 first Chartable
	 * @param c2 second Chartable
	 * @return
	 */
	private String createLine(Chartable c1, Chartable c2){
		if(c1 == null || c2 == null) return "\n";
		CSVStringBuilder csv = new CSVStringBuilder();
		//row number
		csv.append(c1.graphNumber);
		csv.append(c1.peptide);
		csv.append(c1.protein);
		csv.append(c1.experiment);
		csv.append(c1.intsum);
		csv.append(c1.RT);
		csv.append(c1.intensities);
		csv.append(c1.chalf);
		csv.append(c1.chalfSD);
		csv.append(c1.b);
		csv.append(c1.bSD);
		csv.append(c1.adjRSquared);
		csv.append("");
		csv.append(c2.intsum);
		csv.append(c2.RT);
		csv.append(c2.intensities);
		csv.append(c2.chalf);
		csv.append(c2.chalfSD);
		csv.append(c2.b);
		csv.append(c2.bSD);
		csv.append(c2.adjRSquared);
		csv.append("");
		csv.append(c1.chalf-c2.chalf);
		return csv.toString();
	}

	/**
	 * Converts a string array into a CSV readable string
	 * String[]{"x","y","z"} ==> "x,y,z"
	 * @param array
	 * @return CSV'd string 
	 */
	private String stringArrayToCSV(String[] array){
		StringBuilder sb = new StringBuilder();
		for (String ele : array){
			sb.append( "," + ele );
		}
		return (sb.length() > 1) ? sb.substring(1) : sb.toString(); // clip the first comma
	}
	
	/**
	 * Converts a string array to a CSV readable string
	 * {x,y,z,a,b,c}, 2 => "x,y,z"
	 * @param array
	 * @param lastIndex last index to include in the CSV from the string array
	 * @return CSV'd string
	 */
	private String stringArrayToCSV(String[] array, int lastIndex){
		String[] newArray = new String[lastIndex];
		for (int i = 0; i < newArray.length; i++){
			newArray[i] = array[i];
		}
		return stringArrayToCSV(newArray);
	}
	
	private class CSVStringBuilder{
		StringBuilder sb;
		public CSVStringBuilder(){
			sb = new StringBuilder();
		}
		public void append(String s){
			sb.append(s);
			sb.append(",");
		}
		public void append(int i){
			sb.append(i);
			sb.append(",");
		}
		public void append(double d){
			sb.append(d);
			sb.append(",");
		}
		public void append(double[] darr){
			for(int i = 0; i < darr.length; i++){
				this.append(darr[i]);
			}
		}
		
		public String toString(){
			StringBuilder ret = new StringBuilder(sb);
			return ret.substring(0, ret.length()-1).toString()+"\n";
		}
	}
}
