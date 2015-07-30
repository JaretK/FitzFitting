package graph_generators;

import java.awt.Color;
import java.awt.Paint;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.concurrent.Task;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.statistics.HistogramDataset;

import containers.*;
import statics.*;

public class FFHistogramGenerator extends Task<FFError> {

	private final List<Double> histogramPoints;
	private final String title;
	private final String filepath;
	private static final int NUMBER_BINS = 100;
	private final double cutoff;
	private final double LOWER_BOUND; 
	
	public FFHistogramGenerator(List<Double> charts, String title, String filepath, 
			double cutoff, boolean negativesAllowed){
		this.histogramPoints = charts;
		this.title = title;
		this.filepath = filepath;
		this.cutoff = cutoff;
		this.LOWER_BOUND = negativesAllowed? FFConstants.CLEAN_LOWER_BOUND : 0.0d;
	}

	@Override
	public FFError call(){
		return generate();
	}

	@SuppressWarnings("serial")
	private FFError generate(){

		//clean up dataset
		List<Double> tempDataset = new ArrayList<Double>();
		for (Double ele : histogramPoints){
			if(ele < FFConstants.CLEAN_UPPER_BOUND 
					&& ele > LOWER_BOUND){ 
				tempDataset.add(ele);
			}
		}

		//Make dataset
		HistogramDataset hd = new HistogramDataset();
		double[] hists = new double[tempDataset.size()];
		for (int i = 0; i < hists.length; i ++){
			hists[i] = tempDataset.get(i);
		}
		
		//add dataset to histogram set
		hd.addSeries("Values", hists, NUMBER_BINS);

		//make chart
		JFreeChart chart = ChartFactory.createHistogram(this.title, "Values", "Count", hd
				, PlotOrientation.VERTICAL, false, true, false);
		
		double xMax = chart.getXYPlot().getDomainAxis().getUpperBound();
		double xMin = chart.getXYPlot().getDomainAxis().getLowerBound();
		double xStep = (xMax - xMin)/NUMBER_BINS;
		XYItemRenderer renderer = chart.getXYPlot().getRenderer();
		renderer = new XYBarRenderer(){
			@Override 
			public Paint getItemPaint(final int row, final int column){
				if( (xStep * column) > cutoff) return Color.BLUE;
				else return Color.RED;
			}
		};
		
		chart.getXYPlot().setRenderer(renderer);
		try{
			File PNGFile = new File(this.filepath+File.separator+this.title+".png");
			ChartUtilities.saveChartAsPNG(PNGFile, chart, 400, 400);
			return FFError.NoError;
		}
		catch(IOException e){
			e.printStackTrace();
			return FFError.GraphGenerationError;
		}

	}

}
