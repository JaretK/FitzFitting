package models;

import java.io.File;
import java.io.FileWriter;

import javafx.concurrent.Task;
import statics.TextFlowWriter;
import containers.Chartable;
import containers.HitContainer;
import containers.CSVStringBuilder;
import statics.FFOperations;

public class HitSave extends Task<Boolean>{

	private final AbstractFFModel model;
	private static final String SAVE_FILENAME = "Hit List.csv";
	private static final String CSV_ROW_NUMBER_HEADER = "Peptide #,";
	
	public HitSave(AbstractFFModel model){
		this.model = model;
	}


	private void save() throws Exception{

		//make file
		File f = new File(model.getSuperPath() + SAVE_FILENAME);

		//make filewriter
		FileWriter fw = new FileWriter(f);

		/*
		 * Write header. Header will be the last header line, cut off at the appropriate location
		 * To calculate the last location, the following formula will be used
		 * 
		 * last index = 3 + (2 + denaturants.length + 5 + 1 )*2 
		 * 
		 */

		String[] lastHeader = model.data.getHeaders1().get(model.data.getHeaders1().size()-1);
		
		int lastIndex = 3 + (2 + model.data.getDenaturants().length + 5 + 1)*2;
		String header = CSV_ROW_NUMBER_HEADER + FFOperations.stringArrayToCSV(lastHeader, lastIndex);
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
			for (int i = lastIndexFound; i < model.data.getChartables1().size(); i++){
				lastIndexFound++; //expect mode.compSummary.hitList to be in sync with model.data.chartablesN
				c1 = model.data.getChartables1().get(i);
				c2 = model.data.getChartables2().get(i);
				boolean samePeptides = c1.peptide.equals(c2.peptide);
				boolean peptideHit = c1.peptide.equals(hit.peptide);
				if(samePeptides && peptideHit) break;
				if(i == model.data.getChartables1().size()-1) eofChartables = true;
				
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
}
