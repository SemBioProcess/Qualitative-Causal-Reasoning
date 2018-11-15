package myQualitativeReasoning;

import java.net.URI;
import java.util.ArrayList;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;

import myQualitativeReasoning.QualitativeReasoningWorkbench.BatonVal;
import semsim.definitions.SemSimRelations.SemSimRelation;
import semsim.owl.SemSimOWLFactory;

public abstract class Analyzer {

	private OWLOntology inferredAxiomsOntology;
	private String ultimateRootURI;
	private AnalysisResults results;
	public ArrayList<ArrayList<String>> pathstaken;
	public ArrayList<String> currentpath;

	
	public static IRI negPropertyPlayerInIRI = IRI.create(QualitativeReasoningWorkbench.OPBns + "OPB_00006");
	public static IRI posPropertyPlayerInIRI = IRI.create(QualitativeReasoningWorkbench.OPBns + "OPB_00038");
	public static IRI hasSolvedPropertyPlayerIRI = SemSimRelation.HAS_SOLVED_PROPERTY_PLAYER.getIRI();
	public static IRI hasPosPropertyPlayerIRI = IRI.create(QualitativeReasoningWorkbench.OPBns + "OPB01071");
	public static IRI hasNegPropertyPlayerIRI = IRI.create(QualitativeReasoningWorkbench.OPBns + "OPB01106");
	
	public OWLDataFactory factory = QualitativeReasoningWorkbench.factory;

	public static OWLClass OPBdynamicalProcessClass = QualitativeReasoningWorkbench.OPBdynamicalProcessClass;
	public static OWLClass OPBdynamicalEntityClass = QualitativeReasoningWorkbench.OPBdynamicalEntityClass;
	public static OWLClass OPBboundaryMaterialFlowDependencyClass = QualitativeReasoningWorkbench.OPBboundaryMaterialFlowDependencyClass;

	public Analyzer(OWLOntology inferredAxiomsOntology, String urURI, BatonVal batonVal){
		this.setInferredAxiomsOntology(inferredAxiomsOntology);
		this.setUltimateRootName(urURI);
		this.setAnalysisResults(new AnalysisResults(this, urURI, batonVal));
		pathstaken = new ArrayList<ArrayList<String>>();
		currentpath = new ArrayList<String>();
		currentpath.add(getUltimateRootURI());
	}
	
	public abstract void incrementAnalysis(
			BatonVal baton, 
			String currentpropuri, 
			String previouspropuri) throws OWLException;

	public OWLOntology getInferredAxiomsOntology() {
		return inferredAxiomsOntology;
	}
	
	public String getUltimateRootURI(){
		return ultimateRootURI;
	}
	
	public AnalysisResults getAnalysisResults() {
		return results;
	}

	public void setInferredAxiomsOntology(OWLOntology inferredAxiomsOntology) {
		this.inferredAxiomsOntology = inferredAxiomsOntology;
	}
	
	public void setUltimateRootName(String ultimateRootURI){
		this.ultimateRootURI = ultimateRootURI;
	}

	public void setAnalysisResults(AnalysisResults results) {
		this.results = results;
	}	
	
	// Determines baton value for a physical property based on the current baton value
	// and whether the property has a positive or negative association with the property
	// that starts with the baton.
	// Used for both upstream and downstream analyses.
	
	public void passBaton(
			String currenturi,
			String nexturi,
			String previousuri,
			BatonVal baton,
			Set<String> posuris,
			Set<String> neguris,
			String depuriforsolvedprop,
			String uritocheckforbatonswitch) throws OWLException{
		
		Integer currenturiindex = getAnalysisResults().physpropURIlist.indexOf(URI.create(currenturi));
		Integer nexturiindex = getAnalysisResults().physpropURIlist.indexOf(URI.create(nexturi));

		String cun = SemSimOWLFactory.getIRIfragment(currenturi);
		String pun = SemSimOWLFactory.getIRIfragment(previousuri);
		String nun = SemSimOWLFactory.getIRIfragment(nexturi);
		
		System.out.println("Looking at edge between " + cun + " and " + nun + ": (baton is " + baton.name() + ")");
		
		boolean edgealreadytraversed = getAnalysisResults().edgetraversalarray[currenturiindex][nexturiindex];
		boolean polarityknown = ! (posuris.contains(currenturi) && neguris.contains(currenturi));
		
		// If we're at the root, or if we're in a short causation loop, return
		if(nexturi.equals(getUltimateRootURI())){
			System.out.println("Stopping because at root property");
			return;
		}
		
		// If we're in a short loop, and we know the polarity of the role player
		// go to next dependency
		if(nexturi.equals(previousuri) && polarityknown){
			System.out.println("Stopping because short loop identified between " + cun + " and " + pun);
			return;
		}
		
		// If we're propagating an ambiguous baton, only do so if we haven't already
		// traversed the edge
		if(baton==BatonVal.AMBIGUOUS && edgealreadytraversed){
			System.out.println("Ambiguous baton and edge already traversed: skipping dependency");
			return;
		}

		// Switch baton value only if property has a inhibitory influence on solved property
		// Otherwise the value stays where it is (even for ambiguous values).
		
		boolean switchbaton = false;
		BatonVal newbaton = baton;
		
		// Get the polarity of the property if it has dual role player status
		if( ! polarityknown){
			int polarityval = getPolarityOfPlayerFromStoichiometry(currenturi, depuriforsolvedprop);
			System.out.println("Polarity value was " + polarityval);
			switchbaton = (polarityval == -1);
		}
		else if(neguris.contains(uritocheckforbatonswitch))
			switchbaton = true;
		
		if(switchbaton){
			if(baton==BatonVal.UP) newbaton = BatonVal.DOWN;
			else if(baton==BatonVal.DOWN) newbaton = BatonVal.UP;
		}
		
		BatonVal existingbatonval = getAnalysisResults().influencearray[nexturiindex];
		
		if(existingbatonval != null){
			
//				newbval	   A, U,  D
//						A  r  r   r
//			existingval U  A  r+sf A+if
//						D  A  A+if r+sf
			
			if(existingbatonval != BatonVal.AMBIGUOUS){
				
				if(newbaton==BatonVal.AMBIGUOUS){}
				
				else if(existingbatonval==newbaton){
					
					getAnalysisResults().stimulatoryfeedback.add(nexturi);
					
					// Quit, but only if we've previously gone down the edge connecting the two properties
					if(edgealreadytraversed){
						System.out.println("Stopping because edge already traversed with resulting baton value " + newbaton.name());
						return;
					}
				}
				
				else{
					getAnalysisResults().inhibitoryfeedback.add(nexturi);
					newbaton = BatonVal.AMBIGUOUS;
				}
			}
			else{
				System.out.println("Stopping because " + nun + " already recorded as AMBIGUOUS");
				return;
			}
		}
		
		getAnalysisResults().influencearray[nexturiindex] = newbaton;

		System.out.println("Recorded " + nun + " " + newbaton.toString());
		
		// Record which edge we've traversed
		getAnalysisResults().edgetraversalarray[currenturiindex][nexturiindex] = true;
		
		//recurse
		incrementAnalysis(newbaton, nexturi, currenturi);
	}
	
	
	public int getPolarityOfPlayerFromStoichiometry(String currentpropuri, String dependencyuri) throws OWLException{
		
		// If currentpropuri is a property of a process and solvedpropuri 
		// is solved with a boundary material flow dependency (OPB_01663)
		// then get the process associated with currentpropuri.
		// and then get the entity that the dep solves for
		// record the multiplier on the source, record the multiplier on the sink
		String currentphyspropof = SemSimOWLFactory.getFunctionalIndObjectProperty(
				getInferredAxiomsOntology(), currentpropuri, SemSimRelation.PHYSICAL_PROPERTY_OF.getIRI().toString());
		OWLIndividual currentphyspropofind = factory.getOWLNamedIndividual(IRI.create(currentphyspropof));

		OWLIndividual depind = factory.getOWLNamedIndividual(IRI.create(dependencyuri));
		String solvedpropuri = SemSimOWLFactory.getFunctionalIndObjectProperty(
				getInferredAxiomsOntology(), dependencyuri, hasSolvedPropertyPlayerIRI.toString());
		String solvedphyspropof = SemSimOWLFactory.getFunctionalIndObjectProperty(
				getInferredAxiomsOntology(), solvedpropuri, SemSimRelation.PHYSICAL_PROPERTY_OF.getIRI().toString());

		OWLIndividual solvedphyspropofind = factory.getOWLNamedIndividual(IRI.create(solvedphyspropof));
		
		String processuri = null;
		String entityuri = null;
		
		// If the current property is a property of a process, and it's used in a dependency
		// that computes the property of an entity, and the dependency is an OPB:Boundary material flow dependency...
		if(OPBdynamicalProcessClass.getIndividuals(getInferredAxiomsOntology()).contains(currentphyspropofind)
				&& OPBdynamicalEntityClass.getIndividuals(getInferredAxiomsOntology()).contains(solvedphyspropofind)
				&& OPBboundaryMaterialFlowDependencyClass.getIndividuals(getInferredAxiomsOntology()).contains(depind)){
			
			processuri = currentphyspropof;
			entityuri = solvedphyspropof;

			Set<OWLAnnotation> sourceanns = QualitativeReasoningWorkbench.getAnnotationsForProcessParticipant(
					getInferredAxiomsOntology(), processuri, SemSimRelation.HAS_SOURCE.getIRI().toString(), entityuri);
			
			Set<OWLAnnotation> sinkanns = QualitativeReasoningWorkbench.getAnnotationsForProcessParticipant(
					getInferredAxiomsOntology(), processuri, SemSimRelation.HAS_SINK.getIRI().toString(), entityuri);
			
			OWLLiteral sourcemultlit = (OWLLiteral) sourceanns.toArray(new OWLAnnotation[]{})[0].getValue();
			OWLLiteral sinkmultlit = (OWLLiteral) sinkanns.toArray(new OWLAnnotation[]{})[0].getValue();
			
			double diffinsourceandsink = sourcemultlit.parseDouble() - sinkmultlit.parseDouble();
			
			if(diffinsourceandsink==0.0) return 0;
			else if(diffinsourceandsink>0.0) return 1;
			else return -1;
		}
		// Otherwise if the current property is a property of an entity and it's used to compute the
		// property of a process...
		else if(OPBdynamicalProcessClass.getIndividuals(getInferredAxiomsOntology()).contains(solvedphyspropofind)
				&& OPBdynamicalEntityClass.getIndividuals(getInferredAxiomsOntology()).contains(currentphyspropofind)){
			
			processuri = solvedphyspropof;
			entityuri = currentphyspropof;
			
			// Returning 1 means that we assume that if an entity is a source and a sink, that 
			// it's only the amount of the source that influences the process (as in a 1st order chemcial rate law)
			return 1;
		}
		
		return 0;
	}
}
