import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.ApplicationFrame;


public class LineChart extends ApplicationFrame {

private static final long serialVersionUID = 1L;

public LineChart(String s,float[] prop_per_cycle_r,float[] prop_per_cycle_rp,int cycle_num) {
	super(s);
	setContentPane(createDemoLine(prop_per_cycle_r,prop_per_cycle_rp,cycle_num));
}

// update Panel
public void updateChart(float[] prop_per_cycle_r,float[] prop_per_cycle_rp,int cycle_num){
	setContentPane(createDemoLine(prop_per_cycle_r,prop_per_cycle_rp,cycle_num));
}

// create Panel
public static JPanel createDemoLine(float[] prop_per_cycle_r,float[] prop_per_cycle_rp,int cycle_num) {
	JFreeChart jfreechart = createChart(createDataset(prop_per_cycle_r,prop_per_cycle_rp,cycle_num));	
	return new ChartPanel(jfreechart);
}

// create JFreeChart
public static JFreeChart createChart(DefaultCategoryDataset linedataset) {	
	JFreeChart chart = ChartFactory.createLineChart("Conflict Miss/Total Miss", //name of chart
			"Cycle number", // name of x-axis
			"Proportion(%)", // name of y-axis
			linedataset, // dataset of chart
			PlotOrientation.VERTICAL, // show chart horizontally
			true, // include legend
			true, // tooltips
			false // urls
			);
	CategoryPlot plot = chart.getCategoryPlot();
	plot.setRangeGridlinesVisible(true); 
	plot.setBackgroundAlpha(0.3f); 
	
	ValueAxis valueaxis = plot.getRangeAxis();
	valueaxis.setRange(12.0D, 13.0D); //y-axis ranges from 12 to 13
	valueaxis.setAutoRange(false);

	return chart;
}
// create dataset for lineChart
public static DefaultCategoryDataset createDataset(float[] prop_per_cycle_r,float[] prop_per_cycle_rp,int cycle_num) {
	DefaultCategoryDataset linedataset = new DefaultCategoryDataset();
	// name of lines
	String series1 = "Random"; 
	String series2 = "Random with Priority"; 
	for(int i=0;i<cycle_num+1;i++){
		linedataset.addValue(prop_per_cycle_r[i], series1, String.valueOf(i));
		linedataset.addValue(prop_per_cycle_rp[i], series2, String.valueOf(i));
	}
	return linedataset;
	}
}
