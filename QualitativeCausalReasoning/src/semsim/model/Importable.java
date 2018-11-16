package semsim.model;

/**
 * Interface providing methods for importing
 * SemSim model elements
 * 
 */
public interface Importable {

	/** @return The imported element's local name, i.e. the name that is used within the SemSim model. */
	public String getLocalName();
	
	/**
	 * Set the imported element's local name, i.e. the name that is used within the SemSim model.
	 * @param name The local name for the element
	 */
	public void setLocalName(String name);
	
	/** @return The imported element's referenced name, i.e. its name in the model from which it was imported */
	public String getReferencedName();
	
	/** Set the imported element's referenced name, i.e. its name in the model from which it was imported 
	 * @param name Imported element's referenced name
	 */
	public void setReferencedName(String name);
	
	/** @return True if the SemSim model element is imported from another model, otherwise false */
	public boolean isImported();
	
	/** Set whether the SemSim model element is imported from another model 
	 * @param val whether the SemSim model element is imported from another model 
	 */
	public void setImported(boolean val);
	
	/**
	 * @return The href value that indicates the location of the model from which this 
	 * SemSim model element was imported
	 */
	public String getHrefValue();
	
	/**
	 * Set the href value that indicates the location of the model from which this 
	 * SemSim model element was imported
	 * @param hrefVal Href value indicating the location of the model from which this 
	 * SemSim model element was imported
	 */
	public void setHrefValue(String hrefVal);
	
	/**
	 * @return If the SemSim model element is an import of an import, this returns 
	 * its most direct parent import, otherwise null is returned
	 */
	public Importable getParentImport();
	
	/**
	 * Use this to set the direct parent import of a SemSim model element that 
	 * is imported by virtue of another import.
	 * @param parent The parent import
	 */
	public void setParentImport(Importable parent);
}
