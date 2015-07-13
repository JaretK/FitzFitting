import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import javafx.concurrent.Task;


/**
 * Saves the header and ArrayList<String[]> parameters to a new file 
 * 
 * @author jkarnuta
 *
 */
public class FFModelSave extends Task<FFError>{

	private final String[] header;
	private final ArrayList<String[]> runs;
	private final String initialFilePath;
	private String savedFilePath;

	public FFModelSave(String[] header, ArrayList<String[]> runs, String initialFilePath){
		this.header = header;
		this.runs = runs;
		this.initialFilePath = initialFilePath;
		this.savedFilePath = "unknown";
	}

	public String getSavedFilePath(){
		return this.savedFilePath;
	}
	
	@Override 
	public FFError call(){
		return save();
	}

	public FFError save(){
		this.savedFilePath = generateFilePath();
		File newFile = new File(this.savedFilePath);
		long totalIterations = this.runs.size()-1; //ignoring header
		long currentIteration = 0;
		
		try {
			FileWriter fw = new FileWriter(newFile);
			
			//write header
			fw.write(stringArrayToCSV(this.header));
			fw.write("\n");
			
			//loop through runs and write each to file
			for (String[] run : runs){
				fw.write(stringArrayToCSV(run));
				fw.write("\n");
				currentIteration++;
				updateProgress(currentIteration, totalIterations);
			}
			
			fw.flush();
			fw.close();
			
		} catch (IOException e) {
			e.printStackTrace();
			return FFError.FileSaveError;
			
		}
		
		return FFError.NoError;
	}

	/**
	 * Formats the new file name from FILENAME.csv to FILENAME_FITTED_dd-mmm-yyyy.csv
	 * @return formatted file name
	 */
	private String generateFilePath(){
		String[] pathArray = this.initialFilePath.split(File.separator);
		String filename = pathArray[pathArray.length-1];
		String[] splittedFileName = filename.split("\\.");
		pathArray[pathArray.length-1] = splittedFileName[0]+"_FITTED_"+getDate()+"."+splittedFileName[1];
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
	 * mimics sep.join(array) from python
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
	
	/**
	 * Converts a string array into a CSV readable string
	 * String[]{"x","y","z"} ==> "x,y,z"
	 * @param array
	 * @return CSV'd string 
	 */
	private String stringArrayToCSV(String[] array){
		StringBuilder sb = new StringBuilder();
		for (String ele : array){
			sb.append( "," + ele );
		}
		return sb.substring(1); // clip the first comma
	}
}
