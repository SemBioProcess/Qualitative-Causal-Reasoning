package semgen.merging.workbench;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.units.UnitOfMeasurement;
import semsim.owl.SemSimOWLFactory;

public class ModelOverlapMap {
	Pair<Integer, Integer> modelindicies;
	private Set<String> identicalsubmodelnames;
	private Set<String> identicaldsnames;
	private ArrayList<Pair<DataStructure, DataStructure>> dsmap = new ArrayList<Pair<DataStructure, DataStructure>>();
	private HashMap<UnitOfMeasurement, UnitOfMeasurement> unitsmap = new HashMap<UnitOfMeasurement, UnitOfMeasurement>();
	
	private ArrayList<maptype> maptypelist = new ArrayList<maptype>();	
	private int slndomcnt = 0;
	
	protected static enum maptype {
		exactsemaoverlap("exact semantic match"), 
		manualmapping("manual mapping"),
		automapping("automated solution domain mapping");
		private String label;
		maptype(String lbl) {
			label = lbl;
		}
		public String getLabel() {
			return label;
		}
	}

	public ModelOverlapMap(int ind1, int ind2, SemanticComparator comparator) {
		modelindicies = Pair.of(ind1, ind2);
		ArrayList<Pair<DataStructure, DataStructure>> equivlist = comparator.identifyExactSemanticOverlap();		
		
		Pair<DataStructure, DataStructure> dspair;
		if (comparator.hasSolutionMapping()) {
			slndomcnt = 1;
			dspair = equivlist.get(0);
			addDataStructureMapping(dspair.getLeft(), dspair.getRight(), maptype.automapping);
		}
		
		for (int i=slndomcnt; i<(equivlist.size()); i++ ) {
			dspair = equivlist.get(i);
			addDataStructureMapping(dspair.getLeft(), dspair.getRight(), maptype.exactsemaoverlap);
		}
		unitsmap = comparator.identifyEquivalentUnits();
		identicalsubmodelnames = comparator.getIdenticalSubmodels();
		identicaldsnames = comparator.getIdenticalCodewords();
	}
	
	
	public String getMappingType(int index) {
		return maptypelist.get(index).getLabel();
	}
	
	public void addDataStructureMapping(DataStructure ds1, DataStructure ds2, maptype type) {
		dsmap.add(Pair.of(ds1, ds2));
		maptypelist.add(type);
	}
	
	public Set<String> getIdenticalSubmodelNames(){
		return identicalsubmodelnames;
	}
	
	public Set<String> getIdenticalNames(){
		return identicaldsnames;
	}
	
	public Pair<Integer, Integer> getModelIndicies() {
		return modelindicies;
	}
	
	public Pair<String, String> getDataStructurePairNames(int index) {
		Pair<DataStructure, DataStructure> dspair = dsmap.get(index); 
		return getDataStructurePairNames(dspair);
	}
	
	public Pair<String, String> getDataStructurePairNames(Pair<DataStructure, DataStructure> dspair) { 
		return Pair.of(dspair.getLeft().getName(), dspair.getRight().getName());
	}
	
	public Boolean dataStructuresAlreadyMapped(DataStructure ds1, DataStructure ds2) {
		for (Pair<DataStructure, DataStructure> dspair : dsmap) {
			if (dspair.getLeft().equals(ds1) || dspair.getRight().equals(ds2)) return true;
		}
		return false;
	}
	
	public Boolean codewordsAlreadyMapped(String cdwd1uri, String cdwd2uri) {
		String cdwd1 = SemSimOWLFactory.getIRIfragment(cdwd1uri);
		String cdwd2 = SemSimOWLFactory.getIRIfragment(cdwd2uri);
		Pair<String, String> cwnpr = Pair.of(cdwd1, cdwd2);
		Boolean alreadymapped = false;
		while (!alreadymapped) {
			for (Pair<DataStructure, DataStructure> dsp : dsmap) {
				alreadymapped = compareDataStructureNames(cwnpr, getDataStructurePairNames(dsp));		
			}
			break;
		}
		return alreadymapped;
	}
	
	public int getMappingCount() {
		return dsmap.size();
	}
	
	public boolean compareDataStructureNames(Pair<String, String> namepair1, Pair<String, String> namepair2) {
		return (namepair1.getLeft().equals(namepair2.getLeft()) && 
				namepair1.getRight().equals(namepair2.getRight())) || 
				(namepair1.getLeft().equals(namepair2.getRight()) && 
				namepair1.getRight().equals(namepair2.getLeft()));
	}
	
	public Pair<DataStructureDescriptor,DataStructureDescriptor> getDSPairDescriptors(int index) {
		DataStructure ds1 = dsmap.get(index).getLeft();
		DataStructure ds2 = dsmap.get(index).getRight();
		DataStructureDescriptor dsd1 = new DataStructureDescriptor(ds1);
		DataStructureDescriptor dsd2 = new DataStructureDescriptor(ds2);
		return Pair.of(dsd1, dsd2);
	}
	
	public ArrayList<Pair<DataStructure, DataStructure>> getDataStructurePairs() {
		return dsmap;
	}
	
	public HashMap<UnitOfMeasurement, UnitOfMeasurement> getEquivalentUnitPairs() {
		return unitsmap;
	}
	
	//Compare units of all Data Structures in the overlap map. Determine if terms are equivalent
	// for each. Return a list of comparisons
	public ArrayList<Boolean> compareDataStructureUnits() {
		ArrayList<Boolean> unitmatchlist = new ArrayList<Boolean>();
		for (Pair<DataStructure, DataStructure> dsp : dsmap) {
			boolean unitsmatch = true;
			if(dsp.getLeft().hasUnits() && dsp.getRight().hasUnits()){
				UnitOfMeasurement uomleft = dsp.getLeft().getUnit();
				UnitOfMeasurement uomright = dsp.getRight().getUnit();
				
				unitsmatch = unitsmap.containsKey(uomleft);
				if(unitsmatch){
					unitsmatch = unitsmap.get(uomleft).equals(uomright);
				}
			}
			unitmatchlist.add(unitsmatch);
		}
		return unitmatchlist;
	}
	

	
	public int getSolutionDomainCount() {
		return slndomcnt;
	}
}
