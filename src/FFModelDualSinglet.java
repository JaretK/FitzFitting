import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;


public class FFModelDualSinglet extends AbstractFFModel{

	public FFModelDualSinglet(String filePath, String denaturantPath,
			TextFlow tf, boolean generateGraphs, double midpoint) {
		super(filePath, denaturantPath, tf, generateGraphs, midpoint);
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
		// TODO Auto-generated method stub
		FFModelSave ffsave = new FFModelSave(super.data.getHeaders1(), this.data.getRuns1(), this.SPROX1);

		Platform.runLater(()->{
			super.progress.unbind();
			super.progress.bind(ffsave.progressProperty());
		});
		TextFlowWriter.writeInfo("Saving file...", super.output);

		FFError saveStatus = ffsave.call();
		super.savedFilePath = ffsave.getSavedFilePath();
		if(saveStatus == FFError.NoError){
			TextFlowWriter.writeSuccess("Successfully saved "+super.savedFilePath, super.output);
		}
		else{
			writeError("Error saving file: "+saveStatus);
		}
	}

	@Override
	public void generateGraphs() {
		// TODO Auto-generated method stub

		String superPath = super.savedFilePath.equals("") ? this.SPROX1 : this.savedFilePath;
		superPath = splitIntoDirectory(superPath);
		new File(superPath).mkdirs(); //initialize superpath
		final String graphsPath = superPath + File.separator + "Graphs";

		/**
		 * Generate run graphs
		 */
		FFDualGraphGenerator ffdgg = new FFDualGraphGenerator(super.data.getChartables1(), super.data.getChartables2(), 
				super.data.getDenaturants(), graphsPath, super.data.getOffset1(),
				super.output);

		TextFlowWriter.writeInfo("Generating graphs", super.output);

		//bind progress to average of both workers
		Platform.runLater(()->{
			running.set(true);
			progress.unbind();
			progress.bind(ffdgg.progressProperty());
		});
		//generate graphs and return errors
		TextFlowWriter.writeInfo("\nGenerating experiment graphs", super.output);
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


		/**
		 * Generate Histograms
		 */
		TextFlowWriter.writeInfo("Generating Histograms...", this.output);

		List<FFError> histoErrors = new ArrayList<FFError>();
		/*Generate Control Data*/
		List<Double> cHalfValues = new ArrayList<Double>();
		List<Double> bValues = new ArrayList<Double>();
		for (Chartable chartable: super.data.chartables1){
			cHalfValues.add(chartable.chalf);
			bValues.add(chartable.b);
		}
		TextFlowWriter.writeInfo("Calculating C 1/2", this.output);
		FFHistogramGenerator chalfGenerator = new FFHistogramGenerator(cHalfValues, "Control C Midpoint Histogram",superPath);
		histoErrors.add(chalfGenerator.call());

		TextFlowWriter.removeLast(this.output);
		TextFlowWriter.writeInfo("Calculating b", this.output);
		FFHistogramGenerator bGenerator = new FFHistogramGenerator(bValues, "Control b Histogram", superPath);
		histoErrors.add(bGenerator.call());

		/*Generate ligand Data*/
		cHalfValues = new ArrayList<Double>();
		bValues = new ArrayList<Double>();
		for (Chartable chartable: super.data.chartables2){
			cHalfValues.add(chartable.chalf);
			bValues.add(chartable.b);
		}
		TextFlowWriter.writeInfo("Calculating C 1/2", this.output);
		FFHistogramGenerator chalfLigandGenerator = new FFHistogramGenerator(cHalfValues, "Ligand Midpoint Histogram",superPath);
		histoErrors.add(chalfLigandGenerator.call());

		TextFlowWriter.removeLast(this.output);
		TextFlowWriter.writeInfo("Calculating b", this.output);
		FFHistogramGenerator bLigandGenerator = new FFHistogramGenerator(bValues, "Ligand b Histogram", superPath);
		histoErrors.add(bLigandGenerator.call());

		numberErrors = 0; //reset
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

}
