package semgen.utilities.file;

import java.awt.Dimension;
import java.io.File;
import java.util.HashMap;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;


public abstract class SemGenFileChooser extends JFileChooser {
	private static final long serialVersionUID = 1L;
	public static final FileNameExtensionFilter owlfilter = new FileNameExtensionFilter("SemSim (*.owl)", "owl");
	public static final FileNameExtensionFilter cellmlfilter = new FileNameExtensionFilter("CellML (*.cellml, *.xml)", "cellml", "xml");
	public static final FileNameExtensionFilter sbmlfilter = new FileNameExtensionFilter("SBML (*.sbml, *.xml)", "sbml", "xml");
	public static final FileNameExtensionFilter mmlfilter = new FileNameExtensionFilter("MML (*.mod)", "mod");
	public static final FileNameExtensionFilter projfilter = new FileNameExtensionFilter("JSim project file model (*.proj)", "proj");
	public static final FileNameExtensionFilter csvfilter = new FileNameExtensionFilter("CSV (*.csv)", "csv");
	protected FileFilter fileextensions = new FileFilter(new String[]{"owl", "xml", "sbml", "cellml", "mod", "proj"});
	
	private static HashMap<String, FileNameExtensionFilter> filtermap = new HashMap<String, FileNameExtensionFilter>(); 
	
	Dimension filechooserdims = new Dimension(550,550);
	public static File currentdirectory;
	int modeltype = -1;
	
	public SemGenFileChooser(String title) {
		super(currentdirectory);
		setDialogTitle(title);
		createMap();
	}
	
	public SemGenFileChooser(String title, String[] filters) {
		super(currentdirectory);
		setDialogTitle(title);
		addFilters(filters);
		createMap();
	}
	
	private void createMap() {
		filtermap.put("owl", owlfilter);
		filtermap.put("cellml", cellmlfilter);
		filtermap.put("sbml", sbmlfilter);
		filtermap.put("mod", mmlfilter);
		filtermap.put("proj", projfilter);
		filtermap.put("csv", csvfilter);
	}
	
	protected FileNameExtensionFilter getFilter(String key) {
		return filtermap.get(key);
	}
	
	protected FileNameExtensionFilter[] getFilter(String[] keys) {
		FileNameExtensionFilter[] filters = new FileNameExtensionFilter[keys.length];
		int i = 0;
		for (String key : keys) {
			filters[i] = filtermap.get(key);
			i++;
		}
		return filters;
	}
	
	public void addFilters(String[] filters) {
		for(String filter : filters) addChoosableFileFilter(getFilter(filter));
	}
	
	public void addFilters(FileNameExtensionFilter[] filters) {
		for(FileNameExtensionFilter filter : filters) addChoosableFileFilter(filter);
	}
	
	public int getFileType() {
		return modeltype;
	}
	
}
