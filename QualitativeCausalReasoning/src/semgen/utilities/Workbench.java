/**
 * A Workbench in SemGen is a class where all operations dealing with a SemSim model are performed.
 * Rather then directly acting on a model, a task uses a workbench and its helper classes as an 
 * intermediary. 
 */

package semgen.utilities;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Observable;
import java.util.Observer;

import semsim.reading.ModelAccessor;

public abstract class Workbench extends Observable implements Observer, PropertyChangeListener{
	public abstract void initialize();
	
	public boolean getModelSaved() {
		return false;
	}
	
	public abstract File saveModel();
	
	public abstract File saveModelAs();
	
	public abstract void setModelSaved(boolean val);	
	
	public abstract String getCurrentModelName();
	
	public abstract ModelAccessor getModelSourceLocation();
	
	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		
	}
	
}
