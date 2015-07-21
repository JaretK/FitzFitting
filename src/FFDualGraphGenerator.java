import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.io.File;
import java.io.IOException;
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
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.TextAnchor;
import org.jfree.util.ShapeUtilities;


public class FFDualGraphGenerator extends Task<ArrayList<GraphStatus>> {

	/*Overhead*/
	private final String dirPath;
	private final TextFlow output;
	private final int offset;
	private final Double[] denaturants;
	private final String xAxisLabel;

	/*Run 1*/
	private final List<Chartable> runs1;

	/*Run 2*/
	private final List<Chartable> runs2;


	public FFDualGraphGenerator(List<Chartable> chartsList1, List<Chartable> chartsList2 
			,Double[] denaturants, String directoryPath, int offset ,TextFlow tf){

		this.runs1 = chartsList1;
		this.runs2 = chartsList2;
		this.denaturants = denaturants;
		this.dirPath = directoryPath; //need to split to get enclosing directory
		this.output = tf; //used to alert user to current graph progress
		this.offset = offset;

		this.xAxisLabel = "Denaturant Concentration (M)";
	}


	@Override
	protected ArrayList<GraphStatus> call() {		
		return generate(new ArrayList<GraphStatus>());
	}

	private ArrayList<GraphStatus> generate(ArrayList<GraphStatus> errorList){

		/*Initialize Constants*/
		int currentChartNumber = offset+2;
		ArrayList<GraphStatus> returnList = new ArrayList<GraphStatus>();
		DecimalFormat truncation = new DecimalFormat("#.###");

		/*Create Directory*/
		new File(this.dirPath).mkdirs();

		TextFlowWriter.writeInfo("Drawing graphs to "+this.dirPath+File.separator, this.output);
		TextFlowWriter.writeInfo("", this.output);//needed for proper usage of TextFlowWriter.removeLast

		//for each chartable in both runs
		for (int i = 0; i < this.runs1.size(); i ++){
			Chartable control = this.runs1.get(i);
			Chartable ligand = this.runs2.get(i);

			/*Init Constants*/
			control.setGraphNumber(currentChartNumber);
			ligand.setGraphNumber(currentChartNumber);
			String chartTitle = control.peptide+" ("+control.protein+")";
			TextFlowWriter.removeLast(this.output);

			/*Update progress*/
			updateProgress(i, this.runs1.size());

			/*Make sure both peptides are the same*/
			if (!control.peptide.equals(ligand.peptide)){
				TextFlowWriter.writeError("Datasets do not match", this.output);
				TextFlowWriter.writeError("Trying to draw "+control.peptide
						+ "and "+ligand.peptide, this.output);
				TextFlowWriter.writeLine("", this.output);
				continue;
			}

			/*Alert User*/
			TextFlowWriter.writeInfo("Drawing #"+currentChartNumber+" / "+this.runs1.size(),
					this.output);

			/**
			 * Set up Graph
			 */
			double rangeMax = Math.max(FFMath.max(control.intensities),
					FFMath.max(ligand.intensities))+0.03;
			double rangeMin = Math.min(FFMath.min(control.intensities), 
					FFMath.min(ligand.intensities))-0.1;

			/**
			 * Set up Control Graph
			 */

			/*Set up Control plot*/
			XYPlot controlGraph = generatePlot(control);
			NumberAxis controlAxis = new NumberAxis("Control Intensities");
			controlAxis.setTickUnit(new NumberTickUnit(0.1));
			controlAxis.setAutoRangeIncludesZero(false);
			controlAxis.setUpperBound(rangeMax);
			controlAxis.setLowerBound(rangeMin);
			controlGraph.setRangeAxis(controlAxis);

			/* Set up Annotations*/
			String truncatedRSquared = truncation.format(control.adjRSquared);
			String truncatedCHalf = truncation.format(control.chalf);
			String RsqString =  "Adjusted R Squared: "+truncatedRSquared;
			String chalfString = "C 1/2: "+truncatedCHalf;
			String removedPoint = "Excluded Index: "+control.indexRemoved;
			String combinedString = RsqString+"\n"+chalfString+"\n"+removedPoint;
			XYTextAnnotation controlAnnotations = new XYTextAnnotation(combinedString, 0,0);
			controlAnnotations.setPaint(Color.BLUE);
			controlAnnotations.setFont(new Font("expressway.ttf", Font.PLAIN, 12));
			controlAnnotations.setTextAnchor(TextAnchor.BOTTOM_LEFT);


			/**
			 * Set up Ligand Graph
			 */
			/*Make Ligand plot*/
			XYPlot ligandGraph = generatePlot(ligand);
			NumberAxis ligandAxis = new NumberAxis("Ligand Intensities");
			ligandAxis.setTickUnit(new NumberTickUnit(0.1));
			ligandAxis.setAutoRangeIncludesZero(false);
			ligandAxis.setUpperBound(rangeMax);
			ligandAxis.setLowerBound(rangeMin);
			ligandGraph.setRangeAxis(ligandAxis);

			/* Set up Annotations*/
			truncatedRSquared = truncation.format(ligand.adjRSquared);
			truncatedCHalf = truncation.format(ligand.chalf);
			RsqString =  "Adjusted R Squared: "+truncatedRSquared;
			chalfString = "C 1/2: "+truncatedCHalf;
			removedPoint = "Excluded Index: "+ligand.indexRemoved;
			combinedString = RsqString+"\n"+chalfString+"\n"+removedPoint;
			XYTextAnnotation ligandAnnotations = new XYTextAnnotation(combinedString, 0,0);
			ligandAnnotations.setPaint(Color.BLUE);
			ligandAnnotations.setFont(new Font("expressway.ttf", Font.PLAIN, 12));
			ligandAnnotations.setTextAnchor(TextAnchor.BOTTOM_LEFT);

			/*Add custom legend*/
			LegendItem chalfLegend = new LegendItem("C 1/2 Marker", "","","",
					new Line2D.Double(0,5,10,15), dashedLineStroke(), Color.GREEN);
			LegendItemCollection newLegend = ligandGraph.getLegendItems();
			newLegend.add(chalfLegend);
			ligandGraph.setFixedLegendItems(newLegend);

			/*Set up Domain Axis*/
			NumberAxis domainAxis = new NumberAxis(xAxisLabel);
			domainAxis.setVerticalTickLabels(true);
			domainAxis.setTickUnit(new NumberTickUnit(0.2));
			domainAxis.setLowerMargin(0.1);
			domainAxis.setUpperMargin(0.1);
			domainAxis.setAutoRangeIncludesZero(false);


			/*Set up main plot*/
			CombinedDomainXYPlot mainPlot = new CombinedDomainXYPlot(domainAxis);
			mainPlot.add(controlGraph);
			mainPlot.add(ligandGraph);

			/*Set control annotations*/
			controlAnnotations.setX(mainPlot.getDomainAxis().getLowerBound());
			controlAnnotations.setY(rangeMin);
			controlGraph.addAnnotation(controlAnnotations);

			/*Set ligand annotations*/
			ligandAnnotations.setX(mainPlot.getDomainAxis().getLowerBound());
			ligandAnnotations.setY(rangeMin);
			ligandGraph.addAnnotation(ligandAnnotations);

			JFreeChart localChart = new JFreeChart(chartTitle, mainPlot);
			localChart.setBackgroundPaint(Color.WHITE);


			try {
				File PNGFile = new File(this.dirPath + File.separator+"Image "+currentChartNumber+".png");
				ChartUtilities.saveChartAsPNG(PNGFile, localChart, 500, 800);
				errorList.add(new GraphStatus(currentChartNumber, FFError.NoError));
			} catch (IOException e) {
				errorList.add(new GraphStatus(currentChartNumber, FFError.GraphGenerationError));
				TextFlowWriter.writeError(e.getMessage(), this.output);
			}
			currentChartNumber++;
		}

		return errorList;
	}



	private XYPlot generatePlot(Chartable chartable){		
		/* Set up Scatter*/
		XYSeries xyScatter = getXYData(chartable);
		XYDataset scatterDataset = new XYSeriesCollection(xyScatter);
		XYItemRenderer scatterRenderer = getPointRenderer(chartable);
		int scatterIndex = 0;

		/* Set up curve */
		XYSeries xyCurve = getCurveData(chartable);
		XYDataset curveDataset = new XYSeriesCollection(xyCurve);
		XYItemRenderer curveRenderer = getCurveRenderer();
		int curveIndex = 1;

		/* Set up C 1/2 Value Marker*/
		ValueMarker chalfMarker = new ValueMarker(chartable.chalf);
		chalfMarker.setPaint(Color.GREEN);
		chalfMarker.setStroke(
				dashedLineStroke());


		XYPlot plt = new XYPlot();			

		/*Add datasets*/
		plt.setDataset(scatterIndex, scatterDataset);
		plt.setRenderer(scatterIndex, scatterRenderer);
		plt.setDataset(curveIndex, curveDataset);
		plt.setRenderer(curveIndex, curveRenderer);
		/*Add / update markers and Annotations*/
		plt.addDomainMarker(chalfMarker);
		return plt;
	}

	private XYItemRenderer getPointRenderer(Chartable chartable){
		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(false, true){
			@Override
			public Shape getItemShape(int row, int col){
				if( row == 0 & col == chartable.indexRemoved){
					return ShapeUtilities.createDiagonalCross(4, 1);
				}
				else{
					return super.getItemShape(row, col);
				}
			}
		};
		renderer.setBaseFillPaint(Color.RED);
		return renderer;
	}

	private XYItemRenderer getCurveRenderer(){
		XYItemRenderer curveRenderer = new XYLineAndShapeRenderer(true, false);
		curveRenderer.setBasePaint(Color.BLUE);
		return curveRenderer;
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
