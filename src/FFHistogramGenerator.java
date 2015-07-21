import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.concurrent.Task;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;


public class FFHistogramGenerator extends Task<FFError> {

	private final List<Double> histogramPoints;
	private final String title;
	private final String filepath;
	public FFHistogramGenerator(List<Double> charts, String title, String filepath){
		this.histogramPoints = charts;
		this.title = title;
		this.filepath = filepath;
		
	}

	@Override
	protected FFError call(){
		return generate();
	}
	
	private FFError generate(){
		
		//clean up dataset
		List<Double> tempDataset = new ArrayList<Double>();
		for (Double ele : histogramPoints){
			if(ele < 10 && ele > 0) tempDataset.add(ele);
		}
		
		//Make dataset
		HistogramDataset hd = new HistogramDataset();
		double[] hists = new double[tempDataset.size()];
		for (int i = 0; i < hists.length; i ++){
			hists[i] = tempDataset.get(i);
		}
		
		hd.addSeries("Values", hists, 100);
		
		//make chart
		JFreeChart chart = ChartFactory.createHistogram(this.title, "Values", "Count", hd
				, PlotOrientation.VERTICAL, false, true, false);
		
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
