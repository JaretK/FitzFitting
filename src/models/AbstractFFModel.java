package models;
import containers.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.text.TextFlow;

import comparison.*;
import statics.*;
import datasets.*;


public abstract class AbstractFFModel {

	//File Path to the SPROX csv
	protected final String SPROX1;
	protected final File SPROXFile;

	//FilePath to the denaturant csv
	protected final String denaturantPath;
	protected final File denaturantFile;

	//Should graphs be generated? (A time consuming processs)
	protected final boolean generateGraphs;

	//TextFlow hook to direct output
	protected final TextFlow output;

	//Contains the run info and the calculated Chalf values
	public AbstractDataSet data;

	//Communication between DataSet and FFModelSinglet
	protected FFError status;

	//Communication between FFController and IFFModel 
	protected SimpleBooleanProperty running = new SimpleBooleanProperty(true);
	protected SimpleDoubleProperty progress = new SimpleDoubleProperty();

	//Internal Communication
	protected String savedFilePath = "";

	//Filepath for folder which saves all generated files
	private String superPath = "";

	//used for comparisons and hits
	ComparisonSummary compSummary = null;
	
	//AbstractFFModel constructor
	public AbstractFFModel(String filePath, String denaturantPath ,TextFlow tf ,
			boolean generateGraphs){
		this.SPROX1 = filePath;
		this.denaturantPath = denaturantPath;
		this.generateGraphs = generateGraphs;
		this.output = tf;

		this.SPROXFile = getFile(this.SPROX1);
		this.denaturantFile = getFile(this.denaturantPath);
	}
	/**
	 * Only used for Debugging purposes
	 */
	public AbstractFFModel(){
		this.SPROX1 = null;
		this.denaturantPath = null;
		this.generateGraphs = false;
		this.output = null;
		this.SPROXFile = null;
		this.denaturantFile = null;
	}

	/**
	 * Abstract Methods that must be implemented by supclasses
	 */

	public abstract void writeLoadedMessage();

	public abstract void save();

	public abstract void generateGraphs();
	
	public abstract void generateHTML();
	
	public abstract void generateHistograms();


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
	 * END APPLICATION INTERFACE LOGIC
	 * 
	 */
	

	/**
	 * Formats the new file name from FILENAME.csv to FILENAME_FITTED_dd-mmm-yyyy.csv
	 * @return formatted file name
	 */
	protected String generateDirectoryPath(String path){
		String[] pathArray = path.split(File.separator);
		String filename = pathArray[pathArray.length-1];
		String[] splittedFileName = filename.split("\\.");
		pathArray[pathArray.length-1] = splittedFileName[0]+"_FITTED_"+getDate();
		return join(pathArray, File.separator);
	}
	
	/**
	 * retreives the current date in dd-mmm-yyyy format
	 * @return
	 */
	private String getDate(){
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat format = new SimpleDateFormat("dd-MMM-YYYY");
		return format.format(cal.getTime());
	}



	/**
	 * mimics "sep".join(array) from python
	 * @param arr - array to put between seps
	 * @param sep - separation character
	 * @return joined array
	 */
	private String join(String[] arr, String sep){
		StringBuilder sb = new StringBuilder();
		for (String ele : arr){
			if(ele.equals("")){
				continue;
			}
			sb.append(sep+ele);
		}
		return sb.toString();
	}
	

	public void terminate() {
		//Send to FFController
		Platform.runLater(()->{
			running.set(false);
		});
	}
	


	/**
	 * Begin GETTER / SETTER methods
	 */
	
	
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
	
	public String getSPROXFileName(){
		return this.SPROX1;
	}
	
	public boolean getGenerateGraphs(){
		return this.generateGraphs;
	}

	public FFError getStatus(){
		return this.status;
	}

	public void setSuperPath(String path){
		this.superPath = path;
	}
	public String getSuperPath(){
		return this.superPath;
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
