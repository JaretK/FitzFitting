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
	
}
