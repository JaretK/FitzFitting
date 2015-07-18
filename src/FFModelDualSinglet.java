import java.io.File;
import java.util.ArrayList;

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
		final String leftPath = superPath + File.separator + "Left Experiment";
		final String rightPath = superPath + File.separator + "Right Experiment";

		FFGraphGenerator ffggLeft = new FFGraphGenerator(super.data.getChartables1(), 
				super.data.getDenaturants(), leftPath, super.data.getOffset1(),
				super.output);
		FFGraphGenerator ffggRight = new FFGraphGenerator(super.data.getChartables2(),
				super.data.getDenaturants(), rightPath, super.data.getOffset2(), 
				super.output);

		TextFlowWriter.writeInfo("Generating graphs", super.output);

		//bind progress to average of both workers
		Platform.runLater(()->{
			running.set(true);
			progress.unbind();
			progress.bind(ffggLeft.progressProperty());
		});
		//generate graphs and return errors
		TextFlowWriter.writeInfo("\nGenerating left experiment graphs (1/2)", super.output);
		ArrayList<GraphStatus> successListLeft = ffggLeft.call();
		
		Platform.runLater(()->{
			progress.unbind();
			progress.bind(ffggRight.progressProperty());
		});
		TextFlowWriter.writeInfo("\nGenerating right experiment graphs (2/2)", super.output);
		ArrayList<GraphStatus> successListRight = ffggRight.call();
		//check errors
		int numberErrors = 0;
		for (GraphStatus ele : successListLeft){
			if (ele.getStatus() != FFError.NoError) {
				TextFlowWriter.writeError("Error Generating Graph #"+ele.getNumber(), this.output);
				numberErrors++;
			}
		}
		for (GraphStatus ele : successListRight){
			if (ele.getStatus() != FFError.NoError) {
				TextFlowWriter.writeError("Error Generating Graph #"+ele.getNumber(), this.output);
				numberErrors++;
			}
		}
		//successful alert
		System.out.println(numberErrors);
		System.out.println(successListLeft.size());
		System.out.println(successListRight.size());
		System.out.println(super.data.getRuns1().size());
		if(numberErrors == 0 && successListLeft.size() == successListRight.size())
			TextFlowWriter.writeSuccess("Successfully generated "+this.data.getRuns1().size() + " graphs", this.output);
		//alert if any charts are missing
		else{
			int numMissing = (this.data.getRuns1().size() - successListLeft.size());
			String pluralRuns = numMissing == 1 ? "run" : "runs";
			TextFlowWriter.writeError(numMissing+" "+pluralRuns+" unaccounted for", this.output);
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
