import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.concurrent.Task;
import javafx.scene.text.TextFlow;

import containers.*;
import statics.*;

public abstract class AbstractDataSet extends Task<Void>{

	protected Double[] DenaturantConcentrations;
	private final File DenaturantFile;
	protected final TextFlow output;

	//Initialized Variables for run #1
	protected final File SPROX1File;
	private int offset1; // = number of header lines - 1
	protected List<String[]> headers1 = new ArrayList<String[]>();
	protected List<String[]> runs1 = new ArrayList<String[]>();
	protected List<Chartable> chartables1 = new ArrayList<Chartable>();

	//Declared Variables for run #2
	protected File SPROX2File;
	private int offset2;
	protected List<String[]> headers2;
	protected List<String[]> runs2;
	protected List<Chartable> chartables2;



	public AbstractDataSet(String SPROXFile, String DenaturantFile,TextFlow output){
		this.SPROX1File = FFOperations.forceRetrieveFile(SPROXFile);
		this.DenaturantFile = FFOperations.forceRetrieveFile(DenaturantFile);
		this.output = output;
	}

	public void addDoubletNature(String SPROX2File){
		this.SPROX2File = FFOperations.forceRetrieveFile(SPROX2File);
		this.chartables2 = new ArrayList<Chartable>();
	}


	/**
	 * ABSTRACT CLASSES FOR IMPLEMENTATION
	 * 
	 */

	public abstract FFError load();

	protected abstract Void call() throws Exception; //called from AbstractFFModel.start() -> AbstractDataSet.digest()

	protected FFError loadDenaturants(){

		//reads and parses denaturants file
		try (BufferedReader br = new BufferedReader(new FileReader(this.getDenaturantFile()))){
			String line = br.readLine();
			String[] buckets = line.split(",");
			DenaturantConcentrations = new Double[buckets.length];

			for (int i = 0; i < buckets.length ; i++){
				DenaturantConcentrations[i] = Double.parseDouble(buckets[i]);
			}

		} catch (IOException e) {
			TextFlowWriter.writeError(e.getMessage(), this.output);
			return FFError.ErrorParsingFile;
		} catch(NumberFormatException e){
			TextFlowWriter.writeError("NumberFormatException:" +e.getMessage(), this.output);
			return FFError.InvalidDenaturants;
		}
		return FFError.NoError;
	}

	/**
	 * Takes the loaded file and digests it to determine midpoints and b values
	 * Called on FFModel start
	 */

	public FFError digest(){
		try {
			call();
			return FFError.NoError;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return FFError.CalculationFailure;
		}
	}


	/**
	 * 
	 * GETTER / SETTER METHODS
	 * 
	 */

	public File getSPROX1File(){
		return this.SPROX1File;
	}

	public File getDenaturantFile(){
		return this.DenaturantFile;
	}

	public Double[] getDenaturants(){
		return this.DenaturantConcentrations;
	}

	/*
	 * Returns data from first uploaded dataset 
	 */
	public List<String[]> getHeaders1(){
		return headers1;
	}

	public List<String[]> getRuns1(){
		return runs1;
	}

	public int getOffset1(){
		return this.offset1;
	}

	public void setOffset1(int offset1){
		this.offset1 = offset1;
	}

	public void addChartable1(Chartable c){
		this.chartables1.add(c);
	}

	public List<Chartable> getChartables1(){
		return this.chartables1;
	}

	/*
	 * Returns data from second uploaded data set (returns null if running from Singlet)
	 */

	public List<String[]> getHeaders2(){
		return headers2;
	}

	public List<String[]> getRuns12(){
		return runs2;
	}

	public void setOffset2(int offset2){
		this.offset2 = offset2;
	}

	public int getOffset2(){
		return this.offset2;
	}

	public void addChartable2(Chartable c){
		try{
			this.chartables2.add(c);
		}
		catch(NullPointerException e){
			this.chartables2 = new ArrayList<Chartable>();
			this.chartables2.add(c);
		}
	}

	public List<Chartable> getChartables2(){
		return this.chartables2;
	}


}
