import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;


public class FFModelDualSinglet extends AbstractFFModel{

	public FFModelDualSinglet(String filePath, String denaturantPath,
			TextFlow tf, boolean generateGraphs) {
		super(filePath, denaturantPath, tf, generateGraphs);
	}

	@Override
	public void writeLoadedMessage() {
		Text message = new Text(
				"Loaded data into "+this.getClass().getName()+"\n"
						+ "                     SPROX File: "+this.SPROX1 + "\n"
						+ "              Denaturants File: "+this.denaturantPath + "\n"
						+ "             Number Columns: "+super.data.getRuns1().get(0).length+"\n"
						+ "             Generate Graphs: "
				);

		message.setFill(TextFlowWriter.FFBlue);

		//Build genGraphs Message
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
	public void save() {	

		/**
		 * Make folder for Graphs / Histograms / Analysis / Summary file(s)
		 */
		/*Generate the super directory path, which contains all the saved files*/
		super.superPath = generateDirectoryPath(super.SPROX1) + File.separator;
		new File(superPath).mkdirs(); //make directory

		/**
		 * Save main CalculatedParameters.csv
		 */
		// TODO Auto-generated method stub
		FFModelSave ffsave = new FFModelSave(super.data.getHeaders1(), this.data.getRuns1(), super.superPath);

		Platform.runLater(()->{
			super.progress.unbind();
			super.progress.bind(ffsave.progressProperty());
		});
		TextFlowWriter.writeInfo("Saving file...", super.output);

		FFError saveStatus = ffsave.call();
		if(saveStatus == FFError.NoError){
			TextFlowWriter.writeSuccess("Successfully saved "+super.savedFilePath, super.output);
		}
		else{
			writeError("Error saving file: "+saveStatus);
		}

		/**
		 * As this is a Dual experiment file, a comparison is generated from the chartables stored in data
		 */

		TextFlowWriter.writeInfo("Calculating Analysis File", this.output);

		FFChartableComparator ffcc = new FFChartableComparator(super.data.chartables1, super.data.chartables2,
				super.data.headers1, super.superPath, super.output);

		Platform.runLater(()->{
			super.progress.unbind();
			super.progress.bind(ffcc.progressProperty());
		});

		compSummary = ffcc.call();

		if(compSummary != null){
			TextFlowWriter.writeSuccess("Successfully compared runs", this.output);
		}
		else{
			TextFlowWriter.writeError("Error comparing runs", this.output);
		}
		
		/**
		 * We also want to save a new file that contains the intensities of ONLY the hit peptides
		 */
		
		HitSave hs = new HitSave(this);
		TextFlowWriter.writeInfo("Saving Hits...", super.output);
		Platform.runLater(()->{
			super.progress.unbind();
			super.progress.bind(hs.progressProperty());
		});

		boolean success = hs.call();
		if (success) TextFlowWriter.writeSuccess( "Successfully saved Hit List.csv" ,super.output);

	}

	@Override
	public void generateGraphs() {
		// TODO Auto-generated method stub

		final String graphsPath = superPath + "Graphs";

		/**
		 * Generate run graphs
		 */
		FFDualGraphGenerator ffdgg = new FFDualGraphGenerator(super.data.getChartables1(), super.data.getChartables2(), 
				super.data.getDenaturants(), graphsPath, super.data.getOffset1(),
				super.output);

		//bind progress to average of both workers
		Platform.runLater(()->{
			running.set(true);
			progress.unbind();
			progress.bind(ffdgg.progressProperty());
		});
		//generate graphs and return errors
		TextFlowWriter.writeInfo("Generating experiment graphs using "+ffdgg.getClass().getName(), super.output);
		ArrayList<GraphStatus> successList = ffdgg.call();

		//check errors
		int numberErrors = 0;
		for (GraphStatus ele : successList){
			if (ele.getStatus() != FFError.NoError) {
				TextFlowWriter.writeError("Error Generating Graph #"+ele.getNumber(), this.output);
				numberErrors++;
			}
		}

		//successful alert
		if(numberErrors == 0)
			TextFlowWriter.writeSuccess("Successfully generated "+successList.size() + " graphs", this.output);
	}

	@Override
	public void generateHTML() {
		// TODO Auto-generated method stub
		HTMLGenerator hg = new HTMLGenerator(this, compSummary);
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

		List<FFError> histoErrors = new ArrayList<FFError>();
		/*Generate Control Data*/
		List<Double> cHalfValues = new ArrayList<Double>();
		List<Double> bValues = new ArrayList<Double>();
		List<Double> adjustedRSquaredValues = new ArrayList<Double>();
		for (Chartable chartable: super.data.chartables1){
			cHalfValues.add(chartable.chalf);
			bValues.add(chartable.b);
			adjustedRSquaredValues.add(chartable.adjRSquared);
		}
		TextFlowWriter.writeInfo("Calculating C 1/2", this.output);
		FFHistogramGenerator chalfGenerator = new FFHistogramGenerator(cHalfValues, "Control C Midpoint Histogram",
				superPath, -1);
		histoErrors.add(chalfGenerator.call());

		TextFlowWriter.removeLast(this.output);
		TextFlowWriter.writeInfo("Calculating b", this.output);
		FFHistogramGenerator bGenerator = new FFHistogramGenerator(bValues, "Control b Histogram"
				, superPath, FFConstants.InitialBValue);
		histoErrors.add(bGenerator.call());
		
		TextFlowWriter.removeLast(this.output);
		TextFlowWriter.writeInfo("Calculating Adjusted R Squared", this.output);
		FFHistogramGenerator adjRGenerator = new FFHistogramGenerator(adjustedRSquaredValues, "Control Adjusted R Squared Histogram"
				, superPath, FFConstants.ADJ_R_SQ_HEURISTIC);
		histoErrors.add(adjRGenerator.call());

		/*Generate ligand Data*/
		cHalfValues = new ArrayList<Double>();
		bValues = new ArrayList<Double>();
		adjustedRSquaredValues = new ArrayList<Double>();
		for (Chartable chartable: super.data.chartables2){
			cHalfValues.add(chartable.chalf);
			bValues.add(chartable.b);
			adjustedRSquaredValues.add(chartable.adjRSquared);
		}
		
		TextFlowWriter.removeLast(this.output);
		TextFlowWriter.writeInfo("Calculating C 1/2", this.output);
		FFHistogramGenerator chalfLigandGenerator = new FFHistogramGenerator(cHalfValues, "Ligand Midpoint Histogram"
				,superPath, -1);
		histoErrors.add(chalfLigandGenerator.call());

		TextFlowWriter.removeLast(this.output);
		TextFlowWriter.writeInfo("Calculating b", this.output);
		FFHistogramGenerator bLigandGenerator = new FFHistogramGenerator(bValues, "Ligand b Histogram"
				, superPath, FFConstants.InitialBValue);
		histoErrors.add(bLigandGenerator.call());
		
		TextFlowWriter.removeLast(this.output);
		TextFlowWriter.writeInfo("Calculating Adjusted R Squared", this.output);
		FFHistogramGenerator adjRLigandGenerator = new FFHistogramGenerator(adjustedRSquaredValues, "Ligand Adjusted R Squared Histogram"
				, superPath, FFConstants.ADJ_R_SQ_HEURISTIC);
		histoErrors.add(adjRLigandGenerator.call());
		
		/*Generate comparing data*/
		List<Double> comparedMidpoints = new ArrayList<Double>();
		for (Double ele : compSummary.deltaMidpointList){
			comparedMidpoints.add(ele);
		}
		TextFlowWriter.removeLast(this.output);
		TextFlowWriter.writeInfo("Calculating delta Midpoint", this.output);
		FFHistogramGenerator deltaMidpointGenerator = new FFHistogramGenerator(comparedMidpoints, "Delta Midpoint Histogram", 
				superPath, FFConstants.MIDPOINT_HEURISTIC);
		histoErrors.add(deltaMidpointGenerator.call());
		

		int numberErrors = 0;
		for (FFError ffe : histoErrors){
			if (ffe != FFError.NoError)
				numberErrors++;
		}
		if(numberErrors == 0){ //success
			TextFlowWriter.removeLast(this.output);
			TextFlowWriter.writeSuccess("Successfully drew histograms", this.output);
		}
		else{
			TextFlowWriter.removeLast(this.output);
			TextFlowWriter.writeError("Error drawing histograms", this.output);
		}
		
	}
}
