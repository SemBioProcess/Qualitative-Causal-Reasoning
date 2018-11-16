package myQualitativeReasoning;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Reasoner.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLIndividualAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.reasoner.ConsoleProgressMonitor;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.InferredAxiomGenerator;
import org.semanticweb.owlapi.util.InferredClassAssertionAxiomGenerator;
import org.semanticweb.owlapi.util.InferredOntologyGenerator;
import org.semanticweb.owlapi.util.InferredPropertyAssertionGenerator;

import myQualitativeReasoning.QualitativeReasoningGUI.AnalysisType;
import semsim.definitions.RDFNamespace;
import semsim.definitions.SemSimRelations.SemSimRelation;
import semsim.model.collection.SemSimModel;
import semsim.model.computational.Computation;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.object.PhysicalPropertyInComposite;
import semsim.owl.SemSimOWLFactory;
import semsim.writing.SemSimOWLwriter;


public class QualitativeReasoningWorkbench {

	public static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	public static OWLDataFactory factory = manager.getOWLDataFactory();
	
	public OWLOntology OPBedited;
	public OWLOntology OPB_QR;
	public static String OPBns = RDFNamespace.OPB.getNamespaceAsString();
	public static OWLClass OPBtopDepClass = factory.getOWLClass(IRI.create(OPBns + "OPB_01391"));
	public static OWLClass OPBtopPropClass = factory.getOWLClass(IRI.create(OPBns + "OPB_00147"));
	public static OWLClass OPBdynamicalProcessClass = factory.getOWLClass(IRI.create(OPBns + "OPB_01650"));
	public static OWLClass OPBdynamicalEntityClass = factory.getOWLClass(IRI.create(OPBns + "OPB_01014"));
	public static OWLClass OPBboundaryMaterialFlowDependencyClass = factory.getOWLClass(IRI.create(OPBns + "OPB_01663"));
	public static OWLAnnotationProperty multiplierprop = factory.getOWLAnnotationProperty(SemSimRelation.HAS_MULTIPLIER.getIRI());
	
	public static IRI hasDomainIRI = IRI.create(OPBns + "OPB_00320");
	public static OWLObjectPropertyExpression hasPropertyPlayerProp = factory.getOWLObjectProperty(SemSimRelation.HAS_PROPERTY_PLAYER.getIRI());
	public OWLOntology mergedOnt;
	public OWLOntology inferredAxiomsOntology;
	public OWLOntology semsimOrigOnt;
	public OWLOntology semsimDepOnt;
	private Map<String,Integer> propClassCountMap;
	private String modelns;
	
	public Set<String> classifiedprocessprops;
	public Set<String> classifiedentityprops;
	
	public static enum BatonVal {UP, DOWN, AMBIGUOUS};
	
	

	
	public QualitativeReasoningWorkbench(){

	    File OPBeditedFile = new File("cfg/OPBv1.06_MLNedits.owl");
	    File OPB_QRfile = new File("cfg/OPB_QRv0.01.owl");
		try {
			OPBedited = manager.loadOntologyFromOntologyDocument(OPBeditedFile);
			OPB_QR = manager.loadOntologyFromOntologyDocument(OPB_QRfile);
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
		
		propClassCountMap = new HashMap<String,Integer>();

	}
	
	public OWLOntology mergeOPBandSemSimModel(SemSimModel model) throws OWLException{
	     		
		 semsimOrigOnt = new SemSimOWLwriter(model).createOWLOntologyFromModel();
		 semsimDepOnt = manager.createOntology();
	     mergedOnt = manager.createOntology();
	     
	     modelns = model.getNamespace();
	     Set<OWLAxiom> allaxioms = OPBedited.getAxioms();
	     allaxioms.addAll(OPB_QR.getAxioms());
	    // allaxioms.addAll(semsimOrigOnt.getAxioms()); UNCOMMENT TO INCLUDE ALL SEMSIM MODEL AXIOMS
	     
	     for(DataStructure ds : model.getAssociatedDataStructures()){
	    	 
		     propClassCountMap.clear();

	    	 if(ds.getComputationInputs().size() > 0) {
	    		 
	    		 Computation computation = ds.getComputation();
	    		 	    		 
	    		 // Create the dependency individual
	    		 String depIndString = modelns + ds.getName() + "_dependency";
	    		 OWLNamedIndividual depInd = factory.getOWLNamedIndividual(IRI.create(depIndString));
	    		 
	    		 SemSimOWLFactory.createSemSimIndividual(semsimDepOnt, depIndString, OPBtopDepClass, "", manager);
	    		 	    		 
	    		 // Create the property player if the output for the computation
	    		 // is annotated against an OPB term and if it's not solved with an ODE
	    		 // Change this so that we can set the hasSolvedPlayer info for chemical species
	    		 
	    		 if(ds.hasPhysicalProperty()){ 
	    			 
	    			 addPropertyIndividual(ds, modelns, depIndString);
	    			 
	    			// If the Data Structure represents a property of a process
		    		 // add the process and its sources and sinks
		    		 if(ds.hasAssociatedPhysicalComponent()){
		    			 
		    			 if(ds.getAssociatedPhysicalModelComponent() instanceof PhysicalProcess){
		    						    			
		    				 String physpropURI = modelns + ds.getName() + "_property";
		    				 
		    				 // Get the process URI
		    				 String processURI = SemSimOWLFactory.getFunctionalIndObjectPropertyObject(semsimOrigOnt, 
		    						 physpropURI, SemSimRelation.PHYSICAL_PROPERTY_OF.getIRI().toString());
		    				  
		    				 // Get the sources for the process
		    				 Set<String> sources = SemSimOWLFactory.getIndObjectPropertyObjects(semsimOrigOnt, 
		    						 processURI, SemSimRelation.HAS_SOURCE.getIRI().toString());
		    				 
		    				 for(String source : sources){
		    					
		    					 // Create the source entity
			    				 SemSimOWLFactory.createSemSimIndividual(semsimDepOnt, source, 
			    						 OPBdynamicalEntityClass, "", manager);
			    				 
			    				 Set<OWLAnnotation> anns = getAnnotationsForProcessParticipant(
			    						 semsimOrigOnt, processURI, SemSimRelation.HAS_SOURCE.getIRI().toString(), source);	
			    				 
			    				 SemSimOWLFactory.setIndObjectPropertyWithAnnotations(semsimDepOnt, processURI, 
			    						 source, SemSimRelation.HAS_SOURCE, null, anns, manager);
		    				 }
		    				 
		    				 // Get the sinks for the process
		    				 Set<String> sinks = SemSimOWLFactory.getIndObjectPropertyObjects(semsimOrigOnt, 
		    						 processURI, SemSimRelation.HAS_SINK.getIRI().toString());
		    				 
		    				 for(String sink : sinks){
			    					
		    					 // Create the sink entity
			    				 SemSimOWLFactory.createSemSimIndividual(semsimDepOnt, sink, 
			    						 OPBdynamicalEntityClass, "", manager);
			    				 
			    				 Set<OWLAnnotation> anns = getAnnotationsForProcessParticipant(
			    						 semsimOrigOnt, processURI, SemSimRelation.HAS_SINK.getIRI().toString(), sink);
			    				 
			    				 SemSimOWLFactory.setIndObjectPropertyWithAnnotations(semsimDepOnt, processURI, 
			    						 sink, SemSimRelation.HAS_SINK, null, anns, manager);
		    				 }
		    				 
		    				 // Get the mediators for the process
		    				 Set<String> mediators = SemSimOWLFactory.getIndObjectPropertyObjects(semsimOrigOnt, 
		    						 processURI, SemSimRelation.HAS_MEDIATOR.getIRI().toString());
		    				 
		    				 for(String mediator : mediators){
			    					
		    					 // Create the sink entity
			    				 SemSimOWLFactory.createSemSimIndividual(semsimDepOnt, mediator, 
			    						 OPBdynamicalEntityClass, "", manager);
			    				 SemSimOWLFactory.setIndObjectProperty(semsimDepOnt, processURI, 
			    						 mediator, SemSimRelation.HAS_MEDIATOR, null, manager);
		    				 }
		    			 }
		    		 }
	    		 }
	    		 
	    		 
	    		 
	    		 // Add property players from computational input set
	    		 for(DataStructure inputds : computation.getInputs()){
	    			 if(inputds.hasPhysicalProperty())
	    				 addPropertyIndividual(inputds, modelns, depIndString);
	    		 }
	    		 	    		 
	    		 // Create the restrictions on the dependency individual
	    		 Set<OWLClass> propClassSet = new HashSet<OWLClass>();
	    		 OWLClassExpression runningQCRexp = null;
	    		 
	    		 for(String propClassString : propClassCountMap.keySet()){
	    			 
	    			 OWLClass propClass = factory.getOWLClass(IRI.create(propClassString));
	    			 propClassSet.add(propClass);
	    			 
	    			 Integer count = propClassCountMap.get(propClassString);
		    		 OWLClassExpression QCRexp = factory.getOWLObjectExactCardinality(count, hasPropertyPlayerProp, propClass);
		    		 
		    		 if(runningQCRexp != null)  runningQCRexp = factory.getOWLObjectIntersectionOf(runningQCRexp, QCRexp);
		    		 else runningQCRexp = QCRexp;
	    		 }
	    		 
	    		 if(runningQCRexp != null){
	    			 OWLIndividualAxiom universalAxiom = factory.getOWLClassAssertionAxiom(runningQCRexp, depInd);
	    		 	addAxiomToSemSimDepOnt(universalAxiom);
	    		 }
	    		
	    		 // Create the closure axiom on the dependency	    		 
	    		 OWLClassExpression allpropclsexp = factory.getOWLObjectUnionOf(propClassSet);
	    		 OWLClassExpression closureexp = factory.getOWLObjectAllValuesFrom(hasPropertyPlayerProp, allpropclsexp);
	    		 OWLIndividualAxiom closureAxiom = factory.getOWLClassAssertionAxiom(closureexp, depInd);
	    		 addAxiomToSemSimDepOnt(closureAxiom);		 
	    	 }
	     }
	     
	     allaxioms.addAll(semsimDepOnt.getAxioms());
	     
	     for(OWLAxiom ax : allaxioms){
	    	AddAxiom addAxiom = new AddAxiom(mergedOnt, ax);
	 		manager.applyChange(addAxiom);
	     }
	     
	     OWLDifferentIndividualsAxiom diffInds = factory
	                .getOWLDifferentIndividualsAxiom(mergedOnt.getIndividualsInSignature());
	        manager.addAxiom(mergedOnt, diffInds);	
	        
	     String outputfilename = "OPB_" + model.getName() + "_merged.owl";
	     File outputfile = new File("./mergedOnts/" + outputfilename);
	     manager.saveOntology(mergedOnt, new RDFXMLOntologyFormat(), IRI.create(outputfile.toURI()));
	     
	     return mergedOnt;
	}
	
	
	public void classifyOntology(OWLOntology ont, String modelname) throws OWLOntologyCreationException, OWLOntologyStorageException{
			
		// WORKING WITH HERMIT
	
        OWLOntologyManager manager=OWLManager.createOWLOntologyManager();        
        ReasonerFactory reasonerfactory = new ReasonerFactory();
        
        // The factory can now be used to obtain an instance of HermiT as an OWLReasoner. 
        Configuration c=new Configuration();
        c.reasonerProgressMonitor=new ConsoleProgressMonitor();
        OWLReasoner reasoner=reasonerfactory.createReasoner(ont, c);
       
        // The following call causes HermiT to compute the class, object, 
        // and data property hierarchies as well as the class instances. 
        // Hermit does not yet support precomputation of property instances. 
//        reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY, InferenceType.CLASS_ASSERTIONS, InferenceType.OBJECT_PROPERTY_HIERARCHY, InferenceType.DATA_PROPERTY_HIERARCHY, InferenceType.OBJECT_PROPERTY_ASSERTIONS);
        reasoner.precomputeInferences(InferenceType.CLASS_ASSERTIONS, InferenceType.OBJECT_PROPERTY_ASSERTIONS);

        List<InferredAxiomGenerator<? extends OWLAxiom>> generators=new ArrayList<InferredAxiomGenerator<? extends OWLAxiom>>();
        generators.add(new InferredPropertyAssertionGenerator()); // Added by me
//        generators.add(new InferredSubClassAxiomGenerator());  // In orig hermit example
        generators.add(new InferredClassAssertionAxiomGenerator()); // In orig hermit example
//        generators.add(new InferredEquivalentClassAxiomGenerator()); // Added by me
//        generators.add(new InferredInverseObjectPropertiesAxiomGenerator()); // Added by me
//        generators.add(new InferredObjectPropertyCharacteristicAxiomGenerator()); // Added by me
//        generators.add(new InferredSubObjectPropertyAxiomGenerator()); // Added by me

        
        // These are the generators used if just call InferredOntologyGenerator with no generators arguments:
//        axiomGenerators.add(new InferredClassAssertionAxiomGenerator());
//        axiomGenerators.add(new InferredDataPropertyCharacteristicAxiomGenerator());
//        axiomGenerators.add(new InferredEquivalentClassAxiomGenerator());
//        axiomGenerators.add(new InferredEquivalentDataPropertiesAxiomGenerator());
//        axiomGenerators.add(new InferredEquivalentObjectPropertyAxiomGenerator());
//        axiomGenerators.add(new InferredInverseObjectPropertiesAxiomGenerator());
//        axiomGenerators.add(new InferredObjectPropertyCharacteristicAxiomGenerator());
//        axiomGenerators.add(new InferredPropertyAssertionGenerator());
//        axiomGenerators.add(new InferredSubClassAxiomGenerator());
//        axiomGenerators.add(new InferredSubDataPropertyAxiomGenerator());
//        axiomGenerators.add(new InferredSubObjectPropertyAxiomGenerator());
    
        
        // this is the call that freezes the reasoning task
//        generators.add(new InferredDisjointClassesAxiomGenerator() {
//            
//        	boolean precomputed=false;
//            protected void addAxioms(OWLClass entity, OWLReasoner reasoner, OWLDataFactory dataFactory, Set<OWLDisjointClassesAxiom> result) {
//                if (!precomputed) {
//                    reasoner.precomputeInferences(InferenceType.DISJOINT_CLASSES);
//                    precomputed=true;
//                }
//                for (OWLClass cls : reasoner.getDisjointClasses(entity).getFlattened()) {
//                    result.add(dataFactory.getOWLDisjointClassesAxiom(entity, cls));
//                }
//            }
//        });
        
        
        
        // We can now create an instance of InferredOntologyGenerator. 
        InferredOntologyGenerator iog=new InferredOntologyGenerator(reasoner,generators);
                
        // Before we actually generate the axioms into an ontology, we first have to create that ontology. 
        // The manager creates the for now empty ontology for the inferred axioms for us. 
         inferredAxiomsOntology = manager.createOntology();
        
        System.out.println("Filling...");
        
        // Now we use the inferred ontology generator to fill the ontology. That might take some 
        // time since it involves possibly a lot of calls to the reasoner.    
        iog.fillOntology(manager, inferredAxiomsOntology);
        
        // Remove the topObjectProperty assertions for readability
        for(OWLAxiom infax : inferredAxiomsOntology.getAxioms()){
        	
        	if(infax instanceof OWLObjectPropertyAssertionAxiom){
        		OWLObjectPropertyAssertionAxiom oopax = (OWLObjectPropertyAssertionAxiom)infax;
        		
        		if(oopax.getProperty().asOWLObjectProperty().getIRI().toString().equals("http://www.w3.org/2002/07/owl#topObjectProperty")){
        			manager.removeAxiom(inferredAxiomsOntology, oopax);
        		}
        	}
        }


    	System.out.println("Filled.");
        
        addAxiomsToOntology(ont.getAxioms(), inferredAxiomsOntology);
       
        File inferredOntologyFile = new File("./mergedOnts/OPB_" + modelname + "_merged_classified.owl");
	    manager.saveOntology(inferredAxiomsOntology, new RDFXMLOntologyFormat(), IRI.create(inferredOntologyFile.toURI()));  
        System.out.println("DONE");
        
        int nump = OPBtopPropClass.getIndividuals(inferredAxiomsOntology).size(); 
        int numd = OPBtopDepClass.getIndividuals(inferredAxiomsOntology).size();
        
        System.out.println("\nNum physical property individuals: " + nump);
        System.out.println("Num physical dependency individuals: " + numd);
        System.out.println("Summed individuals: " + (numd + nump) + "\n");
	}
	
	
	public AnalysisResults performQualitativeAnalysis(String ultimaterooturi, BatonVal baton, AnalysisType analysistype) throws OWLException{
		
		System.out.println("\n\n*** Starting analysis ***");
		
		if(inferredAxiomsOntology == null){
			System.err.println("Please load an ontology.");
			return null;
		}
			
		Analyzer a;
		if(analysistype == AnalysisType.DOWNSTREAM)
			a = new DownstreamAnalyzer(inferredAxiomsOntology, ultimaterooturi, baton); 
		
		else
			a = new UpstreamAnalyzer(inferredAxiomsOntology, ultimaterooturi, baton);
		
		a.incrementAnalysis(baton, ultimaterooturi, "");
		a.getAnalysisResults().compileResults();
		
		return a.getAnalysisResults();
	}
	
	
	// Copied/edited from SemSimOWLreader
	public static Set<OWLAnnotation> getAnnotationsForProcessParticipant(OWLOntology ont, String process, String prop, String ent){
		Set<OWLAnnotation> returnset = new HashSet<OWLAnnotation>();
		
		OWLIndividual procind = factory.getOWLNamedIndividual(IRI.create(process));
		OWLIndividual entind = factory.getOWLNamedIndividual(IRI.create(ent));
		OWLObjectProperty owlprop = factory.getOWLObjectProperty(IRI.create(prop));
		OWLAxiom axiom = factory.getOWLObjectPropertyAssertionAxiom(owlprop, procind, entind);
		
		OWLAnnotationProperty annprop = factory.getOWLAnnotationProperty(SemSimRelation.HAS_MULTIPLIER.getIRI());
		for(OWLAxiom ax : ont.getAxioms(procind)){
			
			if(ax.equalsIgnoreAnnotations(axiom))
				returnset.addAll(ax.getAnnotations(annprop));
			
		}
		return returnset;
	}
	
	
	private void addPropertyIndividual(DataStructure ds, String modelns, String depindividual) throws OWLException{
		
 		 PhysicalPropertyInComposite prop = ds.getPhysicalProperty();
		 OWLClass OPBparentClass = factory.getOWLClass(IRI.create(prop.getPhysicalDefinitionURI()));
		 String OPBparentClassString = OPBparentClass.getIRI().toString();
		 
		 String propertyURI = modelns + ds.getName() + "_property";
		 
		 SemSimOWLFactory.createSemSimIndividual(semsimDepOnt, propertyURI, OPBparentClass, "", manager);
		 
		 // If the data structure that has the property is an input to the dependency individual
		 // and not solved by the dependency itself, assert hasPropertyPlayer object property
		 SemSimRelation haspropplayer = depindividual.equals(modelns + ds.getName() + "_dependency") ? 
				 SemSimRelation.HAS_SOLVED_PROPERTY_PLAYER : 
					 SemSimRelation.HAS_PROPERTY_PLAYER;
				 
		 SemSimOWLFactory.setIndObjectProperty(semsimDepOnt, depindividual, propertyURI,
				 haspropplayer, null, manager);
		 
		 // Get what it's a property of and add it to the semsimDepOnt
		 if(ds.hasAssociatedPhysicalComponent()){
			 
			 String physpropOfURI = SemSimOWLFactory.getFunctionalIndObjectPropertyObject(semsimOrigOnt, 
					 propertyURI, SemSimRelation.PHYSICAL_PROPERTY_OF.getIRI().toString());
			 
			 OWLClass parentclassforpropof = (ds.getAssociatedPhysicalModelComponent() instanceof PhysicalProcess) ? 
					 OPBdynamicalProcessClass : OPBdynamicalEntityClass;
			 
			 SemSimOWLFactory.createSemSimIndividual(semsimDepOnt, physpropOfURI, 
					 parentclassforpropof, "", manager);
			 
			 SemSimOWLFactory.setIndObjectProperty(semsimDepOnt, propertyURI, physpropOfURI, 
					 SemSimRelation.PHYSICAL_PROPERTY_OF, SemSimRelation.HAS_PHYSICAL_PROPERTY,
					 manager);
		 }
		 
		 // Increment the number of times the physical property is used in the dependency
		 Integer count = 0;
		 if(propClassCountMap.containsKey(OPBparentClassString))
			 count = propClassCountMap.get(OPBparentClassString);
		 
		 propClassCountMap.put(OPBparentClassString, count + 1);	
		 
	}
	
	private void addAxiomToSemSimDepOnt(OWLAxiom axiom){
		AddAxiom addAxiom = new AddAxiom(semsimDepOnt, axiom);
		 manager.applyChange(addAxiom);
	}
	
	private OWLOntology addAxiomsToOntology(Set<OWLAxiom> axioms, OWLOntology destinationOnt){
		for(OWLAxiom ax : axioms){
	    	AddAxiom addAxiom = new AddAxiom(destinationOnt, ax);
	 		manager.applyChange(addAxiom);
	     }
		
		return destinationOnt;
	}
	
	public static BatonVal getOppositeBatonVal(BatonVal val){
		if(val==BatonVal.UP) return BatonVal.DOWN;
		else if(val==BatonVal.DOWN) return BatonVal.UP;
		else return null;
	}
}
