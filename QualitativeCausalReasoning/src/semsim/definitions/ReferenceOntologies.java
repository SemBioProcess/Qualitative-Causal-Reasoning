package semsim.definitions;

import java.net.URI;
import java.util.ArrayList;

import semsim.annotation.Ontology;
import semsim.owl.SemSimOWLFactory;

/**
 * Class that provides methods for working with the preferred ontologies used to annotate
 * SemSim models.
 * @author max_neal
 *
 */
public class ReferenceOntologies {
	
	/**
	 * @return A list of theh reference ontologies commonly used in SemSim 
	 * model annotation.
	 */
	public static ArrayList<ReferenceOntology> getAllOntologies() {
		ArrayList<ReferenceOntology> allont = new ArrayList<ReferenceOntology>();
		for (ReferenceOntology ont : ReferenceOntology.values()) {
			allont.add(ont);
		}
		return allont;
	}
	
	/**
	 * @param name The full name of one of the enumerated ReferenceOntologies (e.g. Chemical Entities of Biological Interest)
	 * @return A ReferenceOntology object corresponding to the name parameter. Returns
	 * ReferenceOntology.UNKONWN if the name is not associated with one of the enumerated
	 *  ReferenceOntologies.
	 */
	public static ReferenceOntology getReferenceOntologybyFullName(String name) {
		for (ReferenceOntology ro : ReferenceOntology.values()) {
			if (ro.getFullName().equals(name)) return ro; 
		}
		return ReferenceOntology.UNKNOWN;
	}
	
	/**
	 * @param namespace A namespace associated with one of the enumerated ReferenceOntologies.
	 * @return A ReferenceOntology object corresponding to the namespace parameter.
	 * Returns ReferenceOntology.UNKNOWN if the namespace is not associated with one
	 * of the enumerated ReferenceOntologies.
	 */
	public static ReferenceOntology getReferenceOntologyByNamespace(String namespace) {
		for (ReferenceOntology ro : ReferenceOntology.values()) {
			if (ro.hasNamespace(namespace)) return ro;
		}
		return ReferenceOntology.UNKNOWN;
	}
	
	/**
	 * @param namespace A namespace associated with one of the enumerated ReferenceOntologies.
	 * @return An {@link Ontology} object corresponding to the namespace parameter.
	 * Returns ReferenceOntology.UNKNOWN if the namespace is not associated with one
	 * of the enumerated ReferenceOntologies.
	 */
	public static Ontology getOntologyByNamespace(String namespace) {
		for (ReferenceOntology ro : ReferenceOntology.values()) {
			if (ro.hasNamespace(namespace)) return ro.getAsOntology();
		}
		return ReferenceOntology.UNKNOWN.getAsOntology();
	}
	
	/**
	 * @param uri The URI of a class from one of the enumerated {@link ReferenceOntology}s.
	 * @return A ReferenceOntology object representing the ontology asociated with the namespace of the URI.
	 * Returns ReferenceOntology.UNKNOWN if the URI's namespace is not associated with one
	 * of the enumerated ReferenceOntologies.
	 */
	public static ReferenceOntology getReferenceOntologyByURI(URI uri){
		String ns = SemSimOWLFactory.getNamespaceFromIRI(uri.toString());
		return getReferenceOntologyByNamespace(ns);
	}
	
	/**
	 * @param uri1 Class URI from one ReferenceOntology.
	 * @param uri2 Class URI from another ReferenceOntology.
	 * @return Whether the URIs represent classes from same ontology.
	 */
	public static boolean URIsAreFromSameReferenceOntology(URI uri1, URI uri2){
		ReferenceOntology ont1 = ReferenceOntologies.getReferenceOntologyByURI(uri1);
		ReferenceOntology ont2 = ReferenceOntologies.getReferenceOntologyByURI(uri2);
		
		return ((ont1==ont2) && (ont1 != ReferenceOntology.UNKNOWN));
	}
	
	/**
	 * Enumeration that delineates which ontologies should be used for the components
	 * of SemSim model annotations.
	 * 
	 */
	public enum OntologyDomain {
		AssociatePhysicalProperty(new ReferenceOntology[]{ReferenceOntology.OPB}),
		PhysicalProperty(new ReferenceOntology[]{ReferenceOntology.OPB, ReferenceOntology.PATO, 
				ReferenceOntology.SBO, ReferenceOntology.SNOMED}),
		PhysicalEntity(new ReferenceOntology[]{ReferenceOntology.CHEBI, ReferenceOntology.CL, ReferenceOntology.FMA, ReferenceOntology.GO,
				ReferenceOntology.MA, ReferenceOntology.OBI, ReferenceOntology.PR, ReferenceOntology.UNIPROT}),
		PhysicalProcess(new ReferenceOntology[]{ReferenceOntology.OPB, ReferenceOntology.GO});
		
		private ArrayList<ReferenceOntology> domainontologies = new ArrayList<ReferenceOntology>();
		private OntologyDomain(ReferenceOntology[] onts) {
			for (ReferenceOntology ont : onts) {
				domainontologies.add(ont);
			}
		}
		
		/**
		 * @return The list of all ontologies recommended for the types of annotations in the OntologyDomain.
		 */
		public ArrayList<ReferenceOntology> getDomainOntologies() {
			return domainontologies;
		}
		
		/**
		 * @return The full names of all ontologies recommended for the types of annotations in the OntologyDomain.
		 */
		public String[] getArrayofOntologyNames() {
			String[] names = new String[domainontologies.size()];
			for (int i = 0; i<domainontologies.size(); i++) {
				names[i] = domainontologies.get(i).getFullName() + " (" + domainontologies.get(i).getNickName() + ")";
			}
			return names;
		}
		
		/**
		 * @param index An integer corresponding to one of the recommended ReferenceOntologies in the OntologyDomain.
		 * @return The ReferenceOntology object in the list of recommended ReferenceOntologies
		 * at the specified index. 
		 */
		public ReferenceOntology getDomainOntologyatIndex(int index) {
			return domainontologies.get(index);
		}
		
		/**
		 * @param ont The ReferenceOntology object that we want the index of
		 * @return The index of the ReferenceOntology in the list of recommended ReferenceOntologies.
		 */
		public int getOrdinalofOntology(ReferenceOntology ont) {
			return domainontologies.indexOf(ont);
		}

		/**
		 * @param ont A ReferenceOntology that will be checked to see if it is appropriate for
		 * use within the OntologyDomain.
		 * @return Whether the ReferenceOntology is recommended for use within the OntologyDomain.
		 */
		public boolean domainHasReferenceOntology(ReferenceOntology ont) {
			if (ont==null) return false;
			return domainontologies.contains(ont);
		}
	};
	
	/**
	 * Enumeration of the various ReferenceOntologies used in SemSim model annotation.
	 */
	public enum ReferenceOntology {
		CHEBI("Chemical Entities of Biological Interest", "CHEBI", "http://purl.obolibrary.org/obo/CHEBI",
				new String[]{"http://purl.org/obo/owl/CHEBI#","https://identifiers.org/chebi/", "http://identifiers.org/chebi/",
				"http://identifiers.org/obo.chebi/", "urn:miriam:obo.chebi:"}, 
				"atoms and small molecules"),
		CL("Cell Type Ontology", "CL", "http://purl.obolibrary.org/obo/CL",
				new String[]{"https://identifiers.org/cl/", "http://identifiers.org/cl/"},
				"non-mammalian cell types"),
		CMO("Clinical Measurement Ontology", "CMO", "http://purl.obolibrary.org/obo/CMO",
				new String[]{"http://purl.bioontology.org/ontology/CMO/",}, ""),
		FMA("Foundational Model of Anatomy", "FMA", "http://purl.org/sig/ont/fma/",
				new String[]{"http://purl.obolibrary.org/obo/FMA", "http://sig.biostr.washington.edu/fma3.0#", "http://sig.uw.edu/fma#", 
						"http://identifiers.org/fma/", "https://identifiers.org/fma/"},
				"macromolecular to organism-level anatomy"),
		GO("Gene Ontology", "GO", "http://purl.obolibrary.org/obo/GO",
				new String[]{"http://purl.org/obo/owl/GO#", "urn:miriam:obo.go:",
				"http://identifiers.org/go/", "http://identifiers.org/obo.go/", "https://identifiers.org/go/"},
				"macromolecular structures not represented in the FMA"),
		MA("Mouse Adult Gross Anatomy Ontology", "MA", "http://purl.obolibrary.org/obo/MA",
				new String[]{"http://purl.bioontology.org/ontology/MA", "http://purl.org/obo/owl/MA#", 
				"http://identifiers.org/ma/","https://identifiers.org/ma/"},
				"rodent-specific anatomy"),
		OBI("Ontology for Biomedical Investigations", "OBI", "http://purl.obolibrary.org/obo/OBI", 
				new String[]{"http://purl.bioontology.org/ontology/OBI","https://identifiers.org/obi/","http://identifiers.org/obi/"},
				"laboratory materials"),
		OPB("Ontology of Physics for Biology", "OPB", "http://bhi.washington.edu/OPB#",
				new String[]{"http://www.owl-ontologies.com/unnamed.owl#", "https://identifiers.org/opb/", "http://identifiers.org/opb/"},
				"physical properties and dependencies"),
		PATO("Phenotype and Trait Ontology", "PATO", "http://purl.obolibrary.org/obo/PATO",
				new String[]{"http://purl.org/obo/owl/PATO#","https://identifiers.org/pato/", "http://identifiers.org/pato/"},
				"phenotypes and traits not represented as properties in the OPB"),
		PR("Protein Ontology", "PR", "http://purl.obolibrary.org/obo/PR",
				new String[]{"http://purl.obolibrary.org/obo/PR","https://identifiers.org/pr/","http://identifiers.org/pr/"},
				"species-agnostic proteins"),
		SBO("Systems Biology Ontology", "SBO", "http://purl.obolibrary.org/obo/SBO",
				new String[]{"http://biomodels.net/SBO/", "http://purl.org/obo/owl/SBO#","https://identifiers.org/sbo/","http://identifiers.org/sbo/"},
				"physical dependencies not in the OPB"),
		SNOMED("SNOMED - Clinical Terms", "SNOMEDCT", "http://purl.bioontology.org/ontology/SNOMEDCT/",
				new String[]{},
				"clinical-domain physical properties not in the OPB"),
		UNIPROT("Universal Protein Resource", "UNIPROT", "",
				new String[]{"http://purl.uniprot.org/uniprot/", "https://identifiers.org/uniprot/", "http://identifiers.org/uniprot/","http://www.uniprot.org/uniprot/"},
				"species-specific proteins"),
		UNKNOWN("Unkown Ontology", "?", "", new String[]{}, "") ;
		
		private String fullname;
		private String nickname;
		private String bioportalnamespace = null;
		private ArrayList<String> namespaces = new ArrayList<String>();
		private String description;
		private Ontology ontology;
		
		private ReferenceOntology(String name, String abrev, String bpns, String[] ns, String desc) {
			fullname = name;
			nickname = abrev;
			bioportalnamespace = bpns;
			
			if( ! bioportalnamespace.isEmpty()) namespaces.add(bioportalnamespace);
			
			description = desc;
			
			for (String s : ns) {
				namespaces.add(s);
			}
			ontology = new Ontology(this);
		}
		
		/**
		 * @param nspace A namespace that may or may not correspond to a ReferenceOntology
		 * @return Whether the namespace corresponds with the ReferenceOntology
		 */
		public boolean hasNamespace(String nspace) {
			for (String ns : namespaces) {
				if (nspace.equals(ns)) return true; 
			}
			return false;
		}
		
		/**
		 * @return Full name of ReferenceOntology
		 */
		public String getFullName() {
			return new String(fullname);
		}
		
		/**
		 * @return Nickname of ReferenceOntology
		 */
		public String getNickName() {
			return new String(nickname);
		}
				
		/**
		 * @return Free-text description indicating scope of ReferenceOntology
		 */
		public String getDescription() {
			return description;
		}
		
		/**
		 * @return The BioPortal namespace of the ReferenceOntology
		 */
		public String getBioPortalNamespace(){
			return bioportalnamespace;
		}
		
		/** @return The namespaces associated with the ReferenceOntology */
		public ArrayList<String> getNamespaces() {
			return namespaces;
		}
		
		/** Cast the ReferenceOntology as an {@link Ontology} 
		 * @return The ReferenceOntology cast as an {@link Ontology}*/
		public Ontology getAsOntology() {
			return ontology;
		}
	}
}
