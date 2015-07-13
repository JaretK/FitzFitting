import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Line2D;
import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javafx.concurrent.Task;
import javafx.scene.text.TextFlow;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;



/**
 * Class that generates and saves graphs
 * 
 * @author jkarnuta
 *
 */
public class FFGraphGenerator extends Task<ArrayList<GraphStatus>> {

	private final ArrayList<String[]> runs;
	private final Double[] denaturants;
	private final String dirPath;
	private final TextFlow output;

	public FFGraphGenerator(ArrayList<String[]> runs, Double[] denaturants, String directoryPath, TextFlow tf){
		this.runs = runs;
		this.denaturants = denaturants;
		this.dirPath = directoryPath; //need to split to get enclosing directory
		this.output = tf; //used to alert user to current graph progress
	}

	public ArrayList<GraphStatus> call(){
		return generate(new ArrayList<GraphStatus>());
	}

	public ArrayList<GraphStatus> generate(ArrayList<GraphStatus> errorList){

		/*Initialize Constants*/
		int currentChartNumber = 2;
		int numberIntensities = this.denaturants.length;
		int numberIterations =this.runs.size()-currentChartNumber; 
		DecimalFormat truncation = new DecimalFormat("#.###");
		truncation.setRoundingMode(RoundingMode.FLOOR);

		/*Create directory*/

		//split dirPath to get enclosing directory
		String[] directoryLocationArray = this.dirPath.split(File.separator);
		StringBuilder directory = new StringBuilder();
		for (int i = 0; i < directoryLocationArray.length-1; i++) 
			directory.append(directoryLocationArray[i]+File.separator);

		//add filename (omit .csv)
		directory.append(directoryLocationArray[directoryLocationArray.length-1].split("\\.")[0]);
		String directoryPath = directory.toString();
		new File(directoryPath).mkdirs();//create the directory at the specified location with the correct name

		TextFlowWriter.writeInfo("Drawing graphs to "+directoryPath+File.separator, this.output);
		TextFlowWriter.writeInfo("", this.output);//needed for proper usage of TextFlowWriter.removeLast
		/*Generate Graphs*/
		for (String[] ele : this.runs){
			/*Alert User */
			TextFlowWriter.removeLast(this.output);
			TextFlowWriter.writeInfo("Drawing #"+currentChartNumber+" / "+this.runs.size(), this.output);

			/*Set up Constants*/
			Chartable chartable = new Chartable(ele, numberIntensities);
			String chartTitle = chartable.peptide+" ("+chartable.protein+")";
			String xAxisLabel = "Denaturant Concentration (M)";
			String yAxisLabel = "Normalized Intensities";

			/* Set up Scatter*/
			XYSeries xyScatter = getXYData(chartable);
			XYDataset scatterDataset = new XYSeriesCollection(xyScatter);
			XYItemRenderer scatterRenderer = new XYLineAndShapeRenderer(false, true);
			int scatterIndex = 0;

			/* Set up curve */
			XYSeries xyCurve = getCurveData(chartable);
			XYDataset curveDataset = new XYSeriesCollection(xyCurve);
			XYItemRenderer curveRenderer = new XYLineAndShapeRenderer(true, false);
			int curveIndex = 1;

			/* Set up C 1/2 Value Marker*/
			ValueMarker chalfMarker = new ValueMarker(chartable.chalf);
			chalfMarker.setPaint(Color.GREEN);
			chalfMarker.setStroke(
					dashedLineStroke());

			/* Set up AdjRSquared Marker*/
			String truncatedRSquared = truncation.format(chartable.adjRSquared);
			final XYTextAnnotation adjRSq = new XYTextAnnotation("Adjusted R Squared: "+truncatedRSquared, 0,0);
			adjRSq.setPaint(Color.RED);
			adjRSq.setFont(new Font("expressway.ttf", Font.PLAIN, 12));
			adjRSq.setTextAnchor(TextAnchor.BOTTOM_LEFT);

			/*Set up Axis*/
			NumberAxis domainAxis = new NumberAxis(xAxisLabel);
			domainAxis.setVerticalTickLabels(true);
			domainAxis.setTickUnit(new NumberTickUnit(0.1));
			domainAxis.setLowerMargin(0.1);
			domainAxis.setUpperMargin(0.1);
			domainAxis.setAutoRangeIncludesZero(false);
			NumberAxis rangeAxis = new NumberAxis(yAxisLabel);
			rangeAxis.setAutoRangeIncludesZero(false);

			XYPlot plt = new XYPlot();
			/*Set Axes*/
			plt.setDomainAxis(domainAxis);
			plt.setRangeAxis(rangeAxis);
			/*Add datasets*/
			plt.setDataset(scatterIndex, scatterDataset);
			plt.setRenderer(scatterIndex, scatterRenderer);
			plt.setDataset(curveIndex, curveDataset);
			plt.setRenderer(curveIndex, curveRenderer);
			/*Add / update markers and Annotations*/
			plt.addDomainMarker(chalfMarker);
			adjRSq.setX(plt.getDomainAxis().getLowerBound());
			adjRSq.setY(plt.getRangeAxis().getLowerBound());
			plt.addAnnotation(adjRSq);

			LegendItem chalfLegend = new LegendItem("C 1/2 Marker", "","","",
					new Line2D.Double(0,5,10,15), dashedLineStroke(), Color.GREEN);
			LegendItemCollection newLegend = plt.getLegendItems();
			newLegend.add(chalfLegend);
			plt.setFixedLegendItems(newLegend);

			JFreeChart chart = new JFreeChart(chartTitle, plt);

			try {
				File PNGFile = new File(directoryPath + File.separator+"Image "+currentChartNumber+".png");
				ChartUtilities.saveChartAsPNG(PNGFile, chart, 1000, 500);
				errorList.add(new GraphStatus(currentChartNumber, FFError.NoError));
			} catch (IOException e) {
				errorList.add(new GraphStatus(currentChartNumber, FFError.GraphGenerationError));
				e.printStackTrace();
			}
			updateProgress(currentChartNumber,numberIterations);
			currentChartNumber++;
		}
		TextFlowWriter.removeLast(this.output);
		return errorList;
	}

	private XYSeries getCurveData(Chartable chartable){
		CHalfFunction function = new CHalfFunction();
		XYSeries xys = new XYSeries("Sigmoidal fit");
		final double[] xDataPoints = smoothInterval(1000);
		function.setA(chartable.A);
		function.setB(chartable.B);
		for (int i = 0; i < xDataPoints.length; i++){
			double xValue = xDataPoints[i];
			double yValue = function.calculateYValue(chartable.chalf, chartable.b, xValue);
			xys.add(new XYDataItem(xValue, yValue));
		}
		return xys;
	}

	private double[] smoothInterval(int numberSplices){
		final double min = FFMath.min(this.denaturants);
		final double max = FFMath.max(this.denaturants);

		final double range = max-min;
		final double step = range/numberSplices;

		double[] steppedArray = new double[numberSplices];

		//iteration method throws npe if 0 not defined
		steppedArray[0] = min;
		for (int i = 1; i < numberSplices; i++){
			steppedArray[i] = steppedArray[i-1] + step;
		}
		return steppedArray;
	}

	private XYSeries getXYData(Chartable chartable){
		final XYSeries xys = new XYSeries("Intensites");
		for (int i = 0; i < chartable.intensities.length; i++){
			xys.add(new XYDataItem((double)this.denaturants[i], (double)chartable.intensities[i]));
		}
		return xys;
	}

	private BasicStroke dashedLineStroke(){
		return new BasicStroke(
				2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
				1.0f, new float[] {6.0f, 6.0f}, 0.0f
				);
	}

	private class Chartable{
		public String peptide;
		public String protein;
		public double[] intensities;
		public double A;
		public double B;
		public double chalf;
		public double b;
		public double adjRSquared;
		public Chartable(String[] run, int numIntensities){
			peptide = run[0];
			protein = run[1];
			//2=intsum, 3=rt(min)

			//populate intensities
			intensities = new double[numIntensities];
			int offset = 4;
			for(int i = 0; i < numIntensities; i++){
				intensities[i] = Double.parseDouble(run[i+offset]);
			}

			//chalf is fifth from last
			chalf = Double.parseDouble(run[run.length-5]);

			//b is third from last
			b = Double.parseDouble(run[run.length-3]);

			//adjusted R Squared is the last value
			adjRSquared = Double.parseDouble(run[run.length-1]);

			//A is calculated from DataRun
			//B is calculated from DataRun
			/*
			 * determine if curve is oxidized or not based on heuristics
			 * assume to be non-oxidized if first two points average to be greater than 1.0
			 */
			boolean nonOx =  (intensities[0] + intensities[1])/2 > 1.0;
			if (nonOx){
				A = FFMath.max(intensities);
				B = FFMath.min(intensities);
			}
			else{
				A = FFMath.min(intensities);
				B = FFMath.max(intensities);
			}
		}
		public String toString(){
			StringBuilder sb = new StringBuilder();
			sb.append("Chartable object:\n");
			sb.append("peptide (protein): "+peptide+" ("+protein+")\n");
			sb.append("C 1/2: "+chalf+"\n");
			sb.append("b: "+b+"\n");
			sb.append("A: "+A+"\n");
			sb.append("B: "+B+"\n");
			sb.append("Adjusted R Squared: "+adjRSquared+"\n");
			return sb.toString();
		}
	}
}
