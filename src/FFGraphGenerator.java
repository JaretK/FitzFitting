import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Line2D;
import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javafx.concurrent.Task;
import javafx.scene.text.TextFlow;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.TextAnchor;



/**
 * Class that generates and saves graphs
 * 
 * @author jkarnuta
 *
 */
public class FFGraphGenerator extends Task<ArrayList<GraphStatus>> {

	private final List<Chartable> runs;
	private final Double[] denaturants;
	private final String dirPath;
	private final TextFlow output;
	private final int offset;

	public FFGraphGenerator(List<Chartable> chartsList, Double[] denaturants, String directoryPath, int offset ,TextFlow tf){
		this.runs = chartsList;
		this.denaturants = denaturants;
		this.dirPath = directoryPath; //need to split to get enclosing directory
		this.output = tf; //used to alert user to current graph progress
		this.offset = offset;
	}

	public ArrayList<GraphStatus> call(){
		return generate(new ArrayList<GraphStatus>());
	}

	public ArrayList<GraphStatus> generate(ArrayList<GraphStatus> errorList){

		/*Initialize Constants*/
		int currentChartNumber = offset+2;
		int numberIterations =this.runs.size()-currentChartNumber; 
		DecimalFormat truncation = new DecimalFormat("#.###");
		truncation.setRoundingMode(RoundingMode.FLOOR);

		/*Create directory*/
		new File(this.dirPath).mkdirs();//create the directory at the specified location with the correct name

		TextFlowWriter.writeInfo("Drawing graphs to "+this.dirPath+File.separator, this.output);
		TextFlowWriter.writeInfo("", this.output);//needed for proper usage of TextFlowWriter.removeLast
		/*Generate Graphs*/
		for (Chartable chartable : this.runs){
			/*Alert User */
			TextFlowWriter.removeLast(this.output);
			TextFlowWriter.writeInfo("Drawing #"+currentChartNumber+" / "+this.runs.size(), this.output);

			/*Set up Constants*/
			chartable.setGraphNumber(currentChartNumber);
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
			domainAxis.setTickUnit(new NumberTickUnit(0.2));
			domainAxis.setLowerMargin(0.1);
			domainAxis.setUpperMargin(0.1);
			domainAxis.setAutoRangeIncludesZero(false);
			NumberAxis rangeAxis = new NumberAxis(yAxisLabel);
			rangeAxis.setTickUnit(new NumberTickUnit(0.1));
			rangeAxis.setAutoRangeIncludesZero(false);
			rangeAxis.setUpperBound(FFMath.max(chartable.intensities)+0.03);
			rangeAxis.setLowerBound(FFMath.min(chartable.intensities)-0.03);

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
				File PNGFile = new File(this.dirPath + File.separator+"Image "+currentChartNumber+".png");
				ChartUtilities.saveChartAsPNG(PNGFile, chart, 500, 400);
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
}
