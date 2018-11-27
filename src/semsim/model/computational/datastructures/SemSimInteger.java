package semsim.model.computational.datastructures;

import semsim.definitions.SemSimTypes;
import semsim.model.computational.Computation;

/** A {@link DataStructure} that is assigned an integer value during simulation.*/
public class SemSimInteger extends DataStructure{

	/** Constructor where only the name of the data structure is specified.
	 * Automatically associates this SemSimInteger with a new {@link Computation} class.
	 * @param name Name of data structure*/
	public SemSimInteger(String name){
		super(SemSimTypes.INTEGER);
		setName(name);
		setComputation(new Computation(this));
	}
	
	/** Constructor where the name of the data structure is specified along with the 
	 * associated {@link Computation} that specifies how the SemSimInteger is solved.
	 * @param name Name of the data structure
	 * @param computation Computation determining the SemSimInteger's value
	 */
	public SemSimInteger(String name, Computation computation){
		super(SemSimTypes.INTEGER);
		setName(name);
		setComputation(computation);
	}
	
	/**
	 * Copy constructor
	 * @param inttocopy The SemSimInteger to copy
	 */
	public SemSimInteger(SemSimInteger inttocopy) {
		super(inttocopy);
	}
	
	@Override
	public boolean isReal() {
		return true;
	}

	@Override
	public DataStructure copy() {
		return new SemSimInteger(this);
	}
}
