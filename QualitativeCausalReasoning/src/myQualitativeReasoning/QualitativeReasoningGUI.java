package myQualitativeReasoning;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jdom.JDOMException;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;

import myQualitativeReasoning.QualitativeReasoningWorkbench.BatonVal;
import semsim.fileaccessors.FileAccessorFactory;
import semsim.fileaccessors.ModelAccessor;
import semsim.model.collection.SemSimModel;
import semsim.reading.SemSimOWLreader;

public class QualitativeReasoningGUI implements ActionListener {

	public JFrame frame;
	private static final int width = 800;
	private static final int height = 600;
	
	private JPanel loadPanel;
	private JButton loadModelButton;
	private JButton loadClassifiedModelButton;
	private JLabel loadLabel;
	private JPanel comboboxpanel;
	private JComboBox<String> updownbox;
	private JComboBox<String> propertybox;
	private JButton analyzecausalitystartbutton;
	private JButton analyzecausalityendbutton;
	private JPanel scrollerpanel;
	private ResultsScroller upscroller;
	private ResultsScroller downscroller;
	private ResultsScroller ambiguousscroller;
	private QualitativeReasoningWorkbench wb;
	
	private String modelnamespace;
	private File mergedontdirectory = new File("/Users/max_neal/Documents/workspaceLUNA/UppyDownyReasoning/mergedOnts");
	private File sourcemoddirectory = new File("/Users/max_neal/Documents/workspaceLUNA/UppyDownyReasoning/sourceModels");
	
	public static enum AnalysisType {DOWNSTREAM, UPSTREAM};

	
	public QualitativeReasoningGUI(){
		
		wb = new QualitativeReasoningWorkbench();
		
		frame = new JFrame();
		frame.setTitle("SemSim qualitative inferencing");
		frame.setVisible(false);

		frame.setPreferredSize(new Dimension(width, height));
		frame.getContentPane().setLayout(new BorderLayout());
		
		loadPanel = new JPanel();
		loadPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
		loadPanel.setLayout(new BorderLayout());
		JPanel loadPanelButtonPanel = new JPanel();
		loadModelButton = new JButton("Load model");
		loadModelButton.addActionListener(this);
		loadPanelButtonPanel.add(loadModelButton);
		loadClassifiedModelButton = new JButton("Load classified model");
		loadClassifiedModelButton.addActionListener(this);
		loadPanelButtonPanel.add(loadClassifiedModelButton);
		loadLabel = new JLabel("");
		loadLabel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));

		loadLabel.setForeground(Color.blue);

		loadPanel.add(loadPanelButtonPanel, BorderLayout.NORTH);
		loadPanel.add(loadLabel, BorderLayout.CENTER);
		loadPanel.add(Box.createGlue(), BorderLayout.EAST);
		
		comboboxpanel = new JPanel();
		comboboxpanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

		updownbox = new JComboBox<String>();
		updownbox.addItem("increase in");
		updownbox.addItem("decrease in");
		
		propertybox = new JComboBox<String>();
		propertybox.addItem("[ no model loaded ]");
		
		analyzecausalitystartbutton = new JButton("Get downstream effects");
		analyzecausalitystartbutton.addActionListener(this);
		
		analyzecausalityendbutton = new JButton("Get upstream causes");
		analyzecausalityendbutton.addActionListener(this);
		
		comboboxpanel.add(updownbox);
		comboboxpanel.add(propertybox);
		comboboxpanel.add(analyzecausalitystartbutton);
		comboboxpanel.add(analyzecausalityendbutton);
				
		scrollerpanel = new JPanel();
		scrollerpanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
		scrollerpanel.setLayout(new BoxLayout(scrollerpanel, BoxLayout.X_AXIS));
		
		upscroller = new ResultsScroller("Increased");
		downscroller = new ResultsScroller("Decreased");
		ambiguousscroller = new ResultsScroller("Ambiguous");
		
		
		scrollerpanel.add(upscroller);
		scrollerpanel.add(downscroller);
		scrollerpanel.add(ambiguousscroller);
		scrollerpanel.add(Box.createGlue());
		
		frame.add(loadPanel, BorderLayout.NORTH);
		frame.add(comboboxpanel, BorderLayout.CENTER);
		frame.add(scrollerpanel, BorderLayout.SOUTH);

		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		
		if(o == loadClassifiedModelButton){
			JFileChooser fc = new JFileChooser(mergedontdirectory);
			fc.setPreferredSize(new Dimension(500,500));
			
			if(fc.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION){
				File file = fc.getSelectedFile();
				
				try {
					wb.inferredAxiomsOntology = QualitativeReasoningWorkbench.manager.loadOntologyFromOntologyDocument(file);
					
					Set<OWLIndividual> inds = QualitativeReasoningWorkbench.OPBtopPropClass.getIndividuals(wb.inferredAxiomsOntology);
					modelnamespace = inds.toArray(new OWLIndividual[]{})[0].asOWLNamedIndividual().getIRI().getStart();
					
					propertybox.removeAllItems();
					clearScrollers();

					for(OWLIndividual ind : inds){
						propertybox.addItem(ind.asOWLNamedIndividual().getIRI().getFragment());
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
		
		if(o == loadModelButton){
			JFileChooser fc = new JFileChooser(sourcemoddirectory);
			fc.setPreferredSize(new Dimension(500,500));
						
			if(fc.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION){
				File file = fc.getSelectedFile();
				
				SemSimModel model;
				try {
					System.out.println("Loading model into SemSimAPI");
					
					ModelAccessor ma = FileAccessorFactory.getModelAccessor(file);
					model = new SemSimOWLreader(ma).read();
					
					loadLabel.setText("Model loaded: " + file.getName());
					
					System.out.println("Model loaded into SemSimAPI");
					
					OWLOntology mergedOnt = wb.mergeOPBandSemSimModel(model);
					modelnamespace = model.getNamespace();
					
					System.out.println("Model merged with OPB");
					
					long startTime = System.currentTimeMillis();
					
					wb.classifyOntology(mergedOnt, model.getName());
					
					long endTime   = System.currentTimeMillis();
					
					System.out.println("Classification took: " + (endTime - startTime) + " milliseconds");
					
					propertybox.removeAllItems();
					clearScrollers();
						
					for(OWLIndividual ind : QualitativeReasoningWorkbench.OPBtopPropClass.getIndividuals(wb.inferredAxiomsOntology)){
						propertybox.addItem(ind.asOWLNamedIndividual().getIRI().getFragment());
					}
				} catch (OWLException e1) {
					e1.printStackTrace();
				} catch (JDOMException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
		
		if(o==analyzecausalitystartbutton){
			
			String startindname = modelnamespace + propertybox.getSelectedItem();
			
			try {
				analyzecausalitystartbutton.setEnabled(false);
				analyzecausalitystartbutton.setText("Working...");
				BatonVal batonval = null;
				
				if(updownbox.getSelectedItem()=="increase in")
					batonval = QualitativeReasoningWorkbench.BatonVal.UP;
				else if(updownbox.getSelectedItem()=="decrease in")
					batonval = QualitativeReasoningWorkbench.BatonVal.DOWN;
				
				long startTime = System.currentTimeMillis();
				
				AnalysisResults results = wb.performQualitativeAnalysis(startindname, batonval, AnalysisType.DOWNSTREAM);
				
				long endTime = System.currentTimeMillis();
				
				System.out.println("Downstream analysis took: " + (endTime - startTime) + " milliseconds");
				
				if(results!=null) displayResults(results);
				
				analyzecausalitystartbutton.setEnabled(true);
				analyzecausalitystartbutton.setText("Get downstream effects");
				
			} catch (OWLException e1) {
				e1.printStackTrace();
			}
		}
		
		if(o == analyzecausalityendbutton){
			
			String startinduri = modelnamespace + propertybox.getSelectedItem();
			
			try {
				analyzecausalityendbutton.setEnabled(false);
				analyzecausalityendbutton.setText("Working...");
				
				BatonVal batonval = null;
				
				if(updownbox.getSelectedItem()=="increase in")
					batonval = QualitativeReasoningWorkbench.BatonVal.UP;
				else if(updownbox.getSelectedItem()=="decrease in")
					batonval = QualitativeReasoningWorkbench.BatonVal.DOWN;
				
				long startTime = System.currentTimeMillis();

				AnalysisResults results = wb.performQualitativeAnalysis(startinduri, batonval, AnalysisType.UPSTREAM);
				
				long endTime = System.currentTimeMillis();
				
				System.out.println("Upstream analysis took: " + (endTime - startTime) + " milliseconds");
				
				if(results!=null) displayResults(results);
			
				analyzecausalityendbutton.setEnabled(true);
				analyzecausalityendbutton.setText("Get upstream causes");
			
			} catch (OWLException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	private void displayResults(AnalysisResults results){
		upscroller.updateResults(results.onlyposfrags.toArray(new String[]{}));
		downscroller.updateResults(results.onlynegfrags.toArray(new String[]{}));
		ambiguousscroller.updateResults(results.ambigfrags.toArray(new String[]{}));
	}

	private void clearScrollers(){
		upscroller.clear();
		downscroller.clear();
		ambiguousscroller.clear();
	}
}
