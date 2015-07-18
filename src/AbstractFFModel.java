import java.io.File;
import java.io.FileNotFoundException;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.text.TextFlow;



public abstract class AbstractFFModel {

	//File Path to the SPROX csv
	protected final String SPROX1;
	protected final File SPROXFile;

	//FilePath to the denaturant csv
	protected final String denaturantPath;
	protected final File denaturantFile;
	
	//significant midpoint difference shifts
	protected final double midpointTolerance;

	//Should graphs be generated? (A time consuming processs)
	protected final boolean generateGraphs;

	//TextFlow hook to direct output
	protected final TextFlow output;

	//Contains the run info and the calculated Chalf values
	protected AbstractDataSet data;

	//Communication between DataSet and FFModelSinglet
	protected FFError status;

	//Communication between FFController and IFFModel 
	protected SimpleBooleanProperty running = new SimpleBooleanProperty(true);
	protected SimpleDoubleProperty progress = new SimpleDoubleProperty();

	//Internal Communication
	protected String savedFilePath = "";
	
	//AbstractFFModel constructor
	public AbstractFFModel(String filePath, String denaturantPath ,TextFlow tf ,
			boolean generateGraphs, double midpoint){
		this.SPROX1 = filePath;
		this.denaturantPath = denaturantPath;
		this.generateGraphs = generateGraphs;
		this.output = tf;

		this.SPROXFile = getFile(this.SPROX1);
		this.denaturantFile = getFile(this.denaturantPath);
		this.midpointTolerance = midpoint;
		

		
	}
	
	/**
	 * Abstract Methods that must be implemented by supclasses
	 */

	public abstract void writeLoadedMessage();

	public abstract void save();

	public abstract void generateGraphs();
	

	/**
	 * Begins the digestion of the inputted csv
	 */
	public void start() {
		// TODO Auto-generated method stub
		TextFlowWriter.writeInfo("Analyzing files...", this.output);
		FFError ffModelExitCode = data.digest();
		if(ffModelExitCode != FFError.NoError){
			TextFlowWriter.writeError("Data digest failed => "+ffModelExitCode, this.output);
		}
		else{
			TextFlowWriter.writeSuccess("Successfully analyzed files", this.output);
		}
		this.status = ffModelExitCode;
		terminate();
	}
	
	
	/**
	 * Loads the AbstractDataSet implementation into the model
	 * @param dataset
	 */
	public void load(AbstractDataSet dataset){
		this.data = dataset;

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
			return FFOperations.retrieveFile(path);
		}
		catch(FileNotFoundException e){
			TextFlowWriter.writeError(e.getMessage(), output);
			return null;
		}
	}

	public FFError getStatus(){
		return this.status;
	}
	
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

	public void writeError(String text){
		TextFlowWriter.writeError(text, this.output);
	}
}
