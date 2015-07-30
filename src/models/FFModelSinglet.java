package models;

import graph_generators.FFHistogramGenerator;
import graph_generators.FFSingleGraphGenerator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import statics.FFConstants;
import statics.TextFlowWriter;
import HTML.HTMLGenerator;
import containers.Chartable;
import containers.FFError;
import containers.GraphStatus;

/**
 * This is the Model class for the MVC architecture of FitzFitting
 * View = FFMain, FFLayout.fxml
 * Control = FFController
 * 
 * FFModelSinglet performs a single computational round on the inputted data set,
 * 
 * without comparison between another data set. This is called when Compare is NOT 
 * 
 * selected.
 * 
 * @author jkarnuta
 *
 */
public class FFModelSinglet extends AbstractFFModel{

	public FFModelSinglet(String filePath, String denaturantPath ,TextFlow tf ,
			boolean generateGraphs){

		super(filePath, denaturantPath, tf, generateGraphs);
	}
	/**
	 * Only used for Debugging purposes
	 */
	public FFModelSinglet(){
	}

	/**
	 * Writes DataSet.getRuns() to a new file
	 */
	public void save(){
		/**
		 * Make folder for Graphs / Histograms / Analysis / Summary file(s)
		 */
		/*Generate the super directory path, which contains all the saved files*/
		super.setSuperPath(generateDirectoryPath(super.SPROX1) + File.separator);
		new File(getSuperPath()).mkdirs(); //make directory
		
		
		FFModelSave ffsave = new FFModelSave(this);

		//reset progress to use FFModelSave's updateProgress
		Platform.runLater(()->{
			progress.unbind();
			progress.bind(ffsave.progressProperty());
		});

		//communicate with GUI
		TextFlowWriter.writeInfo("Saving file...", this.output);

		FFError saveStatus = ffsave.call();
		if(saveStatus == FFError.NoError){
			TextFlowWriter.writeSuccess("Successfully saved "+this.savedFilePath, this.output);
		}
		else{
			writeError("Error saving file: "+saveStatus);
		}
	}


	/**
	 * Generates graphs calculated by Dataset.getRuns()
	 */
	public void generateGraphs(){

		final String graphsPath = getSuperPath() + "Graphs";
		
		/**
		 * Generate individual run plots
		 */
		//Instantiate FFGraphGenerator object
		FFSingleGraphGenerator ffgg = new FFSingleGraphGenerator(this.data.getChartables1(), this.data.getDenaturants(), graphsPath, this.data.getOffset1() ,this.output);

		TextFlowWriter.writeInfo("Generating Graphs", this.output);
		//disable buttons and text fields to wait until graph generation is over
		Platform.runLater(()->{
			running.set(true);
			progress.unbind();
			progress.bind(ffgg.progressProperty());
		});

		ArrayList<GraphStatus> successList = ffgg.call();

		/* Alert User of status of Graph Generation*/
		int numberErrors = 0;
		//Alert if any graph generation failed
		for (GraphStatus ele : successList){
			if (ele.getStatus() != FFError.NoError) {
				TextFlowWriter.writeError("Error Generating Graph #"+ele.getNumber(), this.output);
				numberErrors++;
			}
		}
		//successful alert
		if(numberErrors == 0)
			TextFlowWriter.writeSuccess("Successfully generated "+this.data.getRuns1().size() + " graphs", this.output);
		//alert if any charts are missing
		else{
			int numMissing = (this.data.getRuns1().size() - successList.size());
			String pluralRuns = numMissing == 1 ? "run" : "runs";
			TextFlowWriter.writeError(numMissing+" "+pluralRuns+" unaccounted for", this.output);
		}
	}

	//Called as an indication everything is loaded
	@Override
	public void writeLoadedMessage() {
		//Build skeleton
		Text message = new Text(
				"Loaded data into FFModelSinglet.\n"
						+ "                     SPROX File: "+this.SPROX1 + "\n"
						+ "              Denaturants File: "+this.denaturantPath + "\n"
						+ "             Generate Graphs: ");

		message.setFill(TextFlowWriter.FFBlue);

		//Build genGraphs message
		Text graphsMessage = null;
		if(generateGraphs){
			graphsMessage = new Text("YES");
			graphsMessage.setFill(TextFlowWriter.FFGreen);
		}
		else{
			graphsMessage = new Text("NO");
			graphsMessage.setFill(TextFlowWriter.FFRed);
		}


		Text[] texts = new Text[]{message, graphsMessage, new Text("\n")};
		TextFlowWriter.addArray(texts, this.output);
	}

	@Override
	public void generateHTML() {
		// TODO Auto-generated method stub
		HTMLGenerator hg = new HTMLGenerator(this, null);
		TextFlowWriter.writeInfo("Generating HTML Summary...", super.output);
		try{
			hg.call();
			TextFlowWriter.writeSuccess("Successfully generated HTML Summary", this.output);
		}catch(Exception e){
			e.printStackTrace();
			TextFlowWriter.writeError(e.getMessage(), super.output);
		}
	}
	@Override
	public void generateHistograms() {
		/**
		 * Generate Histograms
		 */
		TextFlowWriter.writeInfo("Generating Histograms...", this.output);
		
		/*Generate C 1/2 Histogram*/
		List<Double> cHalfValues = new ArrayList<Double>();
		List<Double> bValues = new ArrayList<Double>();
		List<Double> adjustedRSquaredValues = new ArrayList<Double>();
		for (Chartable chartable: super.data.getChartables1()){
			cHalfValues.add(chartable.chalf);
			bValues.add(chartable.b);
			adjustedRSquaredValues.add(chartable.adjRSquared);
		}
		TextFlowWriter.writeInfo("Calculating C 1/2", this.output);
		FFHistogramGenerator chalfGenerator = new FFHistogramGenerator(cHalfValues, "Midpoint Histogram",getSuperPath(), -1, false);
		FFError chalfHistogramError = chalfGenerator.call();
		
		TextFlowWriter.removeLast(this.output);
		TextFlowWriter.writeInfo("Calculating b", this.output);
		FFHistogramGenerator bGenerator = new FFHistogramGenerator(bValues, "b Histogram", getSuperPath(), 
				FFConstants.InitialBValue, false);
		FFError bHistogramError = bGenerator.call();
		
		TextFlowWriter.removeLast(this.output);
		TextFlowWriter.writeInfo("Calculating Adjusted R Squared", this.output);
		FFHistogramGenerator adjRGenerator = new FFHistogramGenerator(adjustedRSquaredValues, "Adjusted R Squared Histogram", getSuperPath(),
				FFConstants.ADJ_R_SQ_HEURISTIC, false);
		FFError adjRHistogramGerror = adjRGenerator.call();
		
		if(chalfHistogramError == FFError.NoError && bHistogramError == FFError.NoError
				&& adjRHistogramGerror == FFError.NoError){
			TextFlowWriter.removeLast(this.output);
			TextFlowWriter.writeSuccess("Successfully drew histograms", this.output);
		}
		else{
			TextFlowWriter.removeLast(this.output);
			TextFlowWriter.writeError("Error drawing histograms", this.output);
		}
		
	}

}
