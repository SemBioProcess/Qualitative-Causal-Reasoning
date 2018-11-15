package myQualitativeReasoning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;

import myQualitativeReasoning.QualitativeReasoningWorkbench.BatonVal;
import semsim.owl.SemSimOWLFactory;

public class DownstreamAnalyzer extends Analyzer{
	
	
	public DownstreamAnalyzer(OWLOntology inferredAxiomsOntology, String ultimateRootName, BatonVal batonVal){
		super(inferredAxiomsOntology, ultimateRootName, batonVal);
	}
	
	public void incrementAnalysis(
			BatonVal baton, 
			String currentpropuri, 
			String previouspropuri) throws OWLException{
				
		// Pseudocode for downstream qualitative reasoning procedure
		
		// start with root property, find its role in dependencies, 
		// get solved player
		//  if solved player is root, return (stop)
		//  else set up/down/ambig
		//   if seen before and baton value changed, set as ambiguous
		//   else if seen before and not changed, return (stop)
		//   recurse on solved player
		

		System.out.println("Analyzing " + SemSimOWLFactory.getIRIfragment(currentpropuri));
		System.out.println("Previous property was " + SemSimOWLFactory.getIRIfragment(previouspropuri));
		
		Set<String> posdeps = SemSimOWLFactory.getIndObjectProperty(getInferredAxiomsOntology(), currentpropuri, posPropertyPlayerInIRI.toString());
		Set<String> negdeps = SemSimOWLFactory.getIndObjectProperty(getInferredAxiomsOntology(), currentpropuri, negPropertyPlayerInIRI.toString());
		
		ArrayList<String> alldeps = new ArrayList<String>();
		alldeps.addAll(posdeps);
		alldeps.addAll(negdeps);
		Collections.sort(alldeps);
		
		for(String dep : alldeps){
						
			String solvedpropuri = SemSimOWLFactory.getFunctionalIndObjectProperty(getInferredAxiomsOntology(), dep, hasSolvedPropertyPlayerIRI.toString());

			if(currentpath.contains(solvedpropuri) && ! solvedpropuri.equals(previouspropuri)){
				
				currentpath.add(solvedpropuri);
				pathstaken.add(currentpath);
				currentpath = new ArrayList<String>();
				currentpath.add(currentpropuri);
				currentpath.add(solvedpropuri);
			}
			else currentpath.add(solvedpropuri);
			
			passBaton(currentpropuri, solvedpropuri, previouspropuri, baton, posdeps, negdeps, dep, dep);
		}
	}

}
