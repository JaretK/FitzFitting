import java.io.File;
import java.util.ArrayList;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
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
public class FFModelSinglet implements IFFModel{

	//File Path to the SPROX csv
	private final String filePath;
	private final File SPROXFile;

	//FilePath to the denaturant csv
	private final String denaturantPath;
	private final File denaturantFile;

	//Should graphs be generated? (A time consuming processs)
	private final boolean generateGraphs;

	//TextFlow hook to direct output towards
	private final TextFlow output;

	//Contains the run info and the calculated Chalf values
	private final DataSet data;

	//Communication between DataSet and FFModelSinglet
	private FFError status;

	//Communication between FFController and IFFModel 
	private SimpleBooleanProperty running = new SimpleBooleanProperty(true);
	private SimpleDoubleProperty progress = new SimpleDoubleProperty();

	//Internal Communication
	private String savedFilePath = "";


	public FFModelSinglet(String filePath, String denaturantPath ,TextFlow tf ,
			boolean generateGraphs){

		this.filePath = filePath;
		this.denaturantPath = denaturantPath;
		this.generateGraphs = generateGraphs;
		this.output = tf;

		this.SPROXFile = getFile(this.filePath);
		this.denaturantFile = getFile(this.denaturantPath);
		this.data = new DataSet(this.SPROXFile, this.denaturantFile ,this.output);

		/*
		 * Bind progress to DataSet progress property
		 */
		progress.bind(this.data.progressProperty());

		this.status =  this.data.load();
		if (this.status == FFError.NoError){
			TextFlowWriter.writeSuccess("Successfully loaded CSV", this.output);
		}
		else{
			TextFlowWriter.writeError("Error: "+this.status, this.output);
		}

	}

	/**
	 * 
	 * @return the File object associated with filePath
	 */
	public File getFile(String path) {
		try{
			return new File(path);
		}
		catch(NullPointerException e){
			TextFlowWriter.writeError(e.getMessage(), output);
			return null;
		}
	}

	public FFError getStatus(){
		return this.status;
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
		FFError ffModelSingletExitCode = data.digest();
		if(ffModelSingletExitCode != FFError.NoError){
			TextFlowWriter.writeError("Data digest failed => "+ffModelSingletExitCode, this.output);
		}
		else{
			TextFlowWriter.writeSuccess("Successfully analyzed files", this.output);
		}
		terminate();
	}

	/**
	 * Writes DataSet.getRuns() to a new file
	 */
	public void save(){
		FFModelSave ffsave = new FFModelSave(this.data.getHeader(), this.data.getRuns(), this.filePath);

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

		//If somehow called before saving / saving failed, default to passed filepath
		String filePathToPass = (this.savedFilePath.equals("")) ? this.filePath : this.savedFilePath;

		//Instantiate FFGraphGenerator object
		FFGraphGenerator ffgg = new FFGraphGenerator(this.data.getRuns(), this.data.getDenaturants(), filePathToPass, this.output);

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
		if(numberErrors == 0 && successList.size() == this.data.getRuns().size())
			TextFlowWriter.writeSuccess("Successfully generated "+this.data.getRuns().size() + " graphs", this.output);
		//alert if any charts are missing
		else{
			int numMissing = (this.data.getRuns().size() - successList.size());
			String pluralRuns = numMissing == 1 ? "run" : "runs";
			TextFlowWriter.writeError(numMissing+" "+pluralRuns+" unaccounted for", this.output);
		}
	}

	@Override
	public void terminate() {
		//Send to FFController
		Platform.runLater(()->{
			running.set(false);
		});
	}

	public boolean getGenerateGraphsStatus(){
		return this.generateGraphs;
	}

	public SimpleBooleanProperty runningProperty(){
		return running;
	}

	public SimpleDoubleProperty progressProperty(){
		return progress;
	}

	@Override
	public void writeError(String text){
		TextFlowWriter.writeError(text, this.output);
	}

	//Called as an indication everything is loaded
	@Override
	public void writeLoadedMessage() {
		//Build skeleton
		Text message = new Text(
				"Loaded data into FFModelSinglet.\n"
						+ "                     SPROX File: "+this.filePath + "\n"
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
