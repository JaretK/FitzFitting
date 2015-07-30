package statics;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;


/**
 * Contains static methods for various static operations
 * @author jkarnuta
 *
 */
public class FFOperations {


	/*
	 * Returns a file from the filepath, throws exception
	 */
	public static File retrieveFile(String filepath) throws FileNotFoundException{
		return new File (filepath);
	}

	/*
	 * Returns a file from the filepath, null if error
	 */
	public static File forceRetrieveFile(String filepath){
		try
		{
			return retrieveFile(filepath);
		}
		catch(FileNotFoundException e)
		{
			return null;
		}
	}
	
	/**
	 * Returns the necessary header titles comprised of the labels for the calculated values
	 */
	public static List<String> getHeaderAdditions(){
		@SuppressWarnings("serial")
		ArrayList<String> returnList = new ArrayList<String>(){{
			add("C 1/2");
			add("C 1/2 SD");
			add("b");
			add("b SD");
			add("Adjusted R Squared");
			add("");
		}};
		return returnList;
	}

	public static ArrayList<String> asList(String[] array){
		ArrayList<String> returnList = new ArrayList<String>();
		for (String ele : array){
			returnList.add(ele);
		}
		return returnList;
	}
	
	public static String doubleArrayToCSV(double[] darr){
		StringBuilder csv = new StringBuilder();
		for (int i = 0; i < darr.length; i++){
			csv.append(darr[i]+",");
		}
		return csv.substring(0, csv.length()-1).toString();
	}
	
	/**
	 * Converts a string array into a CSV readable string
	 * String[]{"x","y","z"} ==> "x,y,z"
	 * @param array
	 * @return CSV'd string 
	 */
	public static String stringArrayToCSV(String[] array){
		StringBuilder sb = new StringBuilder();
		for (String ele : array){
			sb.append( "," + ele );
		}
		return (sb.length() > 1) ? sb.substring(1) : sb.toString(); // clip the first comma
	}
	
	/**
	 * Converts a string array to a CSV readable string
	 * {x,y,z,a,b,c}, 2 => "x,y,z"
	 * @param array
	 * @param lastIndex last index to include in the CSV from the string array
	 * @return CSV'd string
	 */
	public static String stringArrayToCSV(String[] array, int lastIndex){
		String[] newArray = new String[lastIndex];
		for (int i = 0; i < newArray.length; i++){
			newArray[i] = array[i];
		}
		return stringArrayToCSV(newArray);
	}
	
}
