import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;



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
			boolean generateGraphs, double midpoint){

		super(filePath, denaturantPath, tf, generateGraphs, midpoint);
	}

	/**
	 * Writes DataSet.getRuns() to a new file
	 */
	public void save(){
		FFModelSave ffsave = new FFModelSave(this.data.getHeaders1(), this.data.getRuns1(), this.SPROX1);

		//reset progress to use FFModelSave's updateProgress
		Platform.runLater(()->{
			progress.unbind();
			progress.bind(ffsave.progressProperty());
		});

		//communicate with GUI
		TextFlowWriter.writeInfo("Saving file...", this.output);

		FFError saveStatus = ffsave.call();
		savedFilePath  = ffsave.getSavedFilePath();
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

		String superPath = super.savedFilePath.equals("") ? this.SPROX1 : this.savedFilePath;
		superPath = splitIntoDirectory(superPath);
		new File(superPath).mkdirs(); //initialize superpath
		final String graphsPath = superPath + File.separator + "Graphs";
		
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
		if(numberErrors == 0 && successList.size() == this.data.getRuns1().size())
			TextFlowWriter.writeSuccess("Successfully generated "+this.data.getRuns1().size() + " graphs", this.output);
		//alert if any charts are missing
		else{
			int numMissing = (this.data.getRuns1().size() - successList.size());
			String pluralRuns = numMissing == 1 ? "run" : "runs";
			TextFlowWriter.writeError(numMissing+" "+pluralRuns+" unaccounted for", this.output);
		}
		
		
		/**
		 * Generate Histograms
		 */
		TextFlowWriter.writeInfo("Generating Histograms...", this.output);
		
		/*Generate C 1/2 Histogram*/
		List<Double> cHalfValues = new ArrayList<Double>();
		List<Double> bValues = new ArrayList<Double>();
		for (Chartable chartable: super.data.chartables1){
			cHalfValues.add(chartable.chalf);
			bValues.add(chartable.b);
		}
		TextFlowWriter.writeInfo("Calculating C 1/2", this.output);
		FFHistogramGenerator chalfGenerator = new FFHistogramGenerator(cHalfValues, "Midpoint Histogram",superPath);
		FFError chalfHistogramError = chalfGenerator.call();
		
		TextFlowWriter.removeLast(this.output);
		TextFlowWriter.writeInfo("Calculating b", this.output);
		FFHistogramGenerator bGenerator = new FFHistogramGenerator(bValues, "b Histogram", superPath);
		FFError bHistogramError = bGenerator.call();
		
		if(chalfHistogramError == FFError.NoError && bHistogramError == FFError.NoError){
			TextFlowWriter.removeLast(this.output);
			TextFlowWriter.writeSuccess("Successfully drew histograms", this.output);
		}
		else{
			TextFlowWriter.removeLast(this.output);
			TextFlowWriter.writeError("Error drawing histograms", this.output);
		}
	}
	private String splitIntoDirectory(String initialPath){
		//split dirPath to get enclosing directory
		String[] directoryLocationArray = initialPath.split(File.separator);
		StringBuilder directory = new StringBuilder();
		for (int i = 0; i < directoryLocationArray.length-1; i++) 
			directory.append(directoryLocationArray[i]+File.separator);

		//add filename (omit .csv)
		System.out.println(directory.toString());
		directory.append(directoryLocationArray[directoryLocationArray.length-1].split("\\.")[0]);
		return directory.toString();
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

}
