package myQualitativeReasoning;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;

import myQualitativeReasoning.QualitativeReasoningWorkbench.BatonVal;
import semsim.owl.SemSimOWLFactory;

public class AnalysisResults {
	// n-by-n matrix where the first dimension represents the index of the start property, the second the end property
	// that are connected by some dependency relation
	// The values in the matrix indicate whether the edge between the two properties has
	// already been traversed by the analyzer
	//public ArrayList<ArrayList<Set<BatonVal>>> edgetraversalarray;
	
	private Analyzer analyzer;
	
	public boolean[][] edgetraversalarray;
	public BatonVal[] influencearray; 
	
	public Set<String> onlyposfrags = new HashSet<String>();
	public Set<String> onlynegfrags = new HashSet<String>();
    public Set<String> ambigfrags = new HashSet<String>();
	
    public Set<String> stimulatoryfeedback = new HashSet<String>();
	public Set<String> inhibitoryfeedback = new HashSet<String>();
	
	public int count;
	public ArrayList<URI> physpropURIlist = new ArrayList<URI>();
	public ArrayList<String> physpropfraglist = new ArrayList<String>(10);


	public AnalysisResults(Analyzer analyzer, String rootname, BatonVal batonvalforroot){
		
		this.analyzer = analyzer;
		
		OWLOntology ont = analyzer.getInferredAxiomsOntology();
		
		// Create indexes for each physical property in the model
		Set<String> allprops;
		try {
			allprops = SemSimOWLFactory.getIndividualsInTreeAsStrings(ont, QualitativeReasoningWorkbench.OPBtopPropClass.getIRI().toString());
			count = allprops.size();
//			edgetraversalarray = new ArrayList<ArrayList<Set<BatonVal>>>(count);
			edgetraversalarray = new boolean[count][count];

//			int i = 0;
			for(String uri : allprops){
				physpropURIlist.add(URI.create(uri));
				physpropfraglist.add(SemSimOWLFactory.getIRIfragment(uri));
//				ArrayList<Set<BatonVal>> sublist = new ArrayList<Set<BatonVal>>(count);
//				
//				for(int j=0; j<count; j++)
//					sublist.add(j, new HashSet<BatonVal>());
				
				//edgetraversalarray.add(i, sublist);
//				i++;
			}
			

			influencearray = new BatonVal[count];
			
			String frag = URI.create(rootname).getFragment();

			if(batonvalforroot==BatonVal.UP)
				onlyposfrags.add(frag);
			else if(batonvalforroot==BatonVal.DOWN)
				onlynegfrags.add(frag);
			
		} catch (OWLException e) {
			e.printStackTrace();
		}
	}
	
	
	public void compileResults(){
		// Display what goes up, what goes down, and what's ambiguous
//		for(String propfrag : physpropfraglist){
//			
//			if(posfrags.contains(propfrag) && negfrags.contains(propfrag))
//				ambigfrags.add(propfrag);
//			
//			else if(posfrags.contains(propfrag))
//				onlyposfrags.add(propfrag);
//			
//			else if(negfrags.contains(propfrag))
//				onlynegfrags.add(propfrag);
//		}
		
		for(int i=0; i<count; i++){
			BatonVal val = influencearray[i];
			
			if(val==BatonVal.UP) onlyposfrags.add(physpropfraglist.get(i));
			
			else if(val==BatonVal.DOWN) onlynegfrags.add(physpropfraglist.get(i));
			
			else if(val==BatonVal.AMBIGUOUS) ambigfrags.add(physpropfraglist.get(i));
		}
		
		System.out.println("\n\n\n");
		System.out.println("---INCREASED---");
		for(String onlyposinfind : onlyposfrags){
			System.out.println(onlyposinfind);
		}
		
		System.out.println("---DECREASED---");
		for(String onlyneginfind : onlynegfrags){
			System.out.println(onlyneginfind);
		}
		
		System.out.println("---AMBIGUOUS---");
		for(String ambig : ambigfrags){
			System.out.println(ambig);
		}
		
		System.out.println("---stimulatory FEEDBACK---");
		for(String posfeed : stimulatoryfeedback){
			System.out.println(posfeed);
		}
		
		System.out.println("---inhibitory FEEDBACK---");
		for(String negfeed : inhibitoryfeedback){
			System.out.println(negfeed);
		}
		
		for(ArrayList<String> list : analyzer.pathstaken){
			System.out.println("\n***");
			for(String uri : list){
				System.out.println(uri);
			}
		}
	}
}
