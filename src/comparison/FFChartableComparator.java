package comparison;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.concurrent.Task;
import javafx.scene.text.TextFlow;
import containers.*;
import statics.*;

public class FFChartableComparator extends Task<ComparisonSummary>{

	private final List<Chartable> charts1;
	private final List<Chartable> charts2;
	private final List<String[]> headers;
	private final String directoryPath;
	private static final String APPEND_FILE_NAME = FFConstants.COMPARISON_FILENAME;
	private final TextFlow output;

	private int numberCompared;
	private boolean allCompared;
	private int numberClean;
	private int numberSignificant;
	private int numberHits;

	private int currentLineNumber = 0;

	public FFChartableComparator(List<Chartable> charts1, List<Chartable> charts2, List<String[]> headers,
			String directoryPath, TextFlow output){
		this.charts1 = charts1;
		this.charts2 = charts2;
		this.headers = headers;
		this.directoryPath = directoryPath;
		this.output = output;

		/*Summary values*/
		numberCompared = 0;
		allCompared = true;
		numberClean = 0;
		numberSignificant = 0;
		numberHits = 0;
	}

	@Override
	public ComparisonSummary call(){
		try{
			return this.save();
		}catch (IOException e){
			TextFlowWriter.writeError(e.getMessage(), this.output);
			return null;
		}
	}

	private ComparisonSummary save() throws IOException{

		/*Set up save variables*/
		final String savePath = this.directoryPath + APPEND_FILE_NAME;
		File savedFile = new File(savePath);
		FileWriter fw = new FileWriter(savedFile);

		/**
		 * Build headers
		 */
		for (int i = 0; i < headers.size(); i++){
			currentLineNumber ++;
			//keep offsets the same, but ignore these lines
			if(i != headers.size()-1) {
				fw.write("\n");
				continue;
			}

			/*build headers based on rows below*/

			//generalized header for experiments
			StringBuilder expHeader = new StringBuilder();
			expHeader.append("C 1/2, C 1/2 SD, b, bSD, Adjusted R Squared,");

			//actual header 
			StringBuilder sb = new StringBuilder();
			sb.append("Peptide");
			sb.append(",");
			sb.append("Protein (or Accession Number)");
			sb.append(","); 
			sb.append(","); //insert empty cell
			sb.append(expHeader);
			sb.append(","); //insert empty cell
			sb.append(expHeader);
			sb.append(","); //insert empty cell
			sb.append("Clean?");
			sb.append(",");
			sb.append("Delta C 1/2 Midpoint");
			sb.append(",");
			sb.append("Significant?");
			sb.append(",");
			sb.append("Passes Difference Analysis?"); //1.3
			sb.append(","); //1.3
			sb.append("Hit?");
			sb.append("\n");
			fw.write(sb.toString());
		}

		/** 
		 * Build / Write rows
		 * Format:
		 * peptide[0] accession_number[1] |space| control values (chalf, sd, b, sd, adjrsq) |space| ligand values (etc...) |space| 
		 * clean? delta c1/2 Signifidant? DifferenceAnalysis? confident?
		 */

		List<HitContainer> hits = new ArrayList<HitContainer>();
		List<Double> allDeltaMidpoints = new ArrayList<Double>();

		for(int i = 0; i < charts1.size(); i++){
			currentLineNumber++;

			Chartable c1 = charts1.get(i);
			Chartable c2 = charts2.get(i);
			updateProgress(i, charts1.size()); //update SimpleDoubleProperty for bindings
			//check to ensure peptides are the same
			if (!c1.peptide.equals(c2.peptide)){
				TextFlowWriter.writeError("SKIP...#"+i+" "+c1.peptide + " != " + c2.peptide, this.output);
				fw.write("\n"); //make sure ordering is kept intact
				allCompared = false;
				continue;
			}		

			numberCompared++; //update after peptide check

			//build line
			StringBuilder line = new StringBuilder();
			line.append(c1.peptide);
			line.append(","); //csv builder
			line.append(c1.protein);
			line.append(",");

			//build control
			line.append(","); //add empty cell between blocks 
			line.append(c1.chalf);
			line.append(",");
			line.append(c1.chalfSD);
			line.append(",");
			line.append(c1.b);
			line.append(",");
			line.append(c1.bSD);
			line.append(",");
			line.append(c1.adjRSquared);
			line.append(",");

			//build ligand
			line.append(","); //add empty cell between blocks 
			line.append(c2.chalf);
			line.append(",");
			line.append(c2.chalfSD);
			line.append(",");
			line.append(c2.b);
			line.append(",");
			line.append(c2.bSD);
			line.append(",");
			line.append(c2.adjRSquared);
			line.append(",");

			//build comparisons
			line.append(","); //add empty cell

			// row clean?
			boolean controlClean = c1.adjRSquared > FFConstants.ADJ_R_SQ_HEURISTIC;
			boolean ligandClean = c2.adjRSquared > FFConstants.ADJ_R_SQ_HEURISTIC;
			boolean overallClean = controlClean == true && ligandClean == true;
			if(overallClean) numberClean ++;
			String clean = (overallClean) ? "TRUE" : "FALSE"; 
			line.append(clean);
			line.append(",");

			//delta midpoint values
			double dMidpoint = c1.chalf - c2.chalf;
			line.append(dMidpoint);
			line.append(",");

			// |dMidpoint| > heuristic?
			boolean significant = Math.abs(dMidpoint) > FFConstants.MIDPOINT_HEURISTIC;
			if (significant) numberSignificant++;
			String significantText = (significant) ? "TRUE" : "FALSE";
			line.append(significantText);
			line.append(",");

			//is this a hit?
			//for hit, both runs must be clean and the dMidpoint must be significant
			boolean hit = (overallClean && significant);
			if(hit) numberHits ++;
			String hitText = (hit) ? "TRUE" : "FALSE";
			line.append(hitText);
			line.append(",");
			line.append("\n");

			allDeltaMidpoints.add(dMidpoint);
			//if a hit is found, make a new HitContainer to pass the data to HTMLGenerator
			if(hit){
				hits.add(new HitContainer(currentLineNumber,
						c1.peptide, c1.protein, c1.chalf, c1.adjRSquared,
						c2.chalf, c2.adjRSquared, dMidpoint));
			}

			//End of line writing, write line to file
			fw.write(line.toString());
		}

		//after all lines are finished, write a brief summary of the comparison

		fw.write(",\n,\n");
		fw.write("Number Peptides Compared,"+numberCompared+"\n");
		fw.write("All Peptides Compared?,"+(allCompared ? "yes" : "no")+"\n");
		fw.write("Number Clean Runs,"+numberClean + "\n");
		fw.write("Number Significant Peptides,"+numberSignificant + "\n");
		fw.write("Number Hits,"+numberHits+"\n");

		fw.flush();
		fw.close();

		ComparisonSummary compSummary = new ComparisonSummary(
				numberCompared, allCompared, numberClean, 
				numberSignificant, numberHits, hits, allDeltaMidpoints);
		return compSummary;
	}
}
