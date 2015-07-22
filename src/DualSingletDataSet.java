import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.scene.text.TextFlow;


public class DualSingletDataSet extends AbstractDataSet {

	public DualSingletDataSet(String SPROXFile, String DenaturantFile,
			TextFlow output) {
		super(SPROXFile, DenaturantFile, output);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Takes the file inputted to the super constructor and loads it into header and single runs.
	 * 
	 * Validate files
	 * 
	 * Read files / parse SPROX file
	 * 
	 * Read / parse Denaturant File (from super)
	 */
	@Override
	public FFError load() {

		/*
		 * SAME AS SingletDataSet
		 */

		/*Tests for SPROX validity*/
		int SPROXValid;
		try{
			SPROXValid = FFFileValidator.VALIDATE_SPROX(super.getSPROX1File());
		}
		catch(IOException e)
		{
			SPROXValid = -1;
			TextFlowWriter.writeError(e.getMessage(), super.output);
		}
		if(SPROXValid == -1)
		{ 
			TextFlowWriter.writeError("SPROX File is not a CSV", this.output);
			return FFError.InvalidFile;
		}
		super.setOffset1(SPROXValid);
		super.setOffset2(SPROXValid);

		/* Tests for Denaturants validity */
		boolean DenaturantsValid;

		try{
			DenaturantsValid = FFFileValidator.VALIDATE_DENATURANTS(super.getDenaturantFile());
		}
		catch(IOException e)
		{
			TextFlowWriter.writeError(e.getMessage(), super.output);
			DenaturantsValid = false;
		}

		if(!DenaturantsValid)
		{ 
			TextFlowWriter.writeError("Denaturant File is not a CSV", this.output);
			return FFError.InvalidFile;
		}

		/* Read SPROX file and parse Header */
		try(BufferedReader br = new BufferedReader(new FileReader(super.getSPROX1File()))){
			String line;
			for (int i = 0; i <= SPROXValid; i++){
				line = br.readLine();
				super.headers1.add(line.split(","));
			}

			while((line = br.readLine()) != null){
				runs1.add(line.split(","));
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			TextFlowWriter.writeError(e.getMessage(), this.output);
			return FFError.ErrorParsingFile;
		} 

		return super.loadDenaturants();
	}

	@Override
	protected Void call() throws Exception {
		/*Constants, etc*/
		final int numberSame = 2; // number of headers that are repeats (besides denaturants)
		final int offset = 5; //HARDCODING IS BAD BUT I DO IT ANYWAYS

		//parse header
		//get last header
		String[] lastHeader = super.getHeaders1().get(super.getHeaders1().size()-1);
		ArrayList<String> titleList = new ArrayList<String>();
		//populate titleList (last header i.e. contains sequence, accession number, etc.)
		for (String ele: lastHeader){
			titleList.add(ele);
		}
		//insert c 1/2, c 1/2 sd, b, b sd, adj rsq, space after each run
		final int firstInsert = offset + super.getDenaturants().length;
		titleList.addAll(firstInsert, FFOperations.getHeaderAdditions());
		final int secondInsert = firstInsert + super.getDenaturants().length + FFOperations.getHeaderAdditions().size() + numberSame;
		titleList.addAll(secondInsert, FFOperations.getHeaderAdditions());

		//header is created, add back to super.headers1
		String[] finalizedTitle = new String[titleList.size()];
		finalizedTitle = titleList.toArray(finalizedTitle); 
		super.headers1.remove(this.headers1.size()-1);
		super.headers1.add(finalizedTitle);

		/*
		 * For each run in runs, calculate chalf, b, standard devs, and adj r sq
		 */

		int totalIterations = super.getRuns1().size();

		TextFlowWriter.writeLine("", super.output);
		TextFlowWriter.writeInfo("Calculating C 1/2 and b values for inputted file(s)", super.output);

		//necessary for removing previous line
		TextFlowWriter.writeLine("", super.output);

		final double[] denaturants = new double[super.getDenaturants().length];
		for (int i = 0; i < denaturants.length; i++)
			denaturants[i] = super.getDenaturants()[i];

		for (int i = 0; i < totalIterations; i++){
			String[] currentRun = super.getRuns1().get(i);
			TextFlowWriter.removeLast(super.output);
			TextFlowWriter.writeInfo("Calculating #"+(i+1)+
					" / "+totalIterations, super.output);
			try{
				if (!(currentRun[0].length() > 0))
					throw new ArrayIndexOutOfBoundsException();
			}catch(ArrayIndexOutOfBoundsException e){
				break;
			}
			//build the peptidecontainer for the dualsingletdataset
			DualSingletPeptideContainer pc = new DualSingletPeptideContainer(super.getRuns1().get(i), denaturants, offset);
			//build the first run from the peptide container
			DataRun r1 = new DataRun(pc.intensities1, super.getDenaturants());
			pc.calculatedValues1 = r1.call();

			DataRun r2 = new DataRun(pc.intensities2, super.getDenaturants());
			pc.calculatedValues2 = r2.call();

			super.runs1.set(i, pc.toStringArray());
			super.addChartable1(pc.toChartable1());
			super.addChartable2(pc.toChartable2());
			updateProgress(i, totalIterations);
		}
		TextFlowWriter.removeLast(super.output);
		return null;
	}

	/*Contains the peptide runs. Works as expected*/
	public class DualSingletPeptideContainer{
		private static final String EOF_STRING = "";

		public String peptide;
		public String accessionNumber;
		public int experiment;
		public double[] denaturants;
		
		/*Run 1*/
		public double isolationInterference1;
		public double rt1;
		public double[] intensities1;
		public SingleFit calculatedValues1;
		/*Run 2*/
		public double isolationInterference2;
		public double rt2;
		public double[] intensities2;
		public SingleFit calculatedValues2;

		public String[] theRest;


		public DualSingletPeptideContainer(String[] list, double[] denaturants, final int offset){
			
			this.denaturants = denaturants;
			int numberDenaturants = this.denaturants.length;
			this.intensities1 = new double[numberDenaturants];
			this.intensities2 = new double[numberDenaturants];

			this.peptide = list[0];
			this.accessionNumber = list[1];
			this.experiment = Integer.parseInt(list[2]);

			this.isolationInterference1 = Double.parseDouble(list[3]);
			this.rt1 = Double.parseDouble(list[4]);
			for (int i = 0; i < numberDenaturants; i++){
				this.intensities1[i] = Double.parseDouble(list[i+offset]);
			}

			final int secondOffset = numberDenaturants + offset;
			this.isolationInterference2 = Double.parseDouble(list[secondOffset]);
			this.rt2 = Double.parseDouble(list[secondOffset+1]);

			for(int i = 0; i < numberDenaturants; i++){
				this.intensities2[i] = Double.parseDouble(list[secondOffset+2+i]);
			}
			final int trailingOffset = secondOffset + 2 + numberDenaturants;
			final int delta = list.length - trailingOffset;
			theRest = new String[delta];
			for (int i = 0; i < list.length - trailingOffset; i++){
				theRest[i] = list[trailingOffset+i];
			}

		}

		public String[] toStringArray(){
			@SuppressWarnings("serial")
			List<String> list = new ArrayList<String>(){{
				add(peptide);
				add(accessionNumber);
				add(String.valueOf(experiment));
				add(String.valueOf(isolationInterference1));
				add(String.valueOf(rt1));
				for (double ele : intensities1){
					add(String.valueOf(ele));
				}
				for(double ele : calculatedValues1.array){
					add(String.valueOf(ele));
				}
				add(EOF_STRING);
				add(String.valueOf(isolationInterference2));
				add(String.valueOf(rt2));
				for (double ele : intensities2){
					add(String.valueOf(ele));
				}
				for(double ele : calculatedValues2.array){
					add(String.valueOf(ele));
				}
				add(EOF_STRING);
				for(String ele : theRest){
					add(ele);
				}
			}};
			String[] arr = new String[list.size()];
			for (int i = 0; i < arr.length; i ++){
				arr[i] = list.get(i);
			}
			return arr;

		}

		public Chartable toChartable1() {
			final double[] calculatedArray1 = this.calculatedValues1.array;
			final double chalf = calculatedArray1[0];
			final double chalfSD = calculatedArray1[1];
			final double b = calculatedArray1[2];
			final double bSD = calculatedArray1[3];
			final double adjrsq = calculatedArray1[4];
			return new Chartable(this.peptide, this.accessionNumber, 
					this.intensities1, this.denaturants, chalf, chalfSD ,b, bSD ,adjrsq, this.calculatedValues1.removedValue);
		}
		
		public Chartable toChartable2(){
			final double[] calculatedArray2 = this.calculatedValues2.array;
			final double chalf = calculatedArray2[0];
			final double chalfSD = calculatedArray2[1];
			final double b = calculatedArray2[2];
			final double bSD = calculatedArray2[3];
			final double adjrsq = calculatedArray2[4];
			return new Chartable(this.peptide, this.accessionNumber, 
					this.intensities2, this.denaturants, chalf, chalfSD ,b, bSD ,adjrsq, this.calculatedValues2.removedValue);
		}
	}
	public static void main(String[] args){
		DualSingletDataSet dsds = new DualSingletDataSet("/Users/jkarnuta/Desktop/Table_S1_revised_02.csv","/Users/jkarnuta/Desktop/manATags.csv",new TextFlow());
		dsds.load();
		dsds.digest();

	}

}
