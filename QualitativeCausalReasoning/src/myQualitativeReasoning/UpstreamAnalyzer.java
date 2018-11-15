package myQualitativeReasoning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;

import myQualitativeReasoning.QualitativeReasoningWorkbench.BatonVal;
import semsim.owl.SemSimOWLFactory;

public class UpstreamAnalyzer extends Analyzer{
	
	public UpstreamAnalyzer(OWLOntology inferredAxiomsOntology, String ultimateRootName, BatonVal batonVal){
		super(inferredAxiomsOntology, ultimateRootName, batonVal);
	}

	@Override
	public void incrementAnalysis(
			BatonVal baton, 
			String currentpropuri, 
			String previouspropuri) throws OWLException{
		
		// Pseudocode for upstream qualitative reasoning procedure
		
		// Get the dependency that solves for the property
		// Get the positive and negative role players
		// for each role player
		//  if positive, record in positive list
		//  if negative, record in negative list
		//  recurse on the dependency that solves for the role player
		
		String currentpropdepuri = currentpropuri.replace("property", "dependency");
		Set<String> posplayers = SemSimOWLFactory.getIndObjectProperty(getInferredAxiomsOntology(), currentpropdepuri, hasPosPropertyPlayerIRI.toString());
		Set<String> negplayers = SemSimOWLFactory.getIndObjectProperty(getInferredAxiomsOntology(), currentpropdepuri, hasNegPropertyPlayerIRI.toString());
		
		ArrayList<String> allplayers = new ArrayList<String>();
		allplayers.addAll(posplayers);
		allplayers.addAll(negplayers);
		Collections.sort(allplayers);

		for(String player : allplayers){
			
			if(currentpath.contains(player)){
				pathstaken.add(currentpath);
				currentpath = new ArrayList<String>();
			}
			else currentpath.add(player);
				
			passBaton(currentpropuri, player, previouspropuri, baton, posplayers, negplayers, currentpropdepuri, player);
		}
	}
}
