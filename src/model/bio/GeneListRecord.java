package model.bio;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Callback;
import model.chart.DimensionRecord;
import model.stat.Range;
import services.bridgedb.BridgeDbIdMapper;
import services.bridgedb.MappingSource;
import util.FileUtil;
import util.StringUtil;

public class GeneListRecord extends TableRecord<Gene> {
	
	public GeneListRecord(String n)
	{
		super(n);
		geneList = new ArrayList<Gene>();
	}
	
	public GeneListRecord(File f)
	{
		this(f.getName());
		
		List<String> strs = FileUtil.readFileIntoStringList(f);
		int sz = strs.size();
		String DELIM = "\t";
		String firstRow = strs.get(0);
		int nCols = firstRow.split(DELIM).length;
		for (int i = 1; i< sz; i++)
		{
			String row = strs.get(i);
			String[] tokens = row.split(DELIM);
			geneList.add(new Gene(this, tokens[0]));
		}
	}
	
	public GeneListRecord(GeneListRecord parent)
	{
		this("Subset of " + parent.getName());
		type.set(parent.getType());
		species.set(parent.getSpecies());
		history.set(parent.getHistory());
		copyColumns(parent);
	}

	private void copyColumns(GeneListRecord parent)
	{
		List<TableColumn<Gene, ?>> parentColumns = parent.getAllColumns();
		for (TableColumn<Gene, ?> col : parentColumns)
		{
			TableColumn<Gene, ?> newColumn = new TableColumn(col.getText());
			newColumn.setEditable(col.isEditable());
			newColumn.setGraphic(col.getGraphic());
			newColumn.setVisible(col.isVisible());
			newColumn.setMinWidth(col.getMinWidth());
			newColumn.setMaxWidth(col.getMaxWidth());
//			newColumn.setCellFactory(col.getCellFactory());
//			newColumn.setCellValueFactory(col.getCellValueFactory());
			allColumns.add(newColumn);
		}

		
	}

	DoubleProperty score = new SimpleDoubleProperty(0);
	DoubleProperty size = new SimpleDoubleProperty(0);
	StringProperty comments = new SimpleStringProperty();
	StringProperty history = new SimpleStringProperty();
	StringProperty species = new SimpleStringProperty();
	StringProperty tissue = new SimpleStringProperty();

	public DoubleProperty  scoreProperty()  { return score;}
	public Double getScore()  { return score.get();}
	public void setScore(Double s)  { score.set(s);}

	public StringProperty  historyProperty()  { return history;}
	public String getHistory()  { return history.get();}
	public void setHistory(String s)  { history.set(s);}

	public StringProperty  speciesProperty()  { return species;}
	public String getSpecies()  { return species.get();}
	public void setSpecies(String s)  { species.set(s);}

	private List<Gene> geneList ;		// observableList created in Doc.readCDT or GPML.readGeneList
	public void setGeneList(List<Gene> g) {  geneList = g; 	}
	public List<Gene>  getGeneList() {	return geneList; }

	Map<String, DimensionRecord> dimensions = new HashMap<String, DimensionRecord>();
	public VBox buildHypercube(List<String> headers)
	{
		VBox vbox = new VBox(12);
		try
		{
			int nCols = headers.size();
			for (int col = 0; col < nCols; col++)
			{
				String title = headers.get(col);
				int index = getValueIndex(title);
				if (index < 0) continue;
				List<Double> vals = new ArrayList<Double>();
				for (Gene g : getGeneList())
					vals.add(new Double(g.getValue(index-8)));		// TODO
				DimensionRecord rec = new DimensionRecord(title, vals);
				dimensions.put(title, rec);
				rec.build1DChart();
	//				vbox.getChildren().add(rec.getChart());
			}
			for (int col = 0; col < nCols; col += 2)
			{
				String xDim = headers.get(col);
				String yDim = headers.get(col+1);
				DimensionRecord xRec = dimensions.get(xDim);
				DimensionRecord yRec = dimensions.get(yDim);
				if (xRec != null && yRec != null)
				{
					LineChart<Number, Number> x1D = xRec.getChart();
					LineChart<Number, Number> y1D = yRec.getChart();
					ScatterChart<Number, Number> xy2D = buildScatterChart(xRec, yRec);
					HBox conglom = new HBox(xy2D, new VBox(x1D, y1D));
					vbox.getChildren().add(conglom);
				}
	//				break;  //  when debugging, quit after first 2D chart
			}
		}
		catch (Exception ex) 	{ ex.printStackTrace();  return null;	}
		return vbox;
	}

	private ScatterChart<Number, Number> buildScatterChart(DimensionRecord xRec, DimensionRecord yRec) {
		final NumberAxis xAxis = new NumberAxis();
		Range xRange = xRec.getRange();
		xAxis.setLowerBound(xRange.min);
		xAxis.setUpperBound(xRange.max);
		xAxis.setLabel(xRec.getTitle());
		final NumberAxis yAxis = new NumberAxis();
		Range yRange = yRec.getRange();
		yAxis.setLowerBound(yRange.min);
		yAxis.setUpperBound(yRange.max);
		yAxis.setLabel(yRec.getTitle());

		ScatterChart<Number, Number>	scatter = new ScatterChart<Number, Number>(xAxis, yAxis);
		scatter.setTitle(xRec.getTitle() + " x " + yRec.getTitle());
		XYChart.Series<Number, Number> dataSeries = new XYChart.Series<Number, Number>();
		scatter.getStyleClass().add("custom-chart");
		dataSeries.setName("Genes");
		int sz = Math.min(xRec.getNValues(), yRec.getNValues());
		for (int i=0; i< sz; i++)
		{
			double x = xRec.getValue(i);
			double y = yRec.getValue(i);
			if (Double.isNaN(x) || Double.isNaN(y)) continue;
			XYChart.Data<Number, Number> data = new XYChart.Data<Number, Number>(x, y);
			Rectangle r = new Rectangle(2,2);
			r.setFill(i<2000 ? Color.FIREBRICK : Color.YELLOW);
	        data.setNode(r);
	        
			dataSeries.getData().add(data);
		}
//			Shape circle = new Circle(1);
//			circle.setFill(Color.RED);
		dataSeries.setNode(new Rectangle(1,1));
		scatter.getData().addAll(dataSeries);
		return scatter;
	}
	public void setColumnList() {
		String header = headers.get(0);
		int skipColumns = 4;
		String[] fields = header.split("\t");
		for (int i=skipColumns; i<fields.length; i++)
		{
			String fld = fields[i];
			String format =  "%4.2f";
			TableColumn<Gene, Double> column = new TableColumn<Gene, Double>(fld);
			column.getProperties().put("Numeric", "TRUE");
			column.getProperties().put("Format", format);
			column.setCellValueFactory(new Callback<CellDataFeatures<Gene, Double>, ObservableValue<Double>>() {
			     public ObservableValue<Double> call(CellDataFeatures<Gene, Double> p) {
			         Gene gene = p.getValue();
			         double d = gene.getValueByName(fld);
			         return new ReadOnlyObjectWrapper(String.format(format, d));
			     }
			  });
			addColumn(column, fld);  //TODO
		}
	}
	static String TAB = "\t";
	static String NL = "\n";
	public static String BDB = "http://webservice.bridgedb.org/";
	public void fillIdlist()
	{
		Species spec = Species.lookup(species.get());
		if (spec == null) 
			spec = Species.Human;
		StringBuilder str = new StringBuilder();
		for (Gene g : geneList)
		{
			if (StringUtil.hasText(g.getIdlist())) continue;
			String name = g.getName();
			MappingSource sys = MappingSource.guessSource(spec, name);
			str.append(name + TAB + sys.system() + NL);
		}
		try
		{
			List<String> output = BridgeDbIdMapper.post(BDB, spec.common(), "xrefsBatch", "", str.toString());
			for (String line : output)
			{
				String [] flds = line.split("\t");
				String name = flds[0];
				String allrefs = flds[2];
				for (Gene g : geneList)
				{
					if (!g.getName().equals(name)) continue;
					System.out.println("setting ids for " + name );	
					g.setIdlist(allrefs);
					g.setEnsembl(BridgeDbIdMapper.getEnsembl(allrefs));
				}
			}
		}
		catch(Exception ex) 
		{ 
			System.err.println(ex.getMessage());	
		}
	}
	public int getRowCount()	{ return geneList.size();}

}
